package ca.barelabs.bareservice.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;


public class GZipServletOutputStream extends ServletOutputStream {

	private final GZIPOutputStream out;
	

	public GZipServletOutputStream(OutputStream output) throws IOException {
		super();
		out = new GZIPOutputStream(output);
	}

	@Override
	public void close() throws IOException {
		out.close();
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public void write(byte b[]) throws IOException {
		out.write(b);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		out.write(b, off, len);
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}
}
