package com.illusivesoulworks.polymorph.common.integration.craftingstation;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.client.integration.CompatWidgets;
import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import com.illusivesoulworks.polymorph.common.integration.util.CompatReflection;
import com.illusivesoulworks.polymorph.common.integration.util.SlotRecipeData;
import java.lang.reflect.Field;
import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CraftingStationModule extends AbstractCompatibilityModule {

  private static final Class<?> MENU =
      CompatReflection.findClass("tfar.craftingstation.menu.CraftingStationMenu");
  private static final Class<?> BLOCK_ENTITY =
      CompatReflection.findClass("tfar.craftingstation.blockentity.CraftingStationBlockEntity");
  private static final Field MENU_BLOCK_ENTITY = CompatReflection.publicField(MENU, "tileEntity");
  private static final Field INPUT = CompatReflection.publicField(BLOCK_ENTITY, "input");

  @Override
  protected void setup() {

    if (MENU == null || BLOCK_ENTITY == null) {
      return;
    }
    PolymorphApi api = PolymorphApi.getInstance();
    // calcResult runs from setChanged with no player, so the pick lives on the block entity.
    api.registerBlockEntity(blockEntity -> BLOCK_ENTITY.isInstance(blockEntity)
        ? new SlotRecipeData(blockEntity, CraftingStationModule::getInput, true) : null);
    // The station's slots hold a wrapper container, so the default slot scan finds no block entity.
    api.registerMenu(menu -> MENU.isInstance(menu)
        && CompatReflection.get(MENU_BLOCK_ENTITY, menu) instanceof BlockEntity be ? be : null);
  }

  @Override
  protected void clientSetup() {

    if (MENU != null) {
      CompatWidgets.registerPersistent(MENU, 0);
    }
  }

  private static NonNullList<ItemStack> getInput(BlockEntity blockEntity) {
    return CompatReflection.get(INPUT, blockEntity) instanceof SimpleContainer input
        ? input.getItems() : null;
  }
}
