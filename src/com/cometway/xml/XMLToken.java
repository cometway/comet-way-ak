
package com.cometway.xml;

import com.cometway.props.Props;


/**
* This class represents a XML element returned by the nextElement() method
* in the XMLParser.
* @see XML
* @see XMLParser
*/

public class XMLToken
{
	/** This is the type of Element */
	public int type;
	/** This is the parsed out data */
	public String data;

	public XMLToken()
	{
		;
	}

	/**
	* This constructor allows for the XML token's type to be specified.
	*/

	public XMLToken(int type, String data)
	{
		this.type = type;
		this.data = data;
	}


	/**
	* This returns a Props containing the attributes if this is a START_TAG or EMPTY_ELEMENT_TAG.
	* The tag name itself is also stored in a property named <PRE>tag_name</PRE>.
	* It throws an XMLParserException otherwise.
	*/

	public Props getProps() throws XMLParserException
	{
		Props p = new Props();


		// Extract the contents of this tag.

		String s;

		if (type == XML.EMPTY_ELEMENT_TAG)
		{
			s = data.substring(1, data.length() - 2);
		}
		else if (type == XML.START_TAG)
		{
			s = data.substring(1, data.length() - 1);
		}
		else
		{
			throw new XMLParserException("Invalid XMLToken.type for this operation.");
		}


//System.out.println("==> " + s);

		// Get the tag name.

		int i = s.indexOf(' ');

		if (i < 0)
		{
			// There are no attributes to parse so just set the tag_name property.

			p.setProperty("tag_name", s);
		}
		else
		{
			// OK it's got attributes, let's parse this!

			p.setProperty("tag_name", s.substring(0, i));

			int start = i + 1;
			int len = s.length();

			while (start < len)
			{
				i = s.indexOf('=', start);

				if (i < 0)
				{
					String key = s.substring(start, len).trim();

					if (key.length() > 0)
					{
						p.setBoolean(key, true);
					}

//					throw new XMLParserException("Incorrectly formatted attributes in: " + s + " (start = " + start + ")");
					break;
				}

				String key = s.substring(start, i).trim();
//System.out.println("==> key = " + key);
				String value = "";

				start = i + 1;

				if (start > len) break;

				char c = s.charAt(start);

				if (c == '\'')
				{
					start++;

					if (start > len)
					{
						throw new XMLParserException("Missing end-quote (') in: " + s + " (start = " + start + ")");
					}

					i = s.indexOf('\'', start);

					if (i < 0)
					{
						throw new XMLParserException("Missing end-quote (') in: " + s + " (start = " + start + ")");
					}

					p.setProperty(key, s.substring(start, i));

					start = i + 1;
				}
				else if (c == '"')
				{
					start++;

					if (start > len)
					{
						throw new XMLParserException("Missing end-quote (\") in: " + s + " (start = " + start + ")");
					}

					i = s.indexOf('"', start);

					if (i < 0)
					{
						throw new XMLParserException("Missing end-quote (\") in: " + s + " (start = " + start + ")");
					}

					p.setProperty(key, s.substring(start, i));


					start = i + 1;
				}
				else
				{
					i = s.indexOf(' ', start);

					if (i < 0)
					{
						p.setProperty(key, s.substring(start));
						break;
					}
					else
					{
						p.setProperty(key, s.substring(start, i));
						start = i + 1;
					}
				}
			}
		}

		return (p);
	}


	// Use this for testing the attribute parsing. It needs a lot of work if it's going to be robust.
	// Maybe we should write a real parser for attributes?

	public static void main(String args[])
	{
		try
		{
			XMLToken t = new XMLToken(XML.EMPTY_ELEMENT_TAG, "<PARAM foo=bar  bar=\"foo\" key='Current Time' value='10:50:30 04/14/2005' BORDER/>");
			Props p = t.getProps();
			p.dump();
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}
}
