
package com.cometway.email;


import com.cometway.ak.*;
import com.cometway.net.*;
import com.cometway.props.Props;
import com.cometway.util.*;
import java.net.*;
import java.util.*;


/**
* This class is used by the ReceiveEmailAgent to process threaded SMTP socket connections.
*/

public class ReceiveEmailHandler extends ESMTPReceiver
{
	protected ReceiveEmailAgent server;


	/**
	* Sets up an instance of this class to receive information from the specified
	* socket on behalf of the specified ReceiveEmailAgent.
	*/

	public ReceiveEmailHandler(Socket sock, ReceiveEmailAgent server)
	{
		super(sock);

		this.server = server;
	}


	public void deliverMessage()
	{
		Message m = new Message(data);

		server.debug("DATA = " + data);

		String to = m.getHeaderInfo("to");
		String from = m.getHeaderInfo("from");

		if (to.length() == 0)
		{
			server.error("Could not route message from " + from + "; no \"To\" field specified");
		}
		else
		{
			server.println("Received (" + from + " -> " + to + ")");

			try
			{
				Pair pair=EmailTools.parseEmailAddress(to);

				String name = (String) pair.first();

				RequestAgent    agent = (RequestAgent) ServiceManager.getService(name);

				if (agent == null)
				{
					if (name.indexOf("@") != -1)
					{
						name = name.substring(0, name.indexOf("@"));
						agent = (RequestAgent) ServiceManager.getService(name);

						if (agent == null)
						{
							server.error("Could not route message to " + to);
						}
					}
				}

				if (agent != null)
				{
					Props		p = new Props();
					Enumeration     e = m.getHeaders();

					while (e.hasMoreElements())
					{
						String  headerName = (String) e.nextElement();

						p.setProperty(headerName.toLowerCase(), m.getHeaderInfo(headerName));
					}

					p.setProperty("request_type", "SMTP");
					p.setProperty("request_server_name", socket.getLocalAddress().getHostAddress());
					p.setProperty("request_server_port", server.getProps().getString("bind_port"));
					p.setProperty("request_remote_host", socket.getInetAddress().getHostName());
					p.setProperty("request_remote_addr", socket.getInetAddress().getHostAddress());
					p.setProperty("request_id", "SMTP:" + from);
					p.setProperty("reply_to", from);
					p.setProperty("message", m);

					AgentRequest    request = new AgentRequest(p);

					agent.handleRequest(request);
				}
			}
			catch (Exception e)
			{
				server.error("Could not route message", e);
			}
		}
	}


	public void run()
	{


		// super.run();

		boolean		loop = true;
		String		line = null;
		String		response = null;
		String		command = null;
		ICommandParser  parser = null;


		// Send welcome

		write("220 " + greeting);

		while (loop)
		{
			line = read();

			if (line != null)
			{
				server.debug("READ: " + line);

				line = line.trim();

				int     index = line.indexOf(" ");

				if (index == -1)
				{
					index = line.length();


					// the toUpperCase here is less general

				}

				command = line.substring(0, index).toUpperCase();
				parser = (ICommandParser) commandHash.get(command);

				if (parser != null)
				{
					response = parser.parseCommand(line);
				}
				else
				{
					response = kCommandUnrecognized;
				}

				server.debug("RESPONSE: " + response);
				write(response);


				// handle exceptions here by closing?  Q.

				if ((response.substring(0, 3).indexOf("221") != -1) || (response.substring(0, 3).indexOf("421") != -1))
				{
					loop = false;

					close();
				}
			}
			else
			{


				// XXX try to reinit streams here?

				loop = false;
			}
		}

		server.returnHandler(this);

		server = null;
	}


}

