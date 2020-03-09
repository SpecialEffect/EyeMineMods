package com.irtimaled.bbor.client.renderers;

import com.mojang.blaze3d.platform.GLX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractRenderer {
    private static double TAU = 6.283185307179586D;
    private static double PI = TAU / 2D;

    public static void renderBlockFace(BlockPos pos, Direction facing, Color color, int alpha) {

    	GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glEnable(GL11.GL_BLEND);
        GLX.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        {
			OffsetPoint min = new OffsetPoint(pos.getX(), pos.getY(), pos.getZ());
			OffsetPoint max = new OffsetPoint(pos.getX()+1, pos.getY()+1, pos.getZ()+1);
	    	
	    	double minX = min.getX();
	        double minY = min.getY();
	        double minZ = min.getZ();
	
	        double maxX = max.getX();
	        double maxY = max.getY();
	        double maxZ = max.getZ();
	
	        Renderer renderer = Renderer.startQuads()
	                .setColor(color)
	                .setAlpha(alpha);
	
	        switch (facing) {
	        case UP:
	        	maxY += 0.01;
	        	renderer.addPoint(minX, maxY, minZ)
			            .addPoint(maxX, maxY, minZ)
			            .addPoint(maxX, maxY, maxZ)
			            .addPoint(minX, maxY, maxZ);
	        	break;
	        case DOWN:
	        	minY -= 0.01;
	        	renderer.addPoint(minX, minY, minZ)
			            .addPoint(maxX, minY, minZ)
			            .addPoint(maxX, minY, maxZ)
			            .addPoint(minX, minY, maxZ);
	        	break;
			case NORTH:
				minZ -= 0.01;
				renderer.addPoint(minX, minY, minZ)
			            .addPoint(minX, maxY, minZ)
			            .addPoint(maxX, maxY, minZ)
			            .addPoint(maxX, minY, minZ);
				break;
			case SOUTH:
				maxZ += 0.01;
				renderer.addPoint(minX, minY, maxZ)
			            .addPoint(minX, maxY, maxZ)
			            .addPoint(maxX, maxY, maxZ)
			            .addPoint(maxX, minY, maxZ);
				break;
	        case EAST:
	        	maxX += 0.01;
	        	renderer.addPoint(maxX, minY, minZ)
			            .addPoint(maxX, minY, maxZ)
			            .addPoint(maxX, maxY, maxZ)
			            .addPoint(maxX, maxY, minZ);
	        	break;
			case WEST:
				minX -= 0.01;
				renderer.addPoint(minX, minY, minZ)
			            .addPoint(minX, minY, maxZ)
			            .addPoint(minX, maxY, maxZ)
			            .addPoint(minX, maxY, minZ);
				break;
			default:
				break;
	        }
        
	        renderer.render();
        }
        
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_LINE);
        GL11.glPolygonOffset(-1.f, -1.f);
    }


    public static void renderBlockFaceCentralisedDwell(BlockPos pos, Direction facing, Color color, double shrink, int opacity) {    
    	
    	shrink = Math.min(shrink, 1.0f);
    	shrink = Math.max(shrink, 0.0f);
    	
    	GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glEnable(GL11.GL_BLEND);
        GLX.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        {
			OffsetPoint min = new OffsetPoint(pos.getX(), pos.getY(), pos.getZ());
			OffsetPoint max = new OffsetPoint(pos.getX()+1, pos.getY()+1, pos.getZ()+1);
	    	
	    	double minX = min.getX();
	        double minY = min.getY();
	        double minZ = min.getZ();
	
	        double maxX = max.getX();
	        double maxY = max.getY();
	        double maxZ = max.getZ();
	        
	        // Shrink in two axes, according to dwell	       
	        double removeX = 0.5f*shrink*(maxX - minX);
	        double removeY = 0.5f*shrink*(maxY - minY);
	        double removeZ = 0.5f*shrink*(maxZ - minZ);
	
	        Renderer renderer = Renderer.startQuads()
	                .setColor(color)
	                .setAlpha(opacity);
	
	        switch (facing) {
	        case UP:
	        	maxY += 0.01;
	        	
	        	minX += removeX;
		        maxX -= removeX;
		        
		        minZ += removeZ;
		        maxZ -= removeZ;
		        
	        	renderer.addPoint(minX, maxY, minZ)
			            .addPoint(maxX, maxY, minZ)
			            .addPoint(maxX, maxY, maxZ)
			            .addPoint(minX, maxY, maxZ);
	        	break;
	        case DOWN:
	        	minY -= 0.01;
	        	
	        	minX += removeX;
		        maxX -= removeX;
		        
		        minZ += removeZ;
		        maxZ -= removeZ;
		        
	        	renderer.addPoint(minX, minY, minZ)
			            .addPoint(maxX, minY, minZ)
			            .addPoint(maxX, minY, maxZ)
			            .addPoint(minX, minY, maxZ);
	        	break;
			case NORTH:
				minZ -= 0.01;
				
				minX += removeX;
		        maxX -= removeX;
		        
		        minY += removeY;
		        maxY -= removeY;
		        
				renderer.addPoint(minX, minY, minZ)
			            .addPoint(minX, maxY, minZ)
			            .addPoint(maxX, maxY, minZ)
			            .addPoint(maxX, minY, minZ);
				break;
			case SOUTH:
				maxZ += 0.01;
				
				minX += removeX;
		        maxX -= removeX;
		        
		        minY += removeY;
		        maxY -= removeY;
		        
				renderer.addPoint(minX, minY, maxZ)
			            .addPoint(minX, maxY, maxZ)
			            .addPoint(maxX, maxY, maxZ)
			            .addPoint(maxX, minY, maxZ);
				break;
	        case EAST:
	        	maxX += 0.01;
	        	
		        minY += removeY;
		        maxY -= removeY;
		        
		        minZ += removeZ;
		        maxZ -= removeZ;
		        
	        	renderer.addPoint(maxX, minY, minZ)
			            .addPoint(maxX, minY, maxZ)
			            .addPoint(maxX, maxY, maxZ)
			            .addPoint(maxX, maxY, minZ);
	        	break;
			case WEST:
				minX -= 0.01;
				
		        minY += removeY;
		        maxY -= removeY;
		        
		        minZ += removeZ;
		        maxZ -= removeZ;
		        
				renderer.addPoint(minX, minY, minZ)
			            .addPoint(minX, minY, maxZ)
			            .addPoint(minX, maxY, maxZ)
			            .addPoint(minX, maxY, minZ);
				break;
			default:
				break;
	        }
        
	        renderer.render();
        }
        
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_LINE);
        GL11.glPolygonOffset(-1.f, -1.f);
    }

    
    public static void renderFaces(OffsetPoint min, OffsetPoint max, Color color, int alpha) {
        double minX = min.getX();
        double minY = min.getY();
        double minZ = min.getZ();

        double maxX = max.getX();
        double maxY = max.getY();
        double maxZ = max.getZ();

        Renderer renderer = Renderer.startQuads()
                .setColor(color)
                .setAlpha(alpha);

        if(minX != maxX && minZ != maxZ) {
            renderer.addPoint(minX, minY, minZ)
                    .addPoint(maxX, minY, minZ)
                    .addPoint(maxX, minY, maxZ)
                    .addPoint(minX, minY, maxZ);

            if (minY != maxY) {
                renderer.addPoint(minX, maxY, minZ)
                        .addPoint(maxX, maxY, minZ)
                        .addPoint(maxX, maxY, maxZ)
                        .addPoint(minX, maxY, maxZ);
            }
        }

        if(minX != maxX && minY != maxY) {
            renderer.addPoint(minX, minY, maxZ)
                    .addPoint(minX, maxY, maxZ)
                    .addPoint(maxX, maxY, maxZ)
                    .addPoint(maxX, minY, maxZ);

            if(minZ != maxZ) {
                renderer.addPoint(minX, minY, minZ)
                        .addPoint(minX, maxY, minZ)
                        .addPoint(maxX, maxY, minZ)
                        .addPoint(maxX, minY, minZ);
            }
        }
        if(minY != maxY && minZ != maxZ) {
            renderer.addPoint(minX, minY, minZ)
                    .addPoint(minX, minY, maxZ)
                    .addPoint(minX, maxY, maxZ)
                    .addPoint(minX, maxY, minZ);

            if(minX != maxX) {
                renderer.addPoint(maxX, minY, minZ)
                        .addPoint(maxX, minY, maxZ)
                        .addPoint(maxX, maxY, maxZ)
                        .addPoint(maxX, maxY, minZ);
            }
        }
        renderer.render();
    }

    void renderLine(OffsetPoint startPoint, OffsetPoint endPoint, Color color) {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        Renderer.startLines()
                .setColor(color)
                .addPoint(startPoint)
                .addPoint(endPoint)
                .render();
    }

    void renderFilledFaces(OffsetPoint min, OffsetPoint max, Color color, int alpha) {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glEnable(GL11.GL_BLEND);
        renderFaces(min, max, color, alpha);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_LINE);
        GL11.glPolygonOffset(-1.f, -1.f);
    }

    void renderText(OffsetPoint offsetPoint, String... texts) {
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;

        GL11.glPushMatrix();
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glTranslated(offsetPoint.getX(), offsetPoint.getY() + 0.002D, offsetPoint.getZ());
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(0.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.0175F, -0.0175F, 0.0175F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GLX.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        float top = -(fontRenderer.FONT_HEIGHT * texts.length) / 2f;
        for (String text : texts) {
            float left = fontRenderer.getStringWidth(text) / 2f;
            fontRenderer.drawString(text, -left, top, -1);
            top += fontRenderer.FONT_HEIGHT;
        }
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    void renderSphere(OffsetPoint center, double radius, Color color, int density, int dotSize) {
        GL11.glEnable(GL11.GL_POINT_SMOOTH);
        GL11.glPointSize(dotSize);
        Renderer renderer = Renderer.startPoints()
                .setColor(color);
        buildPoints(center, radius, density)
                .forEach(renderer::addPoint);
        renderer.render();
    }

    private Set<OffsetPoint> buildPoints(OffsetPoint center, double radius, int density) {
        int segments = 24 + (density * 8);

        Set<OffsetPoint> points = new HashSet<>(segments * segments);

        double thetaSegment = PI / (double) segments;
        double phiSegment = TAU / (double) segments;

        for (double phi = 0.0D; phi < TAU; phi += phiSegment) {
            for (double theta = 0.0D; theta < PI; theta += thetaSegment) {
                double dx = radius * Math.sin(phi) * Math.cos(theta);
                double dz = radius * Math.sin(phi) * Math.sin(theta);
                double dy = radius * Math.cos(phi);

                points.add(center.offset(dx, dy, dz));
            }
        }
        return points;
    }
}
