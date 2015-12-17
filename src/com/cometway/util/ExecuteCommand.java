
package com.cometway.util;

import java.util.*;
import java.io.*;


/**
 * This method extends com.cometway.util.KMethod and implements the Runnable 
 * interface, which allows this KMethod to be executed by a PooledThread from
 * a ThreadPool or another Thread. This class provides a simple way of executing
 * System commands via the Runtime.exec() and Process classes/methods.
 */

public class ExecuteCommand extends KMethod implements Runnable
{
	String				execString;


	/**
	 * This is the Input sent to the Process OuputStream after execution.
	 */

	public String			commandInput;
	Process				process;
	InputStreamReaderKMethod	outReader;
	InputStreamReaderKMethod	errReader;
	boolean				isRunning;
	Object				readerSync;


	/**
	 * This is set to the Thread that is executing this KMethod upon execution.
	 */

	public Thread			execThread;


	/**
	 * Set this to the ThreadPool which is used to read the process' output.
	 */

	public ThreadPool		processOutThreads;


	/**
	 * Set this to the ThreadPool which is used to read the process' error output.
	 */

	public ThreadPool		processErrThreads;


	/**
	 * Set this to the StringBuffer used to store the process' output. If this is not set, no output is read.
	 */

	public StringBuffer		processOut;


	/**
	 * Set this to the StringBuffer used to store the process' error output. If this is not set, no error output is read.
	 */

	public StringBuffer		processErr;


	/**
	 * Set this to False if the process will not be waited for. This means that run() will not wait for the Process to finish.
	 */

	public boolean			waitForProcess;


	/**
	 * Set this to True if the run() method is to block until the readers read until an End Of Transmission character. If this option is set with the 'finishedWaitTime' option, the method will only block for 'finishedWaitTime' milliseconds.
	 */

	public boolean			waitForProcessReaders;


	/**
	 * Set this to the number of milliseconds to wait after the process is finished. This guarantees that the output and error readers will read all the available input on some JVMs. If the 'waitForProcessReaders' flag is set to true, this field denotes how long to wait for the process' output.
	 */

	public int			finishedWaitTime;


	/**
	 * Set this to true if Line Feeds are to be change to Carriage Returns in the process' output.
	 */

	public boolean			changeOutLFtoCR;


	/**
	 * Set this to true if Carriage Returns are to be change to Line Feeds in the process' output.
	 */

	public boolean			changeOutCRtoLF;


	/**
	 * Set this to true if Line Feeds are to be change to Carriage Returns in the process' error output.
	 */

	public boolean			changeErrLFtoCR;


	/**
	 * Set this to true if Carriage Returns are to be change to Line Feeds in the process' error output.
	 */

	public boolean			changeErrCRtoLF;

	/**
	 * Return value of the command
	 */
	public int returnValue = -1;

	/**
	 * Set this to the working directory when the command is executed
	 */
	public File workingDirectory;


	/**
	 * Set this to the environment variables
	 */
	public String[]  environment;

	public ExecuteCommand(String executeString)
	{
		this(executeString, null);
	}


	public ExecuteCommand(String executeString, String commandInput)
	{
		execString = executeString;
		this.commandInput = commandInput;
		waitForProcess = true;
		readerSync = new Object();
	}


	public void execute()
	{
		run();
	}


