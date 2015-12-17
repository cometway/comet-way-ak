
package com.cometway.ak;


import com.cometway.states.*;


/**
* This is a com.cometway.states.CommandInterface implementation
* for standard AgentInterface agents with initProps,
* start, stop, and destroy methods. No other command names
* are supported.
*/

public class AgentCommand implements CommandInterface 
{
	private AgentInterface agent;
	private String name;


	/**
	* Constructor for this class.
	* @param agent the recipient of the method call
	* @param name of the command (method) to execute
	*/

	public AgentCommand(AgentInterface agent, String name)
	{
		this.agent = agent;
		this.name = name;
	}


	/**
	* Returns the name of this command.
	*/

	public String getName()
	{
		return (agent + "." + name);
	}


	/**
	* Executes the command on the agent.
	* @throws CommandException if there was a problem.
	*/

	public void execute() throws CommandException
	{
//		agent.debug("--> Executing " + getName());

		try
		{
			if (name.equals("initProps"))
			{
				agent.initProps();
			}
			else if (name.equals("start"))
			{
				agent.start();
			}
			else if (name.equals("stop"))
			{
				agent.stop();
			}
			else if (name.equals("destroy"))
			{
				agent.destroy();
			}
			else
			{
				throw new CommandException("Unrecognized command: " + name);
			}
		}
		catch (Exception e)
		{
			throw new CommandException("Could not " + name, e);
		}
	}
}


