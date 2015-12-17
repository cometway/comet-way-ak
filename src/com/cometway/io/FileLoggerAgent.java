

package com.cometway.io;


import com.cometway.ak.ServiceAgent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
* This agent logs Strings to a time-stamped file.
*/

public class FileLoggerAgent extends ServiceAgent
{
	protected final static String EOL = System.getProperty("line.separator");

	
	protected int logCount;
	protected BufferedWriter out;
	protected Object logSync;


	/** 
	*Initializes this agent's properties by providing default
	* values for each of the following missing properties:
	* "log_type" sets the type of log file to be used (default: FILE),
	* "log_file_prefix specifies the prefix for the log_file
	* (default: log_), "logs_per_file" specifies the maximum
	* log entries per file (default: 10000).
	*/

	public void initProps()
	{
		setDefault("service_name","logger_agent");
		setDefault("log_file_dir", "./");
		setDefault("log_file_date_format", "yyyyMMdd-HHmmss");
		setDefault("log_file_suffix", ".log");
		setDefault("logs_per_file","10000");
	}


	/**
	* Creates new logSync object, creates the log file, and registers with the service manager.
	*/
	
	public void start()
	{
		logSync = new Object();

		createLogFile();

		register();
	}


	/**
	* Writes information given as a String input to log file.
	*/

	public void log(String s)
	{
		synchronized(logSync)
		{
			try
			{
				out.write(s);
				out.write(EOL);
				out.flush();
				logCount++;

				int logs_per_file = getInteger("logs_per_file");

				if (logCount > logs_per_file)
				{
					closeLogFile();
					createLogFile();
				}
			}
			catch(Exception e)
			{
				error("log", e);
			}
		}
	}


	/**
	* Initializes log File / checks to see if matching file already exists.
	*/

	protected void createLogFile()
	{
		String filename = null;

		try
		{
			Date now = new Date();
			String log_file_dir = getTrimmedString("log_file_dir");
			String log_file_suffix = getTrimmedString("log_file_suffix");
			SimpleDateFormat sdf = new SimpleDateFormat(getTrimmedString("log_file_date_format"));
			filename = log_file_dir + sdf.format(now) + log_file_suffix;

			File file = new File(log_file_dir, sdf.format(now) + log_file_suffix);

			closeLogFile();
			
			out = new BufferedWriter(new FileWriter(file));
			
			logCount = 0;
		}
		catch (Exception e)
		{
			error("Could not create log file: " + filename, e);
		}
	}


	/**
	* Properly closes the currently open log file.
	*/

	protected void closeLogFile()
	{
		if (out == null) return;

		try
		{
			out.close();
		}
		catch(Exception ex)
		{
			error("Could not close BufferedWriter", ex);
		}
		finally
		{
			out = null;
		}
	}
}

