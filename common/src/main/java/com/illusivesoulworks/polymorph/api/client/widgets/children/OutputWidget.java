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

import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class OutputWidget extends AbstractWidget {

  private final ItemStack output;
  private final ResourceLocation resourceLocation;
  private final Pair<WidgetSprites, WidgetSprites> sprites;
  private boolean highlighted = false;

  public OutputWidget(Pair<WidgetSprites, WidgetSprites> sprites, IRecipePair recipePair) {
    super(0, 0, 25, 25, Component.empty());
    this.output = recipePair.getOutput();
    this.resourceLocation = recipePair.getResourceLocation();
    this.sprites = sprites;
  }

  @Override
  public void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY,
                           float partialTicks) {
    Minecraft minecraft = Minecraft.getInstance();
    PoseStack poseStack = guiGraphics.pose();
    WidgetSprites sprite = this.highlighted ? this.sprites.getSecond() : this.sprites.getFirst();
    ResourceLocation texture = sprite.enabled();

    if (this.getX() + 25 > mouseX && this.getX() <= mouseX &&
        this.getY() + 25 > mouseY && this.getY() <= mouseY) {
      texture = sprite.enabledFocused();
    }
    guiGraphics.blitSprite(texture, this.getX(), this.getY(), 600, this.width, this.height);
    int k = 4;
    poseStack.pushPose();
    poseStack.translate(0, 0, 700);
    guiGraphics.renderItem(this.getOutput(), this.getX() + k, this.getY() + k);
    guiGraphics.renderItemDecorations(minecraft.font, this.getOutput(), this.getX() + k,
        this.getY() + k);
    poseStack.popPose();
  }

  public ItemStack getOutput() {
    return this.output;
  }

  public ResourceLocation getResourceLocation() {
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
  protected boolean isValidClickButton(int button) {
    return button == 0 || button == 1;
  }
}
