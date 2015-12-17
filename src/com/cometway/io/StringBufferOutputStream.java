package com.cometway.io;

import java.io.*;

/**
 * This extension of java.io.OutputStream writes to a StringBuffer.
 * The StringBuffer passed in to the constructor of this class will
 * be filled with the data written to this OutputStream.
 */
public class StringBufferOutputStream extends OutputStream
{
	protected StringBuffer buffer;
	protected Object syncObject;

	protected boolean closed;

	/**
	 * The StringBuffer <b>out</b> will be filled with the data written
	 * to this OutputStream.
	 */
	public StringBufferOutputStream(StringBuffer out)
	{
		buffer = out;
		syncObject = new Object();
		closed = false;
	}

	

	/**
	 * This method sets the closed flag. Once this OutputStream has been
	 * closed, no other data can be written to this OutputStream.
	 */
	public void close()
	{
		try {
			synchronized(syncObject) {
				closed = true;
			}
		}
		catch(Exception e) {
			;
		}			
	}

	/**
	 * This method is blank. Data written to this OutputStream is always flushed.
	 */
	public void flush()
	{
		;
	}

	/**
	 * Write a single byte. Throws an IOException if the stream has been closed or 
	 * for some reason the OutputStream is out of sync.
	 */
	public void write(int b) throws IOException
	{
		try {
			synchronized(syncObject) {
				if(!closed) {
					char c = (char)(b & 0xFF);
					buffer.append(c);
				}
				else {
					throw new IOException();
				}
			}
		}
		catch(Exception e) {
			throw new IOException();
		}
	}

	/**
	 * Write an array of bytes. Throws an IOException if the stream has been closed or 
	 * for some reason the OutputStream is out of sync.
	 */
	public void write(byte[] b) throws IOException
	{
		try {
			synchronized(syncObject) {
				if(!closed) {
					char[] c = new char[b.length];
					for(int x=0;x<b.length;x++) {
						c[x] = (char)(b[x] & 0xFF);
					}
					buffer.append(c);
				}
				else {
					throw new IOException();
				}
			}
		}
		catch(Exception e) {
			throw new IOException();
		}
	}

	/**
	 * Write an array of bytes starting from <b>start</b> and writes <b>length</b> 
	 * number of bytes. Throws an IOException if the stream has been closed or 
	 * for some reason the OutputStream is out of sync.
	 */
	public void write(byte[] b, int start, int length) throws IOException
	{	
		try {
			synchronized(syncObject) {
				if(!closed) {
					char[] c = new char[length];
					for(int x=0;x<length;x++) {
						c[x] = (char)(b[x+start] & 0xFF);
					}
					buffer.append(c);
				}
				else {
					throw new IOException();
				}
			}
		}
		catch(Exception e) {
			throw new IOException();
		}
	}
}
