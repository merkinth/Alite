package de.phbouillon.android.games.alite.screens.canvas;

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
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.opengl.GLES11;
import android.widget.Toast;
import de.phbouillon.android.framework.Graphics;
import de.phbouillon.android.framework.Input.TouchEvent;
import de.phbouillon.android.games.alite.*;
import de.phbouillon.android.games.alite.colors.ColorScheme;
import de.phbouillon.android.games.alite.model.generator.StringUtil;

//This screen never needs to be serialized, as it is not part of the InGame state.
@SuppressWarnings("serial")
public class FatalExceptionScreen extends AliteScreen {
	private Throwable cause;
	private TextData[] causeText;
	private Button sendErrorCauseInMail;
	private Button saveErrorCause;
	private Button restartAlite;
	private String plainCauseText;
	private String savedFilename = null;
	private final List <String> textToDisplay = new ArrayList<>();

	public FatalExceptionScreen(Throwable cause) {
		super(Alite.get());
		this.cause = cause;
	}

	private TextData[] computeCRTextDisplay(Graphics g, String text, int x, int y, int fieldWidth, int deltaY, int color, GLText font, boolean centered) {
		String[] textWords = text.split(" ");
		textToDisplay.clear();
		String result = "";
		for (String word: textWords) {
			int cr = word.indexOf("\n");
			String[] crWords = cr == -1 ? new String[] {word} : word.split("\n");
			for (int i = 0; i < crWords.length; i++) {
				String crWord = crWords[i];
				if (result.isEmpty()) {
					result += crWord;
				} else {
					String test = result + " " + crWord;
					int width = g.getTextWidth(test, font);
					if (width > fieldWidth) {
						textToDisplay.add(result);
						result = crWord;
					} else {
						result += " " + crWord;
					}
				}
				if (i < crWords.length - 1 && !result.isEmpty()) {
					textToDisplay.add(result);
					result = "";
				}
			}

		}
		if (!result.isEmpty()) {
			textToDisplay.add(result);
		}
		int count = 0;
		ArrayList <TextData> resultList = new ArrayList<>();
		for (String t: textToDisplay) {
			int halfWidth = centered ? g.getTextWidth(t, font) >> 1 : fieldWidth >> 1;
			resultList.add(new TextData(t, x + (fieldWidth >> 1) - halfWidth, y + deltaY * count++, color, font));
		}
		return resultList.toArray(new TextData[0]);
	}

