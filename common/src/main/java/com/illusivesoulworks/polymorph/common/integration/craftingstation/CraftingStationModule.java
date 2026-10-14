package com.illusivesoulworks.polymorph.common.integration.craftingstation;

import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;

public class CraftingStationModule extends AbstractCompatibilityModule {

  private static final String MENU = "tfar.craftingstation.CraftingStationContainer";

  @Override
  protected boolean openContainer(AbstractContainerMenu containerMenu,
                                  ServerPlayer serverPlayerEntity) {
    return refresh(containerMenu);
  }

  @Override
  protected boolean selectRecipe(AbstractContainerMenu containerMenu, Recipe<?> recipe) {
    return refresh(containerMenu);
  }

  // The station's slotsChanged ignores its argument and re-reads its own grid.
  private static boolean refresh(AbstractContainerMenu containerMenu) {

    if (containerMenu == null || !containerMenu.getClass().getName().equals(MENU)) {
      return false;
    }
    containerMenu.slotsChanged(null);
    return true;
  }
}
