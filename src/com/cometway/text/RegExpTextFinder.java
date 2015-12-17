
package com.cometway.text;

import org.apache.oro.text.perl.*;


/**
 * A text finder which searches for the first range of text matching
* a given Regular Expression (PERL 5 reg exp)
 */

public class RegExpTextFinder implements ITextFinder
{
	private String  pattern;


	/**
	 * Initializes a RegExpTextFinder to search for the specified regular expression. 
	* @param pattern a String containing the search expression.
	 */

	public RegExpTextFinder(String pattern)
	{
		this.pattern = "m/" + pattern + "/m";
	}


	/**
	 * Initializes a RegExpTextFinder to search for the specified regular expression. 
	* @param pattern a String containing the regular expression.
	* @param ignoreCase case-insensitive searches when true; exact searches otherwise.
	 */


	public RegExpTextFinder(String pattern, boolean ignoreCase)
	{
		if (ignoreCase)
		{
			this.pattern = "m/" + pattern + "/im";
		}
		else
		{
			this.pattern = "m/" + pattern + "/m";
		}
	}


	/**
	 * Returns the first matching text range beginning at the specified location
	* using the specified regular expression.
	*
	* @param buffer a character array where the text to be searched is located.
	* @param bufferLength the number of valid characters in the character array.
	* @param fromIndex the index from where the search should begin.
	* @return an TextFinderResult representing the matching range of text.
	 */


	public TextFinderResult findText(char[] buffer, int bufferLength, int fromIndex)
	{
		Perl5Util       perl = new Perl5Util();
		String		content = String.valueOf(buffer, fromIndex, bufferLength - fromIndex);

		if (perl.match(pattern, content))
		{
			return (new TextFinderResult(perl.beginOffset(0) + fromIndex, perl.endOffset(0) + fromIndex));
		}

		return (null);
	}


}

