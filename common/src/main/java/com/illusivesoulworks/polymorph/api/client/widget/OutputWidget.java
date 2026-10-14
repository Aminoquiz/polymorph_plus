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

import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
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
  private final ResourceLocation resourceLocation;
  private boolean highlighted = false;
  private boolean preferred = false;

  public OutputWidget(IRecipePair recipePair) {
    super(0, 0, 25, 25, TextComponent.EMPTY);
    this.output = recipePair.getOutput();
    this.resourceLocation = recipePair.getResourceLocation();
  }

  @Override
  public void renderButton(@Nonnull PoseStack poseStack, int mouseX, int mouseY,
                           float partialTicks) {
    Minecraft minecraft = Minecraft.getInstance();
    minecraft.getTextureManager().bind(AbstractRecipesWidget.WIDGETS);
    int j = 0;

    if (this.x + 25 > mouseX && this.x <= mouseX &&
        this.y + 25 > mouseY && this.y <= mouseY) {
      j += 25;
    }
    blit(poseStack, this.x, this.y, 200, this.highlighted ? 41 : 16, j, this.width, this.height,
        256, 256);
    int k = 4;
    ItemRenderer itemRenderer = minecraft.getItemRenderer();
    float zLevel = itemRenderer.blitOffset;
    itemRenderer.blitOffset = 60.0F;
    itemRenderer.renderAndDecorateItem(this.getOutput(), this.x + k, this.y + k);
    itemRenderer.renderGuiItemDecorations(minecraft.font, this.getOutput(), this.x + k, this.y + k);
    itemRenderer.blitOffset = zLevel;

    if (this.preferred) {
      // The item and its count draw up to blitOffset + 200, so the star goes above that.
      poseStack.pushPose();
      poseStack.translate(0, 0, 300);
      int sx = this.x + this.width - STAR_SIZE - 2;
      int sy = this.y + 2;
      drawStar(poseStack, sx + 1, sy + 1, STAR_SHADOW);
      drawStar(poseStack, sx, sy, STAR_COLOR);
      poseStack.popPose();
    }
  }

  private static void drawStar(PoseStack poseStack, int x, int y, int color) {

    for (int row = 0; row < STAR.length; row++) {
      int bits = STAR[row];

      for (int col = 0; col < STAR_SIZE; col++) {

        if ((bits & (1 << (STAR_SIZE - 1 - col))) != 0) {
          GuiComponent.fill(poseStack, x + col, y + row, x + col + 1, y + row + 1, color);
        }
      }
    }
  }

  public ItemStack getOutput() {
    return this.output;
  }

  public ResourceLocation getResourceLocation() {
    return this.resourceLocation;
  }

  public void setPosition(int x, int y) {
    this.x = x;
    this.y = y;
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

  public List<Component> getTooltipText(Screen screen) {
    return screen.getTooltipFromItem(this.getOutput());
  }

  @Override
  public int getWidth() {
    return 25;
  }

  @Override
  protected boolean isValidClickButton(int button) {
    return button == 0 || button == 1;
  }
}
