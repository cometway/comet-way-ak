
package com.cometway.io;

import com.cometway.util.ReporterInterface;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
* This is a Reporter  that uses VT100 codes to hilight different types
* of messages. Debug messages are bold; warning and error messages are
* inverted. This agent replaces the default Reporter.
*/

public class VT100ConsoleReporter implements ReporterInterface
{
	protected static final SimpleDateFormat SDF = new SimpleDateFormat("yyMMdd-HHmmss");

	protected static final String NORMAL         = "\u001B[0m";
	protected static final String BOLD           = "\u001B[1m";
	protected static final String INVERSE        = "\u001B[7m";
	protected static final String DEBUG_BEFORE   = "(";
	protected static final String DEBUG_AFTER    = ") ";
	protected static final String ERROR_BEFORE   = "!";
	protected static final String ERROR_AFTER    = "! ";
	protected static final String PRINTLN_BEFORE = "[";
	protected static final String PRINTLN_AFTER  = "] ";
	protected static final String WARNING_BEFORE = "?";
	protected static final String WARNING_AFTER  = "? ";

	protected final Object synchObject = new byte[0];


	/**
	* Use this method to report an event for debugging.
	*/

	public void debug(Object objectRef, String message)
	{
		synchronized (synchObject)
		{
			PrintStream o = System.out;
			o.print(BOLD);
			o.print(DEBUG_BEFORE);
			o.print(SDF.format(new Date()));
			o.print(' ');
			o.print(objectRef.toString());
			o.print(DEBUG_AFTER);
			o.print(message);
			o.println(NORMAL);
		}
	}


	/**
	* Use this method to report a warning message.
	*/

	public void warning(Object objectRef, String message)
	{
		synchronized (synchObject)
		{
			PrintStream o = System.err;
			o.print(INVERSE);
			o.print(WARNING_BEFORE);
			o.print(SDF.format(new Date()));
			o.print(' ');
			o.print(objectRef.toString());
			o.print(WARNING_AFTER);
			o.print(message);
			o.println(NORMAL);
		}
	}


	/**
	* Use this method to report a warning message with an Exception.
	*/

	public void warning(Object objectRef, String message, Exception e)
	{
		synchronized (synchObject)
		{
			System.err.print(INVERSE);
			System.err.print(WARNING_BEFORE);
			System.err.print(SDF.format(new Date()));
			System.err.print(' ');
			System.err.print(objectRef.toString());
			System.err.print(WARNING_AFTER);
			System.err.println(message);

			e.printStackTrace(System.err);

			System.err.print(message);
			System.err.println(NORMAL);
		}
	}


	/**
	* Use this method to report a critical error message.
	*/

	public void error(Object objectRef, String message)
	{
		synchronized (synchObject)
		{
			System.err.print(INVERSE);
			System.err.print(ERROR_BEFORE);
			System.err.print(SDF.format(new Date()));
			System.err.print(' ');
			System.err.print(objectRef.toString());
			System.err.print(ERROR_AFTER);
			System.err.print(message);
			System.err.println(NORMAL);
		}
	}


	/**
	* Use this method to report a critical error with an Exception.
	*/

	public void error(Object objectRef, String message, Exception e)
	{
		synchronized (synchObject)
		{
			System.err.print(INVERSE);
			System.err.print(ERROR_BEFORE);
			System.err.print(SDF.format(new Date()));
			System.err.print(' ');
			System.err.print(objectRef.toString());
			System.err.print(ERROR_AFTER);
			System.err.println(message);

			e.printStackTrace(System.err);

			System.err.println(NORMAL);
		}
	}


	/**
	* Use this method to report an event message.
	*/

	public void println(Object objectRef, String message)
	{
		synchronized (synchObject)
		{
			System.out.print(PRINTLN_BEFORE);
			System.out.print(SDF.format(new Date()));
			System.out.print(' ');
			System.out.print(objectRef.toString());
			System.out.print(PRINTLN_AFTER);
			System.out.println(message);
		}
	}
}

