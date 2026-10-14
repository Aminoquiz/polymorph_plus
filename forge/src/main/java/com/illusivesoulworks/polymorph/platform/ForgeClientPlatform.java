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
import com.illusivesoulworks.polymorph.platform.services.IClientPlatform;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;

public class ForgeClientPlatform implements IClientPlatform {

  @Override
  public int getScreenTop(AbstractContainerScreen<?> screen) {
    return screen.getGuiTop();
  }

  @Override
  public int getScreenLeft(AbstractContainerScreen<?> screen) {
    return screen.getGuiLeft();
  }

  @Override
  public void renderTooltip(ItemStack stack, PoseStack poseStack, List<Component> text, int mouseX,
                            int mouseY, AbstractContainerScreen<?> containerScreen, Font font,
                            SelectionWidget.GradientDrawer drawer) {

    if (text.isEmpty()) {
      return;
    }
    RenderTooltipEvent.Pre pre =
        new RenderTooltipEvent.Pre(stack, text, poseStack, mouseX, mouseY, containerScreen.width,
            containerScreen.height, -1, font);

    if (MinecraftForge.EVENT_BUS.post(pre)) {
      return;
    }
    RenderTooltipEvent.Color color =
        new RenderTooltipEvent.Color(stack, text, poseStack, pre.getX(), pre.getY(),
            pre.getFontRenderer(), TooltipRenderer.BACKGROUND, TooltipRenderer.BORDER_START,
            TooltipRenderer.BORDER_END);
    MinecraftForge.EVENT_BUS.post(color);
    TooltipRenderer.draw(poseStack, text, pre.getX(), pre.getY(), pre.getScreenWidth(),
        pre.getScreenHeight(), pre.getFontRenderer(), color.getBackground(),
        color.getBorderStart(), color.getBorderEnd(), drawer);
  }

  @Override
  public int getScreenWidth(AbstractContainerScreen<?> screen) {
    return screen.getXSize();
  }
}
