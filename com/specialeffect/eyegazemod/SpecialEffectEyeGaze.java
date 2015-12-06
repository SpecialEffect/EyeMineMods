package com.specialeffect.eyegazemod;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.lwjgl.input.Keyboard;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
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
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import scala.collection.parallel.mutable.DoublingUnrolledBuffer;

@Mod(modid = SpecialEffectEyeGaze.MODID, 
	 version = SpecialEffectEyeGaze.VERSION,
	 name = SpecialEffectEyeGaze.NAME,
	 guiFactory = "com.specialeffect.eyegazemod.GuiFactorySpecialEffect")
public class SpecialEffectEyeGaze
{
    public static final String MODID = "specialeffect";
    public static final String VERSION = "1.1";
    public static final String NAME = "SpecialEffectEyeGaze";
    public static Configuration mConfig;

    public static KeyBinding walkKeyBinding;
    public static KeyBinding autoJumpKeyBinding;
    public static KeyBinding autoPlaceKeyBinding;
    public static KeyBinding toggleFlyingKeyBinding;
    
    public static SimpleNetworkWrapper network;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {    
    	FMLCommonHandler.instance().bus().register(this);
    	mConfig = new Configuration(event.getSuggestedConfigurationFile());
    	mConfig.load();
        mWalkDistance = mConfig.get(Configuration.CATEGORY_GENERAL, "walkDistance", mWalkDistance).getDouble();
        mFlyHeight = mConfig.get(Configuration.CATEGORY_GENERAL, "flyHeight", mFlyHeight).getInt();
        mConfig.save();
        
        network = NetworkRegistry.INSTANCE.newSimpleChannel("MyChannel");
        network.registerMessage(MyMessage.Handler.class, MyMessage.class, 0, Side.SERVER);

    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        mOnLivingQueue = new LinkedList<OnLivingCallback>();

		// Subscribe to event buses
        FMLCommonHandler.instance().bus().register(this);
    	MinecraftForge.EVENT_BUS.register(this);
    	

    	// Register key bindings
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

        // Walk: Move 100 units forward next onLiving tick.
        if(walkKeyBinding.isPressed()) {
            System.out.println("Walk key event");
            this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer)event.entityLiving;
		            System.out.println("Moving forward "+mWalkDistance);
	    			player.moveEntityWithHeading(0, (float)mWalkDistance);
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
	    				// If flying, stop
	    				if (player.capabilities.isFlying) {
	    		            System.out.println("Turning off flying!");
	    					player.capabilities.isFlying = false;
	    				}
	    				else {
	    		            System.out.println("Turning on flying!");
	    					player.capabilities.isFlying = true;
		    				player.motionY += 5;
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
	                	 
			            // Use item, increment stack size if successful (in creative mode)
	                	if (item.onItemUse(player, world, blockPos, EnumFacing.DOWN, 0, 0, 0) &&
	                	    player.capabilities.isCreativeMode) {
	                		item.stackSize += 1;
	                	}
			    		SpecialEffectEyeGaze.network.sendToServer(new MyMessage("foobar", item, blockPos));
	                }
				}		
			},
        	15 )); // delayed by 5 ticks
        }
    }
}
