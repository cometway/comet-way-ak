
package com.cometway.ak;

/**
 * A standard interface for inter-agent communication.
 */

public interface RequestAgentInterface
{


	/**
	 * Respond to the specified request as appropriate.
	 */

	public void handleRequest(AgentRequest request);
}

