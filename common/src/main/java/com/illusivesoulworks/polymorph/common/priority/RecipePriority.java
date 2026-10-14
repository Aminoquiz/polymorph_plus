/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.common.priority;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Source-priority resolution for recipe conflicts.
 *
 * <p>Holds the ordered namespace list a pack author ships in
 * {@code data/polymorph_plus/priority.json}. When a conflict's candidates are covered by
 * the list, the highest-ranked one is picked automatically and the player never has to
 * click. Kept free of Minecraft types on purpose so the same source compiles on every
 * port and the parsing can be unit-checked in isolation; the datapack plumbing lives in
 * the loader modules.
 *
 * <p>Precedence, from strongest to weakest: an explicit per-conflict selection the player
 * already made, then this list, then vanilla first-match. The feature can therefore only
 * ever remove clicks, never override one the player made.
 */
public final class RecipePriority {

  // The integrated server and its client share this class, so each side keeps its own copy: a
  // client logout (Forge and NeoForge fire one while a singleplayer world is being created) must
  // not wipe the list the server just loaded from the datapacks.
  private static volatile Ranks server = Ranks.EMPTY;
  private static volatile Ranks client = Ranks.EMPTY;

  private RecipePriority() {
  }

  /** Server side: the list loaded from the datapacks. */
  public static void setPackNamespaces(List<String> namespaces) {
    server = Ranks.of(namespaces);
  }

  public static List<String> getPackNamespaces() {
    return server.namespaces;
  }

  public static boolean hasPackNamespaces() {
    return !server.namespaces.isEmpty();
  }

  /** Client side: the list the server pushed on join or after a reload. */
  public static void setClientPackNamespaces(List<String> namespaces) {
    client = Ranks.of(namespaces);
  }

  public static List<String> getClientPackNamespaces() {
    return client.namespaces;
  }

  /**
   * Rank of a namespace in the server's pack list. Lower wins; {@link Integer#MAX_VALUE} means
   * the namespace is not listed and carries no preference at all.
   */
  public static int packRank(String namespace) {
    return server.rank(namespace);
  }

  public static int packRank(String namespace, boolean clientSide) {
    return (clientSide ? client : server).rank(namespace);
  }

  /**
   * Accepts either {@code {"namespaces": ["a", "b"]}} or a bare {@code ["a", "b"]} array.
   * Unparseable input yields an empty list rather than throwing: a malformed priority file
   * must not take a world down, it just means no automatic resolution.
   */
  public static List<String> parse(String json) {
    List<String> result = new ArrayList<>();

    try {
      JsonElement root = JsonParser.parseString(json);
      JsonArray array = null;

      if (root.isJsonArray()) {
        array = root.getAsJsonArray();
      } else if (root.isJsonObject()) {
        JsonObject obj = root.getAsJsonObject();

        if (obj.has("namespaces") && obj.get("namespaces").isJsonArray()) {
          array = obj.getAsJsonArray("namespaces");
        }
      }

      if (array != null) {

        for (JsonElement element : array) {

          if (element.isJsonPrimitive()) {
            String namespace = element.getAsString().trim();

            if (!namespace.isEmpty() && !result.contains(namespace)) {
              result.add(namespace);
            }
          }
        }
      }
    } catch (Exception e) {
      return List.of();
    }
    return result;
  }

  private static final class Ranks {

    static final Ranks EMPTY = new Ranks(List.of(), Map.of());

    final List<String> namespaces;
    final Map<String, Integer> ranks;

    private Ranks(List<String> namespaces, Map<String, Integer> ranks) {
      this.namespaces = namespaces;
      this.ranks = ranks;
    }

    static Ranks of(List<String> namespaces) {
      List<String> copy = List.copyOf(namespaces);
      Map<String, Integer> ranks = new HashMap<>();

      for (int i = 0; i < copy.size(); i++) {
        ranks.putIfAbsent(copy.get(i), i);
      }
      return new Ranks(copy, Map.copyOf(ranks));
    }

    int rank(String namespace) {
      Integer rank = this.ranks.get(namespace);
      return rank == null ? Integer.MAX_VALUE : rank;
    }
  }
}
