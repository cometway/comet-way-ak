

package com.cometway.states;


/**
* This interface defines methods necessary to test for a state transition.
*/

public interface TransitionInterface
{
	/**
	* Tests the transition for the specified object returning
	* the name of the new State if the transition was successful;
	* null otherwise.
	* @throws TransitionException if there is a problem executing the transition.
	*/

	public String execute() throws TransitionException;


	/**
	* Returns the name of this transition.
	*/

	public String getName();
}


