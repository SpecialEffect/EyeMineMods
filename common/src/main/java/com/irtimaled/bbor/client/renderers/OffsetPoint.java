package com.irtimaled.bbor.client.renderers;

import com.irtimaled.bbor.client.Camera;
import com.irtimaled.bbor.client.models.Point;
import com.irtimaled.bbor.common.models.Coords;

public class OffsetPoint {
	private final Point point;

	public OffsetPoint(double x, double y, double z) {
		this(new Point(x, y, z));
	}

	OffsetPoint(Coords coords) {
		this(new Point(coords));
	}

	OffsetPoint(Point point) {
		this.point = point;
	}

	double getX() {
		return point.getX() - Camera.getX();
	}

	double getY() {
		return point.getY() - Camera.getY();
	}

	double getZ() {
		return point.getZ() - Camera.getZ();
	}

	OffsetPoint offset(double x, double y, double z) {
		return new OffsetPoint(point.offset(x, y, z));
	}

	double getDistance(OffsetPoint offsetPoint) {
		return this.point.getDistance(offsetPoint.point);
	}

	Point getPoint() {
		return this.point;
	}
}
