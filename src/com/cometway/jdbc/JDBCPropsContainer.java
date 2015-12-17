package com.cometway.jdbc;

	/**
	* This class implements an IPropsContainer using a JDBC connection
	* to store properties in database tables. Props of the same props 
	* type are stored in the same table.
	*/

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.Math;

import com.cometway.props.*;

public class JDBCPropsContainer extends PropsContainer
{
	protected JDBCConnectionDriver  connectionMgr;
	protected String		table;
	protected String		name;
	private static boolean	debug;
	private static long		readAttempts;
	private static long		readCount;
	private static long		cacheHits;
	private static long		readTime;
	private static long		readMin = 60000;
	private static long		readMax;
	private static long		readErrors;
	private static long		writeAttempts;
	private static long		writeCount;
	private static long		writeTime;
	private static long		writeMin = 60000;
	private static long		writeMax;
	private static long		writeErrors;
	private static int		DEFAULT_MAX_FIELD_SIZE = 255;	// Maximum field size to use if JDBCDriver can't figure it out.
	private int				maxRowSize;			// maximum size of a varbinary/varchar type (in bytes - this is DB dependent)
	private Vector			writeThreads;		// Vector of object writer threads.


	/**
	 * 
	 * @param connectionDriver The JDBCConnectionDriver to use for DB connections.
	 * @param objectWriterThreads The ThreadPool to use when performing lazy writes
	 * @param table The name of the table in the database where these
	 * props are stored.
	 * @param name The name of this props.
	 */

	public JDBCPropsContainer(JDBCConnectionDriver connectionDriver, Vector objectWriterThreads, String table, String name)
	{
		this.writeThreads = objectWriterThreads;
		this.connectionMgr = connectionDriver;
		this.table = table;
		this.name = name;


		// This should work but it doesn't.
		// maxRowSize = connection.getMaxRowSize();

		maxRowSize = DEFAULT_MAX_FIELD_SIZE;
	}


	/**
	 * Copies the data from this JDBCPropsContainer to the one passed in as an argument.
	 * 
	 * @param ipc The props to copy into.
	 */


	// public synchronized void copy(IPropsContainer ipc)

	public void copy(IPropsContainer ipc)
	{


		// New copy code:

		Enumeration     n = enumerateProps();

		while (n.hasMoreElements())
		{
			String  key = (String) n.nextElement();
			Object  value = getProperty(key);

			if (value != null)
			{
				ipc.setProperty(key, value);
			}
		}
	}


	public Enumeration enumerateProps()
	{
		Vector  v = new Vector();

		debug("enumerateProps", table + "|" + name);

		com.cometway.jdbc.JDBCConnection    connection = null;

		try
		{
			connection = connectionMgr.connect();

			//connection.setAutoCommit(false);

			String		sql = "select keyname from " + table + " where name='" + name + "'";
			Statement       statement = connection.createStatement();
			ResultSet       resultSet = statement.executeQuery(sql);
			String		s;

			while (resultSet.next())
			{
				s = resultSet.getString(1);

				v.addElement(s);
			}

			statement.close();
			connection.commit();
		}
		catch (SQLException e)
		{
			sqlError("enumerateProps", e);
		}

		try
		{
			connection.close();
		}
		catch (SQLException e)
		{
			sqlError("enumerateProps", e);
		}

		return (v.elements());
	}


