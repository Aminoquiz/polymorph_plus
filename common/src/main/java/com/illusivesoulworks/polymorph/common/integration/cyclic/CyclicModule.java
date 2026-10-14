package com.illusivesoulworks.polymorph.common.integration.cyclic;

import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;

public class CyclicModule extends AbstractCompatibilityModule {

  private static final String WORKBENCH = "com.lothrazar.cyclic.block.workbench.ContainerWorkbench";

  @Override
  protected boolean selectRecipe(AbstractContainerMenu containerMenu, RecipeHolder<?> recipe) {

    if (containerMenu.getClass().getName().equals(WORKBENCH) && !containerMenu.slots.isEmpty()) {
      // The workbench ignores the container argument and reruns its own lookup.
      containerMenu.slotsChanged(containerMenu.slots.get(0).container);
      return true;
    }
    return false;
  }
}
