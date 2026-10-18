/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Lightweight client-only settings persisted to <gameDir>/config/polymorph_plus_client.json.
 * No loader-specific config API used so the same source compiles on every port.
 */
package com.illusivesoulworks.polymorph.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.illusivesoulworks.polymorph.common.priority.RecipePriority;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;

public final class PolymorphClientConfig {

  private static final String FILE_NAME = "polymorph_plus_client.json";
  private static final String KEY_PIN = "pinSelector";
  private static final String KEY_TUTORIAL_STEP = "tutorialStep";
  private static final String KEY_SCROLL_CYCLE = "scrollCycleOnOutput";
  private static final String KEY_BUTTON_X = "buttonOffsetX";
  private static final String KEY_BUTTON_Y = "buttonOffsetY";
  private static final String KEY_BUTTON_SCALE = "buttonScale";
  private static final String KEY_PRIORITY = "priorityNamespaces";
  private static final String KEY_PRIORITY_RECIPES = "priorityRecipes";
  private static final int MAX_PRIORITY_ENTRIES = 64;
  private static final int MAX_PRIORITY_RECIPES = 256;
  public static final int TUTORIAL_DONE = 6;


  private static Boolean pinSelector;
  private static Integer tutorialStep;
  private static Boolean scrollCycle;
  private static Integer buttonOffsetX;
  private static Integer buttonOffsetY;
  private static Float buttonScale;
  private static List<String> priorityNamespaces;
  private static List<String> priorityRecipes;

  private PolymorphClientConfig() {
  }

  public static boolean isPinSelector() {
    if (pinSelector == null) {
      load();
    }
    return Boolean.TRUE.equals(pinSelector);
  }

  public static void setPinSelector(boolean value) {
    pinSelector = value;
    save();
  }

  /**
   * Shift+scroll over the output slot cycles the conflicting recipes without opening the
   * selector. Opt-out rather than opt-in: it only fires with shift held AND the pointer on
   * the result slot, so it cannot shadow vanilla or inventory-mod scroll handling.
   */
  public static boolean isScrollCycle() {
    if (scrollCycle == null) {
      load();
    }
    return Boolean.TRUE.equals(scrollCycle);
  }

  public static void setScrollCycle(boolean value) {
    scrollCycle = value;
    save();
  }

  /**
   * Per-player source preference, in order. Written by shift-clicking an output in the
   * selector ("always prefer this mod"), and plain enough to hand-edit for anyone who
   * would rather type it. Consulted only where no explicit per-conflict choice exists,
   * and it outranks the pack-shipped list.
   */
  public static List<String> getPriorityNamespaces() {
    if (priorityNamespaces == null) {
      load();
    }
    return List.copyOf(priorityNamespaces);
  }

  public static void setPriorityNamespaces(List<String> namespaces) {
    priorityNamespaces = trim(namespaces, MAX_PRIORITY_ENTRIES);
    save();
  }

  private static List<String> trim(List<String> values, int max) {
    List<String> trimmed = new ArrayList<>();

    for (String value : values) {

      if (value != null && !value.isBlank() && !trimmed.contains(value)
          && trimmed.size() < max) {
        trimmed.add(value);
      }
    }
    return trimmed;
  }

  /**
   * Moves a namespace to the front of the preference list. Returns false when it was
   * already the top preference, so the caller can skip a redundant sync to the server.
   */
  public static boolean promoteNamespace(String namespace) {
    List<String> current = new ArrayList<>(getPriorityNamespaces());

    if (!current.isEmpty() && current.get(0).equals(namespace)) {
      return false;
    }
    current.remove(namespace);
    current.add(0, namespace);
    setPriorityNamespaces(current);
    return true;
  }

  /**
   * Recipes the player has explicitly asked to win, most recent first. A craft produces one
   * outcome, so a per-mod preference cannot settle a conflict where several candidates come
   * from the same mod: this list is what shift-clicking writes, and it outranks
   * {@link #getPriorityNamespaces()}, which stays hand-editable for blanket "this mod over
   * that one" rules.
   */
  public static List<String> getPriorityRecipes() {
    if (priorityRecipes == null) {
      load();
    }
    return List.copyOf(priorityRecipes);
  }

  public static void setPriorityRecipes(List<String> recipes) {
    priorityRecipes = trim(recipes, MAX_PRIORITY_RECIPES);
    save();
  }

  /**
   * Moves a recipe to the front of the list. Returns false when it was already first, so the
   * caller can skip a redundant upload.
   */
  public static boolean promoteRecipe(String recipe) {
    List<String> current = new ArrayList<>(getPriorityRecipes());

    if (!current.isEmpty() && current.get(0).equals(recipe)) {
      return false;
    }
    current.remove(recipe);
    current.add(0, recipe);
    setPriorityRecipes(current);
    return true;
  }

