package com.illusivesoulworks.polymorph.mixin.integration.hephaestus;

import com.illusivesoulworks.polymorph.common.integration.hephaestus.HephaestusModule;
import java.util.List;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// The tanks cap a pour at the amount of the recipe they match, so they must match the pick too.
@SuppressWarnings({"unused", "rawtypes"})
@Pseudo
@Mixin(targets = {"com.titammods.block.TableBlockEntity$3", "com.titammods.block.BasinBlockEntity$2"},
    remap = false)
public abstract class MixinCastingTank {

  @Redirect(method = "fill", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/item/crafting/RecipeManager;getAllRecipesFor(Lnet/minecraft/world/item/crafting/RecipeType;)Ljava/util/List;"))
  private List polymorph$orderCasting(RecipeManager manager, RecipeType type) {
    List recipes = manager.getAllRecipesFor(type);
    BlockEntity owner = HephaestusModule.owner(this);
    return owner == null ? recipes
        : HephaestusModule.order(recipes, owner.getLevel(), owner.getBlockPos());
  }
}
