package com.specialeffect.mods;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class ChildMod {
	
    // Directly reference a log4j logger.	
    @SuppressWarnings("unused")
	public static final Logger LOGGER = LogManager.getLogger();
    protected SimpleChannel channel;

	public void setup(final FMLCommonSetupEvent event) {
		// can be overridden if mod has setup to do
	}
	
	protected void setupChannel(String modid, int protocolVersion) {
		String PROTOCOL_VERSION = Integer.toString(protocolVersion);

		channel = NetworkRegistry.newSimpleChannel(
                new ResourceLocation("specialeffect", modid)
                ,() -> PROTOCOL_VERSION
                , PROTOCOL_VERSION::equals
                , PROTOCOL_VERSION::equals);

	}
	
//	public void onKeyInput(KeyInputEvent event);    	
//	public void onLiving(LivingUpdateEvent event);
//	public void updateIcons();
//	public void syncConfig();
}
