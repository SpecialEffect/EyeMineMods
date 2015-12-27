package com.specialeffect.eyegazemod;

import java.util.Iterator;
import java.util.Queue;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
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
import scala.actors.threadpool.LinkedBlockingQueue;

@Mod(modid = SpecialEffectAutoFly.MODID, 
version = SpecialEffectAutoFly.VERSION,
name = SpecialEffectAutoFly.NAME)
public class SpecialEffectAutoFly extends BaseClassWithCallbacks
{

	public static final String MODID = "specialeffect.autofly";
	public static final String VERSION = "0.1";
	public static final String NAME = "SpecialEffectAutoFly";

    public static Configuration mConfig;
	private static KeyBinding mToggleAutoFlyKB;
    private int mFlyHeight = 5;
    public static SimpleNetworkWrapper network;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {    
		FMLCommonHandler.instance().bus().register(this);    	
		
    	mConfig = new Configuration(event.getSuggestedConfigurationFile());
    	mConfig.load();
        mFlyHeight = mConfig.get(Configuration.CATEGORY_GENERAL, "flyHeight", mFlyHeight).getInt();
        mConfig.save();
        
        network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
        network.registerMessage(ChangeFlyingStateMessage.Handler.class, ChangeFlyingStateMessage.class, 1, Side.SERVER);

	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);    	

		// Register key bindings	
		mToggleAutoFlyKB = new KeyBinding("Toggle flying", Keyboard.KEY_F, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mToggleAutoFlyKB);

	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if(event.entityLiving instanceof EntityPlayer) {
			this.processQueuedCallbacks(event);
		}
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if(mToggleAutoFlyKB.isPressed()) {

			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
			        System.out.println("toggle fly living update");
			        
					EntityPlayer player = (EntityPlayer)event.entityLiving;
					if (player.capabilities.allowFlying) {
						if (player.capabilities.isFlying) {
							// If flying, stop. State must be changed locally *and* on server
							player.capabilities.isFlying = false;
							SpecialEffectAutoFly.network.sendToServer(
									new ChangeFlyingStateMessage(false, mFlyHeight));
						}
						else {
							// start flying, and fly upward.
							player.capabilities.isFlying = true;
							player.motionY += mFlyHeight;
							SpecialEffectAutoFly.network.sendToServer(
									new ChangeFlyingStateMessage(true, mFlyHeight));
						}
					}
				}
			}));
		}
	}

}
