package com.cometway.io;

import java.io.*;

/**
 * This wrapper class wraps around a Reader and makes it look
 * and behave as an InputStream
 */
public class ReaderInputStream extends InputStream
{
	protected Reader reader;

	public ReaderInputStream(Reader in)
	{
		super();
		reader = in;
	}


	public int read() throws IOException
	{
		return(reader.read());
	}

	public int read(byte[] buffer) throws IOException
	{
		char[] tmp = new char[buffer.length];
		int rval = reader.read(tmp);
		for(int x=0;x<rval;x++) {
			buffer[x] = (byte)tmp[x];
		}
		return(rval);
	}

	public int read(byte[] buffer, int start, int length) throws IOException
	{
		char[] tmp = new char[buffer.length];
		int rval = reader.read(tmp,start,length);
		for(int x=start;x<length;x++) {
			buffer[x] = (byte)tmp[x];
		}
		return(rval);
	}

	public long skip(long num) throws IOException
	{
		return(reader.skip(num));
	}

	public int available() throws IOException
	{
		return(0);
	}

	public void close() throws IOException
	{
		reader.close();
	}

	public synchronized void mark(int index)
	{
		try {
			reader.mark(index);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void reset() throws IOException
	{
		reader.reset();
	}

	public boolean markSupported()
	{
		return(reader.markSupported());
	}
}
