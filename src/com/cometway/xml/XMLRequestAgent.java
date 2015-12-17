
package com.cometway.xml;


import com.cometway.ak.AgentRequest;
import com.cometway.ak.RequestAgent;
import com.cometway.ak.RequestAgentInterface;
import com.cometway.net.HTTPLoader;
import com.cometway.io.StringBufferOutputStream;
import com.cometway.props.Props;
import com.cometway.props.PropsException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Vector;


/**
* This is an abstract RequestAgent with convenience methods for
* producing a streamed (indeterminate length) XML-based response.
*/

public abstract class XMLRequestAgent extends RequestAgent
{
	protected final static String EOL = System.getProperty("line.separator");


	private final static void appendAttributes(StringBuffer b, Props attributes)
	{
		Vector keys = attributes.getKeys();
		int count = keys.size();

		for (int i = 0; i < count; i++)
		{
			String key = (String) keys.elementAt(i);

			b.append(' ');
			b.append(key);
			b.append("=\"");
			b.append(attributes.getString(key));
			b.append("\"");
		}
	}


	/**
	* This method is used to create a Props from XML documents
	* created by the <TT>writeProps()</TT> method.
	*/


	protected Props getResponseProps(String xmlResponse, String enclosingTag) throws XMLParserException
	{
		Props p = new Props();
		XMLParser parser = new XMLParser(xmlResponse);

		String startTag = "<" + enclosingTag + ">";
		String endTag = "</" + enclosingTag + ">";

		parser.nextToken(XML.XML_10_HEADER);
		parser.nextToken(startTag);

		while (true)
		{
			XMLToken t = parser.nextToken();

			if (t.data.equals(endTag))
			{
				break;
			}
			else
			{
				if (t.type == XML.START_TAG)
				{
					String key = t.data.substring(1, t.data.length() - 1);
					t = parser.nextToken();

					if (t.type == XML.ELEMENT_CONTENT)
					{
						p.setProperty(key, t.data);
						parser.nextToken("</" + key + '>');
					}
					else if (t.data.equals("</" + key + '>') == false)
					{
						throw new XMLParserException("Invaid token: " + t.data);
					}
				}
				else
				{
					throw new XMLParserException("Invaid token: " + t.data);
				}
			}
		}

		parser.close();

		return (p);
	}


	/**
	* Calls the specified RequestAgent with the specified request Props.
	* Results returned by the RequestAgent as XML are parsed and returned as Props.
	*/

	protected Props sendAgentRequest(Props rp, String agentName) throws XMLParserException
	{
		String s;

		if (agentName.startsWith("http://"))
		{
			HTTPLoader loader = new HTTPLoader();

			s = loader.postURL(agentName, rp);
		}
		else
		{
			StringBuffer xmlResponse = new StringBuffer();
			StringBufferOutputStream out = new StringBufferOutputStream(xmlResponse);
			AgentRequest request = new AgentRequest(rp, out);
	
			RequestAgentInterface agent = (RequestAgentInterface) getServiceImpl(agentName);
			agent.handleRequest(request);

			s = xmlResponse.toString();
		}

		debug("XML Response from " + agentName + ":\n" + s);

		return (getResponseProps(s, "response"));
	}


	/**
	* Adds the appropriate number of tabs to the request output as
	* kept in the request's <TT>auto_indent_level</TT> property.
	*/

	public final static void writeAutoIndent(AgentRequest request)
	{
		StringBuffer b = new StringBuffer();
		int auto_indent_level = request.getInteger("auto_indent_level");

		for (int i = 0; i < auto_indent_level; i++)
		{
			b.append('\t');
		}

		request.print(b.toString());
	}


	/**
	* Writes the specified start tag and end tag to the request output.
	* Use this when your element has no value to write between starting
	* and ending tags. Note: This is different than writeEmptyElement.
	*/

	public final static void writeElement(AgentRequest request, String name)
	{
		writeAutoIndent(request);
		request.println('<' + name + "></" + name + '>');
	}


