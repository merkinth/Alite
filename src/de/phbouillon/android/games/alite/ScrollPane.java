package de.phbouillon.android.games.alite;

/* Alite - Discover the Universe on your Favorite Android Device
 * Copyright (C) 2015 Philipp Bouillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful and
 * fun, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * http://http://www.gnu.org/licenses/gpl-3.0.txt.
 */

import android.graphics.Point;
import android.graphics.Rect;
import de.phbouillon.android.framework.Input.TouchEvent;

import java.io.Serializable;

public class ScrollPane {
	public final Rect area;
	public final Point position = new Point(0, 0);

	private Point touchDownPoint;
	private final SizeFunction size;
	private Point lastPoint;
	private final Point delta = new Point(0,0);


	@FunctionalInterface
	public interface SizeFunction extends Serializable {
		Point getSize();
	}

	public ScrollPane(int left, int top, int right, int bottom, SizeFunction size) {
		area = new Rect(left, top, right, bottom);
		this.size = size;
	}

	public void handleEvent(TouchEvent event) {
		boolean inBounds = area.contains(event.x, event.y);
		if (event.type == TouchEvent.TOUCH_DOWN && event.pointer == 0) {
			touchDownPoint = inBounds ? new Point(event.x, event.y) : null;
			lastPoint = touchDownPoint;
			delta.x = 0;
			delta.y = 0;
		}

		if (event.type == TouchEvent.TOUCH_DRAGGED && event.pointer == 0 && touchDownPoint != null && inBounds) {
			changePosition(lastPoint.x, lastPoint.y, event.x, event.y);
			lastPoint = new Point(event.x, event.y);
			delta.x = 0;
			delta.y = 0;
		}

		if (event.type == TouchEvent.TOUCH_SWEEP && inBounds) {
			delta.x = event.x2;
			delta.y = event.y2;
		}
	}

	public void changePosition(int fromX, int fromY, int toX, int toY) {
		int oversizeX = getOversizeX();
		int oversizeY = getOversizeY();
		int diff = fromX - toX;
		if (toX < fromX) {
			position.x = Math.min(position.x + diff, oversizeX);
		} else {
			position.x = Math.max(position.x + diff, 0);
		}
		diff = fromY - toY;
		if (toY < fromY) {
			position.y = Math.min(position.y + diff, oversizeY);
		} else {
			position.y = Math.max(position.y + diff, 0);
		}
	}

	private int getOversizeX() {
		return Math.max(size.getSize().x - (area.right - area.left), 0);
	}

	private int getOversizeY() {
		return Math.max(size.getSize().y - (area.bottom - area.top), 0);
	}

	public void moveToTop() {
		position.y = 0;
		delta.x = 0;
		delta.y = 0;
	}

	public boolean isAtBottom() {
		return position.y >= getOversizeY();
	}

	public boolean isSweepingGesture(TouchEvent event) {
		return touchDownPoint == null || Math.abs(touchDownPoint.x - event.x) >= 20 ||
			Math.abs(touchDownPoint.y - event.y) >= 20;
	}

	public void scrollingFree() {
		if (delta.x != 0) {
			delta.x += delta.x > 0 ? -1 : 1;
			position.x -= delta.x;
			if (position.x < 0) {
				position.x = 0;
			}
			int oversize = getOversizeX();
			if (position.x > oversize) {
				position.x = oversize;
			}
		}
		if (delta.y != 0) {
			delta.y += delta.y > 0 ? -1 : 1;
			position.y -= delta.y;
			if (position.y < 0) {
				position.y = 0;
			}
			int oversize = getOversizeY();
			if (position.y > oversize) {
				position.y = oversize;
			}
		}
	}

	public void setScrollingTarget(int deltaX, int deltaY) {
		delta.x = deltaX;
		delta.y = deltaY;
	}
}
