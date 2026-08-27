/*
 * Copyright (C) 2020-2022 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Polymorph is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Polymorph.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.illusivesoulworks.polymorph.common;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import com.illusivesoulworks.polymorph.api.common.capability.IRecipeData;
import com.illusivesoulworks.polymorph.common.capability.PlayerRecipeData;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class PolymorphNeoForgeCapabilities {

  private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
      DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, PolymorphApi.MOD_ID);

  public static final Supplier<AttachmentType<RecipeDataAttachment>> RECIPE_DATA =
      ATTACHMENT_TYPES.register(
          "recipe_data", () -> AttachmentType.serializable(RecipeDataAttachment::new).build()
      );

  public static void setup(IEventBus eventBus) {
    ATTACHMENT_TYPES.register(eventBus);
  }

  public static class RecipeDataAttachment implements INBTSerializable<CompoundTag> {

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

    @Override
    public CompoundTag serializeNBT(@Nonnull HolderLookup.Provider provider) {

      if (this.recipeData != null) {
        return this.recipeData.writeNBT(provider);
      }
      return null;
    }

    @Override
    public void deserializeNBT(@Nonnull HolderLookup.Provider provider, @Nonnull CompoundTag nbt) {

      if (this.recipeData != null) {
        this.recipeData.readNBT(provider, nbt);
      }
    }
  }
}
