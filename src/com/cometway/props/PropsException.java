
package com.cometway.props;


/**
* This exception is thrown by com.cometway.props classes when there is a problem.
*/

public class PropsException extends Exception
{
	private Exception originalException;


	/**
	* Constructor for this exception.
	* @param message Message associated with this exception.
	*/

	public PropsException(String message)
	{
		super(message);
	}


	/**
	* Constructor for this exception.
	* @param message Message associated with this exception.
	* @param originalException The original exception associated with this exception.
	*/

	public PropsException(String message, Exception originalException)
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


