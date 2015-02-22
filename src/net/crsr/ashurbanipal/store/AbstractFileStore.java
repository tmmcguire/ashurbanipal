package net.crsr.ashurbanipal.store;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

/*
 * Base file storage for processed data.
 * 
 * The file is written in such a way as to attempt to preserve a backup of
 * the previous version and the validity of the current version.
 * 
 * The file is read (if possible) when the object is created.
 */
abstract public class AbstractFileStore {

	protected final File file;
	protected boolean valid = false;
	
	abstract protected void readData(BufferedReader r) throws IOException;
	abstract protected void writeData(OutputStream w) throws IOException;
	
	protected AbstractFileStore(String filename) throws IOException {
		this.file = new File(filename);
	}
	
	public void read() throws IOException {
		if (! file.exists()) { return; }
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(new FileInputStream(this.file)));
			this.readData(r);
		} finally {
			if (r != null) { r.close(); }
		}
	}
	
	public void write() throws IOException {
		final File newFile = new File(this.file.getAbsolutePath() + ".new");
		OutputStream w = null;
		try {
			w = new FileOutputStream(newFile);
			this.writeData(w);
			this.valid = true;
		} finally {
			if (w != null) { w.close(); }
		}
		if (this.file.exists()) {
			this.file.renameTo(new File(file.getAbsolutePath() + ".bak"));
		}
		newFile.renameTo(file);
	}
}
