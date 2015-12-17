
package com.cometway.ak;

import com.cometway.props.Props;


/**
* Used to create multiple instances of the same kind of agent.
* agent_classname specifies the classname of the agent to start;
* number_of_agents specifies the number of agent_classname to start.
*/

public class AgentReplicator extends Agent
{
	public void start()
	{
		int count = getInteger("number_of_agents");
		String classname = getString("agent_classname");

		for (int i = 0; i < count; i++)
		{
			/* Clones this agent's properties, set classname, and remove extraneous other properties. */

			Props p = new Props();

			p.copyFrom(this);
			p.setProperty("classname", classname);
			p.removeProperty("agent_classname");
			p.removeProperty("agent_id");
			p.removeProperty("name");
			p.removeProperty("number_of_agents");
			p.removeProperty("started");


			/* Create and start the agent */

			AgentControllerInterface agent = AK.getAgentKernel().createAgent(p);

			if (agent != null)
			{
				agent.start();
			}
			else
			{
				error("Couldn't start agent " + i + "/" + count + " " + classname);
			}
		}
	}
}

