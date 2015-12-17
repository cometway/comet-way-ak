
package com.cometway.ak;

import com.cometway.props.Props;
import com.cometway.props.PropsList;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;


/**
* This is the default Startup Agent for the Comet Way Agent Kernel. On startup, it
* scans the <TT>startup_dir</TT> for files ending in <TT>.startup</TT>,
* alphabetizes these filenames, and proceeds by loading a Props file
* for each, which it passes to the Agent Kernel to create and start
* an agent based on each set of Props.
*/

public class StartupAgent extends Agent implements FilenameFilter
{
	/**
	* Returns true if the passed file ends in <TT>.startup</TT>
	*/
	
	public boolean accept(File dir, String name)
	{
		return (name.endsWith(".startup"));
	}
	
	
	/**
	* Initializes the Props for this agent.
	*/

	public void initProps()
	{
		setDefault("startup_dir", ".");
	}


	/**
	* Initiates the startup process by loading agents in the startup directory.
	*/

	public void start()
	{
 		try
 		{
			Vector agentList = getAgentList();

 			startAgents(agentList);
 		}
		catch (Exception e)
		{
			error("Could not continue", e);
		}
	}



	/**
	* Returns a List containing Props of agents to instantiate and start.
	*/

	protected Vector getAgentList()
	{
		Vector agentList = new Vector();
		String fileSeparator = System.getProperty("file.separator");
		String startupDir = getString("startup_dir");
		File f = new File(startupDir);
	
		println("Loading agents from " + f);
	
		startupDir = f.toString();
	
		if (startupDir.endsWith(fileSeparator) == false)
		{
				startupDir += fileSeparator;
		}

		/* Get a list of .startup files from the startup directory */

		String s[] = f.list(this);


		/* Sort the filenames alphabetically using a swap sort algorithm */

		for (int start = 0; start < s.length - 1; start++)
		{
			int startID = 999999999;
			int startIndex = s[start].indexOf('_');
	
			if (startIndex > 0)
			{
				try
				{
					startID = Integer.parseInt(s[start].substring(0, startIndex));
				}
				catch (Exception e)
				{
					warning("Invalid agent_id prefix: " + s[start], e);
				}
			}

			for (int x = start + 1; x < s.length; x++)
			{ 
				int swapID = 999999999;
				int swapIndex = s[x].indexOf('_');
	
				if (swapIndex > 0)
				{
					try
					{
						swapID = Integer.parseInt(s[x].substring(0, swapIndex));
					}
					catch (Exception e)
					{
						warning("Invalid agent_id prefix: " + s[x], e);
					}
				}

				if (startID > swapID)
				{ 
					String temp = s[start];
					s[start] = s[x];
					s[x] = temp;
					startID = swapID;
				}
			}
		}


		// Load the Props for each startup file
		// and add it to the agent list.

		for (int i = 0; i < s.length; i++)
		{
			String str = startupDir + s[i];

			println("Starting " + str);

			Props agentProps = Props.loadProps(str);

			if (agentProps == null)
			{
				error("Could not load agent: " + str);
			}
			else
			{
				agentProps.setProperty("startup", "true");
				agentList.addElement(agentProps);
			}
		}

		return (agentList);
	}


	/**
	* Instantiates and starts agents using the specified
	* List of Props containing agent parameters.
	*/

	protected void startAgents(Vector agentList)
	{
		PropsList.sortPropsList(agentList, "agent_id");

		int count = agentList.size();

		for (int i = 0; i < count; i++)
		{
			Props agentProps = (Props) agentList.elementAt(i);
			String agentName = agentProps.getString("agent_id") + "_" + agentProps.getString("name");

			if (agentProps.getBoolean("startup"))
			{
				AgentControllerInterface agent = AK.getAgentKernel().createAgent(agentProps);
		
				if (agent == null)
				{
					error("Could not create agent: " + agentName);
				}
				else
				{
					agent.start();
				}
			}
			else
			{
				debug("Skipping: " + agentName);
			}
		}
	}
}

