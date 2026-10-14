package com.illusivesoulworks.polymorph.common.integration.refinedstorage;

import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import java.lang.reflect.Method;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;

public class RefinedStorageModule extends AbstractCompatibilityModule {

  private static final String MENU = "com.refinedmods.refinedstorage.container.GridContainer";

  private static final String GRID_NODE =
      "com.refinedmods.refinedstorage.apiimpl.network.node.GridNetworkNode";

  public static boolean isGridMenu(AbstractContainerMenu containerMenu) {
    return containerMenu != null && containerMenu.getClass().getName().equals(MENU);
  }

  @Override
  protected boolean selectRecipe(AbstractContainerMenu containerMenu, Recipe<?> recipe) {

    if (!isGridMenu(containerMenu)) {
      return false;
    }

    try {
      Object grid = containerMenu.getClass().getMethod("getGrid").invoke(containerMenu);

      if (grid == null || !grid.getClass().getName().equals(GRID_NODE)) {
        return false;
      }
      Method refresh = grid.getClass().getMethod("onCraftingMatrixChanged");
      refresh.invoke(grid);
      return true;
    } catch (ReflectiveOperationException e) {
      return false;
    }
  }
}
