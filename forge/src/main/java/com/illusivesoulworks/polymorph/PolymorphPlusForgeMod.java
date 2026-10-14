/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph;

import net.minecraftforge.fml.common.Mod;

/**
 * Container for the polymorph_plus entry of mods.toml. Forge rejects a mods.toml entry with no
 * matching @Mod class (fml.modloading.missingclasses); the real mod runs under the polymorph alias.
 */
@Mod("polymorph_plus")
public class PolymorphPlusForgeMod {
}
