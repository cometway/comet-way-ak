
package com.cometway.text;


/**
 * Represents a range of text returned by the ITextFinder interface.
 */

public class TextFinderResult
{
	private int     start;
	private int     end;


	/**
	 * Both starting and ending indexed are set to 0 (zero) by default.
	 */

	public TextFinderResult() {}


	/**
	 * @param start the starting index of the text result.
	* @param end the ending index of the text result.
	 */


	public TextFinderResult(int start, int end)
	{
		this.start = start;
		this.end = end;
	}


	/**
	 * Returns the starting index of the text result.
	 */


	public int getStart()
	{
		return (start);
	}


	/**
	 * Returns the ending index of the text result.
	 */


	public int getEnd()
	{
		return (end);
	}


	/**
	 * Sets the starting index of the text result.
	* @param start the starting index of the text result.
	 */


	public void setStart(int start)
	{
		this.start = start;
	}


	/**
	 * Sets the ending index of the text result.
	* @param end the ending index of the text result.
	 */


	public void setEnd(int end)
	{
		this.end = end;
	}


	/**
	 * Returns the text result as a String.
	 */


	public String toString()
	{
		return ("(" + start + ", " + end + ")");
	}


}

