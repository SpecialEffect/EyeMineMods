package com.specialeffect.mods.mousehandling;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.util.MouseSmoother;
import net.minecraft.client.util.NativeUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// Based on the vanilla MouseHelper in forge, but also:
// - allows normal mouse control with an ungrabbed mouse
// - reduces the effect of extreme mouse positions
// - filters out any mouse events outside of minecraft window
@OnlyIn(Dist.CLIENT)
public class MouseHelperOwn
extends MouseHelper
{

	public MouseHelperOwn(Minecraft minecraftIn) {
		super(minecraftIn);
		minecraft = minecraftIn;		
	}

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
    
    private final Minecraft minecraft;
    private boolean leftDown;
    private boolean middleDown;
    private boolean rightDown;
    private double mouseX;
    private double mouseY;
    private int simulatedRightClicks;
    private int activeButton = -1;
    private boolean ignoreFirstMove = true;
    private int touchScreenCounter;
    private double eventTime;
    private final MouseSmoother xSmoother = new MouseSmoother();
    private final MouseSmoother ySmoother = new MouseSmoother();
    private double xVelocity;
    private double yVelocity;
    private double accumulatedScrollDelta;
    private double lastLookTime = Double.MIN_VALUE;
    private boolean mouseGrabbed;

    
    /** public entry points for automated cursor actions **/
   
    public void moveCursor(double xpos, double ypos) {
    	long handle = Minecraft.getInstance().mainWindow.getHandle();
    	this.cursorPosCallbackOwn(handle, xpos, ypos);
    }
    
    public void leftMouseClickAtPosition(double xpos, double ypos) {
    	this.moveCursor(xpos, ypos);
    	this.mouseButton(GLFW.GLFW_MOUSE_BUTTON_1, GLFW.GLFW_PRESS, 0);
    	this.mouseButton(GLFW.GLFW_MOUSE_BUTTON_1, GLFW.GLFW_RELEASE, 0);
    }
    
    public void leftShiftMouseClickAtPosition(double xpos, double ypos) {
    	this.moveCursor(xpos, ypos);
    	this.mouseButton(GLFW.GLFW_MOUSE_BUTTON_1, GLFW.GLFW_PRESS, GLFW.GLFW_MOD_SHIFT);
    	this.mouseButton(GLFW.GLFW_MOUSE_BUTTON_1, GLFW.GLFW_RELEASE, GLFW.GLFW_MOD_SHIFT);
    }  
    
    
    /* Move cursor to location and perform mouse action
     * button: any constant GLFW.GLFW_MOUSE_BUTTON_X 
     * action: GLFW.GLFW_PRESS or GLFW.GLFW_RELEASE
     * mods: GLFW.GLFW_MOD_[SHIFT/CONTROL/ALT/SUPER]
     */     
    public void mouseButton(int button, int action, int mods) {    	
    	long handle = Minecraft.getInstance().mainWindow.getHandle();
    	this.mouseButtonCallbackOwn(handle, button, action, mods);
    }       
    

    /**
     * Will be called when a mouse button is pressed or released.
     *  
     * @see GLFWMouseButtonCallbackI
     */
    private void mouseButtonCallbackOwn(long handle, int button, int action, int mods) {
       if (handle == this.minecraft.mainWindow.getHandle()) {
          boolean flag = action == 1;
          if (Minecraft.IS_RUNNING_ON_MAC && button == 0) {
             if (flag) {
                if ((mods & 2) == 2) {
                   button = 1;
                   ++this.simulatedRightClicks;
                }
             } else if (this.simulatedRightClicks > 0) {
                button = 1;
                --this.simulatedRightClicks;
             }
          }

          if (flag) {
             if (this.minecraft.gameSettings.touchscreen && this.touchScreenCounter++ > 0) {
                return;
             }

             this.activeButton = button;
             this.eventTime = NativeUtil.func_216394_b();
          } else if (this.activeButton != -1) {
             if (this.minecraft.gameSettings.touchscreen && --this.touchScreenCounter > 0) {
                return;
             }

             this.activeButton = -1;
          }

          boolean[] aboolean = new boolean[]{false};
          if (this.minecraft.loadingGui == null) {
             if (this.minecraft.currentScreen == null) {
                if (!this.mouseGrabbed && flag) {
                   this.grabMouse();
                }
             } else {
                double d0 = this.mouseX * (double)this.minecraft.mainWindow.getScaledWidth() / (double)this.minecraft.mainWindow.getWidth();
                double d1 = this.mouseY * (double)this.minecraft.mainWindow.getScaledHeight() / (double)this.minecraft.mainWindow.getHeight();
                int p_198023_3_f = button;
                if (flag) {
                   Screen.wrapScreenError(() -> {
                      aboolean[0] = net.minecraftforge.client.ForgeHooksClient.onGuiMouseClickedPre(this.minecraft.currentScreen, d0, d1, p_198023_3_f);
                      if (!aboolean[0]) aboolean[0] = this.minecraft.currentScreen.mouseClicked(d0, d1, p_198023_3_f);
                      if (!aboolean[0]) aboolean[0] = net.minecraftforge.client.ForgeHooksClient.onGuiMouseClickedPost(this.minecraft.currentScreen, d0, d1, p_198023_3_f);
                   }, "mouseClicked event handler", this.minecraft.currentScreen.getClass().getCanonicalName());
                } else {
                   Screen.wrapScreenError(() -> {
                      aboolean[0] = net.minecraftforge.client.ForgeHooksClient.onGuiMouseReleasedPre(this.minecraft.currentScreen, d0, d1, p_198023_3_f);
                      if (!aboolean[0]) aboolean[0] = this.minecraft.currentScreen.mouseReleased(d0, d1, p_198023_3_f);
                      if (!aboolean[0]) aboolean[0] = net.minecraftforge.client.ForgeHooksClient.onGuiMouseReleasedPost(this.minecraft.currentScreen, d0, d1, p_198023_3_f);
                   }, "mouseReleased event handler", this.minecraft.currentScreen.getClass().getCanonicalName());
                }
             }
          }

          if (!aboolean[0] && (this.minecraft.currentScreen == null || this.minecraft.currentScreen.passEvents) && this.minecraft.loadingGui == null) {
             if (button == 0) {
                this.leftDown = flag;
             } else if (button == 2) {
                this.middleDown = flag;
             } else if (button == 1) {
                this.rightDown = flag;
             }

             KeyBinding.setKeyBindState(InputMappings.Type.MOUSE.getOrMakeInput(button), flag);
             if (flag) {
                if (this.minecraft.player.isSpectator() && button == 2) {
                   this.minecraft.ingameGUI.getSpectatorGui().onMiddleClick();
                } else {
                   KeyBinding.onTick(InputMappings.Type.MOUSE.getOrMakeInput(button));
                }
             }
          }
          net.minecraftforge.client.ForgeHooksClient.fireMouseInput(button, action, mods);
       }
    }

    /**
     * Will be called when a scrolling device is used, such as a mouse wheel or scrolling area of a touchpad.
     *  
     * @see GLFWScrollCallbackI
     */
    private void scrollCallbackOwn(long handle, double xoffset, double yoffset) {
       if (handle == Minecraft.getInstance().mainWindow.getHandle()) {
          double d0 = (this.minecraft.gameSettings.discreteMouseScroll ? Math.signum(yoffset) : yoffset) * this.minecraft.gameSettings.mouseWheelSensitivity;
          if (this.minecraft.loadingGui == null) {
             if (this.minecraft.currentScreen != null) {
                double d1 = this.mouseX * (double)this.minecraft.mainWindow.getScaledWidth() / (double)this.minecraft.mainWindow.getWidth();
                double d2 = this.mouseY * (double)this.minecraft.mainWindow.getScaledHeight() / (double)this.minecraft.mainWindow.getHeight();
                if (net.minecraftforge.client.ForgeHooksClient.onGuiMouseScrollPre(this, this.minecraft.currentScreen, d0)) return;
                if (this.minecraft.currentScreen.mouseScrolled(d1, d2, d0)) return;
                net.minecraftforge.client.ForgeHooksClient.onGuiMouseScrollPost(this, this.minecraft.currentScreen, d0);
             } else if (this.minecraft.player != null) {
                if (this.accumulatedScrollDelta != 0.0D && Math.signum(d0) != Math.signum(this.accumulatedScrollDelta)) {
                   this.accumulatedScrollDelta = 0.0D;
                }

                this.accumulatedScrollDelta += d0;
                float f1 = (float)((int)this.accumulatedScrollDelta);
                if (f1 == 0.0F) {
                   return;
                }

                this.accumulatedScrollDelta -= (double)f1;
                if (net.minecraftforge.client.ForgeHooksClient.onMouseScroll(this, d0)) return;
                if (this.minecraft.player.isSpectator()) {
                   if (this.minecraft.ingameGUI.getSpectatorGui().isMenuActive()) {
                      this.minecraft.ingameGUI.getSpectatorGui().onMouseScroll((double)(-f1));
                   } else {
                      float f = MathHelper.clamp(this.minecraft.player.abilities.getFlySpeed() + f1 * 0.005F, 0.0F, 0.2F);
                      this.minecraft.player.abilities.setFlySpeed(f);
                   }
                } else {
                   this.minecraft.player.inventory.changeCurrentItem((double)f1);
                }
             }
          }
       }

    }

    public void registerCallbacks(long handle) {
       InputMappings.func_216503_a(handle, this::cursorPosCallbackOwn, this::mouseButtonCallbackOwn, this::scrollCallbackOwn);
    }
    
    @SuppressWarnings("unused")
	private void debugLogging() {

        long cursorMode = GLFW.glfwGetInputMode(Minecraft.getInstance().mainWindow.getHandle(), GLFW.GLFW_CURSOR);
        System.out.println("***");
        if (cursorMode == GLFW.GLFW_CURSOR_DISABLED) {
      	  System.out.println("cursor disabled");
      	  long raw = GLFW.glfwGetInputMode(Minecraft.getInstance().mainWindow.getHandle(), GLFW.GLFW_RAW_MOUSE_MOTION);
      	  if (raw == GLFW.GLFW_TRUE) {
      		  System.out.println("using raw motion");
      	  }
        }
        else if (cursorMode == GLFW.GLFW_CURSOR_NORMAL) {
      	  System.out.println("normal cursor");
        }
        
        if (GLFW.GLFW_TRUE == GLFW.glfwGetWindowAttrib(Minecraft.getInstance().mainWindow.getHandle(), GLFW.GLFW_HOVERED))
        {
      	  System.out.println("hovered");
        }
    }

    /**
     * Will be called when the cursor is moved.
     *  
     * <p>The callback function receives the cursor position, measured in screen coordinates but relative to the top-left
     * corner of the window client area. On platforms that provide it, the full sub-pixel cursor position is passed
     * on.</p>
     *  
     * @see GLFWCursorPosCallbackI
     */
    private void cursorPosCallbackOwn(long handle, double xpos, double ypos) {
    	
       if (handle == Minecraft.getInstance().mainWindow.getHandle()) {

          if (this.ignoreFirstMove) {
             this.mouseX = xpos;
             this.mouseY = ypos;
             this.ignoreFirstMove = false;
             return;
          }
         
          IGuiEventListener iguieventlistener = this.minecraft.currentScreen;
          if (iguieventlistener != null && this.minecraft.loadingGui == null) {
        	 GLFW.glfwSetInputMode(Minecraft.getInstance().mainWindow.getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
             double d0 = xpos * (double)this.minecraft.mainWindow.getScaledWidth() / (double)this.minecraft.mainWindow.getWidth();
             double d1 = ypos * (double)this.minecraft.mainWindow.getScaledHeight() / (double)this.minecraft.mainWindow.getHeight();
             Screen.wrapScreenError(() -> {
                iguieventlistener.mouseMoved(d0, d1);
             }, "mouseMoved event handler", iguieventlistener.getClass().getCanonicalName());
             if (this.activeButton != -1 && this.eventTime > 0.0D) {
                double d2 = (xpos - this.mouseX) * (double)this.minecraft.mainWindow.getScaledWidth() / (double)this.minecraft.mainWindow.getWidth();
                double d3 = (ypos - this.mouseY) * (double)this.minecraft.mainWindow.getScaledHeight() / (double)this.minecraft.mainWindow.getHeight();
                Screen.wrapScreenError(() -> {
                if (net.minecraftforge.client.ForgeHooksClient.onGuiMouseDragPre(this.minecraft.currentScreen, d0, d1, this.activeButton, d2, d3)) return;
                if (iguieventlistener.mouseDragged(d0, d1, this.activeButton, d2, d3)) return;
                net.minecraftforge.client.ForgeHooksClient.onGuiMouseDragPost(this.minecraft.currentScreen, d0, d1, this.activeButton, d2, d3);
                }, "mouseDragged event handler", iguieventlistener.getClass().getCanonicalName());
             }
             this.mouseX = xpos;
             this.mouseY = ypos;
          }
          else {
    
	          this.minecraft.getProfiler().startSection("mouse");
	          if (this.isMouseGrabbed() && this.minecraft.isGameFocused()) {
	             this.xVelocity += xpos - this.mouseX;
	             this.yVelocity += ypos - this.mouseY;
	             this.processMousePosition(xpos, ypos);
	          }
	
	          this.updatePlayerLook();
	          	          
	          // Reset to centre
	          GLFW.glfwSetCursorPos(Minecraft.getInstance().mainWindow.getHandle(), 0, 0);
	          this.mouseX = 0;
	          this.mouseY = 0;
	          
	          this.minecraft.getProfiler().endSection();
          }
       }
    }
    
    private void processMousePosition(double x, double y) {
		
		double x_abs = Math.abs(x);
		double y_abs = Math.abs(y);
		double w_half = this.minecraft.mainWindow.getWidth() / 2;
		double h_half = this.minecraft.mainWindow.getHeight() / 2;

		double deltaX = 0;
		double deltaY = 0;
		
		// If mouse is outside minecraft window, throw it away
		if (x_abs > w_half * (1 - deadBorder) ||
				y_abs > h_half * (1 - deadBorder)) {
			// do nothing
			this.xVelocity = 0;
			this.yVelocity = 0;
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

			this.xVelocity = deltaX;
			this.yVelocity = deltaY;
			
			// Remember there was a valid event, even if we're not moving
			mHasPendingEvent = true;
		}
	}	

    public void updatePlayerLook() {
       double d0 = NativeUtil.func_216394_b();
       double d1 = d0 - this.lastLookTime;
       this.lastLookTime = d0;
       if (this.isMouseGrabbed() && this.minecraft.isGameFocused()) {
          double d4 = this.minecraft.gameSettings.mouseSensitivity * (double)0.6F + (double)0.2F;
          double d5 = d4 * d4 * d4 * 8.0D;
          double d2;
          double d3;
          if (this.minecraft.gameSettings.smoothCamera) {
             double d6 = this.xSmoother.smooth(this.xVelocity * d5, d1 * d5);
             double d7 = this.ySmoother.smooth(this.yVelocity * d5, d1 * d5);
             d2 = d6;
             d3 = d7;
          } else {
             this.xSmoother.reset();
             this.ySmoother.reset();
             d2 = this.xVelocity * d5;
             d3 = this.yVelocity * d5;
          }

          this.xVelocity = 0.0D;
          this.yVelocity = 0.0D;
          int i = 1;
          if (this.minecraft.gameSettings.invertMouse) {
             i = -1;
          }

          this.minecraft.getTutorial().onMouseMove(d2, d3);
          if (this.minecraft.player != null) {
             this.minecraft.player.rotateTowards(d2, d3 * (double)i);
          }

       } else {
          this.xVelocity = 0.0D;
          this.yVelocity = 0.0D;
       }
    }

    public boolean isLeftDown() {
       return this.leftDown;
    }

    public boolean isRightDown() {
       return this.rightDown;
    }

    public boolean isMiddleDown() {
       return this.middleDown;
    }

    public double getMouseX() {
       return this.mouseX;
    }

    public double getMouseY() {
       return this.mouseY;
    }

    public double getXVelocity() {
       return this.xVelocity;
    }

    public double getYVelocity() {
       return this.yVelocity;
    }

    public void setIgnoreFirstMove() {
       this.ignoreFirstMove = true;
    }

    /**
     * Returns true if the mouse is grabbed.
     */
    public boolean isMouseGrabbed() {
       return this.mouseGrabbed;
    }

    /**
     * Will set the focus to ingame if the Minecraft window is the active with focus. Also clears any GUI screen
     * currently displayed
     */
    public void grabMouse() {
       if (this.minecraft.isGameFocused()) {
          if (!this.mouseGrabbed) {
             if (!Minecraft.IS_RUNNING_ON_MAC) {
                KeyBinding.updateKeyBindState();
             }

             this.mouseGrabbed = true;
             this.mouseX = (double)(this.minecraft.mainWindow.getWidth() / 2);
             this.mouseY = (double)(this.minecraft.mainWindow.getHeight() / 2);
             InputMappings.func_216504_a(this.minecraft.mainWindow.getHandle(), 212995, this.mouseX, this.mouseY);
             this.minecraft.displayGuiScreen((Screen)null);
             //FIXME: not visible this.minecraft.leftClickCounter = 10000;             
             this.ignoreFirstMove = true;
          }
       }
    }

    /**
     * Resets the player keystate, disables the ingame focus, and ungrabs the mouse cursor.
     */
    public void ungrabMouse() {
       if (this.mouseGrabbed) {
          this.mouseGrabbed = false;
          this.mouseX = (double)(this.minecraft.mainWindow.getWidth() / 2);
          this.mouseY = (double)(this.minecraft.mainWindow.getHeight() / 2);
          InputMappings.func_216504_a(this.minecraft.mainWindow.getHandle(), 212993, this.mouseX, this.mouseY);
       }
    }
}