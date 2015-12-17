
package com.cometway.props;


import java.util.Enumeration;


/**
* Use this class to allow read only access to a IPropsContainer.
*/

public class ReadOnlyPropsContainer implements IPropsContainer
{
	private IPropsContainer container;

	public ReadOnlyPropsContainer(IPropsContainer container)
	{
		if (container == null)
		{
			throw new NullPointerException("container cannot be null");
		}

		this.container = container;
	}


	public void copy(IPropsContainer ipc)
	{
		container.copy(ipc);
	}


	public Enumeration enumerateProps()
	{
		return (container.enumerateProps());
	}


	public Object getProperty(String key)
	{
		return (container.getProperty(key));
	}


	/**
	* Throws a RuntimeException when called: This operation is illegal.
	*/

	public boolean removeProperty(String key)
	{
		throw new RuntimeException("The property \"" + key + "\" cannot be removed.");
	}


	/**
	* Throws a RuntimeException when called: This operation is illegal.
	*/

	public void setProperty(String key, Object value)
	{
		throw new RuntimeException("The property \"" + key + "\" cannot be changed.");
	}
}

