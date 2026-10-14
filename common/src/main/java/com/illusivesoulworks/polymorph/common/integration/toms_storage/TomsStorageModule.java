package com.illusivesoulworks.polymorph.common.integration.toms_storage;

import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.RecipeHolder;

public class TomsStorageModule extends AbstractCompatibilityModule {

  private static final String CRAFTING_TERMINAL = "com.tom.storagemod.menu.CraftingTerminalMenu";
  // The terminal's own menu button for "resolve again for this player".
  private static final int POLYMORPH_BUTTON = 1;

  public static boolean isCraftingTerminal(AbstractContainerMenu containerMenu) {
    return containerMenu != null && containerMenu.getClass().getName().equals(CRAFTING_TERMINAL);
  }

  @Override
  protected boolean openContainer(AbstractContainerMenu containerMenu,
                                  ServerPlayer serverPlayerEntity) {
    return refresh(containerMenu, serverPlayerEntity);
  }

  @Override
  protected boolean selectRecipe(AbstractContainerMenu containerMenu, RecipeHolder<?> recipe) {

    if (isCraftingTerminal(containerMenu)) {

      for (Slot slot : containerMenu.slots) {

        if (slot.container instanceof Inventory inventory) {
          return refresh(containerMenu, inventory.player);
        }
      }
    }
    return false;
  }

  private static boolean refresh(AbstractContainerMenu containerMenu, Player player) {

    if (isCraftingTerminal(containerMenu)) {
      containerMenu.clickMenuButton(player, POLYMORPH_BUTTON);
      return true;
    }
    return false;
  }
}