	@Override
	public void present(float deltaTime) {
		if (disposed) {
			return;
		}
		Graphics g = game.getGraphics();
		g.clear(ColorScheme.get(ColorScheme.COLOR_BACKGROUND));
		g.verticalGradientRect(0, 0, 1919, 80,
			ColorScheme.get(ColorScheme.COLOR_BACKGROUND_DARK), ColorScheme.get(ColorScheme.COLOR_BACKGROUND_LIGHT));
		g.drawPixmap(Assets.aliteLogoSmall, 20, 5);
		g.drawPixmap(Assets.aliteLogoSmall, 1800, 5);
		centerTextWide("Fatal Error Occurred", 60, Assets.titleFont, ColorScheme.get(ColorScheme.COLOR_MESSAGE));
		g.drawText("Unfortunately, Alite has crashed. I am sorry for the inconvenience :(",
			20, 150, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT), Assets.regularFont);
		g.drawText("Following is the error cause, maybe it helps tracking down the problem:",
			20, 320, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT), Assets.regularFont);
		if (causeText != null) {
			displayText(g, causeText);
		}
		if (savedFilename != null) {
			g.drawText("Report saved to: " + savedFilename, 20,  190, ColorScheme.get(ColorScheme.COLOR_MAIN_TEXT), Assets.regularFont);
		}
		sendErrorCauseInMail.render(g);
		saveErrorCause.render(g);
		restartAlite.render(g);
	}

	@Override
	public void update(float deltaTime) {
		for (TouchEvent t: game.getInput().getTouchEvents()) {
			if (t.type == TouchEvent.TOUCH_UP) {
				if (sendErrorCauseInMail.isTouched(t.x, t.y)) {
					sendErrorCauseInMail();
					return;
				}
				if (saveErrorCause.isTouched(t.x, t.y)) {
					saveErrorCause();
					return;
				}
				if (restartAlite.isTouched(t.x, t.y)) {
					Intent intent = new Intent(game, AliteIntro.class);
					intent.putExtra(Alite.LOG_IS_INITIALIZED, true);
					game.startActivityForResult(intent, 0);
				}
			}
		}
	}

	private void saveErrorCause() {
		try {
			savedFilename = "crash_reports/report-" + new SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.getDefault()).format(new Date()) + ".txt";
			if (!game.getFileIO().exists("crash_reports")) {
				game.getFileIO().mkDir("crash_reports");
			}
			OutputStream file = game.getFileIO().writeFile(savedFilename);
			String message = getErrorReportText();
			file.write(message.getBytes());
			file.close();
		} catch (IOException ignored) {
			saveErrorCause.setText("Saving Failed");
			savedFilename = null;
			return;
		}
		saveErrorCause.setVisible(false);
	}

	private void sendErrorCauseInMail() {
		try {
			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {"alite.crash.report@gmail.com"});
			String subject = StringUtil.computeSHAString(plainCauseText);
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject.isEmpty() ?
				AliteConfig.GAME_NAME + " Crash Report." : subject);
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getErrorReportText());
			game.startActivityForResult(Intent.createChooser(emailIntent, "Sending email..."), 0);
		} catch (Throwable t) {
			Toast.makeText(game, "Request failed try again: " + t, Toast.LENGTH_LONG).show();
		}
	}

	private String getErrorReportText() {
		return "The following crash occurred in " + AliteConfig.GAME_NAME +
			" (" + AliteConfig.VERSION_STRING + "):\n\n" + plainCauseText +
			"\n\nDevice details:\n" + AliteLog.getDeviceInfo() +
			"\n\nGL Details:\n" + getGlDetails() +
			"\n\nMemory data:\n" + AliteLog.getMemoryData() +
			"\n\nCurrent date/time: " + new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss", Locale.getDefault()).format(new Date());
	}

	private static String getGlDetails() {
		StringBuilder result = new StringBuilder();
		result.append("OpenGL Vendor:   ").append(GLES11.glGetString(GLES11.GL_VENDOR)).append("\n").
			append("OpenGL Renderer: ").append(GLES11.glGetString(GLES11.GL_RENDERER)).append("\n").
			append("OpenGL Version:  ").append(GLES11.glGetString(GLES11.GL_VERSION)).append("\n");
		String extensions = GLES11.glGetString(GLES11.GL_EXTENSIONS);
		if (extensions != null) {
			result.append("Extensions:\n");
			for (String e: extensions.split(" ")) {
				result.append("  ").append(e).append("\n");
			}
		} else {
			result.append("No Extensions.\n");
		}
		return result.toString();
	}

	@Override
	public void activate() {
		StringBuilder buffer = new StringBuilder(cause == null ? "No additional information could be retrieved." :
			 cause.getMessage() == null ? cause.getClass().getName() : cause.getMessage());
		buffer.append("\n");
		while (cause != null) {
			if (cause.getStackTrace() != null) {
				for (StackTraceElement ste: cause.getStackTrace()) {
					buffer.append("-- at ");
					buffer.append(ste.getClassName());
					buffer.append(" .");
					buffer.append(ste.getMethodName());
					buffer.append("(");
					buffer.append(ste.getFileName());
					buffer.append(":");
					buffer.append(ste.getLineNumber());
					buffer.append(")\n");
				}
			}
			cause = cause.getCause();
			if (cause != null) {
				buffer.append("Caused by: ").append(cause.getMessage() == null ? cause.getClass().getName() : cause.getMessage());
			}
		}
		plainCauseText = buffer.toString();
		causeText = computeCRTextDisplay(game.getGraphics(), buffer.toString(), 20, 380, 1880, 40,
			ColorScheme.get(ColorScheme.COLOR_WARNING_MESSAGE), Assets.regularFont, false);
		sendErrorCauseInMail = Button.createGradientRegularButton(620, 180, 400, 110, "Send Error in Mail");
		saveErrorCause = Button.createGradientRegularButton(1070, 180, 400, 110, "Save Error to File");
		restartAlite = Button.createGradientRegularButton(1520, 180, 400, 110, "Restart Alite");
	}

	@Override
	public int getScreenCode() {
		return -1;
	}
}
