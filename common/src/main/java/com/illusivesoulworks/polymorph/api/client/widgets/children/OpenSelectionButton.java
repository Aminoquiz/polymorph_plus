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

package com.illusivesoulworks.polymorph.api.client.widgets.children;

import com.illusivesoulworks.polymorph.api.client.base.AbstractRecipesWidget;
import com.illusivesoulworks.polymorph.client.PolymorphClientConfig;
import com.illusivesoulworks.polymorph.client.look.PolymorphLook;
import com.illusivesoulworks.polymorph.platform.Services;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class OpenSelectionButton extends ImageButton {

  private static final int GLYPH_SIZE = 10;
  /** The swap arrows of selector_button.png, rows 3 to 12, bit 9 is the leftmost pixel. */
  private static final int[] GLYPH = {
      0b0000000100,
      0b0000000110,
      0b0011111111,
      0b0110000110,
      0b1100000100,
      0b0010000011,
      0b0110000110,
      0b1111111100,
      0b0110000000,
      0b0010000000,
  };

  private final AbstractContainerScreen<?> containerScreen;
  private final boolean ownSprites;
  private int xOffset;
  private int yOffset;

  public OpenSelectionButton(AbstractContainerScreen<?> containerScreen, int x, int y,
                             WidgetSprites sprites, OnPress onPress) {
    super(0, 0, size(), size(), sprites, onPress);
    this.containerScreen = containerScreen;
    this.ownSprites = sprites == AbstractRecipesWidget.SELECTOR;
    this.xOffset = x;
    this.yOffset = y;
  }

  private static int size() {
    return Math.round(16 * PolymorphClientConfig.getButtonScale());
  }

  public void setOffsets(int x, int y) {
    this.xOffset = x;
    this.yOffset = y;
  }

  public int getXOffset() {
    return this.xOffset;
  }

  public int getYOffset() {
    return this.yOffset;
  }

  @Override
  public void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY,
                           float partialTicks) {
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    // Clamped so a GUI anchored near a window edge cannot push the button out of reach.
    this.setX(clamp(Services.CLIENT_PLATFORM.getScreenLeft(this.containerScreen) + this.xOffset,
        Minecraft.getInstance().getWindow().getGuiScaledWidth() - this.width));
    this.setY(clamp(Services.CLIENT_PLATFORM.getScreenTop(this.containerScreen) + this.yOffset,
        Minecraft.getInstance().getWindow().getGuiScaledHeight() - this.height));
    if (!this.ownSprites || !PolymorphLook.selectorButton(guiGraphics, this.getX(), this.getY(),
        this.width, this.height, this.isHoveredOrFocused(), GLYPH, GLYPH_SIZE)) {
      super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
    }
  }

  private static int clamp(int value, int max) {
    return max < 0 ? 0 : Math.max(0, Math.min(max, value));
  }
}
