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
import net.minecraft.entity.Entity;
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


@Mod(modid = Dismount.MODID, version = Dismount.VERSION, name = Dismount.NAME)

public class Dismount extends BaseClassWithCallbacks {

	public static final String MODID = "specialeffect.dismount";
	public static final String VERSION = "0.1";
	public static final String NAME = "Dismount";

	private static KeyBinding mDismountKB;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);

		ModUtils.setupModInfo(event, this.MODID, this.VERSION, this.NAME,
				"Add custom key binding to dismount");

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);

		// Register key bindings
		mDismountKB = new KeyBinding("Dismount", Keyboard.KEY_C, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mDismountKB);
	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (event.entityLiving instanceof EntityPlayer) {
			this.processQueuedCallbacks(event);
		}
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if(mDismountKB.isPressed()) {
			System.out.println("dismount key");
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving()
        	{				
				@Override
				public void onLiving(LivingUpdateEvent event) {
					System.out.println("onliving");

					EntityPlayer player = (EntityPlayer)event.entityLiving;
					if (player.isRiding()) {
						System.out.println("is riding");

						Entity riddenEntity = player.ridingEntity;
						if (null != riddenEntity) {
							System.out.println("dismount");

							player.dismountEntity(riddenEntity);
							riddenEntity.riddenByEntity = null;
							player.ridingEntity = null;
							player.motionY += 0.5D;
						}
					}
				}		
			}));
		}
	}

}