	/**
	* Writes the specified start tag, element value,
	* and end tag to the request output.
	*/

	public final static void writeElement(AgentRequest request, String name, String value)
	{
		writeAutoIndent(request);
		request.println('<' + name + '>' + XML.encode(value) + "</" + name + '>');
	}


	/**
	* Writes the specified start tag, attributes, element value,
	* and end tag to the request output.
	*/

	public final static void writeElement(AgentRequest request, String name, String attributes, String value)
	{
		writeAutoIndent(request);
		request.println('<' + name + ' ' + attributes + '>' + XML.encode(value) + "</" + name + '>');
	}
 

	/**
	* Writes the specified start tag, attributes, element value,
	* and end tag to the request output.
	*/

	public final static void writeElement(AgentRequest request, String name, Props attributes, String value)
	{
		StringBuffer b = new StringBuffer();
		b.append('<');
		b.append(name);
		appendAttributes(b, attributes);
		b.append('>');
		b.append(XML.encode(value));
		b.append("</");
		b.append(name);
		b.append('>');

		writeAutoIndent(request);
		request.println(b.toString());
	}
 

	/**
	* Writes the specified empty element tag to the request output,
	* inserting the Props as attributes within the tag.
	*/

	public final static void writeEmptyElement(AgentRequest request, String name)
	{
		writeAutoIndent(request);
		request.println('<' + name + "/>");
	}


	/**
	* Writes the specified empty element tag to the request output,
	* inserting the attributes within the tag.
	*/

	public final static void writeEmptyElement(AgentRequest request, String name, String attributes)
	{
		StringBuffer b = new StringBuffer();
		b.append('<');
		b.append(name);
		b.append(' ');
		b.append(attributes);
		b.append("/>");

		writeAutoIndent(request);
		request.println(b.toString());
	}


	/**
	* Writes the specified empty element tag to the request output,
	* inserting the Props as attributes within the tag.
	*/

	public final static void writeEmptyElement(AgentRequest request, String name, Props attributes)
	{
		StringBuffer b = new StringBuffer();
		b.append('<');
		b.append(name);
		appendAttributes(b, attributes);
		b.append("/>");

		writeAutoIndent(request);
		request.println(b.toString());
	}


	/**
	* Writes the specified start tag to the request output.
	*/

	public final static void writeStartTag(AgentRequest request, String name)
	{
		writeAutoIndent(request);		
		request.println('<' + name + '>');
		request.incrementInteger("auto_indent_level");
	}


	/**
	* Writes the specified start tag to the request output, inserting the
	* attributes within the tag.
	*/

	public final static void writeStartTag(AgentRequest request, String name, String attributes)
	{
		StringBuffer b = new StringBuffer();
		b.append('<');
		b.append(name);
		b.append(' ');
		b.append(attributes);
		b.append('>');

		writeAutoIndent(request);		
		request.println(b.toString());
		request.incrementInteger("auto_indent_level");
	}


	/**
	* Writes the specified start tag to the request output, inserting the Props
	* as attributes within the tag.
	*/

	public final static void writeStartTag(AgentRequest request, String name, Props attributes)
	{
		StringBuffer b = new StringBuffer();
		b.append('<');
		b.append(name);
		appendAttributes(b, attributes);
		b.append('>');

		writeAutoIndent(request);		
		request.println(b.toString());
		request.incrementInteger("auto_indent_level");
	}


	/**
	* Writes the specified close tag to the request output.
	*/

	public final static void writeEndTag(AgentRequest request, String name)
	{
		request.decrementInteger("auto_indent_level");
		writeAutoIndent(request);		
		request.println("</" + name + '>');
	}


	/**
	* Writes XML elements for the specified Props using the keys as tag names
	* and the values as element content for each key.
	*/

