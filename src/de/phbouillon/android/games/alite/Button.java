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

import android.annotation.SuppressLint;
import android.graphics.Point;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.framework.impl.gl.font.GLText;
import de.phbouillon.android.games.alite.colors.ColorScheme;

@SuppressLint("RtlHardcoded")
public class Button {
	private int x;
	private int y;
	private final int width;
	private final int height;
	private String text;
	private Pixmap pixmap;
	private Pixmap pushedBackground;
	private Pixmap[] animation;
	private Pixmap[] overlay = null;
	private GLText font;
	private TextPosition textPosition = TextPosition.ONTOP;
	private boolean useBorder = true;
	private boolean gradient;
	private boolean selected = false;
	private int xOffset = 0;
	private int yOffset = 0;
	private int buttonEnd = 0;
	private int fingerDown = 0;
	private int textColor = ColorScheme.get(ColorScheme.COLOR_MESSAGE);
	private int borderColorLt = ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT);
	private int borderColorDk = ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK);
	private int pixmapXOffset = 0;
	private int pixmapYOffset = 0;
	private boolean visible = true;
	private String name;

	public enum TextPosition {
		ABOVE, LEFT, RIGHT, BELOW, ONTOP
	}

	public static Button createRegularButton(int x, int y, int width, int height, String text) {
		return new Button(x, y, width, height).setText(text).setFont(Assets.regularFont);
	}

	public static Button createTitleButton(int x, int y, int width, int height, String text) {
		return new Button(x, y, width, height).setText(text).setFont(Assets.titleFont).setBorderOff();
	}

	public static Button createGradientRegularButton(int x, int y, int width, int height, String text) {
		return new Button(x, y, width, height).setText(text).setFont(Assets.regularFont).setGradientOn();
	}

	public static Button createGradientTitleButton(int x, int y, int width, int height, String text) {
		return new Button(x, y, width, height).setText(text).setFont(Assets.titleFont).setGradientOn();
	}

	public static Button createGradientSmallButton(int x, int y, int width, int height, String text) {
		return new Button(x, y, width, height).setText(text).setFont(Assets.smallFont).setGradientOn();
	}

	public static Button createPictureButton(int x, int y, int width, int height, Pixmap pixmap) {
		return new Button(x, y, width, height).setPixmap(pixmap).setBorderOff();
	}

	public static Button createGradientPictureButton(int x, int y, int width, int height, Pixmap pixmap) {
		return new Button(x, y, width, height).setPixmap(pixmap).setGradientOn();
	}

	public static Button createOverlayButton(int x, int y, int width, int height, Pixmap pixmap, Pixmap[] overlay) {
		return new Button(x, y, width, height).setPixmap(pixmap).setOverlay(overlay).setBorderOff();
	}

	public static Button createAnimatedButton(int x, int y, int width, int height, Pixmap[] animation) {
		return new Button(x, y, width, height).setPixmap(animation[0]).setAnimation(animation).setBorderOff();
	}

	private Button(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		ButtonRegistry.get().addButton(Alite.getDefiningScreen(), this);
	}

	public void move(int newX, int newY) {
		x = newX;
		y = newY;
	}

	public Button setName(String name) {
		this.name = name;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setPixmapOffset(int x, int y) {
		pixmapXOffset = x;
		pixmapYOffset = y;
	}

	public Button setTextColor(int textColor) {
		this.textColor = textColor;
		return this;
	}

	public void setXOffset(int x) {
		xOffset = x;
	}

	public void setYOffset(int y) {
		yOffset = y;
	}

	public Button setText(String text) {
		this.text = text;
		return this;
	}

	public Button setFont(GLText font) {
		this.font = font;
		return this;
	}

	public Button setTextPosition(TextPosition position) {
		textPosition = position;
		return this;
	}

	private Button setBorderOff() {
		useBorder = false;
		return this;
	}

	private Button setGradientOn() {
		gradient = true;
		return this;
	}

	private Button setOverlay(Pixmap[] overlay) {
		this.overlay = overlay;
		return this;
	}

	public Button setAnimation(Pixmap [] animation) {
		this.animation = animation;
		return this;
	}

	public Button setPixmap(Pixmap pixmap) {
		this.pixmap = pixmap;
		return this;
	}

	public Button setPushedBackground(Pixmap pushedBackground) {
		this.pushedBackground = pushedBackground;
		return this;
	}

	public String getText() {
		return text;
	}

	void fingerDown(int pointer) {
		int val = 1 << pointer;
		if ((fingerDown & val) == 0) {
			fingerDown += val;
		}
	}

	void fingerUp(int pointer) {
		int val = 1 << pointer;
		if ((fingerDown & val) != 0) {
			fingerDown -= val;
		}
	}

	public void render(Graphics g) {
		render(g, 0);
	}

	private Point calculateTextPosition(Graphics g) {
		int halfWidth  = g.getTextWidth(text, font) >> 1;
		int halfHeight = g.getTextHeight(text, font) >> 1;

		TextPosition local = textPosition;
		if (pixmap == null && animation == null) {
			local = TextPosition.ONTOP;
		}
		Point result = new Point();

		int x = this.x + xOffset;
		int y = this.y + yOffset;
		switch (local) {
			case ABOVE: result.y = y;
						result.x = x + (width >> 1) - halfWidth;
			            break;

			case LEFT:  result.x = x;
						result.y = (int) (y + (height >> 1) - halfHeight + font.getSize());
				        break;

			case RIGHT: int l = x + pixmap.getWidth();
			            int r = x + width;
				        result.x = l + ((r - l) >> 1) - halfWidth;
				        result.y = (int) (y + (height >> 1) - halfHeight + font.getSize());
				        break;

			case BELOW: result.y = y + height - (halfHeight << 1);
				        result.x = x + (width >> 1) - halfWidth;
			            break;

			case ONTOP: result.x = x + (width >> 1) - halfWidth;
			            result.y = (int) (y + (height >> 1) - halfHeight + font.getSize());
			            break;
		}

		return result;
	}

	public void setButtonEnd(int pixel) {
		buttonEnd = pixel;
	}

	public void render(Graphics g, int frame) {
		if (!visible) {
			return;
		}
		int x = this.x + xOffset;
		int y = this.y + yOffset;
		if (gradient && useBorder) {
			g.diagonalGradientRect(x + 5, y + 5, width - 10, height - 10, borderColorLt, borderColorDk);
		}
		if (frame > 0 && animation != null && frame < animation.length && animation[frame] != null) {
			g.drawPixmap(animation[frame], x, y);
		} else {
			if (pixmap != null) {
				if (buttonEnd == 0) {
					g.drawPixmap(fingerDown == 0 || pushedBackground == null ? pixmap : pushedBackground, x + pixmapXOffset, y + pixmapYOffset);
				} else {
					g.drawPixmapUnscaled(fingerDown == 0 || pushedBackground == null ? pixmap : pushedBackground,
						x, y, 0, 0, width - buttonEnd + 1, height);
					// Draw last portion of image
					g.drawPixmapUnscaled(fingerDown == 0 || pushedBackground == null ? pixmap : pushedBackground,
						x + width - buttonEnd, y, pixmap.getWidth() - buttonEnd, 0, buttonEnd, height);
				}
			}
			if (frame > 0 && overlay != null && frame < overlay.length) {
				g.drawPixmap(overlay[frame], x, y);
			}
		}
		if (useBorder) {
			if (fingerDown == 0) {
				g.rec3d(x, y, width, height, 5, borderColorLt, borderColorDk);
			} else {
				g.rec3d(x, y, width, height, 5, borderColorDk, borderColorLt);
			}
		}
		if (text != null) {
			Point p = calculateTextPosition(g);
			g.drawText(text, p.x, p.y, textColor, font);
		}
	}

	public boolean isTouched(int x, int y) {
		if (!visible) {
			return false;
		}
		return x >= this.x + xOffset && x <= this.x + xOffset + width - 1 &&
			   y >= this.y + yOffset && y <= this.y + yOffset + height - 1;
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

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}

	public Pixmap getPixmap() {
		return pixmap;
	}

	public boolean isVisible() {
		return visible;
	}

	public Button setVisible(boolean visible) {
		this.visible = visible;
		return this;
	}
}
