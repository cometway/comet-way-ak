
package com.cometway.util;

import java.util.*;


/**
 * A set of utilities for manipulating strings.
 */

public class StringTools
{
	private static final String     kBinHexHeaderString = "(This file must be converted with BinHex";
	private static final String     kBinHexDecodeString = "!\"#$%&'()*+,-012345689@ABCDEFGHIJKLMNPQRSTUVXYZ[`abcdefhijklmpqr";


	/**
	 * This method decodes BinHex 4.0 data. The data is passed in as a String.
	 * If the decoded String is in Mac file format, the Data Fork will automatically
	 * be extracted and returned. The CRC will not be checked and the rest of the file
	 * will be discarded.
	 * @param binhex The BinHex 4.0 data to be decoded and converted.
	 * @return The decoded file and converted to non-Mac format.
	 */

	public static String decodeBinHex(String binhex)
	{
		return (decodeBinHex(binhex, true, false));
	}


	/**
	 * This method decodes BinHex 4.0 data. The data is passed in as a String.
	 * If the decodeMac flag is true, the Data Fork will be extracted out of the decoded 
	 * Mac file and returned. The CRC will not be checked and the rest of the file
	 * will be discarded. If the verbose flag is true, all errors encountered in the
	 * BinHex 4.0 data and the Mac file header will be reported to System.err.
	 * @param binhex The BinHex 4.0 data to be decoded and/or converted.
	 * @param decodeMac If this is TRUE, decoded data will be converted from Mac format.
	 * @param verbose If this is TRUE, errors will be reported to System.err.
	 * @return The decoded and/or converted file.
	 */


	public static String decodeBinHex(String binhex, boolean decodeMac, boolean verbose)
	{
		StringBuffer    sb = new StringBuffer();
		int		i = 0;		// Find the beginning of the binhex'ed file and extract it

		try
		{
			i = binhex.indexOf(kBinHexHeaderString);

			if (i != -1)
			{
				i = i + kBinHexHeaderString.length();
				i = binhex.indexOf(":", i);

				if (i != -1)
				{
					binhex = binhex.substring(i + 1);
					i = binhex.indexOf(":");

					if (i != -1)
					{
						binhex = binhex.substring(0, i);
					}
					else
					{
						throw (new Exception());
					}
				}
				else
				{
					throw (new Exception());
				}
			}
			else
			{
				throw (new Exception());
			}
		}
		catch (Exception e)
		{
			if (verbose)
			{
				System.err.println("StringTools.decodeBinHex(): Error reading BinHex 4.0 header");
			}

			return ("");
		}		// Read the bytes, convert them to the 6bit value, decode the 8bit data

		try
		{
			boolean[]       bits = new boolean[24];
			byte[]		bytes = new byte[3];

			for (int x = 0; x < binhex.length(); x++)
			{
				i = 0;		// Fill bits array with 6 bit encoded data

				while (true)
				{
					int     code = kBinHexDecodeString.indexOf(binhex.charAt(x));

					if (code != -1)
					{
						for (int z = 0; z < 6; z++)
						{
							if (code % 2 == 1)
							{
								bits[(i * 6) + (5 - z)] = true;
							}
							else
							{
								bits[(i * 6) + (5 - z)] = false;
							}

							code = code / 2;
						}
					}
					else
					{
						if (verbose)
						{
							System.err.println("StringTools.decodeBinHex(): Read a non BinHex 4.0 character while decoding.");
						}

						throw (new Exception());
					}

					if (++x == binhex.length())
					{
						i++;

						break;
					}

					if (++i == 4)
					{
						x--;

						break;
					}
				} /* for(int q=0;q<24;q++) {
					if(bits[q]) {
						System.out.print("1");
					}
					else {
						System.out.print("0");
					}
				}
				System.out.println(); */

				if (i < 4)
				{
					for (int z = 0; z < (24 - (i * 6)); z++)
					{
						bits[(i * 4) + z] = false;
					}

					i = i + 2;
				}

				i = (i * 3) / 4;	// Read from bits array 8 bits at a time.

				for (int z = 0; z < i; z++)
				{
					long    word = 0;

					for (int y = 0; y < 8; y++)
					{
						if (bits[(z * 8) + (7 - y)])
						{
							word = word + (long) Math.pow(2, y);
						}
					}		// if(word == 10) {


					// word = 13;
					// }

					bytes[z] = (byte) word;		// System.err.println("word="+word+", char="+(new String(bytes,z,1)));
				}

				sb.append(new String(bytes, 0, i));
			}
		}
		catch (Exception e)
		{
			if (verbose)
			{
				System.err.println("StringTools.decodeBinHex(): Error encountered while decoding BinHex 4.0 data.");
			}

			return ("");
		}

		if (decodeMac)
		{		// Decode DATA Fork out of MAC file header
			try
			{
				String  data = sb.toString();
				int     length = 0;		// first byte should be file name length.

				i = (int) data.charAt(0);		// skip first byte, name length, 10 bytes for FLAG, AUTH, and FLAG
				i = i + 12;		// calculate the data length
				length = ((int) data.charAt(i++)) * 16777216 + length;
				length = ((int) data.charAt(i++)) * 65536 + length;
				length = ((int) data.charAt(i++)) * 256 + length;
				length = ((int) data.charAt(i++)) * 1 + length;		// skip 4 bytes for Resource Fork Length, skip 2 bytes for CRC
				i = i + 6;
				sb = new StringBuffer(data.substring(i, i + length));
			}
			catch (Exception e)
			{
				if (verbose)
				{
					System.err.println("StringTools.decodeBinHex(): Error encountered while decoding BinHex 4.0 data.");
				}
			}
		}

		return (sb.toString());
	}

