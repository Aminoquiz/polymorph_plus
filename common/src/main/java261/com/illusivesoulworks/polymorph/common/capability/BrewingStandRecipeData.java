package com.illusivesoulworks.polymorph.common.capability;

import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BrewingInput;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

/**
 * The stand resolves one recipe per bottle, so vanilla asks the recipe manager up to three
 * times a tick with different inputs, and stops at the first match while idle. Broadcasting
 * from those calls would flip the picker between bottles and miss a conflict on a later one.
 * Resolution still goes through {@link AbstractRecipeData#getRecipe}; what the picker shows is
 * computed here once a tick over all three bottles: the first bottle with a conflict, else the
 * first one that brews.
 */
public class BrewingStandRecipeData extends AbstractBlockEntityRecipeData<BrewingStandBlockEntity> {

  private static final int BOTTLE_SLOTS = 3;
  private static final int REAGENT_SLOT = 3;

  private SortedSet<IRecipePair> resolvedList = new TreeSet<>();
  private RecipeHolder<?> shownRecipe;
  private SortedSet<IRecipePair> sentList;
  private Identifier sentId;

  public BrewingStandRecipeData(BrewingStandBlockEntity owner) {
    super(owner);
  }

  @Override
  protected void updateRecipesList(SortedSet<IRecipePair> recipesList) {
    this.resolvedList = recipesList;
  }

  @Override
  public void tick() {

    if (this.getListeners().isEmpty()
        || !(this.getOwner().getLevel() instanceof ServerLevel level)) {
      return;
    }
    ItemStack reagent = this.getOwner().getItem(REAGENT_SLOT);
    SortedSet<IRecipePair> shown = new TreeSet<>();
    RecipeHolder<?> shownHolder = null;

    if (!reagent.isEmpty()) {

      for (int slot = 0; slot < BOTTLE_SLOTS; slot++) {
        ItemStack bottle = this.getOwner().getItem(slot);

        if (bottle.isEmpty()) {
          continue;
        }
        RecipeHolder<BrewingRecipe> resolved = this.getRecipe(RecipeType.BREWING,
            new BrewingInput(bottle, reagent), level, List.of());
        SortedSet<IRecipePair> candidates = this.resolvedList;

        if ((shown.isEmpty() && !candidates.isEmpty())
            || (shown.size() < 2 && candidates.size() > 1)) {
          shown = candidates;
          shownHolder = resolved;
        }
      }
    }
    this.shownRecipe = shownHolder;
    Identifier shownId = this.getSelectedRecipeId();

    if (!shown.equals(this.sentList) || !Objects.equals(shownId, this.sentId)) {
      this.sentList = new TreeSet<>(shown);
      this.sentId = shownId;
      this.setRecipesList(shown);
      this.sendRecipesListToListeners();
    }
  }

  @Override
  public RecipeHolder<?> getSelectedRecipe() {
    return this.shownRecipe;
  }

  @Override
  public Identifier getSelectedRecipeId() {
    return this.shownRecipe != null ? this.shownRecipe.id().identifier() : null;
  }

  @Override
  public void selectRecipe(@Nonnull RecipeHolder<?> recipe) {
    super.selectRecipe(recipe);
    this.getOwner().setChanged();
  }

  @Override
  public void addListener(@Nonnull ServerPlayer serverPlayer) {
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
