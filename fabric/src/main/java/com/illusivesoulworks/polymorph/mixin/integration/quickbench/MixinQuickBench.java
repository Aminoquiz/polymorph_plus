package com.illusivesoulworks.polymorph.mixin.integration.quickbench;

import org.spongepowered.asm.mixin.Mixin;
import tfar.fastbench.MixinHooks;

@Mixin(MixinHooks.class)
public class MixinQuickBench {

//  @Redirect(
//      at = @At(
//          value = "INVOKE",
//          target = "tfar/fastbench/MixinHooks.findRecipe(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/item/crafting/RecipeHolder;"),
//      method = "slotChangedCraftingGrid")
//  private static RecipeHolder<CraftingRecipe> polymorph$findRecipe(CraftingContainer inv,
//                                                                   Level level, Level unused1,
//                                                                   CraftingContainer unused2,
//                                                                   ResultContainer result) {
//    Player player = PlayerHolder.getPlayer();
//
//    if (player != null) {
//      return RecipeSelection.getPlayerRecipe(player.containerMenu, RecipeType.CRAFTING, inv,
//          level, player).orElse(null);
//    }
//    return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, inv, level).orElse(null);
//  }
//
//  @Inject(
//      at = @At("HEAD"),
//      method = "handleShiftCraft")
//  private static void polymorph$handleShiftCraftPre(Player player,
//                                                    AbstractContainerMenu container,
//                                                    Slot resultSlot,
//                                                    CraftingContainer input,
//                                                    ResultContainer craftResult,
//                                                    int outStart, int outEnd,
//                                                    CallbackInfoReturnable<ItemStack> cir) {
//    if (!player.level().isClientSide()) {
//      PlayerHolder.setPlayer(player);
//    }
//  }
//
//  @Inject(
//      at = @At("RETURN"),
//      method = "handleShiftCraft")
//  private static void polymorph$handleShiftCraftPost(Player player,
//                                                     AbstractContainerMenu container,
//                                                     Slot resultSlot,
//                                                     CraftingContainer input,
//                                                     ResultContainer craftResult,
//                                                     int outStart, int outEnd,
//                                                     CallbackInfoReturnable<ItemStack> cir) {
//    PlayerHolder.setPlayer(null);
//  }
}
