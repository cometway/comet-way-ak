
package com.cometway.states;


/**
* This interface can be implemented by any class which needs to provide
* public access to its StateMachineModelInterface.
*/

public interface HasStateMachineModel
{
	/**
	* Called by AgentController to retrieve a reference to this agent's state machine model.
	*/

	public StateMachineModelInterface getStateMachineModel();
}


