package com.illusivesoulworks.polymorph.api.inworld;

import com.illusivesoulworks.polymorph.common.inworld.InWorldSavedData;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public interface IChoiceStore {

  /**
   * Picks saved per dimension, for blocks with no store of their own. Server side only:
   * on the client, read picks through {@link PolymorphInWorld#chosen}.
   */
  IChoiceStore LEVEL = InWorldSavedData.STORE;

  @Nullable
  ResourceLocation get(Level level, BlockPos pos, ResourceLocation key);

  void set(Level level, BlockPos pos, ResourceLocation key, ResourceLocation id);
}
