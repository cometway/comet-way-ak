
package com.cometway.ak;


import com.cometway.props.Props;


/**
* This agent calls its own run() method on its own Thread.
*/

public class RunnableAgent extends Agent implements Runnable
{
	protected Thread agentThread;

	/**
	* Initializes default properties for this agent:
	* agent_classname (default: none),  java class name that
	* the agent kernel will instantiate and start on
	* a Thread provided by this agent.
	*/

	public void initProps()
	{
		setDefault("agent_classname", "none");
		setDefault("agent_name", "none");
		setDefault("thread_sleep_ms", "none");
	}


	/**
	* Starts the Thread that will execute this agent's run() method.
	* Nothing will happen here if agent_classname is equals to "none".
	*/

	public void start()
	{
		if (getString("agent_classname").equals("none") == false)
		{
			startThread();	
		}
	}


	/**
	* Creates a Thread to execute this agent's run() method.
	*/

	protected void startThread()
	{
		if (agentThread != null)
		{
			error("Could not start thread because one has already been started.");
		}
		else
		{
			agentThread = new Thread(this, this.toString());
			agentThread.start();
		}
	}


	/**
	* This method is executed on a Thread when it is started.
	* 
	* If agent_classname is provided, a new agent of the specified class
	* is instantiated and started between Thread.sleep() cycles of
	* the duration in milliseconds specified by thread_sleep_ms.
	*/

	public void run()
	{
		try
		{
			do
			{
				String agent_classname = getString("agent_classname");
				String agent_name = getString("agent_name");

				/* Clones this agent's properties, set classname, and remove extraneous other properties. */

				Props p = new Props();

				p.copyFrom(this);
				p.removeProperty("agent_classname");
				p.removeProperty("agent_name");
				p.removeProperty("agent_id");
				p.removeProperty("started");
				p.removeProperty("thread_sleep_ms");

				p.setProperty("classname", agent_classname);
				p.setProperty("name", agent_name);


				/* Create and start the agent. */

				AgentControllerInterface agent = AK.getAgentKernel().createAgent(p);

				if (agent != null)
				{
					agent.start();

					if (getTrimmedString("thread_sleep_ms").equals("none"))
					{
						// We're done.

						break;
					}
					else
					{
						Thread.sleep(getInteger("thread_sleep_ms"));
					}
				}
				else
				{
					throw new RuntimeException("Could not start agent: agent_name (" + agent_classname + ')');
				}
			}
			while (currentStateEquals(RUNNING_STATE));
		}
		catch (Exception e)
		{
			error("Could not continue", e);
		}
	}
}


