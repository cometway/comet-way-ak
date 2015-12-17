
package com.cometway.httpd;

import com.cometway.ak.AgentRequest;
import com.cometway.props.Props;
import java.io.IOException;
import java.util.Enumeration;


/**
* XHTMLFormWriter is a tool for writing simple XHTML-based forms
* to a StringBuffer or AgentRequest output stream. Using this class
* it is relatively simple to create generic forms without worrying
* about the XHTML code.
*/

public class XHTMLFormWriter extends HTMLFormWriter
{
	private StringBuffer buffer;
	private AgentRequest agentRequest;


	/**
	* Use this constructor to write XHTML and CSS forms directly to an AgentRequest.
	*/

	public XHTMLFormWriter(AgentRequest agentRequest)
	{
		super(agentRequest);
	}


	/**
	* Use this constructor to write XHTML forms to a StringBuffer.
	*/

	public XHTMLFormWriter(StringBuffer b)
	{
		super(b);
	}
	
	
	/**
	* Adds a caption to the form output.
	*/

	public void writeCaption(String caption) throws IOException
	{
		println("<TR><TD class=\"formcaption\" colspan=\"2\">" + caption + "</TD></TR>");
	}


	/**
	* Writes a checkbox to the form output.
	*/
	
	public void writeCheckbox(String label, String name, boolean checked) throws IOException
	{
		println("<TR>\n<TD class=\"formlabel\"></TD>");

		if (checked)
		{
			println("<TD class=\"formcheckbox\"><INPUT type=\"CHECKBOX\" name=\"" + encode(name) + "\" CHECKED>");
		}
		else
		{
			println("<TD class=\"formcheckbox\"><INPUT type=\"CHECKBOX\" name=\"" + encode(name) + "\">");
		}

		println("<LABEL for=\"" + name + "\">" + label + "</LABEL></TD>\n</TR>");
	}


	/**
	* Writes the form footer to the output. Call this method last to properly terminate the form XHTML.
	*/
	
	public void writeFooter() throws IOException
	{
		println("</TABLE>\n</FORM>");
	}


	/**
	* Writes the form header to the output. Call this method first to properly begin the form XHTML.
	* This creates a POST type request to the specified submitURL.
	*/
	
	public void writeHeader(String submitURL) throws IOException
	{
		println("<FORM method=\"POST\" action=\"" + submitURL + "\">");
		println("<TABLE class=\"formtable\" width=\"100%\">");
	}


	/**
	* Writes the form header to the output. Call this method first to properly begin the form XHTML.
	* This creates a POST type request to the specified submitURL.
	* Hidden "login_name" and "login_hash" parameters are added to the form for the specified
	* userProps.
	*/
	
	public void writeHeader(String submitURL, Props userProps) throws IOException
	{
		writeHeader(submitURL);
		writeLoginFields(userProps);
	}


	/**
	* Adds a help description over the fields.
	*/

	public void writeHelp(String message) throws IOException
	{
		println("<TR><TD class=\"formlabel\"></TD><TD class='formhelp'>" + message + "</TD></TR>");
	}


	/**
	* Writes appropriate XHTML to the output to add vertical space between form items.  
	*/
	
	public void writeSpace() throws IOException
	{
		println("<TR><TD class=\"formspace\" colspan=\"2\">&nbsp;</TD></TR>");
	}


	/**
	* Writes a standard input field to the form output.
	*/
	
	public void writeField(String label, String name, int size) throws IOException
	{
		writeField(label, name, "", size);
	}


	/**
	* Writes a standard input field to the form output displaying the specified value.
	*/
	
	public void writeField(String label, String name, String value, int size) throws IOException
	{
		println("<TR>");
		writeLabel(label, name);
		println("<TD class=\"formfield\"><INPUT id=\"" + name + "\" name=\"" + name + "\" value=\"" + encode(value) + "\" size=" + size + "></TD>\n</TR>");
	}

	/**
	* Writes a standard input field to the form output displaying the value
	* as retrieved from the Props using the specified name as the Props key.
	*/
	
	public void writeField(String label, String name, Props p, int size) throws IOException
	{
		writeField(label, name, p.getString(name), size);
	}


