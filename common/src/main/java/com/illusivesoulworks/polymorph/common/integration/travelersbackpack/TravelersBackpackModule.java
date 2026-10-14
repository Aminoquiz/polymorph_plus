package com.illusivesoulworks.polymorph.common.integration.travelersbackpack;

import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.RecipeHolder;

public class TravelersBackpackModule extends AbstractCompatibilityModule {

  private static final String BACKPACK_MENU =
      "com.tiviacz.travelersbackpack.inventory.menu.BackpackBaseMenu";

  public static boolean isBackpackMenu(AbstractContainerMenu containerMenu) {

    for (Class<?> c = containerMenu == null ? null : containerMenu.getClass(); c != null;
         c = c.getSuperclass()) {

      if (c.getName().equals(BACKPACK_MENU)) {
        return true;
      }
    }
    return false;
  }

  public static Player getPlayer(AbstractContainerMenu containerMenu) {

    for (Slot slot : containerMenu.slots) {

      if (slot.container instanceof Inventory inventory) {
        return inventory.player;
      }
    }
    return null;
  }

  // The menu's slotsChanged ignores its argument and re-runs the grid lookup.
  @Override
  protected boolean selectRecipe(AbstractContainerMenu containerMenu, RecipeHolder<?> recipe) {

    if (isBackpackMenu(containerMenu)) {
      Player player = getPlayer(containerMenu);

      if (player != null) {
        containerMenu.slotsChanged(player.getInventory());
        return true;
      }
    }
    return false;
  }
}
