
package com.cometway.jdbc;


import com.cometway.props.Props;
import java.util.Vector;


/**
* This agent executes SQL the command specified by sql_statement through a JDBC interface.
*/

public class JDBCExecutive extends JDBCAgent
{
	public void initProps()
	{
		setDefault("jdbc_driver", "net.sourceforge.jtds.jdbc.Driver");
		setDefault("jdbc_url", "jdbc:jtds:sqlserver://hostname/database");
		setDefault("jdbc_username", "username");
		setDefault("jdbc_password", "password");
		setDefault("sql_statement", "SELECT * FROM CONTACTS ORDER BY [First Name], [Last Name]");
	}


	public void start()
	{
		String sql_statement = getString("sql_statement");

		if (sql_statement.toLowerCase().startsWith("select"))
		{
			Vector results = executeQuery(sql_statement);
			int count = results.size();

			for (int i = 0; i < count; i++)
			{
				Props p = (Props) results.get(i);

				println("=============== " + (i + 1) + " of " + count + " ===============\n" + p.toString());
			}
		}
		else
		{
			executeUpdate(sql_statement);
		}
	}
}


