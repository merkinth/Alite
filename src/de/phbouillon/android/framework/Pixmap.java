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
import de.phbouillon.android.framework.Graphics.PixmapFormat;

public interface Pixmap {
	int getWidth();
	int getHeight();
	PixmapFormat getFormat();
	void dispose();
	Bitmap getBitmap();
	void setBitmap(Bitmap bitmap);
	void setTextureCoordinates(int left, int top, int right, int bottom);
	void setCoordinates(int left, int top, int right, int bottom);
	void resetTextureCoordinates();
	void render(float alpha);
	void render(int x, int y);
	void render(int x, int y, float pixmapAlpha);
}
