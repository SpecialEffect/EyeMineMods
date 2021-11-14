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
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

/**
 * Manages a Inventory GUI Inventory.
 */
public class CreativeInventoryManager {

	// Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

	private static CreativeInventoryManager instance = null;

	/**
	 * Creates a new Inventory Manager with the given container.
	 *
	 * @param /container The container from a crafting GUI
	 */
	private CreativeInventoryManager() {
	}

	/**
	 * Returns a Inventory Manager Instance operating on the given container
	 * @param creativeContainer 
	 *
	 * @param creativeContainer A container from a GUI
	 * @return manager-singleton
	 */
	public static CreativeInventoryManager getInstance(int left, int top, 
													   int xSize, int ySize,
													   int currTab, 
													   ItemPickerMenu creativeContainer) {
		if (instance == null) {
			instance = new CreativeInventoryManager();
		} 
		instance.updateCoordinates(left, top, xSize, ySize, creativeContainer);
		if (instance.currTab != currTab) {
			instance.onTabChanged();
			instance.currTab = currTab;
		}
		return instance;
	}	

    private final int NUM_TABS = 12;
	private final int NUM_COLS = 9;
	private final int NUM_ROWS = 5;
	
	private ItemPickerMenu creativeContainer;
		
	// GUI position in absolute screen coords (unscaled)
	private int guiLeft = 0;
	private int guiTop = 0;
	
	// Container coords, start from centre of top left slot, in absolute coords
	private int containerTop = 0;
	private int containerLeft = 0;
	private int itemWidth = 0; // width between centres of consecutive items
	
	// Tab coords, start from centres of tabs	
	private int tabsLeft = 0;
	private int tabsTop = 0;
	private int tabsBottom = 0;
	private int tabWidth = 0; 
	
	private float xScale = 1.0f;
	private float yScale = 1.0f;
	
	// Current state, reported by GUI and cached
	private int currTab;	
		
	private void onTabChanged() {
		// reset to hovering over first item when changing tabs
		itemRow = -1;
		itemCol = -1;
	}
	
	private void updateCoordinates(int left, int top, int width, int height, ItemPickerMenu creativeContainer) {
		
		this.creativeContainer = creativeContainer;
		this.guiLeft = left;
		this.guiTop = top;		
		
		// tabs dimensions, hard-coded
		this.tabWidth = (int) (width/6.9);
		this.tabsBottom = top + height + tabWidth/2;
		this.tabsTop = top - tabWidth/2;
		this.tabsLeft = left + tabWidth/2;
		
		// item dimensions, get from container:	
		// SLOTS:
		// 0..44:  Locked slots
		// 45..52: 9 normal slots (hotbar?)		
		int w = creativeContainer.getSlot(1).x - creativeContainer.getSlot(0).x;
		int x = creativeContainer.getSlot(0).x;
		int y = creativeContainer.getSlot(0).y;				
	
		this.itemWidth = w;
		this.containerLeft = left + x + w/2;
		this.containerTop = top + y + w/2;	
		
		// equiv SHIFT CLICK creativeContainer.transferStackInSlot(playerIn, index)
		
//		creativeContainer
		
		// SLOTS:
		// 0..44:  Locked slots
		// 45..52: 9 normal slots (hotbar?)
		
		
		// Sizes need scaling before turning into click locations
		Minecraft mc = Minecraft.getInstance();
		this.xScale = (float) (mc.getWindow().getWidth())/(float)mc.getWindow().getGuiScaledWidth();
		this.yScale = (float) (mc.getWindow().getHeight())/(float)mc.getWindow().getGuiScaledHeight();
	}
	
	private void updateItemPos() {
		// Query mouse position to identify which item we're hovered over
		// This allows users to set cursor position manually and then make adjustments
		// with prev/next
		MouseHandler helper = Minecraft.getInstance().mouseHandler;
		System.out.println(helper.xpos() + ", " + helper.ypos());
		int x = (int) (helper.xpos()/this.xScale);
		int y = (int) (helper.ypos()/this.yScale);
		int i = ModUtils.findSlotInContainer(creativeContainer, guiLeft, guiTop, x, y, itemWidth);
		if (i > -1) {
			LOGGER.debug("slot "+i);
			itemRow = Math.floorDiv(i, NUM_COLS);
			itemCol = i % NUM_COLS;
		}
	}

