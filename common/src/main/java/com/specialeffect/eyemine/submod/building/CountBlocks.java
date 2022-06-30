 /**
 * Copyright (C) 2016-2020 Kirsty McNaught
 * 
 * Developed for SpecialEffect, www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *  
 * This class control counting blocks
 * 
 * @author Kirsty McNaught and Becky Tyler
 * @version 1.0
 */
package com.specialeffect.eyemine.submod.building;

import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import com.specialeffect.eyemine.mixin.KeyMappingAccessor;

import java.util.ArrayList;
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
    public static KeyMapping mNumberBlockKB;

    // - a list of BlockPos positions for all the blocks we are tracking
    public ArrayList<BlockPos> blockPosList;

    // - a boolean indicating whether we are currently counting blocks
    public boolean countingBlocks;

	public void onInitializeClient() {

        // Create the list of BlockPos positions
        blockPosList = new ArrayList<BlockPos>();

        // Initialise the countingBlocks flag
        countingBlocks = false;

        // Register the key binding here
        Keybindings.keybindings.add(mNumberBlockKB = new KeyMapping(
				"Number_Block",                            // this needs to be a unique name
				Type.KEYSYM,                               // this is always KEYSYM
				GLFW.GLFW_KEY_K,                           // this selects the default key. try autocompleting GLFW.GLFW_KEY... to see more options
				"category.eyemine.category.eyegaze_common" // this sets the translation key for the name of the category in the controls list 
				                                           // (we use eyegaze_common, eyegaze_extra and eyegaze_settings depending on the mod)
		));
        
        // by adding to Keybindings.keybindings and         
        // registering function with ClientRawInputEvent.Key_PRESSED
        // (look at PickBlock class for reference)
        ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);

        // Register the "place block" event
        BlockEvent.PLACE.register(this::onPlaceBlock);
    }

	private InteractionResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
        // This method gets called when *any* key is pressed
      
        // Skip if there is a GUI visible
		if (ModUtils.hasActiveGui()) { return InteractionResult.PASS; }

        // Skip if F3 is held down (this is used for debugging)
		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return InteractionResult.PASS; }
		        
        
        // If statement for when the key pressed is the one we are using.
        //       Inside the if statement we will need to
        //          - turn counting on or off 
        //          - empty the list of blocks		
        if (mNumberBlockKB.matches(keyCode, scanCode) && mNumberBlockKB.consumeClick())
        {
            ModUtils.sendPlayerMessage("Key pressed: " + keyCode);

            // Toggle the value of countingBlocks
            countingBlocks = !countingBlocks;

            // Clear the list of BlockPos positions
            blockPosList.clear();
 
        }
		return InteractionResult.PASS;
    }
    
    /**
     * Whenever a block is placed, send a chat message with the position of the block
     * 
     * @param l The level the block is being placed in
     * @param position The position of the block that was placed
     * @param state The block state of the block being placed
     * @param entity The entity that placed the block.
     * @return The return value is an InteractionResult. This is a value that tells the game what to do
     * with the block.
     */
    public InteractionResult onPlaceBlock(Level l, BlockPos position, BlockState state, Entity entity) {
        // Add block's position to list of block positions
        blockPosList.add(position);
        
        // Send a chat message showing position and number of blocks placed
        ModUtils.sendPlayerMessage("Block " + blockPosList.size() + " placed at " + position); // for debugging

        // This method is called whenever a block is placed
        return InteractionResult.PASS;
    }
}
