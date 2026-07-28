# Well Seasoned

Well Seasoned is a Minecraft food overhaul inspired by ingredient-driven
adventure-game cooking.

## Gameplay

- The hunger HUD, exhaustion, starvation, and hunger-based natural regeneration
  are disabled.
- Sprinting is always available.
- Vanilla food may always be eaten and restores health directly.
- The mod registers no custom food items or recipes.
- Datapacks may optionally assign healing and effects to existing food items.

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

A food profile binds an existing item to a preparation tier and intrinsic:

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
