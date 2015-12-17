

package com.cometway.states;


import java.lang.reflect.Method;


/**
* This implementation of CommandInterface using the reflection API
* to invoke a public method of any Object.
*/

public class ReflectionCommand implements CommandInterface
{
	private final static Class[] METHOD_PARAMS = { Object.class };

	private String name;
	private Object objRef;
	private String methodName;


	/**
	* Constructor for this class.
	* @param name the name of this command.
	* @param objRef a reference to the Object whose method will be invoked
	* @param methodName name of the method that will be invoked (method must not have any parameters)
	*/

	public ReflectionCommand(String name, Object objRef, String methodName)
	{
		this.name = name;
		this.objRef = objRef;
		this.methodName = methodName;
	}


	/**
	* Executes this command.
	*/

	public void execute() throws CommandException
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
				m.invoke(objRef, args);
			}
			catch (Exception e)
			{
				String message = "executeCommand failed: "+ name + '(' + oc.getName() + '.' + methodName + ')';
				throw new CommandException(message, e);
			}
		}
		else
		{
			throw new CommandException("null command object: " + name + '(' + methodName + ')');
		}
	}


	/**
	* Returns the name of this command.
	*/

	public String getName()
	{
		return (name);
	}


	/**
	* Returns the String representation of this command.
	*/

	public String toString()
	{
		return ("ReflectionCommand: " + name + "->" + methodName);
	}
}


