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

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Environment;
import de.phbouillon.android.framework.FileIO;
import de.phbouillon.android.games.alite.AliteConfig;
import de.phbouillon.android.games.alite.io.ObbExpansionsManager;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class AndroidFileIO implements FileIO {
	private final Context context;
	private String externalStoragePath;
	private final AssetManager assets;
	private final boolean useExternalStorage;

	public AndroidFileIO(Context context) {
		this.context = context;
		this.assets = context.getAssets();
		useExternalStorage = mountExternalStorage();
	}

	private String stripPath(String fileName) {
		return fileName.substring(fileName.lastIndexOf("/") + 1);
	}

	private boolean mountExternalStorage() {
		if (externalStoragePath != null) {
			return true;
		}
		String externalStorageState = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(externalStorageState)) {
			externalStoragePath = context.getExternalFilesDir(null).getAbsolutePath() + File.separator;
			return true;
		}
		return false;
	}

	@Override
	public InputStream readFile(String fileName) throws IOException {
		if (useExternalStorage) {
			return new FileInputStream(externalStoragePath + fileName);
		}
		return context.openFileInput(stripPath(fileName));

	}

	@Override
	public OutputStream writeFile(String fileName) throws IOException {
		if (useExternalStorage) {
			return new FileOutputStream(externalStoragePath + fileName);
		}
		return context.openFileOutput(stripPath(fileName), Context.MODE_PRIVATE);
	}

	@Override
	public OutputStream appendFile(String fileName) throws IOException {
		if (!exists(fileName)) {
			return writeFile(fileName);
		}
		if (useExternalStorage) {
			return new FileOutputStream(externalStoragePath + fileName, true);
		}
		return context.openFileOutput(stripPath(fileName), Context.MODE_APPEND);
	}

	@Override
	public boolean exists(String fileName) {
		if (useExternalStorage) {
			return new File(externalStoragePath + fileName).exists();
		}
		File file = context.getFileStreamPath(stripPath(fileName));
		return file.exists();
	}

	@Override
	public long fileLastModifiedDate(String fileName) {
		if (useExternalStorage) {
			return new File(externalStoragePath + fileName).lastModified();
		}
		File file = context.getFileStreamPath(stripPath(fileName));
		return file.lastModified();
	}

	@Override
	public boolean mkDir(String fileName) {
		if (useExternalStorage) {
			return new File(externalStoragePath + fileName).mkdirs();
		}
		// Ignore directories if internal storage is used...
		return true;
	}

	private File getFile(String fileName) {
		if (useExternalStorage) {
			return new File(externalStoragePath + fileName);
		}
		return context.getFileStreamPath(stripPath(fileName));
	}

	@Override
	public byte[] readFileContents(String fileName) throws IOException {
		FileInputStream fin = null;
		byte [] fileContent;
		try {
			File file;
			if (useExternalStorage) {
				file = new File(externalStoragePath + fileName);
				fin = new FileInputStream(file);
			} else {
				file = context.getFileStreamPath(stripPath(fileName));
				fin = context.openFileInput(stripPath(fileName));
			}
		    fileContent = new byte[(int) file.length()];
		    fin.read(fileContent);
		} finally {
			if (fin != null) {
				fin.close();
			}
		}
		return fileContent;
	}

	@Override
	public byte[] readPartialFileContents(String fileName, int length) throws IOException {
		byte [] fileContent;
		try(FileInputStream fin = useExternalStorage ?
				new FileInputStream(new File(externalStoragePath + fileName)) :
				context.openFileInput(stripPath(fileName))) {
		    fileContent = new byte[length];
		    fin.read(fileContent);
		}
		return fileContent;
	}

	@Override
	public byte[] readPartialFileContents(String fileName, int fromOffset, int length) throws IOException {
		byte [] fileContent;
		try(FileInputStream fin = useExternalStorage ?
				new FileInputStream(new File(externalStoragePath + fileName)) :
			context.openFileInput(stripPath(fileName))) {
			fileContent = new byte[fromOffset];
			fin.read(fileContent);
		    fileContent = new byte[length];
		    fin.read(fileContent);
		}
		return fileContent;
	}

	@Override
	public byte[] readFileContents(String fileName, int fromOffset) throws IOException {
		FileInputStream fin = null;
		byte [] fileContent;
		try {
			File file;
			if (useExternalStorage) {
				file = new File(externalStoragePath + fileName);
				fin = new FileInputStream(file);
			} else {
				file = context.getFileStreamPath(stripPath(fileName));
				fin = context.openFileInput(stripPath(fileName));
			}
			int bytes = (int) file.length() - fromOffset;
			fileContent = new byte[fromOffset];
			fin.read(fileContent);
			fileContent = new byte[bytes];
		    fin.read(fileContent);
		} finally {
			if (fin != null) {
				fin.close();
			}
		}
		return fileContent;
	}

	@Override
	public File[] getFiles(final String directory, final String fileNamePattern) {
		File root = useExternalStorage ? new File(externalStoragePath + directory + "/") :
					context.getFilesDir();
		return root.listFiles((dir, filename) -> dir.getName().matches(directory) && filename.matches(fileNamePattern));
	}

	@Override
	public boolean deleteFile(String fileName) {
		if (useExternalStorage) {
			return new File(externalStoragePath + fileName).delete();
		}
		return context.deleteFile(stripPath(fileName));
	}

	@Override
	public boolean copyFile(String srcFileName, String dstFileName) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(writeFile(dstFileName));
		BufferedInputStream bis = new BufferedInputStream(readFile(srcFileName));
		byte [] buffer = new byte[2048];
		int length;
		do {
			length = bis.read(buffer);
			if (length != -1) {
				bos.write(buffer, 0, length);
			}
		} while (length != -1);
		bis.close();
		bos.close();
		return true;
	}

	@Override
	public void zip(String zipName, String ...fileNames) throws IOException {
		BufferedInputStream origin;
		File f = getFile(zipName);
	    FileOutputStream dest = new FileOutputStream(f);
	    ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

	    byte data[] = new byte[65536];
	    for (String fileName: fileNames) {
	    	FileInputStream fi = new FileInputStream(getFile(fileName));
	    	origin = new BufferedInputStream(fi, 65536);
	    	ZipEntry entry = new ZipEntry(fileName.substring(fileName.lastIndexOf("/") + 1));
	    	out.putNextEntry(entry);
	    	int count;
	    	while ((count = origin.read(data, 0, 65536)) != -1) {
	    		out.write(data, 0, count);
	    	}
	    	origin.close();
	    }
	    out.close();
	}

	@Override
	public void unzip(File zipFile, File targetDirectory) throws IOException {
		try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
			ZipEntry ze;
			int count;
			byte[] buffer = new byte[8192];
			while ((ze = zis.getNextEntry()) != null) {
				File file = new File(targetDirectory, ze.getName());
				File dir = ze.isDirectory() ? file : file.getParentFile();
				if (!dir.isDirectory() && !dir.mkdirs())
					throw new FileNotFoundException("Failed to ensure directory: " +
						dir.getAbsolutePath());
				if (ze.isDirectory())
					continue;
				try (FileOutputStream fout = new FileOutputStream(file)) {
					while ((count = zis.read(buffer)) != -1)
						fout.write(buffer, 0, count);
				}
			}
		}
	}

	@Override
	public boolean existsPrivateFile(String fileName) {
		if (AliteConfig.HAS_EXTENSION_APK) {
			return new File(ObbExpansionsManager.getInstance().getMainRoot() + "assets/" + fileName).exists();
		}
		try {
			InputStream open = assets.open(fileName);
			open.close();
			return true;
		} catch (IOException ignored) {
			return false;
		}
	}

	@Override
	public InputStream readPrivateFile(String fileName) throws IOException {
		if (AliteConfig.HAS_EXTENSION_APK) {
			return new FileInputStream(getPrivatePath(fileName));
		}
		return assets.open(fileName);
	}

	public AssetFileDescriptor getFileDescriptor(String fileName) throws IOException {
		if (AliteConfig.HAS_EXTENSION_APK) {
			throw new IOException("Cannot access extension assets via AssetDescriptor. Use private path instead.");
		}
		return assets.openFd(fileName);
	}

	@Override
	public String getPrivatePath(String fileName) throws IOException {
		if (AliteConfig.HAS_EXTENSION_APK) {
			String path;
			if (ObbExpansionsManager.getInstance() == null) {
				throw new IOException("Obb not loaded. Please try all-in-one solution from " + AliteConfig.ALITE_WEBSITE);
			}
			if (!fileName.contains("de.phbouillon.android.games.alite")) {
				path = ObbExpansionsManager.getInstance().getMainRoot() + "assets/" + fileName;
			} else {
				int i = fileName.indexOf("assets/");
				path = ObbExpansionsManager.getInstance().getMainRoot() + fileName.substring(i) + fileName;
			}
			if (!new File(path).exists()) {
				throw new FileNotFoundException(path + " not found.");
			}
			return path;
		}
		throw new IOException("Cannot access local assets via path. Use AssetDescriptor instead.");
	}
}
