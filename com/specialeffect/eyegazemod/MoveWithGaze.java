package com.specialeffect.eyegazemod;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

@Mod(modid = MoveWithGaze.MODID, 
	 version = MoveWithGaze.VERSION,
	 name = MoveWithGaze.NAME)
public class MoveWithGaze {
	public static final String MODID = "specialeffect.movewithgaze";
    public static final String VERSION = "0.1";
    public static final String NAME = "SpecialEffectMoveWithGaze";

    private static KeyBinding mToggleAutoWalkKB;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {    
    	FMLCommonHandler.instance().bus().register(this);    	
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
       		if (mPrevLookDirs.size() > 10) {
       			mPrevLookDirs.remove();
       		}
       		
            if (mDoingAutoWalk) {
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
            	double thresh = 0.8; // below this, no movement
            	double distance = Math.max(0, /*mWalkDistance**/(normalCongruency - thresh)/(1.0-thresh));
//                System.out.println("distance = "+distance);
            	//distance = Math.max(distance, 0.0);
            	player.moveEntityWithHeading(0, (float)distance);
            }
    	}
    }
    
    private boolean mDoingAutoWalk = false;
    private double mWalkDistance = 1.0f;
    private Queue<Vec3> mPrevLookDirs;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        
        if(mToggleAutoWalkKB.isPressed()) {

        	mDoingAutoWalk = !mDoingAutoWalk;
        }
    }

}

