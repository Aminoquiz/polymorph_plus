package com.shiver.polymorphlegacy.compat;

import com.shiver.polymorphlegacy.client.ClientRecipes;
import java.awt.Rectangle;
import java.util.List;
import javax.annotation.Nullable;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import net.minecraft.client.gui.inventory.GuiContainer;

@JEIPlugin
public final class PolymorphJeiPlugin implements IModPlugin {
    @Override
    public void register(IModRegistry registry) {
        registry.addAdvancedGuiHandlers(new IAdvancedGuiHandler<GuiContainer>() {
            @Override
            public Class<GuiContainer> getGuiContainerClass() {
                return GuiContainer.class;
            }

            @Override
            public List<Rectangle> getGuiExtraAreas(GuiContainer gui) {
                return ClientRecipes.extraAreas(gui);
            }

            @Nullable
            @Override
            public Object getIngredientUnderMouse(GuiContainer gui, int x, int y) {
                return ClientRecipes.ingredientUnderMouse(gui, x, y);
            }
        });
    }
}
