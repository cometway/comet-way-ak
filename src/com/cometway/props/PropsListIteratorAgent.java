
package com.cometway.props;


import com.cometway.ak.Agent;
import java.util.StringTokenizer;
import java.util.List;
import java.util.Vector;


/**
* This agent can be extended to iterate over a PropsList
* passing each Props value to the handleRequest method
* when the agent is started, or it's iterate method is called.
*/

public class PropsListIteratorAgent extends Agent
{
	public void initProps()
	{
		setDefault("database_name", "database");
	}


	/**
	* Iterates over the PropsList specified by databse_name.
	*/

	public void start()
	{
		String database_name = getString("database_name");

		println("Iterating Props in " + database_name);

		iterate(database_name);
	}


	/**
	* Iterates over the specified PropsList calling handleRequest
	* for each Props in the PropsList.
	*/

	public void iterate(String database_name)
	{
		try
		{
			PropsList list = (PropsList) getServiceImpl(database_name);
			List v = list.listProps();
			int count = v.size();

			for (int i = 0; i < count; i++)
			{
				Props p = (Props) v.get(i);

				println("Processing item " + (i + 1) + " of " + count);
				handleRequest(p);
			}
		}
		catch (Exception e)
		{
			error("Could not continue", e);
		}
	}


	/**
	* Override this method to perform custom operations on each Props
	* in the PropsList. By default it print the Props keys/value pairs.
	*/

	public void handleRequest(Props p) throws Exception
	{
		println("====================================\n" + p.toString());
	}
}


