package com.specialeffect.mods;

import java.util.Iterator;
import java.util.Queue;

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
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


@Mod(modid = Swim.MODID, version = Swim.VERSION, name = Swim.NAME)

public class Swim extends BaseClassWithCallbacks {

	public static final String MODID = "specialeffect.swimtoggle";
	public static final String VERSION = "0.1";
	public static final String NAME = "SwimToggle";

	private static KeyBinding mSwimKB;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);

		ModUtils.setupModInfo(event, this.MODID, this.VERSION, this.NAME,
				"Add key binding to start/stop swimming (= jumping)");

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);

		// Register key bindings
		mSwimKB = new KeyBinding("Toggle swimming", Keyboard.KEY_V, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mSwimKB);
	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (event.entityLiving instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)event.entityLiving;
			
			if (mIsSwimming) {
				final KeyBinding swimBinding = 
						Minecraft.getMinecraft().gameSettings.keyBindJump;

				// Only hold down the swim button when actually in water.
				if (player.isInWater() && 						
						!swimBinding.isKeyDown() ) {
					KeyBinding.setKeyBindState(swimBinding.getKeyCode(), true);
				}
				// Switch off when on land
				else if (!player.isInWater() &&
						swimBinding.isKeyDown() ) {
					// If water is underneath, don't stop swimming yet (probably in the
					// process of swimming).
					BlockPos playerPos = player.getPosition();
					BlockPos blockBelow = new BlockPos(playerPos.getX(),
							playerPos.getY()-1, playerPos.getZ());
			    	World world = Minecraft.getMinecraft().theWorld;
					Block block = world.getBlockState(blockBelow).getBlock();
					if (block != null && block instanceof BlockLiquid) {
						// do nothing
					}
					else {
						KeyBinding.setKeyBindState(swimBinding.getKeyCode(), false);
					}
				}
			}
			this.processQueuedCallbacks(event);
			
		}
	}
	
	private boolean mIsSwimming = false;

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if(mSwimKB.isPressed()) {
			final KeyBinding swimBinding = 
					Minecraft.getMinecraft().gameSettings.keyBindJump;
			
			mIsSwimming = !mIsSwimming;

			if (!mIsSwimming) {
				KeyBinding.setKeyBindState(swimBinding.getKeyCode(), false);
			}
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving()
        	{				
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer)event.entityLiving;
			        player.addChatComponentMessage(new ChatComponentText(
			        		 "Swimming: " + (mIsSwimming? "ON" : "OFF")));
				}		
			}));
		}
	}

}
