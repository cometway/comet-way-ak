
package com.cometway.io;

import com.cometway.ak.Agent;
import java.io.IOException;
import java.io.RandomAccessFile;


public class LogMonitorAgent extends Agent
{

	protected String log_file;
	protected Object sync;
	protected FileReaderThread fileReaderThread;
	protected FileWatcherThread fileWatcherThread;
	protected RandomAccessFile file;
	protected long filePtr;


	public void initProps()
	{
		setDefault("log_file", "/var/log/system.log");
	}


	public void start()
	{
		log_file = getTrimmedString("log_file");

		println("Monitoring log file: " + log_file);

		sync = new Object();

		fileReaderThread = new FileReaderThread();
		fileReaderThread.start();

		fileWatcherThread = new FileWatcherThread();
		fileWatcherThread.start();
	}


	public class FileReaderThread extends Thread
	{
		public void run()
		{
			debug("Starting FileReaderThread.");

			try
			{
				openLogFile();
				
				while (true)
				{
					try
					{
						String line = readLine();

						processLine(line);
					}
					catch (Exception e)
					{
						error("Could not read line.", e);

						openLogFile();
					}					
				}
			}
			catch (Exception e)
			{
				error("Could not continue.", e);
			}
		}
	}


	class FileWatcherThread extends Thread
	{
		public void run()
		{
			debug("Starting FileWatcherThread.");

			try
			{
				int count = 0;

				sleep(1000);

				filePtr = file.getFilePointer();

				while (true)
				{
					sleep(500);

					if (count > 40)	// what's this 40 all about? wait 20 seconds before scanning???
					{
						synchronized(sync)
						{
							try
							{
								openLogFile();

								if (file.length() >= filePtr)
								{
									debug("file.seek(" + filePtr + ")");

									file.seek(filePtr);
								}
								else
								{
									debug("file.seek(0)");

									file.seek(0);
								}
							}
							catch (Exception e)
							{
								warning("Caught exception.", e);
							}

							debug("Resetting count to 0.");

							count = 0;

							if (file.length() != filePtr)
							{
								debug("File length has changed.");

								sync.notify();
							}
							else
							{
								sleep(200);
							}
						}
					}
					else
					{
						if (file.read() != -1)
						{
							debug("Reached EOF.");

							synchronized(sync)
							{
								file.seek(file.getFilePointer() - 1);

								debug("**** filePtr = " + filePtr + " file.pointer = " + file.getFilePointer() + " file.length = " + file.length());
							}

							count = 0;
						}
						else
						{
							count++;
						}
					}
				}
			}
			catch (Exception e)
			{
				error("Could not continue.", e);
			}
		}
	}


	protected void openLogFile() throws IOException
	{
		if (file != null)
		{
			try
			{
				file.close();
			}
			catch (Exception e)
			{
				warning("Could not close file: " + log_file, e);
			}
		}

		file = new RandomAccessFile(log_file, "r");
		file.seek(file.length());

		debug("NEW FILE: filePtr = " + filePtr + ", new filePtr = " + file.length());
	}


	protected String readLine()
	{
		String line = null;

		try
		{
			synchronized(sync)
			{
				line = file.readLine();
			}

			while (line == null)
			{
				synchronized(sync)
				{
					sync.wait(500);

//					debug("file.readLine()");

					line = file.readLine();

					if (line != null)
					{
//						debug("file.getFilePointer()");

						filePtr = file.getFilePointer();
					}
				}
			}
		}
		catch (Exception e)
		{
			error("Could not continue", e);

			throw new RuntimeException("Could not continue", e);
		}

//		debug("line = " + line);

		return (line);
	}


	protected void processLine(String line)
	{
		println(line);
	}
}


