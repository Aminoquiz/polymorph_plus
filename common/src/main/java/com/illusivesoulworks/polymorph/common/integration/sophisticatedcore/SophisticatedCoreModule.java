package com.illusivesoulworks.polymorph.common.integration.sophisticatedcore;

import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import java.util.Map;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;

public class SophisticatedCoreModule extends AbstractCompatibilityModule {

  private static final String MENU_PREFIX = "net.p3pp3rf1y.sophisticated";

  public static boolean isSophisticatedMenu(Object menu) {
    return menu != null && menu.getClass().getName().startsWith(MENU_PREFIX);
  }

  @Override
  protected boolean selectRecipe(AbstractContainerMenu containerMenu, RecipeHolder<?> recipe) {

    if (!isSophisticatedMenu(containerMenu)) {
      return false;
    }
    boolean selected = false;

    try {
      Object containers = containerMenu.getClass().getMethod("getUpgradeContainers")
          .invoke(containerMenu);

      if (containers instanceof Map<?, ?> map) {

        for (Object container : map.values()) {

          if (container instanceof SophisticatedCraftingContainer crafting) {
            selected |= crafting.polymorph$select(recipe);
          }
        }
      }
    } catch (ReflectiveOperationException ignored) {
      // A menu of the family without upgrade slots.
    }
    return selected;
  }
}
