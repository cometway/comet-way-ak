
package com.cometway.states;


/**
* This interface defines methods necessary to retrieve states
* from a model of a state machine.
*/

public interface StateMachineModelInterface
{
	/**
	* Returns a reference to the specified StateModelInterface in this state machine model;
	* null if the specified state is invalid.
	*/

	public StateModelInterface getStateModel(String stateName);


	/**
	* Returns the name of this state machine model.
	*/

	public String getName();
}


