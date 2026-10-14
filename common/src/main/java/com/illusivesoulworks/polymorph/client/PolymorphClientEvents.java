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

package com.illusivesoulworks.polymorph.client;

import com.illusivesoulworks.polymorph.api.client.base.ITickingRecipesWidget;
import com.illusivesoulworks.polymorph.client.recipe.RecipesWidget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.common.priority.RecipePriority;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class PolymorphClientEvents {

  /** Uploads the player's preference lists on join; skipped when both are empty. */
  public static void syncPriority() {
    List<String> namespaces = PolymorphClientConfig.getPriorityNamespaces();
    List<String> recipes = PolymorphClientConfig.getPriorityRecipes();

    if (!namespaces.isEmpty() || !recipes.isEmpty()) {
      PolymorphApi.common().getPacketDistributor().sendPlayerPriorityC2S(namespaces, recipes);
    }
  }

  // A server without Polymorph+ never sends a pack list, so the last world's list must go.
  public static void clearPackPriority() {
    RecipePriority.setClientPackNamespaces(List.of());
  }

  public static void tick() {
    Minecraft mc = Minecraft.getInstance();
    RecipesWidget.get().ifPresent(widget -> {
      if (mc.player == null || mc.screen == null) {
        RecipesWidget.clear();
      } else if (widget instanceof ITickingRecipesWidget) {
        ((ITickingRecipesWidget) widget).tick();
      }
    });
  }

  public static void initScreen(Screen screen) {

    if (screen instanceof AbstractContainerScreen) {
      RecipesWidget.create((AbstractContainerScreen<?>) screen);
    }
  }

  private static boolean drawnThisFrame;

  public static void render(Screen screen, PoseStack poseStack, int mouseX, int mouseY,
                            float partialTicks) {

    if (screen instanceof AbstractContainerScreen) {
      drawnThisFrame = true;
      RecipesWidget.revalidate((AbstractContainerScreen<?>) screen);
      RecipesWidget.get().ifPresent(
          recipeController -> recipeController.render(poseStack, mouseX, mouseY, partialTicks));
    }
  }

  public static void renderIfMissed(Screen screen, PoseStack poseStack, int mouseX,
                                    int mouseY, float partialTicks) {

    if (!drawnThisFrame) {
      render(screen, poseStack, mouseX, mouseY, partialTicks);
    }
    drawnThisFrame = false;
    TutorialOverlay.flush(poseStack);
  }

  public static boolean mouseClick(Screen screen, double mouseX, double mouseY, int button) {

    if (screen instanceof AbstractContainerScreen) {
      return RecipesWidget.get()
          .map(recipeController -> recipeController.mouseClicked(mouseX, mouseY, button))
          .orElse(false);
    }
    return false;
  }

  public static boolean mouseScroll(Screen screen, double mouseX, double mouseY, double scrollY) {

    if (screen instanceof AbstractContainerScreen) {
      return RecipesWidget.get()
          .map(recipeController -> recipeController.mouseScrolled(mouseX, mouseY, scrollY))
          .orElse(false);
    }
    return false;
  }
}
