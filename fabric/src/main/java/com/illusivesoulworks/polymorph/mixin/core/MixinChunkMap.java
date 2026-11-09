package com.illusivesoulworks.polymorph.mixin.core;

import com.illusivesoulworks.polymorph.common.inworld.InWorldServer;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkMap.class)
public class MixinChunkMap {

  @Shadow
  @Final
  ServerLevel level;

  // Fabric has no chunk watch event; Forge uses ChunkWatchEvent.Watch instead.
  @Inject(at = @At("TAIL"), method = "playerLoadedChunk")
  private void polymorph$playerLoadedChunk(
      ServerPlayer player, MutableObject<ClientboundLevelChunkWithLightPacket> packet,
      LevelChunk chunk, CallbackInfo ci) {
    InWorldServer.chunkWatched(player, level, chunk);
  }
}
