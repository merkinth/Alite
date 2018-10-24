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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileIO {
	InputStream readFile(String fileName) throws IOException;
	OutputStream writeFile(String fileName) throws IOException;
	OutputStream appendFile(String fileName) throws IOException;
	long fileLastModifiedDate(String fileName);
	boolean exists(String fileName);
	boolean mkDir(String fileName);
	byte[] readFileContents(String fileName) throws IOException;
	byte[] readPartialFileContents(String fileName, int length) throws IOException;
	byte[] readPartialFileContents(String fileName, int offset, int length) throws IOException;
	byte[] readFileContents(String fileName, int fromOffset) throws IOException;
	File[] getFiles(String directory, String fileNamePattern);
	boolean deleteFile(String fileName);
	void copyFile(String srcFileName, String dstFileName) throws IOException;
	void zip(String zipName, String ...fileNames) throws IOException;
	void unzip(File zipFile, File targetDirectory) throws IOException;

	InputStream readPrivateFile(String fileName) throws IOException;
	Object getPrivatePath(String fileName) throws IOException;
	boolean existsPrivateFile(String fileName);
}
