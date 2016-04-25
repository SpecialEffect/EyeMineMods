package com.specialeffect.mods;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
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
import com.specialeffect.mods.mining.ContinuouslyMine;
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
import net.minecraft.entity.Entity;
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

@Mod(modid = ContinuouslyAttack.MODID, version = ModUtils.VERSION, name = ContinuouslyAttack.NAME)
public class ContinuouslyAttack extends BaseClassWithCallbacks {
	public static final String MODID = "specialeffect.continuouslyAttack";
	public static final String NAME = "ContinuouslyAttack";
    private Robot robot;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);
		
		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to start/stop continuously attacking.");
		
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		
		// Register key bindings	
		mAttackKB = new KeyBinding("Attack", Keyboard.KEY_R, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mAttackKB);
		
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureRight("specialeffect:icons/attack.png");
	}
	
	private static int mIconIndex;
	private static KeyBinding mAttackKB;
	
	public static void stop() {
		mIsAttacking = false;
		StateOverlay.setStateRightIcon(mIconIndex, false);
	}
	
	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (event.entityLiving instanceof EntityPlayer) {
			
			if (mIsAttacking) {
				// Get entity being looked at
				MovingObjectPosition mov = Minecraft.getMinecraft().objectMouseOver;
				Entity entity = mov.entityHit;
				if (null != entity) {
					// It feels like we should be able to just call 
					// player.attackTargetEntityWithCurrentItem but
					// it doesn't seem to work. 
					robot.mousePress(KeyEvent.BUTTON1_MASK);
					robot.mouseRelease(KeyEvent.BUTTON1_MASK);
				}
			}
			
			// When attacking programmatically, the player doesn't swing unless
			// an attackable-block is in reach. We fix that here, for better feedback.
			if (mIsAttacking) {
				event.entityLiving.swingItem();
			}
			
			this.processQueuedCallbacks(event);
		}
	}
	
	private static boolean mIsAttacking = false;
	
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		
		if(mAttackKB.isPressed()) {
			mIsAttacking = !mIsAttacking;
			StateOverlay.setStateRightIcon(mIconIndex, mIsAttacking);
			
			// Note: I'd like to use Minecraft.getMinecraft().gameSettings.keyBindAttack to
			// make this robust to key changes in the config. However, through minecraft key 
			// API you can only set key bind state, which misses the key press event you need
			// for attacking. So we use java.awt.Robot, which requires explicitly using a 
			// mouse event rather than a keyboard event. This is a shame.
			
//			if (mIsAttacking && !Mouse.isButtonDown(0)) {
//				robot.mousePress(KeyEvent.BUTTON1_MASK);
//			}
//			else if (Mouse.isButtonDown(0)) {
//				robot.mouseRelease(KeyEvent.BUTTON1_MASK);
//			}

			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving()
        	{				
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer)event.entityLiving;
			        player.addChatComponentMessage(new ChatComponentText(
			        		 "Attacking: " + (mIsAttacking ? "ON" : "OFF")));
				}		
			}));
			
			// Don't allow mining *and* attacking at same time
			ContinuouslyMine.stop();
		}
	}
}
