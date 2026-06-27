/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * MC 26.1 fork. Renderable.render(GuiGraphics, ...) collapsed into
 * extractRenderState(GuiGraphicsExtractor, ...). GuiEventListener.mouseClicked now takes
 * (MouseButtonEvent, boolean). Tooltips: use GuiGraphicsExtractor#setTooltipForNextFrame
 * (the immediate renderTooltip overload is gone in the extract phase).
 */
package com.illusivesoulworks.polymorph.api.client.widgets.children;

import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.platform.Services;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;

public class SelectionWidget implements Renderable, GuiEventListener {

  public static final int BUTTON_SIZE = 25;
  public static final int MAX_VISIBLE = 7;
  public static final int ARROW_WIDTH = 5;
  public static final int ARROW_GAP = 1;
  private static final int ARROW_BG = 0xCC000000;
  private static final int ARROW_FG_ON = 0xFFFFFFFF;
  private static final int ARROW_FG_OFF = 0x66FFFFFF;

  private final Consumer<Identifier> onSelect;
  private final AbstractContainerScreen<?> containerScreen;
  private final List<OutputWidget> outputWidgets = new ArrayList<>();
  private final Pair<WidgetSprites, WidgetSprites> sprites;
  private int xOffset;
  private int yOffset;

  private OutputWidget hoveredButton;
  private boolean active = false;
  private int x;
  private int y;
  private int lastX;
  private int lastY;
  private int scrollOffset = 0;
  private int leftArrowX;
  private int rightArrowX;
  private int anchorY;
  private boolean lastClickWasArrow;

  public SelectionWidget(int x, int y, int xOffset, int yOffset,
                         Pair<WidgetSprites, WidgetSprites> sprites,
                         Consumer<Identifier> onSelect,
                         AbstractContainerScreen<?> containerScreen) {
    this.setPosition(x, y);
    this.onSelect = onSelect;
    this.containerScreen = containerScreen;
    this.xOffset = xOffset;
    this.yOffset = yOffset;
    this.sprites = sprites;
  }

  public void setPosition(int x, int y) {
    this.x = x;
    this.y = y;
    this.updateButtonPositions();
  }

  public void setOffsets(int x, int y) {
    this.xOffset = x;
    this.yOffset = y;
  }

  public void highlightButton(Identifier resourceLocation) {
    this.outputWidgets.forEach(
        widget -> widget.setHighlighted(widget.getResourceLocation().equals(resourceLocation)));
  }

  private int maxScroll() {
    return Math.max(0, this.outputWidgets.size() - MAX_VISIBLE);
  }

  private void clampScroll() {
    int max = this.maxScroll();
    if (this.scrollOffset > max) this.scrollOffset = max;
    if (this.scrollOffset < 0) this.scrollOffset = 0;
  }

  private void updateButtonPositions() {
    this.clampScroll();
    int size = this.outputWidgets.size();
    int visibleCount = Math.min(MAX_VISIBLE, size);
    int firstVisible = this.scrollOffset;
    int lastVisible = Math.min(size, firstVisible + MAX_VISIBLE) - 1;
    int rowLeft;
    if (this.canScroll()) {
      int rowWidth = visibleCount * BUTTON_SIZE;
      int screenLeft = Services.CLIENT_PLATFORM.getScreenLeft(this.containerScreen);
      rowLeft = screenLeft + (176 - rowWidth) / 2;
      this.anchorY = Services.CLIENT_PLATFORM.getScreenTop(this.containerScreen) - BUTTON_SIZE - 1;
    } else {
      int rowXOffset = (int) (-BUTTON_SIZE * Math.floor(visibleCount / 2.0F));
      if (visibleCount % 2 == 0) rowXOffset += 13;
      rowLeft = this.x + rowXOffset;
      this.anchorY = this.y;
    }

    for (int i = 0; i < size; i++) {
      OutputWidget widget = this.outputWidgets.get(i);
      if (i < firstVisible || i > lastVisible) {
        widget.visible = false;
        widget.setPosition(Integer.MIN_VALUE / 2, Integer.MIN_VALUE / 2);
        continue;
      }
      int relIdx = i - firstVisible;
      int px = rowLeft + relIdx * BUTTON_SIZE;
      widget.visible = true;
      widget.setPosition(px, this.anchorY);
    }
    this.leftArrowX = rowLeft - ARROW_GAP - ARROW_WIDTH;
    this.rightArrowX = rowLeft + visibleCount * BUTTON_SIZE + ARROW_GAP;
  }

