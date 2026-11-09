package com.illusivesoulworks.polymorph.common.integration.hephaestus;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.inworld.Choices;
import com.illusivesoulworks.polymorph.api.inworld.IInWorldProvider;
import com.illusivesoulworks.polymorph.api.inworld.IndicatorSpot;
import com.illusivesoulworks.polymorph.api.inworld.Option;
import com.illusivesoulworks.polymorph.api.inworld.PolymorphInWorld;
import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Hephaestus casts the first recipe matching the mold and fluid. The table and basin offer every
 * match in world; the mixins move the pick to the front of the recipe list they scan.
 * Hephaestus types (FluidStack, ItemStackHandler) are NeoForge classes, out of reach from common,
 * hence the reflection.
 */
public class HephaestusModule extends AbstractCompatibilityModule {

  public static final ResourceLocation KEY =
      ResourceLocation.fromNamespaceAndPath(PolymorphApi.MOD_ID, "casting");

  private static final ResourceLocation FAUCET =
      ResourceLocation.fromNamespaceAndPath("hephaestus", "seared_faucet");

  private static final Set<GlobalPos> DIRTY = ConcurrentHashMap.newKeySet();
  private static final Map<String, Method> METHODS = new ConcurrentHashMap<>();
  private static final Map<Class<?>, Field> FIELDS = new ConcurrentHashMap<>();

  @Override
  protected void setup() {
    // Top face, front right corner: both blocks face the player who placed them, like Tinkers.
    register("seared_table", new Provider(true,
        new IndicatorSpot(0.85f, 0.85f, 0.1f, 1.0f, true)));
    register("seared_basin", new Provider(false,
        new IndicatorSpot(0.9f, 0.9f, 0.06f, 1.0f, true)));
  }

  private static void register(String name, IInWorldProvider provider) {
    Block block = BuiltInRegistries.BLOCK.get(
        ResourceLocation.fromNamespaceAndPath("hephaestus", name));

    if (block != Blocks.AIR) {
      PolymorphInWorld.register(block, provider);
    }
  }

  /**
   * Returns the recipe list with the block's pick first, so the original first-match loop takes
   * it while it still matches and falls back to its own order when it does not.
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public static List order(List recipes, @Nullable Level level, BlockPos pos) {

    if (level == null || level.isClientSide() || recipes.size() < 2) {
      return recipes;
    }
    ResourceLocation picked = PolymorphInWorld.chosen(level, pos, KEY);

    if (picked == null) {
      return recipes;
    }

    for (int i = 0; i < recipes.size(); i++) {

      if (recipes.get(i) instanceof RecipeHolder<?> holder && holder.id().equals(picked)) {

        if (i == 0) {
          return recipes;
        }
        List reordered = new ArrayList(recipes.size());
        reordered.add(holder);

        for (int j = 0; j < recipes.size(); j++) {

          if (j != i) {
            reordered.add(recipes.get(j));
          }
        }
        return reordered;
      }
    }
    return recipes;
  }

  /** Asks the casting block at {@code pos} to recompute its options on its next allowed tick. */
  public static void markDirty(@Nullable Level level, BlockPos pos) {

    if (level != null && !level.isClientSide()) {
      DIRTY.add(GlobalPos.of(level.dimension(), pos.immutable()));
    }
  }

  /** A tank's fluid changed: the casting blocks under the faucets around it may pour something else. */
  public static void markAroundSource(@Nullable Level level, BlockPos source) {

    for (Direction side : Direction.Plane.HORIZONTAL) {
      markDirty(level, source.relative(side).below());
    }
  }

  public static boolean takeDirty(Level level, BlockPos pos) {
    return DIRTY.remove(GlobalPos.of(level.dimension(), pos));
  }

  /** The block entity that owns one of Hephaestus' anonymous tanks. */
  @Nullable
  public static BlockEntity owner(Object tank) {
    try {
      Field field = FIELDS.computeIfAbsent(tank.getClass(), c -> {
        try {
          Field f = c.getDeclaredField("this$0");
          f.setAccessible(true);
          return f;
        } catch (NoSuchFieldException e) {
          throw new IllegalStateException(e);
        }
      });
      return field.get(tank) instanceof BlockEntity be ? be : null;
    } catch (ReflectiveOperationException | RuntimeException e) {
      return null;
    }
  }

  @Nullable
  private static Object call(Object target, String name, Object... args) {
    try {
      Method method = METHODS.computeIfAbsent(target.getClass().getName() + "#" + name, k -> {
        for (Method m : target.getClass().getMethods()) {

          if (m.getName().equals(name) && m.getParameterCount() == args.length) {
            return m;
          }
        }
        throw new IllegalStateException(k);
      });
      return method.invoke(target, args);
    } catch (ReflectiveOperationException | RuntimeException e) {
      return null;
    }
  }

