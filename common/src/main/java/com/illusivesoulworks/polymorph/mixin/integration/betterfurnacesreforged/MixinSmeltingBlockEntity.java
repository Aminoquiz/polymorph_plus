package com.illusivesoulworks.polymorph.mixin.integration.betterfurnacesreforged;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.common.crafting.RecipeSelection;
import com.illusivesoulworks.polymorph.common.integration.betterfurnacesreforged.BetterFurnacesModule;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Pseudo
@Mixin(targets = "wily.betterfurnaces.blockentity.SmeltingBlockEntity", remap = false)
public abstract class MixinSmeltingBlockEntity {

  // grabRecipe memoises one recipe per item in an LRU cache, which would pin the default.
  // Stacks other than the input slot's item (hopper checks) keep the default lookup.
  @SuppressWarnings("unchecked")
  @Inject(at = @At("HEAD"), method = "grabRecipe", cancellable = true)
  private void polymorph$grabRecipe(ItemStack stack,
                                    CallbackInfoReturnable<Optional<AbstractCookingRecipe>> cir) {
    BlockEntity blockEntity = (BlockEntity) (Object) this;
    Level level = blockEntity.getLevel();
    ItemStack input = BetterFurnacesModule.getInput(blockEntity);
    RecipeType<?> type = BetterFurnacesModule.getRecipeType(blockEntity);

    if (level == null || level.isClientSide() || stack.getItem() instanceof AirItem
        || input == null || !ItemStack.isSameItem(stack, input)
        || type == null
        || PolymorphApi.common().getRecipeData(blockEntity).isEmpty()) {
      return;
    }
    cir.setReturnValue(RecipeSelection.getBlockEntityRecipe(
        (RecipeType<AbstractCookingRecipe>) type, new SimpleContainer(stack), level, blockEntity));
  }
}
