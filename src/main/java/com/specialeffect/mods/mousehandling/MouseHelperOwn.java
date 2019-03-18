package com.specialeffect.mods.mousehandling;

import net.java.games.input.Mouse;
import net.minecraft.client.MouseHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// Based on the vanilla MouseHelper in forge, but also:
// - allows normal mouse control with an ungrabbed mouse
// - reduces the effect of extreme mouse positions
// - filters out any mouse events outside of minecraft window
@SideOnly(Side.CLIENT)
public class MouseHelperOwn
extends MouseHelper
{

	private boolean doVanillaMovements = true;
    private long lastTimestamp = 0;
    
	// TODO: different left/right vs up/down?
	private float deadBorder = 0.05f;
	private float clipBorderHorizontal = 0.3f;
	private float clipBorderVertical = 0.2f;
	private boolean mHasPendingEvent = false; 
    
	// Turn vanilla mouse-viewpoint movements on/off
	// If they are off, we'll still process mouse events but just
	// not apply the view changes.
    public void setDoVanillaMovements(boolean doVanilla) {
		doVanillaMovements = doVanilla;
	}
    
    public void setClipBorders(float horz, float vert) {
    	clipBorderHorizontal = horz;
    	clipBorderVertical = vert;
    }
    
    public synchronized void consumePendingEvent() {
    	mHasPendingEvent = false;
    }
    
    public synchronized boolean hasPendingEvent() {
    	return mHasPendingEvent;
    }
    
	@Override
    public synchronized void mouseXYChange()
    {	
		if (Mouse.getEventNanoseconds() > lastTimestamp) {
			lastTimestamp = Mouse.getEventNanoseconds();

			if (Mouse.isGrabbed()) {
				int x = Mouse.getDX();
				int y = Mouse.getDY();
				this.setMousePosition(x, y);
			}
			else {
				int x = Mouse.getX() - Display.getWidth() / 2;
				int y = Mouse.getY() - Display.getHeight() / 2;
				this.setMousePosition(x, y);
			}
		}
		else {
			this.deltaX = 0;
			this.deltaY = 0;
			// consume deltas
			Mouse.getDX();
			Mouse.getDY();
		}
    }
		
	private void setMousePosition(int x, int y) {
		
		float x_abs = Math.abs(x);
		float y_abs = Math.abs(y);
		float w_half = (float) Display.getWidth() / 2;
		float h_half = (float) Display.getHeight() / 2;

		int deltaX = 0;
		int deltaY = 0;
		
		// If mouse is outside minecraft window, throw it away
		if (x_abs > w_half * (1 - deadBorder) ||
				y_abs > h_half * (1 - deadBorder)) {
			// do nothing
		}		
		else {
			// If mouse is around edges, clip effect
			if (x_abs > w_half * (1 - clipBorderHorizontal)) {
				x = (int) (Math.signum(x) * (w_half * (1 - clipBorderHorizontal)));
			}
			if (y_abs > h_half * (1 - clipBorderVertical)) {
				y = (int) (Math.signum(y) * (h_half * (1 - clipBorderVertical)));
			}
			deltaX = x;
			deltaY = y;

			// Carry out the change if we're doing vanilla movements
			if (doVanillaMovements) {
				this.deltaX = deltaX;
				this.deltaY = deltaY;
			}
			
			// Remember there was a valid event, even if we're not moving
			mHasPendingEvent = true;
		}
	}			
}