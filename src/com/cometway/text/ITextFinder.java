
package com.cometway.text;


/**
 * Interface for implementing an abstract text searching algorithm.
 */

public interface ITextFinder
{
	/**
	* Returns the first matching text range beginning at the specified location.
	* This interface makes it possible to search a text buffer in any direction
	* or manner using any sort of algorithm suitable for the search.
	*
	* @param buffer a character array where the text to be searched is located.
	* @param bufferLength the number of valid characters in the character array.
	* @param fromIndex the index from where the search should begin.
	* @return an TextFinderResult representing the matching range of text.
	*/

	public TextFinderResult findText(char buffer[], int bufferLength, int fromIndex);
}

