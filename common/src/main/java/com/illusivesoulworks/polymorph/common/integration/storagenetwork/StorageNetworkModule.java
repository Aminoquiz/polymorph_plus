package com.illusivesoulworks.polymorph.common.integration.storagenetwork;

import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;

public class StorageNetworkModule extends AbstractCompatibilityModule {

  private static final String TABLE =
      "com.lothrazar.storagenetwork.block.request.ContainerNetworkCraftingTable";
  private static final String REMOTE =
      "com.lothrazar.storagenetwork.item.remote.ContainerNetworkCraftingRemote";

  @Override
  protected boolean selectRecipe(AbstractContainerMenu containerMenu, Recipe<?> recipe) {
    String name = containerMenu.getClass().getName();

    if ((name.equals(TABLE) || name.equals(REMOTE)) && !containerMenu.slots.isEmpty()) {
      // The network menu ignores the container argument and reruns its own lookup.
      containerMenu.slotsChanged(containerMenu.slots.get(0).container);
      return true;
    }
    return false;
  }
}
