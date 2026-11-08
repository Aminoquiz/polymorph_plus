package com.illusivesoulworks.polymorph.common.integration.invcrafting;

import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import com.illusivesoulworks.polymorph.common.integration.util.CompatReflection;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;

public class InvCraftingModule extends AbstractCompatibilityModule {

  private static final Class<?> MENU =
      CompatReflection.findClass("com.lothrazar.invcrafting.inventory.ContainerPlayerCrafting");

  @Override
  protected boolean selectRecipe(AbstractContainerMenu containerMenu, RecipeHolder<?> recipe) {

    if (MENU != null && MENU.isInstance(containerMenu) && !containerMenu.slots.isEmpty()) {
      // slotsChanged ignores its argument and reruns the lookup on its own grid.
      containerMenu.slotsChanged(containerMenu.slots.get(0).container);
      return true;
    }
    return false;
  }
}
