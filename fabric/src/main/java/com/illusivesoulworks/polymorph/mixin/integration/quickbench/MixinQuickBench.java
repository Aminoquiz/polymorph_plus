package com.illusivesoulworks.polymorph.mixin.integration.quickbench;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.mixin.PlayerHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfar.fastbench.MixinHooks;

@SuppressWarnings("unused")
@Mixin(MixinHooks.class)
public class MixinQuickBench {

  // QuickBench keeps the previous recipe while it still matches and skips the lookup; the
  // candidate list is only rebuilt inside the lookup, so it has to run on every grid update.
  @Redirect(
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/item/crafting/CraftingRecipe;matches(Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Z"),
      method = "slotChangedCraftingGrid",
      require = 0)
  private static boolean polymorph$matches(CraftingRecipe recipe, RecipeInput input, Level world) {
    return PlayerHolder.getPlayer() == null && recipe.matches((CraftingInput) input, world);
  }

  @Redirect(
      at = @At(
          value = "INVOKE",
          target = "Ltfar/fastbench/MixinHooks;findRecipe(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/item/crafting/RecipeHolder;"),
      method = "slotChangedCraftingGrid")
  private static RecipeHolder<CraftingRecipe> polymorph$findRecipe(CraftingInput input,
                                                                   Level level) {
    Player player = PlayerHolder.getPlayer();

    if (player != null) {
      return PolymorphApi.getInstance().getRecipeManager()
          .getPlayerRecipe(player.containerMenu, RecipeType.CRAFTING, input, level, player)
          .orElse(null);
    }
    return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElse(null);
  }

  @Inject(
      at = @At("HEAD"),
      method = "handleShiftCraft")
  private static void polymorph$handleShiftCraftPre(Player player,
                                                    AbstractContainerMenu container,
                                                    Slot resultSlot,
                                                    CraftingContainer input,
                                                    ResultContainer craftResult,
                                                    int outStart, int outEnd,
                                                    CallbackInfoReturnable<ItemStack> cir) {
    if (!player.level().isClientSide()) {
      PlayerHolder.setPlayer(player);
    }
  }

  @Inject(
      at = @At("RETURN"),
      method = "handleShiftCraft")
  private static void polymorph$handleShiftCraftPost(Player player,
                                                     AbstractContainerMenu container,
                                                     Slot resultSlot,
                                                     CraftingContainer input,
                                                     ResultContainer craftResult,
                                                     int outStart, int outEnd,
                                                     CallbackInfoReturnable<ItemStack> cir) {
    PlayerHolder.setPlayer(null);
  }
}
