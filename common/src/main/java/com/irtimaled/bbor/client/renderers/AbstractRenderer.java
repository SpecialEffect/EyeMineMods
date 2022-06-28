package com.irtimaled.bbor.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractRenderer {
	private static double TAU = 6.283185307179586D;
	private static double PI = TAU / 2D;

	public static void renderBlockFace(PoseStack poseStack, VertexConsumer vertexConsumer, BlockPos pos, Direction facing, Color color, int opacity) {
		OffsetPoint min = new OffsetPoint(pos.getX(), pos.getY(), pos.getZ());
		OffsetPoint max = new OffsetPoint(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);

		double minX = min.getX();
		double minY = min.getY();
		double minZ = min.getZ();

		double maxX = max.getX();
		double maxY = max.getY();
		double maxZ = max.getZ();

		Matrix4f matrix4f = poseStack.last().pose();
		final int colorRGB = color.getRGB();
		final float r = (float) (colorRGB >> 16 & 255) / 255.0F;
		final float g = (float) (colorRGB >> 8 & 255) / 255.0F;
		final float b = (float) (colorRGB & 255) / 255.0F;

		switch (facing) {
			case UP -> {
				maxY += 0.01;
				vertexConsumer.vertex(matrix4f, (float) minX, (float) maxY, (float) minZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) minX, (float) maxY, (float) maxZ).color(r, g, b, opacity).endVertex();
			}
			case DOWN -> {
				minY -= 0.01;
				vertexConsumer.vertex(matrix4f, (float) minX, (float) minY, (float) minZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) minY, (float) minZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) minY, (float) maxZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) minX, (float) minY, (float) maxZ).color(r, g, b, opacity).endVertex();
			}
			case NORTH -> {
				minZ -= 0.01;
				vertexConsumer.vertex(matrix4f, (float) minX, (float) minY, (float) minZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) minX, (float) maxY, (float) minZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) minY, (float) minZ).color(r, g, b, opacity).endVertex();
			}
			case SOUTH -> {
				maxZ += 0.01;
				vertexConsumer.vertex(matrix4f, (float) minX, (float) minY, (float) maxZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) minX, (float) maxY, (float) maxZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) minY, (float) maxZ).color(r, g, b, opacity).endVertex();
			}
			case EAST -> {
				maxX += 0.01;
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) minY, (float) minZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) minY, (float) maxZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, opacity).endVertex();
			}
			case WEST -> {
				minX -= 0.01;
				vertexConsumer.vertex(matrix4f, (float) minX, (float) minY, (float) minZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) minX, (float) minY, (float) maxZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) minX, (float) maxY, (float) maxZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) minX, (float) maxY, (float) minZ).color(r, g, b, opacity).endVertex();
			}
			default -> {
			}
		}
	}


	public static void renderBlockFaceCentralisedDwell(PoseStack poseStack, VertexConsumer vertexConsumer, BlockPos pos, Direction facing, Color color, double shrink, int opacity) {
		shrink = Math.min(shrink, 1.0f);
		shrink = Math.max(shrink, 0.0f);

		OffsetPoint min = new OffsetPoint(pos.getX(), pos.getY(), pos.getZ());
		OffsetPoint max = new OffsetPoint(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);

		double minX = min.getX();
		double minY = min.getY();
		double minZ = min.getZ();

		double maxX = max.getX();
		double maxY = max.getY();
		double maxZ = max.getZ();

		// Shrink in two axes, according to dwell
		double removeX = 0.5f * shrink * (maxX - minX);
		double removeY = 0.5f * shrink * (maxY - minY);
		double removeZ = 0.5f * shrink * (maxZ - minZ);

		Matrix4f matrix4f = poseStack.last().pose();
		final int colorRGB = color.getRGB();
		final float r = (float) (colorRGB >> 16 & 255) / 255.0F;
		final float g = (float) (colorRGB >> 8 & 255) / 255.0F;
		final float b = (float) (colorRGB & 255) / 255.0F;

		switch (facing) {
			case UP -> {
				maxY += 0.01;
				minX += removeX;
				maxX -= removeX;
				minZ += removeZ;
				maxZ -= removeZ;
				vertexConsumer.vertex(matrix4f, (float) minX, (float) maxY, (float) minZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) minX, (float) maxY, (float) maxZ).color(r, g, b, opacity).endVertex();
			}
			case DOWN -> {
				minY -= 0.01;
				minX += removeX;
				maxX -= removeX;
				minZ += removeZ;
				maxZ -= removeZ;
				vertexConsumer.vertex(matrix4f, (float) minX, (float) minY, (float) minZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) minY, (float) minZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) minY, (float) maxZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) minX, (float) minY, (float) maxZ).color(r, g, b, opacity).endVertex();
			}
			case NORTH -> {
				minZ -= 0.01;
				minX += removeX;
				maxX -= removeX;
				minY += removeY;
				maxY -= removeY;
				vertexConsumer.vertex(matrix4f, (float) minX, (float) minY, (float) minZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) minX, (float) maxY, (float) minZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) minY, (float) minZ).color(r, g, b, opacity).endVertex();
			}
			case SOUTH -> {
				maxZ += 0.01;
				minX += removeX;
				maxX -= removeX;
				minY += removeY;
				maxY -= removeY;
				vertexConsumer.vertex(matrix4f, (float) minX, (float) minY, (float) maxZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) minX, (float) maxY, (float) maxZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) minY, (float) maxZ).color(r, g, b, opacity).endVertex();
			}
			case EAST -> {
				maxX += 0.01;
				minY += removeY;
				maxY -= removeY;
				minZ += removeZ;
				maxZ -= removeZ;
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) minY, (float) minZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) minY, (float) maxZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, opacity).endVertex();
			}
			case WEST -> {
				minX -= 0.01;
				minY += removeY;
				maxY -= removeY;
				minZ += removeZ;
				maxZ -= removeZ;
				vertexConsumer.vertex(matrix4f, (float) minX, (float) minY, (float) minZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) minX, (float) minY, (float) maxZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) minX, (float) maxY, (float) maxZ).color(r, g, b, opacity).endVertex();
				vertexConsumer.vertex(matrix4f, (float) minX, (float) maxY, (float) minZ).color(r, g, b, opacity).endVertex();
			}
			default -> {
			}
		}
	}


	public static void renderCubeAtPosition(PoseStack poseStack, VertexConsumer vertexConsumer, Vec3 pos, Color color, int opacity, double size) {

//		RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
//		RenderSystem.enableBlend();
//		RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

		// Set up bounding cube corners
		OffsetPoint min = new OffsetPoint(pos.x() - size, pos.y() - size, pos.z() - size);
		OffsetPoint max = new OffsetPoint(pos.x() + size, pos.y() + size, pos.z() + size);

		float minX = (float) min.getX();
		float minY = (float) min.getY();
		float minZ = (float) min.getZ();

		float maxX = (float) max.getX();
		float maxY = (float) max.getY();
		float maxZ = (float) max.getZ();

		Matrix4f matrix4f = poseStack.last().pose();
		final int colorRGB = color.getRGB();
		final float r = (float) (colorRGB >> 16 & 255) / 255.0F;
		final float g = (float) (colorRGB >> 8 & 255) / 255.0F;
		final float b = (float) (colorRGB & 255) / 255.0F;

		// Render a quad for each face

		vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(r, g, b, opacity).endVertex();
		vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(r, g, b, opacity).endVertex();
		vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(r, g, b, opacity).endVertex();
		vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(r, g, b, opacity).endVertex();

		vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(r, g, b, opacity).endVertex();
		vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(r, g, b, opacity).endVertex();
		vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(r, g, b, opacity).endVertex();
		vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(r, g, b, opacity).endVertex();

		vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(r, g, b, opacity).endVertex();
		vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(r, g, b, opacity).endVertex();
		vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(r, g, b, opacity).endVertex();
		vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(r, g, b, opacity).endVertex();

		vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(r, g, b, opacity).endVertex();
		vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(r, g, b, opacity).endVertex();
		vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(r, g, b, opacity).endVertex();
		vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(r, g, b, opacity).endVertex();

		vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(r, g, b, opacity).endVertex();
		vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(r, g, b, opacity).endVertex();
		vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(r, g, b, opacity).endVertex();
		vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(r, g, b, opacity).endVertex();

//		RenderSystem.disableBlend();
//		RenderSystem.enablePolygonOffset();
//		RenderSystem.polygonOffset(-1.f, -1.f);
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

		if (minX != maxX && minZ != maxZ) {
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

		if (minX != maxX && minY != maxY) {
			renderer.addPoint(minX, minY, maxZ)
					.addPoint(minX, maxY, maxZ)
					.addPoint(maxX, maxY, maxZ)
					.addPoint(maxX, minY, maxZ);

			if (minZ != maxZ) {
				renderer.addPoint(minX, minY, minZ)
						.addPoint(minX, maxY, minZ)
						.addPoint(maxX, maxY, minZ)
						.addPoint(maxX, minY, minZ);
			}
		}
		if (minY != maxY && minZ != maxZ) {
			renderer.addPoint(minX, minY, minZ)
					.addPoint(minX, minY, maxZ)
					.addPoint(minX, maxY, maxZ)
					.addPoint(minX, maxY, minZ);

			if (minX != maxX) {
				renderer.addPoint(maxX, minY, minZ)
						.addPoint(maxX, minY, maxZ)
						.addPoint(maxX, maxY, maxZ)
						.addPoint(maxX, maxY, minZ);
			}
		}
		renderer.render();
	}

	void renderLine(OffsetPoint startPoint, OffsetPoint endPoint, Color color) {
		RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

		Renderer.startLines()
				.setColor(color)
				.addPoint(startPoint)
				.addPoint(endPoint)
				.render();
	}

	void renderFilledFaces(OffsetPoint min, OffsetPoint max, Color color, int alpha) {
		RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		RenderSystem.enableBlend();

		renderFaces(min, max, color, alpha);

		RenderSystem.disableBlend();
		RenderSystem.enablePolygonOffset();
		RenderSystem.polygonOffset(-1.f, -1.f);
	}

	void renderText(PoseStack poseStack, OffsetPoint offsetPoint, String... texts) {
		Font fontRenderer = Minecraft.getInstance().font;

//		RenderSystem.pushMatrix(); TODO: Redo when renderText() is required
//		RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
//		RenderSystem.translated(offsetPoint.getX(), offsetPoint.getY() + 0.002D, offsetPoint.getZ());
//		GL11.glNormal3f(0.0F, 1.0F, 0.0F);
//		RenderSystem.rotatef(0.0F, 0.0F, 1.0F, 0.0F);
//		RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
//		RenderSystem.scalef(-0.0175F, -0.0175F, 0.0175F);
//		RenderSystem.enableTexture();
//
//		RenderSystem.enableBlend();
//		RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
//
//		RenderSystem.disableDepthTest();
//		RenderSystem.enableDepthTest();
//		RenderSystem.depthMask(true);
//        float top = -(fontRenderer.lineHeight * texts.length) / 2f;
//        for (String text : texts) {
//            float left = fontRenderer.width(text) / 2f;
//            fontRenderer.draw(poseStack, text, -left, top, -1);
//            top += fontRenderer.lineHeight;
//        }
//		RenderSystem.disableTexture();
//		RenderSystem.disableBlend();
	}

	private static void enablePointSmooth() {
		RenderSystem.assertOnRenderThread();
		GL11.glEnable(GL11.GL_POINT_SMOOTH);
	}

	private static void pointSize(float dotSize) {
		RenderSystem.assertOnRenderThread();
		GL11.glPointSize(dotSize);
	}

	void renderSphere(OffsetPoint center, double radius, Color color, int density, int dotSize) {
		enablePointSmooth();
//		GL11.glEnable(GL11.GL_POINT_SMOOTH);
		pointSize(dotSize);
//		GL11.glPointSize(dotSize);
//        Renderer renderer = Renderer.startPoints() TODO: Redo when renderSphere is required
//                .setColor(color);
//        buildPoints(center, radius, density)
//                .forEach(renderer::addPoint);
//        renderer.render();
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
