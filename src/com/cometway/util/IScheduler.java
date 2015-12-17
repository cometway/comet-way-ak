
package com.cometway.util;

import java.lang.*;
import java.util.*;


/**
 * A class can implement IScheduler when it wants
* manage objects implementing the ISchedulable interface.
*
* @see ISchedulable
 */

public interface IScheduler
{


/**
 * Adds a ISchedulable item to the Scheduler.
	*
	* @param s	an object which implements the ISchedulable interface.
	* @return	true if the object was added; false otherwise.
	* @see		ISchedulable
 */

	public boolean schedule(ISchedulable s);


	/**
	 * Removes a ISchedulable item from the Scheduler.
	*
	* @param s	an object which implements the ISchedulable interface.
	* @return	true if the object was removed; false if the object was invalid.
	* @see		ISchedulable
	 */

	public boolean unschedule(ISchedulable s);
}

