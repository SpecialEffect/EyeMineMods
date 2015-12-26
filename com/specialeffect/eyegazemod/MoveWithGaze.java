package com.specialeffect.eyegazemod;

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
    	
    	mToggleAutoWalkKB = new KeyBinding("", Keyboard.KEY_H, "SpecialEffect");
        ClientRegistry.registerKeyBinding(mToggleAutoWalkKB);
    }
    
    @SubscribeEvent
    public void onLiving(LivingUpdateEvent event) {
    	if(event.entityLiving instanceof EntityPlayer) {
    		EntityPlayer player = (EntityPlayer)event.entityLiving;
    		
    		// First time, initialise to current view
    		if (mLastLookDir == null) { mLastLookDir = player.getLookVec(); }
       		Vec3 lookVec = player.getLookVec();
            
            if (mDoingAutoWalk) {
            	// If in auto-walk mode, walk forward an amount scaled by the view change (less if looking around)
            	double dot = lookVec.dotProduct(mLastLookDir);
            	double thresh = 0.8; // below this, no movement
            	double distance = mWalkDistance*(dot-thresh)/(1.0-thresh);
            	distance = Math.max(distance, 0.0);
            	player.moveEntityWithHeading(0, (float)distance);
            }
            mLastLookDir = lookVec;

    	}
    }
    
    private boolean mDoingAutoWalk = false;
    private double mWalkDistance = 1.0f;
    private Vec3 mLastLookDir;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        
        if(mToggleAutoWalkKB.isPressed()) {
            System.out.println("Toggle auto-walk");

        	mDoingAutoWalk = !mDoingAutoWalk;
        }
    }

}

