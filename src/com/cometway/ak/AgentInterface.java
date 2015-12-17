
package com.cometway.ak;

import com.cometway.props.Props;
import com.cometway.states.HasStateMachineModel;

/**
 * A standard interface for Agents.
 */

public interface AgentInterface extends HasStateMachineModel
{
	/* ----- agent property methods ----- */


	/**
	* Returns the Props assigned to this agent.
	*/

	public Props getProps();


	/**
	* Called by the agent kernel to assign a Props to this agent.
	*/

	public void setProps(Props p);


	/* ----- state machine methods ----- */


	/**
	* Assigns an AgentController to this agent.
	*/

	public void setAgentController(AgentControllerInterface agentController);


	/**
	* Initialize agent properties before it is started.
	*/

	public void initProps();


	/**
	* Called by agent kernel to initiate activities for this agent.
	*/

	public void start();


	/**
	* Called by agent kernel to send stop requests from the agent kernel.
	*/

	public void stop();


	/**
	* Called by agent kernel for special cleanup before an agent is destroyed.
	*/

	public void destroy();



	/* ----- agent output methods ----- */


	/**
	* Sends the debugging message to the agent's standard output.
	*/

	public void debug(String message);


	/**
	* Sends the error message to the agent's error output.
	*/

	public void error(String message);


	/**
	* Sends the error message and associated exception to the agent's error output.
	*/

	public void error(String message, Exception e);


	/**
	* Sends the message to the agent's standard output.
	*/

	public void println(String message);


	/**
	* Sends the warning message to the agent's standard output.
	*/

	public void warning(String message);


	/**
	* Sends the warning message and associated exception to the agent's error output.
	*/

	public void warning(String message, Exception e);


	/* ----- agent utility methods ----- */



	/**
	* Returns the string representation of this agent.
	*/

	public String toString();

}

