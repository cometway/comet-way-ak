
package com.cometway.states;


import com.cometway.props.Props;


/**
* Implementation of the StateMachineModelInterface.
*/

public class StateMachineModel implements StateMachineModelInterface
{
	private String name;
	private Props states;


	/**
	* Constructor for this state machine model.
	* @param name name of this state machine model.
	*/

	public StateMachineModel(String name)
	{
		this.name = name;
		this.states = new Props();
	}


	/**
	* Adds the specified state model to this state machine model.
	*/

	public void addStateModel(StateModelInterface stateModel)
	{
		states.setProperty(stateModel.getName(), stateModel);
	}


	/**
	* Returns a reference to the specified StateModelInterface in this state machine model;
	* null if the specified state is invalid.
	*/

	public StateModelInterface getStateModel(String stateName)
	{
		return ((StateModelInterface) states.getProperty(stateName));
	}


	/**
	* Returns the name of this state machine model.
	*/

	public String getName()
	{
		return (name);
	}
}


