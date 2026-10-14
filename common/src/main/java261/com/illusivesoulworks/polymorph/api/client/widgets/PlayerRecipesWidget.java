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
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

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
      // Stored by id: the client has no recipe manager to turn this into a holder, and
      // resolution only ever needs to compare it against the candidates it already holds.
      api.getPlayerRecipeData(player).chooseRecipe(resourceLocation);
    }
    api.getNetwork().sendPlayerRecipeSelectionC2S(resourceLocation);
  }

  @Override
  public Slot getOutputSlot() {
    return this.outputSlot;
  }
}
