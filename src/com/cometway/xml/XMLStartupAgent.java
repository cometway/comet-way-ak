
package com.cometway.xml;

import com.cometway.ak.AK;
import com.cometway.ak.AgentControllerInterface;
import com.cometway.ak.StartupAgent;
import com.cometway.props.Props;
import com.cometway.props.PropsException;
import java.io.FileNotFoundException;
import java.util.Vector;


/**
* Loads and starts agents from their XML Props list read from the
* file specified by the <TT>startup_file</TT> property of this agent.
*/

public class XMLStartupAgent extends StartupAgent
{
	/**
	* Agents are started from the <TT>ak.xstartup</TT> file by default.
	*/

	public void initProps()
	{
		setDefault("startup_dir", ".");
		setDefault("startup_file", "ak.xstartup");
	}


	/**
	* Initiates the startup process by loading agents in the startup directory.
	*/

	public void start()
	{
		try
		{
			Vector agentList = null;

			try
			{
				String filename = getString("startup_file");
		
				println("Loading agents from " + filename);
		
				agentList = XMLPropsList.loadFromFile(filename);
			}
			catch (Exception e)
			{
				if (e instanceof PropsException)
				{
					Exception x = ((PropsException) e).getOriginalException();

					if (x instanceof java.io.FileNotFoundException)
					{
						// fail quietly
					}
					else
					{
						error("Error Parsing XML", x);
					}
				}
				else
				{
					error("Error Parsing XML", e);
				}

				agentList = getAgentList();
			}

			startAgents(agentList);
		}
		catch (Exception e)
		{
			error("Could not continue", e);
		}
	}
}


