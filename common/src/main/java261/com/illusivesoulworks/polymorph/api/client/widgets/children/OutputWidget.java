/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * 1.21.11 fork. blitSprite takes a RenderPipeline and no z-arg, and isValidClickButton takes
 * MouseButtonInfo rather than an int.
 */
package com.illusivesoulworks.polymorph.api.client.widgets.children;

import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.mojang.datafixers.util.Pair;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class OutputWidget extends AbstractWidget {

  private static final int STAR_COLOR = 0xFFFFD000;
  private static final int STAR_SHADOW = 0xFF3A2A00;
  private static final int STAR_SIZE = 7;
  /** 7x7 bitmap, one int per row, bit 6 is the leftmost pixel. */
  private static final int[] STAR = {
      0b0001000,
      0b0001000,
      0b1111111,
      0b0111110,
      0b0011100,
      0b0110110,
      0b0100010,
  };

  private final ItemStack output;
  private final Identifier resourceLocation;
  private final Pair<WidgetSprites, WidgetSprites> sprites;
  private boolean highlighted = false;
  private boolean preferred = false;

  public OutputWidget(Pair<WidgetSprites, WidgetSprites> sprites, IRecipePair recipePair) {
    super(0, 0, 25, 25, Component.empty());
    this.output = recipePair.getOutput();
    this.resourceLocation = recipePair.getResourceLocation();
    this.sprites = sprites;
  }

  @Override
  protected void renderWidget(@Nonnull GuiGraphics graphics, int mouseX,
                                          int mouseY, float partialTicks) {
    Minecraft minecraft = Minecraft.getInstance();
    WidgetSprites sprite = this.highlighted ? this.sprites.getSecond() : this.sprites.getFirst();
    Identifier texture = sprite.enabled();

    if (this.getX() + 25 > mouseX && this.getX() <= mouseX &&
        this.getY() + 25 > mouseY && this.getY() <= mouseY) {
      texture = sprite.enabledFocused();
    }
    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, this.getX(), this.getY(),
        this.width, this.height);
    int k = 4;
    graphics.renderItem(this.getOutput(), this.getX() + k, this.getY() + k);
    graphics.renderItemDecorations(minecraft.font, this.getOutput(), this.getX() + k, this.getY() + k);

    if (this.preferred) {
      // Marks the source the player told Polymorph to prefer. In-GUI on purpose: the actionbar
      // message this replaces was drawn behind the open inventory and easy to miss.
      int sx = this.getX() + this.width - STAR_SIZE - 2;
      int sy = this.getY() + 2;
      drawStar(graphics, sx + 1, sy + 1, STAR_SHADOW);
      drawStar(graphics, sx, sy, STAR_COLOR);
    }
  }

  private static void drawStar(GuiGraphics graphics, int x, int y, int color) {

    for (int row = 0; row < STAR.length; row++) {
      int bits = STAR[row];

      for (int col = 0; col < STAR_SIZE; col++) {

        if ((bits & (1 << (STAR_SIZE - 1 - col))) != 0) {
          graphics.fill(x + col, y + row, x + col + 1, y + row + 1, color);
        }
      }
    }
  }

  public ItemStack getOutput() {
    return this.output;
  }

  public Identifier getResourceLocation() {
    return this.resourceLocation;
  }

  public void setHighlighted(boolean highlighted) {
    this.highlighted = highlighted;
  }

  public boolean isHighlighted() {
    return this.highlighted;
  }

  public void setPreferred(boolean preferred) {
    this.preferred = preferred;
  }

  public boolean isPreferred() {
    return this.preferred;
  }

  @Override
  public int getWidth() {
    return 25;
  }

  @Override
  protected void updateWidgetNarration(@Nonnull NarrationElementOutput var1) {

  }

  @Override
  protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
    int b = buttonInfo.button();
    return b == 0 || b == 1;
  }
}
