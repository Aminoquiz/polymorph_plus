package com.illusivesoulworks.polymorph.api.client.widgets;

import com.illusivesoulworks.polymorph.api.client.base.PersistentRecipesWidget;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.NonInteractiveResultSlot;
import net.minecraft.world.inventory.Slot;

public class CrafterRecipesWidget extends PersistentRecipesWidget {

  private Slot outputSlot;

  public CrafterRecipesWidget(AbstractContainerScreen<?> containerScreen) {
    super(containerScreen);
    List<Slot> slots = containerScreen.getMenu().slots;

    for (Slot slot : slots) {

      if (slot instanceof NonInteractiveResultSlot) {
        this.outputSlot = slot;
      }
    }

    if (this.outputSlot == null) {
      this.outputSlot = slots.getFirst();
    }
  }

  @Override
  public Slot getOutputSlot() {
    return this.outputSlot;
  }
}
