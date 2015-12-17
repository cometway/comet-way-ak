
package com.cometway.jdbc;


import java.util.Vector;


/**
* Drops the specified SQL table.
*/

public class JDBCDropTableAgent extends JDBCAgent
{
	public void initProps()
	{
		setDefault("jdbc_driver", "sun.jdbc.odbc.JdbcOdbcDriver");
		setDefault("jdbc_url", "jdbc:odbc:mydb");
		setDefault("jdbc_username", "none");
		setDefault("jdbc_password", "none");
		setDefault("table_name", "[CONTACTS]");
	}


	public void start()
	{
		String table_name = getTrimmedString("table_name");

		dropTable(table_name);
	}
}

