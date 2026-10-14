/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.common;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import com.illusivesoulworks.polymorph.api.common.capability.IRecipeData;
import com.illusivesoulworks.polymorph.common.capability.PlayerRecipeData;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * MC 26.1 / NeoForge 26.1 fork. {@code INBTSerializable} was removed; attachments now
 * persist through {@link ValueOutput} / {@link ValueInput} via either
 * {@link AttachmentType.Builder#serialize(com.mojang.serialization.MapCodec)} or a custom
 * {@link IAttachmentSerializer}. Bridge the existing NBT-based {@code IRecipeData}
 * write/readNBT into ValueIO by storing the produced {@code CompoundTag} under a fixed
 * key with {@link CompoundTag#CODEC}.
 */
public class PolymorphNeoForgeCapabilities {

  private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
      DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, PolymorphApi.MOD_ID);

  private static final String DATA_KEY = "data";

  public static final Supplier<AttachmentType<RecipeDataAttachment>> RECIPE_DATA =
      ATTACHMENT_TYPES.register(
          "recipe_data",
          () -> AttachmentType.builder(RecipeDataAttachment::new)
              .serialize(new RecipeDataSerializer())
              .build()
      );

  public static void setup(IEventBus eventBus) {
    ATTACHMENT_TYPES.register(eventBus);
  }

  public static class RecipeDataAttachment {

    private IRecipeData<?> recipeData;

    public RecipeDataAttachment(IAttachmentHolder attachmentHolder) {

      if (attachmentHolder instanceof Player player) {
        this.recipeData = new PlayerRecipeData(player);
      } else if (attachmentHolder instanceof BlockEntity blockEntity) {
        IBlockEntityRecipeData data =
            PolymorphApi.getInstance().createBlockEntityRecipeData(blockEntity);

        if (data != null) {
          this.recipeData = data;
        }
      }
    }

    public IRecipeData<?> getRecipeData() {
      return this.recipeData;
    }
  }

  private static class RecipeDataSerializer implements IAttachmentSerializer<RecipeDataAttachment> {

    @Override
    public RecipeDataAttachment read(IAttachmentHolder holder, ValueInput input) {
      RecipeDataAttachment attachment = new RecipeDataAttachment(holder);

      if (attachment.recipeData != null) {
        CompoundTag tag = input.read(DATA_KEY, CompoundTag.CODEC).orElse(new CompoundTag());
        attachment.recipeData.readNBT(input.lookup(), tag);
      }
      return attachment;
    }

    @Override
    public boolean write(RecipeDataAttachment attachment, ValueOutput output) {

      if (attachment.recipeData == null) {
        return false;
      }
      CompoundTag tag = attachment.recipeData.writeNBT(null);

      if (tag == null || tag.isEmpty()) {
        return false;
      }
      output.store(DATA_KEY, CompoundTag.CODEC, tag);
      return true;
    }
  }
}
