
package com.cometway.util;

import java.util.*;


/**
* This class starts and stops a pool of PooledThreads. 
*/

public class ThreadPool
{
	protected Queue		queue;
	protected Vector	allocatedThreads;
	public int		num_threads = 0;
	protected int		max_threads = 0;
	protected String	threadPoolName;
	protected int		nextID = 0;
	protected int		priority = Thread.MIN_PRIORITY;
	protected Object	queueChanged = new Object();
	protected boolean stopped;


	/**
	 * Starts a ThreadPool with the max thread limit to the specified number.
	 */

	public ThreadPool(int max_threads)
	{
		this.max_threads = max_threads;
		num_threads = 0;
		allocatedThreads = new Vector();
		queue = new Queue();

		try
		{
			threadPoolName = "ThreadPool(" + Thread.currentThread().getName() + ")";
		}
		catch (Exception e)
		{
			threadPoolName = "ThreadPool(?)";
		}
		stopped = false;
	}


	/**
	 * Starts a ThreadPool with the max thread limit to the specified number
	 * and a priority setting for the PooledThreads.
	 */


	public ThreadPool(int max_threads, int thread_priority)
	{
		this.max_threads = max_threads;
		num_threads = 0;
		priority = thread_priority;
		allocatedThreads = new Vector();
		queue = new Queue();

		try
		{
			threadPoolName = "ThreadPool(" + Thread.currentThread().getName() + ")";
		}
		catch (Exception e)
		{
			threadPoolName = "ThreadPool(?)";
		}
		stopped = false;
	}


	/**
	 * This method stops the ThreadPool allows it to be garbage collected.
	 * This method MUST be called in order for the pool and its threads to be freed.
	 */
	public void stop()
	{
		Vector tmp = new Vector();
		stopped = true;
		synchronized(allocatedThreads) {
			for(int x=0;x<allocatedThreads.size();x++) {
				tmp.addElement(allocatedThreads.elementAt(x));
			}
		}
		for(int x=0;x<tmp.size();x++) {
			PooledThread pt = (PooledThread)tmp.elementAt(x);
			pt.stopRunning = true;
			synchronized(pt) {
				pt.notifyAll();
			}
		}
	}



	/**
	 * This method sets the Name of this ThreadPool. This name will be the prefix name for
	 * all the thread created by this threadPool.
	 */


	public void setName(String name)
	{
		threadPoolName = name;
	}


	/**
	 * This method gets an unused PooledThread or creates one if
	 * the pool has not exceeded its limit and executes the
	 * KMethod passed in. A lock on the obj is first obtained before 
	 * anything is done. The obj will wait until the KMethod is
	 * finished executing and it gets notified, or the specified timeout
	 * has expired.
	 *
	 * @param method This is the KMethod to execute on a PooledThread
	 * @param obj This is the obj used to wait for the PooledThread to finish.
	 * @param timeout This is the number of milliseconds to wait for the PooledThread to finish.
	 * @return Returns true if no exceptions were caught during the PooledThread creation and/or execution.
	 */


	public boolean getThread(KMethod method, Object obj, int timeout)
	{
		if(!stopped) {
			try
			{
				synchronized (obj)
				{
					try
					{
						PooledThread    pt = (PooledThread) queue.nextElement();

						pt.setMethod(method, obj);

						synchronized (pt)
						{
							pt.notifyAll();
						}
					}
					catch (NoSuchElementException asdafg)
					{
						try
						{
							PooledThread    pt = makePooledThread();

							pt.setMethod(method, obj);
							pt.start();
						}
						catch (NullPointerException npe)
						{
							System.out.println("{ThreadPool} WARNING: Reached maximum number of PooledThreads: " + max_threads + ", Queue size: " + queue.size());

							return (false);
						}
					}

					obj.wait(timeout);

					return true;
				}
			}
			catch (InterruptedException ie)
			{
				;
			}
			catch (Exception exn)
			{
				error("Exception caught while initiating KMethod execution", exn);
			}
		}

		return false;
	}


