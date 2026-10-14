/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.common.network.client;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * Uploads the player's namespace preference list and favourite recipes. Resolution runs on the
 * server, so it needs both before the first conflict is resolved.
 */
public final class CPacketPlayerPriority {
  private final List<String> namespaces;
  private final List<String> recipes;

  public CPacketPlayerPriority(List<String> namespaces, List<String> recipes) {
    this.namespaces = namespaces;
    this.recipes = recipes;
  }

  public List<String> namespaces() {
    return this.namespaces;
  }

  public List<String> recipes() {
    return this.recipes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CPacketPlayerPriority)) {
      return false;
    }
    CPacketPlayerPriority other = (CPacketPlayerPriority) o;
    return java.util.Objects.equals(this.namespaces, other.namespaces) && java.util.Objects.equals(this.recipes, other.recipes);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(this.namespaces, this.recipes);
  }


  public static final int MAX_ENTRIES = 64;
  public static final int MAX_LENGTH = 64;
  public static final int MAX_RECIPES = 256;
  public static final int MAX_RECIPE_LENGTH = 256;

  public static void encode(CPacketPlayerPriority packet, FriendlyByteBuf buffer) {
    writeList(buffer, packet.namespaces, MAX_ENTRIES, MAX_LENGTH);
    writeList(buffer, packet.recipes, MAX_RECIPES, MAX_RECIPE_LENGTH);
  }

  public static CPacketPlayerPriority decode(FriendlyByteBuf buffer) {
    return new CPacketPlayerPriority(readList(buffer, MAX_ENTRIES, MAX_LENGTH),
        readList(buffer, MAX_RECIPES, MAX_RECIPE_LENGTH));
  }

  public static void handle(CPacketPlayerPriority packet, ServerPlayer player) {
    PolymorphApi.common().getRecipeData(player).ifPresent(recipeData -> {
      recipeData.setPriorityNamespaces(packet.namespaces);
      recipeData.setPriorityRecipes(packet.recipes);
    });
  }

  static void writeList(FriendlyByteBuf buffer, List<String> list, int maxEntries,
                        int maxLength) {
    int size = Math.min(list.size(), maxEntries);
    buffer.writeVarInt(size);

    for (int i = 0; i < size; i++) {
      buffer.writeUtf(list.get(i), maxLength);
    }
  }

  static List<String> readList(FriendlyByteBuf buffer, int maxEntries, int maxLength) {
    int size = buffer.readVarInt();

    if (size < 0 || size > maxEntries) {
      throw new IllegalArgumentException("Priority list too long: " + size);
    }
    List<String> list = new ArrayList<>(size);

    for (int i = 0; i < size; i++) {
      list.add(buffer.readUtf(maxLength));
    }
    return list;
  }
}
