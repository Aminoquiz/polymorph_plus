package com.illusivesoulworks.polymorph.common.network;

import com.illusivesoulworks.polymorph.common.network.client.CPacketBlockEntityListener;
import com.illusivesoulworks.polymorph.common.network.client.CPacketPersistentRecipeSelection;
import com.illusivesoulworks.polymorph.common.network.client.CPacketPlayerRecipeSelection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

  private static final ServerPayloadHandler INSTANCE = new ServerPayloadHandler();

  public static ServerPayloadHandler getInstance() {
    return INSTANCE;
  }

  private static void handleData(final IPayloadContext context, Runnable handler) {
    context.enqueueWork(handler)
        .exceptionally(e -> {
          context.disconnect(Component.translatable("polymorph.networking.failed", e.getMessage()));
          return null;
        });
  }

  public void handlePacket(final CPacketBlockEntityListener packet, final IPayloadContext ctx) {
    handleData(ctx, () -> {
      if (ctx.player() instanceof ServerPlayer serverPlayer) {
        CPacketBlockEntityListener.handle(packet, serverPlayer);
      }
    });
  }

  public void handlePacket(final CPacketPersistentRecipeSelection packet,
                           final IPayloadContext ctx) {
    handleData(ctx, () -> {
      if (ctx.player() instanceof ServerPlayer serverPlayer) {
        CPacketPersistentRecipeSelection.handle(packet, serverPlayer);
      }
    });
  }

  public void handlePacket(final CPacketPlayerRecipeSelection packet,
                           final IPayloadContext ctx) {
    handleData(ctx, () -> {
      if (ctx.player() instanceof ServerPlayer serverPlayer) {
        CPacketPlayerRecipeSelection.handle(packet, serverPlayer);
      }
    });
  }
}
