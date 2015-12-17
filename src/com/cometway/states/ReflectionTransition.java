

package com.cometway.states;


import java.lang.reflect.Method;


/**
* This implementation of TransitionInterface uses the Java Reflection API
* to execute a specific method on the transition object. The transition object's
* method an Object parameter, and returns the name of the new State if the
* translation was successful; false otherwise.
*/

public class ReflectionTransition implements TransitionInterface
{
	private final static Class[] METHOD_PARAMS = { Object.class };

	private String name;
	private Object objRef;
	private String methodName;


	/**
	* Constructor for this transition.
	* @param name Name of this transition.
	* @param methodName Name of the method to call on the transition object to execute the transition.
	*/

	public ReflectionTransition(String name, Object objRef, String methodName)
	{
		this.name = name;
		this.objRef = objRef;
		this.methodName = methodName;
	}


	/**
	* Executes the transition for the specified object returning
	* the name of the new State if the transition was successful;
	* null otherwise. This implementation uses the Java Reflection API
	* to make a method call on the specified object to execute the transition.
	*/

	public String execute() throws TransitionException
	{
		String newState = null;

		if (objRef != null)
		{
			Class oc = objRef.getClass();

			try
			{
				Method m = oc.getMethod(methodName, METHOD_PARAMS);
				Object[] args = new Object[1];
				args[0] = objRef;
				newState = (String) m.invoke(objRef, args);
			}
			catch (Exception e)
			{
				String message = "executeTransition failed: "+ name + '(' + oc.getName() + '.' + methodName + ')';
				throw new TransitionException(message, e);
			}
		}
		else
		{
			throw new TransitionException("null transition object: " + name + '(' + methodName + ')');
		}

		return (newState);
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
		return ("ReflectionTransition: " + name + "->" + methodName);
	}
}