  private static void drawArrow(GuiGraphicsExtractor gg, int x, int y, boolean rightFacing,
                                boolean enabled) {
    gg.fill(x, y, x + ARROW_WIDTH, y + BUTTON_SIZE, ARROW_BG);
    int fg = enabled ? ARROW_FG_ON : ARROW_FG_OFF;
    int cy = y + BUTTON_SIZE / 2;
    int[] dy = {-4, -3, -2, -1, 0, 1, 2, 3, 4};
    int[] tipsRight = {0, 1, 2, 3, 4, 3, 2, 1, 0};
    int[] tipsLeft = {4, 3, 2, 1, 0, 1, 2, 3, 4};
    int[] tips = rightFacing ? tipsRight : tipsLeft;
    for (int i = 0; i < dy.length; i++) {
      int px = x + tips[i];
      int py = cy + dy[i];
      gg.fill(px, py, px + 1, py + 1, fg);
    }
  }

  private boolean isOverLeftArrow(double mouseX, double mouseY) {
    return mouseX >= this.leftArrowX && mouseX < this.leftArrowX + ARROW_WIDTH
        && mouseY >= this.anchorY && mouseY < this.anchorY + BUTTON_SIZE;
  }

  private boolean isOverRightArrow(double mouseX, double mouseY) {
    return mouseX >= this.rightArrowX && mouseX < this.rightArrowX + ARROW_WIDTH
        && mouseY >= this.anchorY && mouseY < this.anchorY + BUTTON_SIZE;
  }

  public boolean canScroll() {
    return this.maxScroll() > 0;
  }

  public boolean wasLastClickArrow() {
    return this.lastClickWasArrow;
  }

  public List<OutputWidget> getOutputWidgets() {
    return outputWidgets;
  }

  public void setRecipeList(Set<IRecipePair> recipeList) {
    this.outputWidgets.clear();
    recipeList.forEach(data -> {
      if (!data.getOutput().isEmpty()) {
        this.outputWidgets.add(new OutputWidget(this.sprites, data));
      }
    });
    this.scrollOffset = 0;
    this.updateButtonPositions();
  }

  public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
    if (!this.isActive() || !this.canScroll()) {
      return false;
    }
    int delta = scrollY > 0 ? 1 : (scrollY < 0 ? -1 : 0);
    if (delta == 0) {
      return false;
    }
    int previous = this.scrollOffset;
    this.scrollOffset += delta;
    this.clampScroll();
    if (this.scrollOffset != previous) {
      this.updateButtonPositions();
      return true;
    }
    return false;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean isActive() {
    return this.active;
  }

  public void renderTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    Minecraft mc = Minecraft.getInstance();

    if (mc.gui.screen() != null && this.hoveredButton != null) {
      graphics.setTooltipForNextFrame(mc.font, this.hoveredButton.getOutput(), mouseX, mouseY);
    }
  }

  @Override
  public void extractRenderState(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                 float partialTicks) {

    if (this.isActive()) {
      int x = Services.CLIENT_PLATFORM.getScreenLeft(this.containerScreen) + this.xOffset;
      int y = Services.CLIENT_PLATFORM.getScreenTop(this.containerScreen) + this.yOffset;

      if (this.lastX != x || this.lastY != y) {
        this.setPosition(x, y);
        this.lastX = x;
        this.lastY = y;
      }
      this.hoveredButton = null;
      this.outputWidgets.forEach(button -> {
        button.extractRenderState(graphics, mouseX, mouseY, partialTicks);

        if (button.visible && button.isHoveredOrFocused()) {
          this.hoveredButton = button;
        }
      });
      if (this.canScroll()) {
        drawArrow(graphics, this.leftArrowX, this.anchorY, false, this.scrollOffset > 0);
        drawArrow(graphics, this.rightArrowX, this.anchorY, true,
            this.scrollOffset < this.maxScroll());
      }
      this.renderTooltip(graphics, mouseX, mouseY);
    }
  }

  @Override
  public boolean mouseClicked(@Nonnull MouseButtonEvent event, boolean doubleClick) {

    this.lastClickWasArrow = false;
    if (this.isActive()) {
      if (this.canScroll()) {
        double mx = event.x();
        double my = event.y();
        if (this.isOverLeftArrow(mx, my)) {
          if (this.scrollOffset > 0) {
            this.scrollOffset--;
            this.updateButtonPositions();
          }
          this.lastClickWasArrow = true;
          return true;
        }
        if (this.isOverRightArrow(mx, my)) {
          if (this.scrollOffset < this.maxScroll()) {
            this.scrollOffset++;
            this.updateButtonPositions();
          }
          this.lastClickWasArrow = true;
          return true;
        }
      }

      for (OutputWidget widget : this.outputWidgets) {

        if (widget.visible && widget.mouseClicked(event, doubleClick)) {
          onSelect.accept(widget.getResourceLocation());
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void setFocused(boolean var1) {

  }

  @Override
  public boolean isFocused() {
    return false;
  }
}
