package com.illusivesoulworks.polymorph.common.inworld;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.inworld.IChoiceStore;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class InWorldSavedData extends SavedData {

  private record Stored(BlockPos pos, Identifier block, Map<Identifier, Identifier> choices) {

    static final Codec<Stored> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.fieldOf("Pos").forGetter(Stored::pos),
        Identifier.CODEC.fieldOf("Block").forGetter(Stored::block),
        Codec.unboundedMap(Identifier.CODEC, Identifier.CODEC).fieldOf("Choices")
            .forGetter(Stored::choices)
    ).apply(instance, Stored::new));
  }

  private static final Codec<InWorldSavedData> CODEC = Stored.CODEC.listOf()
      .fieldOf("Entries").codec()
      .xmap(InWorldSavedData::fromStored, InWorldSavedData::toStored);

  // Vanilla runs the data fixer of the given type on load; command storage has no fixes that
  // touch our keys.
  private static final SavedDataType<InWorldSavedData> TYPE = new SavedDataType<>(
      Identifier.fromNamespaceAndPath(PolymorphApi.MOD_ID, "inworld"), InWorldSavedData::new,
      CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

  public static final IChoiceStore STORE = new IChoiceStore() {

    @Nullable
    @Override
    public Identifier get(Level level, BlockPos pos, Identifier key) {

      if (level instanceof ServerLevel serverLevel) {
        Entry entry = of(serverLevel).entry(serverLevel, pos);
        return entry == null ? null : entry.choices.get(key);
      }
      return null;
    }

    @Override
    public void set(Level level, BlockPos pos, Identifier key, Identifier id) {

      if (level instanceof ServerLevel serverLevel) {
        of(serverLevel).put(serverLevel, pos, key, id);
      }
    }
  };

  public record Entry(Identifier blockId, Map<Identifier, Identifier> choices) {
  }

  // Grouped by chunk so a chunk watch only walks its own entries.
  private final Map<Long, Map<BlockPos, Entry>> byChunk = new HashMap<>();

  public static InWorldSavedData of(ServerLevel level) {
    return level.getDataStorage().computeIfAbsent(TYPE);
  }

  @Nullable
  private Entry entry(ServerLevel level, BlockPos pos) {
    Map<BlockPos, Entry> chunk = byChunk.get(ChunkPos.pack(pos));

    if (chunk == null) {
      return null;
    }
    Entry entry = chunk.get(pos);

    if (entry != null && !entry.blockId.equals(blockId(level, pos))) {
      remove(pos);
      return null;
    }
    return entry;
  }

  private void put(ServerLevel level, BlockPos pos, Identifier key, Identifier id) {
    Entry entry = entry(level, pos);

    if (entry == null) {
      entry = new Entry(blockId(level, pos), new HashMap<>());
      byChunk.computeIfAbsent(ChunkPos.pack(pos), k -> new HashMap<>())
          .put(pos.immutable(), entry);
    }
    entry.choices.put(key, id);
    setDirty();
  }

  public void remove(BlockPos pos) {
    long chunkKey = ChunkPos.pack(pos);
    Map<BlockPos, Entry> chunk = byChunk.get(chunkKey);

    if (chunk != null && chunk.remove(pos) != null) {

      if (chunk.isEmpty()) {
        byChunk.remove(chunkKey);
      }
      setDirty();
    }
  }

  public Collection<BlockPos> positionsIn(ChunkPos chunkPos) {
    Map<BlockPos, Entry> chunk = byChunk.get(chunkPos.pack());
    return chunk == null ? Collections.emptyList() : chunk.keySet();
  }

  private static Identifier blockId(ServerLevel level, BlockPos pos) {
    return BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock());
  }

  private static InWorldSavedData fromStored(List<Stored> entries) {
    InWorldSavedData data = new InWorldSavedData();

    for (Stored stored : entries) {

      if (!stored.choices().isEmpty()) {
        data.byChunk.computeIfAbsent(ChunkPos.pack(stored.pos()), k -> new HashMap<>())
            .put(stored.pos(), new Entry(stored.block(), new HashMap<>(stored.choices())));
      }
    }
    return data;
  }

  private List<Stored> toStored() {
    List<Stored> entries = new ArrayList<>();

    for (Map<BlockPos, Entry> chunk : byChunk.values()) {
      chunk.forEach((pos, entry) -> entries.add(new Stored(pos, entry.blockId, entry.choices)));
    }
    return entries;
  }
}
