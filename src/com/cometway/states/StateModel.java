
package com.cometway.states;


/**
* Implementation of the StateModelInterface.
*/

public class StateModel implements StateModelInterface
{
	private String name;
	private CommandInterface[] commands;
	private TransitionInterface[] transitions;


	/**
	* Constructor for a state model.
	* @param name name of this state.
	*/

	public StateModel(String name)
	{
		this.name = name;
	}


	/**
	* Constructor for a state model.
	* @param name name of this state.
	* @param command a command for this state.
	* @param transition a transition for this state.
	*/

	public StateModel(String name, CommandInterface command, TransitionInterface transition)
	{
		this.name = name;
		setCommand(command);
		setTransition(transition);
	}

	
	/**
	* Returns an array of CommandInterfaces for this state.
	*/

	public CommandInterface[] getCommands()
	{
		return (commands);
	}


	/**
	* Returns the name of this state.
	*/

	public String getName()
	{
		return (name);
	}


	/**
	* Returns an array of TransitionInterfaces for this state.
	*/

	public TransitionInterface[] getTransitions()
	{
		return (transitions);
	}


	/**
	* Sets the specified CommandInterface as the only command for this state.
	*/

	public void setCommand(CommandInterface command)
	{
		if (command != null)
		{
			commands = new CommandInterface[1];
			commands[0] = command;
		}
	}


	/**
	* Sets the specified array of CommandInterfaces as the commands for this state.
	*/

	public void setCommands(CommandInterface[] commands)
	{
		this.commands = commands;
	}


	/**
	* Sets the specified TransitionInterface as the only transition for this state.
	*/

	public void setTransition(TransitionInterface transition)
	{
		if (transition != null)
		{
			transitions = new TransitionInterface[1];
			transitions[0] = transition;
		}
	}


	/**
	* Sets the specified array of TransitionInterface as the transitions for this state.
	*/

	public void setTransitions(TransitionInterface[] transitions)
	{
		this.transitions = transitions;
	}
}


