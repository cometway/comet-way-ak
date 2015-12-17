package com.cometway.httpd;

import java.io.*;

/**
 * This extension of InputStream allows only a fixed number of bytes
 * to be read from the stream before acting as the Stream is empty.
 * The bytes that are read from this stream are automatically decoded
 * via HTTP parameter decoding.
 */
public class HTTPDecoderStream extends InputStream
{
	protected InputStream sourceStream;
	protected int dataLength;
	protected int count;
	public boolean decodePlusSign = true;

	public HTTPDecoderStream(InputStream source) throws IOException
	{
		sourceStream = source;
		dataLength = -1;
	}

	public HTTPDecoderStream(InputStream source, int length) throws IOException
	{
		sourceStream = source;
		dataLength = length;
	}




	public int read() throws IOException
	{
		if(dataLength != -1) {
			if(count<dataLength) {
				int data = sourceStream.read();
				count++;
				if(((char)data)=='%') {
					String s = "%";
					data = sourceStream.read();
					count++;
					s = s+((char)data);
					data = sourceStream.read();
					count++;
					s = s+((char)data);
					return((int)HTMLStringTools.decode(s).charAt(0));
				}
				else {
					if(decodePlusSign) {
						if(((char)data)=='+') {
							return(((int)' '));
						}
						else {
							return(data);
						}
					}
					else {
						return(data);
					}
				}
			}
			else {
				return(-1);
			}
		}
		else {
			int data = sourceStream.read();
			if(((char)data)=='%') {
				String s = "%";
				data = sourceStream.read();
				s = s+((char)data);
				data = sourceStream.read();
				s = s+((char)data);
				return((int)HTMLStringTools.decode(s).charAt(0));
			}
			else {
				if(decodePlusSign) {
					if(((char)data)=='+') {
						return(((int)' '));
					}
					else {
						return(data);
					}
				}
				else {
					return(data);
				}
			}
		}
	}


	public int read(byte[] buffer) throws IOException
	{
		int data = read();
		if(data!=-1) {
			buffer[0] = (byte)data;
			return(1);
		}
		else {
			return(0);
		}
	}

	public int read(byte[] buffer, int start, int length) throws IOException
	{
		int data = read();
		if(data!=-1) {
			buffer[start] = (byte)data;
			return(1);
		}
		else {
			return(0);
		}
	}

	public long skip(long i) throws IOException
	{
		throw(new IOException("Unsupported"));
	}

	public int available() throws IOException
	{
		throw(new IOException("Unsupported"));
	}

	public void close() throws IOException
	{
		sourceStream.close();
	}
	
	public synchronized void mark(int i)
	{
		;
	}

	public synchronized void reset() throws IOException
	{
		throw(new IOException("Unsupported"));
	}

	public boolean markSupported()
	{
		return(false);
	}




	public static void main(String[] args)
	{
		try {
			File f = new File(args[0]);
			BufferedReader in = new BufferedReader(new FileReader(f));
			File f2 = new File(args[1]);
			FileWriter out = new FileWriter(f2);

			StringBuffer content = new StringBuffer();

			String line = in.readLine();
			while(line!=null) {
				content.append(line);
				content.append("\n");
				line = in.readLine();
			}

			out.write(HTMLStringTools.encode(content.toString()));
			out.flush();


			HTTPDecoderStream test = new HTTPDecoderStream(new FileInputStream(f2),100);
			int data = test.read();
			while(data!=-1) {
				System.out.print((char)data);
				data = test.read();
			}
		}
		catch(Exception e) {e.printStackTrace();}

	}
}
