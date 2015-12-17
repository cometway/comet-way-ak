
package com.cometway.ak;


/**
* A standard interface for inter-agent communication.
*/

public interface MessageAgentInterface
{
	/**
	* Respond to the specified request as appropriate.
	*/

	public void handleMessage(AgentMessage message);
}


