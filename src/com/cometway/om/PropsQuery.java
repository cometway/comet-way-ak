
package com.cometway.om;


/**
 * This is an ObjectQuery which queries Props objects. This object is used
 * to list objects from an IObjectManager through the listObjects() method.
 * The IObjectManager must support PropsTypes.
 */
public class PropsQuery
{
	/** This is the name of the PropsType which to query. */
	public String typeName;
	/** This is the name of the parameter key to query. */
	public String key;
	/** This is the parameter value the parameter key is to match. */
	public Object value;

	/**
	 * Creates a PropsQuery which when given to an IObjectManager's listObjects()
	 * method, a list of ObjectID's returned will be of the given PropsType
	 * name and a parameter with the given name and mapped value.
	 * @param type This is the name of the PropsType.
	 * @param key This is the parameter name to look for.
	 * @param value This is the value of the parameter whose key was given.
	 */
	public PropsQuery(String type, String key, Object value)
	{
		this.typeName	= PropsType.TYPE_STR + "_" + type;
		this.key			= key;
		this.value		= value;
	}

	/**
	 * Creates a PropsQuery which when given to an IObjectManager's listObjects()
	 * method, a list of ObjectID's returned will be of the given PropsType
	 * name and a parameter with the given name and mapped value.
	 * @param type This is the PropsType.
	 * @param key This is the parameter name to look for.
	 * @param value This is the value of the parameter whose key was given.
	 */
	public PropsQuery(PropsType type, String key, Object value)
	{
		this.typeName	= type.getType();
		this.key			= key;
		this.value		= value;
	}


	/**
	 * Overrides Object.toString()
	 */
	public String toString()
	{
		return ("PropsQuery{" + typeName + ", " + key + ", " + value + "}");
	}
}


