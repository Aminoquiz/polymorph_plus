package com.illusivesoulworks.polymorph.common.brewing;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.common.util.RecipePair;
import com.illusivesoulworks.polymorph.mixin.core.AccessorPotionBrewing;
import com.illusivesoulworks.polymorph.mixin.core.AccessorPotionMix;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Below 26.3 brewing mixes are not recipes: no holder, no id, and {@code PotionBrewing.mix}
 * returns the first match. This lists every match for a bottle and reagent, in the order
 * {@code mix} tries them, each with an id synthesized from its output.
 */
public final class BrewingCandidates {

  public interface ExtraSource {
    void collect(PotionBrewing brewing, ItemStack bottle, ItemStack reagent, List<ItemStack> out);
  }

  // Set by a loader whose PotionBrewing.mix consults more than the vanilla lists first.
  public static ExtraSource extraSource = (brewing, bottle, reagent, out) -> {
  };

  private static final ThreadLocal<BlockEntity> CURRENT_STAND = new ThreadLocal<>();

  private BrewingCandidates() {
  }

  public static void setCurrentStand(@Nullable BlockEntity blockEntity) {
    CURRENT_STAND.remove();

    if (blockEntity != null) {
      CURRENT_STAND.set(blockEntity);
    }
  }

  @Nullable
  public static ItemStack resolveForCurrentStand(PotionBrewing brewing, ItemStack reagent,
                                                 ItemStack bottle) {
    BlockEntity stand = CURRENT_STAND.get();

    if (stand == null || stand.isRemoved() || bottle.isEmpty()) {
      return null;
    }

    if (PolymorphApi.getInstance().getBlockEntityRecipeData(stand)
        instanceof IBrewingResolver resolver) {
      return resolver.resolveBrew(brewing, reagent, bottle);
    }
    return null;
  }

  public static List<IRecipePair> collect(PotionBrewing brewing, ItemStack reagent,
                                          ItemStack bottle) {

    if (bottle.isEmpty() || reagent.isEmpty()) {
      return List.of();
    }
    List<ItemStack> outputs = new ArrayList<>();
    extraSource.collect(brewing, bottle, reagent, outputs);
    Optional<Holder<Potion>> potion =
        bottle.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();

    if (potion.isPresent()) {
      AccessorPotionBrewing lists = (AccessorPotionBrewing) brewing;

      for (Object entry : lists.getContainerMixes()) {
        AccessorPotionMix mix = (AccessorPotionMix) entry;

        if (bottle.is(castItem(mix.getFrom())) && mix.getIngredient().test(reagent)) {
          outputs.add(PotionContents.createItemStack(castItem(mix.getTo()).value(), potion.get()));
        }
      }

      for (Object entry : lists.getPotionMixes()) {
        AccessorPotionMix mix = (AccessorPotionMix) entry;

        if (castPotion(mix.getFrom()).is(potion.get()) && mix.getIngredient().test(reagent)) {
          outputs.add(PotionContents.createItemStack(bottle.getItem(), castPotion(mix.getTo())));
        }
      }
    }
    Map<ResourceLocation, IRecipePair> byId = new LinkedHashMap<>();

    for (ItemStack output : outputs) {

      if (!output.isEmpty()) {
        byId.putIfAbsent(idFor(output), new RecipePair(idFor(output), output));
      }
    }
    return new ArrayList<>(byId.values());
  }

  /**
   * Stable across reloads and between server and client: the namespace of the potion (or item)
   * produced, so pack and namespace priorities still apply, and the output in the path.
   */
  public static ResourceLocation idFor(ItemStack output) {
    ResourceLocation item = BuiltInRegistries.ITEM.getKey(output.getItem());
    Optional<Holder<Potion>> potion =
        output.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();

    if (potion.isPresent()) {
      ResourceLocation potionId = BuiltInRegistries.POTION.getKey(potion.get().value());

      if (potionId != null) {
        return ResourceLocation.fromNamespaceAndPath(potionId.getNamespace(),
            "brewing/" + item.getNamespace() + "/" + item.getPath() + "/" + potionId.getPath());
      }
    }
    return ResourceLocation.fromNamespaceAndPath(item.getNamespace(),
        "brewing/" + item.getNamespace() + "/" + item.getPath());
  }

  @SuppressWarnings("unchecked")
  private static Holder<Item> castItem(Holder<?> holder) {
    return (Holder<Item>) holder;
  }

  @SuppressWarnings("unchecked")
  private static Holder<Potion> castPotion(Holder<?> holder) {
    return (Holder<Potion>) holder;
  }
}
