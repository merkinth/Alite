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

import java.io.*;

public class TestFileIO implements FileIO {
	@Override
	public String getFileName(String fileName) {
		return null;
	}

	@Override
	public InputStream readFile(String fileName) throws IOException {
		return null;
	}

	@Override
	public OutputStream writeFile(String fileName) throws IOException {
		return new FileOutputStream(fileName);
	}

	@Override
	public OutputStream appendFile(String fileName) throws IOException {
		return null;
	}

	@Override
	public long fileLastModifiedDate(String fileName) {
		return 0;
	}

	@Override
	public boolean exists(String fileName) {
		return false;
	}

	@Override
	public boolean mkDir(String fileName) {
		return false;
	}

	@Override
	public byte[] readFileContents(String fileName) throws IOException {
		return new byte[0];
	}

	@Override
	public byte[] readPartialFileContents(String fileName, int length) throws IOException {
		return new byte[0];
	}

	@Override
	public byte[] readPartialFileContents(String fileName, int offset, int length) throws IOException {
		return new byte[0];
	}

	@Override
	public byte[] readFileContents(String fileName, int fromOffset) throws IOException {
		return new byte[0];
	}

	@Override
	public File[] getFiles(String directory, String fileNamePattern) {
		return new File[0];
	}

	@Override
	public boolean deleteFile(String fileName) {
		return false;
	}

	@Override
	public void copyFile(String srcFileName, String dstFileName) throws IOException {

	}

	@Override
	public void zip(String zipName, String... fileNames) throws IOException {

	}

	@Override
	public void unzip(File zipFile, File targetDirectory) throws IOException {

	}

	@Override
	public Object getPrivatePath(String fileName) throws IOException {
		return null;
	}

	@Override
	public InputStream readPrivateFile(String fileName) throws IOException {
		return null;
	}

	@Override
	public boolean existsPrivateFile(String fileName) {
		return false;
	}

	@Override
	public InputStream readAssetFile(String fileName) throws IOException {
		return null;
	}

	@Override
	public boolean existsAssetFile(String fileName) {
		return false;
	}
}
