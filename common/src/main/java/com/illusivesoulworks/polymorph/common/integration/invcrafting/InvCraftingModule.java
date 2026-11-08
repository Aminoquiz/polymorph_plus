package com.illusivesoulworks.polymorph.common.integration.invcrafting;

import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import com.illusivesoulworks.polymorph.common.integration.util.CompatReflection;
import com.illusivesoulworks.polymorph.mixin.core.AccessorInventoryMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;

public class InvCraftingModule extends AbstractCompatibilityModule {

  private static final Class<?> MENU =
      CompatReflection.findClass("com.lothrazar.invcrafting.inventory.ContainerPlayerCrafting");

  @Override
  protected boolean selectRecipe(AbstractContainerMenu containerMenu, Recipe<?> recipe) {

    if (MENU != null && MENU.isInstance(containerMenu)) {
      containerMenu.slotsChanged(((AccessorInventoryMenu) containerMenu).getCraftSlots());
      return true;
    }
    return false;
  }
}
