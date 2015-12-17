
package com.cometway.util;


import java.lang.ClassNotFoundException;


/**
* This interface describes a method for creating new class instances.
*/

public interface ClassFactoryInterface
{
	/**
	* Creates a default instance of an object of the specified class.
        * @throws ClassFactoryException If there is a problem.
	*/

	public Object createInstance(String classname) throws ClassFactoryException;
}


