package com.illusivesoulworks.polymorph.mixin.integration.ironfurnaces;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.common.crafting.RecipeSelection;
import java.util.Optional;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Pseudo
@Mixin(targets = "ironfurnaces.tileentity.furnaces.BlockIronFurnaceTileBase", remap = false)
public abstract class MixinBlockIronFurnaceTileBase {

  @Shadow
  public RecipeType<? extends AbstractCookingRecipe> recipeType;

  // getRecipe (slot 0) memoises one recipe per item in an LRU cache and queries a
  // SimpleContainer, so neither the cache nor MixinRecipeManager would see the pick.
  @SuppressWarnings("unchecked")
  @Inject(at = @At("HEAD"), method = "getRecipe", cancellable = true)
  private void polymorph$getRecipe(ItemStack stack,
                                   CallbackInfoReturnable<Optional<AbstractCookingRecipe>> cir) {
    BlockEntity blockEntity = (BlockEntity) (Object) this;
    Level level = blockEntity.getLevel();

    if (level == null || level.isClientSide() || this.recipeType == null
        || stack.getItem() instanceof AirItem
        || PolymorphApi.common().getRecipeData(blockEntity).isEmpty()) {
      return;
    }
    cir.setReturnValue(RecipeSelection.getBlockEntityRecipe(
        (RecipeType<AbstractCookingRecipe>) this.recipeType, new SimpleContainer(stack), level,
        blockEntity));
  }
}
