package com.shiver.polymorphlegacy.priority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;

/**
 * Server-side view of one player's preferences. Favourites and namespaces live in the
 * client's config and are uploaded on join, so they are only held in memory here. The picks
 * a player made in earlier menus are saved with the player, under PlayerPersisted so they
 * survive death.
 */
public final class PlayerPreferences {
    public static final int MAX_NAMESPACES = 64;
    public static final int MAX_RECIPES = 256;
    public static final int MAX_REMEMBERED = 128;
    private static final String TAG = "polymorph_plus:picks";
    private static final Map<UUID, Uploaded> UPLOADED = new ConcurrentHashMap<>();

    private PlayerPreferences() {
    }

    public static void upload(EntityPlayer player, List<String> namespaces, List<String> recipes) {
        UPLOADED.put(player.getUniqueID(), new Uploaded(namespaces, recipes));
    }

    public static void forget(EntityPlayer player) {
        UPLOADED.remove(player.getUniqueID());
    }

    public static RecipeRanking ranking(EntityPlayer player) {
        Uploaded uploaded = UPLOADED.get(player.getUniqueID());
        List<String> remembered = remembered(player);
        return uploaded == null ? new RecipeRanking(Collections.<String>emptyList(), remembered,
                Collections.<String>emptyList())
                : new RecipeRanking(uploaded.recipes, remembered, uploaded.namespaces);
    }

    public static List<String> remembered(EntityPlayer player) {
        NBTTagCompound persisted = persisted(player);
        NBTTagList list = persisted.getTagList(TAG, 8);
        List<String> result = new ArrayList<>(list.tagCount());
        for (int i = 0; i < list.tagCount(); i++) {
            result.add(list.getStringTagAt(i));
        }
        return result;
    }

    public static void remember(EntityPlayer player, ResourceLocation id) {
        List<String> picks = remembered(player);
        String value = id.toString();
        if (!picks.isEmpty() && picks.get(0).equals(value)) {
            return;
        }
        picks.remove(value);
        picks.add(0, value);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < picks.size() && i < MAX_REMEMBERED; i++) {
            list.appendTag(new NBTTagString(picks.get(i)));
        }
        persisted(player).setTag(TAG, list);
    }

    private static NBTTagCompound persisted(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData();
        if (!data.hasKey(EntityPlayer.PERSISTED_NBT_TAG, 10)) {
            data.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
        }
        return data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
    }

    private static final class Uploaded {
        final List<String> namespaces;
        final List<String> recipes;

        Uploaded(List<String> namespaces, List<String> recipes) {
            this.namespaces = Collections.unmodifiableList(new ArrayList<>(namespaces));
            this.recipes = Collections.unmodifiableList(new ArrayList<>(recipes));
        }
    }
}
