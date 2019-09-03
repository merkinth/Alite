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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.opengl.GLES11;
import android.os.Build;
import android.os.Debug;
import android.util.Log;
import de.phbouillon.android.framework.FileIO;
import de.phbouillon.android.games.alite.model.generator.StringUtil;

public class AliteLog implements Loggable {
	public static final int KB = 1024;
	public static final int MB = 1024 * KB;
	private static final int GB = 1024 * MB;

	private static Loggable instance;

	private FileIO fileIO;
	private String logFilename;
	private boolean first = true;
	private long started;

	public AliteLog(FileIO fileIO, String logFilename) {
		this.fileIO = fileIO;
		this.logFilename = logFilename;
	}

	public static void d(String title, String message) {
		instance.debug(title, message);
	}

	@Override
	public void debug(String title, String message) {
		if (!Settings.suppressOnlineLog) {
			Log.d(title, message);
		}
		if (Settings.memDebug && Settings.onlineMemDebug) {
			Log.d("Mem Dump", getMemoryData());
		}
		internalWrite("[Debug]", title, message, null);
	}

	public static void w(String title, String message) {
		instance.warning(title, message);
	}

	@Override
	public void warning(String title, String message) {
		if (!Settings.suppressOnlineLog) {
			Log.w(title, message);
		}
		if (Settings.memDebug && Settings.onlineMemDebug) {
			Log.w("Mem Dump", getMemoryData());
		}
		internalWrite("[Warning]", title, message, null);
	}

	public static void e(String title, String message) {
		instance.error(title, message);
	}

	@Override
	public void error(String title, String message) {
		if (!Settings.suppressOnlineLog) {
			Log.e(title, message);
		}
		if (Settings.memDebug && Settings.onlineMemDebug) {
			Log.d("Mem Dump", getMemoryData());
		}
		internalWrite("[Error]", title, message, null);
	}

	public static void e(String title, String message, Throwable cause) {
		instance.error(title, message, cause);
	}

	@Override
	public void error(String title, String message, Throwable cause) {
		if (!Settings.suppressOnlineLog) {
			Log.e(title, message, cause);
		}
		if (Settings.memDebug && Settings.onlineMemDebug) {
			Log.d("Mem Dump", getMemoryData());
		}
		internalWrite("[Error]", title, message, cause);
	}

	private String toReadableMemString(long memory) {
		if (memory > GB) {
			return StringUtil.format("%3.2f GB", (float)memory / GB);
		}
		if (memory > MB) {
			return StringUtil.format("%5.2f MB", (float)memory / MB);
		}
		if (memory > KB) {
			return StringUtil.format("%5.2f KB", (float)memory / KB);
		}
		return StringUtil.format("%d Bytes", memory);
	}

	public static void dumpStack(String title, String message) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		new Throwable("stack dump").printStackTrace(writer);
		writer.close();
		try {
			stringWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		instance.debug(title, message + " - " + stringWriter);
	}

	@Override
	public String getMemoryData() {
		return "FRM: " + toReadableMemString(Runtime.getRuntime().freeMemory()) +
			   ", MRM: " + toReadableMemString(Runtime.getRuntime().maxMemory()) +
			   ", TRM: " + toReadableMemString(Runtime.getRuntime().totalMemory()) +
			   ", FNM: " + toReadableMemString(Debug.getNativeHeapFreeSize()) +
			   ", ANM: " + toReadableMemString(Debug.getNativeHeapAllocatedSize()) +
			   ", TNM: " + toReadableMemString(Debug.getNativeHeapSize()) + "\n";
	}

	@Override
	public String getDeviceInfo() {
		return "Android version: " + Build.VERSION.RELEASE + "\n" +
               "Device: " + Build.DEVICE + "\n" +
	           "Product: " + Build.PRODUCT + "\n" +
               "Brand: " + Build.BRAND + "\n" +
	           "Display: " + Build.DISPLAY + "\n" +
               "Manufacturer: " + Build.MANUFACTURER + "\n" +
	           "Model: " + Build.MODEL + "\n";

	}

	private void outputDeviceInfo(OutputStream logFile) throws IOException {
		logFile.write(getDeviceInfo().getBytes());
	}

	private void internalWrite(String tag, String title, String message, Throwable cause) {
		if (!Settings.logToFile || fileIO == null) {
			return;
		}
		try {
			fileIO.mkDir("logs");
			OutputStream logFile = fileIO.appendFile(logFilename);
			String errorText = null;
			if (cause != null) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				cause.printStackTrace(pw);
				pw.close();
				errorText = cause.getMessage() + "\n" + sw;
			}
			if (first) {
				first = false;
				started = System.currentTimeMillis();
				logFile.write(("Info - " + AliteConfig.GAME_NAME + " Started - " +
					AliteConfig.GAME_NAME + " version " + AliteConfig.VERSION_STRING + " started on " +
					SimpleDateFormat.getDateTimeInstance().format(new Date()) + "\n").getBytes());
				outputDeviceInfo(logFile);
			}
			long current = System.currentTimeMillis();

			String result = "[" + SimpleDateFormat.getTimeInstance().format(new Date(current)) + ", " +
				(current - started) + "ms] - " + tag + " - " + title + " - " + message +
				(errorText == null ? "" : " - " + errorText) + "\n";
//			result += getMemoryData();
			logFile.write(result.getBytes());
			logFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getGlVendorData(int name) {
		return GLES11.glGetString(name);
	}

	public static void debugGlVendorData() {
		String vendor = instance.getGlVendorData(GLES11.GL_VENDOR);
		String renderer = instance.getGlVendorData(GLES11.GL_RENDERER);
		String version = instance.getGlVendorData(GLES11.GL_VERSION);
		String extensions = instance.getGlVendorData(GLES11.GL_EXTENSIONS);
		d("GL Vendor Data", "Vendor:   " + vendor);
		d("GL Vendor Data", "Renderer: " + renderer);
		d("GL Vendor Data", "Version:  " + version);
		if (extensions != null) {
			for (String e: extensions.split(" ")) {
				d("GL Vendor Data", "Extension: " + e);
			}
		}
	}

	public static void initialize(FileIO fileIO) {
		if (instance == null) {
			instance = new AliteLog(fileIO, "logs/AliteLog-" +
				new SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(new Date()) + ".txt");
		}
	}

	public static Loggable getInstance() {
		return instance;
	}

	public static void setInstance(Loggable instance) {
		AliteLog.instance = instance;
	}
}
