
package com.cometway.io;


import com.cometway.ak.Agent;
import com.cometway.props.Props;
import com.cometway.props.PropsList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.util.Vector;


/**
* This agent reads lines from the specified delimited text file
* and copies its records into the specified PropsList.
* All lines are assumed to be separated by a newline.
*/

public class DelimitedLineImportAgent extends Agent
{
	public void initProps()
	{
		setDefault("load_file", "contacts.tdl");
		setDefault("database_name", "database");
		setDefault("delimiter_char", "\t");
	}


	public void start()
	{
		String database_name = getString("database_name");
		String load_file = getString("load_file");
		char delimiter_char = getCharacter("delimiter_char");
		BufferedReader reader = null;

		try
		{
			println("Loading file: " + load_file);
			println("Storing records in " + database_name);

			File file = new File(load_file);
			reader = new BufferedReader(new FileReader(file));
			String[] columns = getColumns(reader.readLine());
//debug("columns.length = " + columns.length);

			int record = 0;

			while (true)
			{
				String line = reader.readLine();

				if (line == null)
				{
					break;
				}

				Props p = createProps();
				p.setProperty("IMPORT_FILE", load_file);
				p.setInteger("IMPORT_ROW", record + 1);

				for (int c = 0; c < columns.length; c++)
				{
					String s;
					int i = line.indexOf(delimiter_char);

					if (i < 0)
					{
						s = line.trim();

						line = "";
					}
					else
					{
						s = line.substring(0, i).trim();

						line = line.substring(i + 1);
					}
//debug("c = " + c + " - " + columns[c] + " = " + s);

					p.setProperty(columns[c], s);
				}

				record++;
			}
//debug("record = " + record);


			// Save the keys.

			String s = new String();

			for (int i = 0; i < columns.length; i++)
			{
				if (i > 0) s += ',';

				s += columns[i];
			}
debug("database_keys: " + s);
debug("database_rows: " + record);

			setProperty("database_keys", s);
			setInteger("database_rows", record);
		}
		catch (Exception e)
		{
			error("Error importing: " + load_file, e);
		}
		finally
		{
			try
			{
				if (reader != null)
				{
					reader.close();
				}
			}
			catch (Exception ex)
			{
				;
			}
		}
	}


	protected String[] getColumns(String s)
	{
		String delimiter_char = getString("delimiter_char");
		println("Columns: " + s.replace(delimiter_char.charAt(0), ','));

		Vector v = new Vector();
		StringTokenizer t = new StringTokenizer(s, delimiter_char);

		while (t.hasMoreTokens())
		{
			String name = t.nextToken();
//debug("name = " + name);
			v.add(name);
		}

		String[] columns = new String[v.size()];

		for (int i = 0; i < columns.length; i++)
		{
			columns[i] = (String) v.get(i);

//debug("columns[" + i + "] = " + columns[i]);
		}

		return (columns);
	}



	protected Props createProps()
	{
		Props p = null;
		String serviceName = getString("database_name");
		PropsList list = (PropsList) getServiceImpl(serviceName);

		if (list != null)
		{
			p = list.createProps();
		}
		else
		{
			error("Cannot create Props in " + serviceName);
		}

		return (p);
	}
}