	public final static void writeProps(AgentRequest request, Props p, String enclosingTag)
	{
		writeStartTag(request, enclosingTag);

		Vector keys = p.getKeys();
		int count = keys.size();

		for (int i = 0; i < count; i++)
		{
			String key = (String) keys.elementAt(i);
			writeElement(request, key, p.getString(key));
		}

		writeEndTag(request, enclosingTag);
	}


	/**
	* Saves a Vector of Props to a XML file.
	*/

	public final static void savePropsToXMLFile(Props p, String filename, String enclosingTag) throws PropsException
	{
		if (filename.length() == 0)
		{
			throw new PropsException("Zero length filename");
		}

		String tempfile = filename + ".temp";
//System.out.println("*** Saving Props to " + tempfile);

		File file = new File(filename);
		File temp = new File(tempfile);
		BufferedWriter out = null;

		try
		{
			out = new BufferedWriter(new FileWriter(temp));

			out.write(XML.XML_10_HEADER);
			out.write(EOL);

			out.write('<');
			out.write(enclosingTag);
			out.write('>');
			out.write(EOL);

			Vector keys = p.getKeys();
			int count = keys.size();
	
			for (int i = 0; i < count; i++)
			{
				String key = (String) keys.elementAt(i);

				out.write("\t<");
				out.write(key);
				out.write('>');
				out.write(XML.encode(p.getString(key)));
				out.write("</");
				out.write(key);
				out.write('>');
				out.write(EOL);
			}

			out.write("</");
			out.write(enclosingTag);
			out.write('>');
			out.write(EOL);

			// Make sure our stream is closed so others can change this file.
			// Windows 2K is very strict about file locking.

			out.close();
			out = null;

			if (file.exists())
			{
//System.out.println("*** Deleting: " + file);
				boolean result = file.delete();
// This always returns false under Windows 2K...how useful is that?

//System.out.println("*** Could not delete " + file + " for some reason");
			}

			temp.renameTo(file);

//System.out.println("*** Saved Props to " + file);
		}
		catch (Exception e)
		{
			throw new PropsException("Could not save: " + filename, e);
		}
		finally
		{
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (Exception e2)
				{
					System.err.println("could not save: " + filename);
					e2.printStackTrace(System.err);
				}
			}
		}
	}

	protected Props loadPropsFromXMLFile(String filename, String enclosingTag) throws PropsException
	{
		Props p = null;

		try
		{
			File file = new File(filename);
			FileInputStream in = new FileInputStream(file);

			p = new Props();
			XMLParser parser = new XMLParser(in);
	
			String startTag = "<" + enclosingTag + ">";
			String endTag = "</" + enclosingTag + ">";
	
			parser.nextToken(XML.XML_10_HEADER);
			parser.nextToken(startTag);
	
			while (true)
			{
				XMLToken t = parser.nextToken();
	
				if (t.data.equals(endTag))
				{
					break;
				}
				else
				{
					if (t.type == XML.START_TAG)
					{
						String key = t.data.substring(1, t.data.length() - 1);
						t = parser.nextToken();
	
						if (t.type == XML.ELEMENT_CONTENT)
						{
							p.setProperty(key, t.data);
							parser.nextToken("</" + key + '>');
						}
						else if (t.data.equals("</" + key + '>') == false)
						{
							throw new XMLParserException("Invaid token: " + t.data);
						}
					}
					else
					{
						throw new XMLParserException("Invaid token: " + t.data);
					}
				}
			}

			// Make sure our stream is closed so others can change this file.
			// Windows 2K is very strict about file locking.

			//			in.close();
			parser.close();

			return (p);
		}
		catch (Exception e)
		{
			throw new PropsException("Could not parse: " + filename, e);
		}
	}


	/**
	* Writes the <TT>&lt;?xml version="1.0"?&gt; identifier,
	* suitable for the declaration of XML 1.0 format compatibility.
	*/

	public final static void writeXMLHeader(AgentRequest request)
	{
		request.println("<?xml version=\"1.0\"?>");
	}
}


