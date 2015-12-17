
package com.cometway.io;

import java.io.File;
import java.io.FilenameFilter;

/**
* This class is an implementation of java.io.FilenameFilter using
* the JDK regular expression package to either include or exclude
* all files matching the expression.
*/

public class RegExpFilenameFilter implements FilenameFilter
{
	protected String regexp;
	protected boolean exclude;


	/**
	* Creates a FilenameFilter that accepts files matching
	* the specified regular expression.
	* @param regexp is the regular expression matched against filenames.
	*/

	public RegExpFilenameFilter(String regexp)
	{
		this.regexp = regexp;
		this.exclude = false;
	}


	/**
	* Creates a FilenameFilter that filters files based on
	* the specified regular expression.
	* @param regexp is the regular expression matched against filenames.
	* @param exclude if true matching files will not be accepted;
	* if false matching files will be accepted.
	*/

	public RegExpFilenameFilter(String regexp, boolean exclude)
	{
		this.regexp = regexp;
		this.exclude = exclude;
	}


	/**
	* Returns true if the file should be included in a file list.
	* If exclude is false, returns true if name matches the regular expression.
	* If exclude is true, returns false if name matches the regular expression.
	* This switchable behavior allows for two different filtering models.
	*/

	public boolean accept(File dir, String name)
	{
		boolean result = name.matches(regexp);

		if (exclude) result = !result;

		return (result);
	}
}


