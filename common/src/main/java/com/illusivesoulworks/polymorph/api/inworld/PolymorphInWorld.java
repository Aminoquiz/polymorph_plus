package com.illusivesoulworks.polymorph.api.inworld;

import com.illusivesoulworks.polymorph.client.inworld.ClientInWorldStates;
import com.illusivesoulworks.polymorph.common.inworld.InWorldServer;
import com.illusivesoulworks.polymorph.common.inworld.RecipeDataInWorldProvider;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class PolymorphInWorld {

  private PolymorphInWorld() {
  }

  /**
   * Call during common init, on both sides.
   */
  public static void register(Block block, IInWorldProvider provider) {
    InWorldServer.register(block, provider);
  }

  /**
   * A provider for blocks whose block entity has Polymorph recipe data: the options are its
   * recipe candidates and the pick is the same one the menu widget makes.
   */
  public static IInWorldProvider recipeData(IndicatorSpot spot) {
    return new RecipeDataInWorldProvider(spot);
  }

  /**
   * Tells Polymorph the options at this position may have changed. Sends the new state to the
   * players tracking the chunk when it differs from the last one sent. Does nothing on the
   * client.
   */
  public static void refresh(Level level, BlockPos pos) {

    if (level instanceof ServerLevel serverLevel) {
      InWorldServer.refresh(serverLevel, pos);
    }
  }

  /**
   * The option picked at this position for this key, or null if none. On the client this is
   * the option the server last sent for the block.
   */
  @Nullable
  public static ResourceLocation chosen(Level level, BlockPos pos, ResourceLocation key) {

    if (level.isClientSide()) {
      return ClientInWorldStates.chosen(pos, key);
    }
    return InWorldServer.chosen(level, pos, key);
  }
}
