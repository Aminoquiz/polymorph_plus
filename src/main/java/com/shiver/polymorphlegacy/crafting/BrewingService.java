package com.shiver.polymorphlegacy.crafting;

import com.shiver.polymorphlegacy.api.RecipeChoice;
import com.shiver.polymorphlegacy.mixin.core.AccessorContainerBrewingStand;
import com.shiver.polymorphlegacy.mixin.core.AccessorMixPredicate;
import com.shiver.polymorphlegacy.mixin.core.AccessorPotionHelper;
import com.shiver.polymorphlegacy.network.PolymorphNetwork;
import com.shiver.polymorphlegacy.network.RecipesPacket;
import com.shiver.polymorphlegacy.priority.RecipeRanking;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;

/**
 * Brewing conflicts on 1.12. Forge's BrewingRecipeRegistry and vanilla PotionHelper both stop at
 * the first match, so a second mix on the same bottle and reagent never brews. Candidates are
 * every output those two would reach, in their order, with an id built from the output:
 * {@code <item mod>:brewing/<item>/<potion mod>/<potion>} for potions, {@code <item mod>:brewing/<item>/<meta>}
 * otherwise. One pick per stand, stored like the furnace's; it applies to each bottle that offers it.
 */
public final class BrewingService {
    private static final int[] BOTTLES = {0, 1, 2};

    private BrewingService() {
    }

    public static List<ItemStack> outputs(ItemStack input, ItemStack reagent) {
        if (input.isEmpty() || input.getCount() != 1 || reagent.isEmpty()) {
            return Collections.emptyList();
        }
        List<ItemStack> outputs = new ArrayList<>();
        for (IBrewingRecipe recipe : BrewingRecipeRegistry.getRecipes()) {
            if (recipe instanceof VanillaBrewingRecipe) {
                if (recipe.isIngredient(reagent)) {
                    vanilla(input, reagent, outputs);
                }
            } else {
                add(outputs, recipe.getOutput(input, reagent));
            }
        }
        return outputs;
    }

    // PotionHelper.doReaction, keeping every match instead of the first.
    private static void vanilla(ItemStack input, ItemStack reagent, List<ItemStack> outputs) {
        PotionType type = PotionUtils.getPotionFromItem(input);
        Item item = input.getItem();
        for (Object o : AccessorPotionHelper.polymorph$getItemConversions()) {
            AccessorMixPredicate mix = (AccessorMixPredicate) o;
            if (mix.polymorph$getInput().get() == item && mix.polymorph$getReagent().apply(reagent)) {
                add(outputs, PotionUtils.addPotionToItemStack(new ItemStack((Item) mix.polymorph$getOutput().get()), type));
            }
        }
        for (Object o : AccessorPotionHelper.polymorph$getTypeConversions()) {
            AccessorMixPredicate mix = (AccessorMixPredicate) o;
            if (mix.polymorph$getInput().get() == type && mix.polymorph$getReagent().apply(reagent)) {
                add(outputs, PotionUtils.addPotionToItemStack(new ItemStack(item), (PotionType) mix.polymorph$getOutput().get()));
            }
        }
    }

    private static void add(List<ItemStack> outputs, ItemStack output) {
        if (output.isEmpty()) {
            return;
        }
        for (ItemStack other : outputs) {
            if (ItemStack.areItemStacksEqual(other, output)) {
                return;
            }
        }
        outputs.add(output);
    }

