package com.specialeffect.eyegazemod;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.lwjgl.input.Keyboard;

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

@Mod(modid = SpecialEffectContinuouslyDestroy.MODID, version = SpecialEffectContinuouslyDestroy.VERSION, name = SpecialEffectContinuouslyDestroy.NAME)
public class SpecialEffectContinuouslyDestroy {
	public static final String MODID = "specialeffect.continuouslydestroy";
	public static final String VERSION = "0.1";
	public static final String NAME = "SpecialEffectContinuouslyDestroy";

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
		mDestroyKB = new KeyBinding("Destroy", Keyboard.KEY_R, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mDestroyKB);
	}
	
	private static KeyBinding mDestroyKB;
	
	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (event.entityLiving instanceof EntityPlayer) {
			
			final KeyBinding attackBinding = 
					Minecraft.getMinecraft().gameSettings.keyBindAttack;
			
			// When attacking programmatically, the player doesn't swing unless
			// an attackable-block is in reach. We fix that here.
			if (attackBinding.isKeyDown()) {
				event.entityLiving.swingItem();
			}
		}
	}
	
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if(mDestroyKB.isPressed()) {
			final KeyBinding attackBinding = 
					Minecraft.getMinecraft().gameSettings.keyBindAttack;

			if (attackBinding.isKeyDown()) {
				KeyBinding.setKeyBindState(attackBinding.getKeyCode(), false);
			}
			else {
				KeyBinding.setKeyBindState(attackBinding.getKeyCode(), true);
			}
		}
	}
}
