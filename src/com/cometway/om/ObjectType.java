
package com.cometway.om;


/**
* This class represents a type of object recognized by the Object Manager.
*/

public class ObjectType
{
	protected String type;


	/** Default constructor. */

	protected ObjectType()
	{
	}


	/** Constructor. */

	public ObjectType(String type)
	{
		if (type == null || (type.length() == 0))
		{
			throw new NullPointerException("Invalid ObjectType: type cannot be null or zero length.");
		}

		this.type = type;
	}


	/**
	* Returns true of the specified Object is equal to this one.
	*/

	public boolean equals(Object ob)
	{
		return (toString().equals(ob.toString()));
	}


	/**
	* Returns this type as a String.
	*/

	public String getType()
	{
		return (type);
	}


	/**
	* Returns this type as a String.
	*/

	public String toString()
	{
		return (type);
	}
}