  @Nullable
  private static Object field(Object target, String name) {
    try {
      return target.getClass().getField(name).get(target);
    } catch (ReflectiveOperationException e) {
      return null;
    }
  }

  /**
   * The fluid a faucet above would pour, read the way FaucetBlockEntity reads it (the block
   * behind its facing, simulated drain), so an empty table or basin offers the pick before the
   * pour starts and the fill redirect then sizes the pour to the pick.
   */
  @Nullable
  private static Object faucetFluid(ServerLevel level, BlockPos pos) {
    BlockPos faucetPos = pos.above();
    BlockState faucet = level.getBlockState(faucetPos);

    if (!FAUCET.equals(BuiltInRegistries.BLOCK.getKey(faucet.getBlock()))) {
      return null;
    }

    for (Property<?> property : faucet.getProperties()) {

      if (property.getName().equals("facing")
          && faucet.getValue(property) instanceof Direction facing) {
        Direction side = facing.getOpposite();
        Object handler = blockFluidHandler(level, faucetPos.relative(side), side.getOpposite());
        Object drained = handler != null ? simulateDrain(handler) : null;
        return drained != null && !Boolean.TRUE.equals(call(drained, "isEmpty")) ? drained : null;
      }
    }
    return null;
  }

  @Nullable
  private static Object blockFluidHandler(Level level, BlockPos pos, Direction side) {
    try {
      Object capability = Class.forName(
          "net.neoforged.neoforge.capabilities.Capabilities$FluidHandler").getField("BLOCK")
          .get(null);
      Method get = Level.class.getMethod("getCapability",
          Class.forName("net.neoforged.neoforge.capabilities.BlockCapability"), BlockPos.class,
          Object.class);
      return get.invoke(level, capability, pos, side);
    } catch (ReflectiveOperationException | RuntimeException e) {
      return null;
    }
  }

  @Nullable
  private static Object simulateDrain(Object handler) {
    try {
      Class<?> action = Class.forName(
          "net.neoforged.neoforge.fluids.capability.IFluidHandler$FluidAction");
      Method drain = Class.forName("net.neoforged.neoforge.fluids.capability.IFluidHandler")
          .getMethod("drain", int.class, action);
      return drain.invoke(handler, Integer.MAX_VALUE, action.getField("SIMULATE").get(null));
    } catch (ReflectiveOperationException | RuntimeException e) {
      return null;
    }
  }

  private record Provider(boolean table, IndicatorSpot spot) implements IInWorldProvider {

    @Nullable
    @Override
    public Choices choices(ServerLevel level, BlockPos pos, BlockState state) {
      BlockEntity be = level.getBlockEntity(pos);
      Object tank = be != null ? field(be, "tank") : null;
      Object fluidStack = tank != null ? call(tank, "getFluid") : null;

      if (tank == null) {
        return null;
      }

      if (fluidStack == null || Boolean.TRUE.equals(call(fluidStack, "isEmpty"))) {
        fluidStack = faucetFluid(level, pos);

        if (fluidStack == null) {
          return null;
        }
      }
      Object fluid = call(fluidStack, "getFluid");
      ItemStack mold = ItemStack.EMPTY;

      if (this.table) {
        Object inventory = field(be, "inventory");

        if (inventory != null && call(inventory, "getStackInSlot", 0) instanceof ItemStack stack) {
          mold = stack;
        }
      }
      RecipeType<?> type = BuiltInRegistries.RECIPE_TYPE.get(ResourceLocation.fromNamespaceAndPath(
          "hephaestus", this.table ? "casting_table" : "casting_basin"));

      if (type == null) {
        return null;
      }
      List<Option> options = new ArrayList<>();

      for (RecipeHolder<?> holder : allRecipes(level, type)) {
        Object recipe = holder.value();
        Object recipeFluid = call(recipe, this.table ? "fluid" : "input");

        if (recipeFluid == null || call(recipeFluid, "getFluid") != fluid) {
          continue;
        }

        if (this.table && call(recipe, "cast") instanceof Ingredient cast
            && !((cast.isEmpty() && mold.isEmpty()) || cast.test(mold))) {
          continue;
        }

        if (call(recipe, this.table ? "result" : "output") instanceof ItemStack result) {
          options.add(new Option(holder.id(), result.copy()));
        }
      }
      return options.size() < 2 ? null : new Choices(KEY, options, null);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List<RecipeHolder<?>> allRecipes(Level level, RecipeType type) {
      return (List<RecipeHolder<?>>) (List) level.getRecipeManager().getAllRecipesFor(type);
    }

    @Override
    public IndicatorSpot spot(BlockState state) {
      return this.spot;
    }
  }
}
