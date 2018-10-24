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
		return fileName.substring(fileName.lastIndexOf(File.separator) + 1);
	}

	private boolean mountExternalStorage() {
		if (externalStoragePath != null) {
			return true;
		}
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
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
		return writeFileWithMode(fileName, Context.MODE_PRIVATE);
	}

	private OutputStream writeFileWithMode(String fileName, int mode) throws FileNotFoundException {
		if (useExternalStorage) {
			return new FileOutputStream(externalStoragePath + fileName, mode == Context.MODE_APPEND);
		}
		return context.openFileOutput(stripPath(fileName), mode);
	}

	@Override
	public OutputStream appendFile(String fileName) throws IOException {
		if (!exists(fileName)) {
			return writeFile(fileName);
		}
		return writeFileWithMode(fileName, Context.MODE_APPEND);
	}

	@Override
	public boolean exists(String fileName) {
		return getFile(fileName).exists();
	}

	@Override
	public long fileLastModifiedDate(String fileName) {
		return getFile(fileName).lastModified();
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
		File file = getFile(fileName);
		return readPartialFileContents(file, (int) file.length());
	}

	private byte[] readPartialFileContents(File file, int length) throws IOException {
		try(FileInputStream fin = readFile(file)) {
			byte[] fileContent = new byte[length];
			fin.read(fileContent);
			return fileContent;
		}
	}

	private FileInputStream readFile(File file) throws IOException {
		if (useExternalStorage) {
			return new FileInputStream(file);
		}
		return context.openFileInput(file.getPath());
	}

	@Override
	public byte[] readPartialFileContents(String fileName, int length) throws IOException {
		return readPartialFileContents(getFile(fileName), length);
	}

	@Override
	public byte[] readPartialFileContents(String fileName, int fromOffset, int length) throws IOException {
		return readFileContents(getFile(fileName), fromOffset, length);
	}

	private byte[] readFileContents(File file, int fromOffset, int length) throws IOException {
		try (FileInputStream fin = readFile(file)) {
			byte[] fileContent = new byte[fromOffset];
			fin.read(fileContent);
			fileContent = new byte[length];
			fin.read(fileContent);
			return fileContent;
		}
	}

	@Override
	public byte[] readFileContents(String fileName, int fromOffset) throws IOException {
		File file = getFile(fileName);
		return readFileContents(file, fromOffset, (int) file.length() - fromOffset);
	}

	@Override
	public File[] getFiles(final String directory, final String fileNamePattern) {
		File root = useExternalStorage ?
			new File(externalStoragePath + directory + File.separator) : context.getFilesDir();
		return root.listFiles((dir, filename) -> dir.getName().matches(directory) && filename.matches(fileNamePattern));
	}

	@Override
	public boolean deleteFile(String fileName) {
		return getFile(fileName).delete();
	}

	@Override
	public void copyFile(String srcFileName, String dstFileName) throws IOException {
		try(BufferedOutputStream bos = new BufferedOutputStream(writeFile(dstFileName));
				BufferedInputStream bis = new BufferedInputStream(readFile(srcFileName))) {
			byte[] buffer = new byte[2048];
			int length;
			do {
				length = bis.read(buffer);
				if (length != -1) {
					bos.write(buffer, 0, length);
				}
			} while (length != -1);
		}
	}

	@Override
	public void zip(String zipName, String ...fileNames) throws IOException {
		BufferedInputStream origin;
		try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(getFile(zipName))))) {
			byte data[] = new byte[65536];
			for (String fileName : fileNames) {
				FileInputStream fi = new FileInputStream(getFile(fileName));
				origin = new BufferedInputStream(fi, 65536);
				ZipEntry entry = new ZipEntry(fileName.substring(fileName.lastIndexOf(File.separator) + 1));
				out.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, 65536)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();
			}
		}
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
					throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
				if (ze.isDirectory()) {
					continue;
				}
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
			assets.open(fileName).close();
			return true;
		} catch (IOException ignored) {
			return false;
		}
	}

	@Override
	public InputStream readPrivateFile(String fileName) throws IOException {
		if (AliteConfig.HAS_EXTENSION_APK) {
			return new FileInputStream((String) getPrivatePath(fileName));
		}
		return assets.open(fileName);
	}

	@Override
	public Object getPrivatePath(String fileName) throws IOException {
		if (AliteConfig.HAS_EXTENSION_APK) {
			if (ObbExpansionsManager.getInstance() == null) {
				throw new IOException("Obb not loaded. Please try all-in-one solution from " + AliteConfig.ALITE_WEBSITE);
			}
			String path = ObbExpansionsManager.getInstance().getMainRoot() +
				(fileName.contains("de.phbouillon.android.games.alite") ?
					fileName.substring(fileName.indexOf("assets/")) : "assets/") + fileName;
			if (!new File(path).exists()) {
				throw new FileNotFoundException(path + " not found.");
			}
			return path;
		}
		return assets.openFd(fileName);
	}
}
