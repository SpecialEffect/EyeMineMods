package com.specialeffect.eyemine.forge;

import com.inventory.config.InventoryConfig;
import com.specialeffect.eyemine.EyeMine;
import com.specialeffect.eyemine.EyeMineClient;
import com.specialeffect.eyemine.client.forge.ClientHandler;
import com.specialeffect.eyemine.config.EyeMineConfig;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.IExtensionPoint.DisplayTest;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(EyeMine.MOD_ID)
public class EyeMineForge {
	public EyeMineForge() {

		// Submit our event bus to let architectury register our content on the right time
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

		// Config setup
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, EyeMineConfig.CLIENT_CONFIG, "eyemine-config.toml");
		eventBus.register(EyeMineConfig.class);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, InventoryConfig.CLIENT_CONFIG, "eyemine-inventory-config.toml");
		eventBus.register(InventoryConfig.class);

		EventBuses.registerModEventBus(EyeMine.MOD_ID, eventBus);
		EyeMine.init();

		//Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
		ModLoadingContext.get().registerExtensionPoint(DisplayTest.class, () ->
				new IExtensionPoint.DisplayTest(() -> "Everyone is valid",
						(remoteVersionString, networkBool) -> networkBool));

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			// Hook up config gui
//            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> ClientHandler::openSettings);

			// Register this setup method *after* children have registered theirs
			// (this way the children will be fully set up before any config gets loaded)
			FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientHandler::setup);

			MinecraftForge.EVENT_BUS.addListener(ClientHandler::onOutlineRender);
			EyeMineClient.init();
		});
	}
}