	public Object getProperty(String key)
	{
		Object  value;
		long    time;

		debug("getProperty", key);


		// Initialize variables for performance tracking.

		readAttempts++;
		time = System.currentTimeMillis();
		value = super.getProperty(key);

		if (value == null)
		{
			com.cometway.jdbc.JDBCConnection    connection = null;

			try
			{
				connection = connectionMgr.connect();
			}
			catch (SQLException e)
			{
				sqlError("getProperty(" + key + ")", e);

				readErrors++;
			}

			try
			{
				String  sql = "select value from " + table + " where name=? and keyname=?";


				// The "order by" syntax means that the resultSet will be
				// ordered correctly for de-serializing the object.

				if (table != "types")
				{
					sql = "select text_value from " + table + " where name=? and keyname=? order by write_order";
				}

				PreparedStatement       statement = connection.prepareStatement(sql);

				statement.setString(1, name);
				statement.setString(2, key);

				ResultSet       resultSet = statement.executeQuery();


				// If there are results, then this is a text property. If not,
				// look in the value column for binary property data.

				Vector		strings = new Vector();

				while (true)
				{
					if (!resultSet.next())
					{
						break;
					}

					try
					{
						String  b = resultSet.getString(1);

						if (b != null)
						{
							strings.addElement(b);
						}
					}
					catch (Exception e)
					{
						error("getProperty", "", e);

						readErrors++;
					}
				}

				statement.close();

				if (strings.size() > 0)		// This value was stored as a String, so concat the results and return them.
				{
					StringBuffer    resultString = new StringBuffer();

					for (int i = 0; i < strings.size(); i++)
					{
						Object  tempstr = strings.elementAt(i);

						if (tempstr != null)
						{
							resultString.append((String) tempstr);
						}
					}

					value = resultString.toString();
				}
				else
				{		// This property was stored as binary data, so get the binary value.
					sql = "select value from " + table + " where name=? and keyname=? order by write_order";

					PreparedStatement       statement2 = connection.prepareStatement(sql);

					statement2.setString(1, name);
					statement2.setString(2, key);

					ResultSet       resultSet2 = statement2.executeQuery();


					// Create a vector to store concatenatedd chunks.

					Vector		byteArrays = new Vector();


					// Take all the data chunks from our resultSet and
					// concatenate them to build our object's data.

					while (true)
					{
						if (!resultSet2.next())
						{
							break;
						}

						try
						{
							byte[]  b = resultSet2.getBytes(1);

							byteArrays.addElement(b);
						}
						catch (Exception e)
						{
							error("getProperty", "", e);

							readErrors++;
						}
					}

					statement2.close();

					if (byteArrays.size() == 0)
					{
						debug("getProperty", "No data to return, returning null.");

						readCount++;

						connection.close();

						return null;
					}

					try
					{


						// Now that we have the data, instantiate the object.

						PipedInputStream	pin = new PipedInputStream();


						// Spawn a seperate thread to work with the piped data.

						boolean			foundAThread = false;
						int			poolSize = writeThreads.size();

						while (true)
						{
							for (int i = 0; i < poolSize; i++)
							{
								ObjectWriterThread      t = (ObjectWriterThread) writeThreads.elementAt(i);

								if (!t.isBusy())
								{
									foundAThread = true;

									t.connect(pin);
									t.setByteData(byteArrays);
									t.start();

									i = poolSize;
								}
								else
								{
									debug("getProperty", "Waiting for an ObjectWriter thread.");
								}
							}

							if (foundAThread)
							{
								break;
							}
							else
							{
								debug("getProperty", "All ObjectWriter threads are busy, waiting for a thread to become available.");
							}

							try
							{
								wait(1000);
							}
							catch (Exception e)
							{
								debug("getProperty", "Exception:" + e.toString());
							}
						}

						ObjectInputStream       oin = new ObjectInputStream(pin);

						value = oin.readObject();

						pin.close();
						oin.close();
					}
					catch (Exception e)
					{
						debug("getProperty", "Data:" + byteArrays + " Exception:" + e.toString());
						e.printStackTrace();

						readErrors++;
					}
				}

				readCount++;
				time = System.currentTimeMillis() - time;

				if (time < readMin)
				{
					readMin = time;
				}

				if (time > readMax)
				{
					readMax = time;
				}

				readTime += time;

				connection.close();
			}
			catch (SQLException e)
			{
				sqlError("getProperty(" + key + ")", e);

				readErrors++;
			}

			try
			{
				connection.close();
			}
			catch (SQLException e)
			{
				sqlError("getProperty(" + key + ")", e);
			}

			if (value != null)
			{
				super.setProperty(key, value);
			}
		}
		else
		{
			cacheHits++;
		}

		return (value);
	}


	// public synchronized boolean removeProperty(String key)

