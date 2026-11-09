package com.illusivesoulworks.polymorph.common.inworld;

import com.illusivesoulworks.polymorph.api.inworld.IChoiceStore;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class InWorldSavedData extends SavedData {

  private static final String FILE_NAME = "polymorph_plus_inworld";

  public static final IChoiceStore STORE = new IChoiceStore() {

    @Nullable
    @Override
    public ResourceLocation get(Level level, BlockPos pos, ResourceLocation key) {

      if (level instanceof ServerLevel serverLevel) {
        Entry entry = of(serverLevel).entry(serverLevel, pos);
        return entry == null ? null : entry.choices.get(key);
      }
      return null;
    }

    @Override
    public void set(Level level, BlockPos pos, ResourceLocation key, ResourceLocation id) {

      if (level instanceof ServerLevel serverLevel) {
        of(serverLevel).put(serverLevel, pos, key, id);
      }
    }
  };

  public record Entry(ResourceLocation blockId, Map<ResourceLocation, ResourceLocation> choices) {
  }

  // Grouped by chunk so a chunk watch only walks its own entries.
  private final Map<Long, Map<BlockPos, Entry>> byChunk = new HashMap<>();

  public static InWorldSavedData of(ServerLevel level) {
    return level.getDataStorage().computeIfAbsent(InWorldSavedData::load, InWorldSavedData::new, FILE_NAME);
  }

  @Nullable
  private Entry entry(ServerLevel level, BlockPos pos) {
    Map<BlockPos, Entry> chunk = byChunk.get(ChunkPos.asLong(pos));

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

  private void put(ServerLevel level, BlockPos pos, ResourceLocation key, ResourceLocation id) {
    Entry entry = entry(level, pos);

    if (entry == null) {
      entry = new Entry(blockId(level, pos), new HashMap<>());
      byChunk.computeIfAbsent(ChunkPos.asLong(pos), k -> new HashMap<>())
          .put(pos.immutable(), entry);
    }
    entry.choices.put(key, id);
    setDirty();
  }

  public void remove(BlockPos pos) {
    long chunkKey = ChunkPos.asLong(pos);
    Map<BlockPos, Entry> chunk = byChunk.get(chunkKey);

    if (chunk != null && chunk.remove(pos) != null) {

      if (chunk.isEmpty()) {
        byChunk.remove(chunkKey);
      }
      setDirty();
    }
  }

  public Collection<BlockPos> positionsIn(ChunkPos chunkPos) {
    Map<BlockPos, Entry> chunk = byChunk.get(chunkPos.toLong());
    return chunk == null ? Collections.emptyList() : chunk.keySet();
  }

  private static ResourceLocation blockId(ServerLevel level, BlockPos pos) {
    return BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock());
  }

  private static InWorldSavedData load(CompoundTag tag) {
    InWorldSavedData data = new InWorldSavedData();
    ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);

    for (int i = 0; i < list.size(); i++) {
      CompoundTag entryTag = list.getCompound(i);
      ResourceLocation blockId = ResourceLocation.tryParse(entryTag.getString("Block"));

      if (blockId == null) {
        continue;
      }
      Map<ResourceLocation, ResourceLocation> choices = new HashMap<>();
      CompoundTag choicesTag = entryTag.getCompound("Choices");

      for (String key : choicesTag.getAllKeys()) {
        ResourceLocation keyId = ResourceLocation.tryParse(key);
        ResourceLocation optionId = ResourceLocation.tryParse(choicesTag.getString(key));

        if (keyId != null && optionId != null) {
          choices.put(keyId, optionId);
        }
      }

      if (!choices.isEmpty()) {
        BlockPos pos = BlockPos.of(entryTag.getLong("Pos"));
        data.byChunk.computeIfAbsent(ChunkPos.asLong(pos), k -> new HashMap<>())
            .put(pos, new Entry(blockId, choices));
      }
    }
    return data;
  }

  @Override
  public CompoundTag save(CompoundTag tag) {
    ListTag list = new ListTag();

    for (Map<BlockPos, Entry> chunk : byChunk.values()) {

      for (Map.Entry<BlockPos, Entry> e : chunk.entrySet()) {
        CompoundTag entryTag = new CompoundTag();
        entryTag.putLong("Pos", e.getKey().asLong());
        entryTag.putString("Block", e.getValue().blockId.toString());
        CompoundTag choicesTag = new CompoundTag();
        e.getValue().choices.forEach((key, id) -> choicesTag.putString(key.toString(), id.toString()));
        entryTag.put("Choices", choicesTag);
        list.add(entryTag);
      }
    }
    tag.put("Entries", list);
    return tag;
  }
}
