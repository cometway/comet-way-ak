
package com.cometway.jdbc;


import com.cometway.util.ReporterInterface;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.NoSuchElementException;

/**
* The JDBCConnectionPool manages pooled JDBCConnnections for the JDBCConnectionDriver.
*/
public class JDBCConnectionPool
{
	static final private long CONNECTION_OPENER_MS = 10000;
	static final private long CONNECTION_STALE_MS = 60000;
	static final private long GET_CONNECTION_MS = 1000;
	static final private int DEFAULT_POOL_SIZE = 5;

	protected static long uniqueIndex = 0;

	protected ReporterInterface reporter;

	private String uniqueID;
	private ArrayBlockingQueue connections;
	private int connectionsCount;
	private String jdbcURL;
	private String username;
	private String password;
	private ConnectionOpener watchDog;
	private boolean isPooling;
	private boolean testConnections;
	private int poolSize;

	/**
	 * Creates a JDBCConnectionPool using the given jdbcURL, username, password, and reporter.
	 * The pool's size is set to the default size and no validation is used to make sure the
	 * JDBCConnections are valid.
	 */
	public JDBCConnectionPool(String jdbcURL, String username, String password, ReporterInterface reporter) throws SQLException, InterruptedException
	{    
		this(jdbcURL,username,password,reporter,false,DEFAULT_POOL_SIZE);
	}

	/**
	 * Creates a JDBCConnectionPool using the given jdbcURL, username, password, reporter and pool size.
	 * No validation is used to make sure the JDBCConnections are valid.
	 */
	public JDBCConnectionPool(String jdbcURL, String username, String password, ReporterInterface reporter, int poolSize) throws SQLException, InterruptedException
	{    
		this(jdbcURL,username,password,reporter,false,poolSize);
	}

	/**
	 * Creates a JDBCConnectionPool using the given jdbcURL, username, password, reporter, validation, and pool size.
	 * If connectionTesting is set to true, JDBCConnections are validated before returned by getConnection().
	 */
	public JDBCConnectionPool(String jdbcURL, String username, String password, ReporterInterface reporter, boolean connectionTesting, int poolSize) throws SQLException, InterruptedException
	{    
		this.uniqueID = "JDBCConnectionPool[" + (uniqueIndex++) + "]";
		this.jdbcURL = jdbcURL;
		this.username = username;
		this.password = password;
		this.reporter = reporter;
		testConnections = connectionTesting;
		this.poolSize = poolSize;
		connections = new ArrayBlockingQueue(poolSize,true);
		connectionsCount = 0;

		refillConnectionPool();

		isPooling = true;

		watchDog = new ConnectionOpener();
		watchDog.start();
	}



	/**
	 * Returns the Pool's current size
	 */
	public int size()
	{
		return(connectionsCount);
	}


	/**
	 * This should only be called by the watchdog thread
	 */
	private void refillConnectionPool()
	{
		try
		{
			while (connectionsCount < poolSize)
			{
				Connection conn = DriverManager.getConnection(jdbcURL, username, password);
				JDBCConnection c = new JDBCConnection(jdbcURL, conn, this, reporter);
				connectionsCount++;
				if(!connections.offer(c)) {
					removeConnection(c);
					break;
				}

				debug("Added connection: " + c);
			}
		}
		catch (Exception e)
		{
			error("Could not refill the connection pool.", e);
		}
	}


	/**
	* Removes each connection from the connection pool and stops the WatchDog thread.
	*/
	public void closeConnections()
	{
		isPooling = false;

		// Wake the watchdog up
		watchDog.wakeup();

		try {
			while(true) {
				JDBCConnection c = (JDBCConnection) connections.remove();
				removeConnection(c);
			}
		}
		catch(NoSuchElementException e) {;}

		while(connectionsCount>0) {
			try {
				JDBCConnection c = (JDBCConnection) connections.poll(5,TimeUnit.SECONDS);
				removeConnection(c);
			}
			catch(InterruptedException e) {
				// Failed.
				break;
			}
		}
	}


