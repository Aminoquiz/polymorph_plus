package com.illusivesoulworks.polymorph.common.integration.betterfurnacesreforged;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.client.integration.CompatWidgets;
import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import com.illusivesoulworks.polymorph.common.integration.util.CompatReflection;
import com.illusivesoulworks.polymorph.common.integration.util.SlotRecipeData;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BetterFurnacesModule extends AbstractCompatibilityModule {

  private static final Class<?> BLOCK_ENTITY =
      CompatReflection.findClass("wily.betterfurnaces.blockentity.SmeltingBlockEntity");
  private static final Class<?> MENU =
      CompatReflection.findClass("wily.betterfurnaces.inventory.SmeltingMenu");
  private static final Field MENU_BLOCK_ENTITY = CompatReflection.publicField(
      CompatReflection.findClass("wily.betterfurnaces.inventory.AbstractInventoryMenu"), "be");
  private static final Field INVENTORY = CompatReflection.publicField(BLOCK_ENTITY, "inventory");
  private static final Field RECIPE_TYPE = CompatReflection.publicField(BLOCK_ENTITY, "recipeType");
  private static Method inventoryGetItem;

  @Override
  protected void setup() {

    if (BLOCK_ENTITY == null || MENU == null) {
      return;
    }
    PolymorphApi api = PolymorphApi.getInstance();
    api.registerBlockEntity(blockEntity -> BLOCK_ENTITY.isInstance(blockEntity)
        ? new SlotRecipeData(blockEntity, BetterFurnacesModule::getInput, false) : null);
    // Menu slots hold a FactoryAPI handler, not the block entity.
    api.registerMenu(menu -> MENU.isInstance(menu)
        && CompatReflection.get(MENU_BLOCK_ENTITY, menu) instanceof BlockEntity be ? be : null);
  }

  @Override
  protected void clientSetup() {

    if (MENU != null) {
      CompatWidgets.registerPersistent(MENU, 2);
    }
  }

  public static RecipeType<?> getRecipeType(BlockEntity blockEntity) {
    return CompatReflection.get(RECIPE_TYPE, blockEntity) instanceof RecipeType<?> type ? type : null;
  }

  private static NonNullList<ItemStack> getInput(BlockEntity blockEntity) {
    Object inventory = CompatReflection.get(INVENTORY, blockEntity);

    if (inventory == null) {
      return null;
    }

    try {

      if (inventoryGetItem == null) {
        inventoryGetItem = inventory.getClass().getMethod("getItem", int.class);
      }
      return inventoryGetItem.invoke(inventory, 0) instanceof ItemStack stack
          ? NonNullList.of(ItemStack.EMPTY, stack) : null;
    } catch (ReflectiveOperationException e) {
      return null;
    }
  }
}
