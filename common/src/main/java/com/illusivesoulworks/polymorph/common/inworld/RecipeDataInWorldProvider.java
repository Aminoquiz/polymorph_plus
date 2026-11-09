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
package com.illusivesoulworks.polymorph.common.inworld;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.api.inworld.Choices;
import com.illusivesoulworks.polymorph.api.inworld.IChoiceStore;
import com.illusivesoulworks.polymorph.api.inworld.IInWorldProvider;
import com.illusivesoulworks.polymorph.api.inworld.IndicatorSpot;
import com.illusivesoulworks.polymorph.api.inworld.Option;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Offers the candidates of a block entity's Polymorph recipe data and stores the pick in that
 * same data, so the menu widget and the in-world indicator always agree.
 */
public class RecipeDataInWorldProvider implements IInWorldProvider {

  public static final ResourceLocation KEY =
      ResourceLocation.fromNamespaceAndPath(PolymorphApi.MOD_ID, "recipe");

  private final IndicatorSpot spot;
  private final IChoiceStore store = new IChoiceStore() {

    @Nullable
    @Override
    public ResourceLocation get(Level level, BlockPos pos, ResourceLocation key) {
      IBlockEntityRecipeData data = recipeData(level, pos);
      return data != null ? data.getSelectedRecipeId() : null;
    }

    @Override
    public void set(Level level, BlockPos pos, ResourceLocation key, ResourceLocation id) {
      IBlockEntityRecipeData data = recipeData(level, pos);

      if (data != null) {
        data.chooseRecipe(id);
        data.getOwner().setChanged();
      }
    }
  };

  public RecipeDataInWorldProvider(IndicatorSpot spot) {
    this.spot = spot;
  }

  @Nullable
  @Override
  public Choices choices(ServerLevel level, BlockPos pos, BlockState state) {
    IBlockEntityRecipeData data = recipeData(level, pos);

    if (data == null || data.isEmpty()) {
      return null;
    }
    List<Option> options = new ArrayList<>();

    for (IRecipePair pair : data.getRecipesList()) {
      options.add(new Option(pair.getResourceLocation(), pair.getOutput()));
    }
    return new Choices(KEY, options, data.getSelectedRecipeId());
  }

  @Override
  public IndicatorSpot spot(BlockState state) {
    return this.spot;
  }

  @Override
  public IChoiceStore store() {
    return this.store;
  }

  @Nullable
  private static IBlockEntityRecipeData recipeData(Level level, BlockPos pos) {
    BlockEntity blockEntity = level.getBlockEntity(pos);
    return blockEntity != null ? PolymorphApi.getInstance().getBlockEntityRecipeData(blockEntity)
        : null;
  }
}
