package com.illusivesoulworks.polymorph.mixin.integration.roughlyenoughitems;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IPolymorphCommon;
import com.illusivesoulworks.polymorph.client.recipe.RecipesWidget;
import java.util.function.Supplier;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.api.widgets.Button;
import me.shedaniel.rei.impl.InternalWidgets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * REI 5.x: lambda$0 is the "+" click handler, it runs the auto-transfer handlers.
 */
@Mixin(value = InternalWidgets.class, remap = false)
public class MixinInternalWidgets {

  @Inject(
      at = @At("HEAD"),
      method = "lambda$createAutoCraftingButtonWidget$0",
      require = 0
  )
  private static void polymorph$create(AbstractContainerScreen<?> screen,
                                       Supplier<RecipeDisplay> displaySupplier,
                                       Button button, CallbackInfo ci) {
    displaySupplier.get().getRecipeLocation().ifPresent(MixinInternalWidgets::polymorph$select);
  }

  private static void polymorph$select(ResourceLocation id) {

    if (RecipesWidget.get().isPresent()) {
      RecipesWidget.get().get().selectRecipe(id);
      return;
    }
    IPolymorphCommon commonApi = PolymorphApi.common();
    Player player = Minecraft.getInstance().player;

    if (player != null) {
      player.level.getRecipeManager().byKey(id).ifPresent(
          recipe -> commonApi.getRecipeData(player)
              .ifPresent(recipeData -> recipeData.selectRecipe(recipe)));
    }
    commonApi.getPacketDistributor().sendPlayerRecipeSelectionC2S(id);
  }
}
