
package com.cometway.util;


/**
* This is useful when you need to notify another object of an exception
* which has taken place. Any class which supports this interface should
* have a method:
* <BR>
* <CODE>public void setExceptionHandler(IExceptionHandler h);</CODE>
*/

public interface IExceptionHandler
{


	/**
	* This method arbitrarily handles the condition indicated by the passed
	* parameters. Ideally, it would invoke some kind of error reporting or
	* recovery technique based on the exception type and significance of
	* the other parameters. A boolean value may be returned to indicate
	* whether or not some action was taken as a result of the exception.
	*/

	public boolean handleException(Exception e, Object o, String message);
}

