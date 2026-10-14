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

package com.illusivesoulworks.polymorph.platform;

import com.illusivesoulworks.polymorph.api.client.widget.SelectionWidget;
import com.illusivesoulworks.polymorph.client.TooltipRenderer;
import com.illusivesoulworks.polymorph.mixin.AccessorAbstractContainerScreen;
import com.illusivesoulworks.polymorph.platform.services.IClientPlatform;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class FabricClientPlatform implements IClientPlatform {

  @Override
  public int getScreenTop(AbstractContainerScreen<?> screen) {
    return ((AccessorAbstractContainerScreen) screen).getTopPos();
  }

  @Override
  public int getScreenWidth(AbstractContainerScreen<?> screen) {
    return ((AccessorAbstractContainerScreen) screen).getImageWidth();
  }

  @Override
  public int getScreenLeft(AbstractContainerScreen<?> screen) {
    return ((AccessorAbstractContainerScreen) screen).getLeftPos();
  }

  @Override
  public void renderTooltip(ItemStack stack, PoseStack poseStack, List<Component> text, int mouseX,
                            int mouseY, AbstractContainerScreen<?> containerScreen, Font font,
                            SelectionWidget.GradientDrawer drawer) {
    TooltipRenderer.draw(poseStack, text, mouseX, mouseY, containerScreen.width,
        containerScreen.height, font, TooltipRenderer.BACKGROUND, TooltipRenderer.BORDER_START,
        TooltipRenderer.BORDER_END, drawer);
  }
}