	public boolean removeProperty(String key)
	{
		boolean result = false;

		debug("removeProperty", key);
		super.removeProperty(key);

		com.cometway.jdbc.JDBCConnection    connection = null;
		
		try
		{ 
			connection = connectionMgr.connect(); 
		}
		catch (SQLException e)
		{
			sqlError("removeProperty(" + key + ")", e);
		}

		try
		{

			String sql = "delete from " + table + " where name='" + name + "' and keyname='" + key + "'";
			Statement statement = connection.createStatement();

			statement.executeUpdate(sql);
			statement.close();

			/*
			connection.setAutoCommit(false);
			String			sql = "delete from " + table + " where name=? and keyname=?";
			PreparedStatement       statement = connection.prepareStatement(sql);
			statement.setString(1, name);
			statement.setString(2, key);
			statement.executeUpdate();
			statement.close();
			*/

			connection.commit();

			result = true;
		}
		catch (SQLException e)
		{
			sqlError("removeProperty(" + key + ")", e);
		}

		try
		{ 
			connection.close(); 
		}
		catch (SQLException e)
		{
			sqlError("removeProperty(" + key + ")", e);
		}

		return (result);
	}

	public void setProperty(String key, Object value)
	{
		long    time = System.currentTimeMillis();

		debug("setProperty", key);

		writeAttempts++;

		super.setProperty(key, value);

		com.cometway.jdbc.JDBCConnection    connection = null;

	    try
		{
			connection = connectionMgr.connect();
			//mysql doesn't like this	-danik
			//connection.setAutoCommit(false);
		}
		catch (SQLException e)
		{
			sqlError("setProperty(" + key + ")", e);
			writeErrors++;
		}

		try
		{

			try
			{

				String		sql = "delete FROM " + table + " where name='" + name + "' and keyname='" + key +"'";
				Statement	statement = connection.createStatement();


				statement.executeUpdate(sql);
				statement.close();

			}
			catch (SQLException e)
			{
				sqlError("setProperty(" + key + ")", e);

				writeErrors++;
			}

			try
			{
				if (value != null)
				{


					// If the value is something that can be easily
					// cast to text, put it in the text_value column.

					if ((value instanceof String) || (value instanceof Boolean) || (value instanceof Integer))
					{


						// Break the String up into chunks the size of maxRowSize

						int     strLength = (value.toString()).length();
						int     counter = 0;

						for (int i = 0; i < strLength; i = i + maxRowSize)
						{
							String  chunk = (value.toString()).substring(i, java.lang.Math.min(strLength, i + maxRowSize));
							String  sql = "insert " + table + " (name,keyname,write_order,text_value) values (?,?,?,?)";
							PreparedStatement       statement = connection.prepareStatement(sql);

							statement.setString(1, name);
							statement.setString(2, key);
							statement.setInt(3, counter);
							statement.setString(4, chunk);

							try
							{
								statement.executeUpdate();
							}
							catch (SQLException e)
							{
								sqlError("setProperty[text](" + key + ")", e);

								writeErrors++;
							}

							counter++;

							statement.close();
						}
					}
					else
					{


						// The value is an object that must be serialized
						// so put it in a series of varbinary rows.

						PipedInputStream	pin = new PipedInputStream();
						boolean			foundAThread = false;
						int			poolSize = writeThreads.size();

						while (true)
						{
							for (int i = 0; i < poolSize; i++)
							{
								ObjectWriterThread      t = (ObjectWriterThread) writeThreads.elementAt(i);

								if (!t.isBusy())
								{
									foundAThread = true;

									t.connect(pin);
									t.setData(value);
									t.start();

									i = poolSize;
								}
								else
								{
									debug("setProperty", "Waiting for ObjectWriter thread to become available.");
								}
							}

							if (foundAThread)
							{
								break;
							}
							else
							{
								debug("setProperty", "No ObjectWriter threads are available yet.");
							}

							try
							{
								wait(1000);
							}
							catch (Exception e)
							{
								debug("setProperty", "Exception:" + e.toString());
							}
						}


						// For better DB performance, we break up the serialized
						// object data into chunks the size of DB rows. Each data
						// chunk is stored w/ a record of the order in which it was
						// written so the data can be rebuilt properly.

						String  sql = "insert " + table + " (name,keyname,write_order,value) values (?,?,?,?)";
						int     counter = 0;

						while (true)
						{
							byte[]  buffer = new byte[maxRowSize];
							int     amountToRead = maxRowSize;
							int     result = 0;

							try
							{
								result = pin.read(buffer, 0, amountToRead);
							}
							catch (Exception e)
							{
								debug("setProperty", "Exception:" + e.toString());
								e.printStackTrace();

								break;
							}


							// -1 is returned when there is no
							// more data in the pipe.

							if (result == -1)
							{
								break;
							}

							PreparedStatement       statement = connection.prepareStatement(sql);

							statement.setString(1, name);
							statement.setString(2, key);
							statement.setInt(3, counter);
							statement.setBytes(4, buffer);

							try
							{
								statement.executeUpdate();
							}
							catch (SQLException e)
							{
								sqlError("setProperty(" + key + ")", e);

								writeErrors++;
							}

							statement.close();

							counter++;
						}

						pin.close();

						writeCount++;
						time = System.currentTimeMillis() - time;

						if (time < writeMin)
						{
							writeMin = time;
						}

						if (time > writeMax)
						{
							writeMax = time;
						}

						writeTime += time;
					}
				}
			}
			catch (IOException e)
			{
				debug("setProperty", "ioexception:" + e.toString());
				error("setProperty", "Exception setting property", e);
				writeErrors++;
			}

			connection.commit();
		}
		catch (SQLException e)
		{
			sqlError("setProperty(" + key + ")", e);
			writeErrors++;
		}

		try
		{
			connection.close();
		}
		catch (SQLException e)
		{
			sqlError("setProperty(" + key + ")", e);
			writeErrors++;
		}
	}


