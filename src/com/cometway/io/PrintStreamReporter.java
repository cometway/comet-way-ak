
package com.cometway.io;

import com.cometway.util.ReporterInterface;
import java.io.PrintStream;

/**
* This implementation of ReporterInterface streams output using separate PrintStreams for
* println and debug messages, and warning and debug messages.
* These streams are set to System.out and and System.err when using the default constructor.
* Calls to a Reporter instance methods are synchronized.
* The warning and error methods output stack trace information (if available).
*/

public class PrintStreamReporter implements ReporterInterface
{
	protected static final String DEBUG_BEFORE   = "(";
	protected static final String DEBUG_AFTER    = ") ";
	protected static final String ERROR_BEFORE   = "!";
	protected static final String ERROR_AFTER    = "! ";
	protected static final String PRINTLN_BEFORE = "[";
	protected static final String PRINTLN_AFTER  = "] ";
	protected static final String WARNING_BEFORE = "?";
	protected static final String WARNING_AFTER  = "? ";

	protected Object synchObject = new byte[0];
	protected PrintStream out = System.out;
	protected PrintStream err = System.err;


	/**
	* Use this method to report an event for debugging.
	*/

	public void debug(Object objectRef, String message)
	{
		synchronized (synchObject)
		{
			PrintStream o = out;
			o.print(DEBUG_BEFORE);
			o.print(objectRef.toString());
			o.print(DEBUG_AFTER);
			o.println(message);
		}
	}


	/**
	* Use this method to report a warning message.
	*/

	public void warning(Object objectRef, String message)
	{
		synchronized (synchObject)
		{
			PrintStream o = err;
			o.print(WARNING_BEFORE);
			o.print(objectRef.toString());
			o.print(WARNING_AFTER);
			o.println(message);
		}
	}


	/**
	* Use this method to report a warning message with an Exception.
	*/

	public void warning(Object objectRef, String message, Exception e)
	{
		synchronized (synchObject)
		{
			PrintStream o = err;
			o.print(WARNING_BEFORE);
			o.print(objectRef.toString());
			o.print(WARNING_AFTER);
			o.println(message);
			e.printStackTrace(o);
			o.flush();
		}
	}


	/**
	* Use this method to report a critical error with an Exception.
	*/

	public void error(Object objectRef, String message)
	{
		synchronized (synchObject)
		{
			PrintStream o = err;
			o.print(ERROR_BEFORE);
			o.print(objectRef.toString());
			o.print(ERROR_AFTER);
			o.println(message);
		}
	}


	/**
	* Use this method to report a critical error message.
	*/

	public void error(Object objectRef, String message, Exception e)
	{
		synchronized (synchObject)
		{
			PrintStream o = err;
			o.print(ERROR_BEFORE);
			o.print(objectRef.toString());
			o.print(ERROR_AFTER);
			o.println(message);
			e.printStackTrace(o);
			o.flush();
		}
	}


	/**
	* Use this method to report an event message.
	*/

	public void println(Object objectRef, String message)
	{
		synchronized (synchObject)
		{
			PrintStream o = out;
			o.print(PRINTLN_BEFORE);
			o.print(objectRef.toString());
			o.print(PRINTLN_AFTER);
			o.println(message);
		}
	}
}
