package com.illusivesoulworks.polymorph.common.capability;

import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.common.brewing.BrewingCandidates;
import com.illusivesoulworks.polymorph.common.brewing.IBrewingResolver;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

/**
 * The stand resolves one mix per bottle, and while idle vanilla stops at the first bottle that
 * brews, so the picker is computed here once a tick over all three bottles: the first bottle
 * with a conflict, else the first one that brews. The pick is one id; it applies to every
 * bottle that has it among its candidates.
 */
public class BrewingStandRecipeData extends AbstractBlockEntityRecipeData<BrewingStandBlockEntity>
    implements IBrewingResolver {

  private static final int BOTTLE_SLOTS = 3;
  private static final int REAGENT_SLOT = 3;

  private ResourceLocation shownId;
  private SortedSet<IRecipePair> sentList;
  private ResourceLocation sentId;

  public BrewingStandRecipeData(BrewingStandBlockEntity owner) {
    super(owner);
  }

  @Nullable
  @Override
  public ItemStack resolveBrew(PotionBrewing brewing, ItemStack reagent, ItemStack bottle) {
    List<IRecipePair> candidates = BrewingCandidates.collect(brewing, reagent, bottle);
    IRecipePair selected = this.select(candidates);
    return candidates.size() > 1 && selected != null ? selected.getOutput().copy() : null;
  }

  @Nullable
  private IRecipePair select(List<IRecipePair> candidates) {
    IRecipePair sourceMatch = null;
    int sourceRank = Integer.MAX_VALUE;

    for (IRecipePair candidate : candidates) {
      ResourceLocation id = candidate.getResourceLocation();

      if (id.equals(this.getChosenRecipeId())) {
        return candidate;
      }
      int rank = this.sourceRank(id);

      if (rank < sourceRank) {
        sourceRank = rank;
        sourceMatch = candidate;
      }
    }
    return sourceMatch != null ? sourceMatch : candidates.isEmpty() ? null : candidates.get(0);
  }

  @Override
  public void tick() {

    if (this.getListeners().isEmpty()
        || !(this.getOwner().getLevel() instanceof ServerLevel level)) {
      return;
    }
    PotionBrewing brewing = level.potionBrewing();
    ItemStack reagent = this.getOwner().getItem(REAGENT_SLOT);
    List<IRecipePair> shown = List.of();

    for (int slot = 0; slot < BOTTLE_SLOTS; slot++) {
      List<IRecipePair> candidates =
          BrewingCandidates.collect(brewing, reagent, this.getOwner().getItem(slot));

      if ((shown.isEmpty() && !candidates.isEmpty())
          || (shown.size() < 2 && candidates.size() > 1)) {
        shown = candidates;
      }
    }
    IRecipePair selected = this.select(shown);
    this.shownId = selected != null ? selected.getResourceLocation() : null;
    SortedSet<IRecipePair> list = new TreeSet<>(shown);

    if (!list.equals(this.sentList) || !Objects.equals(this.shownId, this.sentId)) {
      this.sentList = list;
      this.sentId = this.shownId;
      this.setRecipesList(list);
      this.sendRecipesListToListeners();
    }
  }

  @Override
  public RecipeHolder<?> getSelectedRecipe() {
    return null;
  }

  @Override
  public ResourceLocation getSelectedRecipeId() {
    return this.shownId;
  }

  @Override
  public void chooseRecipe(ResourceLocation id) {
    super.chooseRecipe(id);
    this.shownId = id;
    this.getOwner().setChanged();
  }

  @Override
  public void addListener(ServerPlayer serverPlayer) {
    super.addListener(serverPlayer);
    this.sentList = null;
  }

  @Override
  public boolean isEmpty() {

    if (this.getOwner().getItem(REAGENT_SLOT).isEmpty()) {
      return true;
    }

    for (int slot = 0; slot < BOTTLE_SLOTS; slot++) {

      if (!this.getOwner().getItem(slot).isEmpty()) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected NonNullList<ItemStack> getInput() {
    NonNullList<ItemStack> input = NonNullList.withSize(BOTTLE_SLOTS + 1, ItemStack.EMPTY);

    for (int slot = 0; slot <= REAGENT_SLOT; slot++) {
      input.set(slot, this.getOwner().getItem(slot));
    }
    return input;
  }
}