	/**
	* Removes the specified connection from the connection pool.
	* This can be called by a caller when the JDBCConnection is no longer
	* connected to the database and the connection should NOT be added back
	* into the pool.
	*/
	public void removeConnection(JDBCConnection c)
	{
		debug("Removing connection: "+c);
		connections.remove(c);
		try {
			c.getConnection().close();
		}
		catch(java.sql.SQLException sqlex) {
			error("Error closing connection",sqlex);
		}
		connectionsCount--;
	}


	/**
	* Attempts to lease a connection from the pool.
	* This method will block until a valid connection is returned.
	*/
	public JDBCConnection getConnection() throws SQLException, InterruptedException
	{
		JDBCConnection c = null;

		while(true) {
			c = (JDBCConnection)connections.take();
			
			if(testConnections) {
				if(!c.validate()) {
					removeConnection(c);
					watchDog.wakeup();
				}
				else {
					break;
				}
			}
			else {
				break;
			}
		}
		return(c);
	}


	/**
	* Marks the specified connection as no longer in use so that it can be
	* returned by subsequent calls to getConnection().
	*/
	public void returnConnection(JDBCConnection conn)
	{
		debug("Returning Connection: "+conn);
		conn.expireLease();
		if(!connections.offer(conn)) {
			removeConnection(conn);
		}
	}

	
	/**
	* Returns this connection as a String.
	*/
	public String toString()
	{
		return (uniqueID);
	}

	/**
	* Prints a debug message tagged with this JDBCConnectionPool's identity to the output stream.
	*/
	public void debug(String message)
	{
		if (reporter != null)
		{
			reporter.debug(this, message);
		}
	}


	/**
	* Prints a warning message tagged with this JDBCConnectionPool's identity to the error stream.
	* If the Exception is type java.lang.reflect.InvocationTargetException
	* that exception's target exception is also stack traced.
	*/
	public void warning(String message)
	{
		if (reporter != null)
		{
			reporter.warning(this, message);
		}
	}


	/**
	* Prints an warning message tagged with this JDBCConnectionPool's identity
	* followed by a stack trace of the passed Exception to error stream.
	* If the Exception is type java.lang.reflect.InvocationTargetException
	* that exception's target exception is also stack traced.
	*/
	public void warning(String message, Exception e)
	{
		if (reporter != null)
		{
			reporter.warning(this, message, e);
		}
	}


	/**
	* Prints an error message tagged with this JDBCConnectionPool's identity to the error stream.
	* If the Exception is type java.lang.reflect.InvocationTargetException
	* that exception's target exception is also stack traced.
	*/
	public void error(String message)
	{
		if (reporter != null)
		{
			reporter.error(this, message);
		}
	}


	/**
	* Prints an error message tagged with this JDBCConnectionPool's identity
	* followed by a stack trace of the passed Exception to the error stream.
	* If the Exception is type java.lang.reflect.InvocationTargetException
	* that exception's target exception is also stack traced.
	*/
	public void error(String message, Exception e)
	{
		if (reporter != null)
		{
			if (e instanceof SQLException)
			{
				SQLException x = (SQLException) e;

				reporter.error(this, message);

				do
				{
					reporter.error(this, "   SQLState = " + x.getSQLState(), x);

					x = x.getNextException();
				}
				while (x != null);
			}
			else
			{
				reporter.error(this, message, e);
			}
		}
	}


	/**
	* Prints a message tagged with this JDBCConnectionPool's identity to the output stream.
	*/
	public void println(String message)
	{
		if (reporter != null)
		{
			reporter.println(this, message);
		}
	}


	/**
	* Sets the reporter for this JDBCConnectionPool.
	*/
	public void setReporter(ReporterInterface reporter)
	{
		this.reporter = reporter;
	}



	/* This class runs on a thread to periocially call refillConnections(). */
	class ConnectionOpener extends Thread
	{
		public void wakeup()
		{
			synchronized(watchDog) {
				watchDog.notifyAll();
			}
		}

		public void run()
		{
			while (isPooling)
			{
				refillConnectionPool();
			   try
			   {
					synchronized(watchDog) {
						watchDog.wait(CONNECTION_OPENER_MS);
					}
			   }
			   catch (InterruptedException e)
			   { }
			}
		}
	}
}


