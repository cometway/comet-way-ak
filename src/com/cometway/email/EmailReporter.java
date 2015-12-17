
package com.cometway.email;


import com.cometway.ak.AK;
import com.cometway.ak.ScheduledAgent;
import com.cometway.io.StringBufferOutputStream;
import com.cometway.util.ReporterInterface;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;


/**
* This is a Reporter Agent that extends FileLoggerAgent in order to log
* AK agent output to sequentially named text files. Upon loading, this agent
* replaces the default Reporter.
*/

public class EmailReporter extends ScheduledAgent implements ReporterInterface
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


	protected StringBuffer history;
	protected Vector recipients;
	protected String reply_to;
	protected Object synchObject = new byte[0];
	protected ReporterInterface defaultReporter;
	protected boolean send_println;
	protected boolean send_debug;
	protected boolean send_warning;
	protected boolean send_error;


	public void initProps()
	{
		setDefault("schedule", "between 0:0:0-23:59:59 every 6h");
		setDefault("send_email_service_name", "send_email");
		setDefault("report_title", "Agent Report");
		setDefault("send_println", "true");
		setDefault("send_debug", "true");
		setDefault("send_warning", "true");
		setDefault("send_error", "true");
		setDefault("reply_to", "replyto@hostname");
		setDefault("recipients", "recipients@hostname");
	}


	public void start()
	{
		// get the list of recipients

		StringTokenizer t = new StringTokenizer(getString("recipients"), ", \n");

		recipients = new Vector();

		while (t.hasMoreTokens())
		{
			recipients.addElement(t.nextToken());
		}

		debug("reply_to: " + getString("reply_to"));
		debug("recipients: " + recipients.toString());

		// patch into the agent kernel

		defaultReporter = AK.getDefaultReporter();
		AK.setDefaultReporter(this);

		printlnReporter = this;
		debugReporter = this;
		warningReporter = this;
		errorReporter = this;

		send_println = getBoolean("send_println");
		send_debug = getBoolean("send_debug");
		send_warning = getBoolean("send_warning");
		send_error = getBoolean("send_error");

		schedule();

		debug("File Reporter started at " + getDateTimeStr());
	}


	public void wakeup()
	{
		Date now = new Date();

		if (history != null)
		{
			String report_title = getString("report_title");
			String reply_to = getString("reply_to");
			String title = report_title + " - " + getDateTimeStr();
			StringBuffer b = new StringBuffer();

			b.append(title);
			b.append("\n\n");
			b.append(history.toString());

			Enumeration e2 = recipients.elements();

			while (e2.hasMoreElements())
			{
				String  to = e2.nextElement().toString();

				println("Sending bug report to " + to);

				Message m = new Message();

				m.setHeaderInfo("from", reply_to);
				m.setHeaderInfo("date", now.toString());
				m.setHeaderInfo("subject", title);
				m.setMessage(b.toString());
				m.setHeaderInfo("to", to);

				SendEmailInterface emailer = (SendEmailInterface) getServiceImpl(getString("send_email_service_name"));
				emailer.sendEmailMessage(m);
			}

			history = null;
		}
	}



	/**
	* This method is used to write a line to the email report.
	*/

	private void log(String message)
	{
		if (history == null)
		{
			history = new StringBuffer();
		}

		history.append(message);
		history.append('\n');
	}


	/**
	* Use this method to report an event for debugging.
	*/

	public void debug(Object objectRef, String message)
	{
		defaultReporter.debug(objectRef, message);

		if (send_debug)
		{
			synchronized (synchObject)
			{
				log(DEBUG_BEFORE + SDF.format(new Date()) + objectRef.toString() + DEBUG_AFTER + message);
			}
		}
	}


	/**
	* Use this method to report a warning message.
	*/

	public void warning(Object objectRef, String message)
	{
		defaultReporter.warning(objectRef, message);

		if (send_warning)
		{
			synchronized (synchObject)
			{
				log(WARNING_BEFORE + SDF.format(new Date()) + objectRef.toString() + WARNING_AFTER + message);
			}
		}
	}


	/**
	* Use this method to report a warning message with an Exception.
	*/

	public void warning(Object objectRef, String message, Exception e)
	{
		defaultReporter.warning(objectRef, message, e);

		if (send_warning)
		{
			synchronized (synchObject)
			{
				StringBuffer b = new StringBuffer();
				StringBufferOutputStream bout = new StringBufferOutputStream(b);
				PrintWriter out = new PrintWriter(bout);
	
				out.print(WARNING_BEFORE);
				out.print(SDF.format(new Date()));
				out.print(' ');
				out.print(objectRef.toString());
				out.print(WARNING_AFTER);
				out.println(message);
				e.printStackTrace(out);
				out.flush();
	
				log(b.toString());

				try {	out.close();} catch(Exception ex) {;}
			}
		}
	}


	/**
	* Use this method to report a critical error with an Exception.
	*/

	public void error(Object objectRef, String message)
	{
		defaultReporter.error(objectRef, message);

		if (send_error)
		{
			synchronized (synchObject)
			{
				log(ERROR_BEFORE + SDF.format(new Date()) + objectRef.toString() + ERROR_AFTER + message);
			}
		}
	}


	/**
	* Use this method to report a critical error message.
	*/

	public void error(Object objectRef, String message, Exception e)
	{
		defaultReporter.error(objectRef, message, e);

		if (send_error)
		{
			synchronized (synchObject)
			{
				StringBuffer b = new StringBuffer();
				StringBufferOutputStream bout = new StringBufferOutputStream(b);
				PrintWriter out = new PrintWriter(bout);
		
				out.print(SDF.format(new Date()));
				out.print(' ');
				out.print(ERROR_BEFORE);
				out.print(objectRef.toString());
				out.print(ERROR_AFTER);
				out.println(message);
				e.printStackTrace(out);
				out.flush();
	
				log(b.toString());

				try {	out.close();} catch(Exception ex) {;}
			}
		}
	}


	/**
	* Use this method to report an event message.
	*/

	public void println(Object objectRef, String message)
	{
		defaultReporter.println(objectRef, message);

		if (send_println)
		{
			synchronized (synchObject)
			{
				log(PRINTLN_BEFORE + SDF.format(new Date()) + objectRef.toString() + PRINTLN_AFTER + message);
			}
		}
	}
}