	public boolean acceptKey(int key) {

		// Poll keyboard
//		InventoryConfig.acceptKeyPress(key);
		boolean handled = false;
		
		// Handle key press
		// First 5 tabs on top (not inc search which has it's own key already)
		if (key == InventoryConfig.getKey0()) {
			this.switchToTab(0);	
			handled = true;
		} else if (key == InventoryConfig.getKey1()) {
			this.switchToTab(1);
			handled = true;
		} else if (key == InventoryConfig.getKey2()) {
			this.switchToTab(2);
			handled = true;
		} else if (key == InventoryConfig.getKey3()) {
			this.switchToTab(3);
			handled = true;
		} else if (key == InventoryConfig.getKey4()) {
			this.switchToTab(4);	
			handled = true;
		} else if (key == InventoryConfig.getKeySearch()) {
			this.switchToTab(5);
			handled = true;
		}
		// 5 tabs on bottom (not inc survival since it's unlikely you need it)
		// Note indices are offset by one since we skipped search.
		else if (key == InventoryConfig.getKey5()) {
			this.switchToTab(6);
			handled = true;
		} else if (key == InventoryConfig.getKey6()) {
			this.switchToTab(7);
			handled = true;
		} else if (key == InventoryConfig.getKey7()) {
			this.switchToTab(8);
			handled = true;
		} else if (key == InventoryConfig.getKey8()) {
			this.switchToTab(9);
			handled = true;
		} else if (key == InventoryConfig.getKey9()) {
			this.switchToTab(10);
			handled = true;
		} else if (key == InventoryConfig.getKeyPrev()) {
			this.switchToTab(validateTabIdx(currTab - 1));
			handled = true;
		} else if (key == InventoryConfig.getKeyNext()) {
			this.switchToTab(validateTabIdx(currTab + 1));
			handled = true;
		} else if (key == InventoryConfig.getKeyNextItemRow()) {
			this.updateItemPos();
			itemRow++;
			itemRow %= NUM_ROWS;
			// first position on a page starts at -1, -1
			itemCol = Math.max(itemCol, 0); 
			this.hoverItem();
			handled = true;
		} else if (key == InventoryConfig.getKeyNextItemCol()) {
			this.updateItemPos();
			itemCol++;
			itemCol %= NUM_COLS;
			// first position on a page starts at -1, -1
			itemRow = Math.max(itemRow, 0);
			this.hoverItem();
			handled = true;
		} else if (key == InventoryConfig.getKeyDrop()) {
			this.switchToTab(-1);
			handled = true;
		} else if (key == InventoryConfig.getKeyScrollUp()) {
			this.scrollDown(-2);
			handled = true;
			// (scrollamount = almost an entire screen, but 1 row repeated to ground you)
		} else if (key == InventoryConfig.getKeyScrollDown()) {
			this.scrollDown(+2);
			handled = true;
		}
		return handled;
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
	
	private void switchToTab(int iTab) {
		
		// Set up (x, y) for specified tab 
		int xPos = -1;
		int yPos = -1;
		switch(iTab) {
		case -1:
			// this is proxy for "drop by clicking outside inventory"
			xPos = tabsLeft - tabWidth;
			yPos = tabsTop;
			break;
		case 0:
			xPos = tabsLeft;
			yPos = tabsTop;
			break;
		case 1:
			xPos = tabsLeft+tabWidth;
			yPos = tabsTop;
			break;
		case 2:
			xPos = tabsLeft+2*tabWidth;
			yPos = tabsTop;
			break;
		case 3:
			xPos = tabsLeft+3*tabWidth;
			yPos = tabsTop;
			break;
		case 4:
			xPos = tabsLeft+5*tabWidth;
			yPos = tabsTop;
			break;
		case 5: 
			xPos = tabsLeft+6*tabWidth;
			yPos = tabsTop;
			break;
		case 6:
			xPos = tabsLeft;
			yPos = tabsBottom;
			break;
		case 7:
			xPos = tabsLeft+tabWidth;;
			yPos = tabsBottom;
			break;
		case 8:
			xPos = tabsLeft+2*tabWidth;;
			yPos = tabsBottom;
			break;
		case 9:
			xPos = tabsLeft+3*tabWidth;;
			yPos = tabsBottom;
			break;
		case 10:
			xPos = tabsLeft+4*tabWidth;
			yPos = tabsBottom;
			break;			
		case 11:
			xPos = tabsLeft+6*tabWidth;
			yPos = tabsBottom;
			break;
		default:
			LOGGER.debug("Unknown tab requested");
			break;
		}
		
		// Select the tab via a mouse action
		if (xPos > -1) {			
			//FIXME: test with eye-gaze mouse emulation, no stray cursor movements interfere
			// Do we need synchronisation in the mouse helper??
			MouseHandler helper = Minecraft.getInstance().mouseHandler;
			MouseHelper.instance().leftMouseClickAtPosition(helper, xPos*this.xScale, yPos*this.yScale);
			
			// we want to trigger 'tabChanged' if user has explicitly selected
			// the same tab again (otherwise this gets missed)
			this.onTabChanged();
			
			GLFW.glfwSetCursorPos(Minecraft.getInstance().getWindow().getWindow(), xPos*this.xScale, yPos*this.yScale);
		}	
	}
		
	// Ensure index in range, wrap if necessary
	private int validateTabIdx(int idx) {
		idx += NUM_TABS; // ensure positive
		idx %= NUM_TABS; // modulo into range	
		return idx;
	}

	public void resetMouse() {
		LOGGER.debug("reset mouse");
		MouseHandler helper = Minecraft.getInstance().mouseHandler;
		MouseHelper.instance().moveCursor(helper, guiLeft*xScale, guiTop*yScale);
	}
	
}