package com.specialeffect.eyemine;

import com.inventory.config.InventoryConfig;
import com.specialeffect.eyemine.client.ClientHandler;
import me.shedaniel.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;

@Mod(EyeMine.MOD_ID)
public class EyeMineForge {
    public EyeMineForge() {
        // Submit our event bus to let architectury register our content on the right time
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(EyeMine.MOD_ID, eventBus);
        EyeMine.init();

        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, ()-> Pair.of(
                () -> "Everyone is valid", // if I'm actually on the server, this string is sent, but I'm a client only mod, so it won't be
                (removeVersion, networkBool) -> networkBool // I accept anything from the server, by returning true if it's asking about the server
        ));

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            // Hook up config gui
//            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> ClientHandler::openSettings);

            // Config setup
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, EyeMineConfig.CLIENT_CONFIG, "eyemine-config.toml");
            eventBus.register(EyeMineConfig.class);
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, InventoryConfig.CLIENT_CONFIG, "eyemine-inventory-config.toml");
            eventBus.register(InventoryConfig.class);

            // Register this setup method *after* children have registered theirs
            // (this way the children will be fully set up before any config gets loaded)
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientHandler::setup);

            MinecraftForge.EVENT_BUS.addListener(ClientHandler::onOutlineRender);
        });
    }
}
