package com.shiver.polymorphlegacy.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shiver.polymorphlegacy.priority.PlayerPreferences;
import com.shiver.polymorphlegacy.priority.RecipePriority;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;

/** Client settings in {@code config/polymorph_plus_client.json}, same keys as the other ports. */
public final class PolymorphClientConfig {
    public static final int TUTORIAL_DONE = 6;
    private static final String FILE_NAME = "polymorph_plus_client.json";

    private static boolean loaded;
    private static boolean pinSelector;
    private static int tutorialStep;
    private static boolean scrollCycle = true;
    private static int buttonOffsetX;
    private static int buttonOffsetY;
    private static float buttonScale = 1.0F;
    private static List<String> priorityNamespaces = new ArrayList<>();
    private static List<String> priorityRecipes = new ArrayList<>();

    private PolymorphClientConfig() {
    }

    /**
     * Hand-edited shift of the selector button (and the row it opens) from its spot above the
     * output slot, for inventory mods that draw over that spot.
     */
    public static int getButtonOffsetX() {
        load();
        return buttonOffsetX;
    }

    public static int getButtonOffsetY() {
        load();
        return buttonOffsetY;
    }

    /** Size factor of the 16x16 selector button, clamped to 0.5..4. */
    public static float getButtonScale() {
        load();
        return buttonScale;
    }

    public static boolean isPinSelector() {
        load();
        return pinSelector;
    }

    public static void setPinSelector(boolean value) {
        load();
        pinSelector = value;
        save();
    }

    public static boolean isScrollCycle() {
        load();
        return scrollCycle;
    }

    public static int getTutorialStep() {
        load();
        return tutorialStep;
    }

    public static void setTutorialStep(int value) {
        load();
        tutorialStep = value;
        save();
    }

    public static List<String> getPriorityNamespaces() {
        load();
        return Collections.unmodifiableList(new ArrayList<>(priorityNamespaces));
    }

    public static List<String> getPriorityRecipes() {
        load();
        return Collections.unmodifiableList(new ArrayList<>(priorityRecipes));
    }

    /** Moves a recipe to the front of the favourites; false when it already led the list. */
    public static boolean promoteRecipe(String recipe) {
        load();
        if (!priorityRecipes.isEmpty() && priorityRecipes.get(0).equals(recipe)) {
            return false;
        }
        priorityRecipes.remove(recipe);
        priorityRecipes.add(0, recipe);
        priorityRecipes = trim(priorityRecipes, PlayerPreferences.MAX_RECIPES);
        save();
        return true;
    }

    /** Rank among the favourites, {@link Integer#MAX_VALUE} when absent. */
    public static int recipeRankOf(String recipe) {
        load();
        int index = priorityRecipes.indexOf(recipe);
        return index < 0 ? Integer.MAX_VALUE : index;
    }

    private static List<String> trim(List<String> values, int max) {
        List<String> trimmed = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.trim().isEmpty() && !trimmed.contains(value) && trimmed.size() < max) {
                trimmed.add(value);
            }
        }
        return trimmed;
    }

    private static File file() {
        return new File(new File(Minecraft.getMinecraft().gameDir, "config"), FILE_NAME);
    }

    private static void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        File file = file();
        if (!file.isFile()) {
            return;
        }
        try {
            JsonObject obj = new JsonParser().parse(new String(Files.readAllBytes(file.toPath()),
                    StandardCharsets.UTF_8)).getAsJsonObject();
            if (obj.has("pinSelector")) {
                pinSelector = obj.get("pinSelector").getAsBoolean();
            }
            if (obj.has("tutorialStep")) {
                tutorialStep = obj.get("tutorialStep").getAsInt();
            }
            if (obj.has("scrollCycleOnOutput")) {
                scrollCycle = obj.get("scrollCycleOnOutput").getAsBoolean();
            }
            if (obj.has("buttonOffsetX")) {
                buttonOffsetX = obj.get("buttonOffsetX").getAsInt();
            }
            if (obj.has("buttonOffsetY")) {
                buttonOffsetY = obj.get("buttonOffsetY").getAsInt();
            }
            if (obj.has("buttonScale")) {
                float scale = obj.get("buttonScale").getAsFloat();
                buttonScale = Float.isNaN(scale) ? 1.0F : Math.max(0.5F, Math.min(4.0F, scale));
            }
            if (obj.has("priorityNamespaces")) {
                priorityNamespaces = trim(RecipePriority.parse(obj.get("priorityNamespaces").toString()),
                        PlayerPreferences.MAX_NAMESPACES);
            }
            if (obj.has("priorityRecipes")) {
                priorityRecipes = trim(RecipePriority.parse(obj.get("priorityRecipes").toString()),
                        PlayerPreferences.MAX_RECIPES);
            }
        } catch (Exception ignored) {
            // A hand-edited file that no longer parses falls back to the defaults.
        }
    }

    private static void save() {
        JsonObject obj = new JsonObject();
        obj.addProperty("pinSelector", pinSelector);
        obj.addProperty("tutorialStep", tutorialStep);
        obj.addProperty("scrollCycleOnOutput", scrollCycle);
        obj.addProperty("buttonOffsetX", buttonOffsetX);
        obj.addProperty("buttonOffsetY", buttonOffsetY);
        obj.addProperty("buttonScale", buttonScale);
        JsonArray namespaces = new JsonArray();
        priorityNamespaces.forEach(namespaces::add);
        obj.add("priorityNamespaces", namespaces);
        JsonArray recipes = new JsonArray();
        priorityRecipes.forEach(recipes::add);
        obj.add("priorityRecipes", recipes);
        try {
            File file = file();
            file.getParentFile().mkdirs();
            Files.write(file.toPath(), obj.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {
            // Losing a UI preference is not worth a crash.
        }
    }
}