  /** Position of a recipe id in the list, or {@link Integer#MAX_VALUE} when absent. */
  public static int recipeRankOf(String recipe) {
    if (priorityRecipes == null) {
      load();
    }
    int index = priorityRecipes.indexOf(recipe);
    return index < 0 ? Integer.MAX_VALUE : index;
  }


  /**
   * Position of a namespace in the preference list, or {@link Integer#MAX_VALUE} when it is
   * absent. Lower wins. The selector uses this to mark which candidate the player has taught
   * Polymorph to prefer, so the choice is visible in the GUI instead of only in a message
   * that the open inventory covers up.
   */
  public static int rankOf(String namespace) {
    if (priorityNamespaces == null) {
      load();
    }
    int index = priorityNamespaces.indexOf(namespace);
    return index < 0 ? Integer.MAX_VALUE : index;
  }

  /**
   * Hand-edited shift of the selector button (and the list it opens) from where each screen
   * places it, for inventory mods that draw over that spot (Inventorio).
   */
  public static int getButtonOffsetX() {
    if (buttonOffsetX == null) {
      load();
    }
    return buttonOffsetX;
  }

  public static int getButtonOffsetY() {
    if (buttonOffsetY == null) {
      load();
    }
    return buttonOffsetY;
  }

  /** Size factor of the 16x16 selector button, clamped to 0.5..4. */
  public static float getButtonScale() {
    if (buttonScale == null) {
      load();
    }
    return buttonScale;
  }

  public static int getTutorialStep() {
    if (tutorialStep == null) {
      load();
    }
    return tutorialStep;
  }

  public static void setTutorialStep(int value) {
    tutorialStep = value;
    save();
  }

  private static Path path() {
    return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(FILE_NAME);
  }

  private static void load() {
    pinSelector = Boolean.FALSE;
    tutorialStep = 0;
    scrollCycle = Boolean.TRUE;
    buttonOffsetX = 0;
    buttonOffsetY = 0;
    buttonScale = 1.0F;
    priorityNamespaces = new ArrayList<>();
    priorityRecipes = new ArrayList<>();
    Path p = path();
    if (!Files.exists(p)) {
      return;
    }
    try {
      JsonObject obj = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
      if (obj.has(KEY_PIN)) {
        pinSelector = obj.get(KEY_PIN).getAsBoolean();
      }
      if (obj.has(KEY_TUTORIAL_STEP)) {
        tutorialStep = obj.get(KEY_TUTORIAL_STEP).getAsInt();
      }
      if (obj.has(KEY_SCROLL_CYCLE)) {
        scrollCycle = obj.get(KEY_SCROLL_CYCLE).getAsBoolean();
      }
      if (obj.has(KEY_BUTTON_X)) {
        buttonOffsetX = obj.get(KEY_BUTTON_X).getAsInt();
      }
      if (obj.has(KEY_BUTTON_Y)) {
        buttonOffsetY = obj.get(KEY_BUTTON_Y).getAsInt();
      }
      if (obj.has(KEY_BUTTON_SCALE)) {
        float scale = obj.get(KEY_BUTTON_SCALE).getAsFloat();
        buttonScale = Float.isNaN(scale) ? 1.0F : Math.max(0.5F, Math.min(4.0F, scale));
      }
      if (obj.has(KEY_PRIORITY)) {
        priorityNamespaces =
            new ArrayList<>(RecipePriority.parse(obj.get(KEY_PRIORITY).toString()));
      }
      if (obj.has(KEY_PRIORITY_RECIPES)) {
        priorityRecipes =
            new ArrayList<>(RecipePriority.parse(obj.get(KEY_PRIORITY_RECIPES).toString()));
      }
    } catch (Exception ignored) {
    }
  }

  private static void save() {
    Path p = path();
    JsonObject obj = new JsonObject();
    obj.addProperty(KEY_PIN, Boolean.TRUE.equals(pinSelector));
    obj.addProperty(KEY_TUTORIAL_STEP, tutorialStep == null ? 0 : tutorialStep);
    obj.addProperty(KEY_SCROLL_CYCLE, scrollCycle == null || Boolean.TRUE.equals(scrollCycle));
    obj.addProperty(KEY_BUTTON_X, buttonOffsetX == null ? 0 : buttonOffsetX);
    obj.addProperty(KEY_BUTTON_Y, buttonOffsetY == null ? 0 : buttonOffsetY);
    obj.addProperty(KEY_BUTTON_SCALE, buttonScale == null ? 1.0F : buttonScale);
    JsonArray priority = new JsonArray();

    if (priorityNamespaces != null) {
      priorityNamespaces.forEach(priority::add);
    }
    obj.add(KEY_PRIORITY, priority);
    JsonArray recipes = new JsonArray();

    if (priorityRecipes != null) {
      priorityRecipes.forEach(recipes::add);
    }
    obj.add(KEY_PRIORITY_RECIPES, recipes);
    try {
      Files.createDirectories(p.getParent());
      Files.writeString(p, obj.toString());
    } catch (IOException ignored) {
    }
  }
}
