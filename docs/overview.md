# Overview

Well Seasoned replaces Minecraft's hunger loop with direct healing and
data-driven food effects. The mod supplies the food system. Modpacks and
datapacks supply the food profiles.

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

Fabric also requires Fabric API. Syrup Library provides the typed configuration
system. It is bundled inside the Well Seasoned JAR. You do not need to install
it separately. Install the mod and its required dependencies on both the client
and server.
