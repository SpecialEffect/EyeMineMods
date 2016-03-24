package com.specialeffect.mods;

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
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

@Mod(modid = MoveWithGaze.MODID, 
	 version = MoveWithGaze.VERSION,
	 name = MoveWithGaze.NAME,
	 guiFactory = "com.specialeffect.gui.GuiFactoryWalkWithGaze")
public class MoveWithGaze extends BaseClassWithCallbacks {
	public static final String MODID = "specialeffect.movewithgaze";
    public static final String VERSION = "0.1";
    public static final String NAME = "MoveWithGaze";

    private static KeyBinding mToggleAutoWalkKB;
    public static Configuration mConfig;
    private static int mQueueLength = 50;
    private static boolean mMoveWhenMouseStationary = false;
    private boolean mPendingMouseEvent = true;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {    
    	FMLCommonHandler.instance().bus().register(this);  
    	
    	ModUtils.setupModInfo(event, this.MODID, this.VERSION, this.NAME,
				"Add key binding to start/stop walking continuously, with direction controlled by mouse/eyetracker");
    	
    	// Set up config
    	mConfig = new Configuration(event.getSuggestedConfigurationFile());
    	this.syncConfig();
    }
    
    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
    	if (mUserMouseSensitivity > 0) {
			Minecraft.getMinecraft().gameSettings.mouseSensitivity = mUserMouseSensitivity;
		}
    }
    
    @SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if(eventArgs.modID.equals(this.MODID)) {
			syncConfig();
		}
	}
	
	public static void syncConfig() {
        mQueueLength = mConfig.getInt("Smoothness filter", Configuration.CATEGORY_GENERAL, mQueueLength, 
				1, 200, "How many ticks to take into account for slowing down while looking around. (smaller number = faster)");
        mMoveWhenMouseStationary = mConfig.getBoolean("Move when mouse stationary", Configuration.CATEGORY_GENERAL, 
        									mMoveWhenMouseStationary, "Continue walking forward when the mouse is stationary. Recommended to be turned off for eye gaze control.");
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
    	mToggleAutoWalkKB = new KeyBinding("Toggle auto-walk", Keyboard.KEY_H, "SpecialEffect");
        ClientRegistry.registerKeyBinding(mToggleAutoWalkKB);
        
        mPrevLookDirs = new LinkedBlockingQueue<Vec3>();
    }
    
    @SubscribeEvent
    public void onLiving(LivingUpdateEvent event) {
    	if(event.entityLiving instanceof EntityPlayer) {
    		EntityPlayer player = (EntityPlayer)event.entityLiving;    		
    		
       		// Add current look dir to queue
    		mPrevLookDirs.add(player.getLookVec());
       		if (mPrevLookDirs.size() > mQueueLength) {
       			mPrevLookDirs.remove();
       		}
       		
       		// Explanation of strategy:
       		// - when turning a corner, we want to slow down to make it a bit more manageable.
       		// - if it takes time to turn the auto-walk function off (e.g. using an eye gaze with dwell click) then
       		//   you don't want to continue walking. In this case you can opt to not walk on any ticks where the mouse
       		//   hasn't moved at all. This is mainly applicable to gaze input.
            if (mDoingAutoWalk && (mMoveWhenMouseStationary || mPendingMouseEvent) ) {
            	// Scale forward-distance by the normal congruency of the last X view-dirs.
            	// We use normal congruency over several ticks to:
            	// - smooth out noise, and
            	// - smooth out effect over time (e.g. keep slow-ish for a couple of ticks after movement).
            	double scalarLength = mPrevLookDirs.size();
            	Vec3 vectorSum = new Vec3(0, 0, 0);
            	// TODO: Sums can be done incrementally rather than looping over everything each time.
            	Iterator<Vec3> iter = mPrevLookDirs.iterator();
            	while (iter.hasNext()) {
                    vectorSum = vectorSum.add(iter.next());
            	}
            	double vectorLength = vectorSum.lengthVector();            	
            	double normalCongruency = vectorLength/scalarLength;
            	
            	// If in auto-walk mode, walk forward an amount scaled by the view change (less if looking around)
            	double thresh = 0.9; // below this, no movement
            	double distance = Math.max(0, /*mWalkDistance**/(normalCongruency - thresh)/(1.0-thresh));
//                System.out.println("distance = "+distance);
            	//distance = Math.max(distance, 0.0);
            	player.moveEntityWithHeading(0, (float)distance);
            }
            
            mPendingMouseEvent = false;
			this.processQueuedCallbacks(event);

    	}
    }
    
    private boolean mDoingAutoWalk = false;
    private double mWalkDistance = 1.0f;
    private Queue<Vec3> mPrevLookDirs;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        
        if(mToggleAutoWalkKB.isPressed()) {
        	mDoingAutoWalk = !mDoingAutoWalk;
        	this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving()
        	{				
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer)event.entityLiving;
			        player.addChatComponentMessage(new ChatComponentText(
			        		 "Auto walk: " + (mDoingAutoWalk ? "ON" : "OFF")));
				}		
			}));
        }
    }
    
    float mUserMouseSensitivity = -1.0f;
    
    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {

    	// Cancel any mouse events within a certain border. This avoids mouse movements outside the window (e.g. from
    	// eye gaze system) from having an impact on view direction.
    	float r = 0.05f;
    	float x_abs = Math.abs((float)Mouse.getEventDX()); // distance from centre
    	float y_abs = Math.abs((float)Mouse.getEventDY());
    	float w_half = (float)Minecraft.getMinecraft().displayWidth/2;
    	float h_half = (float)Minecraft.getMinecraft().displayHeight/2;
    	
    	if (x_abs > w_half*(1-r) ||
    		y_abs > h_half*(1-r)) {    		
    		// In v1.8, it would be sufficient to query getDX and DY to consume the deltas.
    		// ... but this doesn't work in 1.8.8, so we hack it by setting the mouse sensitivity down low.
    		// See: http://www.minecraftforge.net/forum/index.php?topic=29216.10;wap2
    		if (Minecraft.getMinecraft().gameSettings.mouseSensitivity > 0) {
    			mUserMouseSensitivity = Minecraft.getMinecraft().gameSettings.mouseSensitivity;
    		}
    		Minecraft.getMinecraft().gameSettings.mouseSensitivity = -1F/3F; 
    	}
    	else {
    		if (Minecraft.getMinecraft().gameSettings.mouseSensitivity < 0) {
    			Minecraft.getMinecraft().gameSettings.mouseSensitivity = mUserMouseSensitivity;
    		}

        	mPendingMouseEvent = true;
    	}
    }

}

