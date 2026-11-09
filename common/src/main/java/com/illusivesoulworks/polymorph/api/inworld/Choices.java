package com.illusivesoulworks.polymorph.api.inworld;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

/**
 * The options a block can pick between.
 *
 * @param key     scopes the pick, so one block can keep a separate pick per key
 * @param current the option the block uses when nothing was picked, or null for the first one
 */
public record Choices(ResourceLocation key, List<Option> options,
                      @Nullable ResourceLocation current) {
}
