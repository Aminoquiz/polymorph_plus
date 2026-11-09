package com.illusivesoulworks.polymorph.mixin.core;

import com.illusivesoulworks.polymorph.common.inworld.InWorldServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerChunkSender.class)
public class MixinPlayerChunkSender {

  // Fabric has no chunk watch event; NeoForge uses ChunkWatchEvent.Sent instead.
  @Inject(at = @At("TAIL"), method = "sendChunk")
  private static void polymorph$sendChunk(ServerGamePacketListenerImpl connection,
                                          ServerLevel level, LevelChunk chunk,
                                          CallbackInfo ci) {
    InWorldServer.chunkWatched(connection.player, level, chunk);
  }
}
