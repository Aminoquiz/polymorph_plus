package com.illusivesoulworks.polymorph.api.inworld;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IInWorldProvider {

  /**
   * Returns null, or fewer than 2 options, when there is nothing to pick.
   */
  @Nullable
  Choices choices(ServerLevel level, BlockPos pos, BlockState state);

  IndicatorSpot spot(BlockState state);

  /**
   * How many separate choices the block holds, each with its own indicator, such as one per
   * campfire slot. Each index must use its own {@link Choices#key()}.
   */
  default int count(BlockState state) {
    return 1;
  }

  @Nullable
  default Choices choices(ServerLevel level, BlockPos pos, BlockState state, int index) {
    return choices(level, pos, state);
  }

  default IndicatorSpot spot(BlockState state, int index) {
    return spot(state);
  }

  default IChoiceStore store() {
    return IChoiceStore.LEVEL;
  }

  /**
   * Called on the server after a player picks, and on the client each time a new state for
   * the block arrives.
   */
  default void onSelected(Level level, BlockPos pos, ResourceLocation key, ResourceLocation id) {
  }
}
