// This is a OpenGL ES 1.0 dynamic font rendering system. It loads actual font
// files, generates a font map (texture) from them, and allows rendering of
// text strings.
//
// NOTE: the rendering portions of this class uses a sprite batcher in order
// provide decent speed rendering. Also, rendering assumes a BOTTOM-LEFT
// origin, and the (x,y) positions are relative to that, as well as the
// bottom-left of the string to render.
// Taken from here: http://fractiousg.blogspot.de/2012/04/rendering-text-in-opengl-on-android.html

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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLES11;
import android.opengl.GLUtils;
import androidx.core.content.res.ResourcesCompat;
import de.phbouillon.android.framework.MemUtil;
import de.phbouillon.android.games.alite.L;
import de.phbouillon.android.games.alite.R;

public class GLText {

	/*
	Every strings.xml must contain a string array with name "char_sets".
	Its items must contain start and end char code of required chars for the current locale
	in format "start_char_code..end_char_code".
	In default values/strings.xml resource file can be used '\\uXXXX' form
	but the form '&#xXXXX;' can be used in the all other "external" language packs
	since those are plain xml files rather than resource files.
	See the following example to defining char sets in
	strings.xml resource				strings.xml of language packs
	<string-array name="char_sets">		<string-array name="char_sets">
		<item>\u0020..\u007e</item>			<item>&#x0020;..&#x007e;</item>
		<item>\u00a1..\u00ff</item>			<item>&#x00a1;..&#x00ff;</item>
		<item>\u0100..\u017f</item>			<item>&#x0100;..&#x017f;</item>
	</string-array>						</string-array>
	*/

	private static final char CHAR_UNKNOWN = '\u00b0'; // Code of the Unknown Character

	private final int charCount = getCharCount();

	private static final int FONT_SIZE_MIN = 6; // Minumum Font Size (Pixels)
	private static final int FONT_SIZE_MAX = 180; // Maximum Font Size (Pixels)

	private static final int CHAR_BATCH_SIZE = 100; // Number of Characters to Render Per Batch

	private int givenFontSize;

	// --Members--//
	private SpriteBatch batch; // Batch Renderer

	private int fontPadX;
	private int fontPadY; // Font Padding (Pixels; On Each Side, ie. Doubled on Both X+Y Axis)

	private float fontHeight; // Font Height (Actual; Pixels)
	private float fontAscent; // Font Ascent (Above Baseline; Pixels)
	private float fontDescent; // Font Descent (Below Baseline; Pixels)

	private int textureId; // Font Texture ID
	private int textureWidth;
	private int textureHeight;

	private float charWidthMax; // Character Width (Maximum; Pixels)
	private float charHeight; // Character Height (Maximum; Pixels)
	private float[] charWidths = new float[charCount]; // Width of Each Character (Actual; Pixels)
	private CharacterData[] charData = new CharacterData[charCount]; // Data of Each Character (Width, height, texture Coordinates)
	private int cellWidth;
	private int cellHeight; // Character Cell Width/Height
	private int rowCnt;
	private int colCnt; // Number of Rows/Columns

	private float spaceX; // Additional (X,Y Axis) Spacing (Unscaled)
	private Bitmap.Config config;

	// --Constructor--//
	// D: save GL instance + asset manager, create arrays, and initialize the members
	// A: gl - OpenGL ES 10 Instance

	private GLText(int colorDepth) {
		config = colorDepth == 1 ? Bitmap.Config.ARGB_8888 : Bitmap.Config.ARGB_4444;
		batch = new SpriteBatch(CHAR_BATCH_SIZE); // Create Sprite Batch (with Defined Size)

		// initialize remaining members
		fontPadX = 0;
		fontPadY = 0;

		fontHeight = 0.0f;
		fontAscent = 0.0f;
		fontDescent = 0.0f;

		textureId = -1;
		textureWidth = 0;
		textureHeight = 0;

		charWidthMax = 0;
		charHeight = 0;

		cellWidth = 0;
		cellHeight = 0;
		rowCnt = 0;
		colCnt = 0;

		spaceX = 0.0f;
	}

	// --Load Font--//
	// description
	// this will load the specified font file, create a texture for the defined
	// character range, and setup all required values used to render with it.
	// arguments:
	// fontId - font (.ttf, .otf) to use in 'res/font' folder.
	// size - Requested pixel size of font (height)
	// padX, padY - Extra padding per character (X+Y Axis); to prevent
	// overlapping characters.
	public static GLText load(Context context, int colorDepth, int fontId, int size, int givenSize, int padX, int padY) {
		return new GLText(colorDepth).load(ResourcesCompat.getFont(context, fontId), size, givenSize, padX, padY);
	}

