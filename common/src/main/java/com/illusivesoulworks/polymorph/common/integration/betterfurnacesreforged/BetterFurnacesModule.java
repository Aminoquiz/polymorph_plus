package com.illusivesoulworks.polymorph.common.integration.betterfurnacesreforged;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IPolymorphCommon;
import com.illusivesoulworks.polymorph.client.integration.CompatWidgets;
import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import com.illusivesoulworks.polymorph.common.integration.util.CompatReflection;
import com.illusivesoulworks.polymorph.common.integration.util.SlotRecipeData;
import java.lang.reflect.Field;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BetterFurnacesModule extends AbstractCompatibilityModule {

  public static final Class<?> BLOCK_ENTITY =
      CompatReflection.findClass("wily.betterfurnaces.blockentity.SmeltingBlockEntity");
  private static final Class<?> MENU =
      CompatReflection.findClass("wily.betterfurnaces.inventory.SmeltingMenu");
  private static final Field MENU_BLOCK_ENTITY = CompatReflection.publicField(
      CompatReflection.findClass("wily.betterfurnaces.inventory.AbstractInventoryMenu"), "be");
  private static final Field INVENTORY = CompatReflection.publicField(BLOCK_ENTITY, "inventory");
  private static final Field RECIPE_TYPE = CompatReflection.publicField(BLOCK_ENTITY, "recipeType");

  @Override
  protected void setup() {

    if (BLOCK_ENTITY == null || MENU == null) {
      return;
    }
    IPolymorphCommon api = PolymorphApi.common();
    api.registerBlockEntity2RecipeData(blockEntity -> BLOCK_ENTITY.isInstance(blockEntity)
        ? new SlotRecipeData(blockEntity, BetterFurnacesModule::getInput) : null);
    // Menu slots hold a FactoryAPI handler, not the block entity.
    api.registerContainer2BlockEntity(menu -> MENU.isInstance(menu)
        && CompatReflection.get(MENU_BLOCK_ENTITY, menu) instanceof BlockEntity be ? be : null);
  }

  @Override
  protected void clientSetup() {

    if (MENU != null) {
      CompatWidgets.registerFurnace(MENU);
    }
  }

  public static RecipeType<?> getRecipeType(BlockEntity blockEntity) {
    return CompatReflection.get(RECIPE_TYPE, blockEntity) instanceof RecipeType<?> type ? type : null;
  }

  // FactoryItemHandler extends SimpleContainer on both loaders.
  public static ItemStack getInput(BlockEntity blockEntity) {
    return CompatReflection.get(INVENTORY, blockEntity) instanceof Container container
        && container.getContainerSize() > 0 ? container.getItem(0) : null;
  }
}
