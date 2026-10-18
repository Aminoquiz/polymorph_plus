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

import com.illusivesoulworks.polymorph.client.PolymorphClientConfig;
import com.illusivesoulworks.polymorph.platform.Services;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class OpenSelectionButton extends ImageButton {

  private final AbstractContainerScreen<?> containerScreen;
  private int xOffset;
  private int yOffset;
  private final int size;

  public OpenSelectionButton(AbstractContainerScreen<?> containerScreen, int x, int y,
                             Button.OnPress onPress) {
    super(0, 0, 16, 16, 0, 0, 17, AbstractRecipesWidget.WIDGETS, 256, 256, onPress);
    this.containerScreen = containerScreen;
    this.size = Math.round(16 * PolymorphClientConfig.getButtonScale());
    this.width = this.size;
    this.height = this.size;
    this.xOffset = x;
    this.yOffset = y;
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
  public void renderButton(@Nonnull PoseStack poseStack, int mouseX, int mouseY,
                           float partialTicks) {
    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    // Clamped so a GUI anchored near a window edge cannot push the button out of reach.
    this.x = clamp(Services.CLIENT_PLATFORM.getScreenLeft(this.containerScreen) + this.xOffset,
        Minecraft.getInstance().getWindow().getGuiScaledWidth() - this.width);
    this.y = clamp(Services.CLIENT_PLATFORM.getScreenTop(this.containerScreen) + this.yOffset,
        Minecraft.getInstance().getWindow().getGuiScaledHeight() - this.height);
    if (this.size == 16) {
      super.renderButton(poseStack, mouseX, mouseY, partialTicks);
      return;
    }
    // The texture blit draws width x height texels, so it runs at 16 under a scaled pose.
    float scale = this.size / 16.0F;
    this.width = 16;
    this.height = 16;
    poseStack.pushPose();
    poseStack.translate(this.x, this.y, 0);
    poseStack.scale(scale, scale, 1.0F);
    poseStack.translate(-this.x, -this.y, 0);
    super.renderButton(poseStack, mouseX, mouseY, partialTicks);
    poseStack.popPose();
    this.width = this.size;
    this.height = this.size;
  }

  private static int clamp(int value, int max) {
    return max < 0 ? 0 : Math.max(0, Math.min(max, value));
  }
}
