package com.specialeffect.eyegazemod;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.lwjgl.input.Keyboard;

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
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
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

@Mod(modid = SpecialEffectEyeGaze.MODID, 
	 version = SpecialEffectEyeGaze.VERSION,
	 name = SpecialEffectEyeGaze.NAME,
	 guiFactory = "com.specialeffect.eyegazemod.GuiFactorySpecialEffect")
public class SpecialEffectEyeGaze
{
    public static final String MODID = "specialeffect";
    public static final String VERSION = "1.4";
    public static final String NAME = "SpecialEffectEyeGaze";
    public static Configuration mConfig;

    public static KeyBinding walkKeyBinding;
    public static KeyBinding walkDirectionKeyBinding;
    public static KeyBinding autoJumpKeyBinding;
    public static KeyBinding autoPlaceKeyBinding;
    public static KeyBinding toggleFlyingKeyBinding;
    
    public static SimpleNetworkWrapper network;
    
    private KeyPressCounter keyCounterWalkDir = new KeyPressCounter();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {    
    	FMLCommonHandler.instance().bus().register(this);
    	mConfig = new Configuration(event.getSuggestedConfigurationFile());
    	mConfig.load();
        mWalkDistance = mConfig.get(Configuration.CATEGORY_GENERAL, "walkDistance", mWalkDistance).getDouble();
        mFlyHeight = mConfig.get(Configuration.CATEGORY_GENERAL, "flyHeight", mFlyHeight).getInt();
        mConfig.save();
        
        network = NetworkRegistry.INSTANCE.newSimpleChannel("MyChannel");
        network.registerMessage(UseItemAtPositionMessage.Handler.class, UseItemAtPositionMessage.class, 0, Side.SERVER);
        network.registerMessage(ChangeFlyingStateMessage.Handler.class, ChangeFlyingStateMessage.class, 1, Side.SERVER);

    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        mOnLivingQueue = new LinkedList<OnLivingCallback>();

		// Subscribe to event buses
        FMLCommonHandler.instance().bus().register(this);
    	MinecraftForge.EVENT_BUS.register(this);
    	

    	// Register key bindings
    	
    	walkDirectionKeyBinding = new KeyBinding("Configure walking direction", Keyboard.KEY_O, "SpecialEffect");
        ClientRegistry.registerKeyBinding(walkDirectionKeyBinding);
        
    	walkKeyBinding = new KeyBinding("Step forward", Keyboard.KEY_P, "SpecialEffect");
        ClientRegistry.registerKeyBinding(walkKeyBinding);
        
        autoJumpKeyBinding = new KeyBinding("Toggle Auto-Jump", Keyboard.KEY_J, "SpecialEffect");
        ClientRegistry.registerKeyBinding(autoJumpKeyBinding);
        
        autoPlaceKeyBinding = new KeyBinding("Auto-place block", Keyboard.KEY_L, "SpecialEffect");
        ClientRegistry.registerKeyBinding(autoPlaceKeyBinding);
        
        toggleFlyingKeyBinding = new KeyBinding("Toggle flying", Keyboard.KEY_F, "SpecialEffect");
        ClientRegistry.registerKeyBinding(toggleFlyingKeyBinding);
        
    }
    
    @SubscribeEvent
    public void onLiving(LivingUpdateEvent event) {
    	if(event.entityLiving instanceof EntityPlayer) {
    		EntityPlayer player = (EntityPlayer)event.entityLiving;
    		
    		if (mDoingAutoJump) {
    			player.stepHeight = 1.0f;
    		}
    		else {
    			player.stepHeight = 0.6f;
    		}
    		
            synchronized (mOnLivingQueue) {
            	Iterator<OnLivingCallback> it = mOnLivingQueue.iterator();
            	while (it.hasNext()) {
            		OnLivingCallback item = it.next();
            		item.onLiving(event);
            		if (item.hasCompleted()) {
            			it.remove();
            		}        		
            	}
            }
    	}
    }
    
