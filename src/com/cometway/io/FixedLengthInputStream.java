package com.cometway.io;

import java.io.*;

/**
 * This subclass of InputStream acts as a wrapper to control the amount of
 * data which can be read from the Stream. It is instantiated with a fixed
 * limit, when that number of bytes have been read from this stream, no further
 * data can be read.
 */

public class FixedLengthInputStream extends InputStream
{   
    	InputStream source;
	int limit;
	int bytesRead;

    public boolean allowStreamClose;

	public FixedLengthInputStream(InputStream reader, int readLimit)
	{
		super();
		source = reader;
		limit = readLimit;
		bytesRead = 0;
	}

	public int read() throws java.io.IOException
	{
		if(bytesRead < limit) {
			bytesRead++;
			return(source.read());	
		}
		else {
			throw(new IOException());
		}
	}

	
	public int read(byte[] in) throws java.io.IOException
	{
		if(bytesRead < limit) {
			bytesRead++;
			in[0] = (byte)source.read();
			return(1);
		}
		else {
			return(-1);
		}

	}

	public int read(byte[] in, int a, int b) throws java.io.IOException
	{
	    if(bytesRead < limit) {
		int data = source.read();
		in[a] = (byte)data;
		bytesRead++;
		return(1);
	    }
	    else {
		System.out.println("reached limit");
		return(-1);
	    }
	}

	public long skip(long num) throws IOException 
	{
		return(source.skip(num));
	}

	public int available() throws IOException
	{
		return(source.available());
	}


	public void close() throws java.io.IOException
	{
	    //		System.out.println("CLOSE!");
	    if(allowStreamClose) {
		source.close();
	    }
	}

	public synchronized void mark(int num)
	{
		source.mark(num);
	}

	public synchronized void reset() throws IOException
	{
		source.reset();
	}

	public boolean markSupported()
	{
		return(false);
	}
}
 


