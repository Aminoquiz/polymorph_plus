package com.illusivesoulworks.polymorph.common.inworld;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.inworld.Choices;
import com.illusivesoulworks.polymorph.api.inworld.IChoiceStore;
import com.illusivesoulworks.polymorph.api.inworld.IInWorldProvider;
import com.illusivesoulworks.polymorph.api.inworld.IndicatorSpot;
import com.illusivesoulworks.polymorph.common.capability.CampfireRecipeData;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * One choice per campfire slot, each shown on the side under that slot's item.
 */
public class CampfireInWorldProvider implements IInWorldProvider {

  private static final int SLOTS = 4;
  private static final ResourceLocation[] KEYS = new ResourceLocation[SLOTS];

  // On the log, right of the flames, which would hide a centred icon. Vanilla lays item i in
  // the corner on the right of face (i + facing + 2) seen from outside, hence the turns.
  private static final IndicatorSpot SPOT = IndicatorSpot.onFrontFace(0.825f, 0.125f, 0.125f);

  static {
    for (int slot = 0; slot < SLOTS; slot++) {
      KEYS[slot] = ResourceLocation.fromNamespaceAndPath(PolymorphApi.MOD_ID,
          "campfire_slot_" + slot);
    }
  }

  private final IChoiceStore store = new IChoiceStore() {

    @Nullable
    @Override
    public ResourceLocation get(Level level, BlockPos pos, ResourceLocation key) {
      CampfireRecipeData data = recipeData(level, pos);
      int slot = slotOf(key);
      return data != null && slot >= 0 ? data.getSlotPick(slot) : null;
    }

    @Override
    public void set(Level level, BlockPos pos, ResourceLocation key, ResourceLocation id) {
      CampfireRecipeData data = recipeData(level, pos);
      int slot = slotOf(key);

      if (data != null && slot >= 0) {
        data.setSlotPick(slot, id);
        data.getOwner().setChanged();
      }
    }
  };

  @Override
  public int count(BlockState state) {
    return SLOTS;
  }

  @Nullable
  @Override
  public Choices choices(ServerLevel level, BlockPos pos, BlockState state) {
    return choices(level, pos, state, 0);
  }

  @Nullable
  @Override
  public Choices choices(ServerLevel level, BlockPos pos, BlockState state, int index) {
    CampfireRecipeData data = recipeData(level, pos);
    return data != null ? data.slotChoices(level, index, KEYS[index]) : null;
  }

  @Override
  public IndicatorSpot spot(BlockState state) {
    return spot(state, 0);
  }

  @Override
  public IndicatorSpot spot(BlockState state, int index) {
    return SPOT.turned(index + 2);
  }

  @Override
  public IChoiceStore store() {
    return this.store;
  }

  private static int slotOf(ResourceLocation key) {

    for (int slot = 0; slot < SLOTS; slot++) {

      if (KEYS[slot].equals(key)) {
        return slot;
      }
    }
    return -1;
  }

  @Nullable
  private static CampfireRecipeData recipeData(Level level, BlockPos pos) {
    BlockEntity blockEntity = level.getBlockEntity(pos);
    return blockEntity != null && PolymorphApi.getInstance()
        .getBlockEntityRecipeData(blockEntity) instanceof CampfireRecipeData data ? data : null;
  }
}
