
import com.cometway.ak.*;
import com.cometway.httpd.*;
import com.cometway.util.*;


public class SnoopPage extends RequestAgent
{
	public void initProps()
	{
		setDefault("service_name", "/snoop.agent");
	}

	public void handleRequest(AgentRequest request)
	{
		request.println("<HTML>\n<HEAD>");
		request.println("<TITLE>Snoop</TITLE>");
		request.println("</HEAD>\n<BODY>");
		request.println("<H1>Snoop</H1>");
		request.println("<P>Use the AgentRequest.getProps() method to access parameters:</P>");

		try
		{
			HTMLFormWriter w = new HTMLFormWriter(request);

			w.writeProps(request);
		}
		catch (java.io.IOException e)
		{
			error("Could not write Props", e);
		}

		request.println("<P ALIGN='CENTER'>Powered by <A HREF='http://www.cometway.com'>Comet Way</A> Agents</P>");
		request.println("</BODY>\n</HTML>");
	}
}



