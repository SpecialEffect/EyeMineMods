/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 * 
 * Developed for SpecialEffect, www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.inventory.manager;

import com.specialeffect.eyemine.platform.InventoryConfig;
import com.specialeffect.eyemine.utils.MouseHelper;
import com.specialeffect.utils.ModUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.inventory.ChestMenu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

/**
 * Manages a Inventory GUI Inventory.
 */
public class ChestInventoryManager {
	
	// Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

	private static ChestInventoryManager instance = null;
    
	private final int NUM_COLS = 9;	
	private int num_rows = 1; // populated from container

	/**
	 * Creates a new Inventory Manager with the given container.
	 *
	 * @param //container The container from a crafting GUI
	 */
	private ChestInventoryManager() {
	}

	/**
	 * Returns a Inventory Manager Instance operating on the given container
	 * @param chestContainer 
	 *
	 * @param chestContainer A container from a GUI
	 * @return manager-singleton
	 */
	public static ChestInventoryManager getInstance(int left, int top, 
													   int xSize, int ySize, 
													   ChestMenu chestContainer) {
		if (instance == null) {
			instance = new ChestInventoryManager();
		} 
		instance.updateCoordinates(left, top, xSize, ySize, chestContainer);
		return instance;
	}
		
	
	private int guiLeft = 0;
	private int guiTop = 0;
	
	// container coords start from centre of top left slot
	private int containerTop = 0;
	private int containerLeft = 0;
	
	private int itemWidth = 0; // width between centres of consecutive items
		
	private float xScale = 1.0f;
	private float yScale = 1.0f;
	
	
	ChestMenu chestContainer;


	private void updateCoordinates(int left, int top, int width, int height, ChestMenu chestContainer) {
				
		this.chestContainer = chestContainer;	
		this.guiLeft = left;
		this.guiTop = top;

		// Get positions straight from container
		// SLOTS in container are: 
		// {nRows}x9: chest slots
		// 3x9: inventory slots
		// 1x9: hotbar
				
		num_rows = chestContainer.getRowCount();
		
		int w = chestContainer.getSlot(1).x - chestContainer.getSlot(0).x;
		int x = chestContainer.getSlot(0).x;
		int y = chestContainer.getSlot(0).y;
		
		this.itemWidth = w;
		this.containerLeft = left + x + w/2;
		this.containerTop = top + y + w/2;
		
		// Sizes need scaling before turning into click locations
		Minecraft mc = Minecraft.getInstance();
		this.xScale = (float) (mc.getWindow().getWidth())/(float)mc.getWindow().getGuiScaledWidth();
		this.yScale = (float) (mc.getWindow().getHeight())/(float)mc.getWindow().getGuiScaledHeight();
	}
	
		
	public void clickItem() {
		int yPos = containerTop + itemRow*itemWidth;
		int xPos = containerLeft + itemCol*itemWidth;

		MouseHandler helper = Minecraft.getInstance().mouseHandler;
		MouseHelper.instance().leftMouseClickAtPosition(helper, xPos*this.xScale, yPos*this.yScale);
	}
	
	public void shiftClickItem() {
		int yPos = containerTop + itemRow*itemWidth;
		int xPos = containerLeft + itemCol*itemWidth;

		MouseHandler helper = Minecraft.getInstance().mouseHandler;
		MouseHelper.instance().moveCursor(helper, xPos*this.xScale, yPos*this.yScale);
				
		int idx = ModUtils.findSlotInContainer(chestContainer, guiLeft, guiTop, xPos, yPos, itemWidth);
		if (idx > -1) {
			LOGGER.debug("taking slot "+idx);
			chestContainer.quickMoveStack(Minecraft.getInstance().player, idx);
		}
		else {
			LOGGER.debug("No slot found at ("+xPos+", "+yPos+")");
		}	
	}

	public void acceptKey(int key) {
		// Handle key press
		// First 5 tabs on top (not inc search which has it's own key already)
		if (key == InventoryConfig.getKey0()) {
			this.clickItem();	
		} else if (key == InventoryConfig.getKey1()) {
			this.shiftClickItem();
		} 
			
			
		if (key == InventoryConfig.getKeyNextItemCol()) {
			itemCol++;
			itemCol %= NUM_COLS;
			// first position on a page starts at -1, -1
			itemRow = Math.max(itemRow, 0);
			this.hoverItem();
		}
		else if (key == InventoryConfig.getKeyNextItemRow()) {
			itemRow++;
			itemRow %= num_rows;
			// first position on a page starts at -1, -1
			itemCol = Math.max(itemCol, 0); 
			this.hoverItem();
		} 
	}
	
	private int itemRow = -1;
	private int itemCol = -1;
	
	private void scrollDown(int amount) {
		MouseHandler helper = Minecraft.getInstance().mouseHandler;
		MouseHelper.instance().scroll(helper, amount);
	}
	
	private void hoverItem() {		
		int yPos = containerTop + itemRow*itemWidth;
		int xPos = containerLeft + itemCol*itemWidth;
		
		GLFW.glfwSetCursorPos(Minecraft.getInstance().getWindow().getWindow(), xPos*this.xScale, yPos*this.yScale);
	}
}