	/**
	 * This method gets an unused PooledThread or creates one if
	 * the pool has not exceeded its limit and executes the
	 * KMethod passed in. A lock on the obj is first obtained before 
	 * anything is done. The obj will wait until the KMethod is
	 * finished executing and it gets notified.
	 *
	 * @param method This is the KMethod to execute on a PooledThread
	 * @param obj This is the obj used to wait for the PooledThread to finish.
	 * @return Returns true if no exceptions were caught during the PooledThread creation and/or execution.
	 */


	public boolean getThread(KMethod method, Object obj)
	{
		if(!stopped) {
			try
			{
				synchronized (obj)
				{
					try
					{
						PooledThread    pt = (PooledThread) queue.nextElement();

						pt.setMethod(method, obj);

						synchronized (pt)
						{
							pt.notifyAll();
						}
					}
					catch (NoSuchElementException asdafg)
					{
						try
						{
							PooledThread    pt = makePooledThread();

							pt.setMethod(method, obj);
							pt.start();
						}
						catch (NullPointerException npe)
						{
							System.out.println("{ThreadPool} WARNING: Reached maximum number of PooledThreads: " + max_threads + ", Queue size: " + queue.size());

							return (false);
						}
					}

					obj.wait();

					return true;
				}
			}
			catch (Exception exn)
			{
				error("Exception caught while initiating KMethod execution", exn);
			}
		}

		return false;
	}


	/**
	 * This method gets an unused PooledThread or creates one if
	 * the pool has not exceeded its limit and executes the
	 * KMethod passed in. This method does not block on an object,
	 * this method returns immediately.
	 *
	 * @param method This is the KMethod to execute on a PooledThread
	 * @return Returns true if no exceptions were caught during the PooledThread creation and/or execution.
	 */


	public boolean getThread(KMethod method)
	{
		if(!stopped) {
			try
			{
				try
				{
					PooledThread    pt = (PooledThread) queue.nextElement();

					pt.setMethod(method);

					synchronized (pt)
					{
						pt.notifyAll();
					}
				}
				catch (NoSuchElementException asdafg)
				{
					try
					{
						PooledThread    pt = makePooledThread();

						pt.setMethod(method);
						pt.start();
					}
					catch (NullPointerException npe)
					{
						System.out.println("{ThreadPool} WARNING: Reached maximum number of PooledThreads: " + max_threads + ", Queue size: " + queue.size());

						return (false);
					}
				}
			}
			catch (Exception exn)
			{
				error("Exception caught while initiating KMethod execution", exn);

				return false;
			}
		}

		return true;
	}


