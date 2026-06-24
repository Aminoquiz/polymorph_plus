/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * MC 26.1 fork. Level.getRecipeManager() gone — for SP we can still reach the integrated
 * server's RecipeManager via player.level().getServer(). On a remote MP client there is
 * no full RecipeManager, so the eager local apply is skipped and we rely on the C2S round
 * trip + server-driven sync.
 * Recipe lookup also went through ResourceKey<Recipe<?>>, not Identifier directly.
 */
package com.illusivesoulworks.polymorph.api.client.widgets;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.client.base.AbstractRecipesWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

public class PlayerRecipesWidget extends AbstractRecipesWidget {

  final Slot outputSlot;

  public PlayerRecipesWidget(AbstractContainerScreen<?> containerScreen, Slot outputSlot) {
    super(containerScreen);
    this.outputSlot = outputSlot;
  }

  @Override
  public void selectRecipe(Identifier resourceLocation) {
    PolymorphApi api = PolymorphApi.getInstance();
    Player player = Minecraft.getInstance().player;

    if (player != null) {
      MinecraftServer server = player.level().getServer();

      if (server != null) {
        RecipeManager rm = server.getRecipeManager();
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, resourceLocation);
        rm.byKey(key).ifPresent(recipe -> api.getPlayerRecipeData(player).selectRecipe(recipe));
      }
    }
    api.getNetwork().sendPlayerRecipeSelectionC2S(resourceLocation);
  }

  @Override
  public Slot getOutputSlot() {
    return this.outputSlot;
  }
}
