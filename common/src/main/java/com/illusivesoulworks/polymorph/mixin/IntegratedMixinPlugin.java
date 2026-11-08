/*
 * Copyright (C) 2020-2022 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Polymorph is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Polymorph.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.illusivesoulworks.polymorph.mixin;

import com.illusivesoulworks.polymorph.PolymorphConstants;
import com.illusivesoulworks.polymorph.common.integration.PolymorphIntegrations;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

@SuppressWarnings("unused")
public class IntegratedMixinPlugin implements IMixinConfigPlugin, IMixinErrorHandler {

  private static final Map<String, String> CLASS_TO_MOD = new HashMap<>();
  private static final Map<String, Boolean> LOADED = new ConcurrentHashMap<>();
  private static Set<String> disabled;

  static {
    CLASS_TO_MOD.put("dev.shadowsoffire.fastbench.", PolymorphIntegrations.Mod.FASTWORKBENCH.getId());
    CLASS_TO_MOD.put("dev.shadowsoffire.fastsuite.", PolymorphIntegrations.Mod.FASTSUITE.getId());
    CLASS_TO_MOD.put("net.p3pp3rf1y.sophisticatedcore.",
        PolymorphIntegrations.Mod.SOPHISTICATEDCORE.getId());
    CLASS_TO_MOD.put("com.refinedmods.refinedstorage.", PolymorphIntegrations.Mod.REFINEDSTORAGE.getId());
    CLASS_TO_MOD.put("com.tom.storagemod.", PolymorphIntegrations.Mod.TOMS_STORAGE.getId());
    CLASS_TO_MOD.put("com.lothrazar.storagenetwork.", PolymorphIntegrations.Mod.STORAGENETWORK.getId());
    CLASS_TO_MOD.put("com.lothrazar.cyclic.", PolymorphIntegrations.Mod.CYCLIC.getId());
    CLASS_TO_MOD.put("tfar.fastbench.", PolymorphIntegrations.Mod.QUICKBENCH.getId());
    CLASS_TO_MOD.put("net.blay09.mods.craftingcraft.", PolymorphIntegrations.Mod.CRAFTINGCRAFT.getId());
    CLASS_TO_MOD.put("me.shedaniel.rei.", PolymorphIntegrations.Mod.REI.getId());
    CLASS_TO_MOD.put("com.lothrazar.invcrafting.", PolymorphIntegrations.Mod.INVCRAFTING.getId());
    CLASS_TO_MOD.put("ironfurnaces.", PolymorphIntegrations.Mod.IRONFURNACES.getId());
    CLASS_TO_MOD.put("wily.betterfurnaces.", PolymorphIntegrations.Mod.BETTERFURNACES.getId());
    CLASS_TO_MOD.put("dev.emi.emi.", PolymorphIntegrations.Mod.EMI.getId());
  }

  @Override
  public void onLoad(String mixinPackage) {
    Mixins.registerErrorHandlerClass("com.illusivesoulworks.polymorph.mixin.IntegratedMixinPlugin");
  }

  @Override
  public String getRefMapperConfig() {
    return null;
  }

  // Runs while mixin configs are still being prepared: loading any game class here (Services,
  // PolymorphIntegrations) loads it before other mods' mixins are registered (issue #14).
  @Override
  public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {

    for (String prefix : CLASS_TO_MOD.keySet()) {

      if (targetClassName.startsWith(prefix)) {
        String modId = CLASS_TO_MOD.get(prefix);
        return isEnabled(modId) && isLoaded(modId);
      }
    }
    return true;
  }

  // Asks the loader's mod list by reflection: loader classes are safe to load here.
  private static boolean isLoaded(String modId) {
    return LOADED.computeIfAbsent(modId, IntegratedMixinPlugin::findMod);
  }

  private static boolean findMod(String modId) {
    String[] lists = {"net.fabricmc.loader.api.FabricLoader", "getInstance", "isModLoaded",
        "net.neoforged.fml.loading.LoadingModList", "get", "getModFileById",
        "net.minecraftforge.fml.loading.LoadingModList", "get", "getModFileById"};

    for (int i = 0; i < lists.length; i += 3) {

      try {
        Class<?> clazz = Class.forName(lists[i]);
        Object list = clazz.getMethod(lists[i + 1]).invoke(null);

        if (list != null) {
          Object found = clazz.getMethod(lists[i + 2], String.class).invoke(list, modId);
          return found != null && !Boolean.FALSE.equals(found);
        }
      } catch (ReflectiveOperationException | LinkageError e) {
        // not this loader
      }
    }
    return false;
  }

  // Reads polymorph-integrations.toml as PolymorphIntegrations.loadConfig writes it, without
  // loading that class; a missing file leaves everything on.
  private static synchronized boolean isEnabled(String modId) {

    if (disabled == null) {
      disabled = new HashSet<>();
      Path file = configDir().resolve("polymorph-integrations.toml");

      if (Files.isRegularFile(file)) {

        try {

          for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] entry = line.split("#", 2)[0].split("=", 2);

            if (entry.length == 2 && entry[1].trim().equals("false")) {
              disabled.add(entry[0].trim().replace("\"", ""));
            }
          }
        } catch (IOException e) {
          // unreadable file: keep everything on
        }
      }
    }
    return !disabled.contains(modId);
  }

  private static Path configDir() {

    try {
      Class<?> fabric = Class.forName("net.fabricmc.loader.api.FabricLoader");
      Object loader = fabric.getMethod("getInstance").invoke(null);
      return (Path) fabric.getMethod("getConfigDir").invoke(loader);
    } catch (ReflectiveOperationException | LinkageError e) {
      // not Fabric
    }

    try {
      Class<?> paths = Class.forName("net.minecraftforge.fml.loading.FMLPaths");
      return (Path) paths.getMethod("get").invoke(paths.getField("CONFIGDIR").get(null));
    } catch (ReflectiveOperationException | LinkageError e) {
      return Paths.get("config");
    }
  }

  @Override
  public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

  }

  @Override
  public List<String> getMixins() {
    return null;
  }

  @Override
  public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName,
                       IMixinInfo mixinInfo) {

  }

  @Override
  public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName,
                        IMixinInfo mixinInfo) {

  }

  @Override
  public ErrorAction onPrepareError(IMixinConfig config, Throwable th, IMixinInfo mixin,
                                    ErrorAction action) {
    return null;
  }

  @Override
  public ErrorAction onApplyError(String targetClassName, Throwable th, IMixinInfo mixin,
                                  ErrorAction action) {

    if (mixin.getConfig().getMixinPackage()
        .startsWith("com.illusivesoulworks.polymorph.mixin.integration")) {
      String modId = "{MOD NOT FOUND - THIS SHOULD NOT HAPPEN}";

      for (Map.Entry<String, String> entry : CLASS_TO_MOD.entrySet()) {
        String id = entry.getValue();

        if (targetClassName.startsWith(entry.getKey())) {
          modId = id;
          break;
        }
      }
      PolymorphIntegrations.disable(modId);
      PolymorphConstants.LOG.error("Polymorph encountered an error while transforming: {}",
          targetClassName);
      PolymorphConstants.LOG.error("The integration module for {} will be disabled.", modId);
      PolymorphConstants.LOG.error(
          "Please report this bug to Polymorph only, do not report this to {}.", modId);
      return ErrorAction.WARN;
    }
    return null;
  }
}
