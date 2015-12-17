
package com.cometway.props;


/**
* A class can implement IPropsChangeListener when it wants
* to be notified about changes to a Props object.
*/

public interface IPropsChangeListener
{
	/**
	* Notifies this object that the Props have changed.
	* @param props	a reference to the Props object which has changed.
	* @param changedKeys	an array of Strings listing the property keys that have changed.
	*/

	public void propsChanged(Props props, String changedKeys[]);
}

