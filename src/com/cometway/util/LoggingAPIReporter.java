
package com.cometway.util;


import com.cometway.io.StringBufferOutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
* Each ReporterInterface method call is output using on of the appropriate
* Logging API methods defined by java.util.logging.Logger.
*/

public class LoggingAPIReporter implements ReporterInterface
{
	/**
	* Use this method to report an event for debugging.
	*/

	public void debug(Object objectRef, String message)
	{
		Logger logger = Logger.getLogger(objectRef.toString());

		logger.logp(Level.FINE, objectRef.toString(), objectRef.toString(), "debug", message);
	}


	/**
	* Use this method to report a warning message.
	*/

	public void warning(Object objectRef, String message)
	{
		Logger logger = Logger.getLogger(objectRef.toString());

		logger.logp(Level.WARNING, objectRef.toString(), "warning", message);
	}


	/**
	* Use this method to report a warning message with an Exception.
	*/

	public void warning(Object objectRef, String message, Exception e)
	{
		Logger logger = Logger.getLogger(objectRef.toString());

		logger.logp(Level.WARNING, objectRef.toString(), "warning", message, e);
	}


	/**
	* Use this method to report a critical error message.
	*/

	public void error(Object objectRef, String message)
	{
		Logger logger = Logger.getLogger(objectRef.toString());

		logger.logp(Level.INFO, objectRef.toString(), "error", message);
	}


	/**
	* Use this method to report a critical error with an Exception.
	*/

	public void error(Object objectRef, String message, Exception e)
	{
		Logger logger = Logger.getLogger(objectRef.toString());

		logger.logp(Level.SEVERE, objectRef.toString(), "error",  message, e);
	}


	/**
	* Use this method to report an event message.
	*/

	public void println(Object objectRef, String message)
	{
		Logger logger = Logger.getLogger(objectRef.toString());
		
		logger.logp(Level.INFO, objectRef.toString(), "println",  message);
	}
}

