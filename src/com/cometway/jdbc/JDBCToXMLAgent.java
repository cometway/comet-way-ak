
package com.cometway.jdbc;

import com.cometway.ak.*;
import com.cometway.props.*;
import java.io.*;
import java.util.*;

/**
* This agent uses the JDBCAgent to access a relational database and output 
* data from specified tables in XML format. The tables to be parsed are
* specified in the property <TT>table_names<TT>.
*/

public class JDBCToXMLAgent extends ServiceAgent
{
	public void initProps()
	{
		setDefault("service_name", "jdbc_to_xml");
		setDefault("table_names", "Table1,Table2,Table3");
	}


	public void start()
	{
		register();

		String table_names = getString("table_names");
		StringTokenizer t = new StringTokenizer(table_names, ",");

		while (t.hasMoreTokens())
		{
			String tableName = t.nextToken();

			String xml = getTableAsXML(tableName);
			System.out.println(xml);
		}
	}


	public String getTableAsXML(String table_name)
	{
		StringBuffer b = new StringBuffer();
		JDBCAgent jdbc = (JDBCAgent) ServiceManager.getService("jdbc_agent");
		Vector results = jdbc.executeQuery("SELECT * FROM '" + table_name + "'"); // for typical SQL
//		Vector results = jdbc.executeQuery("SELECT * FROM [" + table_name + "]"); // for MS Access (JET-DRIVER)
//		Vector results = jdbc.executeQuery("SELECT * FROM [" + table_name + "$]"); // for accessing Excel via ODBC

		b.append("<TABLE>\n");
		b.append("\t<NAME>");
		b.append(table_name);
		b.append("</NAME>\n");

		for (int i = 0; i < results.size(); i++)
		{
			Props p = (Props) results.elementAt(i);
			Vector keys = p.getKeys();

			b.append("\t<ROW>\n");

			for (int x = 0; x < keys.size(); x++)
			{
				String key = (String) keys.elementAt(x);
				String value = p.getString(key);

				b.append("\t\t");
				b.append('<');
				b.append(key);
				b.append('>');
				b.append(value);
				b.append("</");
				b.append(key);
				b.append('>');
				b.append('\n');
			}

			b.append("\t</ROW>\n");
		}
			
		b.append("</TABLE>\n");

		return(b.toString());
	}
}


