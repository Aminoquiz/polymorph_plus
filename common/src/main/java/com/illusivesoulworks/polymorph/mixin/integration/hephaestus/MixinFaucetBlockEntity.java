package com.illusivesoulworks.polymorph.mixin.integration.hephaestus;

import com.illusivesoulworks.polymorph.common.integration.hephaestus.HephaestusModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

// SearedFaucetBlock overrides neither onPlace nor onRemove, so the faucet's block entity, which
// joins and leaves the level with it, adds the overrides here.
@SuppressWarnings("unused")
@Pseudo
@Mixin(targets = "com.titammods.block.FaucetBlockEntity", remap = false)
public abstract class MixinFaucetBlockEntity extends BlockEntity {

  private MixinFaucetBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  @Override
  public void clearRemoved() {
    super.clearRemoved();
    HephaestusModule.markDirty(this.getLevel(), this.getBlockPos().below());
  }

  @Override
  public void setRemoved() {
    super.setRemoved();
    HephaestusModule.markDirty(this.getLevel(), this.getBlockPos().below());
  }
}