	/**
	 * Creates a table in the database using a
	 * connection from the connection pool.
	 * 
	 * Tables are created with the following columns:
	 * name        - the UniqueID of this props object, all properties of this props have the same name ID.
	 * keyname     - the name of this property in the props object.
	 * write_order - a number representing which chunk of data this is (i.e. first chunk, third chunk).
	 * value       - a chunk (varbinary(maxRowSize)) of the serialized Java object representing
	 * this property's value.
	 * There can be multiple rows in a table that contain PARTS of a value;
	 * these rows will have the same 'name' and 'keyname' values, use the "order by" syntax to
	 * sort data by the "write_order" value so data is returned in the order that the object
	 * must be de-serialized (top to bottom.)
	 * 
	 * @param connectionMgr The connectionManager to get a pooled connection from.
	 * @param name The name of the table to create.
	 */

	public static boolean createTable(JDBCConnectionDriver connectionMgr, String name)
	{
		boolean		result;
		Statement       statement;
		String		s;

		debug("createTable", name);

		result = false;


		// this should work to get the max row size, but it doesn't.
		// int maxRSize = connection.getMaxRowSize();

		int					maxRSize = DEFAULT_MAX_FIELD_SIZE;
		JDBCConnection    connection = null;

		try
		{
			connection = connectionMgr.connect();
		}
		catch (SQLException e)
		{
			sqlError("createTable", e);
		}

		try
		{
			String  blobStr = connection.getTypeName(Types.LONGVARBINARY);
			statement = connection.createStatement();

			s = "create table IF NOT EXISTS " + name 
				+ " (name varchar(64) not null," 
				+ "keyname varchar(64) not null," 
				+ "write_order int not null," 
				+ "text_value varchar(" + maxRSize + ") null," 
				+ "value " + blobStr +  ")";

			try
			{
				debug("createTable", "SQL: " + s);
				statement.executeUpdate(s);

				result = true;
			}
			catch (SQLException e)
			{
				s = e.getSQLState();
				result = false;


				// Check if the table already exists.

				if ((s.equals("S0001")) || (s.equals("01000")))
				{
					result = true;
				}

				if (result == false)
				{
					sqlError("createTable", e);
				}
			}

			statement.close();


			// Grant appropriate permissions for this table.

			statement = connection.createStatement();
			s = "grant ALL on " + name + " to PUBLIC";

			try
			{
				debug("createTable", "SQL: " + s);
				statement.executeUpdate(s);

				result = true;
			}
			catch (SQLException e)
			{
				s = e.getSQLState();

				if ((s.equals("S0001")) || (s.equals("01000")))		// Table already exists.
				{
					result = true;
				}

				if (result == false)
				{
					sqlError("createTable", e);
				}
			}

			connection.commit();
		}
		catch (SQLException e)
		{
			sqlError("createTable", e);
		}

		try
		{
			connection.close();
		}
		catch (SQLException e)
		{
			sqlError("createTable", e);
		}

		return (result);
	}


