package com.specialeffect.mods.moving;

import java.util.Iterator;
import java.util.Queue;

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.messages.DismountPlayerMessage;
import com.specialeffect.messages.UseDoorAtPositionMessage;
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
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;


@Mod(modid = Dismount.MODID, version = ModUtils.VERSION, name = Dismount.NAME)

public class Dismount extends BaseClassWithCallbacks {

	public static final String MODID = "specialeffect.dismount";
	public static final String NAME = "Dismount";

	private static KeyBinding mDismountKB;
	
    public static SimpleNetworkWrapper network;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add custom key binding to dismount");
    	ModUtils.setAsParent(event, SpecialEffectMovements.MODID);

		network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
        network.registerMessage(DismountPlayerMessage.Handler.class, 
        						DismountPlayerMessage.class, 0, Side.SERVER);

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
			// Dismount player locally
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer)event.entityLiving;

					if (player.isRiding()) {

						Entity riddenEntity = player.ridingEntity;
						if (null != riddenEntity) {
							player.dismountEntity(riddenEntity);
							riddenEntity.riddenByEntity = null;
							player.ridingEntity = null;
							player.motionY += 0.5D;
						}
					}
				}
			}));
			
			// Dismount player on server
			this.network.sendToServer(
					new DismountPlayerMessage());
		}
	}

}
