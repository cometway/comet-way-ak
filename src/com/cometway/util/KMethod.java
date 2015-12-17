
package com.cometway.util;

import java.util.*;
import java.lang.reflect.*;


/**
 * KMethod is a wrapper for java.lang.reflect.Method. This object allows
 * Threads to easily pass code to be executed (a Method or a Runnable) around
 * with each other. PooledThreads execute KMethods in order to maximize their
 * efficiency by quickly determining what Threads will execute the code at
 * Runtime. KMethods can also wrap classes that implement Runnable.
 * Special instances of execution which may require state information or other
 * specific runtime information can extend this class so that PooledThreads
 * and ThreadPools can be utilized.
 *
 * @see ThreadPool
 * @see PooledThread
 */

public class KMethod
{
	protected String	debugName = "KMethod";
	protected boolean       print_errors = true;
	protected boolean       verbose = false;
	protected Object	obj;
	protected Method	method;
	protected Object	args[];
	protected Runnable      run;


	/**
	 * Create a KMethod around a Runnable. Runnable.run() will be the Method executed.
	 * @param run This is the Runnable which will be executed.
	 */

	public KMethod(Runnable run)
	{
		this.run = run;
	}


	/**
	 * Create a KMethod around a java.lang.reflect.Method. When this KMethod is executed
	 * this Method will be invoked.
	 * @param m This is the java.lang.reflect.Method to invoke.
	 * @param obj This is passed to the Method when invoked.
	 * @param args These are the arguments to pass to the Method when invoked.
	 */


	public KMethod(Method m, Object obj, Object[] args)
	{
		this.method = m;
		this.obj = obj;
		this.args = args;
	}


	/**
	 * Creates an empty KMethod.
	 */


	public KMethod() {}


	/**
	 * Calling this will execute this KMethod, which executes what the KMethod wraps.
	 */


	public void execute()
	{
		try
		{
			if (run != null)
			{
				run.run();
			}
			else
			{
				method.invoke(obj, args);
			}
		}
		catch (Exception exn)
		{
			error("execute", exn);
		}
	}


	protected void error(String s, Exception e)
	{
		error(s + ", " + e);

		if (print_errors)
		{
			e.printStackTrace();
		}
	}


	protected void error(String s)
	{
		if (print_errors)
		{
			System.out.println("[" + debugName + "] Error: " + s);
		}
	}


	protected void print(String s)
	{
		if (verbose)
		{
			System.out.println("[" + debugName + "]  " + s);
		}
	}


}

