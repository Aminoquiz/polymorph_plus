package com.shiver.polymorphlegacy.compat;

import java.util.Arrays;
import java.util.List;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import zone.rong.mixinbooter.ILateMixinLoader;

public final class CompatibilityLoader implements ILateMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        return Arrays.asList("mixins.polymorph_plus.jei.json", "mixins.polymorph_plus.ae2.json",
                "mixins.polymorph_plus.ae2jei.json", "mixins.polymorph_plus.tconstruct.json",
                "mixins.polymorph_plus.craftingstation.json");
    }

    @Override
    public boolean shouldMixinConfigQueue(String config) {
        if ("mixins.polymorph_plus.tconstruct.json".equals(config)) {
            return Loader.isModLoaded("tconstruct");
        }
        if ("mixins.polymorph_plus.craftingstation.json".equals(config)) {
            return Loader.isModLoaded("craftingstation");
        }
        if ("mixins.polymorph_plus.ae2.json".equals(config)) {
            return Loader.isModLoaded("appliedenergistics2");
        }
        if ("mixins.polymorph_plus.ae2jei.json".equals(config)) {
            return FMLLaunchHandler.side().isClient() && Loader.isModLoaded("jei")
                    && Loader.isModLoaded("appliedenergistics2");
        }
        return "mixins.polymorph_plus.jei.json".equals(config)
                && FMLLaunchHandler.side().isClient() && Loader.isModLoaded("jei");
    }
}
