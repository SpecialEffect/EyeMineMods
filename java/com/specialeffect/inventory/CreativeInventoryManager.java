package com.specialeffect.inventory;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import com.specialeffect.utils.ModUtils;

import de.skate702.craftingkeys.CraftingKeys;
import de.skate702.craftingkeys.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

/**
 * Manages a Inventory GUI Inventory.
 */
public class CreativeInventoryManager {

	private static CreativeInventoryManager instance = null;

    private Robot robot;
    
    private final int NUM_TABS = 12;
	private final int NUM_COLS = 9;
	private final int NUM_ROWS = 5;

	/**
	 * Creates a new Inventory Manager with the given container.
	 *
	 * @param container The container from a crafting GUI
	 */
	private CreativeInventoryManager() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a Inventory Manager Instance operating on the given container
	 *
	 * @param container A container from a GUI
	 * @return manager-singleton
	 */
	public static CreativeInventoryManager getInstance(int left, int top, 
													   int xSize, int ySize,
													   int currTab) {
		if (instance == null) {
			instance = new CreativeInventoryManager();
		} 
		instance.updateCoordinates(left, top, xSize, ySize);
		if (instance.currTab != currTab) {
			instance.onTabChanged();
			instance.currTab = currTab;
		}
		return instance;
	}
		
	
	private int tabWidth = 0; // width between centres of consecutive tabs
	private int itemWidth = 0; // width between centres of consecutive items
	
	private int topRowYPos = 0;
	private int bottomRowYPos = 0;
	private int topItemYPos = 0;
	
	private int leftColXPos = 0;
	private int leftItemXPos = 0;
	
	private float xScale = 1.0f;
	private float yScale = 1.0f;
	
	private int currTab;
	
	private void onTabChanged() {
		// reset to hovering over first item when changing tabs
		itemRow = -1;
		itemCol = -1;
	}
	
	private void updateCoordinates(int left, int top, int width, int height) {
		int inventoryWidth = width;
		this.tabWidth = (int) (inventoryWidth/6.9);
		this.itemWidth = (int) (inventoryWidth/10.8);
		this.bottomRowYPos = top - tabWidth/2;
		this.topRowYPos = top + height + tabWidth/2;
		this.topItemYPos = (int) (top + height - itemWidth*1.5);

		this.leftColXPos = left + tabWidth/2;
		this.leftItemXPos = (int) (left + itemWidth*0.9);
		
		// Sizes need scaling before turning into click locations
		Minecraft mc = Minecraft.getMinecraft();
		Point size = ModUtils.getScaledDisplaySize(mc);
		this.xScale = (float) (mc.displayWidth)/(float)size.getX();
		this.yScale = (float) (mc.displayHeight)/(float)size.getY();			
		
	}

	public void acceptKey() {

		// Poll keyboard
		Config.pollKeyPresses();

		// Handle key press
		int xPos = -1;
		int yPos = -1;
		// First 5 tabs on top (not inc search which has it's own key already)
		if (Config.isKey0Pressed()) {
			this.switchToTab(0);
		} else if (Config.isKey1Pressed()) {
			this.switchToTab(1);
		} else if (Config.isKey2Pressed()) {
			this.switchToTab(2);
		} else if (Config.isKey3Pressed()) {
			this.switchToTab(3);
		} else if (Config.isKey4Pressed()) {
			this.switchToTab(4);			
		} 
		// 5 tabs on bottom (not inc survival since it's unlikely you need it)
		// Note indices are offset by one since we skipped search.
		else if (Config.isKey5Pressed()) {
			this.switchToTab(6);
		} else if (Config.isKey6Pressed()) {
			this.switchToTab(7);
		} else if (Config.isKey7Pressed()) {
			this.switchToTab(8);
		} else if (Config.isKey8Pressed()) {
			this.switchToTab(9);
		} else if (Config.isKey9Pressed()) {
			this.switchToTab(10);
		} else if (Config.isKeyPrevPressed()) {
			this.switchToTab(validateTabIdx(currTab - 1));
		} else if (Config.isKeyNextPressed()) {
			this.switchToTab(validateTabIdx(currTab + 1));
		} else if (Config.isKeyNextRowPressed()) {
			itemRow++;
			itemRow %= NUM_ROWS;
			// first position on a page starts at -1, -1
			itemCol = Math.max(itemCol, 0); 
			this.hoverItem();
		} else if (Config.isKeyNextColPressed()) {
			itemCol++;
			itemCol %= NUM_COLS;
			// first position on a page starts at -1, -1
			itemRow = Math.max(itemRow, 0);
			this.hoverItem();
		}		
		
	}
	
	private int itemRow = -1;
	private int itemCol = -1;
	
	private void hoverItem() {		
		int yPos = topItemYPos - itemRow*itemWidth;
		int xPos = leftItemXPos + itemCol*itemWidth;
		
		org.lwjgl.input.Mouse.setCursorPosition((int)(xPos*this.xScale),
				(int)(yPos*this.yScale));
	}
	
	private void switchToTab(int iTab) {
		
		// Set up (x, y) for specified tab 
		int xPos = -1;
		int yPos = -1;
		switch(iTab) {
		case 0:
			xPos = leftColXPos;
			yPos = topRowYPos;
			break;
		case 1:
			xPos = leftColXPos+tabWidth;
			yPos = topRowYPos;
			break;
		case 2:
			xPos = leftColXPos+2*tabWidth;
			yPos = topRowYPos;
			break;
		case 3:
			xPos = leftColXPos+3*tabWidth;
			yPos = topRowYPos;
			break;
		case 4:
			xPos = leftColXPos+4*tabWidth;
			yPos = topRowYPos;
			break;
		case 5: 
			xPos = leftColXPos+6*tabWidth;
			yPos = topRowYPos;
			break;
		case 6:
			xPos = leftColXPos;
			yPos = bottomRowYPos;
			break;
		case 7:
			xPos = leftColXPos+tabWidth;;
			yPos = bottomRowYPos;
			break;
		case 8:
			xPos = leftColXPos+2*tabWidth;;
			yPos = bottomRowYPos;
			break;
		case 9:
			xPos = leftColXPos+3*tabWidth;;
			yPos = bottomRowYPos;
			break;
		case 10:
			xPos = leftColXPos+4*tabWidth;
			yPos = bottomRowYPos;
			break;			
		case 11:
			xPos = leftColXPos+6*tabWidth;
			yPos = bottomRowYPos;
			break;
		default:
			System.out.println("Unknown tab requested");
			break;
		}
		
		// Select the tab via a mouse action
		if (xPos > -1) {
			org.lwjgl.input.Mouse.setCursorPosition((int)(xPos*this.xScale),
													(int)(yPos*this.yScale));
			// NB: We use lwjgl to move the mouse because it uses coordinates
			// relative to minecraft window. We use a java robot to click because
			// I don't know how else to.
			robot.mousePress(KeyEvent.BUTTON1_MASK);
			robot.mouseRelease(KeyEvent.BUTTON1_MASK);
			
			// we want to trigger 'tabChanged' if user has explicitly selected
			// the same tab again (otherwise this gets missed)
			this.onTabChanged();
		}	
	}
		
	// Ensure index in range, wrap if necessary
	private int validateTabIdx(int idx) {
		idx += NUM_TABS; // ensure positive
		idx %= NUM_TABS; // modulo into range	
		return idx;
	}
	
}