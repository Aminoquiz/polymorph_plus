package com.illusivesoulworks.polymorph.mixin.integration.quickbench;

import com.illusivesoulworks.polymorph.common.crafting.RecipeSelection;
import com.illusivesoulworks.polymorph.common.integration.quickbench.PlayerHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfar.fastbench.MixinHooks;

@Mixin(MixinHooks.class)
public class MixinQuickBench {

  @Redirect(
      at = @At(
          value = "INVOKE",
          target = "tfar/fastbench/MixinHooks.findRecipe(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/item/crafting/Recipe;"),
      method = "slotChangedCraftingGrid")
  private static Recipe<CraftingContainer> polymorph$findRecipe(CraftingContainer inv, Level level,
                                                                Level unused1,
                                                                CraftingContainer unused2,
                                                                ResultContainer result) {
    Player player = PlayerHolder.getPlayer();

    if (player != null) {
      return RecipeSelection.getPlayerRecipe(player.containerMenu, RecipeType.CRAFTING, inv,
          level, player).orElse(null);
    }
    return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, inv, level).orElse(null);
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
    if (!player.level.isClientSide()) {
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