	private GLText load(Typeface tf, int size, int givenSize, int padX, int padY) {
		// setup requested values
		fontPadX = padX; // Set Requested X Axis Padding
		fontPadY = padY; // Set Requested Y Axis Padding

		givenFontSize = givenSize;

		// load the font and setup paint instance for drawing

		Paint paint = new Paint(); // Create Android Paint Instance
		paint.setAntiAlias(true); // Enable Anti Alias
		paint.setTextSize(size); // Set Text Size
		paint.setColor(Color.WHITE); // Set ARGB (White, Opaque)
		paint.setTypeface(tf); // Set Typeface

		// get font metrics
		Paint.FontMetrics fm = paint.getFontMetrics(); // Get Font Metrics
		fontHeight = (float) Math.ceil(Math.abs(fm.bottom) + Math.abs(fm.top)); // Calculate Font Height
		fontAscent = (float) Math.ceil(Math.abs(fm.ascent)); // Save Font Ascent
		fontDescent = (float) Math.ceil(Math.abs(fm.descent)); // Save Font Descent

		// determine the width of each character (including unknown character)
		// also determine the maximum character width
		charWidthMax = 0;
		charHeight = 0; // Reset Character Width/Height Maximums
		float[] w = new float[2]; // Working Width Value
		int cnt = 0; // Array Counter
		for (String charRange : L.array(R.array.char_sets)) {
			for (char c = charRange.charAt(0); c <= charRange.charAt(3); c++) { // FOR Each Character
				paint.getTextWidths("" + c, 0, 1, w); // Get Character Bounds
				charWidths[cnt] = w[0]; // Get Width
				if (charWidths[cnt] > charWidthMax) // IF Width Larger Than Max Width
					charWidthMax = charWidths[cnt]; // Save New Max Width
				cnt++; // Advance Array Counter
			}
		}

		// set character height to font height
		charHeight = fontHeight; // Set Character Height

		// find the maximum size, validate, and setup cell sizes
		cellWidth = (int) charWidthMax + 2 * fontPadX; // Set Cell Width
		cellHeight = (int) charHeight + 2 * fontPadY; // Set Cell Height
		int maxSize = cellWidth > cellHeight ? cellWidth : cellHeight; // Save Max Size (Width/Height)
		if (maxSize < FONT_SIZE_MIN || maxSize > FONT_SIZE_MAX) // IF Maximum Size Outside Valid Bounds
			// Return Error
			return this;

		colCnt = (int) Math.ceil(Math.sqrt(cnt * cellWidth * cellHeight) / cellWidth);
		rowCnt = (int) Math.ceil(charCount / (float) colCnt);

		textureWidth = colCnt * cellWidth;
		textureHeight = rowCnt * cellHeight;

		// create an empty bitmap (alpha only)
		Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight, config); // Create Bitmap
		Canvas canvas = new Canvas(bitmap); // Create Canvas for Rendering to Bitmap
		bitmap.eraseColor(0x00000000); // Set Transparent Background (ARGB)


		// render each of the characters to the canvas (ie. build the font map)
		float x = 0; // Set Start Position (X)
		float y = 0;// Set Start Position (Y)
		float yShift = (cellHeight - 1) - fontDescent - fontPadY;
		cnt = 0;
		for (String charRange : L.array(R.array.char_sets)) {
			for (char c = charRange.charAt(0); c <= charRange.charAt(3); c++) { // FOR Each Character
				canvas.drawText("" + c, x + fontPadX, y + yShift, paint); // Draw Character
				charData[cnt] = new CharacterData((int)charWidths[cnt], (int)charHeight, textureWidth, textureHeight,
					x, y, cellWidth - 1, cellHeight - 1); // Create Region for Character
				x += cellWidth; // Move to Next Character
				if (x + cellWidth > textureWidth) { // IF End of Line Reached
					x = 0; // Set X for New Row
					y += cellHeight; // Move Down a Row
				}
				cnt++; // Advance Array Counter
			}
		}

		// generate a new texture
		int[] textureIds = new int[1]; // Array to Get Texture Id
		GLES11.glGenTextures(1, textureIds, 0); // Generate New Texture
		textureId = textureIds[0]; // Save Texture Id
		// setup filters for texture
		GLES11.glBindTexture(GLES11.GL_TEXTURE_2D, textureId); // Bind Texture
		GLES11.glTexParameterf(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MAG_FILTER, GLES11.GL_LINEAR); // Set Magnification Filter
		GLES11.glTexParameterf(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MIN_FILTER, GLES11.GL_NEAREST); // Set Minification Filter
		GLES11.glTexParameterf(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_WRAP_S, GLES11.GL_CLAMP_TO_EDGE); // Set U Wrapping
		GLES11.glTexParameterf(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_WRAP_T, GLES11.GL_CLAMP_TO_EDGE); // Set V Wrapping

