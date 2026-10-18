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

package com.illusivesoulworks.polymorph;

import com.illusivesoulworks.polymorph.client.ClientEventsListener;
import com.illusivesoulworks.polymorph.common.CommonEventsListener;
import com.illusivesoulworks.polymorph.common.PolymorphForgeNetwork;
import com.illusivesoulworks.polymorph.common.brewing.BrewingCandidates;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PolymorphConstants.MOD_ID)
public class PolymorphForgeMod {

  public PolymorphForgeMod() {
    PolymorphCommonMod.init();
    IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
    eventBus.addListener(this::setup);
    eventBus.addListener(this::clientSetup);
  }

  private void setup(final FMLCommonSetupEvent evt) {
    PolymorphCommonMod.setup();
    PolymorphForgeNetwork.setup();
    BrewingCandidates.setup(new BrewingCandidates.MixReader() {
      @Override
      public Object from(Object mix) {
        return ((PotionBrewing.Mix<?>) mix).from.get();
      }

      @Override
      public Ingredient ingredient(Object mix) {
        return ((PotionBrewing.Mix<?>) mix).ingredient;
      }

      @Override
      public Object to(Object mix) {
        return ((PotionBrewing.Mix<?>) mix).to.get();
      }
    }, (bottle, reagent) -> {
      List<ItemStack> outputs = new ArrayList<>();

      for (IBrewingRecipe recipe : BrewingRecipeRegistry.getRecipes()) {

        if (!(recipe instanceof VanillaBrewingRecipe) && recipe.isInput(bottle) &&
            recipe.isIngredient(reagent)) {
          outputs.add(recipe.getOutput(bottle, reagent));
        }
      }
      return outputs;
    });
    MinecraftForge.EVENT_BUS.register(new CommonEventsListener());
  }

  private void clientSetup(final FMLClientSetupEvent evt) {
    PolymorphCommonMod.clientSetup();
    MinecraftForge.EVENT_BUS.register(new ClientEventsListener());
  }
}