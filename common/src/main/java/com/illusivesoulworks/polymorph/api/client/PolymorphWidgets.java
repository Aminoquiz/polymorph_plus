package com.illusivesoulworks.polymorph.api.client;

import com.illusivesoulworks.polymorph.api.client.base.IRecipesWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

public abstract class PolymorphWidgets {

  private static PolymorphWidgets instance;

  public static PolymorphWidgets getInstance() {

    if (instance == null) {
      throw new RuntimeException("Missing PolymorphWidgets implementation!");
    }
    return instance;
  }

  public abstract IRecipesWidget getCurrentWidget();

  public abstract IRecipesWidget getWidgetOrDefault(AbstractContainerScreen<?> containerScreen);

  public abstract IRecipesWidget getWidget(AbstractContainerScreen<?> containerScreen);

  public abstract void registerWidget(IRecipesWidgetFactory factory);

  public abstract Slot findResultSlot(AbstractContainerScreen<?> containerScreen);

  public interface IRecipesWidgetFactory {

    IRecipesWidget createWidget(AbstractContainerScreen<?> containerScreen);
  }
}
