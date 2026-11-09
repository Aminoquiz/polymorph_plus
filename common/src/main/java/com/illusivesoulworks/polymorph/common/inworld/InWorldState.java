package com.illusivesoulworks.polymorph.common.inworld;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.inworld.Option;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record InWorldState(ResourceLocation key, List<Option> options, int shown) {

  public static final ResourceLocation NO_KEY =
      ResourceLocation.fromNamespaceAndPath(PolymorphApi.MOD_ID, "none");
  public static final InWorldState EMPTY = new InWorldState(NO_KEY, List.of(), 0);

  public boolean isEmpty() {
    return options.isEmpty();
  }

  public ResourceLocation shownId() {
    return options.get(shown).id();
  }

  // ItemStack has no value equality, so records holding one cannot use equals().
  public boolean sameAs(InWorldState other) {

    if (shown != other.shown || !key.equals(other.key) ||
        options.size() != other.options.size()) {
      return false;
    }

    for (int i = 0; i < options.size(); i++) {
      Option a = options.get(i);
      Option b = other.options.get(i);

      if (!a.id().equals(b.id()) || !ItemStack.matches(a.icon(), b.icon())) {
        return false;
      }
    }
    return true;
  }
}
