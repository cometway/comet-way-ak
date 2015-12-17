
package com.cometway.jdbc;


import java.util.Vector;


/**
* Creates a new table using the specified table name, and field properties.
*/

public class JDBCCreateTableAgent extends JDBCAgent
{
	public void initProps()
	{
		setDefault("jdbc_driver", "sun.jdbc.odbc.JdbcOdbcDriver");
		setDefault("jdbc_url", "jdbc:odbc:mydb");
		setDefault("jdbc_username", "none");
		setDefault("jdbc_password", "none");
		setDefault("table_name", "[CONTACTS]");
		setDefault("table_fields", "[First Name], [Last Name], [Address], [City], [State], [Zip Code], [Birth Date], [Age]");
		setDefault("table_types", "varchar (50) not null, varchar (50) not null, varchar (50), varchar (50), varchar (2), varchar (15), datetime, int");
	}


	public void start()
	{
		String table_name = getTrimmedString("table_name");
		Vector fieldList = getTokens("table_fields");
		Vector typeList = getTokens("table_types");

		createTable(table_name, fieldList, typeList);
	}
}

