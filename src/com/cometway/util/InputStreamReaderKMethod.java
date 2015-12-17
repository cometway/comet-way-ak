
package com.cometway.util;

import java.io.*;


/**
 * This method extends com.cometway.util.KMethod and implements the Runnable 
 * interface, which allows this KMethod to be executed by a PooledThread from
 * a ThreadPool or another Thread. When executed, the InputStream given to 
 * this class is read from until either an End Of Transmission character is 
 * reached or the Thread is stopped.
 */

public class InputStreamReaderKMethod extends KMethod implements Runnable
{
	InputStream     in;
	StringBuffer    processOut;
	int		stopChar;


	/**
	 * Set this to the Object to notify() when the read has completed.
	 */

	public Object   finishedReadNotify;


	/**
	 * This is set to the Thread that is executing this KMethod upon execution.
	 */

	public Thread   currentThread;


	/**
	 * This flag is set to TRUE when this KMethod is being executed.
	 */

	public boolean  isRunning;


	/**
	 * Set this flag to true if you want Line Feeds to be replaced by Carriage Returns.
	 */

	public boolean  changeLFtoCR;


	/**
	 * Set this flag to true if you want to be Carriage Returns replaced by Line Feeds.
	 */

	public boolean  changeCRtoLF;		// public boolean changeLFCRtoCR;


	// public boolean changeLFCRtoLF;


	/**
	 * Set this to a value greater than zero and only this many characters will be read.
	 */

	public int      readCharLimit;

	public InputStreamReaderKMethod(InputStream inputStream)
	{
		this(inputStream, null, -1);
	}


	public InputStreamReaderKMethod(InputStream inputStream, StringBuffer readBuffer)
	{
		this(inputStream, readBuffer, -1);
	}


	public InputStreamReaderKMethod(InputStream inputStream, int endOfTransmissionChar)
	{
		this(inputStream, null, endOfTransmissionChar);
	}


	public InputStreamReaderKMethod(InputStream inputStream, StringBuffer readBuffer, int endOfTransmissionChar)
	{
		in = inputStream;
		processOut = readBuffer;
		stopChar = endOfTransmissionChar;
	}


	/**
	 * Overrides KMethod.execute()
	 */


	public void execute()
	{
		isRunning = true;

		run();
	}


	/**
	 * This starts reading the InputStream. This method also sets the 'isRunning'
	 * and the 'currentThread' fields.
	 */


	public void run()
	{		// System.out.println("Reader Running");
		currentThread = Thread.currentThread();
		isRunning = true;

		if (processOut == null)
		{
			processOut = new StringBuffer();
		}

		int     charCount = 0;
		byte[] buffer = new byte[1024];
		int bytesRead = 0;

		try
		{
			bytesRead = in.read(buffer);
			if(changeLFtoCR) {
				while(bytesRead>0) {
					String tmp = new String(buffer,0,bytesRead);
					int index = tmp.indexOf("\n");
					while(index!=-1) {
						tmp = tmp.substring(0,index)+"\r"+tmp.substring(index+1);
						index = tmp.indexOf("\n");
					}
					processOut.append(tmp);
					charCount = charCount+bytesRead;
					if(readCharLimit>0 && charCount>readCharLimit) {
						break;
					}
					bytesRead = in.read(buffer);
				}					
			}
			else if(changeCRtoLF) {
				while(bytesRead>0) {
					String tmp = new String(buffer,0,bytesRead);
					int index = tmp.indexOf("\r");
					while(index!=-1) {
						tmp = tmp.substring(0,index)+"\n"+tmp.substring(index+1);
						index = tmp.indexOf("\r");
					}
					processOut.append(tmp);
					charCount = charCount+bytesRead;
					if(readCharLimit>0 && charCount>readCharLimit) {
						break;
					}
					bytesRead = in.read(buffer);
				}					
			}
			else {
				while(bytesRead>0) {
					processOut.append(new String(buffer,0,bytesRead));
					charCount = charCount+bytesRead;
					if(readCharLimit>0 && charCount>readCharLimit) {
						break;
					}
					bytesRead = in.read(buffer);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		isRunning = false;

		if (finishedReadNotify != null)
		{
			synchronized (finishedReadNotify)
			{		// System.out.println("notifying 'finishedReadNotify', chars read="+charCount);
				finishedReadNotify.notify();
			}
		}

		currentThread = null;
	}


	/**
	 * This method returns the StringBuffer that the InputStream is read into.
	 */


	public StringBuffer getBuffer()
	{
		return (processOut);
	}


	/**
	 * This method stops the Thread that is executing this KMethod. If the Thread
	 * is a PooledThread, it should be released from its ThreadPool before calling
	 * this method. This method calls Thread.stop() on the Thread executing this
	 * KMethod.
	 */


	public void stopReadThread()
	{
		if (isRunning)
		{
			if (currentThread != null)
			{
				try
				{
					currentThread.interrupt();
//					currentThread.stop();
				}
				catch (Exception e)
				{
					;
				}
			}

			isRunning = false;
		}
	}


}

