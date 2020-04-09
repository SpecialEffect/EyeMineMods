package com.specialeffect.inventory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import com.specialeffect.mods.mousehandling.MouseHelperOwn;
import net.minecraft.client.Minecraft;

/**
 * Manages a Inventory GUI Inventory.
 */
public class SurvivalInventoryManager {
	
	// Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

	private static SurvivalInventoryManager instance = null;

    private final int NUM_TABS = 5;
	private final int NUM_COLS = 9;
	private final int NUM_ROWS = 5;

	/**
	 * Creates a new Inventory Manager with the given container.
	 *
	 * @param container The container from a crafting GUI
	 */
	private SurvivalInventoryManager() {
	}

	/**
	 * Returns a Inventory Manager Instance operating on the given container
	 *
	 * @param container A container from a GUI
	 * @return manager-singleton
	 */
	public static SurvivalInventoryManager getInstance(int left, int top, 
													   int xSize, int ySize) {
		if (instance == null) {
			instance = new SurvivalInventoryManager();
		} 
		instance.updateCoordinates(left, top, xSize, ySize);
		return instance;
	}
		
	int left = 0;
	int top = 0;
	
	private int tabWidth = 0; // width between centres of consecutive tabs
	private int itemWidth = 0; // width between centres of consecutive items
	
	private int topRowYPos = 0;
	private int bottomRowYPos = 0;
	private int topItemYPos = 0;
	
	private int leftColXPos = 0;
	private int leftItemXPos = 0;
	
	private float xScale = 1.0f;
	private float yScale = 1.0f;
	
	private int recipeX = 0;
	private int recipeY = 0;
	
	private int currTab;
	
	private void onTabChanged() {
		// reset to hovering over first item when changing tabs
		itemRow = -1;
		itemCol = -1;
	}
	
	private void updateCoordinates(int left, int top, int width, int height) {
		int inventoryWidth = width;
		this.left = left;
		this.top = top;
				
		
		this.tabWidth = (int) (inventoryWidth/6.9);
		this.itemWidth = (int) (inventoryWidth/9.5);		
		this.topRowYPos = top + tabWidth/2;
		this.topItemYPos = (int) (top + itemWidth*1.5);

		this.leftColXPos = left-inventoryWidth;
		this.leftItemXPos = (int) (left + itemWidth*0.9);
		
		this.recipeX = (int) (inventoryWidth*0.65);
		this.recipeY = (int) (inventoryWidth*0.4);
		
		// Sizes need scaling before turning into click locations
		Minecraft mc = Minecraft.getInstance();
		this.xScale = (float) (mc.mainWindow.getWidth())/(float)mc.mainWindow.getScaledWidth();
		this.yScale = (float) (mc.mainWindow.getHeight())/(float)mc.mainWindow.getScaledHeight();						
	}
	
	public void clickRecipeBook() {
		LOGGER.debug("Recipe book");		
		int xPos = left + recipeX; 
		int yPos = top + recipeY;
		MouseHelperOwn helper = (MouseHelperOwn)Minecraft.getInstance().mouseHelper;		
		helper.leftMouseClickAtPosition(xPos*this.xScale, yPos*this.yScale);		
	}
	
	public void changePage(boolean forward) {
		LOGGER.debug("Page "+forward);
		
		int yPos = (int) (top + 5.5*tabWidth);
		int xPos = forward ? xPos = left - 100 : left - 50;		
		 		
		MouseHelperOwn helper = (MouseHelperOwn)Minecraft.getInstance().mouseHelper;	
		helper.moveCursor(xPos*this.xScale, yPos*this.yScale);
		helper.leftMouseClickAtPosition(xPos*this.xScale, yPos*this.yScale);		
	}
	
	public void toggleCraftable() {
		LOGGER.debug("craftable");
		
		int yPos = (int) (top + tabWidth/2);
		int xPos = 	left - 20;
		 		
		MouseHelperOwn helper = (MouseHelperOwn)Minecraft.getInstance().mouseHelper;	
		helper.moveCursor(xPos*this.xScale, yPos*this.yScale);
		helper.leftMouseClickAtPosition(xPos*this.xScale, yPos*this.yScale);		
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
		// 
		LOGGER.debug(InventoryConfig.keySurvRecipes.get());
		LOGGER.debug(key);
		if (key == InventoryConfig.keySurvRecipes.get()) {
			this.clickRecipeBook();			
		} 
		else if (key == InventoryConfig.keySurvPrevTab.get()) {
			this.switchToTab(validateTabIdx(currTab - 1));			
		}
		else if (key == InventoryConfig.keySurvNextTab.get()) {
			this.switchToTab(validateTabIdx(currTab + 1));			
		}		
		else if (key == InventoryConfig.keySurvPrevPage.get()) {
			this.changePage(true);			
		}
		else if (key == InventoryConfig.keySurvNextPage.get()) {
			this.changePage(false);			
		}
		else if (key == InventoryConfig.keySurvCraftable.get()) {
			this.toggleCraftable();
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
		LOGGER.debug("switch tab " + iTab);
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
			xPos = leftColXPos;
			yPos = topRowYPos+tabWidth;
			break;
		case 2:
			xPos = leftColXPos;
			yPos = topRowYPos+2*tabWidth;
			break;
		case 3:
			xPos = leftColXPos;
			yPos = topRowYPos+3*tabWidth;
			break;
		case 4:
			xPos = leftColXPos;
			yPos = topRowYPos+4*tabWidth;
			break;
		case 5: 
			xPos = leftColXPos;
			yPos = topRowYPos+6*tabWidth;
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
			LOGGER.debug("Unknown tab requested");
			break;
		}
		
		// Select the tab via a mouse action
		if (xPos > -1) {			
			//FIXME: test with eye-gaze mouse emulation, no stray cursor movements interfere
			// Do we need synchronisation in the mouse helper??
			MouseHelperOwn helper = (MouseHelperOwn)Minecraft.getInstance().mouseHelper;
			helper.moveCursor(xPos*this.xScale, yPos*this.yScale);
			helper.leftMouseClickAtPosition(xPos*this.xScale, yPos*this.yScale);
			
			// we want to trigger 'tabChanged' if user has explicitly selected
			// the same tab again (otherwise this gets missed)
			this.onTabChanged();
			
			currTab = iTab;
		}	
	}
		
	// Ensure index in range, wrap if necessary
	private int validateTabIdx(int idx) {
		idx += NUM_TABS; // ensure positive
		idx %= NUM_TABS; // modulo into range	
		return idx;
	}
	
}