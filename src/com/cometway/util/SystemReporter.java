
package com.cometway.util;


import com.cometway.io.StringBufferOutputStream;
import java.io.PrintWriter;


/**
* Each ReporterInterface method call is output using System.out.println or System.err.println
* exactly once.
* Calls to println and debug are output to System.out.
* Calls to warning and error are output to System.err.
* This is important when running the AK as a NT Service, which sends each call to System.out.print
* as an event viewable using the Event Viewer administration utility.
*/

public class SystemReporter implements ReporterInterface
{
	protected static final String DEBUG_BEFORE   = "(";
	protected static final String DEBUG_AFTER    = ") ";
	protected static final String ERROR_BEFORE   = "!";
	protected static final String ERROR_AFTER    = "! ";
	protected static final String PRINTLN_BEFORE = "[";
	protected static final String PRINTLN_AFTER  = "] ";
	protected static final String WARNING_BEFORE = "?";
	protected static final String WARNING_AFTER  = "? ";

	protected final byte[] synchObject = new byte[0];


	/**
	* Use this method to report an event for debugging.
	*/

	public void debug(Object objectRef, String message)
	{
		synchronized (synchObject)
		{
			System.out.println(DEBUG_BEFORE + objectRef.toString() + DEBUG_AFTER + message);
		}
	}


	/**
	* Use this method to report a warning message.
	*/

	public void warning(Object objectRef, String message)
	{
		synchronized (synchObject)
		{
			System.out.println(WARNING_BEFORE + objectRef.toString() + WARNING_AFTER + message);
		}
	}


	/**
	* Use this method to report a warning message with an Exception.
	*/

	public void warning(Object objectRef, String message, Exception e)
	{
		synchronized (synchObject)
		{
			StringBuffer b = new StringBuffer();
			StringBufferOutputStream bout = new StringBufferOutputStream(b);
			PrintWriter out = new PrintWriter(bout);

			b.append(WARNING_BEFORE);
			b.append(objectRef.toString());
			b.append(WARNING_AFTER);
			b.append(message);
			b.append('\n');

			e.printStackTrace(out);
			out.flush();

			System.err.println(b.toString());
		}
	}


	/**
	* Use this method to report a critical error message.
	*/

	public void error(Object objectRef, String message)
	{
		synchronized (synchObject)
		{
			System.out.println(ERROR_BEFORE + objectRef.toString() + ERROR_AFTER + message);
		}
	}


	/**
	* Use this method to report a critical error with an Exception.
	*/

	public void error(Object objectRef, String message, Exception e)
	{
		synchronized (synchObject)
		{
			StringBuffer b = new StringBuffer();
			StringBufferOutputStream bout = new StringBufferOutputStream(b);
			PrintWriter out = new PrintWriter(bout);

			b.append(ERROR_BEFORE);
			b.append(objectRef.toString());
			b.append(ERROR_AFTER);
			b.append(message);
			b.append('\n');

			e.printStackTrace(out);
			out.flush();

			System.err.println(b.toString());
		}
	}


	/**
	* Use this method to report an event message.
	*/

	public void println(Object objectRef, String message)
	{
		synchronized (synchObject)
		{
			System.out.println(PRINTLN_BEFORE + objectRef.toString() + PRINTLN_AFTER + message);
		}
	}
}

