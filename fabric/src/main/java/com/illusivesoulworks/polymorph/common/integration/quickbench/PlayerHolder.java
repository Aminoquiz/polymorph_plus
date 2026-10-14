package com.illusivesoulworks.polymorph.common.integration.quickbench;

import net.minecraft.world.entity.player.Player;

public class PlayerHolder {

  private static Player player = null;

  public static void setPlayer(Player player) {
    PlayerHolder.player = player;
  }

  public static Player getPlayer() {
    return player;
  }
}
