package com.specialeffect.mods.moving;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.DelayedOnLivingCallback;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.OnLivingCallback;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.gui.StateOverlay;
import com.specialeffect.messages.UseItemAtPositionMessage;
import com.specialeffect.mods.utils.MouseHandler;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.KeyPressCounter;
import com.specialeffect.utils.ModUtils;
import com.sun.prism.Material;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import scala.collection.parallel.mutable.DoublingUnrolledBuffer;

@Mod(modid = TurnMouseOnOff.MODID, 
	 version = ModUtils.VERSION,
	 name = TurnMouseOnOff.NAME)
public class TurnMouseOnOff
extends BaseClassWithCallbacks
{
    public static final String MODID = "specialeffect.mouseOnOff";
    public static final String NAME = "mouseOnOff";

    public static KeyBinding mouseOnOffKeyBinding;
    
    public static Configuration mConfig;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {    
    	FMLCommonHandler.instance().bus().register(this);
        
        ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Turn mouse control on/off");
    	ModUtils.setAsParent(event, SpecialEffectMovements.MODID);

    } 
    
    // When we leave a GUI and enter the game, make sure mouse isn't captured
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
    	// This is an 'open' and 'close' event
    	
    	if (event.gui == null) { // close event
    		Mouse.setGrabbed(false);
    	}
    }
    

	@EventHandler
    public void init(FMLInitializationEvent event)
    {
		// Subscribe to event buses
        FMLCommonHandler.instance().bus().register(this);
    	MinecraftForge.EVENT_BUS.register(this);
    	
    	// Subscribe to parent's config changes
//    	SpecialEffectMovements.registerForConfigUpdates((ChildModWithConfig) this);
    	
    	// Register key bindings
        mouseOnOffKeyBinding = new KeyBinding("Toggle mouse usage", Keyboard.KEY_N, "SpecialEffect");
        ClientRegistry.registerKeyBinding(mouseOnOffKeyBinding);
        
        // Register an icon for the overlay
        mIconIndex = StateOverlay.registerTextureLeft("specialeffect:icons/jump.png");
        
        // Make sure icon is up to date (might be on by default).
        StateOverlay.setStateLeftIcon(mIconIndex, mDoOwnViewHandling);
        
    }
	
	private int mIconIndex;
    
    @SubscribeEvent
    public void onLiving(LivingUpdateEvent event) {
    	if(event.entityLiving instanceof EntityPlayer) {
    		EntityPlayer player = (EntityPlayer)event.entityLiving;
    		
    		// Process any events which were queued by key events
    		this.processQueuedCallbacks(event);
    	}
    }
    
    private boolean mDoOwnViewHandling = true;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        
        if(mouseOnOffKeyBinding.isPressed()) {
        	
        	mDoOwnViewHandling = !mDoOwnViewHandling;
    		StateOverlay.setStateLeftIcon(mIconIndex, mDoOwnViewHandling);

    		//MouseHandler.setVanillaMouseHandling(mDoOwnViewHandling);
    		MouseHandler.mDoOwnViewControl = mDoOwnViewHandling;
    		
	        this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving()
        	{				
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer)event.entityLiving;
			        player.addChatComponentMessage(new ChatComponentText(
			        		 "Mouse effect: " + (mDoOwnViewHandling ? "ON" : "OFF")));
				}		
			}));
        }
    }
}
