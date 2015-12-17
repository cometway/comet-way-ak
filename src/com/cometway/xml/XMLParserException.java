
package com.cometway.xml;


/**
* This exception is thrown by XMLParser when there is a problem.
*/

public class XMLParserException extends Exception
{
	private Exception originalException;


	/**
	* Constructor used when there is a Parsing problem.
	*/

	public XMLParserException(String message)
	{
		super(message);
	}

	/**
	* Constructor to use when the XMLParserException was caused by another exception.
	*/

	public XMLParserException(String message, Exception originalException)
	{
		super(message);

		this.originalException = originalException;
	}


	/**
	* Returns a reference to the exception that caused this one.
	*/ 

	public Exception getOriginalException()
	{
		return (originalException);
	}
}


