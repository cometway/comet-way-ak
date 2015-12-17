
package com.cometway.om;


/**
 * This is an IObjectManager type. An ObjectManager that supports this type can be used
 * to create and manage Props objects. This Object is used to create new objects in an
 * ObjectManager and fetch them by helping create the ObjectID.
 */
public class PropsType extends ObjectType
{
	/** This is the String name prefix for the PropsType ObjectType. */
	public final static String TYPE_STR = "props";


	protected PropsType()
	{
	}

	/**
	 * Creates a PropsType ObjectType whose name is to be the String parameter.
	 * @param type This is the name of this PropsType.
	 */
	public PropsType(String type)
	{
		if (type == null || (type.length() == 0))
		{
			throw new NullPointerException("Invalid PropsType: type cannot be null or zero length.");
		}

		this.type = TYPE_STR + '_' + type;
	}
}



