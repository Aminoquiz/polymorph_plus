package com.shiver.polymorphlegacy.server;

import com.shiver.polymorphlegacy.crafting.FurnaceService;
import com.shiver.polymorphlegacy.priority.PriorityLoader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;

public final class PolymorphCommand extends CommandBase {
    @Override
    public String getName() {
        return "polymorph";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.polymorph.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
            @Nullable BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "conflicts", "reload")
                : Collections.<String>emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1 && "conflicts".equals(args[0])) {
            conflicts(server, sender);
        } else if (args.length == 1 && "reload".equals(args[0])) {
            List<String> namespaces = PriorityLoader.load(new File(server.getDataDirectory(), "config"));
            notifyCommandListener(sender, this, "commands.polymorph.reload.success",
                    namespaces.isEmpty() ? "-" : String.join(" > ", namespaces));
        } else {
            throw new WrongUsageException(getUsage(sender));
        }
    }

    private void conflicts(MinecraftServer server, ICommandSender sender) {
        notifyCommandListener(sender, this, "commands.polymorph.conflicts.starting");
        // Registries are frozen once a server runs, so the scan may leave the server thread; the
        // reply goes back through the scheduler.
        Thread thread = new Thread(() -> {
            List<String> output = new ArrayList<>();
            int count = scanCrafting(output) + scanSmelting(output);
            if (count > 0) {
                try {
                    File log = new File(server.getDataDirectory(), "logs/conflicts.log");
                    log.getParentFile().mkdirs();
                    Files.write(log.toPath(), output, StandardCharsets.UTF_8);
                } catch (Exception e) {
                    LogManager.getLogger("Polymorph+").error("Failed to write the recipe conflict report", e);
                }
            }
            server.addScheduledTask(() -> sender.sendMessage(
                    new TextComponentTranslation("commands.polymorph.conflicts.success", count)));
        }, "Polymorph+ conflict scan");
        thread.setDaemon(true);
        thread.start();
    }

    private static int scanCrafting(List<String> output) {
        List<IRecipe> recipes = new ArrayList<>();
        Set<ResourceLocation> skipped = new TreeSet<>(ResourceLocationOrder.INSTANCE);
        for (IRecipe recipe : ForgeRegistries.RECIPES) {
            if (recipe.isDynamic() || recipe.getIngredients().isEmpty()) {
                skipped.add(recipe.getRegistryName());
            } else {
                recipes.add(recipe);
            }
        }
        List<Set<String>> conflicts = new ArrayList<>();
        Set<IRecipe> processed = new HashSet<>();
        for (int i = 0; i < recipes.size(); i++) {
            IRecipe recipe = recipes.get(i);
            if (!processed.add(recipe)) {
                continue;
            }
            Set<String> group = new TreeSet<>();
            for (int j = i + 1; j < recipes.size(); j++) {
                IRecipe other = recipes.get(j);
                if (!processed.contains(other) && conflict(recipe, other)) {
                    group.add(recipe.getRegistryName().toString());
                    group.add(other.getRegistryName().toString());
                    processed.add(other);
                }
            }
            if (!group.isEmpty()) {
                conflicts.add(group);
            }
        }
        header(output, "minecraft:crafting", conflicts);
        if (!skipped.isEmpty()) {
            output.add("Skipped special recipes: ");
            for (ResourceLocation id : skipped) {
                output.add(String.valueOf(id));
            }
            output.add("");
        }
        return conflicts.size();
    }

    private static boolean conflict(IRecipe a, IRecipe b) {
        if (a instanceof IShapedRecipe && b instanceof IShapedRecipe) {
            IShapedRecipe sa = (IShapedRecipe) a;
            IShapedRecipe sb = (IShapedRecipe) b;
            if (sa.getRecipeWidth() != sb.getRecipeWidth() || sa.getRecipeHeight() != sb.getRecipeHeight()) {
                return false;
            }
        }
        List<Ingredient> ia = a.getIngredients();
        List<Ingredient> ib = b.getIngredients();
        if (ia.size() != ib.size()) {
            return false;
        }
        for (int i = 0; i < ia.size(); i++) {
            if (!overlap(ia.get(i), ib.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean overlap(Ingredient a, Ingredient b) {
        if (a == Ingredient.EMPTY || b == Ingredient.EMPTY) {
            return a == b;
        }
        for (ItemStack sa : a.getMatchingStacks()) {
            for (ItemStack sb : b.getMatchingStacks()) {
                if (sameInput(sa, sb)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean sameInput(ItemStack a, ItemStack b) {
        return a.getItem() == b.getItem() && (a.getMetadata() == b.getMetadata()
                || a.getMetadata() == 32767 || b.getMetadata() == 32767);
    }

    private static int scanSmelting(List<String> output) {
        List<Set<String>> conflicts = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ItemStack input : FurnaceRecipes.instance().getSmeltingList().keySet()) {
            if (input.isEmpty()) {
                continue;
            }
            List<ItemStack> outputs = FurnaceService.outputs(input);
            if (outputs.size() < 2) {
                continue;
            }
            Set<String> group = new TreeSet<>();
            for (ItemStack result : outputs) {
                group.add(FurnaceService.idOf(result).toString());
            }
            if (seen.add(input.getItem().getRegistryName() + "@" + input.getMetadata() + group)) {
                conflicts.add(group);
            }
        }
        header(output, "minecraft:smelting", conflicts);
        return conflicts.size();
    }

    private static void header(List<String> output, String type, List<Set<String>> conflicts) {
        output.add("===================================================================");
        output.add(type + " recipe conflicts (" + conflicts.size() + ")");
        output.add("===================================================================");
        output.add("");
        int count = 1;
        for (Set<String> conflict : conflicts) {
            StringJoiner joiner = new StringJoiner(", ");
            conflict.forEach(joiner::add);
            output.add(count++ + ": " + joiner);
            output.add("");
        }
    }

    private enum ResourceLocationOrder implements java.util.Comparator<ResourceLocation> {
        INSTANCE;

        @Override
        public int compare(ResourceLocation a, ResourceLocation b) {
            return String.valueOf(a).compareTo(String.valueOf(b));
        }
    }
}
