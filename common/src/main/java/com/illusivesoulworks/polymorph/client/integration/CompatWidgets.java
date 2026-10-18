package com.illusivesoulworks.polymorph.client.integration;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.client.recipe.widget.FurnaceRecipesWidget;

public class CompatWidgets {

  // Furnace-style selector on a modded menu whose output is slot 2.
  public static void registerFurnace(Class<?> menuClass) {
    PolymorphApi.client().registerWidget(containerScreen ->
        menuClass.isInstance(containerScreen.getMenu())
            && containerScreen.getMenu().slots.size() > 2
            ? new FurnaceRecipesWidget(containerScreen) : null);
  }
}
