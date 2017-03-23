package ca.barelabs.bareservice.internal;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


public class GZipServletResponseWrapper extends HttpServletResponseWrapper implements Closeable {
	
	private Flushable mOutput;
	

	public GZipServletResponseWrapper(HttpServletResponse response) {
		super(response);
	}

	@Override
	public void flushBuffer() throws IOException {
		if (mOutput != null) {
			mOutput.flush();
		}
		super.flushBuffer();
	}
	
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (mOutput instanceof PrintWriter) {
			throw new IllegalStateException("PrintWriter already obtained. Only one of PrintWriter or OutputStream can be obtained.");
	    }
		if (mOutput == null) {
			mOutput = new GZipServletOutputStream(getResponse().getOutputStream());
		}
	    return (ServletOutputStream) mOutput;
	}
	
	@Override
	public PrintWriter getWriter() throws IOException {
		if (mOutput instanceof ServletOutputStream) {
			throw new IllegalStateException("OutputStream already obtained. Only one of PrintWriter or OutputStream can be obtained.");
		}
		if (mOutput == null) {
			ServletOutputStream out = new GZipServletOutputStream(getResponse().getOutputStream());
			mOutput = new PrintWriter(new OutputStreamWriter(out, getResponse().getCharacterEncoding()));
		}
		return (PrintWriter) mOutput;
	}
	
	@Override
	public void setContentLength(int len) {
		// Would normally call through and set the length of the content body in the response (setting the HTTP Content-Length header)
		// but since the encoded content length will not be the same as the decoded content length we don't want to set this
	}
	
	@Override
	public void close() throws IOException {
		flushBuffer();
		if (mOutput instanceof Closeable) {
			((Closeable) mOutput).close();
		}
	}
}
