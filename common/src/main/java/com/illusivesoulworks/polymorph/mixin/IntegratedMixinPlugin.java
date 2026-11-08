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

  private static final String PREFIX = "com.illusivesoulworks.polymorph.mixin.integration.";
  private static final Map<String, Boolean> LOADED = new ConcurrentHashMap<>();

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

    if (mixinClassName.startsWith(PREFIX)) {
      String modId = mixinClassName.substring(PREFIX.length()).split("\\.")[0];
      return isIntegration(modId) && isLoaded(modId) && !hasOwnCompat(modId);
    }
    return true;
  }

  private static boolean isIntegration(String modId) {

    for (PolymorphIntegrations.Mod mod : PolymorphIntegrations.Mod.values()) {

      if (mod.getId().equals(modId)) {
        return true;
      }
    }
    return false;
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

  // Traveler's builds that ship their own Polymorph code already resolve through our API; the
  // built-in hook is only for builds without it (26.3 11.4.x).
  private static boolean hasOwnCompat(String modId) {
    return modId.equals("travelersbackpack") && IntegratedMixinPlugin.class.getClassLoader()
        .getResource("com/tiviacz/travelersbackpack/compat/polymorph/PolymorphCompat.class") != null;
  }

  @Override
  public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

  }

  @Override
  public List<String> getMixins() {
    return null;
  }

  @Override
  public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

  }

  @Override
  public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

  }

  @Override
  public ErrorAction onPrepareError(IMixinConfig config, Throwable th, IMixinInfo mixin,
                                    ErrorAction action) {
    return null;
  }

  @Override
  public ErrorAction onApplyError(String targetClassName, Throwable th, IMixinInfo mixin,
                                  ErrorAction action) {
    String pack = mixin.getClassName();

    if (pack.startsWith(PREFIX)) {
      String modId = pack.substring(PREFIX.length(), pack.lastIndexOf('.'));
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
