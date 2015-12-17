

package com.cometway.states;


import java.lang.reflect.Method;


/**
* This is an implementation of TransitionInterface that will always
* return the state specified in the constructor.
*/

public class AutoTransition implements TransitionInterface
{
	private String name;
	private String state;


	/**
	* Constructor for this transition.
	* @param state Name of the state to return when execute is called.
	*/

	public AutoTransition(String name, String state)
	{
		this.name = name;
		this.state = state;
	}


	/**
	* Always returns the state when called.
	*/

	public String execute() throws TransitionException
	{
		return (state);
	}


	/**
	* Returns the name of this transition.
	*/

	public String getName()
	{
		return (name);
	}


	/**
	* Returns a String representative of this transition.
	*/

	public String toString()
	{
		return ("AutoTransition: " + name + "->" + state);
	}
}


