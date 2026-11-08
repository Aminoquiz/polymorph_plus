package com.illusivesoulworks.polymorph.common.integration.craftingstation;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.client.integration.CompatWidgets;
import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import com.illusivesoulworks.polymorph.common.integration.util.CompatReflection;
import com.illusivesoulworks.polymorph.common.integration.util.SlotRecipeData;
import java.lang.reflect.Field;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CraftingStationModule extends AbstractCompatibilityModule {

  private static final Class<?> MENU =
      CompatReflection.findClass("tfar.craftingstation.menu.CraftingStationMenu");
  private static final Class<?> BLOCK_ENTITY =
      CompatReflection.findClass("tfar.craftingstation.blockentity.CraftingStationBlockEntity");
  private static final Field MENU_BLOCK_ENTITY = CompatReflection.publicField(MENU, "tileEntity");
  private static final Field INPUT = CompatReflection.publicField(BLOCK_ENTITY, "input");

  // Crafting Station: J/EMI Edition Updated, a separate mod with the same mod id. It reads the
  // player's pick itself, but only when its grid updates.
  private static final String UPDATED_MENU =
      "com.leclowndu93150.craftingstationjei.menu.CraftingStationMenu";

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

  // The grid is saved in the block, so it can already hold a conflict when the menu opens.
  @Override
  protected boolean openContainer(AbstractContainerMenu containerMenu,
                                  ServerPlayer serverPlayerEntity) {
    return refreshUpdated(containerMenu);
  }

  @Override
  protected boolean selectRecipe(AbstractContainerMenu containerMenu, RecipeHolder<?> recipe) {
    return refreshUpdated(containerMenu);
  }

  // Its slotsChanged does nothing unless it is handed its own crafting grid.
  private static boolean refreshUpdated(AbstractContainerMenu containerMenu) {

    if (containerMenu == null || !containerMenu.getClass().getName().equals(UPDATED_MENU)) {
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

  private static NonNullList<ItemStack> getInput(BlockEntity blockEntity) {
    return CompatReflection.get(INPUT, blockEntity) instanceof SimpleContainer input
        ? input.getItems() : null;
  }
}
