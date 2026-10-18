package com.illusivesoulworks.polymorph.client.integration;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import com.illusivesoulworks.polymorph.api.client.base.PersistentRecipesWidget;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public class CompatWidgets {

  // Persistent selector on a modded menu whose output sits at a fixed slot index.
  public static void registerPersistent(Class<?> menuClass, int outputSlot) {
    PolymorphWidgets.getInstance().registerWidget(containerScreen -> {
      AbstractContainerMenu menu = containerScreen.getMenu();

      if (!menuClass.isInstance(menu) || menu.slots.size() <= outputSlot) {
        return null;
      }
      Slot output = menu.slots.get(outputSlot);
      return new PersistentRecipesWidget(containerScreen) {
        @Override
        public Slot getOutputSlot() {
          return output;
        }
      };
    });
  }
}
