package com.illusivesoulworks.polymorph.mixin.integration.hephaestus;

import com.illusivesoulworks.polymorph.api.inworld.PolymorphInWorld;
import com.illusivesoulworks.polymorph.common.integration.hephaestus.HephaestusModule;
import java.util.List;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"unused", "rawtypes"})
@Pseudo
@Mixin(targets = {"com.titammods.block.TableBlockEntity", "com.titammods.block.BasinBlockEntity"},
    remap = false)
public abstract class MixinCastingBlockEntity {

  @Redirect(method = "tick", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/item/crafting/RecipeManager;getAllRecipesFor(Lnet/minecraft/world/item/crafting/RecipeType;)Ljava/util/List;"))
  private List polymorph$orderCasting(RecipeManager manager, RecipeType type) {
    BlockEntity self = (BlockEntity) (Object) this;
    return HephaestusModule.order(manager.getAllRecipesFor(type), self.getLevel(),
        self.getBlockPos());
  }

  @Unique
  private long polymorph$lastRefresh = Long.MIN_VALUE / 2;

  @Unique
  private long polymorph$nextPoll = Long.MIN_VALUE;

  // Events (tank contents, faucet placed or removed, cast moved) mark the block dirty; a slow
  // poll, phased by position so loaded blocks do not all poll on one tick, catches the fluid
  // sources no event covers. One recompute per 10 ticks at most, so a
  // burst of events costs one.
  @Inject(method = "tick", at = @At("HEAD"))
  private void polymorph$refreshCasting(CallbackInfo ci) {
    BlockEntity self = (BlockEntity) (Object) this;
    Level level = self.getLevel();

    if (level == null || level.isClientSide()) {
      return;
    }
    long time = level.getGameTime();
    boolean poll = false;

    if (this.polymorph$nextPoll == Long.MIN_VALUE) {
      this.polymorph$nextPoll = time + Math.floorMod(self.getBlockPos().hashCode(), 100);
    } else if (time >= this.polymorph$nextPoll) {
      poll = true;
      this.polymorph$nextPoll =
          time - this.polymorph$nextPoll < 100 ? this.polymorph$nextPoll + 100 : time + 100;
    }

    if (time - this.polymorph$lastRefresh < 10) {
      return;
    }

    if (HephaestusModule.takeDirty(level, self.getBlockPos()) || poll) {
      this.polymorph$lastRefresh = time;
      PolymorphInWorld.refresh(level, self.getBlockPos());
    }
  }

  @Inject(method = "interact", at = @At("TAIL"), require = 0)
  private void polymorph$afterInteract(CallbackInfo ci) {
    BlockEntity self = (BlockEntity) (Object) this;
    HephaestusModule.markDirty(self.getLevel(), self.getBlockPos());
  }
}