	/**
	 * This method parses a comma separated String of elements and returns them in an array
	 */
	public static String[] commaToArray(String in)
	{
		String[] rval = new String[0];

		if(in!=null) {
			try {
				Vector v = new Vector();
				int i = in.indexOf(",");
				while(i!=-1) {
					String tmp = in.substring(0,i);
					v.addElement(tmp.trim());
					in = in.substring(i+1);
					i = in.indexOf(",");
				}
				if(in.trim().length()>0) {
					v.addElement(in.trim());
				}
				
				if(v.size()>0) {
					rval = new String[v.size()];
					for(int x=0;x<v.size();x++) {
						rval[x] = (String)v.elementAt(x);
					}
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}

		return(rval);
	}




	/**
	 * returns a the number of rows (lines/Height) and columns (Width) of a 
	 * string as a java.awt.Dimension 
	 * <pre>
	 * There are some specifics: (h x w)
	 *   ''  				will return  0 x 0
	 *   '\n\n'  			will return  0 x 2
	 *   '\na\na\n'  		will return  1 x 3
	 *   '\ra\ra'  		will return  1 x 2
	 *   '\n\rabc\n\rab'	will return  3 x 3
	 *   '\r\nabcd\r\n' 	will return  4 x 2
	 * </pre>
	 * @param in The string to find the max dimension of
	 * @return A Dimension of this String
	 * @see java.awt.Dimension
	 */


	public static java.awt.Dimension getDimension(String in)
	{
		int     index = 0;
		int     overflow = in.length();
		int     maxRows = 0;
		int     maxCols = 0;
		String  token = "\n";

		if (in.indexOf("\r\n") != -1)
		{
			token = "\r\n";
		}
		else if (in.indexOf("\n\r") != -1)
		{
			token = "\n\r";
		}

		if (in.indexOf(token) == -1)
		{
			token = "\r";

			if (in.indexOf(token) == -1)
			{
				if (overflow == 0)
				{
					return (new java.awt.Dimension(0, 0));
				}

				return (new java.awt.Dimension(overflow, 1));
			}
			else
			{
				maxCols = in.indexOf(token);
				index = maxCols;
				maxRows = 1;
			}
		}
		else
		{
			maxCols = in.indexOf(token);
			index = maxCols;
			maxRows = 1;

			if (in.lastIndexOf(token) < in.length() - token.length())
			{
				in = in + token;
			}
		}

		try
		{
			int     tempindex = in.indexOf(token, index + 1);

			while (tempindex != -1)
			{
				maxRows++;

				if (maxCols < (tempindex - (index + token.length())))
				{
					maxCols = tempindex - (index + token.length());
				}

				index = tempindex;
				tempindex = in.indexOf(token, index + 1);
			}
		}
		catch (Exception t)
		{
			System.out.println("Unexpected error: " + t);
		}

		return (new java.awt.Dimension(maxCols, maxRows));
	}


	/**
	 * returns an int value of a parameter 'sw' given all the parameters 'args'
	 */


	public static int getIntParam(String[] args, String sw)
	{
		int     intValue = 0;
		String  strValue = getParam(args, sw);

		if (strValue != null)
		{
			intValue = Integer.parseInt(strValue);
		}

		return (intValue);
	}


	/**
	 * returns a String value of the parameger 'sw' given all the parameter 'args'
	 */


	public static String getParam(String[] args, String sw)
	{
		String  value = null;

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals(sw) && (args.length > (i + 1)))
			{
				i++;
				value = args[i];
			}
		}

		return (value);
	}


