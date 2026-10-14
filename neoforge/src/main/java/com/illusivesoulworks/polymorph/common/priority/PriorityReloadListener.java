/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.common.priority;

import com.illusivesoulworks.polymorph.PolymorphConstants;
import com.illusivesoulworks.polymorph.api.PolymorphApi;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Loads {@code data/polymorph_plus/priority.json} out of the datapack stack.
 *
 * <p>A datapack rather than a config file so the list ships with the modpack, is
 * server-authoritative (no config sync to get wrong) and reloads with {@code /reload}.
 * Several packs may each ship one: the highest-priority pack's order wins, and lower packs
 * append whatever namespaces the winner did not already name. That way a modpack can layer
 * its own preferences on top of a mod's defaults without either side losing entries.
 */
public class PriorityReloadListener extends SimplePreparableReloadListener<List<String>> {

  public static final ResourceLocation ID =
      ResourceLocation.fromNamespaceAndPath(PolymorphApi.MOD_ID, "priority");
  private static final ResourceLocation PATH =
      ResourceLocation.fromNamespaceAndPath(PolymorphApi.MOD_ID, "priority.json");

  @Nonnull
  @Override
  protected List<String> prepare(@Nonnull ResourceManager resourceManager,
                                 @Nonnull ProfilerFiller profiler) {
    List<Resource> stack = resourceManager.getResourceStack(PATH);
    List<String> merged = new ArrayList<>();
    Set<String> seen = new HashSet<>();

    // getResourceStack is ordered lowest-priority first, so walk it backwards to let the
    // topmost datapack define the ordering.
    for (int i = stack.size() - 1; i >= 0; i--) {
      Resource resource = stack.get(i);

      try (BufferedReader reader = resource.openAsReader()) {
        String json = reader.lines().collect(Collectors.joining("\n"));

        for (String namespace : RecipePriority.parse(json)) {

          if (seen.add(namespace)) {
            merged.add(namespace);
          }
        }
      } catch (Exception e) {
        PolymorphConstants.LOG.error("Failed to read {} from pack {}", PATH,
            resource.sourcePackId(), e);
      }
    }
    return merged;
  }

  @Override
  protected void apply(@Nonnull List<String> namespaces, @Nonnull ResourceManager resourceManager,
                       @Nonnull ProfilerFiller profiler) {
    RecipePriority.setPackNamespaces(namespaces);

    if (!namespaces.isEmpty()) {
      PolymorphConstants.LOG.info("Recipe source priority: {}", String.join(" > ", namespaces));
    }
    // A /reload has to reach players who are already connected, otherwise their client keeps
    // ranking conflicts by the previous list.
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

    if (server != null) {

      for (ServerPlayer player : server.getPlayerList().getPlayers()) {
        PolymorphApi.getInstance().getNetwork().sendPackPriorityS2C(player, namespaces);
      }
    }
  }
}
