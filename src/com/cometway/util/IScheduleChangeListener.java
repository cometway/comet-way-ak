
package com.cometway.util;

import java.lang.*;
import java.util.*;


/**
 * A class can implement IScheduleChangeListener when it wants
* to be notified about changes to the schedule of an IScheduleable object.
 */

public interface IScheduleChangeListener
{


/**
 * Notifies this object that the Schedule has changed.
	 *
	 * @param schedule	a reference to the IScheduleable object whose schedule has changed.
 */

	public void scheduleChanged(ISchedulable schedule);
}

