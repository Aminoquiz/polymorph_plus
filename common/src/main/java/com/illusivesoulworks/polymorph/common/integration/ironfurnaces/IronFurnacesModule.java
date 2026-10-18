package com.illusivesoulworks.polymorph.common.integration.ironfurnaces;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.client.integration.CompatWidgets;
import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import com.illusivesoulworks.polymorph.common.integration.util.CompatReflection;
import com.illusivesoulworks.polymorph.common.integration.util.SlotRecipeData;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

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
    // Slot 0 is the furnace input; factory slots holding another item fall back to the default.
    PolymorphApi.getInstance().registerBlockEntity(blockEntity ->
        BLOCK_ENTITY.isInstance(blockEntity) && blockEntity instanceof Container container
            ? new SlotRecipeData(blockEntity,
            be -> NonNullList.of(ItemStack.EMPTY, container.getItem(0)), false) : null);
  }

  @Override
  protected void clientSetup() {

    if (MENU != null) {
      CompatWidgets.registerPersistent(MENU, 2);
    }
  }
}
