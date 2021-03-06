package de.phbouillon.android.framework.impl;

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

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;

import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.opengl.GLES11;
import de.phbouillon.android.framework.*;
import de.phbouillon.android.framework.impl.gl.GlUtils;
import de.phbouillon.android.framework.impl.gl.font.GLText;
import de.phbouillon.android.games.alite.Settings;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.generator.StringUtil;

public class AndroidGraphics implements Graphics {
	private final float scaleFactor;
	private final Rect visibleArea;
	private final FloatBuffer lineBuffer;
	private final FloatBuffer rectBuffer;
	private final FloatBuffer circleBuffer;
	private final FloatBuffer colorBuffer;
	private final Texture textureManager;
	private final Canvas converterCanvas = new Canvas();
	private final Paint paint = new Paint();
	private final Canvas canvas = new Canvas();
	private final Paint filterPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
	private final Matrix scaleMatrix = new Matrix();
	private final Options options = new Options();
	private final FileIO fileIO;

	AndroidGraphics(FileIO fileIO, float scaleFactor, Rect visibleArea, Texture textureManager) {
		this.fileIO = fileIO;
		this.scaleFactor = scaleFactor;
		this.visibleArea = visibleArea;
		lineBuffer = GlUtils.allocateFloatBuffer(2 * 8);
		rectBuffer = GlUtils.allocateFloatBuffer(4 * 8);
		circleBuffer = GlUtils.allocateFloatBuffer(64 * 8);
		colorBuffer = GlUtils.allocateFloatBuffer(4 * 16);
		this.textureManager = textureManager;
	}

	@Override
	public final Rect getVisibleArea() {
		return visibleArea;
	}

	@Override
	public int transX(int x) {
		return (int) (scaleFactor * x + visibleArea.left);
	}

	@Override
	public int transY(int y) {
		return (int) (scaleFactor * y + visibleArea.top);
	}

	@Override
	public boolean existsAssetsFile(String fileName) {
		return fileIO.existsPrivateFile(fileName);
	}

	@Override
	public Pixmap newPixmap(String fileName) {
		Bitmap bitmap = loadBitmap(fileName);
		return drawBitmapAndScale(fileName, bitmap, bitmap.getWidth(), bitmap.getHeight(), scaleMatrix, canvas, filterPaint);
	}

	private Bitmap loadBitmap(String fileName) {
		try (InputStream is = fileIO.readPrivateFile(fileName)) {
			return loadBitmap(fileName, is);
		} catch (IOException ignored) {
			throw new RuntimeException("Couldn't load bitmap from asset '" + fileName + "'");
		}
	}

	private Bitmap loadBitmap(String fileName, InputStream is) {
		options.inPreferredConfig = Settings.colorDepth == 1 ? Config.ARGB_8888 : Config.ARGB_4444;
		options.inSampleSize = Settings.textureLevel;
		Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
		if (bitmap == null) {
			throw new RuntimeException("Couldn't load bitmap from asset '" + fileName + "'");
		}
		return bitmap;
	}

	@Override
	public Pixmap newPixmap(String fileName, int width, int height) {
		return drawBitmapAndScale(fileName, loadBitmap(fileName), width, height, scaleMatrix, canvas, filterPaint);
	}

	@Override
	public Pixmap newPixmap(String fileName, InputStream is, int width, int height) {
		return drawBitmapAndScale(fileName, loadBitmap(fileName, is), width, height, scaleMatrix, canvas, filterPaint);
	}

	@Override
	public Pixmap newPixmap(Bitmap bitmap, String fileName) {
		return newPixmap(bitmap, fileName, bitmap.getWidth(), bitmap.getHeight());
	}

	@Override
	public Pixmap newPixmap(Bitmap bitmap, String fileName, int width, int height) {
		return drawBitmapAndScale(fileName, bitmap, width, height, new Matrix(), new Canvas(), new Paint(Paint.FILTER_BITMAP_FLAG));
	}

	private Pixmap drawBitmapAndScale(String fileName, Bitmap bitmap, float newWidth, float newHeight, Matrix scaleMatrix, Canvas canvas, Paint filterPaint) {
		if (newWidth == -1) {
			newWidth = bitmap.getWidth();
		}
		if (newHeight == -1) {
			newHeight = bitmap.getHeight();
		}
		newWidth *= Settings.textureLevel * scaleFactor;
		newHeight *= Settings.textureLevel * scaleFactor;
		int textureWidth = determineTextureSize((int) newWidth);
		int textureHeight = determineTextureSize((int) newHeight);
		float tx2 = newWidth / textureWidth;
		float ty2 = newHeight / textureHeight;
		Bitmap scaledBitmap = Bitmap.createBitmap(textureWidth, textureHeight, bitmap.getConfig());

		float ratioX = newWidth / bitmap.getWidth();
		float ratioY = newHeight / bitmap.getHeight();
		float middleX = newWidth / 2.0f;
		float middleY = newHeight / 2.0f;

		scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

		canvas.setBitmap(scaledBitmap);
		canvas.setMatrix(scaleMatrix);
		canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2.0f, middleY - bitmap.getHeight() / 2.0f, filterPaint);

