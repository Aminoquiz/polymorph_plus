[![Support me by hosting a minecraft server on Wabbanode ! (prices start as low as 1.49$USD / month)](https://cdn.modrinth.com/data/cached_images/101574d2252d501a181cc771473884e9d4b81a63.png)](https://wabbanode.com/affiliate/amine)

# Polymorph+

[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?&style=flat-square)](https://www.gnu.org/licenses/lgpl-3.0)
[![Modrinth](https://img.shields.io/badge/Modrinth-polymorph__plus-00AF5C?style=flat-square&logo=modrinth&logoColor=white)](https://modrinth.com/mod/polymorph_plus)
[![CurseForge](https://img.shields.io/badge/CurseForge-polymorph__plus-F16436?style=flat-square&logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/polymorph-plus)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.1.2-62B47A?style=flat-square)](https://neoforged.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-26.1.2.x-D7742F?style=flat-square)](https://neoforged.net/)
[![ko-fi](https://img.shields.io/badge/Support%20Me-Ko--fi-%23FF5E5B?style=flat-square)](https://ko-fi.com/aminoquiz)

An **unofficial port of [Polymorph](https://github.com/illusivesoulworks/polymorph)** by TheIllusiveC4 (Illusive Soulworks) to **Minecraft 26.1.2 / NeoForge 26.1.2.x**, the new year-based versioning line that replaces the old `1.22` scheme. Mod id is `polymorph_plus` since `polymorph` is reserved for upstream. Same behavior, just compiled for the new MC line.

> ⚠️ **Beta.** NeoForge 26.1.2 itself is in beta and so is this port. Back up your worlds before loading it. Bug reports and logs are welcome on the [issue tracker](https://github.com/Aminoquiz/polymorph_plus/issues).

Polymorph+ solves recipe conflicts by letting players choose between all potential outputs that share the same ingredients. Instead of forcing modpack authors to datapack them away one by one, every conflicting recipe co-exists and the player makes the final pick.

## Features

### Crafting

![](https://i.ibb.co/TkWswkG/polymorph.gif)

When a group of ingredients matches more than one recipe, a button appears above the output slot. Clicking it shows the list of all possible results. Select one and the crafting output switches to match. The last selection is remembered as long as the ingredients don't change, so repeated crafts on the same selection keep working.

### Smelting

![](https://i.ibb.co/QX9MNYM/polymorph-furnacedemo.gif)

When a valid input matches more than one output, a button appears above the output slot. Clicking it shows the possible results with the current selection highlighted. The choice is saved to the block itself and persists across world load / unload.

### Smithing

> 🚧 Stubbed in this beta. The vanilla `SmithingMenu` / `ItemCombinerMenu` pipeline was rewritten in 26.1 and the selection UI will return in a follow-up release.

### Commands

`/polymorph conflicts` scans crafting, smelting, blasting, smoking, and smithing recipes and dumps a list of detected conflicts to the logs folder.

## Downloads

- **Modrinth**: [modrinth.com/mod/polymorph_plus](https://modrinth.com/mod/polymorph_plus)
- **CurseForge**: [curseforge.com/minecraft/mc-mods/polymorph-plus](https://www.curseforge.com/minecraft/mc-mods/polymorph-plus)

## Addons

Companion mods that bring Polymorph compatibility to mods with their own crafting menus.

[![Polymorphic Occultism](https://img.shields.io/badge/ADDON-POLYMORPHIC%20OCCULTISM-purple?style=for-the-badge)](https://modrinth.com/mod/polymorphic-occultism)
[![Polymorphic Extended Crafting](https://img.shields.io/badge/ADDON-POLYMORPHIC%20EXTENDED%20CRAFTING-lightgreen?style=for-the-badge)](https://modrinth.com/mod/polymorphic-extended-crafting)
[![Polymorphic Refined Storage](https://img.shields.io/badge/ADDON-POLYMORPHIC%20RS-lightblue?style=for-the-badge)](https://modrinth.com/mod/polymorphic-refined-storage)

## Partners

[![Host your Minecraft server on Wabbanode (from 1.49 USD/month), use code AMINE](https://cdn.modrinth.com/data/cached_images/101574d2252d501a181cc771473884e9d4b81a63.png)](https://wabbanode.com/affiliate/amine)

Hosting your server with [Wabbanode](https://wabbanode.com/affiliate/amine) (code `AMINE`, from 1.49 USD/month) helps keep these mods going.

## Building from source

```
./gradlew :neoforge:jar
```

Produces `neoforge/build/libs/polymorph_plus-neoforge-26.1.2-<version>.jar`. A `:fabric:` subproject is checked in but parked in `settings.gradle`; re-enable once Mojang publishes official Mojmaps for 26.1.2 (Yarn doesn't ship 26.1.x yet either, so Loom can't decompile vanilla without them).

`conflict-tester/` is a tiny companion mod that duplicates the diamond-pickaxe recipe, useful for verifying the selection UI surfaces.

## Support

- Issues: [github.com/Aminoquiz/polymorph_plus/issues](https://github.com/Aminoquiz/polymorph_plus/issues)
- Original mod issues (upstream): [illusivesoulworks/polymorph/issues](https://github.com/illusivesoulworks/polymorph/issues)

## License

All source code and assets are licensed under **LGPL-3.0-or-later**, matching upstream. This port is unofficial and not endorsed by the original author.

## Credits

- Original mod by [TheIllusiveC4](https://github.com/illusivesoulworks). [Upstream repo](https://github.com/illusivesoulworks/polymorph).
- 26.1.2 port by [Aminoquiz](https://github.com/Aminoquiz).

## Donations

Help keep these mods going via [ko-fi.com/aminoquiz](https://ko-fi.com/aminoquiz).
