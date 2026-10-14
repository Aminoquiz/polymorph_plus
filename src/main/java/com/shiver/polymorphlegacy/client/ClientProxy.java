package com.shiver.polymorphlegacy.client;

import com.shiver.polymorphlegacy.CommonProxy;
import com.shiver.polymorphlegacy.network.RecipesPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

public final class ClientProxy extends CommonProxy {
    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.register(new ClientRecipes());
    }

    @Override
    public void receiveRecipes(RecipesPacket packet) {
        Minecraft.getMinecraft().addScheduledTask(() -> ClientRecipes.receive(packet));
    }
}
