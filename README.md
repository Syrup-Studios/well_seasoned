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
- The same data works with vanilla items and items from other mods.

An unconfigured food restores half of its vanilla nutrition value as health,
with a minimum of one health point. Two health points equal one heart.

## Supported targets

| Minecraft | Mod loader | Java |
| --- | --- | --- |
| 1.20.1 | Fabric | 17 |
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
      "intrinsics": [
        "example:fortifying"
      ]
    }
  ]
}
```

Run `/reload` after you change a catalog. Well Seasoned validates all catalogs
as one set. If any catalog is invalid, the reload fails and the last valid set
stays active. Intrinsic IDs and food item IDs must be unique across the full
set.

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

### Food fields

| Field | Required | Description |
| --- | --- | --- |
| `item` | Yes | Namespaced ID of an existing food item. |
| `tier` | Yes | One of the four preparation tiers. |
| `healing` | Yes | Base health points from `0` through `40`. |
| `intrinsics` | Yes | One or more intrinsic IDs. |

## Build from source

Use the included Gradle wrapper. The toolchain resolver can download the
required JDK when needed.

```bash
./gradlew :1.20.1-fabric:buildAndCollect \
  :1.21.1-neoforge:buildAndCollect
```

The JARs and source JARs are written to:

```text
build/libs/<mod-version>/
```

To run a development client, use one of these commands:

```bash
./gradlew :1.20.1-fabric:runClient
./gradlew :1.21.1-neoforge:runClient
```

## Links

- [Source code](https://github.com/Syrup-Studios/well_seasoned)
- [Issue tracker](https://github.com/Syrup-Studios/well_seasoned/issues)
