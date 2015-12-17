
package com.cometway.io;


import com.cometway.ak.Agent;
import com.cometway.props.Props;
import com.cometway.props.PropsException;
import com.cometway.props.PropsList;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;


/**
* This agent exports the contents of a Props list
* to a tab delimited text file suitable for import by
* other applications such as FileMaker or Excel.
*/

public class DelimitedLineExportAgent extends Agent
{
	protected final static String EOL = System.getProperty("line.separator");


	public void initProps()
	{
		setDefault("database_name", "database");
		setDefault("delimiter_char", "\t");
		setDefault("from_fields", "FIRST_NAME, LAST_NAME, ADDRESS, CITY, STATE, ZIP, BIRTHDATE, AGE");
		setDefault("save_file", "contacts.tdl");
		setDefault("to_fields", "First Name, Last Name, Address, City, State, Zip Code, Birthdate, Age");
	}


	public void start()
	{
		try
		{
			saveToFile();
		}
		catch (Exception e)
		{
			error("Could not continue.", e);
		}
	}


	public void saveToFile() throws PropsException
	{
		String save_file = getString("save_file");

		if (save_file.length() == 0)
		{
			throw new PropsException("Zero length filename");
		}

		String tempfile = save_file + ".temp";

		File file = new File(save_file);
		File temp = new File(tempfile);
		FileWriter tempWriter = null;
		BufferedWriter out = null;

		try
		{
			tempWriter = new FileWriter(temp);
			out = new BufferedWriter(tempWriter);

			saveAllRecords(out);

			// Make sure our stream is closed so others can change this file.
			// Windows 2K is very strict about file locking.

			out.close();
			out = null;

			tempWriter.close();
			tempWriter = null;

			if (file.exists())
			{
				boolean result = file.delete();
// This always returns false under Windows 2K...how useful is that?
			}

			temp.renameTo(file);
		}
		catch (Exception e)
		{
			throw new PropsException("Could not save: " + save_file, e);
		}
		finally
		{
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (Exception e2)
				{
					System.err.println("Could not close BufferedWriter: " + save_file);
					e2.printStackTrace(System.err);
				}
			}

			if (tempWriter != null)
			{
				try
				{
					tempWriter.close();
				}
				catch (Exception e2)
				{
					System.err.println("Could not close FileWriter: " + save_file);
					e2.printStackTrace(System.err);
				}
			}
		}
	}


	protected void saveAllRecords(Writer out) throws Exception
	{
		Vector from_fields = getTokens("from_fields");
		Vector to_fields = getTokens("to_fields");
		char delimiter_char = getCharacter("delimiter_char");
		int toCount = to_fields.size();

		for (int i = 0; i < toCount; i++)
		{
			if (i > 0) out.write(delimiter_char);

			out.write((String) to_fields.get(i));
		}

		out.write(EOL);

		String database_name = getString("database_name");
		PropsList database = (PropsList) getServiceImpl(database_name);
		List v = database.listProps();
		int count = v.size();

		for (int i = 0; i < count; i++)
		{
			Props p = (Props) v.get(i);

			println("Saving " + database_name + " record (" + (i + 1) + " of " + count + ")");

			for (int x = 0; x < toCount; x++)
			{
				if (x > 0) out.write(delimiter_char);
	
				String value = (String) from_fields.get(x);

				out.write(p.getString(value));
			}
	
			out.write(EOL);
		}

		out.write(EOL);
		out.flush();
	}
}

