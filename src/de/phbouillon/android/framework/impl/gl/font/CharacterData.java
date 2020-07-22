package de.phbouillon.android.framework.impl.gl.font;

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

import java.io.Serializable;

public class CharacterData implements Serializable {
	private static final long serialVersionUID = 3097765273945320958L;

	public final int width;
	public final int height;
	public final float uStart;
	public final float uEnd;
	public final float vStart;
	public final float vEnd;

	public CharacterData(int charWidth, int charHeight, float texWidth, float texHeight,
			float x, float y, float cellWidth, float cellHeight) {
		width = charWidth;
		height = charHeight;
		uStart = x / texWidth;
		vStart = y / texHeight;
		uEnd = uStart + cellWidth / texWidth;
		vEnd = vStart + cellHeight / texHeight;
	}
}
