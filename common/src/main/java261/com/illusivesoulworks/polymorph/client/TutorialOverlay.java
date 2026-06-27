/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * 1.21.11 fork. MC 26.x renamed GuiGraphics#drawString to GuiGraphics#text; 1.21.11 still
 * exposes drawString(Font, FormattedCharSequence, int, int, int, boolean), so this fork
 * keeps that call. Everything else mirrors the java/ source.
 */
package com.illusivesoulworks.polymorph.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public final class TutorialOverlay {

  private static final int MAX_WIDTH = 150;
  private static final int PADDING = 4;
  private static final int BG_COLOR = 0xEE101010;
  private static final int BORDER_COLOR = 0xFFFFD000;
  private static final int TEXT_COLOR = 0xFFFFFFFF;
  private static final long AUTO_DISMISS_MS = 8000L;

  private static int currentStep = -1;
  private static long shownAt = 0L;

  private TutorialOverlay() {
  }

  public static void renderForOpenButton(GuiGraphics gg, int btnX, int btnY,
                                         boolean openButtonVisible) {
    int step = PolymorphClientConfig.getTutorialStep();
    if (step == 0 && openButtonVisible) {
      ensureTimer(0);
      drawBubble(gg, "polymorph_plus.tutorial.conflict", btnX + 20, btnY - 2);
      autoDismiss(0);
    } else if (step == 2 && openButtonVisible) {
      ensureTimer(2);
      drawBubble(gg, "polymorph_plus.tutorial.pin", btnX + 20, btnY - 2);
      autoDismiss(2);
    }
  }

  public static void renderForSelector(GuiGraphics gg, int screenLeft, int screenTop,
                                       boolean selectorActive, boolean canScroll) {
    int step = PolymorphClientConfig.getTutorialStep();
    if (step == 1 && selectorActive) {
      ensureTimer(1);
      drawBubble(gg, "polymorph_plus.tutorial.select", screenLeft, screenTop + 170);
      autoDismiss(1);
    } else if (step == 3 && selectorActive && canScroll) {
      ensureTimer(3);
      drawBubble(gg, "polymorph_plus.tutorial.scroll", screenLeft, screenTop + 170);
      autoDismiss(3);
    }
  }

  public static void onScrolledOrArrowClicked() {
    if (PolymorphClientConfig.getTutorialStep() == 3) {
      PolymorphClientConfig.setTutorialStep(4);
      currentStep = -1;
    }
  }

  public static void onOpenButtonClicked() {
    if (PolymorphClientConfig.getTutorialStep() == 0) {
      PolymorphClientConfig.setTutorialStep(1);
      currentStep = -1;
    }
  }

  public static void onRecipePicked() {
    if (PolymorphClientConfig.getTutorialStep() == 1) {
      PolymorphClientConfig.setTutorialStep(2);
      currentStep = -1;
    }
  }

  public static void onPinToggled() {
    if (PolymorphClientConfig.getTutorialStep() == 2) {
      PolymorphClientConfig.setTutorialStep(3);
      currentStep = -1;
    }
  }

  private static void ensureTimer(int step) {
    if (currentStep != step) {
      currentStep = step;
      shownAt = System.currentTimeMillis();
    }
  }

  private static void autoDismiss(int step) {
    if (System.currentTimeMillis() - shownAt > AUTO_DISMISS_MS) {
      PolymorphClientConfig.setTutorialStep(step + 1);
      currentStep = -1;
    }
  }

  private static void drawBubble(GuiGraphics gg, String langKey, int x, int y) {
    Font font = Minecraft.getInstance().font;
    Component msg = Component.translatable(langKey);
    List<FormattedCharSequence> lines = font.split(msg, MAX_WIDTH);
    int textWidth = 0;
    for (FormattedCharSequence line : lines) {
      textWidth = Math.max(textWidth, font.width(line));
    }
    int boxW = textWidth + PADDING * 2;
    int boxH = lines.size() * font.lineHeight + PADDING * 2;
    gg.fill(x, y, x + boxW, y + boxH, BG_COLOR);
    gg.fill(x, y, x + boxW, y + 1, BORDER_COLOR);
    gg.fill(x, y + boxH - 1, x + boxW, y + boxH, BORDER_COLOR);
    gg.fill(x, y, x + 1, y + boxH, BORDER_COLOR);
    gg.fill(x + boxW - 1, y, x + boxW, y + boxH, BORDER_COLOR);
    int ty = y + PADDING;
    for (FormattedCharSequence line : lines) {
      gg.drawString(font, line, x + PADDING, ty, TEXT_COLOR, false);
      ty += font.lineHeight;
    }
  }
}
