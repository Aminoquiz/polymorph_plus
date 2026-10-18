package com.illusivesoulworks.polymorph.client;

import com.illusivesoulworks.polymorph.api.client.widgets.PlayerRecipesWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

// Same placement as Traveler's own compat widget on 26.x: button right of the result slot,
// clear of the bottom grid row.
public class TravelersBackpackRecipesWidget extends PlayerRecipesWidget {

  private static final int X_SHIFT = 20;
  private static final int Y_SHIFT = 72;
  private static final int BUTTON_Y_SHIFT = -50;

  public TravelersBackpackRecipesWidget(AbstractContainerScreen<?> containerScreen,
                                        Slot outputSlot) {
    super(containerScreen, outputSlot);
  }

  @Override
  public int getXPos() {
    return super.getXPos() + X_SHIFT;
  }

  @Override
  public int getYPos() {
    return super.getYPos() + Y_SHIFT;
  }

  @Override
  public void initChildWidgets() {
    super.initChildWidgets();
    this.openButton.setOffsets(this.getXPos() + PolymorphClientConfig.getButtonOffsetX(),
        this.getYPos() + BUTTON_Y_SHIFT + PolymorphClientConfig.getButtonOffsetY());
  }

  @Override
  protected void resetWidgetOffsets() {
    super.resetWidgetOffsets();
    this.openButton.setOffsets(this.getXPos() + PolymorphClientConfig.getButtonOffsetX(),
        this.getYPos() + BUTTON_Y_SHIFT + PolymorphClientConfig.getButtonOffsetY());
  }
}