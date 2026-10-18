package com.illusivesoulworks.polymorph.common.integration.craftingslots;

import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;

public class CraftingSlotsModule extends AbstractCompatibilityModule {

  private static final Class<?> MENU = findMenu();

  private static Class<?> findMenu() {
    try {
      return Class.forName("net.blay09.mods.craftingslots.menu.CustomCraftingMenu");
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  @Override
  protected boolean selectRecipe(AbstractContainerMenu containerMenu, RecipeHolder<?> recipe) {

    if (MENU != null && MENU.isInstance(containerMenu) && !containerMenu.slots.isEmpty()) {
      containerMenu.slotsChanged(containerMenu.slots.get(0).container);
      return true;
    }
    return false;
  }
}
