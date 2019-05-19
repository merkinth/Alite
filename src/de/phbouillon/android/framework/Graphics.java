package de.phbouillon.android.framework;

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

import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import de.phbouillon.android.framework.impl.gl.font.GLText;

public interface Graphics {
	enum PixmapFormat {
		ARGB8888, ARGB4444, RGB565
	}

	enum ArrowDirection {
		LEFT, UP, RIGHT, DOWN
	}


	Rect getVisibleArea();
	boolean existsAssetsFile(String fileName);

	void clear(int color);
	void setClip(int x1, int y1, int x2, int y2);

	void setColor(int color, float alpha);
	void setColor(int color);

	void drawLine(int x, int y, int x2, int y2, int color);
	void drawRect(int x, int y, int width, int height, int color);
	void fillRect(int x, int y, int width, int height, int color);
	void rec3d(int x, int y, int width, int height, int borderSize, int lightColor, int darkColor);
	void verticalGradientRect(int x, int y, int width, int height, int color1, int color2);
	void diagonalGradientRect(int x, int y, int width, int height, int color1, int color2);
	void drawArc(int cx, int cy, int r, int color, int degree);
	void drawCircle(int cx, int cy, int r, int color, int segments);
	void fillCircle(int cx, int cy, int r, int color, int segments);
	void drawDashedCircle(int cx, int cy, int r, int color, int segments);
	void drawArrow(int x1, int y1, int x2, int y2, int color, ArrowDirection arrowHead);

	void drawText(String text, int x, int y, int color, GLText font);
	void drawText(String text, int x, int y, int color, GLText font, float scale);
	void drawCenteredText(String text, int x, int y, int color, GLText font, float scale);

	int getTextWidth(String text, GLText font);
	int getTextHeight(String text, GLText font);

	Pixmap newPixmap(String fileName);
	Pixmap newPixmap(String fileName, int width, int height);
	Pixmap newPixmap(Bitmap bitmap, String fileName);

	void drawPixmap(Pixmap pixmap, int x, int y);
	void drawPixmap(Pixmap pixmap, int x, int y, float pixmapAlpha);
	void applyFilterToPixmap(Pixmap pixmap, ColorFilter filter);
	void drawPixmapUnscaled(Pixmap pixmap, int x, int y, int srcX, int srcY, int srcWidth, int srcHeight);

}
