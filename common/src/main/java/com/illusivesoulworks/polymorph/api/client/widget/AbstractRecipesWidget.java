/*
 * Copyright (C) 2020-2022 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Polymorph is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Polymorph.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.illusivesoulworks.polymorph.api.client.widget;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.client.base.IRecipesWidget;
import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.platform.Services;
import com.mojang.blaze3d.vertex.PoseStack;
import com.illusivesoulworks.polymorph.client.PolymorphClientConfig;
import com.illusivesoulworks.polymorph.client.TutorialOverlay;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractRecipesWidget implements IRecipesWidget {

  public static final ResourceLocation WIDGETS =
      new ResourceLocation(PolymorphApi.MOD_ID, "textures/gui/widgets.png");
  public static final int BUTTON_X_OFFSET = 0;
  public static final int BUTTON_Y_OFFSET = -22;
  public static final int WIDGET_X_OFFSET = -4;
  public static final int WIDGET_Y_OFFSET = -26;

  protected final AbstractContainerScreen<?> containerScreen;
  protected final int xOffset;
  protected final int yOffset;
  private int anchorX;
  private int anchorY;

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
    this.anchorX = this.getXPos();
    this.anchorY = this.getYPos();
    int x = Services.CLIENT_PLATFORM.getScreenLeft(this.containerScreen) + this.getXPos();
    int y = Services.CLIENT_PLATFORM.getScreenTop(this.containerScreen) + this.getYPos();
    this.selectionWidget =
        new SelectionWidget(x + this.xOffset, y + this.yOffset, this.getXPos() + this.xOffset,
            this.getYPos() + this.yOffset, this::selectRecipe, this.containerScreen);
    this.openButton = new OpenSelectionButton(this.containerScreen, this.getXPos(), this.getYPos(),
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

  // Gated on shift AND the 16x16 result slot so plain scrolling and sorting mods are untouched.
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
    return Screen.hasShiftDown();
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
    ResourceLocation target = outputs.get(next).getResourceLocation();
    // Highlighted locally first for immediate feedback; the server echoes the same selection.
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

  // Moves by the anchor shift only, so offsets a subclass set in initChildWidgets (Traveler's
  // Backpack's own widget) survive.
  protected void resetWidgetOffsets() {
    int dx = this.getXPos() - this.anchorX;
    int dy = this.getYPos() - this.anchorY;

    if (dx == 0 && dy == 0) {
      return;
    }
    this.anchorX += dx;
    this.anchorY += dy;
    this.selectionWidget.setOffsets(this.selectionWidget.getXOffset() + dx,
        this.selectionWidget.getYOffset() + dy);
    this.openButton.setOffsets(this.openButton.getXOffset() + dx,
        this.openButton.getYOffset() + dy);
  }

  @Override
  public abstract void selectRecipe(ResourceLocation resourceLocation);

  @Override
  public SelectionWidget getSelectionWidget() {
    return selectionWidget;
  }

  @Override
  public void highlightRecipe(ResourceLocation resourceLocation) {

    if (this.selectionWidget != null) {
      this.selectionWidget.highlightButton(resourceLocation);
    }
  }

  @Override
  public void setRecipesList(Set<IRecipePair> recipesList, ResourceLocation selected) {

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
  public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float renderPartialTicks) {

    // initChildWidgets can be abandoned partway on an unknown screen: degrade, do not crash.
    if (this.selectionWidget == null || this.openButton == null) {
      return;
    }
    // Some screens move their slots after init (Traveler's Backpack tabs).
    this.resetWidgetOffsets();
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
    boolean pinned = PolymorphClientConfig.isPinSelector();

    if (button == 0 && pinned && this.isOverOpenButton(mouseX, mouseY)) {
      PolymorphClientConfig.setPinSelector(false);
      this.selectionWidget.setActive(false);
      return true;
    }

    if (this.openButton.mouseClicked(mouseX, mouseY, button)) {
      TutorialOverlay.onOpenButtonClicked();
      return true;
    } else if (this.selectionWidget.mouseClicked(mouseX, mouseY, button)) {
      boolean remembered = false;

      if (this.selectionWidget.wasLastClickArrow()) {
        TutorialOverlay.onScrolledOrArrowClicked();
      } else {
        TutorialOverlay.onRecipePicked();
        remembered = this.rememberSourceIfShiftHeld();
      }
      // A shift-click keeps the panel open: the star it draws is the only confirmation.
      if (!pinned && !remembered && !this.selectionWidget.wasLastClickArrow()) {
        this.selectionWidget.setActive(false);
      }
      return true;
    } else if (this.selectionWidget.isActive()) {

      if (pinned) {
        return false;
      }

      if (!this.openButton.mouseClicked(mouseX, mouseY, button)) {
        this.selectionWidget.setActive(false);
      }
      return true;
    }
    return false;
  }

  /**
   * Shift-click marks the picked recipe as a favourite: it moves to the front of the player's
   * recipe preference list, which is re-uploaded, so every conflict offering it resolves to it.
   */
  private boolean rememberSourceIfShiftHeld() {

    if (!isShiftDown()) {
      return false;
    }
    ResourceLocation picked = this.selectionWidget.getLastSelected();

    if (picked == null) {
      return false;
    }

    if (PolymorphClientConfig.promoteRecipe(picked.toString())) {
      PolymorphApi.common().getPacketDistributor()
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
