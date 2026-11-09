package com.illusivesoulworks.polymorph.common.inworld;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.inworld.Choices;
import com.illusivesoulworks.polymorph.api.inworld.IChoiceStore;
import com.illusivesoulworks.polymorph.api.inworld.IInWorldProvider;
import com.illusivesoulworks.polymorph.api.inworld.Option;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

public final class InWorldServer {

  // Same bound vanilla 1.20.1 puts on block use packets.
  private static final double MAX_INTERACT_DISTANCE_SQR = 64.0D;

  private static final int CYCLE_COOLDOWN_TICKS = 6;

  private static final Map<Block, IInWorldProvider> PROVIDERS = new ConcurrentHashMap<>();
  // One entry per index of the block's provider, EMPTY where nothing shows.
  private static final Map<ServerLevel, Map<BlockPos, InWorldState[]>> LAST_SENT =
      new WeakHashMap<>();
  private static final Map<UUID, Integer> LAST_CYCLE = new HashMap<>();

  private InWorldServer() {
  }

  public static void register(Block block, IInWorldProvider provider) {
    PROVIDERS.put(block, provider);
  }

  @Nullable
  public static IInWorldProvider provider(Block block) {
    return PROVIDERS.get(block);
  }

  @Nullable
  public static ResourceLocation chosen(Level level, BlockPos pos, ResourceLocation key) {
    IInWorldProvider provider = provider(level.getBlockState(pos).getBlock());
    IChoiceStore store = provider == null ? IChoiceStore.LEVEL : provider.store();
    return store.get(level, pos, key);
  }

  public static void refresh(ServerLevel level, BlockPos pos) {
    InWorldState[] states = compute(level, pos);
    Map<BlockPos, InWorldState[]> sent = LAST_SENT.computeIfAbsent(level, k -> new HashMap<>());
    InWorldState[] last = sent.get(pos);
    int size = Math.max(states.length, last == null ? 0 : last.length);

    for (int index = 0; index < size; index++) {
      InWorldState state = at(states, index);

      if (!at(last, index).sameAs(state)) {
        sendToTracking(level, pos, index, state);
      }
    }
    remember(sent, pos, states);
  }

  public static void chunkWatched(ServerPlayer player, ServerLevel level, LevelChunk chunk) {

    if (PROVIDERS.isEmpty()) {
      return;
    }
    Map<BlockPos, InWorldState[]> sent = LAST_SENT.computeIfAbsent(level, k -> new HashMap<>());

    for (BlockPos pos : chunk.getBlockEntities().keySet()) {
      sendOnWatch(player, level, pos, sent);
    }

    for (BlockPos pos : List.copyOf(InWorldSavedData.of(level).positionsIn(chunk.getPos()))) {

      if (!chunk.getBlockEntities().containsKey(pos)) {
        sendOnWatch(player, level, pos, sent);
      }
    }
  }

  private static void sendOnWatch(ServerPlayer player, ServerLevel level, BlockPos pos,
                                  Map<BlockPos, InWorldState[]> sent) {

    if (provider(level.getBlockState(pos).getBlock()) == null) {
      return;
    }
    InWorldState[] states = compute(level, pos);

    for (int index = 0; index < states.length; index++) {

      if (!states[index].isEmpty()) {
        send(player, pos, index, states[index]);
      }
    }
    remember(sent, pos, states);
  }

  private static void remember(Map<BlockPos, InWorldState[]> sent, BlockPos pos,
                               InWorldState[] states) {

    for (InWorldState state : states) {

      if (!state.isEmpty()) {
        sent.put(pos.immutable(), states);
        return;
      }
    }
    sent.remove(pos);
  }

  private static InWorldState at(@Nullable InWorldState[] states, int index) {
    return states != null && index < states.length ? states[index] : InWorldState.EMPTY;
  }

  public static void chunkUnloaded(ServerLevel level, ChunkPos chunkPos) {
    Map<BlockPos, InWorldState[]> sent = LAST_SENT.get(level);

    if (sent != null) {
      long chunkKey = chunkPos.toLong();
      sent.keySet().removeIf(pos -> ChunkPos.asLong(pos) == chunkKey);
    }
  }

