package com.illusivesoulworks.polymorph.common.integration.craftingstation;

import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.Recipe;

// Crafting Station: J/EMI Edition Updated. It reads the player's pick itself, but only when its
// grid updates, and its slotsChanged does nothing unless it is handed that grid.
public class CraftingStationJeiModule extends AbstractCompatibilityModule {

  private static final String MENU =
      "com.leclowndu93150.craftingstationjei.menu.CraftingStationMenu";

  // The grid is saved in the block, so it can already hold a conflict when the menu opens.
  @Override
  protected boolean openContainer(AbstractContainerMenu containerMenu,
                                  ServerPlayer serverPlayerEntity) {
    return refresh(containerMenu);
  }

  @Override
  protected boolean selectRecipe(AbstractContainerMenu containerMenu, Recipe<?> recipe) {
    return refresh(containerMenu);
  }

  private static boolean refresh(AbstractContainerMenu containerMenu) {

    if (containerMenu == null || !containerMenu.getClass().getName().equals(MENU)) {
      return false;
    }

    for (Slot slot : containerMenu.slots) {

      if (slot.container instanceof CraftingContainer grid) {
        containerMenu.slotsChanged(grid);
        return true;
      }
    }
    return false;
  }
}
