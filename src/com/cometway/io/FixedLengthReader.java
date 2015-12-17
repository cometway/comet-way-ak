package com.cometway.io;


import java.io.*;


/**
 * This subclass of ReaderInputStream is a wrapper for a Reader that limits
 * the number of bytes that can be read from this InputStream. 
 *
 */

public class FixedLengthReader extends ReaderInputStream
{   
	int limit;
	int bytesRead;

    public boolean allowStreamClose;

	public FixedLengthReader(Reader reader, int readLimit)
	{
		super(reader);
		limit = readLimit;
		bytesRead = 0;
	}

	public int read() throws java.io.IOException
	{
		if(bytesRead < limit) {
			bytesRead++;
			return(reader.read());	
		}
		else {
			throw(new IOException());
		}
	}

	
	public int read(byte[] in) throws java.io.IOException
	{
		if(bytesRead < limit) {
			bytesRead++;
			in[0] = (byte)reader.read();
			return(1);
		}
		else {
			return(-1);
		}

	}

	public int read(byte[] in, int a, int b) throws java.io.IOException
	{
	    if(bytesRead < limit) {
		int data = reader.read();
		in[a] = (byte)data;
		bytesRead++;
		return(1);
	    }
	    else {
		return(-1);
	    }
	}



	public void close() throws java.io.IOException
	{
	    if(allowStreamClose) {
		reader.close();
	    }
	}


}
