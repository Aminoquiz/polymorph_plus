package com.illusivesoulworks.polymorph.common.components;

import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

// Delegates every IBlockEntityRecipeData method to the inner recipeData so that loader
// agnostic state (selected recipe, listener list) lives on a single object. Without these
// overrides, callers retrieving the component-as-IBlockEntityRecipeData hit this wrapper's
// own (unused) base-class state, while the real data sits on the wrapped instance.
public class WrappedRecipeDataComponent<M extends BlockEntity>
    extends AbstractBlockEntityRecipeDataComponent<M> {

  public final IBlockEntityRecipeData recipeData;

  public WrappedRecipeDataComponent(IBlockEntityRecipeData recipeData) {
    super((M) recipeData.getOwner());
    this.recipeData = recipeData;
  }

  @Override
  public void readData(ValueInput readView) {
  }

  @Override
  public void writeData(ValueOutput writeView) {
  }

  @Override
  protected NonNullList<ItemStack> getInput() {
    return NonNullList.create();
  }

  @Override
  public void tick() {
    this.recipeData.tick();
  }

  @Override
  public boolean isEmpty() {
    return this.recipeData.isEmpty();
  }

  @Override
  public <I extends RecipeInput, T extends Recipe<I>> RecipeHolder<T> getRecipe(
      RecipeType<T> type, I inventory, Level level, List<RecipeHolder<T>> recipes) {
    return this.recipeData.getRecipe(type, inventory, level, recipes);
  }

  @Override
  public void selectRecipe(@Nonnull RecipeHolder<?> recipe) {
    this.recipeData.selectRecipe(recipe);
  }

  @Override
  public RecipeHolder<?> getSelectedRecipe() {
    return this.recipeData.getSelectedRecipe();
  }

  @Override
  public void setSelectedRecipe(RecipeHolder<?> recipe) {
    this.recipeData.setSelectedRecipe(recipe);
  }

  @Override
  @Nonnull
  public SortedSet<IRecipePair> getRecipesList() {
    return this.recipeData.getRecipesList();
  }

  @Override
  public void setRecipesList(@Nonnull SortedSet<IRecipePair> recipesList) {
    this.recipeData.setRecipesList(recipesList);
  }

  @Override
  public Collection<ServerPlayer> getListeners() {
    return this.recipeData.getListeners();
  }

  @Override
  public void addListener(@Nonnull ServerPlayer player) {
    this.recipeData.addListener(player);
  }

  @Override
  public void removeListener(@Nonnull ServerPlayer player) {
    this.recipeData.removeListener(player);
  }

  @Override
  public void clearListeners() {
    this.recipeData.clearListeners();
  }

  @Override
  public void sendRecipesListToListeners() {
    this.recipeData.sendRecipesListToListeners();
  }
}
