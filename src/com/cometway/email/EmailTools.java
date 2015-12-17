
package com.cometway.email;

import java.io.*;
import java.net.*;
import java.util.*;
import com.cometway.text.*;
import com.cometway.util.*;


/**
* Defines a set of static methods useful for parsing email information.
*/

public class EmailTools
{
	/**
	* Does a case-sensitive find and replace within the specified com.cometway.text.TextRange.
	* Returns a new TextRange representing the changed contents of the TextRange.
	*/

	public static TextRange change(String find, String replace, TextRange t)
	{
		return change(find, replace, t, false);
	}


	/**
	* Does a find and replace within the specified com.cometway.text.TextRange.
	* Case-sensitivity can be specified.
	* Returns a new TextRange representing the changed contents of the TextRange.
	*/

	public static TextRange change(String find, String replace, TextRange t, boolean ignoreCase)
	{
		int			x = 0;
		TextFinder		f = new TextFinder(find, ignoreCase);
		StringTextBuffer	s = new StringTextBuffer(t.getText());
		TextRange		a = s.findText(0, f);

		while (a != null)
		{
			TextPointer     p = a.getStartPointer();

			a.delete();
			p.insertText(replace);

			a = s.findText(0, f);
		}

		return new TextRange(s, 0, s.getLength());
	}


	/**
	* Returns a TextRange representing the text between before and after parameters within the
	* specified com.cometway.text.TextRange.
	* Returns a new TextRange representing the isolated text.
	*/

	public static TextRange textBetween(String b, String a, TextRange t)
	{
		return textBetween(b, a, t, true);
	}


	/**
	* Returns a TextRange representing the text between before and after parameters within the
	* specified com.cometway.text.StringTextBuffer.
	* Returns a new TextRange representing the isolated text.
	*/

	public static TextRange textBetween(String b, String a, StringTextBuffer s)
	{
		return textBetween(b, a, new TextRange(s, 0, s.getLength()), true);
	}


	/**
	* Returns a TextRange representing the text between before and after parameters within the
	* specified com.cometway.text.TextRange. Case-sensitivity can be specified.
	* Returns a new TextRange representing the isolated text.
	*/

	public static TextRange textBetween(String b, String a, TextRange t, boolean ignoreCase)
	{
		TextFinder		before = new TextFinder(b, ignoreCase);
		TextFinder		after = new TextFinder(a, ignoreCase);
		StringTextBuffer	s = (StringTextBuffer) t.getTextBuffer();
		TextRange		begin = s.findText(t.getStart(), before);

		if (begin == null || begin.getEnd() > t.getEnd())
		{
			return null;
		}

		TextRange       end = s.findText(begin.getEnd(), after);

		if (end == null || end.getEnd() > t.getEnd())
		{
			return null;
		}

		return new TextRange(s, begin.getStart(), end.getEnd());
	}


	/**
	* Word wraps the a String to the specified number of columns
	* and returns the wrapped String.
	*/

	public static String formatWrap(String s, int width)
	{
		StringBuffer    st = new StringBuffer(s);
		int		lastspace = 0;
		int		charsonline = 0;

		for (int i = 0; i < st.length(); i++)
		{
			if (st.charAt(i) == ' ')
			{
				lastspace = i;
			}

			if (st.charAt(i) == '\n')
			{
				charsonline = 0;
			}

			if (charsonline == width)
			{
				st.setCharAt(lastspace, '\n');

				i = lastspace;
				charsonline = 0;
			}

			charsonline++;
		}

		String  temp = new String(st);

		return temp;
	}


	/**
	* Removes any escaped ASCII characters that use the notation &#ascii_val; (ie: &#201;)
	* and returns the result as a String.
	*/

	public static String stripSpecialChars(String s)
	{
		StringTextBuffer	b = new StringTextBuffer(s);
		RegExpTextFinder	f = new RegExpTextFinder("&#*.*;");

		while (true)
		{
			TextRange       r = b.findText(0, f);

			if (r == null)
			{
				break;
			}

			r.delete();
		}

		return (b.getText());
	}


	/**
	* Formats a double value to 2 decimal places as a String.
	* Ending zeros are ommitted.
	*/

	public static String formatDouble(double value)
	{
		return formatDouble(value, 2, false);
	}


	/**
	* Formats a double value to the specified number of decimal places.
	* Ending zeros are ommitted.
	*/

	public static String formatDouble(double value, int places)
	{
		return formatDouble(value, places, false);
	}


	/**
	* Formats a double value to 2 decimal places as a String.
	* Set the fixed parameter to true if ending zeros are required.
	*/

