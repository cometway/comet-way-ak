
package com.cometway.util;

import java.util.*;


/**
 * This is a Thread used by the ThreadPool class. This thread does not stop
 * until it is no longer needed and is dispoed of. After this thread executes
 * a KMethod (which can wrap a Runnable), Object.wait() is called. The PooledThread
 * will wait until it is notified by Object.notify(), in which case it checks its
 * method field for a KMethod to execute. If an object was passed in through setMethod(),
 * then that object is notified when the execution is complete.
 */

public class PooledThread extends Thread
{
	public boolean  stopRunning = false;
	public int      threadTimeOut = -1;		// thread pool
	ThreadPool      pool;				// method to run
	KMethod		method = null;			// object to notify
	Object		obj = null;			// debug
	public int      id = 0;


	/**
	 * Creates a new PooledThread. Thread.start() must be called before
	 * it can be notified.
	 */

	public PooledThread(ThreadPool pool)
	{
		super();

		this.pool = pool;

		setPriority(MIN_PRIORITY);		// threadTimeOut = 3600000; // 1 hour
	}


	/**
	 * This sets the KMethod and Object fields of this PooledThread. Afterwards, 
	 * this PooledThread can be notified and it will execute the KMethod and notify the
	 * Object after its execution.
	 */


	public void setMethod(KMethod method, Object obj)
	{
		if (this.method == null && this.obj == null)
		{
			this.method = method;
			this.obj = obj;
		}
		else
		{
			System.out.println("{PThread} trying to setMethod, but i already have a obj");
		}
	}


	/**
	 * This sets the KMethod field of this PooledThread. Afterwards, this PooledThread can
	 * be notified and it will execute the KMethod.
	 */


	public void setMethod(KMethod method)
	{
		if (this.method == null)
		{
			this.method = method;
		}
		else
		{
			System.out.println("{PThread} trying to setMethod, but i already have a obj");
		}
	}


	/**
	 * Executes the KMethod.execute(), notifies any object which was waiting, resets
	 * state and waits.
	 */


	public void run()
	{
		try
		{
			while (!stopRunning)
			{
				try
				{
					setName(pool.threadPoolName + " (" + id + ") " + method.toString());
				}
				catch (Exception e)
				{
					;
				}			// do the work

				try
				{
					method.execute();
				}
				catch (Exception easd)
				{
					;
				}		// inform anyone waiting for you

				if (obj != null)
				{
					synchronized (obj)
					{
						obj.notifyAll();
					}		// clear the refs
				}

				method = null;
				obj = null;

				try
				{
					setName(pool.threadPoolName + " (" + id + ") IDLE");
				}
				catch (Exception e)
				{
					;
				}

				if (!stopRunning)
				{
					try
					{		// wait to be woken again
						synchronized (this)
						{		// get back in the pool
							pool.returnThread(this);

							if (threadTimeOut > 0)
							{
								wait(threadTimeOut);
							}
							else
							{
								wait();
							}
						}
					}
					catch (Exception e)
					{
						stopRunning = true;

						pool.releaseThread(this);
						System.err.println("{PThread} ERROR: " + e + " : stopping myself.");
					}
				}
			}
		}
		catch (Exception exn)
		{
			System.err.println("{PThread} ERROR: " + exn + " : Exception in the run() method.");
		}

		pool.releaseThread(this);

		method = null;
		obj = null;
		pool = null;
	}


}

