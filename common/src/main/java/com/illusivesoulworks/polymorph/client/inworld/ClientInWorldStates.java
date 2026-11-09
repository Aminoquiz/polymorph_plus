package com.illusivesoulworks.polymorph.client.inworld;

import com.illusivesoulworks.polymorph.api.inworld.IInWorldProvider;
import com.illusivesoulworks.polymorph.common.inworld.InWorldServer;
import com.illusivesoulworks.polymorph.common.inworld.InWorldState;
import com.illusivesoulworks.polymorph.common.network.server.SPacketInWorldState;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

public final class ClientInWorldStates {

  // Position, then the provider's index, then what shows there.
  private static final Map<BlockPos, Map<Integer, InWorldState>> STATES = new HashMap<>();
  @Nullable
  private static ClientLevel owner;

  private ClientInWorldStates() {
  }

  public static void apply(SPacketInWorldState packet) {
    ClientLevel level = Minecraft.getInstance().level;

    checkLevel(level);

    if (level == null) {
      return;
    }

    if (packet.options().isEmpty() || packet.shown() < 0 ||
        packet.shown() >= packet.options().size()) {
      Map<Integer, InWorldState> states = STATES.get(packet.pos());

      if (states != null) {
        states.remove(packet.index());

        if (states.isEmpty()) {
          STATES.remove(packet.pos());
        }
      }
      return;
    }
    InWorldState state = new InWorldState(packet.key(), packet.options(), packet.shown());
    STATES.computeIfAbsent(packet.pos(), k -> new HashMap<>()).put(packet.index(), state);
    IInWorldProvider provider = InWorldServer.provider(level.getBlockState(packet.pos()).getBlock());

    if (provider != null) {
      provider.onSelected(level, packet.pos(), state.key(), state.shownId());
    }
  }

  @Nullable
  public static Map<Integer, InWorldState> at(BlockPos pos) {
    return STATES.get(pos);
  }

  public static Map<BlockPos, Map<Integer, InWorldState>> all() {
    checkLevel(Minecraft.getInstance().level);
    return STATES;
  }

  @Nullable
  public static ResourceLocation chosen(BlockPos pos, ResourceLocation key) {
    Map<Integer, InWorldState> states = STATES.get(pos);

    if (states != null) {

      for (InWorldState state : states.values()) {

        if (state.key().equals(key)) {
          return state.shownId();
        }
      }
    }
    return null;
  }

  public static void clear() {
    STATES.clear();
    owner = null;
  }

  public static void chunkUnloaded(ChunkPos chunkPos) {
    long chunkKey = chunkPos.toLong();
    STATES.keySet().removeIf(pos -> ChunkPos.asLong(pos) == chunkKey);
  }

  /** A dimension change swaps the level without unloading its chunks one by one. */
  private static void checkLevel(@Nullable ClientLevel level) {

    if (level != owner) {
      STATES.clear();
      owner = level;
    }
  }
}
