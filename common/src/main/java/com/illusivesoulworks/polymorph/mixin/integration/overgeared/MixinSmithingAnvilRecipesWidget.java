package com.illusivesoulworks.polymorph.mixin.integration.overgeared;

import com.illusivesoulworks.polymorph.api.client.base.PersistentRecipesWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

// Above the output slot the button covers the anvil's "Hits Left" text, so it moves to the right.
@Pseudo
@Mixin(targets = "net.stirdrem.overgeared.compat.polymorph.client.SmithingAnvilRecipesWidget",
    remap = false)
public abstract class MixinSmithingAnvilRecipesWidget extends PersistentRecipesWidget {

  private MixinSmithingAnvilRecipesWidget(AbstractContainerScreen<?> screen) {
    super(screen);
  }

  @Override
  public int getXPos() {
    return this.getOutputSlot().x + 22;
  }

  @Override
  public int getYPos() {
    return this.getOutputSlot().y - 1;
  }
}
