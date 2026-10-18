package com.illusivesoulworks.polymorph.common.integration.jei;

import com.illusivesoulworks.polymorph.PolymorphConstants;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;

/**
 * Selects the recipe JEI just filled through JEI's transfer listener API. JEI builds older than
 * the listener API lack addRecipeTransferListener, so registration skips them.
 */
@JeiPlugin
public final class PolymorphJeiPlugin implements IModPlugin {

  @Override
  public ResourceLocation getPluginUid() {
    return new ResourceLocation("polymorph", "recipe_transfer");
  }

  @Override
  public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    try {
      Class.forName("mezz.jei.api.recipe.transfer.IRecipeTransferListener", false,
          PolymorphJeiPlugin.class.getClassLoader());
    } catch (ClassNotFoundException e) {
      PolymorphConstants.LOG.warn("JEI too old for recipe transfer listeners, skipping");
      return;
    }
    JeiTransferListener.register(registration);
  }
}
