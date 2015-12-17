

package com.cometway.states;


/**
* This interface defines methods necessary to execute a state command.
*/

public interface CommandInterface
{
	/**
	* Executes this command.
	* @throws CommandException There was a problem executing the command.
	*/

	public void execute() throws CommandException;


	/**
	* Returns the name of this command.
	*/

	public String getName();
}


