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
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.framework.Pixmap;
import de.phbouillon.android.framework.Rect;
import de.phbouillon.android.framework.impl.gl.font.GLText;
import de.phbouillon.android.games.alite.colors.AliteColor;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.screens.canvas.TextData;

@SuppressLint("RtlHardcoded")
public class Button extends Component<Button> {

	public static final int BORDER_SIZE = 5;
	private static final double BUTTON_BORDER_DIFF_PERCENT = 0.1;

	private final int BKG_COLOR_DARK = ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK);
	private final int BKG_COLOR_LIGHT = ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT);
	private final int BORDER_COLOR_DARK = AliteColor.lighten(BKG_COLOR_DARK, -BUTTON_BORDER_DIFF_PERCENT);
	private final int BORDER_COLOR_LIGHT = AliteColor.lighten(BKG_COLOR_LIGHT, BUTTON_BORDER_DIFF_PERCENT);

	private int x;
	private int y;
	private final int width;
	private final int height;
	private String text;
	private TextData[] textData;
	private Pixmap pixmap;
	private Pixmap pushedBackground;
	private Pixmap[] animation;
	private Pixmap[] overlay;
	private float pixmapAlpha = 1;
	private GLText font;
	private TextPosition textPosition = TextPosition.ONTOP;
	private boolean useBorder = true;
	private boolean gradient;
	private boolean selected = false;
	private int xOffset;
	private int yOffset;
	private int buttonEnd;
	private int textColor = ColorScheme.get(ColorScheme.COLOR_MESSAGE);

	private int pixmapXOffset;
	private int pixmapYOffset;
	private boolean visible = true;
	private String name;
	private boolean touchedDown;
	private int command;

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

	public static Button createPictureButton(int x, int y, int width, int height, Pixmap pixmap, float pixmapAlpha) {
		return new Button(x, y, width, height).setPixmap(pixmap, pixmapAlpha).setBorderOff();
	}

	public static Button createGradientPictureButton(int x, int y, int width, int height, Pixmap pixmap) {
		return new Button(x, y, width, height).setPixmap(pixmap).setGradientOn();
	}

	public static Button createOverlayButton(int x, int y, int width, int height, Pixmap pixmap, Pixmap[] overlay) {
		return new Button(x, y, width, height).setPixmap(pixmap).setOverlay(overlay).setBorderOff();
	}

	private Button(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		ButtonRegistry.get().addButton(this);
	}

	public Button move(int newX, int newY) {
		x = newX;
		y = newY;
		return this;
	}

	public Button setName(String name) {
		this.name = name;
		return this;
	}

	public String getName() {
		return name;
	}

	public Button setPixmapOffset(int x, int y) {
		pixmapXOffset = x;
		pixmapYOffset = y;
		return this;
	}

	public Button setTextColor(int textColor) {
		this.textColor = textColor;
		return this;
	}

	public Button setXOffset(int x) {
		xOffset = x;
		return this;
	}

	public Button setYOffset(int y) {
		yOffset = y;
		return this;
	}

	public Button setText(String text) {
		this.text = text;
		return this;
	}

	public Button setTextData(TextData[] textData) {
		this.textData = textData;
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

	public int getFrameCount() {
		return animation == null ? 0 : animation.length;
	}

	public Button setPixmap(Pixmap pixmap) {
		this.pixmap = pixmap;
		return this;
	}

	private Button setPixmap(Pixmap pixmap, float pixmapAlpha) {
		this.pixmap = pixmap;
		this.pixmapAlpha = pixmapAlpha;
		return this;
	}

	public Button setPushedBackground(Pixmap pushedBackground) {
		this.pushedBackground = pushedBackground;
		return this;
	}

	public String getText() {
		return text;
	}

	@Override
	public void render(Graphics g) {
		render(g, 0);
	}

	private Point calculateTextPosition(Graphics g) {
		int halfWidth  = g.getTextWidth(text, font) >> 1;
		int halfHeight = g.getTextHeight(text, font) >> 1;
		int height = this.height - (useBorder ? 2 * BORDER_SIZE : 0);
		int width = this.width - (useBorder ? 2 * BORDER_SIZE : 0);

		TextPosition local = textPosition;
		if (pixmap == null && animation == null) {
			local = TextPosition.ONTOP;
		}
		Point result = getInterior();

		if (local == TextPosition.ABOVE) {
			result.offset((width >> 1) - halfWidth, (int) font.getSize());
			return result;
		}
		if (local == TextPosition.LEFT) {
			result.offset(0, (int) ((height >> 1) - halfHeight + font.getSize()));
			return result;
		}
		if (local == TextPosition.RIGHT) {
			result.offset((width + pixmap.getWidth() >> 1) - halfWidth,
				(int) ((height >> 1) - halfHeight + font.getSize()));
			return result;
		}
		if (local == TextPosition.BELOW) {
			result.offset((width >> 1) - halfWidth, height - (halfHeight << 1));
			return result;
		}
		if (local == TextPosition.ONTOP) {
			result.offset((width >> 1) - halfWidth,
				(int) ((height >> 1) - halfHeight + font.getSize()));
			return result;
		}
		return result;
	}

	private Point getInterior() {
		int x = this.x + xOffset;
		int y = this.y + yOffset;
		if (useBorder) {
			x += BORDER_SIZE;
			y += BORDER_SIZE;
		}
		return new Point(x,y);
	}

	public void setButtonEnd(int pixel) {
		buttonEnd = pixel;
	}

	public void render(Graphics g, int frame) {
		if (!visible) {
			return;
		}
		Point interior = getInterior();

		if (gradient && useBorder) {
			g.diagonalGradientRect(interior.x, interior.y,
				width - 2 * BORDER_SIZE, height - 2 * BORDER_SIZE, BKG_COLOR_LIGHT, BKG_COLOR_DARK);
		}
		if (frame > 0 && animation != null && frame < animation.length && animation[frame] != null) {
			g.drawPixmap(animation[frame], interior.x, interior.y);
		} else {
			if (pixmap != null) {
				Point shiftedInterior = new Point(interior);
				if (isDown() && pushedBackground == null) {
					shiftedInterior.offset(BORDER_SIZE, BORDER_SIZE);
				}
				if (buttonEnd == 0) {
					g.drawPixmap(!isDown() || pushedBackground == null ? pixmap : pushedBackground,
						shiftedInterior.x + pixmapXOffset, shiftedInterior.y + pixmapYOffset, pixmapAlpha);
				} else {
					g.drawPixmapUnscaled(!isDown() || pushedBackground == null ? pixmap : pushedBackground,
						shiftedInterior.x, shiftedInterior.y, 0, 0, width - buttonEnd + 1, height);
					// Draw last portion of image
					g.drawPixmapUnscaled(!isDown() || pushedBackground == null ? pixmap : pushedBackground,
						shiftedInterior.x + width - buttonEnd, shiftedInterior.y,
						pixmap.getWidth() - buttonEnd, 0, buttonEnd, height);
				}
			}
			if (frame > 0 && overlay != null && frame < overlay.length) {
				g.drawPixmap(overlay[frame], interior.x, interior.y);
			}
		}
		if (useBorder) {
			interior.offset(-BORDER_SIZE, -BORDER_SIZE);
			if (isDown()) {
				g.rec3d(interior.x, interior.y, width, height, BORDER_SIZE, BORDER_COLOR_DARK, BORDER_COLOR_LIGHT);
			} else {
				g.rec3d(interior.x, interior.y, width, height, BORDER_SIZE, BORDER_COLOR_LIGHT, BORDER_COLOR_DARK);
			}
			interior.offset(BORDER_SIZE, BORDER_SIZE);
		}
		if (text != null) {
			Point p = calculateTextPosition(g);
			if (isDown()) {
				p.offset(BORDER_SIZE, BORDER_SIZE);
			}
			g.drawText(text, p.x, p.y, textColor, font);
		} else if (textData != null) {
			int offset = isDown() ? BORDER_SIZE : 0;
			for (TextData td: textData) {
				g.drawText(td.text, interior.x + td.x + offset, interior.y + td.y + offset, td.color, td.font);
			}
		}
	}

	public boolean isTouched(int x, int y) {
		if (!visible) {
			return false;
		}
		return x >= this.x + xOffset && x <= this.x + xOffset + width - 1 &&
			   y >= this.y + yOffset && y <= this.y + yOffset + height - 1;
	}

	@Override
	public boolean checkEvent(TouchEvent e) {
		return isPressed(e);
	}

	public boolean isPressed(TouchEvent e) {
		if (!visible) {
			return false;
		}
		if (!Rect.inside(e.x, e.y, x + xOffset, y + yOffset,
				x + xOffset + width - 1, y + yOffset + height - 1)) {
			return false;
		}
		if (e.type == TouchEvent.TOUCH_UP && touchedDown) {
			touchedDown = false;
			SoundManager.play(Assets.click);
			return true;
		}
		if (e.type == TouchEvent.TOUCH_DOWN) {
			touchedDown = true;
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

	public Button setCommand(int command) {
		this.command = command;
		return this;
	}

	public int getCommand() {
		return command;
	}
}
