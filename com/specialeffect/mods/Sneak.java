package com.specialeffect.mods;

import java.util.Iterator;
import java.util.Queue;

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;


@Mod(modid = Sneak.MODID, version = Sneak.VERSION, name = Sneak.NAME)

public class Sneak extends BaseClassWithCallbacks {

	public static final String MODID = "specialeffect.sneaktoggle";
	public static final String VERSION = "0.1";
	public static final String NAME = "SneakToggle";

	private static KeyBinding mSneakKB;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);

		ModUtils.setupModInfo(event, this.MODID, this.VERSION, this.NAME,
				"Add key binding to start/stop sneaking");

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);

		// Register key bindings
		mSneakKB = new KeyBinding("Toggle sneak", Keyboard.KEY_Z, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mSneakKB);
	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (event.entityLiving instanceof EntityPlayer) {
			this.processQueuedCallbacks(event);
		}
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if(mSneakKB.isPressed()) {
			final KeyBinding sneakBinding = 
					Minecraft.getMinecraft().gameSettings.keyBindSneak;

			if (sneakBinding.isKeyDown()) {
				KeyBinding.setKeyBindState(sneakBinding.getKeyCode(), false);
				
			}
			else {
				KeyBinding.setKeyBindState(sneakBinding.getKeyCode(), true);
			}
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving()
        	{				
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer)event.entityLiving;
			        player.addChatComponentMessage(new ChatComponentText(
			        		 "Sneaking: " + (sneakBinding.isKeyDown() ? "ON" : "OFF")));
				}		
			}));
		}
	}

}
