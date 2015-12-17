
package com.cometway.util;


/**
* This is an interface for reporting agent progress and errors.
*/

public interface ReporterInterface
{
	/**
	* Use this method to report an event for debugging.
	*/

	public void debug(Object objectRef, String message);

	/**
	* Use this method to report a critical error message.
	*/

	public void error(Object objectRef, String message);

	/**
	* Use this method to report a critical error with an Exception.
	*/

	public void error(Object objectRef, String message, Exception e);

	/**
	* Use this method to report an event message.
	*/

	public void println(Object objectRef, String message);

	/**
	* Use this method to report a warning message.
	*/

	public void warning(Object objectRef, String message);

	/**
	* Use this method to report a warning message with an Exception.
	*/

	public void warning(Object objectRef, String message, Exception e);
}

