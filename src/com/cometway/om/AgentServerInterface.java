
package com.cometway.om;

import com.cometway.ak.*;
import com.cometway.props.Props;


/**
 * A standard server interface for managing agents in a agent kernel
 * using an object manager for the agent Props storage.
 */

public interface AgentServerInterface
{
	/**
	 * Creates an agent instance using the Props information referenced
	 * by the specified ObjectID. If the ObjectID refers to a valid
	 * Props object, and the agent kernel was able to instantiate a
	 * agent instance based on the information it contains, a String
	 * reference will be returned containing the agentName; it returns
	 * null otherwise. The agentName should be unique among agents
	 * referenced by the agent kernel.
	 */

	public String createAgent(ObjectID agentPropsID);


	/**
	 * Calls the agent's destroy method, then destroys the agent instance.
	 * @return true if successful; false otherwise.
	 */

	public boolean destroyAgent(String agentName);


	/**
	 * Starts the specified agent instance by calling agent's start method.
	 * @return true if successful; false otherwise.
	 */

	public boolean startAgent(String agentName);


	/**
	 * Signals the specified agent to stop by calling the agent's stop method.
	 * @return true if successful; false otherwise.
	 */

	public boolean stopAgent(String agentName);
}



