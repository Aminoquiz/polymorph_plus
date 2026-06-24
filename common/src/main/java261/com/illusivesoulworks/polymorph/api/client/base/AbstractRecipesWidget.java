/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * MC 26.1 fork. Children are extracted via render, not render. AbstractWidget
 * mouseClicked now takes (MouseButtonEvent, boolean) — we wrap the mod's (mouseX, mouseY,
 * button) call into a synthetic MouseButtonEvent.
 */
package com.illusivesoulworks.polymorph.api.client.base;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.client.widgets.children.OpenSelectionButton;
import com.illusivesoulworks.polymorph.api.client.widgets.children.SelectionWidget;
import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.platform.Services;
import com.mojang.datafixers.util.Pair;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.resources.Identifier;

public abstract class AbstractRecipesWidget implements IRecipesWidget {

  public static final WidgetSprites OUTPUT =
      new WidgetSprites(Identifier.fromNamespaceAndPath(PolymorphApi.MOD_ID, "output_button"),
          Identifier.fromNamespaceAndPath(PolymorphApi.MOD_ID, "output_button_highlighted"));
  public static final WidgetSprites CURRENT_OUTPUT = new WidgetSprites(
      Identifier.fromNamespaceAndPath(PolymorphApi.MOD_ID, "current_output"),
      Identifier.fromNamespaceAndPath(PolymorphApi.MOD_ID, "current_output_highlighted"));
  public static final WidgetSprites SELECTOR = new WidgetSprites(
      Identifier.fromNamespaceAndPath(PolymorphApi.MOD_ID, "selector_button"),
      Identifier.fromNamespaceAndPath(PolymorphApi.MOD_ID, "selector_button_highlighted"));
  public static final int BUTTON_X_OFFSET = 0;
  public static final int BUTTON_Y_OFFSET = -22;
  public static final int WIDGET_X_OFFSET = -4;
  public static final int WIDGET_Y_OFFSET = -26;

  protected final AbstractContainerScreen<?> containerScreen;
  protected final int xOffset;
  protected final int yOffset;

  protected SelectionWidget selectionWidget;
  protected OpenSelectionButton openButton;

  public AbstractRecipesWidget(AbstractContainerScreen<?> containerScreen, int xOffset,
                               int yOffset) {
    this.containerScreen = containerScreen;
    this.xOffset = xOffset;
    this.yOffset = yOffset;
  }

  public AbstractRecipesWidget(AbstractContainerScreen<?> containerScreen) {
    this(containerScreen, WIDGET_X_OFFSET, WIDGET_Y_OFFSET);
  }

  @Override
  public void initChildWidgets() {
    int x = Services.CLIENT_PLATFORM.getScreenLeft(this.containerScreen) + this.getXPos();
    int y = Services.CLIENT_PLATFORM.getScreenTop(this.containerScreen) + this.getYPos();
    this.selectionWidget =
        new SelectionWidget(x + this.xOffset, y + this.yOffset, this.getXPos() + this.xOffset,
            this.getYPos() + this.yOffset, this.getOutputSprites(), this::selectRecipe,
            this.containerScreen);
    this.openButton = new OpenSelectionButton(this.containerScreen, this.getXPos(), this.getYPos(),
        this.getSelectorSprites(),
        clickWidget -> this.selectionWidget.setActive(!this.selectionWidget.isActive()));
    this.openButton.visible = this.selectionWidget.getOutputWidgets().size() > 1;
  }

  public WidgetSprites getSelectorSprites() {
    return SELECTOR;
  }

  public Pair<WidgetSprites, WidgetSprites> getOutputSprites() {
    return Pair.of(OUTPUT, CURRENT_OUTPUT);
  }

  protected void resetWidgetOffsets() {
    int x = this.getXPos();
    int y = this.getYPos();
    this.selectionWidget.setOffsets(x + this.xOffset, y + this.yOffset);
    this.openButton.setOffsets(x, y);
  }

  @Override
  public abstract void selectRecipe(Identifier resourceLocation);

  @Override
  public SelectionWidget getSelectionWidget() {
    return selectionWidget;
  }

  @Override
  public void highlightRecipe(Identifier resourceLocation) {
    this.selectionWidget.highlightButton(resourceLocation);
  }

  @Override
  public void setRecipesList(Set<IRecipePair> recipesList, Identifier selected) {
    SortedSet<IRecipePair> sorted = new TreeSet<>(recipesList);
    this.selectionWidget.setRecipeList(sorted);
    this.openButton.visible = recipesList.size() > 1;

    if (selected != null) {
      this.highlightRecipe(selected);
    }
  }

  @Override
  public void render(GuiGraphics guiGraphics, int mouseX, int mouseY,
                     float renderPartialTicks) {
    this.selectionWidget.render(guiGraphics, mouseX, mouseY, renderPartialTicks);
    this.openButton.render(guiGraphics, mouseX, mouseY, renderPartialTicks);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    MouseButtonEvent event = new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0));

    if (this.openButton.mouseClicked(event, false)) {
      return true;
    } else if (this.selectionWidget.mouseClicked(event, false)) {
      this.selectionWidget.setActive(false);
      return true;
    } else if (this.selectionWidget.isActive()) {

      if (!this.openButton.mouseClicked(event, false)) {
        this.selectionWidget.setActive(false);
      }
      return true;
    }
    return false;
  }

  @Override
  public int getXPos() {
    return this.getOutputSlot().x + BUTTON_X_OFFSET;
  }

  @Override
  public int getYPos() {
    return this.getOutputSlot().y + BUTTON_Y_OFFSET;
  }
}
