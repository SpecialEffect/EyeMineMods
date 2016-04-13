package com.specialeffect.mods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.lwjgl.opengl.GL11;

//
// StateOverlay implements a simple status bar at the top of the screen which 
// shows the current states such as attacking, walking, etc.
//
public class StateOverlay extends Gui
{
	private Minecraft mc;

	public StateOverlay(Minecraft mc)
	{
		super();

		// We need this to invoke the render engine.
		this.mc = mc;

		mResourcesLeft = new ArrayList<ResourceLocation>();
		mResourcesRight = new ArrayList<ResourceLocation>();
		mFlagsLeft = new ArrayList<Boolean>();
		mFlagsRight = new ArrayList<Boolean>();
		
	}
	
	private void rescale() {
		// Scale icon sizes to fit screen
		ScaledResolution res = new ScaledResolution( mc,
				mc.displayWidth, mc.displayHeight);
		mDisplayWidth = res.getScaledWidth();
		mDisplayHeight = res.getScaledHeight();
		int maxSizeByWidth = mDisplayWidth/(mIconsPerRow+mIconPadding);
		int maxSizeByHeight = 2*mDisplayHeight/(mIconsPerRow+mIconPadding);
		mIconSize = Math.min(maxSizeByWidth, maxSizeByHeight);
		//mIconSize = 18*2;
	}

	private static int mIconSize = 30;
	private static int mIconPadding = 5;
	private int mDisplayWidth;
	private int mDisplayHeight;
	private static final int mIconsPerRow = 10;

	// Lists of icons to draw on each half of screen
	private static List<ResourceLocation> mResourcesLeft;
	private static List<ResourceLocation> mResourcesRight;
	private static List<Boolean> mFlagsLeft;
	private static List<Boolean> mFlagsRight;

	// Add texture to list of icons, return position. 
	// You need to hang onto the position to later turn the
	// icon on/off.
	public static int registerTextureLeft(String filepath) {
		ResourceLocation res = new ResourceLocation(filepath);
		mResourcesLeft.add(res);
		mFlagsLeft.add(false);
		return mResourcesLeft.size()-1;
	}

	// Add texture to list of icons, return position. 
	// You need to hang onto the position to later turn the
	// icon on/off.
	public static int registerTextureRight(String filepath) {
		ResourceLocation res = new ResourceLocation(filepath);
		mResourcesRight.add(res);
		mFlagsRight.add(false);
		return mResourcesRight.size()-1;
	}

	// A helper function to draw a texture scaled to fit.
	private void drawScaledTexture(ResourceLocation res,
			int x, int y, 
			int width, int height)
	{
		this.mc.renderEngine.bindTexture(res);

		// Make sure icons stay pixelated rather than blurring.
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, 
				GL11.GL_TEXTURE_MIN_FILTER, 
				GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, 
				GL11.GL_TEXTURE_MAG_FILTER, 
				GL11.GL_NEAREST);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.startDrawingQuads();
		worldrenderer.addVertexWithUV(x        , y + height, 0, 0.0, 1.0);
		worldrenderer.addVertexWithUV(x + width, y + height, 0, 1.0, 1.0);
		worldrenderer.addVertexWithUV(x + width, y         , 0, 1.0, 0.0);
		worldrenderer.addVertexWithUV(x        , y         , 0, 0.0, 0.0);
		tessellator.draw();
	}

	// This event is called by GuiIngameForge during each frame by
	// GuiIngameForge.pre() and GuiIngameForce.post().
	@SubscribeEvent
	public void onRenderExperienceBar(RenderGameOverlayEvent event)
	{

		// We draw after the ExperienceBar has drawn.  The event raised by GuiIngameForge.pre()
		// will return true from isCancelable.  If you call event.setCanceled(true) in
		// that case, the portion of rendering which this event represents will be canceled.
		// We want to draw *after* the experience bar is drawn, so we make sure isCancelable() returns
		// false and that the eventType represents the ExperienceBar event.
		if(event.isCancelable() || event.type != ElementType.EXPERIENCE)
		{      
			return;
		}
		
		this.rescale();

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_LIGHTING); 
	

		// LEFT icons
		int xPos = mIconPadding;
		int yPos = mIconPadding;
		for (int i=0; i < mResourcesLeft.size(); i++) {
			if (mFlagsLeft.get(i)) {
				drawScaledTexture(mResourcesLeft.get(i), xPos, yPos, mIconSize, mIconSize);
			}
			xPos += mIconSize + mIconPadding;
		}
		
		// RIGHT ICONS
		xPos = mDisplayWidth - mIconSize - mIconPadding;
		for (int i=0; i < mResourcesRight.size(); i++) {
			if (mFlagsRight.get(i)) {
				drawScaledTexture(mResourcesRight.get(i), xPos, yPos, mIconSize, mIconSize);
			}
			xPos -= (mIconSize + mIconPadding);
		}
	}

	public static void setStateLeftIcon(int i, boolean b) {
		mFlagsLeft.set(i, b);	
	}

	public static void setStateRightIcon(int i, boolean b) {
		mFlagsRight.set(i, b);	
	}
}