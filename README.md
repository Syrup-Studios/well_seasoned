# Well Seasoned

Well Seasoned replaces Minecraft's hunger loop with direct healing and
data-driven food effects. It is inspired by ingredient-based cooking systems like in Breath of the Wild and the Matcha Flavoured Datapack.

The mod supplies the food system. Modpacks and datapacks supply the food
profiles.

## What changes

- The hunger HUD is hidden.
- Hunger, exhaustion, starvation, and hunger-based regeneration are disabled.
- Players can always sprint and eat.
- Unconfigured food restores health instead of hunger.
- Datapacks can set the healing, status effects, and preparation tier of any
  existing food item.
- Food tooltips show the final heart value and configured status effects.
- The same data works with vanilla items and items from other mods.

An unconfigured food restores half of its vanilla nutrition value as health,
with a minimum of one health point. Two health points equal one heart.

## Supported targets

| Minecraft | Mod loader | Java |
| --- | --- | --- |
| 1.21.1 | Fabric | 21 |
| 1.21.1 | NeoForge | 21 |

Fabric also requires Fabric API. Install the mod on both the client and server.

## Datapack setup

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

There are 20 ticks in one second. The tier multiplier is applied before the
duration and amplifier caps.

If a player already has the same effect at the same level, eating the food adds
half of the new duration, with a minimum addition of 20 ticks. The result
cannot exceed `maximum_duration`. A weaker food effect does not replace a
stronger active effect.

When several food sources supply the same effect, the strongest amplifier
wins. Effects with the same amplifier add their durations. The result cannot
exceed the largest `maximum_duration` from those sources. Built-in item effect
probabilities are preserved.

### Food fields

| Field | Required | Description |
| --- | --- | --- |
| `item` | Conditional | Namespaced ID of an existing food item. Use either `item` or `tag`. |
| `tag` | Conditional | Namespaced ID of an item tag. Use either `tag` or `item`. |
| `tier` | Yes | One of the four preparation tiers. |
| `healing` | Yes | Base health points from `0` through `40`. |
| `mode` | No | `append` keeps built-in item effects. `replace` removes them. The default is `append`. |
| `intrinsics` | Yes | One or more intrinsic IDs. |

## Build from source

Use the included Gradle wrapper. The toolchain resolver can download the
required JDK when needed.

```bash
./gradlew :1.21.1-fabric:buildAndCollect \
  :1.21.1-neoforge:buildAndCollect
```

The JARs and source JARs are written to:

```text
build/libs/<mod-version>/
```

To run a development client, use one of these commands:

```bash
./gradlew :1.21.1-fabric:runClient
./gradlew :1.21.1-neoforge:runClient
```

## Links

- [Source code](https://github.com/Syrup-Studios/well_seasoned)
- [Issue tracker](https://github.com/Syrup-Studios/well_seasoned/issues)

## Planned features

The roadmap is split into three systems. These features are planned and are
not part of the current release unless another section says that they are
already supported.

### Food-effect compatibility

- Add configurable global defaults for effect combination behavior.
- Expose item tags and data formats for compatibility with other mods.
- Provide example data for fire-resistant food.

### Dish effect inheritance

- Detect the ingredients or recipe components of a dish, including food from
  Farmer's Delight and its add-ons.
- Let dishes inherit and combine ingredient effects when they do not have a
  specific food entry.
- Let pack makers override, disable, or replace inherited effects for a
  specific dish.
- Add configurable rules for duplicate effects and for duration and amplifier
  scaling.
- Add separate duration and strength multipliers for combined dishes. Values
  above `1.0` will be allowed. For example, a multiplier of `0.75` gives each
  inherited effect 75% efficiency.
- Support data-driven booster ingredients that make prepared meals stronger or
  longer-lasting than simple foods. For example, potato soup could give more
  fire resistance than a baked potato.
- Expose ingredient tags and data formats for compatibility with other mods.
- Validate ingredient data and provide example data for combined meals.

### Diet tracking and rewards

- Add data-driven food groups, such as fruit, vegetables, carbohydrates, meat,
  and protein.
- Derive the food groups of foods and dishes from their base ingredients and
  recipe components. One complete meal can count for several groups.
- Track and save each player's food-group consumption across a configurable
  in-game time window.
- Add configurable rewards for a varied diet. Possible rewards include more
  healing from food, temporary or permanent extra hearts, and a small movement
  speed bonus.
- Keep rewards small enough that effect-specific food stays useful. Rewards
  will be the default behavior.
- Add an optional penalty mode for an unbalanced diet. Possible penalties
  include reduced maximum health, reduced maximum movement speed, and negative
  status effects. Penalties will be disabled by default, and modpacks will be
  able to configure their time limits and strength.
- Support reward-only and reward-and-penalty rules.
- Expose food-group tags and data formats for compatibility with other mods.
- Validate food-group data and report invalid entries.

The suggested implementation order is:

1. Complete food-effect compatibility.
2. Add dish effect inheritance and balance rules.
3. Add food groups and consumption tracking.
4. Add balanced-diet rewards.
5. Add optional diet penalties.
