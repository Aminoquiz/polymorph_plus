package com.shiver.polymorphlegacy.network;

import com.shiver.polymorphlegacy.PolymorphLegacy;
import com.shiver.polymorphlegacy.Tags;
import com.shiver.polymorphlegacy.crafting.CraftingService;
import com.shiver.polymorphlegacy.priority.PlayerPreferences;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class PolymorphNetwork {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);

    private PolymorphNetwork() {
    }

    public static void init() {
        CHANNEL.registerMessage((OpenViewPacket packet, net.minecraftforge.fml.common.network.simpleimpl.MessageContext ctx) -> {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> CraftingService.openView(player, packet.windowId, packet.viewId));
            return null;
        }, OpenViewPacket.class, 0, Side.SERVER);
        CHANNEL.registerMessage((SelectRecipePacket packet, net.minecraftforge.fml.common.network.simpleimpl.MessageContext ctx) -> {
            EntityPlayerMP player = ctx.getServerHandler().player;
            // 和 JEI 的填料包一样排入主线程；同一连接先发送的填料先执行。
            player.getServerWorld().addScheduledTask(() -> CraftingService.select(player, packet.windowId,
                    packet.viewId, packet.recipe));
            return null;
        }, SelectRecipePacket.class, 1, Side.SERVER);
        CHANNEL.registerMessage((RecipesPacket packet, net.minecraftforge.fml.common.network.simpleimpl.MessageContext ctx) -> {
            PolymorphLegacy.proxy.receiveRecipes(packet);
            return null;
        }, RecipesPacket.class, 2, Side.CLIENT);
        CHANNEL.registerMessage((PlayerPriorityPacket packet, net.minecraftforge.fml.common.network.simpleimpl.MessageContext ctx) -> {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> PlayerPreferences.upload(player,
                    packet.namespaces, packet.recipes));
            return null;
        }, PlayerPriorityPacket.class, 3, Side.SERVER);
    }
}
