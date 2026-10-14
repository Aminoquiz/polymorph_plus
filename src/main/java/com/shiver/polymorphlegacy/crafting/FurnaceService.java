package com.shiver.polymorphlegacy.crafting;

import com.shiver.polymorphlegacy.api.RecipeChoice;
import com.shiver.polymorphlegacy.mixin.core.AccessorContainerFurnace;
import com.shiver.polymorphlegacy.network.PolymorphNetwork;
import com.shiver.polymorphlegacy.network.RecipesPacket;
import com.shiver.polymorphlegacy.priority.RecipeRanking;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ResourceLocation;

/**
 * Smelting conflicts on 1.12. FurnaceRecipes is a plain input-to-output map without ids, and
 * Forge refuses a second entry for an input it already smelts, so conflicts only come from a
 * wildcard-damage entry next to an exact one, or from mods writing the map directly. Each
 * candidate gets an id built from its output, {@code <output mod>:furnace/<item>/<meta>}, so
 * the selector, the priority lists and the furnace NBT can name it.
 */
public final class FurnaceService {
    private static Map<Item, List<Map.Entry<ItemStack, ItemStack>>> index;
    private static Map<ItemStack, ItemStack> indexedMap;
    private static int indexedSize = -1;

    private FurnaceService() {
    }

    private static synchronized List<Map.Entry<ItemStack, ItemStack>> entriesFor(Item item) {
        Map<ItemStack, ItemStack> map = FurnaceRecipes.instance().getSmeltingList();
        if (index == null || indexedMap != map || indexedSize != map.size()) {
            Map<Item, List<Map.Entry<ItemStack, ItemStack>>> built = new IdentityHashMap<>();
            for (Map.Entry<ItemStack, ItemStack> entry : map.entrySet()) {
                if (!entry.getKey().isEmpty()) {
                    built.computeIfAbsent(entry.getKey().getItem(), k -> new ArrayList<>()).add(entry);
                }
            }
            index = built;
            indexedMap = map;
            indexedSize = map.size();
        }
        List<Map.Entry<ItemStack, ItemStack>> entries = index.get(item);
        return entries == null ? Collections.<Map.Entry<ItemStack, ItemStack>>emptyList() : entries;
    }

    /** Distinct outputs for an input, in map order. */
    public static List<ItemStack> outputs(ItemStack input) {
        if (input.isEmpty()) {
            return Collections.emptyList();
        }
        List<ItemStack> outputs = new ArrayList<>();
        for (Map.Entry<ItemStack, ItemStack> entry : entriesFor(input.getItem())) {
            int meta = entry.getKey().getMetadata();
            if (meta != 32767 && meta != input.getMetadata()) {
                continue;
            }
            ItemStack output = entry.getValue();
            boolean known = output.isEmpty();
            for (ItemStack other : outputs) {
                known |= ItemStack.areItemStacksEqual(other, output);
            }
            if (!known) {
                outputs.add(output);
            }
        }
        return outputs;
    }

    public static ResourceLocation idOf(ItemStack output) {
        ResourceLocation item = output.getItem().getRegistryName();
        String namespace = item == null ? "minecraft" : item.getNamespace();
        String path = item == null ? "unknown" : item.getPath();
        return new ResourceLocation(namespace, "furnace/" + path + "/" + output.getMetadata());
    }

    private static List<ResourceLocation> ids(List<ItemStack> outputs) {
        List<ResourceLocation> ids = new ArrayList<>(outputs.size());
        for (ItemStack output : outputs) {
            ids.add(idOf(output));
        }
        return ids;
    }

    @Nullable
    private static ResourceLocation choose(List<ResourceLocation> ids, @Nullable ResourceLocation stored) {
        if (ids.isEmpty()) {
            return null;
        }
        if (stored != null && ids.contains(stored)) {
            return stored;
        }
        List<String> names = new ArrayList<>(ids.size());
        for (ResourceLocation id : ids) {
            names.add(id.toString());
        }
        return ids.get(RecipeRanking.NONE.choose(names));
    }

    /** Replaces FurnaceRecipes.getSmeltingResult inside the furnace's own tick. */
    public static ItemStack result(TileEntityFurnace furnace, ItemStack input, ItemStack vanilla) {
        if (vanilla.isEmpty()) {
            return vanilla;
        }
        List<ItemStack> outputs = outputs(input);
        if (outputs.size() < 2) {
            return vanilla;
        }
        List<ResourceLocation> ids = ids(outputs);
        ResourceLocation chosen = choose(ids, ((FurnaceSelectionHolder) furnace).polymorph$getFurnaceSelection());
        return outputs.get(ids.indexOf(chosen));
    }

    public static boolean isFurnace(EntityPlayerMP player, int windowId) {
        Container container = player.openContainer;
        return container instanceof ContainerFurnace && container.windowId == windowId
                && furnace((ContainerFurnace) container) != null;
    }

    @Nullable
    private static TileEntityFurnace furnace(ContainerFurnace container) {
        IInventory inventory = ((AccessorContainerFurnace) container).polymorph$getTileFurnace();
        return inventory instanceof TileEntityFurnace ? (TileEntityFurnace) inventory : null;
    }

    private static CraftingState state(Container container) {
        return ((CraftingStateHolder) container).polymorph$getCraftingState();
    }

    public static void openView(EntityPlayerMP player, int viewId) {
        if (viewId != 0 && player.openContainer.canInteractWith(player)) {
            state(player.openContainer).openView(viewId);
            update((ContainerFurnace) player.openContainer, player);
        }
    }

    public static boolean select(EntityPlayerMP player, int viewId, ResourceLocation id) {
        ContainerFurnace container = (ContainerFurnace) player.openContainer;
        TileEntityFurnace furnace = furnace(container);
        if (viewId == 0 || state(container).getViewId() != viewId || !container.canInteractWith(player)) {
            return false;
        }
        boolean valid = ids(outputs(furnace.getStackInSlot(0))).contains(id);
        if (valid) {
            ((FurnaceSelectionHolder) furnace).polymorph$setFurnaceSelection(id);
            furnace.markDirty();
        }
        update(container, player);
        return valid;
    }

    /** Called from detectAndSendChanges, so a changed input or another viewer's pick reaches this one. */
    public static void detectChanges(ContainerFurnace container, List<IContainerListener> listeners) {
        if (state(container).getViewId() == 0) {
            return;
        }
        for (IContainerListener listener : listeners) {
            if (listener instanceof EntityPlayerMP && ((EntityPlayerMP) listener).openContainer == container) {
                update(container, (EntityPlayerMP) listener);
                return;
            }
        }
    }

    private static void update(ContainerFurnace container, EntityPlayerMP player) {
        TileEntityFurnace furnace = furnace(container);
        if (furnace == null) {
            return;
        }
        List<ItemStack> outputs = outputs(furnace.getStackInSlot(0));
        List<ResourceLocation> ids = ids(outputs);
        List<RecipeChoice> choices = new ArrayList<>(outputs.size());
        for (int i = 0; i < outputs.size(); i++) {
            choices.add(new RecipeChoice(ids.get(i), outputs.get(i)));
        }
        CraftingState state = state(container);
        ResourceLocation chosen = choose(ids, ((FurnaceSelectionHolder) furnace).polymorph$getFurnaceSelection());
        state.getSelection().clear();
        state.getSelection().select(chosen, ids);
        state.resolved(choices, null);
        if (state.needsSync()) {
            PolymorphNetwork.CHANNEL.sendTo(new RecipesPacket(container.windowId, state.getViewId(),
                    choices, chosen), player);
            state.markSynced();
        }
    }
}