	/**
	* Writes a hidden input parameter to the form output.
	*/
	
	public void writeHiddenField(String name, String value) throws IOException
	{
		println("<INPUT type=\"HIDDEN\" name=\"" + name + "\" value=\"" + encode(value) + "\">");
	}


	/**
	* Writes a hidden input parameter to the form output
	* as retrieved from the Props using the specified name as the Props key.
	*/
	
	public void writeHiddenField(String name, Props p) throws IOException
	{
		writeHiddenField(name, p.getString(name));
	}


	/**
	* Hidden "login_name" and "login_hash" parameters are added to the form output
	* using the specified name and password.
	*/

	public void writeLoginFields(String name, String password) throws IOException
	{
		writeHiddenField("login_user", name);
		writeHiddenField("login_hash", String.valueOf(password.hashCode()));

	}


	/**
	* Hidden "login_name" and "login_hash" parameters are added to the form output
	* using the "name" and "password" values of the specified Props.
	*/
	
	public void writeLoginFields(Props p) throws IOException
	{
		writeLoginFields(p.getString("name"), p.getString("password"));
	}


	/**
	* Writes a multiline field (TEXTAREA) to the form output.
	*/
	
	public void writeMultilineField(String label, String name, int columns, int rows) throws IOException
	{
		writeMultilineField(label, name, "", columns, rows);
	}


	/**
	* Writes a multiline field (TEXTAREA) to the form output displaying the specified value.
	*/
	
	public void writeMultilineField(String label, String name, String value, int columns, int rows) throws IOException
	{
		if (rows < 3)
		{
			rows = 3;	// Avoids IE 4.01 Mac crashing problem.
		}

		println("<TR>");
		writeLabel(label, name);
		println("<TD class=\"formtextarea\"><TEXTAREA name=\"" + name + "\" cols=\"" + columns + "\" rows=\"" + rows + "\">" + encode(value) + "</TEXTAREA></TD>\n</TR>");
	}


	/**
	* Writes a multiline field (TEXTAREA) to the form output displaying the specified value
	* as retrieved from the Props using the specified name as a key.
	*/
	
	public void writeMultilineField(String label, String name, Props p, int columns, int rows) throws IOException
	{
		writeMultilineField(label, name, p.getString(name), columns, rows);
	}


	/**
	* Writes a password input field to the form output.
	*/

	public void writePassword(String label, String name, int size) throws IOException
	{
		writePassword(label, name, "", size);
	}


	/**
	* Writes a password input field to the form output containing the specified value.
	*/
	
	public void writePassword(String label, String name, String value, int size) throws IOException
	{
		println("<TR>");
		writeLabel(label, name);
		println("<TD class=\"formfield\"><INPUT type=\"password\" name=\"" + name + "\" value=\"" + value + "\" size=" + size + "></TD>\n</TR>");
	}


	/**
	* Writes a table containing the Props keys and associated values to the output.
	* This is useful for debugging the contents of AgentRequest Props.
	*/
	
	public void writeProps(Props p) throws IOException
	{
		Enumeration e = p.enumerateKeys();

		println("<TABLE BORDER>");
		println("<TR>\n<TH>Key<TH>Value<TH>Class");

		while (e.hasMoreElements())
		{
			String	key = (String) e.nextElement();

			print("<TR><TD>");
			print(key);
			print("</TD><TD>");
			print(p.getString(key));
			print("</TD><TD>");
			print(p.getProperty(key).getClass().getName());
			println("</TD></TR>");
		}

		println("</TABLE>");
	}


	/**
	* Writes a reset button and description to the form output that resets changes to the form when pressed.
	*/
	
	public void writeResetButton(String title, String description) throws IOException
	{
		println("<TR>\n<TD class=\"formbutton\"><INPUT type=\"reset\" value=\"" + encode(title) + "\"></TD>");
		println("<TD class=\"formdescription\">" + description + "</TD>\n</TR>");
	}


	/**
	* Writes the footer XHTML for a SELECT to the form output.
	*/
	
	public void writeSelectFooter() throws IOException
	{
		println("</SELECT></TD>\n</TR>");
	}


