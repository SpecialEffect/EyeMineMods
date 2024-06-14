package com.specialeffect.eyemine.neoforge;

import com.inventory.config.InventoryConfig;
import com.specialeffect.eyemine.EyeMine;
import com.specialeffect.eyemine.EyeMineClient;
import com.specialeffect.eyemine.client.neoforge.ClientHandler;
import com.specialeffect.eyemine.config.EyeMineConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(EyeMine.MOD_ID)
public class EyeMineNeoForge {
	public EyeMineNeoForge(IEventBus eventBus, ModContainer container, Dist dist) {
		EyeMine.init();

		if (dist.isClient()) {
			// Config setup
			container.registerConfig(ModConfig.Type.CLIENT, EyeMineConfig.CLIENT_CONFIG, "eyemine-config.toml");
			eventBus.register(EyeMineConfig.class);
			container.registerConfig(ModConfig.Type.CLIENT, InventoryConfig.CLIENT_CONFIG, "eyemine-inventory-config.toml");
			eventBus.register(InventoryConfig.class);

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
