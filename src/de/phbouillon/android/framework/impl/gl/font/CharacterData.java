package de.phbouillon.android.framework.impl.gl.font;

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
