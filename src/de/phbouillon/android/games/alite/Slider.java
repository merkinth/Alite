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

import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Rect;
import de.phbouillon.android.framework.impl.gl.font.GLText;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.generator.StringUtil;

public class Slider extends Component<Slider> {
	private static final int SLIDER_SIZE = 40;

	private static final int TEXT_COLOR = ColorScheme.get(ColorScheme.COLOR_MESSAGE);
	private static final int LIGHT_SCALE_COLOR = ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT);
	private static final int DARK_SCALE_COLOR = ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK);
	private static final int TICK_COLOR = ColorScheme.get(ColorScheme.COLOR_MESSAGE);

	private static final int NUMBER_OF_TICKS = 8;

	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private final float minValue;
	private final float maxValue;
	private float currentValue;
	private String text;
	private GLText font;
	private final int scaleHeight;
	private final int[] tickX = new int[NUMBER_OF_TICKS + 1];
	private String minText;
	private String maxText;
	private String middleText;
	private boolean currentValueChanged;

	public Slider(int x, int y, int width, int height, float minValue, float maxValue, float currentValue, String text, GLText font) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.text = text;
		this.font = font;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.currentValue = currentValue;
		scaleHeight = Math.min(height >> 1, 10);
		setTickX();
	}

	private void setTickX() {
		float cPos = x;
		int current = 0;
		while (cPos < x + width && current <= NUMBER_OF_TICKS) {
			tickX[current++] = (int) cPos;
			cPos = (float) width / NUMBER_OF_TICKS * current + x;
		}
		tickX[NUMBER_OF_TICKS] = x + width - 1;
	}

	public void setScaleTexts(String min, String max, String middle) {
		minText = min;
		maxText = max;
		middleText = middle;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setFont(GLText font) {
		this.font = font;
	}

	public String getText() {
		return text;
	}

	private void renderScale(Graphics g) {
		int middle = y + (height >> 1);
		g.rec3d(x, middle - (scaleHeight >> 1), width, scaleHeight, 2, LIGHT_SCALE_COLOR, DARK_SCALE_COLOR);
		for (int x: tickX) {
			g.drawLine(x, middle - scaleHeight - 3, x, middle + scaleHeight + 3, TICK_COLOR);
		}
		g.drawText(text + ": " + StringUtil.format("%3.2f", currentValue),
			x + (width >> 1) - (g.getTextWidth(text, font) >> 1), middle - 20, TEXT_COLOR, font);
		if (minText != null) {
			int x = tickX[0] - (g.getTextWidth(minText, Assets.regularFont) >> 1);
			g.drawText(minText, x, middle + scaleHeight + 30, TEXT_COLOR, Assets.regularFont);
		}
		if (middleText != null) {
			int x = tickX[NUMBER_OF_TICKS >> 1] - (g.getTextWidth(middleText, Assets.regularFont) >> 1);
			g.drawText(middleText, x, middle + scaleHeight + 30, TEXT_COLOR, Assets.regularFont);
		}
		if (maxText != null) {
			int x = tickX[NUMBER_OF_TICKS] - (g.getTextWidth(maxText, Assets.regularFont) >> 1);
			g.drawText(maxText, x, middle + scaleHeight + 30, TEXT_COLOR, Assets.regularFont);
		}
	}

	private void renderPointer(Graphics g) {
		int middleX = (int) ((currentValue - minValue) / maxValue * width + x);
		int middleY = y + (height >> 1);
		g.fillCircle(middleX, middleY, SLIDER_SIZE, TICK_COLOR);
	}

	@Override
	public void render(Graphics g) {
		renderScale(g);
		renderPointer(g);
	}

	private void modifyValue(int xVal) {
		if (xVal < x) {
			xVal = x;
		}
		if (xVal >= x + width) {
			xVal = x + width;
		}
		currentValue = (xVal - (float) x) / width;
		if (currentValue < minValue) {
			currentValue = minValue;
		}
		if (currentValue > maxValue) {
			currentValue = maxValue;
		}
		currentValueChanged = true;
	}

	@Override
	public boolean checkEvent(TouchEvent e) {
		if (e.type == TouchEvent.TOUCH_DRAGGED) {
			if (isDown(e.pointer)) {
				modifyValue(e.x);
				return true;
			}
		}
		if (e.type == TouchEvent.TOUCH_DOWN) {
			if (Rect.inside(e.x, e.y, x, y, x + width, y + height)) {
				fingerDown(e.pointer);
				modifyValue(e.x);
				return false;
			}
			int middleX = (int) ((currentValue - minValue) / maxValue * width + x);
			int middleY = y + (height >> 1);
			if (Rect.inside(e.x, e.y, middleX - SLIDER_SIZE, middleY - SLIDER_SIZE,
					middleX + SLIDER_SIZE, middleY + SLIDER_SIZE)) {
				fingerDown(e.pointer);
				return false;
			}
		}
		if (e.type == TouchEvent.TOUCH_UP) {
			if (isDown(e.pointer)) {
				fingerUp(e.pointer);
				boolean result = currentValueChanged;
				currentValueChanged = false;
				return result;
			}
		}
		return false;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public float getCurrentValue() {
		return currentValue;
	}

	public float getCurrentValue(float step) {
		return Math.round(currentValue / step) * step;
	}

}
