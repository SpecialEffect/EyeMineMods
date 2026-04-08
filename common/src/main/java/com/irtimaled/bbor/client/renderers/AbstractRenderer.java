package com.irtimaled.bbor.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.awt.Color;

public abstract class AbstractRenderer {

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
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) maxY, (float) minZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) maxY, (float) minZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) maxY, (float) maxZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) maxY, (float) maxZ).setColor(r, g, b, opacity);
			}
			case DOWN -> {
				minY -= 0.01;
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) minY, (float) minZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) minY, (float) minZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) minY, (float) maxZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) minY, (float) maxZ).setColor(r, g, b, opacity);
			}
			case NORTH -> {
				minZ -= 0.01;
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) minY, (float) minZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) maxY, (float) minZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) maxY, (float) minZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) minY, (float) minZ).setColor(r, g, b, opacity);
			}
			case SOUTH -> {
				maxZ += 0.01;
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) minY, (float) maxZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) maxY, (float) maxZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) maxY, (float) maxZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) minY, (float) maxZ).setColor(r, g, b, opacity);
			}
			case EAST -> {
				maxX += 0.01;
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) minY, (float) minZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) minY, (float) maxZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) maxY, (float) maxZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) maxY, (float) minZ).setColor(r, g, b, opacity);
			}
			case WEST -> {
				minX -= 0.01;
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) minY, (float) minZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) minY, (float) maxZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) maxY, (float) maxZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) maxY, (float) minZ).setColor(r, g, b, opacity);
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
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) maxY, (float) minZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) maxY, (float) minZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) maxY, (float) maxZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) maxY, (float) maxZ).setColor(r, g, b, opacity);
			}
			case DOWN -> {
				minY -= 0.01;
				minX += removeX;
				maxX -= removeX;
				minZ += removeZ;
				maxZ -= removeZ;
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) minY, (float) minZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) minY, (float) minZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) minY, (float) maxZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) minY, (float) maxZ).setColor(r, g, b, opacity);
			}
			case NORTH -> {
				minZ -= 0.01;
				minX += removeX;
				maxX -= removeX;
				minY += removeY;
				maxY -= removeY;
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) minY, (float) minZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) maxY, (float) minZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) maxY, (float) minZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) minY, (float) minZ).setColor(r, g, b, opacity);
			}
			case SOUTH -> {
				maxZ += 0.01;
				minX += removeX;
				maxX -= removeX;
				minY += removeY;
				maxY -= removeY;
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) minY, (float) maxZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) maxY, (float) maxZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) maxY, (float) maxZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) minY, (float) maxZ).setColor(r, g, b, opacity);
			}
			case EAST -> {
				maxX += 0.01;
				minY += removeY;
				maxY -= removeY;
				minZ += removeZ;
				maxZ -= removeZ;
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) minY, (float) minZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) minY, (float) maxZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) maxY, (float) maxZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) maxX, (float) maxY, (float) minZ).setColor(r, g, b, opacity);
			}
			case WEST -> {
				minX -= 0.01;
				minY += removeY;
				maxY -= removeY;
				minZ += removeZ;
				maxZ -= removeZ;
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) minY, (float) minZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) minY, (float) maxZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) maxY, (float) maxZ).setColor(r, g, b, opacity);
				vertexConsumer.addVertex(matrix4f, (float) minX, (float) maxY, (float) minZ).setColor(r, g, b, opacity);
			}
			default -> {
			}
		}
	}


	public static void renderCubeAtPosition(PoseStack poseStack, VertexConsumer vertexConsumer, Vec3 pos, Color color, int opacity, double size) {


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

		vertexConsumer.addVertex(matrix4f, minX, maxY, minZ).setColor(r, g, b, opacity);
		vertexConsumer.addVertex(matrix4f, maxX, maxY, minZ).setColor(r, g, b, opacity);
		vertexConsumer.addVertex(matrix4f, maxX, maxY, maxZ).setColor(r, g, b, opacity);
		vertexConsumer.addVertex(matrix4f, minX, maxY, maxZ).setColor(r, g, b, opacity);

		vertexConsumer.addVertex(matrix4f, minX, minY, minZ).setColor(r, g, b, opacity);
		vertexConsumer.addVertex(matrix4f, maxX, minY, minZ).setColor(r, g, b, opacity);
		vertexConsumer.addVertex(matrix4f, maxX, minY, maxZ).setColor(r, g, b, opacity);
		vertexConsumer.addVertex(matrix4f, minX, minY, maxZ).setColor(r, g, b, opacity);

		vertexConsumer.addVertex(matrix4f, minX, minY, minZ).setColor(r, g, b, opacity);
		vertexConsumer.addVertex(matrix4f, minX, maxY, minZ).setColor(r, g, b, opacity);
		vertexConsumer.addVertex(matrix4f, maxX, maxY, minZ).setColor(r, g, b, opacity);
		vertexConsumer.addVertex(matrix4f, maxX, minY, minZ).setColor(r, g, b, opacity);

		vertexConsumer.addVertex(matrix4f, minX, minY, maxZ).setColor(r, g, b, opacity);
		vertexConsumer.addVertex(matrix4f, minX, maxY, maxZ).setColor(r, g, b, opacity);
		vertexConsumer.addVertex(matrix4f, maxX, maxY, maxZ).setColor(r, g, b, opacity);
		vertexConsumer.addVertex(matrix4f, maxX, minY, maxZ).setColor(r, g, b, opacity);

		vertexConsumer.addVertex(matrix4f, minX, minY, minZ).setColor(r, g, b, opacity);
		vertexConsumer.addVertex(matrix4f, minX, minY, maxZ).setColor(r, g, b, opacity);
		vertexConsumer.addVertex(matrix4f, minX, maxY, maxZ).setColor(r, g, b, opacity);
		vertexConsumer.addVertex(matrix4f, minX, maxY, minZ).setColor(r, g, b, opacity);

	}

}
