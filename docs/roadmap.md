# Planned features

[Back to README](../README.md)

The roadmap is split into three systems. These features are planned. They are
not part of the current release unless another document states that they are
already supported.

## Food-effect compatibility

- Add configurable global defaults for effect combination behavior.
- Expose item tags and data formats for compatibility with other mods.
- Provide example data for fire-resistant food.

## Dish effect inheritance

- Detect the ingredients or recipe components of a dish. This includes food
  from Farmer's Delight and its add-ons.
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

## Diet tracking and rewards

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
  status effects. Penalties will be disabled by default. Modpacks will be able
  to configure their time limits and strength.
- Support reward-only and reward-and-penalty rules.
- Expose food-group tags and data formats for compatibility with other mods.
- Validate food-group data and report invalid entries.

The suggested implementation order is:

1. Complete food-effect compatibility.
2. Add dish effect inheritance and balance rules.
3. Add food groups and consumption tracking.
4. Add balanced-diet rewards.
5. Add optional diet penalties.
