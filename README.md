# Well Seasoned

Well Seasoned is a Minecraft food overhaul inspired by ingredient-driven
adventure-game cooking. It currently targets Minecraft 1.21.1 on NeoForge.

## Gameplay

- The hunger HUD, exhaustion, starvation, and hunger-based natural regeneration
  are disabled.
- Sprinting is always available.
- Food may always be eaten and restores health directly.
- Twelve ingredient families have predictable `intrinsics`.
- Simple, preserved, meal, and special-meal tiers increase healing and effect
  duration.
- Forty authored foods are included.
- All heat-processing recipes use the vanilla smoker, including the four
  expedition meals.
- Recipe chains unlock as their defining ingredients and preparations are
  discovered.

The internal food level remains full instead of being removed. This preserves
compatibility with vanilla movement and systems that expect `FoodData` to
exist.

## Cooking data

Cooking balance is loaded from:

```text
data/<namespace>/well_seasoned/cooking/*.json
```

Catalogs may contain `intrinsics`, `foods`, or both. The complete set is
validated before it replaces the last successful reload.

An intrinsic defines one or more effects:

```json
{
  "id": "example:fortifying",
  "translation": "intrinsic.example.fortifying",
  "color": "#E8952E",
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
```

A food binds a registered item to a preparation tier and intrinsic:

```json
{
  "item": "example:pumpkin_jam",
  "tier": "preserved",
  "healing": 3,
  "intrinsics": ["example:fortifying"]
}
```

Healing is measured in health points, where two points equal one heart.
Durations are measured in ticks.

## Development

Build the current target:

```shell
./gradlew :1.21.1-neoforge:build
```

Start a client:

```shell
./gradlew :1.21.1-neoforge:runClient
```

Start a development server:

```shell
./gradlew :1.21.1-neoforge:runServer
```

The 1.20.1 Fabric port is scaffolded but has not yet been brought to feature
parity.
