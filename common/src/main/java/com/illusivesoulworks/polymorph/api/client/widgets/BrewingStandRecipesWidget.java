package com.illusivesoulworks.polymorph.api.client.widgets;

import com.illusivesoulworks.polymorph.api.client.base.PersistentRecipesWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

public class BrewingStandRecipesWidget extends PersistentRecipesWidget {

  private static final int REAGENT_SLOT = 3;
  // Right of the brew progress arrow, level with the reagent: the only empty spot on the
  // texture that stays clear of the centred title.
  private static final int REAGENT_X_SHIFT = 45;
  private static final int REAGENT_Y_SHIFT = 22;

  private final Slot reagentSlot;

  public BrewingStandRecipesWidget(AbstractContainerScreen<?> containerScreen) {
    super(containerScreen);
    this.reagentSlot = containerScreen.getMenu().slots.get(REAGENT_SLOT);
  }

  @Override
  public Slot getOutputSlot() {
    return this.reagentSlot;
  }

  @Override
  public int getXPos() {
    return super.getXPos() + REAGENT_X_SHIFT;
  }

  @Override
  public int getYPos() {
    return super.getYPos() + REAGENT_Y_SHIFT;
  }
}
