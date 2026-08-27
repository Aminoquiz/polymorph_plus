/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * 1.21.11 fork. GuiEventListener.mouseClicked takes (MouseButtonEvent, boolean), and tooltips
 * go through GuiGraphics#setTooltipForNextFrame.
 */
package com.illusivesoulworks.polymorph.api.client.widgets.children;

import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.client.PolymorphClientConfig;
import com.illusivesoulworks.polymorph.platform.Services;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
  private static final int[] TIPS_FORWARD = {0, 1, 2, 3, 4, 3, 2, 1, 0};
  private static final int[] TIPS_BACK = {4, 3, 2, 1, 0, 1, 2, 3, 4};

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
  private int scrollOffset = 0;
  private int anchorX;
  private int anchorY;
  private final int[] prevArrow = new int[4];
  private final int[] nextArrow = new int[4];
  private boolean lastClickWasArrow;
  private Identifier lastSelected;

  public SelectionWidget(int x, int y, int xOffset, int yOffset,
                         Pair<WidgetSprites, WidgetSprites> sprites,
                         Consumer<Identifier> onSelect,
                         AbstractContainerScreen<?> containerScreen) {
    this.onSelect = onSelect;
    this.containerScreen = containerScreen;
    this.xOffset = xOffset;
    this.yOffset = yOffset;
    this.sprites = sprites;
    // Last: setPosition runs a full layout pass, which reads containerScreen.
    this.setPosition(x, y);
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
    this.refreshPreferred();
  }

  private int maxScroll() {
    return Math.max(0, this.outputWidgets.size() - MAX_VISIBLE);
  }

  private void clampScroll() {
    int max = this.maxScroll();
    if (this.scrollOffset > max) this.scrollOffset = max;
    if (this.scrollOffset < 0) this.scrollOffset = 0;
  }

  /**
   * Lays the visible buttons out as a row sitting just above the selector button.
   *
   * <p>Upstream anchored the scrollable variant to the top of the container GUI instead, which
   * is off screen in a tall GUI such as AE2's terminal styles. The non-scrolling variant was
   * already anchored to the button, so both now use the same anchor and the row simply follows
   * the button wherever the GUI puts it. Still clamped into the window as a backstop.
   */
  private void updateButtonPositions() {
    this.clampScroll();

    if (this.containerScreen == null) {
      return;
    }
    int size = this.outputWidgets.size();
    int visibleCount = Math.min(MAX_VISIBLE, size);
    int firstVisible = this.scrollOffset;
    int lastVisible = Math.min(size, firstVisible + MAX_VISIBLE) - 1;
    int screenLeft = Services.CLIENT_PLATFORM.getScreenLeft(this.containerScreen);
    int guiWidth = Services.CLIENT_PLATFORM.getScreenWidth(this.containerScreen);
    boolean scrolls = this.canScroll();
    int rowWidth = visibleCount * BUTTON_SIZE;
    int left;

    if (scrolls) {
      // Wider than the button can carry once the arrows are added, so centre it on the GUI.
      left = screenLeft + (guiWidth - rowWidth) / 2;
    } else {
      int rowXOffset = (int) (-BUTTON_SIZE * Math.floor(visibleCount / 2.0F));

      if (visibleCount % 2 == 0) {
        rowXOffset += 13;
      }
      left = this.x + rowXOffset;
    }
    int margin = scrolls ? ARROW_WIDTH + ARROW_GAP : 0;
    this.anchorX = clamp(left, margin, windowWidth() - rowWidth - margin);
    this.anchorY = clamp(this.y, 0, windowHeight() - BUTTON_SIZE);
    setRect(this.prevArrow, this.anchorX - ARROW_GAP - ARROW_WIDTH, this.anchorY, ARROW_WIDTH,
        BUTTON_SIZE);
    setRect(this.nextArrow, this.anchorX + rowWidth + ARROW_GAP, this.anchorY, ARROW_WIDTH,
        BUTTON_SIZE);

    for (int i = 0; i < size; i++) {
      OutputWidget widget = this.outputWidgets.get(i);

      if (i < firstVisible || i > lastVisible) {
        widget.visible = false;
        widget.setPosition(Integer.MIN_VALUE / 2, Integer.MIN_VALUE / 2);
        continue;
      }
      widget.visible = true;
      widget.setPosition(this.anchorX + (i - firstVisible) * BUTTON_SIZE, this.anchorY);
    }
  }

  /**
   * The star means one thing only: this recipe is a favourite. The namespace and pack lists
   * are deliberately not mirrored here, since they rank below the player's remembered click
   * and marking them would promise a win the resolution does not give them.
   */
  private static int preferenceRank(Identifier id) {
    return PolymorphClientConfig.recipeRankOf(id.toString());
  }

  public void refreshPreferred() {
    OutputWidget marked = null;
    int best = Integer.MAX_VALUE;

    for (OutputWidget widget : this.outputWidgets) {
      int rank = preferenceRank(widget.getResourceLocation());

      // Ties go to the highlighted candidate so a shift-click always stars the button that was
      // clicked, then to list order, which is how the server breaks ties too.
      if (rank < best || (rank == best && rank != Integer.MAX_VALUE && widget.isHighlighted()
          && (marked == null || !marked.isHighlighted()))) {
        best = rank;
        marked = widget;
      }
    }
    boolean ranked = best != Integer.MAX_VALUE;

    for (OutputWidget widget : this.outputWidgets) {
      widget.setPreferred(ranked && widget == marked);
    }
  }

  private static void setRect(int[] rect, int x, int y, int width, int height) {
    rect[0] = x;
    rect[1] = y;
    rect[2] = width;
    rect[3] = height;
  }

  private static int clamp(int value, int min, int max) {
    if (max < min) {
      return min;
    }
    return Math.max(min, Math.min(max, value));
  }

  private static int windowWidth() {
    return Minecraft.getInstance().getWindow().getGuiScaledWidth();
  }

  private static int windowHeight() {
    return Minecraft.getInstance().getWindow().getGuiScaledHeight();
  }

  private static void drawArrowH(GuiGraphics gg, int x, int y, boolean rightFacing,
                                 boolean enabled) {
    gg.fill(x, y, x + ARROW_WIDTH, y + BUTTON_SIZE, ARROW_BG);
    int fg = enabled ? ARROW_FG_ON : ARROW_FG_OFF;
    int cy = y + BUTTON_SIZE / 2;
    int[] offsets = {-4, -3, -2, -1, 0, 1, 2, 3, 4};
    int[] tips = rightFacing ? TIPS_FORWARD : TIPS_BACK;

    for (int i = 0; i < offsets.length; i++) {
      int px = x + tips[i];
      int py = cy + offsets[i];
      gg.fill(px, py, px + 1, py + 1, fg);
    }
  }

  private void drawArrow(GuiGraphics gg, int[] rect, boolean forward, boolean enabled) {
    drawArrowH(gg, rect[0], rect[1], forward, enabled);
  }

  private static boolean isOver(int[] rect, double mouseX, double mouseY) {
    return mouseX >= rect[0] && mouseX < rect[0] + rect[2]
        && mouseY >= rect[1] && mouseY < rect[1] + rect[3];
  }

  public boolean canScroll() {
    return this.maxScroll() > 0;
  }

  /**
   * Scrolls the visible row so that {@code resourceLocation} is on screen. Used by the
   * shift+scroll cycling on the output slot: the selection can move to an entry that is
   * currently paged out, and a pinned selector must follow it instead of going stale.
   */
  public void scrollIntoView(Identifier resourceLocation) {
    if (!this.canScroll()) {
      return;
    }
    int index = this.indexOf(resourceLocation);
    if (index < 0) {
      return;
    }
    int previous = this.scrollOffset;
    if (index < this.scrollOffset) {
      this.scrollOffset = index;
    } else if (index >= this.scrollOffset + MAX_VISIBLE) {
      this.scrollOffset = index - MAX_VISIBLE + 1;
    }
    this.clampScroll();
    if (this.scrollOffset != previous) {
      this.updateButtonPositions();
    }
  }

  public int indexOf(Identifier resourceLocation) {
    for (int i = 0; i < this.outputWidgets.size(); i++) {
      if (this.outputWidgets.get(i).getResourceLocation().equals(resourceLocation)) {
        return i;
      }
    }
    return -1;
  }

  public boolean wasLastClickArrow() {
    return this.lastClickWasArrow;
  }

  /**
   * The recipe the last accepted click landed on. The select callback is fired from inside
   * this widget, so callers that need to know what was picked (shift-click to remember a
   * source preference) read it back from here instead of duplicating the hit test.
   */
  public Identifier getLastSelected() {
    return this.lastSelected;
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
    this.refreshPreferred();
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

  public void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
    Minecraft mc = Minecraft.getInstance();

    if (mc.screen != null && this.hoveredButton != null) {
      graphics.setTooltipForNextFrame(mc.font, this.hoveredButton.getOutput(), mouseX, mouseY);
    }
  }

  @Override
  public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY,
                                 float partialTicks) {

    if (this.isActive()) {
      int x = Services.CLIENT_PLATFORM.getScreenLeft(this.containerScreen) + this.xOffset;
      int y = Services.CLIENT_PLATFORM.getScreenTop(this.containerScreen) + this.yOffset;

      // Recomputed every frame: the resolved orientation depends on the window size and on a
      // config key that can change while the screen is open, not only on x/y.
      this.setPosition(x, y);
      this.hoveredButton = null;
      this.outputWidgets.forEach(button -> {
        button.render(graphics, mouseX, mouseY, partialTicks);

        if (button.visible && button.isHoveredOrFocused()) {
          this.hoveredButton = button;
        }
      });
      if (this.canScroll()) {
        this.drawArrow(graphics, this.prevArrow, false, this.scrollOffset > 0);
        this.drawArrow(graphics, this.nextArrow, true, this.scrollOffset < this.maxScroll());
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
        if (isOver(this.prevArrow, mx, my)) {
          if (this.scrollOffset > 0) {
            this.scrollOffset--;
            this.updateButtonPositions();
          }
          this.lastClickWasArrow = true;
          return true;
        }
        if (isOver(this.nextArrow, mx, my)) {
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
          this.lastSelected = widget.getResourceLocation();
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
