package com.illusivesoulworks.polymorph.mixin.integration.storagenetwork;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Pseudo
@Mixin(targets = "com.lothrazar.storagenetwork.gui.ContainerNetwork", remap = false)
public abstract class MixinContainerNetwork {

  @Shadow
  protected Player player;

  @Redirect(
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"),
      method = "findMatchingRecipe")
  private <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> polymorph$getRecipeFor(
      RecipeManager recipeManager, RecipeType<T> type, I input, Level level) {

    if (this.player != null) {
      return PolymorphApi.getInstance().getRecipeManager()
          .getPlayerRecipe((AbstractContainerMenu) (Object) this, type, input, level, this.player);
    }
    return recipeManager.getRecipeFor(type, input, level);
  }
}
