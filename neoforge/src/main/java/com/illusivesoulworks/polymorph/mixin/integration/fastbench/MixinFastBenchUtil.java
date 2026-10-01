package com.illusivesoulworks.polymorph.mixin.integration.fastbench;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import dev.shadowsoffire.fastbench.api.ICraftingContainer;
import dev.shadowsoffire.fastbench.util.CraftingInventoryExt;
import dev.shadowsoffire.fastbench.util.FastBenchUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Mixin(value = FastBenchUtil.class, remap = false)
public class MixinFastBenchUtil {

  // FastWorkbench keeps the previous recipe while it still matches and skips the lookup. The
  // candidate list is only rebuilt inside the lookup, so it has to run on every grid update.
  @Redirect(
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/item/crafting/CraftingRecipe;matches(Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Z"),
      method = "slotChangedCraftingGrid",
      require = 1)
  private static boolean polymorph$matches(CraftingRecipe recipe, RecipeInput input, Level world,
                                           ServerLevel level, Player player,
                                           CraftingInventoryExt inv, ResultContainer result) {
    return !polymorph$isOpenGrid(player, result) && recipe.matches((CraftingInput) input, world);
  }

  @Redirect(
      at = @At(
          value = "INVOKE",
          target = "Ldev/shadowsoffire/fastbench/util/FastBenchUtil;findRecipe(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/item/crafting/RecipeHolder;"),
      method = "slotChangedCraftingGrid",
      require = 1)
  private static RecipeHolder<CraftingRecipe> polymorph$findRecipe(CraftingInput input,
                                                                   ServerLevel world,
                                                                   ServerLevel level,
                                                                   Player player,
                                                                   CraftingInventoryExt inv,
                                                                   ResultContainer result) {

    if (!polymorph$isOpenGrid(player, result)) {
      return FastBenchUtil.findRecipe(input, world);
    }
    return PolymorphApi.getInstance().getRecipeManager()
        .getPlayerRecipe(player.containerMenu, RecipeType.CRAFTING, input, world, player)
        .orElse(null);
  }

  // The update is queued and runs up to a tick later, by which time the player may have closed
  // the menu the grid belongs to.
  @Unique
  private static boolean polymorph$isOpenGrid(Player player, ResultContainer result) {
    return player.containerMenu instanceof ICraftingContainer container
        && container.getResult() == result;
  }
}
