
package com.cometway.xml;


import com.cometway.ak.Agent;
import com.cometway.props.Props;
import com.cometway.props.PropsList;
import com.cometway.props.PropsException;
import com.cometway.util.Base64Encoding;
import com.cometway.util.DateTools;
import com.cometway.util.ObjectSerializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Vector;



/**
* Imports Props records from a XMLPropsList file into the specified PropsList.
*/

public class XMLPropsListImportAgent extends Agent
{
	public void initProps()
	{
		setDefault("load_file", "database.xml");
		setDefault("database_name", "database");
		setDefault("container_tag", "props_list");
		setDefault("props_tag", "props");
	}


	/**
	* Loads a Props List XML file into a Vector of Props.
	*/

	public void start()
	{
		String filename = getTrimmedString("load_file");
		FileInputStream in = null;

		try
		{
			println("Importing Props from " + filename);

			in = new FileInputStream(new File(filename));

			int count = parseInputStream(in);

			println("" + count + " records loaded");
		}
		catch (Exception e)
		{
			error("Could not continue", e);
		}
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (Exception e)
				{
					error("Could not close InputStream for " + filename + ": " + in, e);
				}
			}
		}
	}


	/**
	* Loads a Props List XML document into a Vector of Props.
	*/

	public int parseInputStream(InputStream in) throws XMLParserException
	{
		PropsList database = (PropsList) getServiceImpl(getTrimmedString("database_name"));
		String containerStart = '<' + getString("container_tag") + '>';
		String containerEnd = "</" + getString("container_tag") + '>';
		String propsStart = '<' + getString("props_tag") + '>';
		String propsEnd = "</" + getString("props_tag") + '>';
		int count = 0;

		XMLParser parser = new XMLParser(in);
		parser.nextToken(XML.XML_10_HEADER);
		parser.nextToken(containerStart);

		while (true)
		{
			XMLToken t = parser.nextToken();

			if (t.data.equals(propsStart))
			{
				Props p = new Props();

				while (true)
				{
					t = parser.nextToken();

					if (t.type == XML.START_TAG)
					{
						String key = t.data.substring(1, t.data.length() - 1);
						t = parser.nextToken();

						if (t.data.equals("<base64>"))
						{
							t = parser.nextElementContent();

							try
							{
								byte[] data = Base64Encoding.decode(t.data);
								Object o = ObjectSerializer.unserialize(data);
								p.setProperty(key, o);
							}
							catch (Exception e)
							{
								throw new XMLParserException("Invaid token: " + t.data);
							}

							parser.nextToken("</base64>");
							parser.nextToken("</" + key + '>');
						}
						else if (t.data.equals("<date>"))
						{
							t = parser.nextElementContent();

							try
							{
// At some point we should recognize UTC formatting (ends with a Z)
// as well as decimal seconds. It would be prudent to also
// remove intermediate dashes and slashes before parsing.

								Date theDate = DateTools.parseISO8601String(t.data);

								p.setProperty(key, theDate);
							}
							catch (Exception e)
							{
								throw new XMLParserException("Invaid token: " + t.data);
							}

							parser.nextToken("</date>");
							parser.nextToken("</" + key + '>');
						}
						else if (t.type == XML.ELEMENT_CONTENT)
						{
							p.setProperty(key, t.data);
							parser.nextToken("</" + key + '>');
						}
						else if (t.data.equals("</" + key + '>') == false)
						{
							throw new XMLParserException("Invaid token: " + t.data);
						}
					}
					else if (t.data.equals(propsEnd))
					{
						break;
					}
					else
					{
						throw new XMLParserException("Invaid token: " + t.data);
					}
				}

				count++;
//				debug("Importing record " + count);
				database.addProps(p);
			}
			else if (t.data.equals(containerEnd))
			{
				break;
			}
			else
			{
				throw new XMLParserException("Invaid token: " + t.data);
			}
		}

		parser.close();

		return (count);
	}
}

