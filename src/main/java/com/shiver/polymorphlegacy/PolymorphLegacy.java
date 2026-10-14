package com.shiver.polymorphlegacy;

import com.shiver.polymorphlegacy.network.PolymorphNetwork;
import com.shiver.polymorphlegacy.priority.PlayerPreferences;
import com.shiver.polymorphlegacy.priority.PriorityLoader;
import com.shiver.polymorphlegacy.server.PolymorphCommand;
import java.io.File;
import java.util.Map;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        acceptedMinecraftVersions = "[1.12.2]", dependencies = "required-after:mixinbooter")
public final class PolymorphLegacy {
    @SidedProxy(clientSide = "com.shiver.polymorphlegacy.client.ClientProxy",
            serverSide = "com.shiver.polymorphlegacy.CommonProxy")
    public static CommonProxy proxy;

    private File configDir;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        configDir = event.getModConfigurationDirectory();
        PolymorphNetwork.init();
        MinecraftForge.EVENT_BUS.register(this);
        proxy.init();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        PriorityLoader.load(configDir);
        event.registerServerCommand(new PolymorphCommand());
    }

    @SubscribeEvent
    public void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerPreferences.forget(event.player);
    }

    /** A peer without the mod is fine (no selector, server-side defaults still apply); a peer with it must match. */
    @NetworkCheckHandler
    public boolean checkRemote(Map<String, String> mods, Side remote) {
        String version = mods.get(Tags.MOD_ID);
        return version == null || version.equals(Tags.VERSION);
    }
}
