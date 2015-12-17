
package com.cometway.util;


/**
* A class can implement ISchedulable when it wants
* to be scheduled for execution by a Scheduler.
*/

public interface ISchedulable
{


/**
* Adds an IScheduleChangeListener to the list of listeners for this object.
* @param l a reference to an IScheduleChangeListener.
* @return true if the listener already exists.
 */

	public boolean addScheduleChangeListener(IScheduleChangeListener l);


	/**
	* Returns a schedule for this object.
	* @return a reference to an ISchedule.
	*/

	public ISchedule getSchedule();


	/**
	* Removes an IScheduleChangeListener from the list of listeners for this object.
	* @param l a reference to an IScheduleChangeListener.
	* @return true if the listener was removed; false otherwise.
	*/

	public boolean removeScheduleChangeListener(IScheduleChangeListener l);


	/**
	* Called when the wakeup timer expires.
	*/

	public void wakeup();
}

;
