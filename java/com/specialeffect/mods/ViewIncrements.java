package com.specialeffect.mods;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
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
import net.minecraft.util.Vec3;
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

@Mod(modid = ViewIncrements.MODID, 
	 version = ModUtils.VERSION,
	 name = ViewIncrements.NAME,
	 guiFactory = "com.specialeffect.gui.GuiFactoryViewIncrements")
public class ViewIncrements extends BaseClassWithCallbacks
{
    public static final String MODID = "specialeffect.ViewIncrements";
    public static final String NAME = "ViewIncrements";
    public static Configuration mConfig;

    public static KeyBinding moveViewKB;
	public static KeyBinding viewDirectionKeyBinding;
    
    public static SimpleNetworkWrapper network;
    
    private KeyPressCounter keyCounterViewDir = new KeyPressCounter();

    private Robot robot;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {    
    	FMLCommonHandler.instance().bus().register(this);
    	mConfig = new Configuration(event.getSuggestedConfigurationFile());
    	this.syncConfig();
        
        ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key bindings to change view by fixed amount, for alternative inputs.");
        network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
        network.registerMessage(UseItemAtPositionMessage.Handler.class, UseItemAtPositionMessage.class, 0, Side.SERVER);
        
        try {
			robot = new Robot();
		} catch (AWTException e) {
			System.out.println("Can't create robot for " + this.NAME);
			e.printStackTrace();
		}
    }
    
    @SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if(eventArgs.modID.equals(this.MODID)) {
			syncConfig();
		}
	}
    
    private static void syncConfig() {
    	mViewDeltaRelative = mConfig.getInt("View delta (degrees)", 
    			Configuration.CATEGORY_GENERAL, 
    			mViewDeltaRelative, 
        		1, 45, 
        		"Fixed rotation to change view with single key press");
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
    	viewDirectionKeyBinding = new KeyBinding("Configure direction for view delta", Keyboard.KEY_U, "SpecialEffect");
        ClientRegistry.registerKeyBinding(viewDirectionKeyBinding);
        
    	moveViewKB = new KeyBinding("Apply view delta", Keyboard.KEY_I, "SpecialEffect");
        ClientRegistry.registerKeyBinding(moveViewKB);
        
    }
    
    @SubscribeEvent
    public void onLiving(LivingUpdateEvent event) {
    	if(event.entityLiving instanceof EntityPlayer) {
    		// Process any events which were queued by key events
    		this.processQueuedCallbacks(event);
    	}
    }
    
    private static int mViewDeltaRelative = 2;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        
        // Configure walk direction for next "walk" command.
        // a = north, aa = north-east, aaa = east, etc.
        if(viewDirectionKeyBinding.isPressed()) {
        	keyCounterViewDir.increment();
        }

        // Walk: Move 100 units forward next onLiving tick.
        if(moveViewKB.isPressed()) {
        	final int i = keyCounterViewDir.getCount();
        	keyCounterViewDir.reset();
        	
            this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					
					Point p = ModUtils.getCompassPoint(i);
			    	int dYaw = (int)p.getX() * mViewDeltaRelative;
			    	int dPitch = - (int)p.getY() * mViewDeltaRelative; // pitch is opposite to what you expect

			    	EntityPlayer player = (EntityPlayer)event.entityLiving;

			    	Vec3 pos = player.getPositionVector();
	    			float yaw = player.rotationYaw;
	    			float pitch = player.rotationPitch; 
	    			
	    			player.setPositionAndRotation(pos.xCoord, pos.yCoord, pos.zCoord, 
	    					(float)(yaw+dYaw), (float)(pitch+dPitch));
				}
			}));
        }
    }
}
