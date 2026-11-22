package com.illusivesoulworks.polymorph.client.look;

import dev.dryaides.minuit.Draw;
import dev.dryaides.minuit.FrameStyle;
import dev.dryaides.minuit.Palette;
import dev.dryaides.minuit.Shapes;
import dev.dryaides.minuit.Theme;
import dev.dryaides.minuit.Themes;
import dev.dryaides.minuit.client.GuiDraw;
import dev.dryaides.minuit.client.ThemeScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/** Loaded only after PolymorphLook has checked that minuit is installed. */
final class MinuitLook {

  private static final String ID = "polymorph_plus";
  private static final int OPAQUE = 0xFF000000;

  private static int generation = Integer.MIN_VALUE;
  private static Theme theme;
  private static Palette tip;

  private MinuitLook() {
  }

  static void register() {
    Themes.register(Themes.defaultTheme().as(ID), "Polymorph+");
  }

  static Screen configScreen(Screen parent) {
    return new ThemeScreen(parent, ID);
  }

  /** The player's theme for Polymorph+, or null while no choice reaches it (the sprites stay). */
  private static Theme theme() {
    int now = Themes.generation();

    if (now != generation) {
      theme = picked() ? Themes.byId(ID) : null;
      tip = theme == null ? null : Themes.tipPalette(theme);
      generation = now;
    }
    return theme;
  }

  // minuit 2.2.x has no Themes.picked: fall back to comparing with the registered default.
  private static boolean picked() {
    try {
      return Themes.picked(ID).isPresent();
    } catch (NoSuchMethodError e) {
      return Themes.byId(ID) != Themes.registered(ID);
    }
  }

  static int ink(PolymorphLook.Ink ink, int vanilla) {
    Theme t = theme();

    if (t == null) {
      return vanilla;
    }
    Palette p = t.palette();
    return switch (ink) {
      case STAR -> p.warn() | OPAQUE;
      case STAR_SHADOW -> p.shadow();
      case PIN_FACE -> p.accent() | OPAQUE;
      case PIN_EDGE -> p.accentDeep() | OPAQUE;
      case BUBBLE_FACE -> tip.surface() | OPAQUE;
      case BUBBLE_EDGE -> tip.accent() | OPAQUE;
      case BUBBLE_TEXT -> tip.text() | OPAQUE;
      case ARROW_FACE -> p.raised() | OPAQUE;
      case ARROW_GLYPH -> p.raisedText() | OPAQUE;
      case ARROW_GLYPH_OFF -> p.disabledText() | OPAQUE;
      case WORLD_FRAME -> p.accent() | OPAQUE;
    };
  }

  static boolean outputButton(GuiGraphics g, int x, int y, int w, int h,
                              boolean current, boolean hovered) {
    Theme t = theme();

    if (t == null) {
      return false;
    }
    Palette p = t.palette();
    FrameStyle f = t.frame();
    Draw d = new GuiDraw(g);
    int face = current ? (hovered ? p.primaryHover() : p.primary())
        : (hovered ? p.raisedHover() : p.raised());
    // Inset by half of FrameStyle.BLEED_AXIS so neighbouring tiles leave the gap a family may bleed into.
    int inset = FrameStyle.BLEED_AXIS / 2;
    f.button(d, p, x + inset, y + inset, w - 2 * inset, h - 2 * inset, null, face,
        Shapes.Relief.RAISED);
    // The item sits at +4, one pixel inside an 18 px slot.
    f.slot(d, p, x + 3, y + 3, hovered, current);
    return true;
  }

  static boolean selectorButton(GuiGraphics g, int x, int y, int w, int h,
                                boolean hovered, int[] glyph, int glyphSize) {
    Theme t = theme();

    if (t == null) {
      return false;
    }
    Palette p = t.palette();
    Draw d = new GuiDraw(g);
    t.frame().button(d, p, x, y, w, h, null, hovered ? p.primaryHover() : p.primary(),
        Shapes.Relief.RAISED);
    int px = Math.max(1, Math.min(w, h) / 16);
    int gx = x + (w - glyphSize * px) / 2;
    int gy = y + (h - glyph.length * px) / 2;
    int ink = p.primaryText() | OPAQUE;

    for (int row = 0; row < glyph.length; row++) {

      for (int col = 0; col < glyphSize; col++) {

        if ((glyph[row] & (1 << (glyphSize - 1 - col))) != 0) {
          d.fill(gx + col * px, gy + row * px, gx + (col + 1) * px, gy + (row + 1) * px, ink);
        }
      }
    }
    return true;
  }
}
