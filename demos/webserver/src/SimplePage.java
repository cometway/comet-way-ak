
import com.cometway.ak.*;
import com.cometway.httpd.*;
import com.cometway.util.*;


public class SimplePage extends RequestAgent
{
	public void initProps()
	{
		setDefault("service_name", "/simple.agent");
		setDefault("title", "SimplePage");
		setDefault("body", "This is output from the SimplePage agent.");
	}

	public void handleRequest(AgentRequest request)
	{
		String title = getString("title");
		String body = getString("body");

		request.println("<HTML>\n<HEAD>");
		request.println("<TITLE>" + title + "</TITLE>");
		request.println("</HEAD>\n<BODY>");
		request.println("<H1>" + title + "</H1>");
		request.println("<P>" + body + "</P>");
		request.println("</BODY>\n</HTML>");
	}
}



