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

package com.illusivesoulworks.polymorph.common.brewing;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.common.impl.RecipePair;
import com.illusivesoulworks.polymorph.mixin.core.AccessorPotionBrewing;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Brewing mixes have no recipe id before 26.3, so every matching mix is listed by a synthetic id
 * built from its output.
 */
public final class BrewingCandidates {

  public interface MixReader {

    Object from(Object mix);

    Ingredient ingredient(Object mix);

    Object to(Object mix);
  }

  private static final ThreadLocal<BlockEntity> CURRENT = new ThreadLocal<>();

  private static MixReader mixReader;
  private static BiFunction<ItemStack, ItemStack, List<ItemStack>> extraOutputs =
      (bottle, reagent) -> Collections.emptyList();

  public static void setup(MixReader reader,
                           @Nullable BiFunction<ItemStack, ItemStack, List<ItemStack>> extra) {
    mixReader = reader;

    if (extra != null) {
      extraOutputs = extra;
    }
  }

  public static List<IRecipePair> candidates(ItemStack bottle, ItemStack reagent) {

    if (bottle.isEmpty() || reagent.isEmpty()) {
      return Collections.emptyList();
    }
    List<ItemStack> outputs = new ArrayList<>();

    if (mixReader != null) {
      Item item = bottle.getItem();
      Potion potion = PotionUtils.getPotion(bottle);

      for (Object mix : AccessorPotionBrewing.getContainerMixes()) {

        if (mixReader.from(mix) == item && mixReader.ingredient(mix).test(reagent)) {
          outputs.add(PotionUtils.setPotion(new ItemStack((Item) mixReader.to(mix)), potion));
        }
      }

      for (Object mix : AccessorPotionBrewing.getPotionMixes()) {

        if (mixReader.from(mix) == potion && mixReader.ingredient(mix).test(reagent)) {
          outputs.add(PotionUtils.setPotion(new ItemStack(item), (Potion) mixReader.to(mix)));
        }
      }
    }
    outputs.addAll(extraOutputs.apply(bottle, reagent));
    Map<ResourceLocation, IRecipePair> result = new LinkedHashMap<>();

    for (ItemStack output : outputs) {

      if (!output.isEmpty()) {
        result.putIfAbsent(idOf(output), new RecipePair(idOf(output), output));
      }
    }
    return new ArrayList<>(result.values());
  }

  public static ResourceLocation idOf(ItemStack output) {
    ResourceLocation item = Registry.ITEM.getKey(output.getItem());
    Potion potion = PotionUtils.getPotion(output);

    if (potion == Potions.EMPTY) {
      return new ResourceLocation(item.getNamespace(), "brewing/" + item.getPath());
    }
    ResourceLocation key = Registry.POTION.getKey(potion);
    return new ResourceLocation(key.getNamespace(),
        "brewing/" + item.getPath() + "/" + key.getPath());
  }

  public static void enter(BlockEntity blockEntity) {
    CURRENT.set(blockEntity);
  }

  public static void exit() {
    CURRENT.remove();
  }

  /** The picked output for the stand ticking on this thread, or null to let brewing run as is. */
  @Nullable
  public static ItemStack override(ItemStack bottle, ItemStack reagent) {
    BlockEntity blockEntity = CURRENT.get();

    if (blockEntity == null) {
      return null;
    }
    return PolymorphApi.common().getRecipeData(blockEntity)
        .filter(data -> data instanceof IBrewingStandRecipeData)
        .map(data -> ((IBrewingStandRecipeData) data).getBrewing().resolve(bottle, reagent))
        .orElse(null);
  }
}
