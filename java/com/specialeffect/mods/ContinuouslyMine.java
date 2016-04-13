package com.specialeffect.mods;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
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

@Mod(modid = ContinuouslyMine.MODID, version = ContinuouslyMine.VERSION, name = ContinuouslyMine.NAME)
public class ContinuouslyMine extends BaseClassWithCallbacks {
	public static final String MODID = "specialeffect.continuouslydestroy";
	public static final String VERSION = "0.1";
	public static final String NAME = "ContinuouslyDestroy";
	private int mIconIndex;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);
		
		ModUtils.setupModInfo(event, this.MODID, this.VERSION, this.NAME,
				"Add key binding to start/stop continuously attacking.");
		
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		
		// Register key bindings	
		mDestroyKB = new KeyBinding("Mine", Keyboard.KEY_M, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mDestroyKB);
		
		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureRight("specialeffect:icons/mine.png");
	}
	
	private static KeyBinding mDestroyKB;
	
	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (event.entityLiving instanceof EntityPlayer) {
			
			final KeyBinding attackBinding = 
					Minecraft.getMinecraft().gameSettings.keyBindAttack;
			
			// Set mouse in correct state - shouldn't attack unless there's an
			// accompanying mouse movement.
			// TODO: Encapsulate pending-mouse-event in separate class, for everyone
			// to query.
			if (mIsAttacking) {
				if (MoveWithGaze.mPendingMouseEvent || mMouseEventLastTick) {
					KeyBinding.setKeyBindState(attackBinding.getKeyCode(), true);
				}
				else {
					KeyBinding.setKeyBindState(attackBinding.getKeyCode(), false);
				}
			}
			
			// When attacking programmatically, the player doesn't swing unless
			// an attackable-block is in reach. We fix that here.
			if (attackBinding.isKeyDown()) {
				event.entityLiving.swingItem();
			}
			
			// Remember mouse status so we can have one tick of grace
			// (necessary if minecraft running faster than eye tracker).
			mMouseEventLastTick = MoveWithGaze.mPendingMouseEvent;
			
			this.processQueuedCallbacks(event);
		}
	}
	
	private boolean mIsAttacking = false;
	private boolean mMouseEventLastTick = false;
	
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if(mDestroyKB.isPressed()) {
			
			mIsAttacking = !mIsAttacking;
			StateOverlay.setStateRightIcon(mIconIndex, mIsAttacking);

			final KeyBinding attackBinding = 
					Minecraft.getMinecraft().gameSettings.keyBindAttack;
			
			if (mIsAttacking) {
				KeyBinding.setKeyBindState(attackBinding.getKeyCode(), true);
			}
			else {
				KeyBinding.setKeyBindState(attackBinding.getKeyCode(), false);
			}

			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving()
        	{				
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer)event.entityLiving;
			        player.addChatComponentMessage(new ChatComponentText(
			        		 "Mining: " + (mIsAttacking ? "ON" : "OFF")));
				}		
			}));
		}
	}
}
