package com.specialeffect.eyemine.neoforge;

import com.inventory.config.InventoryConfig;
import com.specialeffect.eyemine.EyeMine;
import com.specialeffect.eyemine.EyeMineClient;
import com.specialeffect.eyemine.client.neoforge.ClientHandler;
import com.specialeffect.eyemine.config.EyeMineConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(EyeMine.MOD_ID)
public class EyeMineNeoForge {
	public EyeMineNeoForge(IEventBus eventBus) {
		// Config setup
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, EyeMineConfig.CLIENT_CONFIG, "eyemine-config.toml");
		eventBus.register(EyeMineConfig.class);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, InventoryConfig.CLIENT_CONFIG, "eyemine-inventory-config.toml");
		eventBus.register(InventoryConfig.class);

		EyeMine.init();

		//Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () ->
				new IExtensionPoint.DisplayTest(() -> "Everyone is valid",
						(remoteVersionString, networkBool) -> networkBool));

		if (FMLEnvironment.dist.isClient()) {
			// Hook up config gui
//            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> ClientHandler::openSettings);

			// Register this setup method *after* children have registered theirs
			// (this way the children will be fully set up before any config gets loaded)
			eventBus.addListener(ClientHandler::setup);

			NeoForge.EVENT_BUS.addListener(ClientHandler::onOutlineRender);
			EyeMineClient.init();
		}
	}
}