	public void run()
	{
		isRunning = true;

		Thread  outReaderThread = null;
		Thread  errReaderThread = null;

		process = null;
		execThread = Thread.currentThread();

		try
		{			// Execute the command
			Runtime runtime = Runtime.getRuntime();

			if(workingDirectory==null) {
				workingDirectory = new File(".");
			}
			process = runtime.exec(execString,environment,workingDirectory);		// Read the process output

			if (processOut != null)
			{
				outReader = new InputStreamReaderKMethod(process.getInputStream(), processOut);
				outReader.changeLFtoCR = changeOutLFtoCR;
				outReader.changeCRtoLF = changeOutCRtoLF;

				if (waitForProcessReaders)
				{		// System.out.println("Setting process out notify");
					outReader.finishedReadNotify = readerSync;
				}

				if (processOutThreads != null)
				{
					processOutThreads.getThread(outReader);

					outReader.isRunning = true;
				}
				else
				{
					outReaderThread = new Thread(outReader);

					outReaderThread.start();
				}
			}		// Read the process error output

			if (processErr != null)
			{
				errReader = new InputStreamReaderKMethod(process.getErrorStream(), processErr);
				errReader.changeLFtoCR = changeErrLFtoCR;
				errReader.changeCRtoLF = changeErrCRtoLF;

				if (waitForProcessReaders)
				{		// System.out.println("Setting process err notify");
					errReader.finishedReadNotify = readerSync;
				}

				if (processErrThreads != null)
				{
					processErrThreads.getThread(errReader);

					errReader.isRunning = true;
				}
				else
				{
					errReaderThread = new Thread(errReader);

					errReaderThread.start();
				}
			}		// Write anything that the process needs

			if (commandInput != null)
			{
				BufferedWriter  writer = null;

				try
				{
					Thread.sleep(1000);

					writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

					if (writer != null)
					{
						writer.write(commandInput);
						writer.flush();
					}		// System.out.println("Finished writing to process.");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				if (writer != null)
				{
					try
					{
						writer.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}		// stopProcess();


			// waiting for the process

			if (finishedWaitTime > 0)
			{
				if (waitForProcessReaders)
				{
					boolean exceededTime = false;		// System.out.println("waiting for process readers processOut="+(processOut!=null)+", outReader.isRunning="+outReader.isRunning);

					if (((processOut != null) && outReader.isRunning) || ((processErr != null) && errReader.isRunning))
					{		// System.out.println("sync for process readers");
						synchronized (readerSync)
						{		// System.out.println("waiting... wait time="+finishedWaitTime);
							try
							{
								readerSync.wait(finishedWaitTime);

								exceededTime = true;		// System.out.println("Exceeded wait time.");
							}
							catch (InterruptedException ie)
							{
								ie.printStackTrace();
							}
						}
					}

					if (waitForProcess &&!exceededTime)
					{
						returnValue = process.waitFor();
					}
				}
				else
				{
					if (waitForProcess)
					{
						returnValue = process.waitFor();
					}

					Thread.sleep(finishedWaitTime);
				}
			}
			else if (waitForProcessReaders)
			{
				boolean exceededTime = false;

				if (((processOut != null) && outReader.isRunning) || ((processErr != null) && errReader.isRunning))
				{
					synchronized (readerSync)
					{		// System.out.println("waiting...");
						try
						{
							readerSync.wait();

							exceededTime = true;
						}
						catch (InterruptedException ie)
						{
							;
						}
					}
				}

				if (waitForProcess &&!exceededTime)
				{
					returnValue = process.waitFor();
				}
			}
			else
			{
				if (waitForProcess)
				{
					returnValue = process.waitFor();
				}
			}		// System.out.println("Finished...");

			try
			{
				if (process != null)
				{
					process.destroy();
				}
			}
			catch (Exception e)
			{
				;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		process = null;
		isRunning = false;
		execThread = null;
	}

	public boolean isRunning()
	{
		return(isRunning);
	}


	/**
	 * This method should be called before the PooledThread is released from 
	 * the ThreadPool.
	 */


	public void stopProcess()
	{		// System.out.println("STOPPING");
		if (isRunning)
		{
			try
			{
				if (processOut != null)
				{
					if (processOutThreads != null)
					{
						if (outReader.isRunning)
						{		// System.out.println("releasing Thread "+outReader.currentThread);
							processOutThreads.releaseThread((PooledThread) outReader.currentThread);
						}
					}

					outReader.stopReadThread();
				}

				if (processErr != null)
				{
					if (processErrThreads != null)
					{
						if (errReader.isRunning)
						{		// System.out.println("releasing Thread");
							processErrThreads.releaseThread((PooledThread) errReader.currentThread);
						}
					}

					errReader.stopReadThread();
				}

				if (process != null)
				{
					try
					{
						process.destroy();
					}
					catch (Exception e)
					{
						;
					}
				}

				execThread.interrupt();
//				execThread.stop();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}


	public static void main(String[] args)
	{
		ThreadPool      threads = null;
		ExecuteCommand  command = new ExecuteCommand(args[0]);

		for (int x = 1; x < args.length; x++)
		{
			if (args[x].equals("-input"))
			{
				command.commandInput = args[++x];
			}
			else if (args[x].equals("-noWait"))
			{
				command.waitForProcess = false;
			}
			else if (args[x].equals("-finishWait"))
			{
				command.finishedWaitTime = Integer.parseInt(args[++x]);
			}
			else if (args[x].equals("-threadPool"))
			{
				threads = new ThreadPool(10);
			}
			else if (args[x].equals("-out"))
			{
				System.out.println("Reading output");

				command.processOut = new StringBuffer();
			}
			else if (args[x].equals("-err"))
			{
				System.out.println("Reading error");

				command.processErr = new StringBuffer();
			}
			else if (args[x].equals("-readWait"))
			{
				command.waitForProcessReaders = true;
			}
		}

		if (threads != null)
		{
			command.processOutThreads = threads;
			command.processErrThreads = threads;
		}

		command.execute();

		if (command.processOut != null)
		{
			System.out.println(command.processOut.toString() + "\n");
		}

		if (command.processErr != null)
		{
			System.out.println(command.processErr.toString() + "\n");
		}

		if (threads != null)
		{
			System.out.println("Total Threads in pool: " + threads.totalThreads());
		}
	}


}

