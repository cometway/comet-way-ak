
package com.cometway.util;


/**
* This exception is thrown by ClassFactoryInterface.createInstance when there is a problem.
*/

public class ClassFactoryException extends Exception
{
	private Exception originalException;


	/**
	* Constructor for this exception.
	* @param message Message associated with this exception.
	*/

	public ClassFactoryException(String message)
	{
		super(message);
	}


	/**
	* Constructor for this exception.
	* @param message Message associated with this exception.
	* @param originalException The original exception associated with this exception.
	*/

	public ClassFactoryException(String message, Exception originalException)
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


