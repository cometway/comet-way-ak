
import com.cometway.ak.*;
import com.cometway.httpd.*;
import com.cometway.util.*;


public class PhonePage extends RequestAgent
{
	public void initProps()
	{
		setDefault("service_name", "/phone.agent");
		setDefault("+Comet Way", "412.682.JAVA");
		setDefault("+Pizza", "412.818.1212");
		setDefault("+Car Service", "412.687.8478");
	}

	public void handleRequest(AgentRequest request)
	{
		request.println("<HTML>\n<HEAD>");
		request.println("<TITLE>Phone</TITLE>");
		request.println("</HEAD>\n<BODY>");
		request.println("<H1>Phone</H1>");


		String name = request.getString("name");


		/* If the name was specified, look it up. */

		if (name.length() > 0)
		{
			request.println("<P>");

			String number = getString("+" + name);

			if (number.length() == 0)
			{
				request.println("There was no phone number for " + name + ".");
			}
			else
			{
				request.println("The phone number for " + name + " is: " + number);
			}

			request.println("</P>");
		}


		/* Write the form at the bottom of the page. */

		try
		{
			HTMLFormWriter w = new HTMLFormWriter(request);

			w.writeHeader("/phone.agent");
			w.writeField("Name", "name", 40);
			w.writeSubmitButton("Find", "opcode", "Find a number in the phone directory.");
			w.writeSpace();
			w.writeCaption("<I>Try Comet Way, Pizza, or Car Service</I>");
			w.writeFooter();
		}
		catch (java.io.IOException e)
		{
			request.println("Sorry, there was a problem processing your request.");
			error("Writing form", e);
		}

		request.println("<P ALIGN='CENTER'>Powered by <A HREF='http://www.cometway.com'>Comet Way</A> Agents</P>");
		request.println("</BODY>\n</HTML>");
	}
}



