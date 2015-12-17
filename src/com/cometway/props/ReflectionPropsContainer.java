
package com.cometway.props;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Vector;


/**
* Provides a reflection-enabled Props container for access to the public fields of an Object.
*/

public class ReflectionPropsContainer extends AbstractPropsContainer
{
	/** Reference to the Object used for reflection. */

	private Object objectRef;


	/**
	* Creates an instance using itself as the reflected Object.
	* This is used when extending this class.
	*/

	public ReflectionPropsContainer()
	{
		objectRef = this;
	}


	/**
	* Created an instance using the specified Object for reflection.
	* This is used to reflect Objects that do not extend this class.
	*/

	public ReflectionPropsContainer(Object objectRef)
	{
		this.objectRef = objectRef;
	}


	/** Returns a the reflected Object. */

	public Object getObjectRef()
	{
		return (objectRef);
	}


	/** Sets the reflected Object. */

	public void setObjectRef(Object objectRef)
	{
		this.objectRef = objectRef;
	}
	

	// Methods from IPropsContainer


	/**
	* Returns the specified public field of the reflected object.
	*/

	public Object getProperty(String key)
	{
		Object o = null;

		try
		{
			Class c = objectRef.getClass();
			Field f = c.getField(key);

			o = f.get(objectRef);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Could not access reflected property \"" + key + "\"", e);
		}

		return (o);
	}


	/** Properties of a reflected object cannot be removed; returns false. */

	public boolean removeProperty(String key)
	{
		return (false);
	}


	/** Sets the specified public field of the reflected Object. */

	public void setProperty(String key, Object value)
	{
		try
		{
			Class c = objectRef.getClass();
			Field f = c.getField(key);

			f.set(objectRef, value);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Could not set reflected property \"" + key + "\" = \"" + value + "\"", e);
		}
	}


	/** Copies the public fields from the reflected Object to the specified Props. */

	public void copy(IPropsContainer ipc)
	{
		String key = null;
		Object value = null;

		try
		{
			Class c = objectRef.getClass();
			Field fields[] = c.getFields();

			if (fields != null)
			{
				for (int i = 0; i < fields.length; i++)
				{
					Field f = fields[i];
					key = f.getName();
					value = ipc.getProperty(key);

					f.set(objectRef, value);
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Could not copy reflected property \"" + key + "\" = \"" + value + "\"", e);
		}
	}


	/** Returns an Enumeration of public fields from the reflected Object. */

	public Enumeration enumerateProps()
	{
		Vector v = new Vector();

		try
		{
			Class c = objectRef.getClass();
			Field fields[] = c.getFields();

			if (fields != null)
			{
				for (int i = 0; i < fields.length; i++)
				{
					v.addElement(fields[i].getName());
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Could not enumerate", e);
		}

		return (v.elements());
	}
}



