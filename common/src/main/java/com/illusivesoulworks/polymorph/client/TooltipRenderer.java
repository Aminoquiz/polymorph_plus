package com.illusivesoulworks.polymorph.client;

import com.illusivesoulworks.polymorph.api.client.widget.SelectionWidget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public final class TooltipRenderer {

  public static final int BACKGROUND = 0xF0100010;
  public static final int BORDER_START = 0x505000FF;
  public static final int BORDER_END = 0x5028007F;

  // Vanilla draws tooltips at z 400, under the selection buttons at 600.
  private static final int Z_LEVEL = 901;

  private TooltipRenderer() {
  }

  public static void draw(PoseStack poseStack, List<? extends FormattedText> text, int x, int y,
                          int screenWidth, int screenHeight, Font font, int background,
                          int borderStart, int borderEnd, SelectionWidget.GradientDrawer drawer) {

    if (text.isEmpty()) {
      return;
    }
    List<FormattedCharSequence> lines = new ArrayList<>();

    for (FormattedText line : text) {
      lines.add(Language.getInstance().getVisualOrder(line));
    }
    int width = 0;

    for (FormattedCharSequence line : lines) {
      width = Math.max(width, font.width(line));
    }
    int left = x + 12;
    int top = y - 12;
    int height = 8;

    if (lines.size() > 1) {
      height += 2 + (lines.size() - 1) * 10;
    }

    if (left + width > screenWidth) {
      left -= 28 + width;
    }

    if (top + height + 6 > screenHeight) {
      top = screenHeight - height - 6;
    }
    poseStack.pushPose();
    Tesselator tesselator = Tesselator.getInstance();
    BufferBuilder builder = tesselator.getBuilder();
    builder.begin(7, DefaultVertexFormat.POSITION_COLOR);
    Matrix4f matrix = poseStack.last().pose();
    int z = Z_LEVEL;
    drawer.fillGradient(matrix, builder, left - 3, top - 4, left + width + 3, top - 3, z,
        background, background);
    drawer.fillGradient(matrix, builder, left - 3, top + height + 3, left + width + 3,
        top + height + 4, z, background, background);
    drawer.fillGradient(matrix, builder, left - 3, top - 3, left + width + 3, top + height + 3,
        z, background, background);
    drawer.fillGradient(matrix, builder, left - 4, top - 3, left - 3, top + height + 3, z,
        background, background);
    drawer.fillGradient(matrix, builder, left + width + 3, top - 3, left + width + 4,
        top + height + 3, z, background, background);
    drawer.fillGradient(matrix, builder, left - 3, top - 2, left - 2, top + height + 2, z,
        borderStart, borderEnd);
    drawer.fillGradient(matrix, builder, left + width + 2, top - 2, left + width + 3,
        top + height + 2, z, borderStart, borderEnd);
    drawer.fillGradient(matrix, builder, left - 3, top - 3, left + width + 3, top - 2, z,
        borderStart, borderStart);
    drawer.fillGradient(matrix, builder, left - 3, top + height + 2, left + width + 3,
        top + height + 3, z, borderEnd, borderEnd);
    RenderSystem.enableDepthTest();
    RenderSystem.disableTexture();
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    RenderSystem.shadeModel(7425);
    builder.end();
    BufferUploader.end(builder);
    RenderSystem.shadeModel(7424);
    RenderSystem.disableBlend();
    RenderSystem.enableTexture();
    MultiBufferSource.BufferSource buffers =
        MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
    poseStack.translate(0.0D, 0.0D, z);

    for (int i = 0; i < lines.size(); i++) {
      font.drawInBatch(lines.get(i), (float) left, (float) top, -1, true, matrix, buffers, false,
          0, 15728880);

      if (i == 0) {
        top += 2;
      }
      top += 10;
    }
    buffers.endBatch();
    poseStack.popPose();
  }
}
