[![Support me by hosting a minecraft server on Wabbanode ! (prices start as low as 1.49$USD / month) ](https://cdn.modrinth.com/data/cached_images/101574d2252d501a181cc771473884e9d4b81a63.png)](https://wabbanode.com/affiliate/amine)


<div><h1 style="font-size:2.5em">Polymorph+</h1><p>


<a href="https://modrinth.com/mod/polymorphic-occultism" rel="nofollow"> <img src="https://img.shields.io/badge/ADDON-POLYMORPHIC%20OCCULTISM-purple?style=for-the-badge" alt="Same compatibility mod but for Occultism!"></a>
<a href="https://modrinth.com/mod/polymorphic-extended-crafting" rel="nofollow"> <img src="https://img.shields.io/badge/ADDON-POLYMORPHIC%20EXTENDED%20CRAFTING-lightgreen?style=for-the-badge" alt="Same compatibility mod but for Extended Crafting!"></a>
<a href="https://modrinth.com/mod/polymorphic-refined-storage" rel="nofollow"> <img src="https://img.shields.io/badge/ADDON-POLYMORPHIC%20RS-lightblue?style=for-the-badge" alt="Same compatibility mod but for Refined Storage!"></a>

</p></div>


An **unofficial port of [Polymorph](https://www.curseforge.com/minecraft/mc-mods/polymorph)** by TheIllusiveC4 (Illusive Soulworks) to **Minecraft 26.1.2 / NeoForge 26.1.2.x** — the new year-based versioning line that replaces the old `1.22` scheme. Mod id is `polymorph_plus` since `polymorph` is reserved for upstream; same behavior, just compiled for the new MC line.

> ⚠️ **Beta.** NeoForge 26.1.2 is itself still in beta. Every workstation has been verified end-to-end in singleplayer and on a dedicated server, but back up before you load this into an existing world. Bug reports and logs are very welcome.

Polymorph solves recipe conflicts by letting players choose between all potential outputs that share the same ingredients. With enough mods installed, recipe conflicts are a common occurrence; instead of forcing modpack authors to datapack them away one by one, Polymorph lets every conflicting recipe co-exist and gives the player the final pick.

## Features

### Crafting table
When a group of ingredients matches more than one recipe, a button appears above the output slot. Clicking it shows the list of all possible results, click one and the crafting output switches to match. The last selection is remembered as long as the ingredients don't change. Also works on the player inventory 2x2 grid.

![Crafting table](craft.gif)

### Crafter (1.21+ block)
Same selector inside the vanilla Crafter screen. The selection is persisted on the block entity so redstone automation respects your pick across the whole world load. Pair multiple Crafters with different recipe locks to split outputs from shared ingredients.

![Crafter](crafter.gif)

![Crafter automation](crafters.gif)

### Smelting, blasting, smoking
Furnace, blast furnace, and smoker all surface the same selector above the output slot when an input has more than one possible result. The choice is saved to the block itself and persists across world load and unload.

![Furnace](furnace.gif)

### Smithing
Full support on 26.1. When a smithing transform input has more than one matching recipe, the selector appears next to the result slot. The choice is remembered across reopens.

![Smithing table](smithing.gif)

### "And more" beyond upstream

- **Scrollable selector when there are more than 7 conflicts.** Mouse wheel scrolls, side arrows browse. The panel lifts above the inventory area so it doesn't overlap JEI / REI side panels.

  ![Scrollable bar](scrollable_bar.gif)

- **Pinned panel.** Right click the open arrow to dock the selector open for the whole session (yellow corner dot indicates the pinned state). Left click the arrow again to unpin. Pin state is saved to `config/polymorph_plus_client.json`.
- **First run tutorial.** Four short hint bubbles guide a new player through the conflict, the selector, the pin shortcut, and the scroll affordance. Available in English and French, auto dismiss after 8 seconds or after the matching action.
- **JEI and REI compatibility.** The "+" fill button in JEI and the auto craft button in REI both work with polymorph_plus installed (recipe-clicked selection persistence is a known issue tracked for follow up, you can still pick manually).

### Commands
`/polymorph conflicts` scans crafting, smelting, blasting, smoking, and smithing recipes and dumps a list of detected conflicts to the logs folder.

## Credits & license

- Original mod by [TheIllusiveC4](https://www.curseforge.com/members/theillusivec4) — [upstream repo](https://github.com/illusivesoulworks/polymorph).
- This port is distributed under the same license as upstream: **LGPL-3.0-or-later**.
- All assets and source remain under that license; this port is unofficial and not endorsed by the original author.

***


If you want to help keep these mods going, check my ko-fi : [https://ko-fi.com/aminoquiz](https://ko-fi.com/aminoquiz), or consider hosting a server on wabbanode with my code AMINE (prices starts from 1.49 USD/month) !

Thank you <3

[![Support me by hosting a minecraft server on Wabbanode ! (prices start as low as 1.49$USD / month)](https://cdn.modrinth.com/data/cached_images/101574d2252d501a181cc771473884e9d4b81a63.png)](https://wabbanode.com/affiliate/amine)
