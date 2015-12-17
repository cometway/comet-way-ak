
package com.cometway.email;

import com.cometway.net.*;
import com.cometway.util.*;
import java.util.*;
import java.net.*;
import com.cometway.ak.*;


/**
* This agent receives SMTP messages on a server socket (port 25 by default)
* and forwards them to RequestAgents registered to the Service Manager using
* the recipients email address (ie: name@domain) or email account
* name only (ie: name). On UNIX operating systems, root access is required
* to serve on port 25. You will also need to kill sendmail or any other SMTP
* servers that may already be using port 25. It may also be necessary to
* add a MX DNS entry for your server in order to allow SMTP messages to be
* routed properly to your server (read "DNS & BIND" from O'Reilly publishing
* more a good reference on DNS).
*/

public class ReceiveEmailAgent extends Agent implements Runnable
{
	protected Vector	receivers;
	private Thread		runThread;
	protected Object	waitSync;

	/**
	* Sets the default Props for this agent:
	* "bind_port" is the port this agent opens to listen for SMTP connections,
	* "max_connections" is the maximum number of simultaneous SMTP connections
	* this agent allows.
	*/

	public void initProps()
	{
                setDefault("bind_port", "25");
                setDefault("max_connections", "5");
	}


	/**
	* Starts this agent by spawning a thread that listens for SMTP connections
	* and passes them to REceiveEmailHandlers for further processing.
	*/

	public void start()
	{
		receivers = new Vector();
		waitSync = new Object();

		runThread = new Thread(this);
		runThread.start();
	}


	/**
	* Opens a server socket on the bind_port and listens for SMTP connections.
	* New socket connections are passed to handleClient for further processing.
	*/

	public void run()
	{
                int bind_port = getInteger("bind_port");
                
		ServerSocket    socket = null;
		Socket		client = null;

		try
		{
			socket = new ServerSocket(bind_port);

                        println("Listening on port " + bind_port + " for SMTP");

                        /* This loop should be suspended when stop is called. */

			while (true)
			{
				client = socket.accept();

				if (client != null)
				{
					handleClient(client);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		println("ReceiveEmailAgent Stopping");
	}


	/**
	* If the max_connections parameter has been not been exceeded, this method
	* passes the socket connection to a ReceiveEmailHandler for further processing
	* on its own Thread (owned by ESMTPReceiver).
	*/

	public void handleClient(Socket client)
	{
		//debug("NUM HANDLERS = " + receivers.size());

                int max_connections = getInteger("max_connections");

		if (receivers.size() >= max_connections)
		{
			try
			{
				synchronized (waitSync)
				{
					warning("Waiting for free connection handler.");

					waitSync.wait(10000);
				}
			}
			catch (Exception e)
			{
				warning("handleClient", e);
			}

			if (receivers.size() >= max_connections)
			{
				// Screw the client, something's wrong with the server

				try
				{
					client.close();
				}
				catch (Exception e)
				{
					warning("Maximum number of connections (" + max_connections + ") reached", e);
				}

				return;
			}
		}

		receivers.addElement(new ReceiveEmailHandler(client, this));
	}


	/**
	* This method is called when the ReceiveEmailHandler is no longer needed.
	*/

	public void returnHandler(ReceiveEmailHandler handler)
	{
		receivers.removeElement(handler);

		synchronized (waitSync)
		{
			waitSync.notifyAll();
		}
	}
}

