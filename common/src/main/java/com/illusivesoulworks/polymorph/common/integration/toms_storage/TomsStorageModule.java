package com.illusivesoulworks.polymorph.common.integration.toms_storage;

import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import java.lang.reflect.Field;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;

public class TomsStorageModule extends AbstractCompatibilityModule {

  private static final String CRAFTING_TERMINAL = "com.tom.storagemod.gui.ContainerCraftingTerminal";

  public static boolean isCraftingTerminal(AbstractContainerMenu containerMenu) {
    return containerMenu != null && containerMenu.getClass().getName().equals(CRAFTING_TERMINAL);
  }

  @Override
  protected boolean openContainer(AbstractContainerMenu containerMenu,
                                  ServerPlayer serverPlayerEntity) {
    return refresh(containerMenu);
  }

  @Override
  protected boolean selectRecipe(AbstractContainerMenu containerMenu, Recipe<?> recipe) {
    return refresh(containerMenu);
  }

  // This version has no menu button that resolves the recipe again, so the block entity held in
  // the menu's protected "te" field is asked directly.
  private static boolean refresh(AbstractContainerMenu containerMenu) {

    if (!isCraftingTerminal(containerMenu)) {
      return false;
    }

    for (Class<?> clazz = containerMenu.getClass(); clazz != null;
         clazz = clazz.getSuperclass()) {

      try {
        Field field = clazz.getDeclaredField("te");
        field.setAccessible(true);

        Object te = field.get(containerMenu);
        if (te instanceof TomsCraftingTerminal) {
          ((TomsCraftingTerminal) te).polymorph$refresh();
          return true;
        }
        return false;
      } catch (NoSuchFieldException e) {
        // Declared on a superclass.
      } catch (IllegalAccessException e) {
        return false;
      }
    }
    return false;
  }
}
