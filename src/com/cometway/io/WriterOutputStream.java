package com.cometway.io;

import java.io.*;

/**
 * This wrapper class wraps around a Writer and makes it look
 * and behave as an OutputStream.
 */
public class WriterOutputStream extends OutputStream
{
	protected Writer writer;

	public WriterOutputStream(Writer out)
	{
		super();
		writer = out;
	}

	public void write(int data) throws IOException
	{
		writer.write(data);
	}

	public void write(byte[] buffer) throws IOException
	{
		char[] tmp = new char[buffer.length];
		for(int x=0;x<buffer.length;x++) {
			tmp[x] = (char)buffer[x];
		}
		writer.write(tmp);
	}

	public void write(byte[] buffer, int start, int length) throws IOException
	{
		char[] tmp = new char[length];
		for(int x=0;x<tmp.length;x++) {
			tmp[x] = (char)buffer[x+start];
		}
		writer.write(tmp);
	}

	public void flush() throws IOException
	{
		writer.flush();
	}

	public void close() throws IOException
	{
		writer.close();
	}

}
