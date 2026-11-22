package com.illusivesoulworks.polymorph.client.look;

import com.illusivesoulworks.polymorph.PolymorphConstants;
import com.illusivesoulworks.polymorph.platform.Services;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

/**
 * The look of Polymorph+'s widgets: the sprites and colours it ships, or the player's minuit theme
 * when minuit is installed and the player picked a theme that reaches Polymorph+. Callers pass
 * the vanilla value; it comes back unchanged whenever minuit is not in play.
 */
public final class PolymorphLook {

  public enum Ink {
    STAR, STAR_SHADOW, PIN_FACE, PIN_EDGE, BUBBLE_FACE, BUBBLE_EDGE, BUBBLE_TEXT, ARROW_FACE,
    ARROW_GLYPH, ARROW_GLYPH_OFF, WORLD_FRAME
  }

  private static boolean minuit;

  private PolymorphLook() {
  }

  public static void init() {

    if (Services.PLATFORM.isModLoaded("minuit")) {
      minuit = true;

      try {
        MinuitLook.register();
      } catch (LinkageError | RuntimeException e) {
        off(e);
      }
    }
  }

  public static boolean hasConfigScreen() {
    return minuit;
  }

  /** The minuit theme page for Polymorph+, or null when minuit is not in play. */
  public static Screen configScreen(Screen parent) {

    if (!minuit) {
      return null;
    }

    try {
      return MinuitLook.configScreen(parent);
    } catch (LinkageError | RuntimeException e) {
      off(e);
      return null;
    }
  }

  public static int ink(Ink ink, int vanilla) {

    if (!minuit) {
      return vanilla;
    }

    try {
      return MinuitLook.ink(ink, vanilla);
    } catch (LinkageError | RuntimeException e) {
      off(e);
      return vanilla;
    }
  }

  /** True when it drew the candidate button; false leaves the sprite to the caller. */
  public static boolean outputButton(GuiGraphicsExtractor g, int x, int y, int w, int h,
                                     boolean current, boolean hovered) {

    if (!minuit) {
      return false;
    }

    try {
      return MinuitLook.outputButton(g, x, y, w, h, current, hovered);
    } catch (LinkageError | RuntimeException e) {
      off(e);
      return false;
    }
  }

  /**
   * True when it drew the open-selector button. {@code glyph} holds one int per row,
   * {@code glyphSize} bits wide, the leftmost pixel in the highest bit.
   */
  public static boolean selectorButton(GuiGraphicsExtractor g, int x, int y, int w, int h,
                                       boolean hovered, int[] glyph, int glyphSize) {

    if (!minuit) {
      return false;
    }

    try {
      return MinuitLook.selectorButton(g, x, y, w, h, hovered, glyph, glyphSize);
    } catch (LinkageError | RuntimeException e) {
      off(e);
      return false;
    }
  }

  private static void off(Throwable e) {
    minuit = false;
    PolymorphConstants.LOG.warn("minuit integration turned off: {}", e.toString());
  }
}