	/**
	 * returns true if 'sw' is a param within 'args', the list of parameters
	 */


	public static boolean hasParam(String[] args, String sw)
	{
		boolean hasIt = false;

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals(sw))
			{
				hasIt = true;

				break;
			}
		}

		return (hasIt);
	}


	/**
	 * returns a string without anything matching <*> (all HTML tags)
	 */

	public static String removeHTMLTags(String in)
	{
		int		index = 0;
		boolean		tag = false;
		boolean		paren = false;
		StringBuffer    out = new StringBuffer();

		while (index < in.length())
		{
			if (in.charAt(index) == '"')
			{
				if (tag)
				{
					paren = !paren;
				}
				else if (!tag && paren)
				{
					paren = false;
				}
			}

			if ((!paren) && (in.charAt(index) == '<'))
			{
				tag = true;
			}

			if (!tag)
			{
				if (in.charAt(index) == '&')
				{
					int     mark = index + 1;

					if (mark < in.length())
					{
						if (in.indexOf("lt", mark) == mark)
						{
							out.append("<");

							index = mark + 1;
						}
						else if (in.indexOf("gt", mark) == mark)
						{
							out.append(">");

							index = mark + 1;
						}
						else if (in.indexOf("amp", mark) == mark)
						{
							out.append("&");

							index = mark + 2;
						}
						else if (in.indexOf("quot", mark) == mark)
						{
							out.append("\"");

							index = mark + 3;
						}
						else if (in.indexOf("nbsp", mark) == mark)
						{
							out.append(" ");

							index = mark + 3;
						}
						else if (in.indexOf("reg", mark) == mark)
						{
							out.append("(R)");

							index = mark + 2;
						}
						else if (in.indexOf("copy", mark) == mark)
						{
							out.append("(c)");

							index = mark + 3;
						}
						else if (in.indexOf("ensp", mark) == mark)
						{
							out.append(" ");

							index = mark + 3;
						}
						else if (in.indexOf("emsp", mark) == mark)
						{
							out.append(" ");

							index = mark + 3;
						}
						else if (in.indexOf("endash", mark) == mark)
						{
							out.append("-");

							index = mark + 5;
						}
						else if (in.indexOf("emdash", mark) == mark)
						{
							out.append("-");

							index = mark + 5;
						}
						else
						{
							out.append(in.charAt(index));
						}
					}
					else
					{
						out.append(in.charAt(index));
					}
				}
				else
				{
					out.append(in.charAt(index));
				}
			}

			if ((!paren) && (in.charAt(index) == '>'))
			{
				tag = false;
			}

			index++;
		}

		return (out.toString());
	}


	/**
	 * Sorts Pairs within a vector based on the first object (which should be string)
	 */


	public static Vector pairCaseSensitiveSort(Vector in)
	{
		Vector  rval = new Vector();

		if (in.size() > 1)
		{
			Pair[]		pairs = new Pair[in.size()];
			Enumeration     e = in.elements();
			int		count = 0;

			while (e.hasMoreElements())
			{
				Object  o = e.nextElement();

				if (o != null)
				{
					if (o instanceof Pair)
					{
						pairs[count] = (Pair) o;
						count++;
					}
				}
			}

			boolean sorted = false;

			while (!sorted)
			{
				sorted = true;

				for (int x = 0; x < pairs.length - 1; x++)
				{
					if (((String) pairs[x].first()).compareTo((String) pairs[x + 1].first()) > 0)
					{
						Pair    temp = pairs[x];

						pairs[x] = pairs[x + 1];
						pairs[x + 1] = temp;
						sorted = false;
					}
				}
			}

			for (int x = 0; x < pairs.length; x++)
			{
				rval.addElement(pairs[x]);
			}
		}
		else
		{
			rval = in;
		}

		return (rval);
	}


	/**
	 * Returns a string that is guaranteed to fit within 'length' using a FontMetrics of a graphics.
	 */


	public static String truncateString(java.awt.Graphics g, String inString, int length)
	{
		java.awt.FontMetrics    metrics = g.getFontMetrics();

		if (length > metrics.stringWidth(inString))
		{
			return (inString);
		}
		else
		{
			int     dotlength = metrics.stringWidth("...");

			if (dotlength > length)
			{
				dotlength = metrics.stringWidth("..");

				if (dotlength > length)
				{
					dotlength = metrics.stringWidth(".");

					if (dotlength > length)
					{
						return ("");
					}
					else
					{
						return (".");
					}
				}
				else
				{
					return ("..");
				}
			}
			else
			{
				while (length <= (metrics.stringWidth(inString) + dotlength))
				{
					inString = inString.substring(0, inString.length() - 1);
				}

				return (inString + "...");
			}
		}
	}


	/**
	 * Case-insensitive Sort of Pairs within a vector based on the first object (which should be string)
	 */


	public static Vector pairSort(Vector in)
	{
		Vector  rval = new Vector();

		if (in.size() > 1)
		{
			Pair[]		pairs = new Pair[in.size()];
			Enumeration     e = in.elements();
			int		count = 0;

			while (e.hasMoreElements())
			{
				Object  o = e.nextElement();

				if (o != null)
				{
					if (o instanceof Pair)
					{
						pairs[count] = new Pair(((String) ((Pair) o).first()).toLowerCase(), (Pair) o);
						count++;
					}
				}
			}

			boolean sorted = false;

			while (!sorted)
			{
				sorted = true;

				for (int x = 0; x < pairs.length - 1; x++)
				{
					if (((String) pairs[x].first()).compareTo((String) pairs[x + 1].first()) > 0)
					{
						Pair    temp = pairs[x];

						pairs[x] = pairs[x + 1];
						pairs[x + 1] = temp;
						sorted = false;
					}
				}
			}

			for (int x = 0; x < pairs.length; x++)
			{
				rval.addElement(pairs[x].second());
			}
		}
		else
		{
			rval = in;
		}

		return (rval);
	}


	/**
	 * Takes a string and parses them into arguments, preserving backslashes and double quotes.
	 */


	public static String[] parseArgs(String in)
	{
		Vector		words = new Vector();
		String[]	rval;
		String		word = "";
		int		index = 0;
		int		length = in.length();
		boolean		dq = false;
		boolean		bslash = false;
		char		c;

		while (index < length)
		{
			c = in.charAt(index);

			if (c == '\"')
			{
				if ((dq) && (!bslash))
				{
					words.addElement((Object) word);

					word = "";
					dq = false;
				}
				else
				{
					if (bslash)
					{
						if (dq)
						{
							word = word + "\"";
						}
						else
						{
							words.addElement((Object) word);

							word = "\"";
						}
					}
					else if (!dq)
					{
						if (!word.equals(""))
						{
							words.addElement((Object) word);

							word = "";
						}

						dq = true;
					}
				}

				bslash = false;
			}
			else if (c == ' ')
			{
				if (dq)
				{
					word = word + " ";
				}
				else
				{
					if (!word.equals(""))
					{
						words.addElement((Object) word);

						word = "";
					}
				}

				bslash = false;
			}
			else if (c == '\\')
			{
				if (bslash || (!dq))
				{
					word = word + "\\";
					bslash = false;
				}
				else
				{
					bslash = true;
				}
			}
			else
			{
				word = word + c;
				bslash = false;
			}		// System.out.println("c="+c+", bslash="+bslash+", dq="+dq+", word="+word);

			index++;
		}

		if (!word.equals(""))
		{
			words.addElement((Object) word);
		}

		rval = new String[words.size()];

		for (int x = 0; x < rval.length; x++)
		{
			rval[x] = (String) words.elementAt(x);
		}

		return (rval);
	}

}

