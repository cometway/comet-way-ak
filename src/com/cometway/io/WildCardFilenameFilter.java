
package com.cometway.io;

import java.io.File;
import java.io.FilenameFilter;


/**
* This class is an implementation of java.io.FilenameFilter. This
* FilenameFilter filters filenames using a wildcard match. 
* Currently, only one wildcard can be used.
*/

public class WildCardFilenameFilter implements FilenameFilter
{
	String expression1;
	String expression2;


	/**
	* Creates a new WildCardFilenameFilter which filters out
	* filenames that do not match the String expression parameter.
	* The expression must contain the absolute path. The '*' symbol
	* is used as the wildcard. The wildcard can only appear at the end
	* of the absolute path. For example '/usr/home/java/Test*.java' is
	* valid whereas '/usr/home/java*_dir/Test.class' is not valid. 
	* Currently, only one wildcard per expression is supported.
	* @param expression This is the absolute path to wildcard match.
	* If the expression contains no wildcards, an exact match will be made.
	* If the expression is empty, all matches are accepted.
	*/

	public WildCardFilenameFilter(String expression)
	{
		if (expression.length() > 0)
		{
			if (expression.charAt(expression.length() - 1) == '/')
			{
				expression.substring(0, expression.length() - 1);
			}
		}

		if (expression.indexOf("*") != -1)
		{
			int star = expression.indexOf("*");

			expression1 = expression.substring(0, star);
			expression2 = expression.substring(star + 1);
		}
		else
		{
			expression1 = expression;
			expression2 = "";
		}
	}


	/**
	* Implements java.io.FilenameFilter.accept(File,String).
	*/

	public boolean accept(File dir, String name)
	{
		boolean p1 = false;
		boolean p2 = false;
		String match = (new File(dir, name)).getAbsolutePath();

		if (expression1.length() > 0)
		{
			if (match.indexOf(expression1) == 0)
			{
				p1 = true;
			}
		}
		else
		{
			p1 = true;
		}

		if (expression2.length() > 0)
		{
			if (match.indexOf(expression2) == (match.length() - expression2.length()))
			{
				p2 = true;
			}
		}
		else
		{
			p2 = true;
		}

		return (p1 && p2);
	}
}

