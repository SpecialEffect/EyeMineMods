package com.specialeffect.mods;

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
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.messages.MovePlayerMessage;
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
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

@Mod(modid = MoveWithGaze2.MODID, 
	 version = ModUtils.VERSION,
	 name = MoveWithGaze2.NAME,
	 guiFactory = "com.specialeffect.gui.GuiFactoryWalkWithGaze")
public class MoveWithGaze2 extends BaseClassWithCallbacks {
	public static final String MODID = "specialeffect.movewithgaze2";
    public static final String VERSION = "0.1";
    public static final String NAME = "MoveWithGaze2";

    private static KeyBinding mToggleAutoWalkKB;
    
    public static Configuration mConfig;

    private static boolean mMoveWhenMouseStationary = false;
    private static float mCustomSpeedFactor = 0.8f;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {    
    	FMLCommonHandler.instance().bus().register(this);  
    	
    	ModUtils.setupModInfo(event, this.MODID, this.VERSION, this.NAME,
				"Add key binding to start/stop walking continuously, with direction controlled by mouse/eyetracker");
    	
    	// Set up config
    	mConfig = new Configuration(event.getSuggestedConfigurationFile());
    	this.syncConfig();
    	
    	mOverlay = new JoystickControlOverlay(Minecraft.getMinecraft());
    }
    
    @EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(mOverlay);
	}
    
    
    @SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if(eventArgs.modID.equals(this.MODID)) {
			syncConfig();
		}
	}
	
	public static void syncConfig() {
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
    	mToggleAutoWalkKB = new KeyBinding("Toggle auto-walk2", Keyboard.KEY_B, "SpecialEffect");
        ClientRegistry.registerKeyBinding(mToggleAutoWalkKB);
        
        mPrevLookDirs = new LinkedBlockingQueue<Vec3>();
        
		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureLeft("specialeffect:icons/legacy-mode.png");
    }
    
    private static int mIconIndex;
    
	private static JoystickControlOverlay mOverlay;

	public static void stop() {
    	mDoingAutoWalk = false;
		StateOverlay.setStateLeftIcon(mIconIndex, mDoingAutoWalk);        	
		MouseHandler.setVanillaMouseHandling(!mDoingAutoWalk);
    	mOverlay.setVisible(mDoingAutoWalk);
    }
	
	// Some hard-coded fudge factors for maximums.
	private final float mMaxForward = 1.5f;
	private final float mMaxBackward = 0.5f;
	private final int mMaxYaw = 100; // at 100% sensitivity
	
    @SubscribeEvent
    public void onLiving(LivingUpdateEvent event) {
    	if(event.entityLiving instanceof EntityPlayer) {

    		EntityPlayer player = (EntityPlayer)event.entityLiving;    		
    		if (mDoingAutoWalk && (mMoveWhenMouseStationary || MouseHandler.hasPendingEvent()) ) {
    			
    			// Y gives distance to walk forward/back.
    			float walkForwardAmount = 0.0f;
    			float h = (float)Minecraft.getMinecraft().displayHeight;
    			float h3 = h/3.0f;

    			if (lastMouseY < h3) {
    				walkForwardAmount = -mMaxBackward*(h3-lastMouseY)/h3;
    			}
    			else if (lastMouseY > 2*h3) {
    				walkForwardAmount = mMaxForward*(lastMouseY-2*h3)/h3;
    			}

    			// scaled by mCustomSpeedFactor from MoveWithGaze
    			walkForwardAmount *= MoveWithGaze.mCustomSpeedFactor;

    			// X gives how far to rotate viewpoint
    			float w = (float)Minecraft.getMinecraft().displayWidth;
    			float w2 = w/2.0f;

    			float yawAmount = (lastMouseX - w2)/w2;
    			yawAmount*= mMaxYaw;
    			    			
    			// scaled by user sensitivity
    			yawAmount *= MouseHandler.mUserMouseSensitivity;
    			
    			// TODO: Scale by user sensitivity?
    			
    			player.rotationYaw += yawAmount;
    			player.moveEntityWithHeading(0.0f, walkForwardAmount);
    		}
			this.processQueuedCallbacks(event);
			
    	}
    }
    
	private static boolean mDoingAutoWalk = false;
    private double mWalkDistance = 1.0f;
    private Queue<Vec3> mPrevLookDirs;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        
        if(mToggleAutoWalkKB.isPressed()) {
        	mDoingAutoWalk = !mDoingAutoWalk;
        	if (mDoingAutoWalk) { 
        		MoveWithGaze.stop();
        	}
        	MouseHandler.setVanillaMouseHandling(!mDoingAutoWalk);
        	mOverlay.setVisible(mDoingAutoWalk);
			StateOverlay.setStateLeftIcon(mIconIndex, mDoingAutoWalk);
        	this.queueChatMessage("Auto walk: " + (mDoingAutoWalk ? "ON" : "OFF"));
        }
    }
    
    private int i = 0;
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    
    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
    	if (mDoingAutoWalk) {
    		// since mouse is captured, x and y pos are encoded in deltas.
    		lastMouseX = Minecraft.getMinecraft().displayWidth/2 + Mouse.getEventDX();
    		lastMouseY = Minecraft.getMinecraft().displayHeight/2 + Mouse.getEventDY(); 
    	}
    	else {
    		lastMouseX = Minecraft.getMinecraft().displayWidth/2;
    		lastMouseY = Minecraft.getMinecraft().displayHeight/2;
    	}
    }
}


