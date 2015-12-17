
package com.cometway.ak;


/**
 * A standard agent kernel interface.
 */

public interface AgentKernelInterface
{
	/**
	 * Creates an agent instance based on implementation-specific
	 * agent information; typically a String containing the classname
	 * of an AgentInterface implementation, or a Props with the
	 * classname property. The agent's initProps method is called
	 * before this method returns.
	 */

	public AgentControllerInterface createAgent(Object agentInfo);
}