	private static void debug(String methodName, String message)
	{
		if (debug)
		{
			System.out.println("JDBCPropsContainer." + methodName + ": " + message);
		}
	}


	/**
	 * Drops a table from the database.
	 * 
	 * @param connectionMgr The connectionManager to get a pooled DB connection from.
	 * @param name The name of the table to drop.
	 */

	public static boolean dropTable(JDBCConnectionDriver connectionMgr, String name)
	{
		Statement       statement;

		debug("dropTable", name);

		boolean					result = false;
		com.cometway.jdbc.JDBCConnection    connection = null;

		try
		{
			connection = connectionMgr.connect();			
		}
		catch (SQLException e)
		{
			sqlError("dropTable", e);
		}

		try
		{
			statement = connection.createStatement();

			statement.executeUpdate("drop table " + name);
			statement.close();

			result = true;
		}
		catch (SQLException e)
		{
			sqlError("dropTable", e);
		}
		
		try
		{
			connection.close();
		}
		catch (SQLException e)
		{
			sqlError("dropTable", e);
		}

		return (result);
	}


	private static void error(String methodName, String message, Exception e)
	{
		System.err.println("JDBCPropsContainer." + methodName + ": " + message + "\n" + e);
	}


	/**
	 * Returns a string containing current status information about JDBCPropsContainer performance.
	 */

	public static String getStatus()
	{
		StringBuffer    b = new StringBuffer();

		try
		{
			b.append("JDBCPropsContainer Status\n");
			b.append("          Version: 1.0a\n");
			b.append("        Debugging: " + debug + "\n");
			b.append("    Read Attempts: " + readAttempts + " (" + cacheHits + " cache hits)\n");
			b.append("       Read Count: " + readCount + " (" + readErrors + " errors)\n");
		}
		catch (java.lang.ArithmeticException e)
		{
			;
		}

		try
		{
			b.append("    Avg Read Time: " + (readTime / readCount) + " ms (" + readMin + "/" + readMax + ")\n");
		}
		catch (java.lang.ArithmeticException e)
		{
			;
		}

		try
		{
			b.append("   Write Attempts: " + writeAttempts + "\n");
			b.append("      Write Count: " + writeCount + " (" + writeErrors + " errors)\n");
			b.append("   Avg Write Time: " + (writeTime / writeCount) + " ms (" + writeMin + "/" + writeMax + ")\n");
		}
		catch (java.lang.ArithmeticException e)
		{
			;
		}

		return (b.toString());
	}


	/**
	 * Enables debugging messages that are printed to the System.out stream.
	 * This feature is turned off by default.
	 * 
	 * @param state true enables debugging; false disables it.
	 */

	public static void setDebug(boolean state)
	{
		debug = state;
	}


	/**
	 * Called internally to report JDBC related exceptions.
	 */

	private static void sqlError(String methodName, SQLException e)
	{
		System.err.println("*** SQL Error ***");
		System.err.println("JDBCPropsContainer." + methodName + ":");

		while (e != null)
		{
			System.err.println("SQLState   = " + e.getSQLState());
			System.err.println("Error Code = " + e.getErrorCode());
			System.err.println(e.getMessage());
			System.err.println();
			System.err.flush();

			e = e.getNextException();
		}
	}


}

