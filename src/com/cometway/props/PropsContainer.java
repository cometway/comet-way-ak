
package com.cometway.props;


import java.util.Enumeration;
import java.util.Hashtable;


/**
* A Props class can reference this generic PropsContainer for simple
* transient storage of properties. This class can also be extended to
* provide support for persistence and caching.
*/

public class PropsContainer extends AbstractPropsContainer
{
	private Hashtable props = new Hashtable(5);


	/**
	* Returns a reference to a named object in the container.
	* @param key the name of the object to retrieve.
	* @return an object reference.
	*/

	public Object getProperty(String key)
	{
		Object o = null;

		// Colons are used as a PropsContainer hierarchy delimeter.
		// Assume that the first part of the key prior to a colon is the
		// key to a child instance of IPropsContainer. Access the child
		// IPropsContainer with the remainder of the key.

		int index = key.indexOf(':');

		if (index >= 0)
		{
			Object node = props.get(key.substring(0, index));

			if (node instanceof IPropsContainer)
			{
				IPropsContainer c = (IPropsContainer) node;
				o = c.getProperty(key.substring(index + 1));
			}

			// If the IPropsContainer node was not found using the part
			// of the key before the colon, retrieve the value using the
			// entire key, but only if the node was null.

			else if (node == null)
			{
				o = props.get(key);
			}
		}

		// No colon was found in the key.
		// Retrieve the value using the entire key.

		else
		{
			o = props.get(key);
		}

		return (o);
	}


	/**
	* Removes a named object from the container.
	* @param key the name of the object to remove.
	* @return true if the object existed and was removed; false otherwise.
	*/

	public boolean removeProperty(String key)
	{
		boolean result = false;
		int index = key.indexOf(':');

		// Colons are used as a PropsContainer hierarchy delimeter.
		// Assume that the first part of the key prior to a colon is the
		// key to a child instance of IPropsContainer. Access the child
		// IPropsContainer with the remainder of the key.

		if (index >= 0)
		{
			Object node = props.get(key.substring(0, index));

			if (node instanceof IPropsContainer)
			{
				IPropsContainer c = (IPropsContainer) node;

				result = c.removeProperty(key.substring(index + 1));
			}

			// If the IPropsContainer node was not found using the part
			// of the key before the colon, remove the value using the
			// entire key, but only if the node was null.

			else if (node == null)
			{
				result = (props.remove(key) != null);
			}
		}

		// No colon was found in the key.
		// Remove the value using the entire key.

		else
		{
			result = (props.remove(key) != null);
		}

		return (result);
	}


	/**
	* Adds a named object to the container.
	* If the value is null, the property will be removed.
	* @param key the name of the obejct to add.
	* @param value the object to add.
	*/

	public void setProperty(String key, Object value)
	{
		boolean result = false;
		int index = key.indexOf(':');

		// Colons are used as a PropsContainer hierarchy delimeter.
		// Assume that the first part of the key prior to a colon is the
		// key to a child instance of IPropsContainer. Access the child
		// IPropsContainer with the remainder of the key.

		if (index >= 0)
		{
			Object node = props.get(key.substring(0, index));

			if (node instanceof IPropsContainer)
			{
				IPropsContainer c = (IPropsContainer) node;

				c.setProperty(key.substring(index + 1), value);
			}

			// If the IPropsContainer node was not found using the part
			// of the key before the colon, set or remove the value using the
			// entire key, but only if the node was null.

			else if (node == null)
			{
				if (value == null)
				{
					props.remove(key);
				}
				else
				{
					props.put(key, value);
				}
			}
		}
		else
		{
			// No colon was found in the key.
			// Set or remove the value using the entire key.

			if (value == null)
			{
				props.remove(key);
			}
			else
			{
				props.put(key, value);
			}
		}
	}


	/**
	* Copys the contents of this container to another one.
	* @param ipc the destination container
	*/

	public void copy(IPropsContainer ipc)
	{
		String s;
		Enumeration e = props.keys();

		while (e.hasMoreElements())
		{
			s = e.nextElement().toString();

			ipc.setProperty(s, props.get(s));
		}
	}


	/**
	* Returns an enumeration of the keys for this container.
	* @return an enumeration of keys as Strings.
	*/

	public Enumeration enumerateProps()
	{
		return (props.keys());
	}
}


