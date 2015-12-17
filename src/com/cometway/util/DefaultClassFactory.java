
package com.cometway.util;


/**
* This default implementation of ClassFactoryInterface uses Class.forName() and
* Class.newInstance() to resolve and instantiate the specified class.
* Replace this class to handle ClassLoader limitations in your favorite environment.
*/

public class DefaultClassFactory implements ClassFactoryInterface
{
	/**
	* Creates a default instance of an object of the specified class.
	* @throws ClassFactoryException If there is a problem.
	*/

	public Object createInstance(String classname) throws ClassFactoryException
	{
		Object o = null;

		try
		{
			Class newClass = Class.forName(classname);
	
			o = newClass.newInstance();
		}
		catch (Exception e)
		{
			throw new ClassFactoryException("Could not create class: " + classname, e);
		}

		return (o);
	}
}