	/**
	* Writes the header XHTML for a SELECT to the form output to display a popup menu
	* or selection list. The multiple parameter should be set to false for a popup; set it
	* to true to create a selection list. A call to this method
	* is typically followed by multiple calls to writeSelectItem followed by a call
	* to writeSelectFooter to terminate the SELECT.
	*/
	
	public void writeSelectHeader(String label, String name, boolean multiple) throws IOException
	{
		println("<TR>");
		writeLabel(label, name);

		if (multiple)
		{
			println("<TD class=\"formselect\"><SELECT name=\"" + name + "\" multiple>");
		}
		else
		{
			println("<TD class=\"formselect\"><SELECT name=\"" + name + "\">");
		}
	}


	/**
	* Writes a SELECT OPTION to the form output. Calls to this method must be
	* surrounded by calls to writeSelectHeader and writeSelectFooter.
	*/
	
	public void writeSelectItem(String name, String value, boolean selected) throws IOException
	{
		if (selected)
		{
			println("<OPTION value=\"" + value + "\" selected>" + name + "</OPTION>");
		}
		else
		{
			println("<OPTION value=\"" + value + "\">" + name + "</OPTION>");
		}
	}


	/**
	* Writes a SELECT OPTION to the form output. If the name is equal to the currentSelection
	* the item is selected; otherwise it is not selected. Calls to this method must be
	* surrounded by calls to writeSelectHeader and writeSelectFooter.
	*/
	
	public void writeSelectItem(String name, String value, String currentSelection) throws IOException
	{
		writeSelectItem(name, value, currentSelection.equals(value));
	}


	/**
	* Writes a static text field to the form output.
	*/
	
	public void writeStaticField(String label, String value) throws IOException
	{
		println("<TR>");
		writeLabel(label);
		println("<TD class=\"formstatic\"><INPUT readonly value=\"" + encode(value) + "\"></TD>\n</TR>");
	}

	/**
	* Writes a static text field to the form output.
	*/
	
	public void writeStaticField(String label, String name, String value, int size) throws IOException
	{
		println("<TR>");
		writeLabel(label, name);
		println("<TD class=\"formstatic\"><INPUT readonly name=\"" + name + "\" value=\"" + encode(value) + "\" size=" + size + "></TD>\n</TR>");
	}

	/**
	* Writes a submit button and description to the form output.
	*/
	
	public void writeSubmitButton(String title, String name, String description) throws IOException
	{
		println("<TR>\n<TD class=\"formbutton\"><INPUT type=\"submit\" name=" + name + " value=\"" + title + "\"></TD>");
		println("<TD class=\"formdescription\">" + description + "</TD>\n</TR>");
	}


    protected String encode(String in)
    {
		StringBuffer rval = new StringBuffer();
		int index = in.indexOf("&");
		int lastIndex = 0;

		while(index!=-1)
		{
			index++;
			rval.append(in.substring(lastIndex,index));
			rval.append("amp;");
			lastIndex = index;
			index = in.indexOf("&",index);
		}

		rval.append(in.substring(lastIndex));
		in = rval.toString();
		rval = new StringBuffer();
		index = in.indexOf("\"");
		lastIndex = 0;

		while(index!=-1)
		{
			rval.append(in.substring(lastIndex,index));
			index++;
			rval.append("&quot;");
			lastIndex = index;
			index = in.indexOf("\"",index);
		}

		rval.append(in.substring(lastIndex));

		return (rval.toString());
    }


	protected void writeLabel(String title) throws IOException
	{
		if (title.length() > 0)
		{
			println("<TD class=\"formlabel\"><LABEL>" + title + ":</LABEL></TD>");		
		}
		else
		{
			println("<TD class=\"formlabel\"></TD>");		
		}
	}


	protected void writeLabel(String title, String name) throws IOException
	{
		if (title.length() > 0)
		{
			println("<TD class=\"formlabel\"><LABEL for=\"" + name + "\">" + title + ":</LABEL></TD>");		
		}
		else
		{
			println("<TD class=\"formlabel\"></TD>");		
		}
	}
}


