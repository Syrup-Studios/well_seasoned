# Datapack setup and catalog reference

[Back to README](../README.md)

Place cooking catalogs in this directory:

```text
<datapack>/
└── data/
    └── <namespace>/
        └── well_seasoned/
            └── cooking/
                └── <catalog>.json
```

Each catalog is one JSON object. It can contain an `intrinsics` array, a
`foods` array, or both. Definitions can refer to definitions in other catalog
files.

This complete example defines one intrinsic and assigns it to one food:

```json
{
  "intrinsics": [
    {
      "id": "example:fortifying",
      "effects": [
        {
          "id": "minecraft:resistance",
          "duration": 240,
          "amplifier": 0,
          "maximum_duration": 4800,
          "maximum_amplifier": 1,
          "ambient": false,
          "show_particles": true
        }
      ]
    }
  ],
  "foods": [
    {
      "item": "example:pumpkin_jam",
      "tier": "preserved",
      "healing": 3,
      "mode": "append",
      "intrinsics": [
        "example:fortifying"
      ]
    }
  ]
}
```

A food entry can select all food items in an item tag. Use `tag` instead of
`item`:

```json
{
  "foods": [
    {
      "tag": "example:fruit",
      "tier": "simple",
      "healing": 2,
      "intrinsics": [
        "example:refreshing"
      ]
    }
  ]
}
```

The tag uses the normal datapack item-tag format. For the example above, define
the tag at `data/example/tags/item/fruit.json`. Do not add a `#` before the tag
ID. Missing and empty tags are allowed. Non-food items in a tag are ignored.

An explicit `item` entry overrides a matching `tag` entry. If two configured
tags contain the same food and there is no explicit item entry, reload fails.

Run `/reload` after you change a catalog. Well Seasoned validates all catalogs
as one set. If any catalog is invalid, the reload fails and the last valid set
stays active. Intrinsic IDs, explicit food item IDs, and food tag IDs must be
unique across the full set.

## Preparation tiers

The tier changes the base healing and effect duration. A special meal also adds
one effect level. The configured maximum values still apply.

| Tier | Healing | Effect duration | Effect level |
| --- | ---: | ---: | ---: |
| `simple` | 1.00x | 1.00x | +0 |
| `preserved` | 1.35x | 2.50x | +0 |
| `meal` | 1.75x | 5.00x | +0 |
| `special_meal` | 2.00x | 8.00x | +1 |

For example, a preserved food with `healing: 3` restores 4.05 health points.

## Catalog reference

### Intrinsic fields

| Field | Required | Default | Description |
| --- | --- | --- | --- |
| `id` | Yes | — | Unique namespaced ID for the intrinsic. |
| `effects` | Yes | — | One or more effect objects. |

### Effect fields

| Field | Required | Default | Description |
| --- | --- | --- | --- |
| `id` | Yes | — | Namespaced mob-effect ID. |
| `duration` | No | `1` | Base duration in ticks. Must be positive. |
| `amplifier` | No | `0` | Base effect level, where `0` is level I. |
| `maximum_duration` | No | `24000` | Duration cap in ticks. |
| `maximum_amplifier` | No | `2` | Effect-level cap, where `2` is level III. |
| `ambient` | No | `false` | Uses the ambient effect style. |
| `show_particles` | No | `true` | Shows effect particles. |
| `chance` | No | `1.0` | Probability from `0.0` through `1.0` that the configured effect is applied when the food is consumed. |

There are 20 ticks in one second. The tier multiplier is applied before the
duration and amplifier caps.

A `chance` below `1.0` is rolled separately for each configured effect each time
the food is consumed. The roll happens server-side when the food is eaten. It
does not happen during loading, tooltip rendering, or profile creation. A failed
roll adds no effect. It does not merge durations or extend an active effect.
Healing always applies normally. This example has a 30% chance:

```json
{
  "id": "minecraft:nausea",
  "duration": 100,
  "amplifier": 0,
  "chance": 0.3
}
```

If a player already has the same effect at the same level, Well Seasoned adds a
reduced portion of the incoming duration. The reduction follows a logarithmic
curve based on the ratio between the active duration and the incoming duration.

The first application grants the full duration. Repeated stacking remains
beneficial but becomes progressively less efficient. The more duration a player
already has, the less the next food adds. Waiting for an effect to tick down
makes the next food more efficient again.

The final duration still cannot exceed `maximum_duration`. A weaker food effect
does not replace a stronger active effect.

When several food sources supply the same effect, the strongest amplifier wins.
Effects with the same amplifier add their durations. The result cannot exceed
the largest `maximum_duration` from those sources. Built-in item effect
probabilities are preserved. With `mode: "append"`, the built-in effect
probabilities of vanilla items stay intact alongside the configured effects.
For example, this applies to the 60% Poison chance of a poisonous potato.

### Food fields

| Field | Required | Description |
| --- | --- | --- |
| `item` | Conditional | Namespaced ID of an existing food item. Use either `item` or `tag`. |
| `tag` | Conditional | Namespaced ID of an item tag. Use either `tag` or `item`. |
| `tier` | Yes | One of the four preparation tiers. |
| `healing` | Yes | Base health points from `0` through `40`. |
| `mode` | No | `append` keeps built-in item effects. `replace` removes them. The default is `append`. |
| `intrinsics` | Yes | One or more intrinsic IDs. May be empty for a food that only restores health. |
