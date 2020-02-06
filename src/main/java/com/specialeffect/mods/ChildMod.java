package com.specialeffect.mods;

import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public interface ChildMod {
	public void setup(final FMLCommonSetupEvent event);
//	public void onKeyInput(KeyInputEvent event);    	
//	public void onLiving(LivingUpdateEvent event);
//	public void updateIcons();
//	public void syncConfig();
}
