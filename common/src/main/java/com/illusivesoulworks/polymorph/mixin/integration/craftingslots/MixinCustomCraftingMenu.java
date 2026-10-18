package com.illusivesoulworks.polymorph.mixin.integration.craftingslots;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import java.util.Optional;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Pseudo
@Mixin(targets = "net.blay09.mods.craftingslots.container.CustomCraftingMenu", remap = false)
public abstract class MixinCustomCraftingMenu {

  @Shadow
  @Final
  private Inventory playerInventory;

  @Redirect(
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;",
          remap = true),
      // Pseudo target, no refmap entry for its override: method_7609 is the intermediary name of
      // AbstractContainerMenu.slotsChanged, checked in the 1.21.1 tiny mappings.
      method = {"slotsChanged", "method_7609"},
      remap = false)
  private <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> polymorph$getRecipeFor(
      RecipeManager recipeManager, RecipeType<T> type, I input, Level level) {
    return PolymorphApi.getInstance().getRecipeManager()
        .getPlayerRecipe((AbstractContainerMenu) (Object) this, type, input, level,
            this.playerInventory.player);
  }
}
