package de.phbouillon.android.framework;

import java.io.IOException;
import java.io.InputStream;

public interface ResourceStream {
	InputStream getStream(String fileName) throws IOException;
}
