/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Lightweight client-only settings persisted to <gameDir>/config/polymorph_plus_client.json.
 * No loader-specific config API used so the same source compiles on every port.
 */
package com.illusivesoulworks.polymorph.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;

public final class PolymorphClientConfig {

  private static final String FILE_NAME = "polymorph_plus_client.json";
  private static final String KEY_PIN = "pinSelector";
  private static final String KEY_TUTORIAL_STEP = "tutorialStep";
  public static final int TUTORIAL_DONE = 4;

  private static Boolean pinSelector;
  private static Integer tutorialStep;

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
    } catch (Exception ignored) {
    }
  }

  private static void save() {
    Path p = path();
    JsonObject obj = new JsonObject();
    obj.addProperty(KEY_PIN, Boolean.TRUE.equals(pinSelector));
    obj.addProperty(KEY_TUTORIAL_STEP, tutorialStep == null ? 0 : tutorialStep);
    try {
      Files.createDirectories(p.getParent());
      Files.writeString(p, obj.toString());
    } catch (IOException ignored) {
    }
  }
}
