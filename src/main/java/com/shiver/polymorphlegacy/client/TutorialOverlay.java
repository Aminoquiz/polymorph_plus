package com.shiver.polymorphlegacy.client;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

/**
 * Six one-time hints, one per step: open the selector, pick, pin, scroll, favourite, cycle.
 * Each advances when the player does the thing, or after eight seconds on screen.
 */
public final class TutorialOverlay {
    private static final int MAX_WIDTH = 150;
    private static final int PADDING = 4;
    private static final long AUTO_DISMISS_MS = 8000L;
    private static int currentStep = -1;
    private static long shownAt;
    private static String pendingText;
    private static int pendingX;
    private static int pendingY;

    private TutorialOverlay() {
    }

    public static void renderForButton(int buttonX, int buttonY) {
        int step = PolymorphClientConfig.getTutorialStep();
        if (step == 0) {
            show(0, "polymorph_plus.tutorial.conflict", buttonX + 20, buttonY - 2);
        } else if (step == 2) {
            show(2, "polymorph_plus.tutorial.pin", buttonX + 20, buttonY - 2);
        } else if (step == 5) {
            show(5, "polymorph_plus.tutorial.cycle", buttonX + 20, buttonY - 2);
        }
    }

    public static void renderForSelector(int x, int y, boolean canScroll) {
        int step = PolymorphClientConfig.getTutorialStep();
        if (step == 1) {
            show(1, "polymorph_plus.tutorial.select", x, y);
        } else if (step == 3 && canScroll) {
            show(3, "polymorph_plus.tutorial.scroll", x, y);
        } else if (step == 4) {
            show(4, "polymorph_plus.tutorial.prefer", x, y);
        }
    }

    public static void onOpened() {
        advance(0);
    }

    public static void onPicked() {
        advance(1);
    }

    public static void onPinToggled() {
        advance(2);
    }

    public static void onScrolled() {
        advance(3);
    }

    public static void onFavourite() {
        advance(4);
    }

    public static void onCycled() {
        advance(5);
    }

    private static void advance(int from) {
        if (PolymorphClientConfig.getTutorialStep() == from) {
            PolymorphClientConfig.setTutorialStep(from + 1);
            currentStep = -1;
        }
    }

    private static void show(int step, String key, int x, int y) {
        if (currentStep != step) {
            currentStep = step;
            shownAt = System.currentTimeMillis();
        }
        drawBubble(I18n.format(key), x, y);
        if (System.currentTimeMillis() - shownAt > AUTO_DISMISS_MS) {
            advance(step);
        }
    }

    private static void drawBubble(String text, int x, int y) {
        pendingText = text;
        pendingX = x;
        pendingY = y;
    }

    /** Draws the recorded bubble last, from DrawScreenEvent.Post at LOWEST, so it sits above JEI. */
    public static void flush() {
        if (pendingText == null) {
            return;
        }
        String text = pendingText;
        int x = pendingX;
        int y = pendingY;
        pendingText = null;
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 500);
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer font = mc.fontRenderer;
        List<String> lines = font.listFormattedStringToWidth(text, MAX_WIDTH);
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, font.getStringWidth(line));
        }
        int boxW = width + PADDING * 2;
        int boxH = lines.size() * font.FONT_HEIGHT + PADDING * 2;
        // A tall GUI pushes the bubble past the window edge.
        ScaledResolution res = new ScaledResolution(mc);
        x = Math.max(0, Math.min(x, res.getScaledWidth() - boxW));
        y = Math.max(0, Math.min(y, res.getScaledHeight() - boxH));
        Gui.drawRect(x, y, x + boxW, y + boxH, 0xEE101010);
        Gui.drawRect(x, y, x + boxW, y + 1, 0xFFFFD000);
        Gui.drawRect(x, y + boxH - 1, x + boxW, y + boxH, 0xFFFFD000);
        Gui.drawRect(x, y, x + 1, y + boxH, 0xFFFFD000);
        Gui.drawRect(x + boxW - 1, y, x + boxW, y + boxH, 0xFFFFD000);
        int ty = y + PADDING;
        for (String line : lines) {
            font.drawStringWithShadow(line, x + PADDING, ty, 0xFFFFFF);
            ty += font.FONT_HEIGHT;
        }
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1, 1);
    }
}
