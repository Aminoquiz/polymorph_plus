package com.illusivesoulworks.polymorph.mixin.integration.toms_storage;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.common.integration.toms_storage.TomsStorageModule;
import java.lang.ref.WeakReference;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The terminal ships its own Polymorph hook, but it is gated on the upstream mod id and its
// Fabric build is compiled against a stub whose signatures do not exist in the real API.
@SuppressWarnings("unused")
@Pseudo
@Mixin(targets = "com.tom.storagemod.block.entity.CraftingTerminalBlockEntity", remap = false)
public class MixinCraftingTerminalBlockEntity {

  @Shadow
  private WeakReference<Player> polymorphPlayer;

  @Inject(at = @At("HEAD"), method = "getRecipe", cancellable = true)
  private void polymorph$getRecipe(CraftingInput input,
                                   CallbackInfoReturnable<Optional<RecipeHolder<CraftingRecipe>>> cir) {
    Player player = this.polymorph$viewer();

    if (player != null) {
      cir.setReturnValue(PolymorphApi.getInstance().getRecipeManager()
          .getPlayerRecipe(player.containerMenu, RecipeType.CRAFTING, input, player.level(),
              player));
    }
  }

  // The terminal keeps its previous recipe while it still matches and skips the lookup. The
  // candidate list is only rebuilt inside the lookup, so it has to run on every grid update.
  @Redirect(
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/item/crafting/CraftingRecipe;matches(Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Z"),
      method = "onCraftingMatrixChanged")
  private boolean polymorph$matches(CraftingRecipe recipe, RecipeInput input, Level level) {
    return this.polymorph$viewer() == null && recipe.matches((CraftingInput) input, level);
  }

  @Unique
  private Player polymorph$viewer() {
    Player player = this.polymorphPlayer != null ? this.polymorphPlayer.get() : null;
    return player != null && TomsStorageModule.isCraftingTerminal(player.containerMenu) ? player
        : null;
  }
}
