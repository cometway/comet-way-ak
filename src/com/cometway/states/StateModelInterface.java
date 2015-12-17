
package com.cometway.states;


/**
* This interface defines methods necessary to retrieve information
* from a model of a state.
*/

public interface StateModelInterface
{
	/**
	* Returns an array of CommandInterfaces for this state.
	*/

	public CommandInterface[] getCommands();


	/**
	* Returns the name of this state.
	*/

	public String getName();


	/**
	* Returns an array of TransitionInterfaces for this state.
	*/

	public TransitionInterface[] getTransitions();
}