    public static ResourceLocation idOf(ItemStack output) {
        ResourceLocation item = output.getItem().getRegistryName();
        String namespace = item == null ? "minecraft" : item.getNamespace();
        String path = "brewing/" + (item == null ? "unknown" : item.getPath());
        ResourceLocation potion = PotionType.REGISTRY.getNameForObject(PotionUtils.getPotionFromItem(output));
        if (potion != null && output.hasTagCompound() && output.getTagCompound().hasKey("Potion", 8)) {
            path += "/" + potion.getNamespace() + "/" + potion.getPath();
        } else {
            path += "/" + output.getMetadata();
        }
        return new ResourceLocation(namespace, path);
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

    @Nullable
    private static ResourceLocation stored(TileEntityBrewingStand stand) {
        return ((FurnaceSelectionHolder) stand).polymorph$getFurnaceSelection();
    }

    /** Replaces BrewingRecipeRegistry.getOutput for one bottle inside the stand's own brew. */
    public static ItemStack result(TileEntityBrewingStand stand, ItemStack input, ItemStack reagent) {
        ItemStack vanilla = BrewingRecipeRegistry.getOutput(input, reagent);
        if (vanilla.isEmpty()) {
            return vanilla;
        }
        List<ItemStack> outputs = outputs(input, reagent);
        if (outputs.size() < 2) {
            return vanilla;
        }
        List<ResourceLocation> ids = ids(outputs);
        return outputs.get(ids.indexOf(choose(ids, stored(stand)))).copy();
    }

    /** The bottle the picker shows: the first with a conflict, else the first that brews. */
    private static final java.util.Map<TileEntityBrewingStand, ShownCache> SHOWN =
            new java.util.WeakHashMap<>();

    private static final class ShownCache {
        final ItemStack[] inputs;
        final List<ItemStack> outputs;

        ShownCache(ItemStack[] inputs, List<ItemStack> outputs) {
            this.inputs = inputs;
            this.outputs = outputs;
        }
    }

    private static List<ItemStack> cachedShown(TileEntityBrewingStand stand) {
        ItemStack[] inputs = new ItemStack[BOTTLES.length + 1];
        for (int i = 0; i < BOTTLES.length; i++) {
            inputs[i] = stand.getStackInSlot(BOTTLES[i]);
        }
        inputs[BOTTLES.length] = stand.getStackInSlot(3);
        ShownCache cache = SHOWN.get(stand);
        if (cache != null && sameStacks(cache.inputs, inputs)) {
            return cache.outputs;
        }
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = inputs[i].copy();
        }
        List<ItemStack> outputs = shown(stand);
        SHOWN.put(stand, new ShownCache(inputs, outputs));
        return outputs;
    }

    private static boolean sameStacks(ItemStack[] a, ItemStack[] b) {
        for (int i = 0; i < a.length; i++) {
            if (!ItemStack.areItemStacksEqual(a[i], b[i])) {
                return false;
            }
        }
        return true;
    }

    private static List<ItemStack> shown(TileEntityBrewingStand stand) {
        ItemStack reagent = stand.getStackInSlot(3);
        List<ItemStack> first = Collections.emptyList();
        for (int slot : BOTTLES) {
            List<ItemStack> outputs = outputs(stand.getStackInSlot(slot), reagent);
            if (outputs.size() > 1) {
                return outputs;
            }
            if (first.isEmpty()) {
                first = outputs;
            }
        }
        return first;
    }

    public static boolean isBrewingStand(EntityPlayerMP player, int windowId) {
        Container container = player.openContainer;
        return container instanceof ContainerBrewingStand && container.windowId == windowId
                && stand((ContainerBrewingStand) container) != null;
    }

    @Nullable
    private static TileEntityBrewingStand stand(ContainerBrewingStand container) {
        IInventory inventory = ((AccessorContainerBrewingStand) container).polymorph$getTileBrewingStand();
        return inventory instanceof TileEntityBrewingStand ? (TileEntityBrewingStand) inventory : null;
    }

    private static CraftingState state(Container container) {
        return ((CraftingStateHolder) container).polymorph$getCraftingState();
    }

    public static void openView(EntityPlayerMP player, int viewId) {
        if (viewId != 0 && player.openContainer.canInteractWith(player)) {
            state(player.openContainer).openView(viewId);
            update((ContainerBrewingStand) player.openContainer, player);
        }
    }

    public static boolean select(EntityPlayerMP player, int viewId, ResourceLocation id) {
        ContainerBrewingStand container = (ContainerBrewingStand) player.openContainer;
        TileEntityBrewingStand stand = stand(container);
        if (viewId == 0 || state(container).getViewId() != viewId || !container.canInteractWith(player)) {
            return false;
        }
        boolean valid = ids(shown(stand)).contains(id);
        if (valid) {
            ((FurnaceSelectionHolder) stand).polymorph$setFurnaceSelection(id);
            stand.markDirty();
        }
        update(container, player);
        return valid;
    }

    /** Called from detectAndSendChanges, so a changed bottle or reagent or another viewer's pick reaches this one. */
    public static void detectChanges(ContainerBrewingStand container, List<IContainerListener> listeners) {
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

    private static void update(ContainerBrewingStand container, EntityPlayerMP player) {
        TileEntityBrewingStand stand = stand(container);
        if (stand == null) {
            return;
        }
        List<ItemStack> outputs = cachedShown(stand);
        List<ResourceLocation> ids = ids(outputs);
        List<RecipeChoice> choices = new ArrayList<>(outputs.size());
        for (int i = 0; i < outputs.size(); i++) {
            choices.add(new RecipeChoice(ids.get(i), outputs.get(i)));
        }
        CraftingState state = state(container);
        ResourceLocation chosen = choose(ids, stored(stand));
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