		// load the generated bitmap onto the texture
		GLUtils.texImage2D(GLES11.GL_TEXTURE_2D, 0, bitmap, 0); // Load Bitmap to Texture
		GLES11.glBindTexture(GLES11.GL_TEXTURE_2D, 0); // Unbind Texture

/*
		// Save font bitmap to file 'font.png' for test purposes
		try {
			FileOutputStream fos = new FileOutputStream(new File(
				new AndroidFileIO(Alite.get().getApplicationContext()).getFileName("font.png")));
			bitmap.compress(Bitmap.CompressFormat.PNG, 85, fos);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
*/
		// release the bitmap
		MemUtil.freeBitmap(bitmap);
		return this;
	}

	public void begin() {
		GLES11.glBindTexture(GLES11.GL_TEXTURE_2D, textureId); // Bind the Texture
		batch.beginBatch(); // Begin Batch
	}

	public void end() {
		batch.endBatch(); // End Batch
		GLES11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f); // Restore Default Color/Alpha
	}

	// --Draw Text--//
	// D: draw text at the specified x,y position
	// A: text - the string to draw
	// x, y - the x,y position to draw text at (bottom left of text; including descent)
	// R: [none]
	public void draw(String text, float x, float y, float scale) {
		float chrHeight = cellHeight * scale; // Calculate Scaled Character Height
		float chrWidth = cellWidth * scale; // Calculate Scaled Character Width
		int len = text.length(); // Get String Length
		x += chrWidth / 2.0f - fontPadX * scale; // Adjust Start X
		y += chrHeight / 2.0f - fontPadY * scale; // Adjust Start Y
		for (int i = 0; i < len; i++) { // FOR Each Character in String
			int c = getCharIndex(text.charAt(i));
			batch.drawSprite(x, y, chrWidth, chrHeight, charData[c]); // Draw the Character
			x += (charWidths[c] + spaceX)  * scale; // Advance X Position by Scaled Character Width
		}
	}

	private int getCharCount() {
		int count = 0;
		for (String charRange : L.array(R.array.char_sets)) {
			count += charRange.charAt(3) - charRange.charAt(0) + 1;
		}
		return count;
	}

	private int getCharIndex(char c) {
		int index = 0;
		for (String charRange : L.array(R.array.char_sets)) {
			if (c >= charRange.charAt(0) && c <= charRange.charAt(3)) {
				return index + c - charRange.charAt(0);
			}
			index += charRange.charAt(3) - charRange.charAt(0) + 1;
		}
		return c == CHAR_UNKNOWN ? 0 : getCharIndex(CHAR_UNKNOWN);
	}

	// --Set Space--//
	// D: set the spacing (unscaled; ie. pixel size) to use for the font
	// A: space - space for x axis spacing
	// R: [none]
	public void setSpace(float space) {
		spaceX = space;
	}

	// --Get Space--//
	// D: get the current spacing used for the font
	// A: [none]
	// R: the x/y space currently used for scale
	public float getSpace() {
		return spaceX;
	}

	// --Get width of a String--//
	// D: return the width of the specified string if rendered using current settings
	// A: text - the string to get length for
	// R: the length of the specified string (pixels)
	public float getWidth(String text, float scale) {
		float len = 0.0f; // Working Length
		int strLen = text.length(); // Get String Length (Characters)
		for (int i = 0; i < strLen; i++) { // For Each Character in String (Except Last
			if (text.charAt(i) == '\n') {
				break;
			}
			len += charWidths[getCharIndex(text.charAt(i))] * scale; // Add Scaled Character Width to Total Length
		}
		len += strLen > 1 ? (strLen - 1) * spaceX * scale : 0; // Add Space Length
		return len; // Return Total Length
	}

	// --Get unscaled Width/Height of Character--//
	// D: return the unscaled width/height of a character, or max character width
	// NOTE: since all characters are the same height, no character index is required!
	// NOTE: excludes spacing!!
	// A: chr - the character to get width for
	// R: the requested character size (unscaled)
	public float getCharWidth(char chr) {
		return charWidths[getCharIndex(chr)]; // Return unscaled Character Width
	}

	public float getCharWidthMax() {
		return charWidthMax; // Return unscaled Max Character Width
	}

	private float getCharHeight() {
		return charHeight; // Return unscaled Character Height
	}

	// --Get Font Metrics--//
	// D: return the specified (unscaled) font metric
	// A: [none]
	// R: the requested font metric (unscaled)
	public float getAscent() {
		return fontAscent;
	}

	public float getDescent() {
		return fontDescent;
	}

	public float getHeight() {
		return fontHeight;
	}

	public float getSize() {
		return givenFontSize;
	}
}
