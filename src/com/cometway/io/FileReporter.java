
package com.cometway.io;

import com.cometway.ak.AK;
import com.cometway.util.ReporterInterface;
import java.io.PrintWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
* This is a Reporter Agent that extends FileLoggerAgent in order to log
* Agent Kernel agent output to sequentially named text files. Upon loading, this agent
* replaces the default Reporter.
*/

public class FileReporter extends FileLoggerAgent implements ReporterInterface
{
	protected final static SimpleDateFormat SDF = new SimpleDateFormat("yyMMdd-HHmmss");

	protected static final String DEBUG_BEFORE   = "(";
	protected static final String DEBUG_AFTER    = ") ";
	protected static final String ERROR_BEFORE   = "!";
	protected static final String ERROR_AFTER    = "! ";
	protected static final String PRINTLN_BEFORE = "[";
	protected static final String PRINTLN_AFTER  = "] ";
	protected static final String WARNING_BEFORE = "?";
	protected static final String WARNING_AFTER  = "? ";


	protected Object synchObject = new byte[0];
	protected ReporterInterface defaultReporter;
	protected boolean log_println;
	protected boolean log_debug;
	protected boolean log_warning;
	protected boolean log_error;
	protected boolean echo_println;
	protected boolean echo_debug;
	protected boolean echo_warning;
	protected boolean echo_error;


	public void initProps()
	{
//		setDefault("service_name","logger_agent");
		setDefault("log_file_dir", "./");
		setDefault("log_file_date_format", "yyyyMMdd-HHmmss");
		setDefault("log_file_suffix", ".log");
		setDefault("logs_per_file","10000");

		setDefault("log_println", "true");
		setDefault("log_debug", "true");
		setDefault("log_warning", "true");
		setDefault("log_error", "true");

		setDefault("echo_println", "true");
		setDefault("echo_debug", "true");
		setDefault("echo_warning", "true");
		setDefault("echo_error", "true");
	}


	public void start()
	{
		super.start();

		defaultReporter = AK.getDefaultReporter();
		AK.setDefaultReporter(this);

		printlnReporter = this;
		debugReporter = this;
		warningReporter = this;
		errorReporter = this;

		log_println = getBoolean("log_println");
		log_debug = getBoolean("log_debug");
		log_warning = getBoolean("log_warning");
		log_error = getBoolean("log_error");

		echo_println = getBoolean("echo_println");
		echo_debug = getBoolean("echo_debug");
		echo_warning = getBoolean("echo_warning");
		echo_error = getBoolean("echo_error");

		debug("File Reporter started at " + getDateTimeStr());
	}


	/**
	* Use this method to report an event for debugging.
	*/

	public void debug(Object objectRef, String message)
	{
		if (echo_debug) defaultReporter.debug(objectRef, message);

		if (log_debug)
		{
			synchronized (synchObject)
			{
				log(DEBUG_BEFORE + SDF.format(new Date()) + ' ' + objectRef.toString() + DEBUG_AFTER + message);
			}
		}
	}


	/**
	* Use this method to report a warning message.
	*/

	public void warning(Object objectRef, String message)
	{
		if (echo_warning) defaultReporter.warning(objectRef, message);

		if (log_warning)
		{
			synchronized (synchObject)
			{
				log(WARNING_BEFORE + SDF.format(new Date()) + ' ' + objectRef.toString() + WARNING_AFTER + message);
			}
		}
	}


	/**
	* Use this method to report a warning message with an Exception.
	*/

	public void warning(Object objectRef, String message, Exception e)
	{
		if (echo_warning) defaultReporter.warning(objectRef, message, e);

		if (log_warning)
		{
			synchronized (synchObject)
			{
				StringBuffer b = new StringBuffer();
				StringBufferOutputStream bout = new StringBufferOutputStream(b);
				PrintWriter out = new PrintWriter(bout);
	
				out.print(WARNING_BEFORE);
				out.print(SDF.format(new Date()));
				out.println(' ');
				out.print(objectRef.toString());
				out.print(WARNING_AFTER);
				out.println(message);
				e.printStackTrace(out);
				out.flush();
	
				log(b.toString());
			}
		}
	}


	/**
	* Use this method to report a critical error with an Exception.
	*/

	public void error(Object objectRef, String message)
	{
		if (echo_error) defaultReporter.error(objectRef, message);

		if (log_error)
		{
			synchronized (synchObject)
			{
				log(ERROR_BEFORE + SDF.format(new Date()) + ' ' + objectRef.toString() + ERROR_AFTER + message);
			}
		}
	}


	/**
	* Use this method to report a critical error message.
	*/

	public void error(Object objectRef, String message, Exception e)
	{
		if (echo_error) defaultReporter.error(objectRef, message, e);

		if (log_error)
		{
			synchronized (synchObject)
			{
				StringBuffer b = new StringBuffer();
				StringBufferOutputStream bout = new StringBufferOutputStream(b);
				PrintWriter out = new PrintWriter(bout);
		
				out.print(ERROR_BEFORE);
				out.print(SDF.format(new Date()));
				out.println(' ');
				out.print(objectRef.toString());
				out.print(ERROR_AFTER);
				out.println(message);
				e.printStackTrace(out);
				out.flush();
	
				log(b.toString());
			}
		}
	}


	/**
	* Use this method to report an event message.
	*/

	public void println(Object objectRef, String message)
	{
		if (echo_error) defaultReporter.println(objectRef, message);

		if (log_println)
		{
			synchronized (synchObject)
			{
				log(PRINTLN_BEFORE + SDF.format(new Date()) + ' ' + objectRef.toString() + PRINTLN_AFTER + message);
			}
		}
	}
}


