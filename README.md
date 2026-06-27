[![Wabbanode](https://cdn.modrinth.com/data/cached_images/101574d2252d501a181cc771473884e9d4b81a63.png)](https://wabbanode.com/affiliate/amine)

# Polymorph+

[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?&style=flat-square)](https://www.gnu.org/licenses/lgpl-3.0)
[![Modrinth](https://img.shields.io/badge/Modrinth-polymorph__plus-00AF5C?style=flat-square&logo=modrinth&logoColor=white)](https://modrinth.com/mod/polymorph_plus)
[![CurseForge](https://img.shields.io/badge/CurseForge-polymorph__plus-F16436?style=flat-square&logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/polymorph-plus)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-62B47A?style=flat-square)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.11.x-D7742F?style=flat-square)](https://neoforged.net/)
[![Fabric](https://img.shields.io/badge/Fabric-1.21.11-1976D2?style=flat-square)](https://fabricmc.net/)
[![ko-fi](https://img.shields.io/badge/Ko--fi-aminoquiz-FF5E5B?style=flat-square)](https://ko-fi.com/aminoquiz)

Unofficial port of [Polymorph](https://github.com/illusivesoulworks/polymorph) by TheIllusiveC4 (Illusive Soulworks) to **Minecraft 1.21.11** on **NeoForge** and **Fabric**. Mod id is `polymorph_plus` (the original `polymorph` id is reserved for upstream).

Polymorph+ resolves recipe conflicts by letting the player pick which output a given set of ingredients should produce. Conflicting recipes stay co-installed, you choose the result.

> Beta. Back up your worlds before loading the mod. Bug reports go to the [issue tracker](https://github.com/Aminoquiz/polymorph_plus/issues).

## Features

### Crafting table

![](craft.gif)

A button appears above the output slot when ingredients match more than one recipe. Click it, pick the result. The selection persists as long as the input stays the same.

### Crafter (1.21+ block)

![](crafter.gif)

Same selector inside the vanilla Crafter screen. The choice is saved on the block entity so redstone automation respects it across world reloads.

### Smelting (furnace, blast furnace, smoker)

![](furnace.gif)

Same selector above the output slot. The choice is saved on the block.

### Smithing

![](smithing.gif)

When a smithing transform input matches more than one recipe, the selector appears next to the result slot.

### Quality of life

![](scrollable_bar.gif)

- **Scrollable selector** when there are more than seven conflicts. Mouse wheel or side arrows.
- **Pinned panel.** Right click the open arrow to dock the selector for the session. Left click again to unpin. Pin state is saved to `config/polymorph_plus_client.json`.
- **First-run tutorial.** Four hint bubbles, available in English and French, auto-dismiss after eight seconds.
- **JEI and REI compatibility.** The "+" fill button (JEI) and auto-craft button (REI) work with Polymorph+ installed.

### Commands

`/polymorph conflicts` scans crafting, smelting, blasting, smoking, and smithing recipes and writes a list of detected conflicts to the logs folder.

## Downloads

- Modrinth: [modrinth.com/mod/polymorph_plus](https://modrinth.com/mod/polymorph_plus)
- CurseForge: [curseforge.com/minecraft/mc-mods/polymorph-plus](https://www.curseforge.com/minecraft/mc-mods/polymorph-plus)

## Addons

[![Polymorphic Occultism](https://img.shields.io/badge/ADDON-POLYMORPHIC%20OCCULTISM-purple?style=for-the-badge)](https://modrinth.com/mod/polymorphic-occultism)
[![Polymorphic Extended Crafting](https://img.shields.io/badge/ADDON-POLYMORPHIC%20EXTENDED%20CRAFTING-lightgreen?style=for-the-badge)](https://modrinth.com/mod/polymorphic-extended-crafting)
[![Polymorphic Refined Storage](https://img.shields.io/badge/ADDON-POLYMORPHIC%20RS-lightblue?style=for-the-badge)](https://modrinth.com/mod/polymorphic-refined-storage)

## Build

```
./gradlew :neoforge:build
./gradlew :fabric:build
```

Jars land in `neoforge/build/libs/` and `fabric/build/libs/`. The Fabric variant bundles Cardinal Components API. The `conflict-tester/` subproject builds a small companion datapack mod that adds many conflicting recipes (useful for smoke testing the selector).

## Partner

[![Wabbanode](https://cdn.modrinth.com/data/cached_images/101574d2252d501a181cc771473884e9d4b81a63.png)](https://wabbanode.com/affiliate/amine)

Host your Minecraft server on [Wabbanode](https://wabbanode.com/affiliate/amine) (code `AMINE`, from 1.49 USD/month).

## Credits and license

Original mod by [TheIllusiveC4](https://github.com/illusivesoulworks). Port maintained by [Aminoquiz](https://github.com/Aminoquiz). Source and assets are LGPL-3.0-or-later, matching upstream. This port is unofficial and not endorsed by the original author.

Support: [ko-fi.com/aminoquiz](https://ko-fi.com/aminoquiz).
