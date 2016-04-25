package com.specialeffect.mods.utils;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.OnLivingCallback;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.messages.MovePlayerMessage;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

@Mod(modid = MouseHandler.MODID, 
	 version = ModUtils.VERSION,
	 name = MouseHandler.NAME)
// TODO: gui factory for config
public class MouseHandler 
extends BaseClassWithCallbacks 
implements ChildModWithConfig
{
	public static final String MODID = "specialeffect.MouseHandler";
    public static final String NAME = "MouseHandler";

    public static Configuration mConfig;
    
    private static KeyBinding mSensivityUpKB;
    private static KeyBinding mSensivityDownKB;
    
    private static boolean mPendingMouseEvent = false;
    public static boolean mLastEventWithinBounds = false; 
	private static boolean mDoVanilla = true; // whether to allow normal mouse processing	
    
    public static float mUserMouseSensitivity = -1.0f; // internal cache of user's preference.
	private static int mIgnoreEventCount = 0;
    private static float mDeadBorder = 0.1f;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {    
    	FMLCommonHandler.instance().bus().register(this);  
    	
    	ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to start/stop walking continuously, with direction controlled by mouse/eyetracker");
    	ModUtils.setAsParent(event, SpecialEffectUtils.MODID);

    	// Set up config
    	mConfig = new Configuration(event.getSuggestedConfigurationFile());
    	this.syncConfig();
    	
    	// Check the initial sensitivity setting.
    	this.querySensitivity();
    }
    
	public void syncConfig() {
		mDeadBorder = SpecialEffectUtils.mDeadBorder;
	}
	
	public static void setVanillaMouseHandling(boolean doVanilla) {
		mDoVanilla = doVanilla;
	}
	
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
		// Subscribe to event buses
        FMLCommonHandler.instance().bus().register(this);
    	MinecraftForge.EVENT_BUS.register(this);    	
    	
    	// Subscribe to config changes from parent
    	SpecialEffectUtils.registerForConfigUpdates((ChildModWithConfig) this);

    	// Register key bindings	
        mSensivityUpKB = new KeyBinding("Turn mouse sensitivity UP", Keyboard.KEY_ADD, "SpecialEffect");
        ClientRegistry.registerKeyBinding(mSensivityUpKB);
        
        mSensivityDownKB = new KeyBinding("Turn mouse sensitivity DOWN", Keyboard.KEY_SUBTRACT, "SpecialEffect");
        ClientRegistry.registerKeyBinding(mSensivityDownKB);
        
    }
    
    @SubscribeEvent(priority=EventPriority.LOWEST)  // important we get this *after* other mods
    public void onLiving(LivingUpdateEvent event) {
    	if(event.entityLiving instanceof EntityPlayer) {
    		EntityPlayer player = (EntityPlayer)event.entityLiving;    		
			mPendingMouseEvent = false;
			
			this.processQueuedCallbacks(event);
    	}
    }
    
    
    @SubscribeEvent(priority=EventPriority.HIGHEST)  // important we get this *before* other mods
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (mSensivityUpKB.isPressed()) {
        	this.resetSensitivity();
        	Minecraft.getMinecraft().gameSettings.mouseSensitivity *= 1.1;
        	this.querySensitivity();
        	this.queueChatMessage("Sensitivity: " + toPercent(Minecraft.getMinecraft().gameSettings.mouseSensitivity));
        }
        else if (mSensivityDownKB.isPressed()) {
        	this.resetSensitivity();
        	Minecraft.getMinecraft().gameSettings.mouseSensitivity /= 1.1;
        	this.querySensitivity();
        	this.queueChatMessage("Sensitivity: " + toPercent(Minecraft.getMinecraft().gameSettings.mouseSensitivity));
        }
    }
    
    public static void setIgnoreNextEvent() {
    	mIgnoreEventCount++;
    }
    
    public static boolean hasPendingEvent() {
    	return mPendingMouseEvent;
    }
    
    public boolean lastEventWithinBounds() {
    	return mPendingMouseEvent && mLastEventWithinBounds;
    }
    
    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
    	
    	// Cancel any mouse events within a certain border. This avoids mouse movements outside the window (e.g. from
    	// eye gaze system) from having an impact on view direction.
    	float r = 2*mDeadBorder;
    	
    	float x_abs = Math.abs((float)Mouse.getEventDX()); // distance from centre
    	float y_abs = Math.abs((float)Mouse.getEventDY());
    	float w_half = (float)Minecraft.getMinecraft().displayWidth/2;
    	float h_half = (float)Minecraft.getMinecraft().displayHeight/2;
    	  	
    	if (mIgnoreEventCount > 0 ||
    		x_abs > w_half*(1-r) ||
    		y_abs > h_half*(1-r)) {    		
    		// In v1.8, it would be sufficient to query getDX and DY to consume the deltas.
    		// ... but this doesn't work in 1.8.8, so we hack it by setting the mouse sensitivity down low.
    		// See: http://www.minecraftforge.net/forum/index.php?topic=29216.10;wap2
    		this.zeroSensitivity();
    	}
    	// turn off anyway, if vanilla mouse movements turned off, but record pending event.
    	else if (!mDoVanilla) {
    		this.zeroSensitivity();
        	mPendingMouseEvent = true;
    	}
    	else {
    		this.resetSensitivity();
        	mPendingMouseEvent = true;
    	}
    	
    	mIgnoreEventCount = Math.max(mIgnoreEventCount-1, 0);
    }
    
    // When we leave a GUI and enter the game, we record the user's
    // chosen sensitivity (and hack around with it in-game).
    // When we leave the game, we reset the sensitivty to how we found it.
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
    	// This is an 'open' and 'close' event
    	
    	if (event.gui != null) { // open event
    		this.resetSensitivity();
    	}
    	else {
    		this.querySensitivity();
    	}
    }
    
    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
    	this.resetSensitivity();
    }
    
    private void resetSensitivity() {
    	if (mUserMouseSensitivity > 0) {
			Minecraft.getMinecraft().gameSettings.mouseSensitivity = mUserMouseSensitivity;
		}
    }
    
    private void zeroSensitivity() {
    	// See  http://www.minecraftforge.net/forum/index.php?topic=29216.10;wap2 for
    	// magic number.
		Minecraft.getMinecraft().gameSettings.mouseSensitivity = -1F/3F; 
    }
    private void querySensitivity() {
		mUserMouseSensitivity = Minecraft.getMinecraft().gameSettings.mouseSensitivity;
    }
    
    String toPercent(float input) {
    	DecimalFormat myFormatter = new DecimalFormat("#0.0");
        return myFormatter.format(input*100) + "%";
    }
}