	public boolean getThreadOrWait(KMethod method, int timeout)
	{
		boolean rval = false;

		if(!stopped) {
			try
			{
				PooledThread    pt = null;

				try
				{
					pt = (PooledThread) queue.nextElement();

					pt.setMethod(method);

					synchronized (pt)
					{
						pt.notifyAll();
					}

					rval = true;
				}
				catch (NoSuchElementException e)
				{
					try
					{
						pt = makePooledThread();

						if (pt != null)
						{
							pt.setMethod(method);
							pt.start();

							rval = true;
						}
					}
					catch (NullPointerException npe)
					{
						long    startWait = System.currentTimeMillis();

						pt = null;

						while (pt == null)
						{
							try
							{
								pt = (PooledThread) queue.nextElement();
							}
							catch (NoSuchElementException e234)
							{
								try
								{
									synchronized (queueChanged)
									{
										System.out.println("getThreadOrWait() starting wait with timeout: " + timeout);
										queueChanged.wait(timeout);
									}
								}
								catch (Exception e231)
								{
									;
								}

								System.out.println("getThreadOrWait() done waiting");
							}

							if (pt == null)
							{
								if ((System.currentTimeMillis() - startWait) > timeout)
								{
									break;
								}
							}
						}

						if (pt != null)
						{
							pt.setMethod(method);

							synchronized (pt)
							{
								pt.notifyAll();
							}

							rval = true;
						}
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return (rval);
	}


	public boolean getThreadOrWait(KMethod method, Object obj, int timeout)
	{
		boolean rval = false;

		if(!stopped) {
			try
			{
				PooledThread    pt = null;

				try
				{
					pt = (PooledThread) queue.nextElement();

					pt.setMethod(method, obj);

					synchronized (pt)
					{
						pt.notifyAll();
					}

					rval = true;
				}
				catch (NoSuchElementException e)
				{
					try
					{
						pt = makePooledThread();

						if (pt != null)
						{
							pt.setMethod(method, obj);
							pt.start();

							rval = true;
						}
					}
					catch (NullPointerException npe)
					{
						long    startWait = System.currentTimeMillis();

						pt = null;

						while (pt == null)
						{
							try
							{
								pt = (PooledThread) queue.nextElement();
							}
							catch (NoSuchElementException e234)
							{
								try
								{
									synchronized (queueChanged)
									{
										System.out.println("getThreadOrWait() starting wait with timeout: " + timeout);
										queueChanged.wait(timeout);
									}
								}
								catch (Exception e231)
								{
									;
								}

								System.out.println("getThreadOrWait() done waiting");
							}

							if (pt == null)
							{
								if ((System.currentTimeMillis() - startWait) > timeout)
								{
									break;
								}
							}
						}

						if (pt != null)
						{
							pt.setMethod(method, obj);

							synchronized (pt)
							{
								pt.notifyAll();
							}

							rval = true;
						}
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return (rval);
	}


	/**
	 * This returns the total number of allocated PooledThreads.
	 */


	public int totalThreads()
	{
		return allocatedThreads.size();
	}


	/**
	 * This returns the number of allocated PooledThreads that are active.
	 */


	public int usedThreads()
	{
		return (allocatedThreads.size() - queue.size());
	}
	


	/**
	 * This returns the number of allocated PooledThreads that are free
	 */
	public int freeThreads()
	{
		return(queue.size());
	}


	// override this to use a subclass of PooledThread


	protected PooledThread makePooledThread() throws NullPointerException
	{
		if(!stopped) {
			if (max_threads > allocatedThreads.size())
			{
				PooledThread    pt = new PooledThread(this);

				pt.id = nextID++;

				pt.setDaemon(false);
				pt.setPriority(priority);
				allocatedThreads.addElement(pt);

				num_threads = allocatedThreads.size();

				return (pt);
			}
			else
			{
				throw (new NullPointerException("Reached Maximum PooledThread limit."));
			}
		}
		return(null);
	}


	protected void returnThread(PooledThread t)
	{
		if (allocatedThreads.contains(t))
		{
			queue.addElement(t);

			synchronized (queueChanged)
			{
				queueChanged.notify();
			}
		}
		else
		{
			System.err.println("{ThreadPool} Cannot return a PooledThread that isn't allocated by this ThreadPool.");
		}
	}


	protected void releaseThread(PooledThread t)
	{
		if (allocatedThreads.contains(t))
		{
			queue.removeElement(t);
			allocatedThreads.removeElement(t);

			num_threads = allocatedThreads.size();
		}
	}


	protected void error(String s, Exception e)
	{
		System.err.println("{ThreadPool} " + s + " : " + e);
		e.printStackTrace();
	}


	public static void main(String args[])
	{
		try
		{
			ThreadPool      pool = new ThreadPool(1000);

			for (int i = 0; i < 1000; i++)
			{
				System.out.println("i: " + i);

				if (args.length == 0)
				{
					Thread.currentThread().sleep(1);
				}
			}

			Thread.currentThread().sleep(5000);
			System.out.println("pts alloced: " + pool.num_threads);
		}
		catch (Exception exn)
		{
			System.out.println("main error");
			exn.printStackTrace();
		}
	}


}