		MemUtil.freeBitmap(bitmap);
		bitmap = scaledBitmap;
		PixmapFormat format;
		if (bitmap.getConfig() == Config.RGB_565) {
			format = PixmapFormat.RGB565;
		} else if (bitmap.getConfig() == Config.ARGB_4444) {
			format = PixmapFormat.ARGB4444;
		} else {
			format = PixmapFormat.ARGB8888;
		}
		return new AndroidPixmap(bitmap, format, fileName, textureManager, (int) newWidth, (int) newHeight, tx2, ty2);
	}

	private int determineTextureSize(int size) {
		int tSize = 64;
		while (tSize < size && tSize < 4096) {
			tSize <<= 1;
		}
		return tSize;
	}

	@Override
	public void clear(int color) {
		GLES11.glClearColor(Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color));
		GLES11.glClear(GLES11.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void drawLine(int x, int y, int x2, int y2, int color) {
		x = transX(x);
		y = transY(y);
		x2 = transX(x2 + 1);
		y2 = transY(y2 + 1);

		GLES11.glLineWidth(scaleFactor);
		setColor(color);
		lineBuffer.clear();
		lineBuffer.put(x);
		lineBuffer.put(y);
		lineBuffer.put(x2);
		lineBuffer.put(y2);
		lineBuffer.position(0);
		GLES11.glEnableClientState(GLES11.GL_VERTEX_ARRAY);
		GLES11.glVertexPointer(2, GLES11.GL_FLOAT, 0, lineBuffer);
		GLES11.glDrawArrays(GLES11.GL_LINES, 0, 2);
		GLES11.glLineWidth(1);
	}

	@Override
	public void drawRect(int x, int y, int width, int height, int color) {
		GLES11.glLineWidth(scaleFactor);
		setColor(color);
		if (!setRectBuffer(x, y, width, height)) return;
		GLES11.glVertexPointer(2, GLES11.GL_FLOAT, 0, rectBuffer);
		GLES11.glDrawArrays(GLES11.GL_LINE_LOOP, 0, 4);
		GLES11.glLineWidth(1);
	}

	private boolean setRectBuffer(int left, int top, int width, int height) {
		int bottom = transY(top + height);
		if (bottom < 0) {
			return false;
		}
		int right = transX(left + width);
		left = transX(left);
		top = transY(top);
		rectBuffer.clear();
		rectBuffer.put(left);
		rectBuffer.put(top);
		rectBuffer.put(right);
		rectBuffer.put(top);
		rectBuffer.put(right);
		rectBuffer.put(bottom);
		rectBuffer.put(left);
		rectBuffer.put(bottom);
		rectBuffer.position(0);
		return true;
	}

	@Override
	public void rec3d(int x, int y, int width, int height, int borderSize, int lightColor, int darkColor) {
		for (int i = 0; i < borderSize; i++) {
			drawLine(x + i, y + i + 1, x + i, y + height - 2 - i, lightColor);
			drawLine(x + i, y + i, x + width - 2 - i, y + i, lightColor);
			drawLine(x + width - 1 - i, y + i, x + width - 1 - i, y + height - 2 - i, darkColor);
			drawLine(x + i, y + height - 1 - i, x + width - 1 - i, y + height - 1 - i, darkColor);
		}
	}

	@Override
	public void fillRect(int x, int y, int width, int height, int color) {
		setColor(color);
		if (!setRectBuffer(x, y, width, height)) return;
		GLES11.glVertexPointer(2, GLES11.GL_FLOAT, 0, rectBuffer);
		GLES11.glDrawArrays(GLES11.GL_TRIANGLE_FAN, 0, 4);
	}

	@Override
	public void verticalGradientRect(int x, int y, int width, int height, int color1, int color2) {
		gradientRect(x, y, width, height, false, color1, color2);
	}

	@Override
	public void diagonalGradientRect(int x, int y, int width, int height, int color1, int color2) {
		gradientRect(x, y, width, height, true, color1, color2);
	}

	private void gradientRect(int x, int y, int width, int height, boolean horizontal, int color1, int color2) {
		GLES11.glEnableClientState(GLES11.GL_VERTEX_ARRAY);
		GLES11.glEnableClientState(GLES11.GL_COLOR_ARRAY);
		if (!setRectBuffer(x, y, width, height)) return;
		colorBuffer.clear();
		putToColorBuffer(color1);
		putToColorBuffer(horizontal ? color2 : color1);
		putToColorBuffer(color2);
		putToColorBuffer(color2);
		colorBuffer.position(0);
		GLES11.glColorPointer(4, GLES11.GL_FLOAT, 0, colorBuffer);
		GLES11.glVertexPointer(2, GLES11.GL_FLOAT, 0, rectBuffer);
		GLES11.glDrawArrays(GLES11.GL_TRIANGLE_FAN, 0, 4);
		GLES11.glDisableClientState(GLES11.GL_COLOR_ARRAY);
	}

	private void putToColorBuffer(int color) {
		colorBuffer.put(Color.red(color) / 255.0f);
		colorBuffer.put(Color.green(color) / 255.0f);
		colorBuffer.put(Color.blue(color) / 255.0f);
		colorBuffer.put(Color.alpha(color) / 255.0f);
	}

	@Override
	public void fillCircle(int cx, int cy, int r, int color) {
		drawCircleWithMode(cx, cy, r, color, 32, GLES11.GL_TRIANGLE_FAN, 360);
	}

	private void drawCircleWithMode(int cx, int cy, int r, int color, int segments, int mode, float angle) {
		if (segments > 64) {
			segments = 64;
		}
		cx = transX(cx);
		cy = transY(cy);
		r = (int) (r * scaleFactor);

		circleBuffer.clear();
		float step = angle / (segments - 1);
		for (int i = 0; i < segments; i++) {
			float ang = (float) (Math.toRadians(i * step) - Math.PI / 2);
			circleBuffer.put((float) (cx + Math.cos(ang) * r));
			circleBuffer.put((float) (cy + Math.sin(ang) * r));
		}
		circleBuffer.position(0);
		setColor(color);
		GLES11.glEnableClientState(GLES11.GL_VERTEX_ARRAY);
		GLES11.glVertexPointer(2, GLES11.GL_FLOAT, 0, circleBuffer);
		GLES11.glDrawArrays(mode, 0, segments);
	}

	@Override
	public void drawArc(int cx, int cy, int r, int color, int angle) {
		drawCircleWithMode(cx, cy, r, color,  64, GLES11.GL_LINE_STRIP, angle);
	}

	@Override
	public void drawCircle(int cx, int cy, int r, int color) {
		drawCircleWithMode(cx, cy, r, color, 64, GLES11.GL_LINE_LOOP, 360);
	}

	@Override
	public void drawDashedCircle(int cx, int cy, int r, int color) {
		drawCircleWithMode(cx, cy, r, color, 64, GLES11.GL_LINES, 360);
	}

	@Override
	public void drawPixmapUnscaled(Pixmap pixmap, int x, int y, int srcX, int srcY, int srcWidth, int srcHeight) {
		x = transX(x);
		y = transY(y);

		srcX = Math.round(srcX * scaleFactor);
		srcY = Math.round(srcY * scaleFactor);
		srcWidth = Math.round(srcWidth * scaleFactor);
		srcHeight = Math.round(srcHeight * scaleFactor);

		pixmap.setTextureCoordinates(srcX, srcY, srcX + srcWidth - 1, srcY + srcHeight - 1);
		pixmap.setCoordinates(x, y, x + srcWidth - 1, y + srcHeight - 1);
		pixmap.render(1);
		pixmap.resetTextureCoordinates();
	}

	@Override
	public Pixmap getNotificationNumber(GLText font, int number) {
		Paint paint = new Paint();
		paint.setTypeface(font.getTypeface());
		paint.setTextSize(font.getSize());

		String text = StringUtil.format("%d", number);

		int width = getTextWidth(text, font);
		int r = (Math.max(width, getTextHeight(text, font)) >> 1) + 5;

		Bitmap bitmap = Bitmap.createBitmap(r << 1, r << 1,
			Settings.colorDepth  == 1 ? Bitmap.Config.ARGB_8888 : Bitmap.Config.ARGB_4444);
		bitmap.eraseColor(Color.TRANSPARENT);

		Canvas canvas = new Canvas(bitmap);
		paint.setColor(ColorScheme.get(ColorScheme.COLOR_WARNING_MESSAGE));
		paint.setStyle(Paint.Style.FILL);
		canvas.drawCircle(r, r, r, paint);

		paint.setColor(ColorScheme.get(ColorScheme.COLOR_MESSAGE));
		canvas.drawText(text, r - (width >> 1), r  + ((int)font.getSize() >> 1) - 5, paint);
		return newPixmap(bitmap, "notificationCircle" + number);
	}

	@Override
	public void drawPixmap(Pixmap pixmap, int x, int y) {
		pixmap.render(transX(x), transY(y));
	}

	@Override
	public void drawPixmap(Pixmap pixmap, int x, int y, float pixmapAlpha) {
		pixmap.render(transX(x), transY(y), pixmapAlpha);
	}

	@Override
	public void applyFilterToPixmap(Pixmap pixmap, ColorFilter filter) {
		Bitmap bit = pixmap.getBitmap();
		converterCanvas.setBitmap(bit);
	    paint.setColorFilter(filter);
	    converterCanvas.drawBitmap(bit, 0, 0, paint);
	    pixmap.setBitmap(bit);
	}

	@Override
	public void drawText(String text, int x, int y, int color, GLText font) {
		drawText(text, x, y, color, font, false);
	}

	private void drawText(String text, int x, int y, int color, GLText font, boolean underlined) {
		if (font == null) {
			return;
		}
		GLES11.glBlendFunc(GLES11.GL_ONE, GLES11.GL_ONE);
		GLES11.glEnable(GLES11.GL_TEXTURE_2D);
		drawTextCommon(text, x, y, color, font, 1);
		if (underlined) {
			int linePos = (int) (y + font.getDescent());
			drawLine(x, linePos, x + getTextWidth(text, font), linePos, color);
		}
		GLES11.glDisable(GLES11.GL_TEXTURE_2D);
	}

	@Override
	public void drawUnderlinedText(String text, int x, int y, int color, GLText font) {
		drawText(text, x, y, color, font, true);
	}

	private void drawTextCommon(String text, int x, int y, int color, GLText font, float scale) {
		GLES11.glEnable(GLES11.GL_BLEND);
	    setColor(color);
		font.begin();
		font.draw(text, transX(x), transY(y - (int) font.getSize()), scale);
		font.end();
		GLES11.glDisable(GLES11.GL_BLEND);
		textureManager.setTexture(null);
	}

	@Override
	public void drawText(String text, int x, int y, int color, GLText font, float scale) {
		if (font == null) {
			return;
		}
		GLES11.glDisable(GLES11.GL_LIGHTING);
		GLES11.glDisable(GLES11.GL_CULL_FACE);
		GLES11.glBlendFunc(GLES11.GL_ONE, GLES11.GL_ONE_MINUS_SRC_ALPHA);
		drawTextCommon(text, x, y, color, font, scale);
		GLES11.glEnable(GLES11.GL_CULL_FACE);
		GLES11.glEnable(GLES11.GL_LIGHTING);
	}

	@Override
	public void drawCenteredText(String text, int x, int y, int color, GLText font, float scale) {
		drawText(text, x - ((int) font.getWidth(text, scale) >> 1), y, color, font, scale);
	}

	@Override
	public int getTextWidth(String text, GLText font) {
		if (font == null) {
			return 0;
		}
		return (int) (font.getWidth(text, 1) / scaleFactor);
	}

	@Override
	public int getTextHeight(String text, GLText font) {
		if (font == null) {
			return 0;
		}
		return (int) (font.getHeight() / scaleFactor);
	}

	@Override
	public void setClip(int x1, int y1, int x2, int y2) {
		if (x1 == -1 && y1 == -1 && x2 == -1 && y2 == -1) {
			GLES11.glDisable(GLES11.GL_SCISSOR_TEST);
			return;
		}
		x1 = x1 == -1 ? Math.max(visibleArea.left - 1, 0) : transX(x1);
		y1 = y1 == -1 ? Math.max(visibleArea.top  - 1, 0) : transY(y1);
		x2 = x2 == -1 ? Math.min(visibleArea.right + 1, visibleArea.width()) : transX(x2);
		y2 = y2 == -1 ? Math.min(visibleArea.bottom + 1, visibleArea.height()) : transY(y2);
		GLES11.glEnable(GLES11.GL_SCISSOR_TEST);
		GLES11.glScissor(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
	}

	@Override
	public void setColor(int color, float alpha) {
		GLES11.glColor4f(Color.red(color) / 255.0f, Color.green(color) / 255.0f, Color.blue(color) / 255.0f, alpha);
	}

	@Override
	public void setColor(int color) {
		setColor(color, Color.alpha(color) / 255.0f);
	}

	@Override
	public void drawArrow(int x1, int y1, int x2, int y2, int color, ArrowDirection arrowHead) {
		int temp;
		if (x1 > x2) {
			temp = x1;
			x1 = x2;
			x2 = temp;
		}
		if (y1 > y2) {
			temp = y1;
			y1 = y2;
			y2 = temp;
		}
		drawLine(x1, y1, x2, y2, color);
		for (int i = 1; i < 10; i++) {
			switch (arrowHead) {
				case LEFT:  drawLine(x1 + i, y1 - i, x1 + i, y1 + i, color); break;
				case RIGHT: drawLine(x2 - i, y1 - i, x2 - i, y1 + i, color); break;
				case UP:    drawLine(x1 - i, y1 + i, x1 + i, y1 + i, color); break;
				case DOWN:  drawLine(x1 - i, y2 - i, x1 + i, y2 - i, color); break;
			}
		}
	}

}
