package com.illusivesoulworks.polymorph.common.integration.ironfurnaces;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.client.integration.CompatWidgets;
import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import com.illusivesoulworks.polymorph.common.integration.util.CompatReflection;
import com.illusivesoulworks.polymorph.common.integration.util.SlotRecipeData;
import net.minecraft.world.Container;

public class IronFurnacesModule extends AbstractCompatibilityModule {

  private static final Class<?> BLOCK_ENTITY =
      CompatReflection.findClass("ironfurnaces.tileentity.furnaces.BlockIronFurnaceTileBase");
  private static final Class<?> MENU =
      CompatReflection.findClass("ironfurnaces.container.furnaces.BlockIronFurnaceContainerBase");

  @Override
  protected void setup() {

    if (BLOCK_ENTITY == null) {
      return;
    }
    // Menu slots hold the block entity itself, so the default container lookup finds it.
    PolymorphApi.common().registerBlockEntity2RecipeData(blockEntity ->
        BLOCK_ENTITY.isInstance(blockEntity) && blockEntity instanceof Container container
            ? new SlotRecipeData(blockEntity, be -> container.getItem(0)) : null);
  }

  @Override
  protected void clientSetup() {

    if (MENU != null) {
      CompatWidgets.registerFurnace(MENU);
    }
  }
}
