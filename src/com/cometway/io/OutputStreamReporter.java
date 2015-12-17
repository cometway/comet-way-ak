
package com.cometway.io;


import com.cometway.util.ReporterInterface;
import java.io.OutputStream;
import java.io.IOException;


/**
* This uses an OutputStream for directing reporting information.
* Output may be lost if the OutputStream throws an IOException.
*/

public class OutputStreamReporter implements ReporterInterface
{
	private final static byte[] DEBUG_BEFORE   = new byte[] { '(' };
	private final static byte[] DEBUG_AFTER    = new byte[] { ')', ' ' };
	private final static byte[] ERROR_BEFORE   = new byte[] { '!' };
	private final static byte[] ERROR_AFTER    = new byte[] { '!', ' ' };
	private final static byte[] PRINTLN_BEFORE = new byte[] { '[' };
	private final static byte[] PRINTLN_AFTER  = new byte[] { ']', ' ' };
	private final static byte[] WARNING_BEFORE = new byte[] { '?' };
	private final static byte[] WARNING_AFTER  = new byte[] { '?', ' ' };

	protected final byte[] synchObject = new byte[0];

	protected OutputStream out;
	protected OutputStream err;
	protected byte[] endOfLine;


	/**
	* Constructor for combined println, debug, warning, and error output to one OutputStream.
	* @param out The OutputStream to be used for all reporting output.
	*/

	public OutputStreamReporter(OutputStream out)
	{
		this.out = out;
		this.err = out;
		this.endOfLine = System.getProperty("line.separator").getBytes();
	}



	/**
	* Constructor for sending println, debug reporting to one OutputStream, 
	* and warning and error output to another OutputStream.
	* @param out The OutputStream to be used for println and debug output.
	* @param err The OutputStream to be used for warning and error output.
	*/

	public OutputStreamReporter(OutputStream out, OutputStream err)
	{
		this.out = out;
		this.err = err;
		this.endOfLine = System.getProperty("line.separator").getBytes();
	}



	/**
	* Constructor for sending println, debug reporting to one OutputStream, 
	* and warning and error output to another OutputStream. An alternate line
	* separator byte sequence can be specified (System.getProperty("line.separator")
	* is used by default for other constructors).
	* @param out The OutputStream to be used for println and debug output.
	* @param err The OutputStream to be used for warning and error output.
	* @param lineSeparator Contains the sequence of bytes to be sent when the end of a line has been reached.
	*/

	public OutputStreamReporter(OutputStream out, OutputStream err, String lineSeparator)
	{
		this.out = out;
		this.err = err;
		this.endOfLine = lineSeparator.getBytes();
	}



	/**
	* Use this method to report an event for debugging.
	*/

	public void debug(Object objectRef, String message)
	{
		try
		{
			synchronized (synchObject)
			{
				OutputStream o = out;
				o.write(DEBUG_BEFORE);
				o.write(objectRef.toString().getBytes());
				o.write(DEBUG_AFTER);
				o.write(message.getBytes());
				o.write(endOfLine);
				o.flush();
			}
		}
		catch (IOException x)
		{
		}
	}


	/**
	* Use this method to report a warning message.
	*/

	public void warning(Object objectRef, String message)
	{
		try
		{
			synchronized (synchObject)
			{
				OutputStream o = err;
				o.write(WARNING_BEFORE);
				o.write(objectRef.toString().getBytes());
				o.write(WARNING_AFTER);
				o.write(message.getBytes());
				o.write(endOfLine);
				o.flush();
			}
		}
		catch (IOException x)
		{
		}
	}


	/**
	* Use this method to report a warning message with an Exception.
	*/

	public void warning(Object objectRef, String message, Exception e)
	{
		try
		{
			synchronized (synchObject)
			{
				OutputStream o = err;
				o.write(WARNING_BEFORE);
				o.write(objectRef.toString().getBytes());
				o.write(WARNING_AFTER);
				o.write(message.getBytes());
				o.write(endOfLine);
				o.write(e.toString().getBytes());
				o.write(endOfLine);
				o.flush();
			}
		}
		catch (IOException x)
		{
		}
	}


	/**
	* Use this method to report a critical error with an Exception.
	*/

	public void error(Object objectRef, String message)
	{
		try
		{
			synchronized (synchObject)
			{
				OutputStream o = err;
				o.write(ERROR_BEFORE);
				o.write(objectRef.toString().getBytes());
				o.write(ERROR_AFTER);
				o.write(message.getBytes());
				o.write(endOfLine);
				o.flush();
			}
		}
		catch (IOException x)
		{
		}
	}


	/**
	* Use this method to report a critical error message.
	*/

	public void error(Object objectRef, String message, Exception e)
	{
		try
		{
			synchronized (synchObject)
			{
				OutputStream o = err;
				o.write(ERROR_BEFORE);
				o.write(objectRef.toString().getBytes());
				o.write(ERROR_AFTER);
				o.write(message.getBytes());
				o.write(endOfLine);
				o.write(e.toString().getBytes());
				o.write(endOfLine);
				o.flush();
			}
		}
		catch (IOException x)
		{
		}
	}


	/**
	* Use this method to report an event message.
	*/

	public void println(Object objectRef, String message)
	{
		try
		{
			synchronized (synchObject)
			{
				OutputStream o = out;
				o.write(PRINTLN_BEFORE);
				o.write(objectRef.toString().getBytes());
				o.write(PRINTLN_AFTER);
				o.write(message.getBytes());
				o.write(endOfLine);
				o.flush();
			}
		}
		catch (IOException x)
		{
		}
	}
}


