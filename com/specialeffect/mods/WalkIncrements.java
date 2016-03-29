package com.specialeffect.mods;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.DelayedOnLivingCallback;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.OnLivingCallback;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.messages.UseItemAtPositionMessage;
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

@Mod(modid = WalkIncrements.MODID, 
	 version = WalkIncrements.VERSION,
	 name = WalkIncrements.NAME,
	 guiFactory = "com.specialeffect.gui.GuiFactorySpecialEffect")
public class WalkIncrements extends BaseClassWithCallbacks
{
    public static final String MODID = "specialeffect.WalkIncrements";
    public static final String VERSION = "1.4";
    public static final String NAME = "WalkIncrements";
    public static Configuration mConfig;

    public static KeyBinding walkKeyBinding;
    public static KeyBinding walkDirectionKeyBinding;
    
    public static SimpleNetworkWrapper network;
    
    private KeyPressCounter keyCounterWalkDir = new KeyPressCounter();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {    
    	FMLCommonHandler.instance().bus().register(this);
    	mConfig = new Configuration(event.getSuggestedConfigurationFile());
    	this.syncConfig();
        
        ModUtils.setupModInfo(event, this.MODID, this.VERSION, this.NAME,
				"Add key bindings to walk fixed amount, for alternative inputs.");
		
        
        network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
        network.registerMessage(UseItemAtPositionMessage.Handler.class, UseItemAtPositionMessage.class, 0, Side.SERVER);

    }
    
    @SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if(eventArgs.modID.equals(this.MODID)) {
			syncConfig();
		}
	}
    
    private static void syncConfig() {
        mWalkDistance = (double)mConfig.getFloat("Walk distance", Configuration.CATEGORY_GENERAL, (float)mWalkDistance, 
        		0.1f, 10.0f, "Fixed distance to walk with single key press");
        if (mConfig.hasChanged()) {
        	mConfig.save();
        }
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
		// Subscribe to event buses
        FMLCommonHandler.instance().bus().register(this);
    	MinecraftForge.EVENT_BUS.register(this);
    	

    	// Register key bindings
    	
    	walkDirectionKeyBinding = new KeyBinding("Configure walking direction", Keyboard.KEY_O, "SpecialEffect");
        ClientRegistry.registerKeyBinding(walkDirectionKeyBinding);
        
    	walkKeyBinding = new KeyBinding("Step forward", Keyboard.KEY_P, "SpecialEffect");
        ClientRegistry.registerKeyBinding(walkKeyBinding);
        
    }
    
    @SubscribeEvent
    public void onLiving(LivingUpdateEvent event) {
    	if(event.entityLiving instanceof EntityPlayer) {
    		// Process any events which were queued by key events
    		this.processQueuedCallbacks(event);
    	}
    }
    
    private static double mWalkDistance = 1.0f;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        
        // Configure walk direction for next "walk" command.
        // a = north, aa = north-east, aaa = east, etc.
        if(walkDirectionKeyBinding.isPressed()) {
        	keyCounterWalkDir.increment();
        }

        // Walk: Move 100 units forward next onLiving tick.
        if(walkKeyBinding.isPressed()) {
        	final int i = keyCounterWalkDir.getCount();
        	keyCounterWalkDir.reset();
        	
            this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					float strafe = 0.0f;
					float forward = 0.0f;
					if (i > 0 && i < 4) {
						strafe = -1.0f;
					}
					else if (i > 4 && i < 8 ) {
						strafe = 1.0f;
					}
					int iShifted = (Math.floorMod(i-6, 8) + 8) % 8;
					if (iShifted > 0 && iShifted < 4) {
						forward = 1.0f;
					}
					else if (iShifted > 4 && iShifted < 8 ) {
						forward = -1.0f;
					}

					EntityPlayer player = (EntityPlayer)event.entityLiving;
	    			player.moveEntityWithHeading(strafe*(float)mWalkDistance, 
	    										 forward*(float)mWalkDistance);
				}
			}));
        }
    }
}
