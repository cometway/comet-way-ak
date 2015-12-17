
package com.cometway.text;


/**
 * A text finder which uses generic search algorithms.
 */

public class TextFinder implements ITextFinder
{
	private String  searchString;
	private boolean caseInsensitive;
	private boolean searchBackwards;


	/**
	 * Initializes a text finder with a null search string, case sensitivity turned on,
	* and search backwards turned off.
	 */

	public TextFinder()
	{
		searchString = new String();
	}


	/**
	 * Initializes a text finder with the specified search string, case sensitivity turned on,
	* and search backwards turned off.
	 */


	public TextFinder(String searchString)
	{
		setSearchString(searchString);
	}


	/**
	 * Initializes a text finder with the specified search string and case sensitivity,
	* and search backwards turned off.
	 */


	public TextFinder(String searchString, boolean caseInsensitive)
	{
		setSearchString(searchString);
		setCaseInsensitive(caseInsensitive);
	}


	/**
	 * Returns true if the text finder will ignore case; false otherwise.
	 */


	public boolean getCaseInsensitive()
	{
		return (caseInsensitive);
	}


	/**
	 * Returns true if the text finder will search backwards; false otherwise.
	 */


	public boolean getSearchBackwards()
	{
		return (searchBackwards);
	}


	/**
	 * Returns the current search string.
	 */


	public String getSearchString()
	{
		return (searchString);
	}


	/**
	 * Returns the first matching text range beginning at the specified location
	* using the specified search parameters.
	*
	* @param buffer a character array where the text to be searched is located.
	* @param bufferLength the number of valid characters in the character array.
	* @param fromIndex the index from where the search should begin.
	* @return an TextFinderResult representing the matching range of text.
	 */


	public TextFinderResult findText(char buffer[], int bufferLength, int fromIndex)
	{
		if (searchBackwards)
		{
			return (findTextBackwards(buffer, bufferLength, fromIndex));
		}
		else
		{
			return (findTextForwards(buffer, bufferLength, fromIndex));
		}
	}


	public TextFinderResult findTextBackwards(char buffer[], int bufferLength, int fromIndex)
	{
		TextFinderResult	result = null;

		if (searchString.length() > 0)
		{
			int     start = (fromIndex < bufferLength) ? fromIndex : bufferLength;

			start--;

			if (caseInsensitive)
			{
				int     length = searchString.length();
				String  upperStr = searchString.toUpperCase();
				String  lowerStr = searchString.toLowerCase();
				char    upperChar = upperStr.charAt(0);
				char    lowerChar = lowerStr.charAt(0);

				while (start >= 0)
				{
					if ((buffer[start] == lowerChar) || (buffer[start] == upperChar))
					{
						int     x = 1;
						int     i = start + x;
						int     end = start + length;

						while (i < end)
						{
							if (i >= bufferLength)
							{
								break;
							}

							if ((buffer[i] != lowerStr.charAt(x)) && (buffer[i] != upperStr.charAt(x)))
							{
								break;
							}

							i++;
							x++;
						}

						if (i == end)
						{
							result = new TextFinderResult(start, end);

							break;
						}
					}

					start--;
				}
			}
			else
			{
				int     length = searchString.length();
				char    findChar = searchString.charAt(0);

				while (start >= 0)
				{
					if (buffer[start] == findChar)
					{
						int     x = 1;
						int     i = start + x;
						int     end = start + length;

						while (i < end)
						{
							if (i >= bufferLength)
							{
								break;
							}

							if (buffer[i] != searchString.charAt(x))
							{
								break;
							}

							i++;
							x++;
						}

						if (i == end)
						{
							result = new TextFinderResult(start, end);

							break;
						}
					}

					start--;
				}
			}
		}

		return (result);
	}


	public TextFinderResult findTextForwards(char buffer[], int bufferLength, int fromIndex)
	{
		TextFinderResult	result = null;

		if (searchString.length() > 0)
		{
			int     start = (fromIndex < bufferLength) ? fromIndex : bufferLength;

			if (caseInsensitive)
			{
				int     length = searchString.length();
				String  upperStr = searchString.toUpperCase();
				String  lowerStr = searchString.toLowerCase();
				char    upperChar = upperStr.charAt(0);
				char    lowerChar = lowerStr.charAt(0);

				while (start < bufferLength)
				{
					if ((buffer[start] == lowerChar) || (buffer[start] == upperChar))
					{
						int     x = 1;
						int     i = start + x;
						int     end = start + length;

						while (i < end)
						{
							if (i >= bufferLength)
							{
								break;
							}

							if ((buffer[i] != lowerStr.charAt(x)) && (buffer[i] != upperStr.charAt(x)))
							{
								break;
							}

							i++;
							x++;
						}

						if (i == end)
						{
							result = new TextFinderResult(start, end);

							break;
						}
					}

					start++;
				}
			}
			else
			{
				int     length = searchString.length();
				char    findChar = searchString.charAt(0);

				while (start < bufferLength)
				{
					if (buffer[start] == findChar)
					{
						int     x = 1;
						int     i = start + x;
						int     end = start + length;

						while (i < end)
						{
							if (i >= bufferLength)
							{
								break;
							}

							if (buffer[i] != searchString.charAt(x))
							{
								break;
							}

							i++;
							x++;
						}

						if (i == end)
						{
							result = new TextFinderResult(start, end);

							break;
						}
					}

					start++;
				}
			}
		}

		return (result);
	}


	/**
	 * When set to true, the text finder will ignore case.
	 */


	public void setCaseInsensitive(boolean state)
	{
		caseInsensitive = state;
	}


	/**
	 * When set to true, the text finder will search backwards.
	 */


	public void setSearchBackwards(boolean state)
	{
		searchBackwards = state;
	}


	/**
	 * Sets the search string.
	 */


	public void setSearchString(String str)
	{
		searchString = (str != null) ? str : new String();
	}


}

