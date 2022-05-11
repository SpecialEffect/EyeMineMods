package com.specialeffect.eyemine.submod.building;

import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import com.specialeffect.eyemine.mixin.KeyMappingAccessor;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;

import org.lwjgl.glfw.GLFW;

import me.shedaniel.architectury.event.events.client.ClientRawInputEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import me.shedaniel.architectury.event.events.GuiEvent;
import me.shedaniel.architectury.event.events.BlockEvent;


public class CountBlocks extends SubMod {
	public final String MODID = "countblocks"; // this needs to be a unique ID

    // Member variables we need
    // - a static KeyMapping for the shortcut key
    // - a list of BlockPos positions for all the blocks we are tracking
    // - a boolean indicating whether we are currently counting blocks

	public void onInitializeClient() {

        // TODO: Register the key binding here
        // by adding to Keybindings.keybindings and         
        // registering function with ClientRawInputEvent.Key_PRESSED
        // (look at PickBlock class for reference)

        // Register the "place block" event
        BlockEvent.PLACE.register(this::onPlaceBlock);
    }

	private InteractionResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
        // This method gets called when *any* key is pressed

        // Skip if there is a GUI visible
		if (ModUtils.hasActiveGui()) { return InteractionResult.PASS; }

        // Skip if F3 is held down (this is used for debugging)
		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return InteractionResult.PASS; }
		        
        ModUtils.sendPlayerMessage("Key pressed: " + keyCode);
        
        // TODO: add if statement for if the key pressed is the one we are using.
        //       Inside the if statement we will need to
        //          - turn counting on or off 
        //          - empty the list of blocks		
        
		return InteractionResult.PASS;
    }
    
    public InteractionResult onPlaceBlock(Level l, BlockPos position, BlockState state, Entity entity) {
        // This method is called whenever a block is placed
        return InteractionResult.PASS;
    }
}
