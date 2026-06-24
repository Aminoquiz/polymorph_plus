/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * MC 26.1 fork. ImageButton now overrides extractContents(GuiGraphicsExtractor, ...)
 * not renderWidget(GuiGraphics, ...). RenderSystem.setShaderColor gone — color tinting
 * goes through GuiGraphicsExtractor blit overloads.
 */
package com.illusivesoulworks.polymorph.api.client.widgets.children;

import com.illusivesoulworks.polymorph.platform.Services;
import javax.annotation.Nonnull;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class OpenSelectionButton extends ImageButton {

  private final AbstractContainerScreen<?> containerScreen;
  private int xOffset;
  private int yOffset;

  public OpenSelectionButton(AbstractContainerScreen<?> containerScreen, int x, int y,
                             WidgetSprites sprites, OnPress onPress) {
    super(0, 0, 16, 16, sprites, onPress);
    this.containerScreen = containerScreen;
    this.xOffset = x;
    this.yOffset = y;
  }

  public void setOffsets(int x, int y) {
    this.xOffset = x;
    this.yOffset = y;
  }

  @Override
  public void extractContents(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                              float partialTicks) {
    this.setX(Services.CLIENT_PLATFORM.getScreenLeft(this.containerScreen) + this.xOffset);
    this.setY(Services.CLIENT_PLATFORM.getScreenTop(this.containerScreen) + this.yOffset);
    super.extractContents(graphics, mouseX, mouseY, partialTicks);
  }
}
