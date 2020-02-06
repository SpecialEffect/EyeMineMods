package com.specialeffect.inventory;

import org.lwjgl.glfw.GLFW;

import com.specialeffect.mods.mousehandling.MouseHelperOwn;
import net.minecraft.client.Minecraft;

/**
 * Manages a Inventory GUI Inventory.
 */
public class ChestInventoryManager {

	private static ChestInventoryManager instance = null;

    private final int NUM_TABS = 12;
	private final int NUM_COLS = 9;
	private final int NUM_ROWS = 5;

	/**
	 * Creates a new Inventory Manager with the given container.
	 *
	 * @param container The container from a crafting GUI
	 */
	private ChestInventoryManager() {
	}

	/**
	 * Returns a Inventory Manager Instance operating on the given container
	 *
	 * @param container A container from a GUI
	 * @return manager-singleton
	 */
	public static ChestInventoryManager getInstance(int left, int top, 
													   int xSize, int ySize) {
		if (instance == null) {
			instance = new ChestInventoryManager();
		} 
		instance.updateCoordinates(left, top, xSize, ySize);
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
		this.itemWidth = (int) (inventoryWidth/9.5);
		this.bottomRowYPos = top + height + tabWidth/2;
		this.topRowYPos = top - tabWidth/2;
		this.topItemYPos = (int) (top + itemWidth*1.5);

		this.leftColXPos = left + tabWidth/2;
		this.leftItemXPos = (int) (left + itemWidth*0.9);
		
		// Sizes need scaling before turning into click locations
		Minecraft mc = Minecraft.getInstance();
		this.xScale = (float) (mc.mainWindow.getWidth())/(float)mc.mainWindow.getScaledWidth();
		this.yScale = (float) (mc.mainWindow.getHeight())/(float)mc.mainWindow.getScaledHeight();						
	}
	
		
	public void clickItem() {
		int yPos = topItemYPos;
		int xPos = leftItemXPos;
		
		MouseHelperOwn helper = (MouseHelperOwn)Minecraft.getInstance().mouseHelper;
		helper.leftMouseClickAtPosition(xPos*this.xScale, yPos*this.yScale);
		
		//GLFW.glfwSetCursorPos(Minecraft.getInstance().mainWindow.getHandle(), xPos*this.xScale, yPos*this.yScale);		
	}
	
	public void shiftClickItem() {
		int yPos = topItemYPos;
		int xPos = leftItemXPos;
		
		MouseHelperOwn helper = (MouseHelperOwn)Minecraft.getInstance().mouseHelper;
		helper.leftShiftMouseClickAtPosition(xPos*this.xScale, yPos*this.yScale);
		
	}

	public void acceptKey(int key) {

		// Handle key press
		// First 5 tabs on top (not inc search which has it's own key already)
		if (key == InventoryConfig.key0.get()) {
			this.clickItem();	
		} else if (key == InventoryConfig.key1.get()) {
			this.shiftClickItem();
		} else if (key == InventoryConfig.key2.get()) {
			this.switchToTab(2);
		} else if (key == InventoryConfig.key3.get()) {
			this.switchToTab(3);
		} else if (key == InventoryConfig.key4.get()) {
			this.switchToTab(4);			
		} else if (key == InventoryConfig.keySearch.get()) {
			this.switchToTab(5);
		}
		// 5 tabs on bottom (not inc survival since it's unlikely you need it)
		// Note indices are offset by one since we skipped search.
		else if (key == InventoryConfig.key5.get()) {
			this.switchToTab(6);
		} else if (key == InventoryConfig.key6.get()) {
			this.switchToTab(7);
		} else if (key == InventoryConfig.key7.get()) {
			this.switchToTab(8);
		} else if (key == InventoryConfig.key8.get()) {
			this.switchToTab(9);
		} else if (key == InventoryConfig.key9.get()) {
			this.switchToTab(10);
		} else if (key == InventoryConfig.keyPrev.get()) {
			this.switchToTab(validateTabIdx(currTab - 1));
		} else if (key == InventoryConfig.keyNext.get()) {
			this.switchToTab(validateTabIdx(currTab + 1));
		} else if (key == InventoryConfig.keyNextItemRow.get()) {
			itemRow++;
			itemRow %= NUM_ROWS;
			// first position on a page starts at -1, -1
			itemCol = Math.max(itemCol, 0); 
			this.hoverItem();
		} else if (key == InventoryConfig.keyNextItemCol.get()) {
			itemCol++;
			itemCol %= NUM_COLS;
			// first position on a page starts at -1, -1
			itemRow = Math.max(itemRow, 0);
			this.hoverItem();
		} else if (key == InventoryConfig.keyDrop.get()) {
			this.switchToTab(-1);
		} else if (key == InventoryConfig.keyScrollUp.get()) {
			this.scrollDown(-2);
			// FIXME: decide how far to scroll, this seems to be nice
			// (almost an entire screen, but 1 row repeated to ground you)
		} else if (key == InventoryConfig.keyScrollDown.get()) {
			this.scrollDown(+2);
		}		
	}
	
	private int itemRow = -1;
	private int itemCol = -1;
	
	private void scrollDown(int amount) {		
		MouseHelperOwn helper = (MouseHelperOwn)Minecraft.getInstance().mouseHelper;
		helper.scroll(amount);				
	}
	
	private void hoverItem() {		
		int yPos = topItemYPos + itemRow*itemWidth;
		int xPos = leftItemXPos + itemCol*itemWidth;
		
		GLFW.glfwSetCursorPos(Minecraft.getInstance().mainWindow.getHandle(), xPos*this.xScale, yPos*this.yScale);		
	}
	
	private void switchToTab(int iTab) {
		
		// Set up (x, y) for specified tab 
		int xPos = -1;
		int yPos = -1;
		switch(iTab) {
		case -1:
			// this is proxy for "drop by clicking outside inventory"
			xPos = leftColXPos - tabWidth;
			yPos = topRowYPos;
			break;
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
			//FIXME: test with eye-gaze mouse emulation, no stray cursor movements interfere
			// Do we need synchronisation in the mouse helper??
			MouseHelperOwn helper = (MouseHelperOwn)Minecraft.getInstance().mouseHelper;
			helper.leftMouseClickAtPosition(xPos*this.xScale, yPos*this.yScale);
			
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