/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * 1.21.11 fork. AbstractWidget.mouseClicked takes (MouseButtonEvent, boolean), so the mod's
 * (mouseX, mouseY, button) call is wrapped into a synthetic MouseButtonEvent.
 */
package com.illusivesoulworks.polymorph.api.client.base;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.client.widgets.children.OpenSelectionButton;
import com.illusivesoulworks.polymorph.api.client.widgets.children.OutputWidget;
import com.illusivesoulworks.polymorph.api.client.widgets.children.SelectionWidget;
import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.client.PolymorphClientConfig;
import com.illusivesoulworks.polymorph.client.TutorialOverlay;
import com.illusivesoulworks.polymorph.platform.Services;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;

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
    this.applyPinState();
  }

  private void applyPinState() {
    if (this.selectionWidget != null
        && PolymorphClientConfig.isPinSelector()
        && this.selectionWidget.getOutputWidgets().size() > 1) {
      this.selectionWidget.setActive(true);
    }
  }

  /**
   * Shift+scroll on the result slot cycles the conflicting outputs without opening the
   * selector. Deliberately gated on BOTH the shift modifier and the pointer being inside
   * the 16x16 result slot, so plain scrolling and inventory-sorting mods are untouched.
   */
  private boolean isOverOutputSlot(double mouseX, double mouseY) {
    Slot slot = this.getOutputSlot();

    if (slot == null) {
      return false;
    }
    int x = Services.CLIENT_PLATFORM.getScreenLeft(this.containerScreen) + slot.x;
    int y = Services.CLIENT_PLATFORM.getScreenTop(this.containerScreen) + slot.y;
    return mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
  }

  private static boolean isShiftDown() {
    Minecraft minecraft = Minecraft.getInstance();
    Window window = minecraft.getWindow();

    if (window == null) {
      return false;
    }
    // MC 26.1 dropped Screen.hasShiftDown(); query GLFW directly instead.
    return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
        || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
  }

  private boolean cycleSelection(double scrollY) {

    if (this.selectionWidget == null) {
      return false;
    }
    List<OutputWidget> outputs = this.selectionWidget.getOutputWidgets();

    if (outputs.size() < 2) {
      return false;
    }
    // Scroll up walks towards the start of the list, matching the selector's own arrows.
    int step = scrollY > 0 ? -1 : (scrollY < 0 ? 1 : 0);

    if (step == 0) {
      return false;
    }
    int current = -1;

    for (int i = 0; i < outputs.size(); i++) {

      if (outputs.get(i).isHighlighted()) {
        current = i;
        break;
      }
    }
    int next = current < 0 ? (step > 0 ? 0 : outputs.size() - 1)
        : Math.floorMod(current + step, outputs.size());
    Identifier target = outputs.get(next).getResourceLocation();
    // Highlight locally first so the feedback is immediate; the server echoes the same
    // selection back through the recipes-list sync.
    this.highlightRecipe(target);
    this.selectionWidget.scrollIntoView(target);
    this.selectRecipe(target);
    TutorialOverlay.onRecipePicked();
    TutorialOverlay.onScrollCycled();
    return true;
  }

  private boolean isOverOpenButton(double mouseX, double mouseY) {
    return this.openButton != null && this.openButton.visible
        && mouseX >= this.openButton.getX() && mouseX < this.openButton.getX() + 16
        && mouseY >= this.openButton.getY() && mouseY < this.openButton.getY() + 16;
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

    if (this.selectionWidget != null) {
      this.selectionWidget.highlightButton(resourceLocation);
    }
  }

  @Override
  public void setRecipesList(Set<IRecipePair> recipesList, Identifier selected) {

    if (this.selectionWidget == null || this.openButton == null) {
      return;
    }
    SortedSet<IRecipePair> sorted = new TreeSet<>(recipesList);
    this.selectionWidget.setRecipeList(sorted);
    this.openButton.visible = recipesList.size() > 1;

    if (selected != null) {
      this.highlightRecipe(selected);
    }
    this.applyPinState();
  }

  @Override
  public void render(GuiGraphics guiGraphics, int mouseX, int mouseY,
                     float renderPartialTicks) {

    // initChildWidgets can be abandoned partway through if a screen it does not know about
    // throws while being measured. Degrade to no selector rather than crashing every frame.
    if (this.selectionWidget == null || this.openButton == null) {
      return;
    }
    this.selectionWidget.render(guiGraphics, mouseX, mouseY, renderPartialTicks);
    this.openButton.render(guiGraphics, mouseX, mouseY, renderPartialTicks);
    if (PolymorphClientConfig.isPinSelector() && this.openButton.visible) {
      int bx = this.openButton.getX();
      int by = this.openButton.getY();
      guiGraphics.fill(bx + 11, by, bx + 16, by + 5, 0xFF202020);
      guiGraphics.fill(bx + 12, by + 1, bx + 15, by + 4, 0xFFFFD000);
    }
    TutorialOverlay.renderForOpenButton(guiGraphics, this.openButton.getX(),
        this.openButton.getY(), this.openButton.visible);
    TutorialOverlay.renderForSelector(guiGraphics,
        Services.CLIENT_PLATFORM.getScreenLeft(this.containerScreen) + 16,
        Services.CLIENT_PLATFORM.getScreenTop(this.containerScreen),
        this.selectionWidget.isActive(), this.selectionWidget.canScroll());
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {

    if (this.selectionWidget == null || this.openButton == null) {
      return false;
    }
    if (button == 1 && this.isOverOpenButton(mouseX, mouseY)) {
      boolean next = !PolymorphClientConfig.isPinSelector();
      PolymorphClientConfig.setPinSelector(next);
      if (next) {
        this.applyPinState();
      }
      TutorialOverlay.onPinToggled();
      return true;
    }
    MouseButtonEvent event = new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0));
    boolean pinned = PolymorphClientConfig.isPinSelector();

    if (button == 0 && pinned && this.isOverOpenButton(mouseX, mouseY)) {
      PolymorphClientConfig.setPinSelector(false);
      this.selectionWidget.setActive(false);
      return true;
    }
    if (this.openButton.mouseClicked(event, false)) {
      TutorialOverlay.onOpenButtonClicked();
      return true;
    } else if (this.selectionWidget.mouseClicked(event, false)) {
      boolean remembered = false;

      if (this.selectionWidget.wasLastClickArrow()) {
        TutorialOverlay.onScrolledOrArrowClicked();
      } else {
        TutorialOverlay.onRecipePicked();
        remembered = this.rememberSourceIfShiftHeld();
      }
      // A plain pick closes the panel, a shift-click does not: it just marked a preference
      // with a star on the buttons, and closing would hide the only confirmation there is.
      if (!pinned && !remembered && !this.selectionWidget.wasLastClickArrow()) {
        this.selectionWidget.setActive(false);
      }
      return true;
    } else if (this.selectionWidget.isActive()) {

      if (pinned) {
        return false;
      }
      if (!this.openButton.mouseClicked(event, false)) {
        this.selectionWidget.setActive(false);
      }
      return true;
    }
    return false;
  }

  /**
   * Shift-clicking an output means "always give me this outcome": the recipe's id is moved to
   * the front of the player preference list and re-uploaded, so this conflict and any other one
   * offering the same recipe resolve without a click.
   *
   * <p>Deliberately per recipe, not per mod. A craft yields a single outcome, so a namespace
   * rule cannot settle a conflict whose candidates come from the same mod, which is the common
   * case in a large pack. The namespace list is still consulted, just below this one, and stays
   * hand-editable for blanket rules.
   *
   * <p>Feedback is the star the selector then draws on the winning candidate rather than an
   * actionbar message, which the open inventory covers.
   */
  private boolean rememberSourceIfShiftHeld() {

    if (!isShiftDown()) {
      return false;
    }
    Identifier picked = this.selectionWidget.getLastSelected();

    if (picked == null) {
      return false;
    }

    // Only worth a packet when the list actually moved, but the stars are refreshed either
    // way so a repeat shift-click still shows the player where the preference sits.
    if (PolymorphClientConfig.promoteRecipe(picked.toString())) {
      PolymorphApi.getInstance().getNetwork()
          .sendPlayerPriorityC2S(PolymorphClientConfig.getPriorityNamespaces(),
              PolymorphClientConfig.getPriorityRecipes());
    }
    this.selectionWidget.refreshPreferred();
    TutorialOverlay.onSourceRemembered();
    return true;
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
    if (PolymorphClientConfig.isScrollCycle() && isShiftDown()
        && this.isOverOutputSlot(mouseX, mouseY) && this.cycleSelection(scrollY)) {
      return true;
    }
    if (this.selectionWidget != null && this.selectionWidget.isActive()) {
      boolean consumed = this.selectionWidget.mouseScrolled(mouseX, mouseY, scrollY);
      if (consumed) {
        TutorialOverlay.onScrolledOrArrowClicked();
      }
      return consumed;
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
