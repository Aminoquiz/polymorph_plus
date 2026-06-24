/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * MC 26.1 fork. Two-phase GUI render: AbstractWidget now exposes
 * renderWidget(GuiGraphics, ...) instead of renderWidget(GuiGraphics, ...).
 * blitSprite signature dropped the z-arg; GuiGraphics's renderItem/renderItemDecorations
 * became GuiGraphics.item/itemDecorations. The new GuiGraphics handles
 * layering internally — no manual pose translate(0, 0, z) needed.
 * isValidClickButton now takes MouseButtonInfo, not int.
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

  private final ItemStack output;
  private final Identifier resourceLocation;
  private final Pair<WidgetSprites, WidgetSprites> sprites;
  private boolean highlighted = false;

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