	public static String formatDouble(double value, boolean fixed)
	{
		return formatDouble(value, 2, fixed);
	}


	/**
	* Formats a double value to the specified number of decimal places as a String.
	* Set the fixed parameter to true if ending zeros are required.
	*/

	public static String formatDouble(double value, int places, boolean fixed)
	{
		String  str = Double.toString(value);
		if (str.indexOf("E")!=-1) //it's in scientific notation
			return str;
		int     i = str.indexOf(".");

		if (i != -1)
		{
			String  postDec = str.substring(i + 1, str.length());
			String  preDec = str.substring(0, i);

			if (postDec.length() > places)
			{
				postDec = postDec.substring(0, places);
			}

			if (!fixed)
			{
				if (Double.valueOf(postDec).doubleValue() == 0)
				{
					postDec = "";
				}
			} else {
				if (postDec.length()<places)
				{
					for (int j=0;j<places-postDec.length();j++)
						postDec += "0";
				}
			}

			if (!postDec.equals(""))
			{
				str = preDec + "." + postDec;
			}
			else
			{
				str = preDec;
			}
		}
		else if (fixed)
		{
			str += ".";

			for (int j = 0; j < places; j++)
			{
				str += "0";
			}
		}

		return (str);
	}


	/**
	* This method parses the specified String typically containing the text contents
	* from an email message header (ie: "Bob Smith" <bob@smith.com>) and returns two
	* String components as a Pair. The first element of the Pair is the email name
	* and the second element will contain the email address.
	*/

	public static Pair parseEmailAddress(String email)
	{
		if (email == null)
		{
			return (new Pair("No Email", ""));
		}

		email = stripEnds(email);

		if (email.equals(""))
		{
			return (new Pair("No Email", ""));
		}

		int     amp = email.indexOf("@");

		if (amp == -1)
		{
			return (new Pair("No Email", email));
		}

		if ((amp == 0) || (amp == email.length() - 1))
		{
			return (new Pair(email, ""));


			// search forward to find end of domain

		}

		int     endPt = -1;
		int     spot = amp;

		while (endPt == -1)
		{
			spot++;

			if (spot == email.length())
			{
				endPt = spot;

				continue;
			}

			if (email.charAt(spot) == ' ')
			{
				endPt = spot;
			}
		}


		// search backward to find start of address

		int     startPt = -1;

		spot = amp;

		char    searchChar = ' ';


		// special case "Todd Phillips"@domain.com

		if (email.charAt(spot - 1) == '\"')
		{
			searchChar = '\"';
			spot--;
		}

		while (startPt == -1)
		{
			spot--;

			if (spot <= 0)
			{
				startPt = 0;

				continue;
			}

			if (email.charAt(spot) == searchChar)
			{
				startPt = spot;
			}
		}

		String  address = email.substring(startPt, endPt);

		address = stripEnds(address);


		// take name to be whatever is before and after the address

		String  before = "";
		String  after = "";

		if (startPt == 0)
		{
			before = "";
		}
		else
		{
			before = email.substring(0, startPt);
		}

		if (endPt == email.length())
		{
			after = "";
		}
		else
		{
			after = email.substring(endPt);
		}

		before = stripEnds(before);
		after = stripEnds(after);

		String  name = before + after;

		name = stripEnds(name);

		return (new Pair(address, name));
	}


	/**
	* Returns a String with surrounding symbols (ie: (), <>, "") removed
	* from the specified String.
	*/

	public static String stripEnds(String s)
	{
		if (s == null)
		{
			return (new String(""));
		}

		s = s.trim();

		int     l = s.length();

		if (l == 0)
		{
			return (new String(""));
		}

		char    tags[][] = 
		{
			{
				'(', ')'
			}, 
			{
				'<', '>'
			}, 
			{
				'\"', '\"'
			}
		};

		for (int i = 0; i < tags.length; i++)
		{
			if ((s.charAt(0) == tags[i][0]) && (s.charAt(l - 1) == tags[i][1]))
			{
				s = s.substring(1, l - 1);
				s = s.trim();
				l = s.length();

				if (l == 0)
				{
					return (new String(""));
				}

				i = 0;
			}
		}

		return s;
	}


	/**
	* Returns a String with greater-than (>), less-than (<), and carriage return (\n)
	* characters changed for HTML compatibility (&gt;, &lt;, and <BR> respectively).
	*/

	public static String fixForHTML(String text)
	{
		StringTextBuffer	s = new StringTextBuffer(text);
		TextRange		t = new TextRange(s, 0, s.getLength());

		t = change("<", "&lt;", t);
		t = change(">", "&gt;", t);
		t = change("\n", "<BR>", t);

		return t.toString();
	}
}