    LinkedList<OnLivingCallback> mOnLivingQueue;
    
    private void queueOnLivingCallback(OnLivingCallback onLivingCallback) {
    	synchronized (mOnLivingQueue) {
        	mOnLivingQueue.add(onLivingCallback);
		}
    }

    private boolean mDoingAutoJump = false;
    private double mWalkDistance = 1.0f;
    private int mFlyHeight = 5;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        System.out.println("Key event");
        
        // Configure walk direction for next "walk" command.
        // a = north, aa = north-east, aaa = east, etc.
        if(walkDirectionKeyBinding.isPressed()) {
        	keyCounterWalkDir.increment();
        }

        // Walk: Move 100 units forward next onLiving tick.
        if(walkKeyBinding.isPressed()) {
        	final int i = keyCounterWalkDir.getCount();
        	keyCounterWalkDir.reset();
        	
            System.out.println("Walk key event");
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
		            System.out.println("Moving forward "+mWalkDistance);
	    			player.moveEntityWithHeading(strafe*(float)mWalkDistance, 
	    										 forward*(float)mWalkDistance);
				}
			}));
        }
        
        if(autoJumpKeyBinding.isPressed()) {
        	mDoingAutoJump = !mDoingAutoJump;
            System.out.println("Turning auto jump " + (mDoingAutoJump ? "ON" : "OFF"));
        }
        
        if (toggleFlyingKeyBinding.isPressed()) {
        	this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer)event.entityLiving;
	    			if (player.capabilities.allowFlying) {
	    				if (player.capabilities.isFlying) {
	    					// If flying, stop. State must be changed locally *and* on server
	    					player.capabilities.isFlying = false;
	    					SpecialEffectEyeGaze.network.sendToServer(
	    							new ChangeFlyingStateMessage(false, mFlyHeight));
	    				}
	    				else {
	    					// start flying, and fly upward.
	    					player.capabilities.isFlying = true;
                			player.motionY += mFlyHeight;
	    					SpecialEffectEyeGaze.network.sendToServer(
	    							new ChangeFlyingStateMessage(true, mFlyHeight));
	    				}
	    			}
				}
			}));
        }
        
        // Auto place is implemented as:
        // Next onLiving tick: look at floor, jump
        // After 15 ticks (when jump is high): place current item below player.
        if(autoPlaceKeyBinding.isPressed()) {
        	this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving()
        	{				
				@Override
				public void onLiving(LivingUpdateEvent event) {
		            // It's important to make sure we're approximately - but not exactly - centred
		            // on a block here, so that the block always ends up under us (if you do this
		            // with integer positions you often find your position alternating between 2 blocks)
		    		// Also look down, purely for effect.
					EntityPlayer player = (EntityPlayer)event.entityLiving;
					player.setPositionAndRotation(Math.floor(player.posX)+0.4, 
		    						   Math.floor(player.posY), 
		    						   Math.floor(player.posZ)+0.4,
		    						   player.rotationYaw,
		    						   85); // 90 = look straight down
		    		
		    		// Then jump
		    		player.jump();
				}		
			}));
        	this.queueOnLivingCallback(new DelayedOnLivingCallback(new IOnLiving()
        	{				
				@Override
				public void onLiving(LivingUpdateEvent event) {
 	                World world = Minecraft.getMinecraft().theWorld;
		    		EntityPlayer player = (EntityPlayer)event.entityLiving;
		    		BlockPos playerPos = player.getPosition();

	                ItemStack item = player.getHeldItem();
	                if (item != null) {
	                	BlockPos blockPos = new BlockPos(playerPos.getX(), 
	                									 playerPos.getY()-1, // y = up 
	                									 playerPos.getZ());
	                	
			            // Ask server to use item
			    		SpecialEffectEyeGaze.network.sendToServer(
			    				new UseItemAtPositionMessage(item, blockPos));

	                }
				}		
			},
        	10 )); // delayed by 5 ticks
        }
    }
}
