package com.shiver.polymorphlegacy.mixin.core;

import java.util.List;
import net.minecraft.potion.PotionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PotionHelper.class)
public interface AccessorPotionHelper {
    @Accessor("POTION_TYPE_CONVERSIONS")
    static List<Object> polymorph$getTypeConversions() {
        throw new AssertionError();
    }

    @Accessor("POTION_ITEM_CONVERSIONS")
    static List<Object> polymorph$getItemConversions() {
        throw new AssertionError();
    }
}
