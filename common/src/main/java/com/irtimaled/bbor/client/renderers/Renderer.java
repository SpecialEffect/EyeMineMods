package com.irtimaled.bbor.client.renderers;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import java.awt.Color;

public class Renderer {
	private final Mode glMode;

	static Renderer startLines() {
		return new Renderer(Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
	}

	static Renderer startQuads() {
		return new Renderer(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
	}

//    static Renderer startPoints() {
//        return new Renderer(GL11.GL_POINTS, DefaultVertexFormat.POSITION_COLOR);
//    }

	public static Renderer startTextured() {
		return new Renderer(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
	}

	private static final Tesselator tessellator = new Tesselator(2097152);
	private static BufferBuilder bufferBuilder;

	private int red;
	private int green;
	private int blue;
	private int alpha;

	private Renderer(Mode glMode, VertexFormat vertexFormat) {
		this.bufferBuilder = tessellator.begin(glMode, vertexFormat);
		this.glMode = glMode;
	}

	Renderer setColor(Color color) {
		return setColor(color.getRed(), color.getGreen(), color.getBlue())
				.setAlpha(color.getAlpha());
	}

	public Renderer setColor(int red, int green, int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		return this;
	}

	public Renderer setAlpha(int alpha) {
		this.alpha = alpha;
		return this;
	}

	Renderer addPoint(OffsetPoint point) {
		return addPoint(point.getX(), point.getY(), point.getZ());
	}

	public Renderer addPoints(OffsetPoint[] points) {
		Renderer renderer = this;
		for (OffsetPoint point : points) {
			renderer = renderer.addPoint(point);
		}
		return renderer;
	}

	Renderer addPoint(double x, double y, double z) {
		pos((float) x, (float) y, (float) z);
		color();
		draw();
		return this;
	}

	public Renderer addPoint(double x, double y, double z, float u, float v) {
		pos((float) x, (float) y, (float) z);
		tex(u, v);
		color();
		draw();
		return this;
	}


	private void pos(float x, float y, float z) {
		bufferBuilder.addVertex(x, y, z);
	}

	private void tex(float u, float v) {
		bufferBuilder.setUv(u, v);
	}

	private void color() {
		bufferBuilder.setColor(red, green, blue, alpha);
	}

	public void draw() {
		BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
	}
}