  public static void blockBroken(ServerLevel level, BlockPos pos) {
    InWorldSavedData.of(level).remove(pos);
    Map<BlockPos, InWorldState[]> sent = LAST_SENT.get(level);
    InWorldState[] last = sent != null ? sent.remove(pos) : null;

    if (last != null) {

      for (int index = 0; index < last.length; index++) {

        if (!last[index].isEmpty()) {
          sendToTracking(level, pos, index, InWorldState.EMPTY);
        }
      }
    }
  }

  /**
   * Forge and NeoForge still send START_DESTROY_BLOCK after the client cancels its own
   * event, so the server repeats the client's test to keep the block from being mined.
   */
  public static boolean isAttackOnIndicator(ServerPlayer player, BlockPos pos) {

    if (!player.getMainHandItem().isEmpty()) {
      return false;
    }
    Map<BlockPos, InWorldState[]> sent = LAST_SENT.get(player.serverLevel());
    InWorldState[] last = sent != null ? sent.get(pos) : null;
    BlockState state = player.serverLevel().getBlockState(pos);
    IInWorldProvider provider = provider(state.getBlock());

    if (last == null || provider == null) {
      return false;
    }

    for (int index = 0; index < last.length; index++) {

      if (!last[index].isEmpty() &&
          InWorldHit.isLookingAtSpot(player, pos, state, provider.spot(state, index))) {
        return true;
      }
    }
    return false;
  }

  public static void playerLeft(ServerPlayer player) {
    LAST_CYCLE.remove(player.getUUID());
  }

  public static void cycle(ServerPlayer player, BlockPos pos, int index) {
    int now = player.server.getTickCount();
    Integer last = LAST_CYCLE.get(player.getUUID());

    if (last != null && now - last < CYCLE_COOLDOWN_TICKS) {
      return;
    }
    ServerLevel level = player.serverLevel();

    if (!level.isLoaded(pos) || player.distanceToSqr(Vec3.atCenterOf(pos)) > MAX_INTERACT_DISTANCE_SQR) {
      return;
    }
    BlockState blockState = level.getBlockState(pos);
    IInWorldProvider provider = provider(blockState.getBlock());

    if (provider == null || index < 0 || index >= provider.count(blockState)) {
      return;
    }
    Choices choices = provider.choices(level, pos, blockState, index);

    if (choices == null || choices.options().size() < 2) {
      return;
    }
    LAST_CYCLE.put(player.getUUID(), now);
    List<Option> options = choices.options();
    Option next = options.get((shownIndex(provider, level, pos, choices) + 1) % options.size());
    provider.store().set(level, pos, choices.key(), next.id());
    provider.onSelected(level, pos, choices.key(), next.id());
    refresh(level, pos);
    player.displayClientMessage(Component.translatable("polymorph.inworld.output",
        next.icon().getHoverName(), next.id().getNamespace()), true);
  }

  private static InWorldState[] compute(ServerLevel level, BlockPos pos) {
    BlockState blockState = level.getBlockState(pos);
    IInWorldProvider provider = provider(blockState.getBlock());

    if (provider == null) {
      return new InWorldState[0];
    }
    InWorldState[] states = new InWorldState[provider.count(blockState)];

    for (int index = 0; index < states.length; index++) {
      Choices choices = provider.choices(level, pos, blockState, index);
      states[index] = choices == null || choices.options().size() < 2 ? InWorldState.EMPTY
          : new InWorldState(choices.key(), List.copyOf(choices.options()),
              shownIndex(provider, level, pos, choices));
    }
    return states;
  }

  private static int shownIndex(IInWorldProvider provider, ServerLevel level, BlockPos pos,
                                Choices choices) {
    int index = indexOf(choices.options(), provider.store().get(level, pos, choices.key()));

    if (index < 0) {
      index = indexOf(choices.options(), choices.current());
    }
    return Math.max(index, 0);
  }

  private static int indexOf(List<Option> options, @Nullable ResourceLocation id) {

    if (id != null) {

      for (int i = 0; i < options.size(); i++) {

        if (options.get(i).id().equals(id)) {
          return i;
        }
      }
    }
    return -1;
  }

  private static void sendToTracking(ServerLevel level, BlockPos pos, int index,
                                     InWorldState state) {

    for (ServerPlayer player : level.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos),
        false)) {
      send(player, pos, index, state);
    }
  }

  private static void send(ServerPlayer player, BlockPos pos, int index, InWorldState state) {
    PolymorphApi.common().getPacketDistributor()
        .sendInWorldStateS2C(player, pos, index, state.key(), state.options(), state.shown());
  }
}
