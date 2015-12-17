
package com.cometway.email;


/**
* This interface describes functionality for reporting bugs.
*/

public interface BugReportInterface
{
	/**
	* Submits a bug report from the specified author (or entity) using the description.
	*/

	public void submitBugReport(String author, String description);
}

