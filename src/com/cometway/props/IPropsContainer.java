
package com.cometway.props;


import java.util.Enumeration;


/**
* A class can implement IPropsContainer when it wants
* to implement a properties storage mechanism.
*/

public interface IPropsContainer
{
	/**
	* Returns a reference to a named object.
	* @param key the name of the object to retrieve.
	* @return an object reference.
	*/

	public Object getProperty(String key);


	/**
	* Removes a named object from the container.
	* @param key the name of the object to remove.
	* @return true if the object existed and was removed; false otherwise.
	*/

	public boolean removeProperty(String key);


	/**
	* Adds or changes a named object to the one specified.
	* @param key the name of the object to add or change.
	* @param value a reference to the new object.
	*/

	public void setProperty(String key, Object value);


	/**
	* copy the properties in the current container to another container
	* @param ipc the container to be copied into.  
	*/

	public void copy(IPropsContainer ipc);


	/**
	* list the properties in the current container
	* @return an Enumeration of keys
	*/
	
	public Enumeration enumerateProps();
}

