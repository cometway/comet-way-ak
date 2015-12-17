
package com.cometway.om;

import com.cometway.ak.*;
import com.cometway.util.*;
import com.cometway.props.Props;

/**
 * A standard server interface to the agent kernel.
 */

public class AgentServer extends ServiceAgent implements AgentServerInterface
{
	protected IObjectManager om;
	protected AgentKernelInterface agentKernel;
	protected Props agentList;
	protected String agent_name_prefix;


	public void initProps()
	{
		setDefault("service_name", "agent_server");
		setDefault("om_service_name", "object_manager");
	}


	public void start()
	{
		agentKernel = AK.getAgentKernel();
		agentList = new Props();
		agent_name_prefix = getString("agent_name_prefix");

		String  serviceName = getString("om_service_name");

		if (serviceName.length() > 0)
		{
			om = (IObjectManager) getServiceImpl(serviceName);

			if (om == null)
			{
				error("Cannot locate the IObjectManager named \"" + serviceName + "\"");
			}
		}

		if (om == null)
		{
			throw new RuntimeException("Cannot continue without a valid IObjectManager");
		}

		println("Using " + om.getClass().getName() + " for agent storage");
		register();
	}


	public void stop()
	{
		unregister();

		om = null;
		agentKernel = null;
		agentList = null;
	}


	/**
	 * Creates an agent instance based on an Object Manager Props
	 * referenced by the specified ObjectID or String.
	 * The agent's initProps method is called before this method returns.
	 * @return a reference to an object identifying the created agent instance.
	 */

	public String createAgent(ObjectID agentPropsID)
	{
		if (agentPropsID == null)
		{
			throw new IllegalArgumentException("agentPropsID is null");
		}

		String agentName = null;
		Props agentProps = (Props) om.getObject(agentPropsID);

		// The classname property should have been set or the kernel will bitch
		// when we try to create a new agent.

		AgentControllerInterface agent = agentKernel.createAgent(agentProps);

		if (agent != null)
		{
			agentName = agent_name_prefix + agent.toString();

			agentList.setProperty(agentName, agent);

			println("Created new agent: " + agentName);
		}
		else
		{
			error("Could not instantiate agent: " + agentName);
		}

		return (agentName);
	}


	/** Returns the AgentController corresponding to the specified ObjectID or String. */

	private AgentController getAgentInstance(String agentName)
	{
		return ((AgentController) agentList.getProperty(agentName));
	}


	/**
	 * Calls the agent's destroy method, then destroys the agent instance.
	 */

	public boolean destroyAgent(String agentName)
	{
		boolean success = false;
		AgentControllerInterface agentInstance = getAgentInstance(agentName);

		if (agentInstance != null)
		{
			agentList.removeProperty(agentName);
			agentInstance.destroy();
			success = true;
		}

		return (success);
	}


	/**
	 * Starts the specified agent instance by calling agent's start method.
	 */

	public boolean startAgent(String agentName)
	{
		boolean success = false;
		AgentControllerInterface agentInstance = getAgentInstance(agentName);

		if (agentInstance != null)
		{
			try
			{
				agentInstance.start();
				success = true;
			}
			catch (Exception e)
			{
				error("Cannot start agent: " + agentName, e);
			}
		}
		else
		{
			error("Cannot start agent: " + agentName);
		}

		return (success);
	}


	/**
	 * Signals the specified agent to stop by calling the agent's stop method.
	 */

	public boolean stopAgent(String agentName)
	{
		boolean success = false;
		AgentControllerInterface agentInstance = getAgentInstance(agentName);

		if (agentInstance != null)
		{
			agentInstance.stop();
			success = true;
		}
		else
		{
			error("Cannot start agent: " + agentName);
		}

		return (success);
	}
}

