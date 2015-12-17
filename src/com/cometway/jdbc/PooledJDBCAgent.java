
package com.cometway.jdbc;

import java.sql.Connection;
import java.sql.SQLException;


public class PooledJDBCAgent extends JDBCAgent
{
	protected JDBCConnectionDriver jdbcConnectionDriver;


	public void initProps()
	{
		setDefault("jdbc_driver", "sun.jdbc.odbc.JdbcOdbcDriver");
		setDefault("jdbc_url", "jdbc:odbc:mydb");
//		setDefault("jdbc_username", "username");
//		setDefault("jdbc_password", "password");
	}


	public void start()
	{
		super.start();

		openConnectionDriver();
	}


	public void stop()
	{
		super.stop();

		closeConnectionDriver();
	}


	protected void closeConnectionDriver()
	{
		// We should be doing more to kill the JDBC thread pool here.

		jdbcConnectionDriver = null;
	}


	public Connection getConnection() throws SQLException
	{
		println("Connecting to database: " + getString("jdbc_url"));

		JDBCConnection connection = jdbcConnectionDriver.connect();

//		if (connection != null)
//		{
//			debug("Building data types...");
//
//			connection.buildDataTypeInfo();
//
//			debug("Connection established.");
//		}

		return (connection);
	}


	protected void openConnectionDriver()
	{
		String jdbc_driver = getString("jdbc_driver");
		String jdbc_url = getString("jdbc_url");
		String jdbc_username = getString("jdbc_username");
		String jdbc_password = getString("jdbc_password");

		try
		{
			debug("Creating JDBCConnectionDriver...");

			jdbcConnectionDriver = new JDBCConnectionDriver(jdbc_driver, jdbc_url, jdbc_username, jdbc_password, debugReporter);

			debug("Database is open.");
		}
		catch (ClassNotFoundException ex)
		{
			throw new RuntimeException("Could not find the JDBCDriver:" + ex.toString());
		}
		catch (InstantiationException ex2)
		{
			throw new RuntimeException("Could not instantiate the JDBCDriver:" + ex2.toString());
		}
		catch (IllegalAccessException ex3)
		{
			throw new RuntimeException("Could not access the JDBCDriver:" + ex3.toString());
		}
		catch (SQLException ex4)
		{
			throw new RuntimeException("Could not create connectionPool:" + ex4.toString());
		}
	}
}


