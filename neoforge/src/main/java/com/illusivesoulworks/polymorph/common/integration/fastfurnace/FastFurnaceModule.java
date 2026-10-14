package com.illusivesoulworks.polymorph.common.integration.fastfurnace;

import com.illusivesoulworks.polymorph.PolymorphConstants;
import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import com.illusivesoulworks.polymorph.common.integration.PolymorphIntegrations;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.reflect.FieldUtils;

public class FastFurnaceModule extends AbstractCompatibilityModule {

  // FastFurnace keeps its cached recipe while it still matches the input, so a new pick only
  // takes effect once that cache holds it.
  @Override
  public boolean selectRecipe(BlockEntity blockEntity, RecipeHolder<?> recipe) {

    if (recipe.value() instanceof AbstractCookingRecipe &&
        blockEntity instanceof AbstractFurnaceBlockEntity) {
      try {
        FieldUtils.writeField(blockEntity, "curRecipe", recipe, true);
      } catch (IllegalAccessException | IllegalArgumentException e) {
        PolymorphIntegrations.disable("fastfurnace");
        PolymorphConstants.LOG.error(
            "Polymorph encountered an error with its fastfurnace integration.");
        PolymorphConstants.LOG.error("The integration module for fastfurnace will be disabled.");
        PolymorphConstants.LOG.error(
            "Please report this bug to Polymorph only, do not report this to fastfurnace.");
        e.printStackTrace();
      }
    }
    return false;
  }
}
