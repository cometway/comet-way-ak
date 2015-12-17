
package com.cometway.ak;


/**
* This exception is thrown by AgentController when the requested
* operation is illegal from its agent's current state.
*/

public class AgentStateException extends RuntimeException
{
	private Exception originalException;


	/**
	* Constructor for this exception.
	* @param message Message associated with this exception.
	*/

	public AgentStateException(String message)
	{
		super(message);
	}


	/**
	* Constructor for this exception.
	* @param message Message associated with this exception.
	* @param originalException The original exception associated with this exception.
	*/

	public AgentStateException(String message, Exception originalException)
	{
		super(message);

		this.originalException = originalException;
	}


	/**
	* Returns a reference to the orignal exception associated with this exception.
	*/

	public Exception getOriginalException()
	{
		return (originalException);
	}
}


