
package com.cometway.ak;


import com.cometway.props.Props;
import com.cometway.props.PropsList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;


/**
* Reference implementation of the ServiceManagerInterface.
* There's nothing implicitly "secure" about this implementation.
* This agent will generate HTML detailing its registered directory if
* service_name is setup (default: /admin/services.agent).
*/

public class SecureServiceManager extends RequestAgent
{
	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd HHmmss.SSS");

	protected HashMap services = new HashMap();
	protected ServiceManagerInterface sm;


	public void initProps()
	{
		setDefault("service_name", "/admin/services.agent");
	}


	/**
	* Starts the service manager.
	*/

	public void start()
	{
		sm = AK.getDefaultServiceManager();

		if (sm == null)
		{
			sm = new SecureServiceManagerImpl();

			AK.setDefaultServiceManager(sm);

			register();
		}
		else
		{
			unregister();

			String message = "A ServiceManager instance is already running.";

			error(message);

			throw new RuntimeException(message);
		}
	}


	/**
	* Stops the service manager.
	*/

	public void stop()
	{
		AK.setDefaultServiceManager(null);

		sm = null;

		println("Stopped");
	}


	/**
	* This agent implements and registers itself as a RequestAgent that generates an
	* HTML list of currently registered services and other stats. This is a useful
	* agent when you are developing agents for use with com.cometway.httpd.Webserver.
	*/

	public void handleRequest(AgentRequest request)
	{
		String filter = request.getTrimmedString("filter");

		// Render the list as HTML

		request.println("<HTML>\n<HEAD>");
		request.println("<TITLE>SecureServiceManager</TITLE>");
		request.println("</HEAD>\n<BODY BGCOLOR='DDDDDD'>");
		request.println("<TABLE BORDER='1' CELLSPACING='0' CELLPADDING='4' WIDTH='100%'>");
		request.println("<TR><TH BGCOLOR='BBBBBB'>Service Name</TH><TH BGCOLOR='BBBBBB'>Description</TH><TH BGCOLOR='BBBBBB'>Class Name</TH><TH BGCOLOR='BBBBBB'>Registered</TH><TH BGCOLOR='BBBBBB'>Last Accessed</TH><TH BGCOLOR='BBBBBB'>Access Count</TH></TR>");

		List v = getServiceList();
		int count = v.size();

		for (int i = 0; i < count; i++)
		{
			Props p = (Props) v.get(i);
			String service_name = p.getString("service_name");
			String description = p.getString("description");
			String classname = p.getString("classname");
			String registered = p.getString("registered");
			String last_accessed = p.getString("last_accessed");
			String access_count = p.getString("access_count");

			if ((filter.length() > 0) && (service_name.indexOf(filter) == -1)) continue;

			request.println("<TR><TD>" + service_name + "</TD><TD>" + description + "</TD><TD>" + classname + "</TD><TD>" + registered + "</TD><TD>" + last_accessed + "</TD><TD>" + access_count + "</TD></TR>");
		}

		request.println("</TABLE>");
		request.println("</BODY>");
		request.println("</HTML>");
	}


	/**
	* Returns a List containing the Props of currently registered services.
	*/

	protected List getServiceList()
	{
		Vector v = new Vector();

		Set keySet = services.keySet();
		Iterator i = keySet.iterator();

		while (i.hasNext())
		{
			String service_name = (String) i.next();
			Props p = (Props) services.get(service_name);
			v.add(p);
		}

		PropsList.sortPropsList(v, "service_name");

		return (v);
	}


	/**
	* Returns a string with the current time using SimpleDateFormat("yyyyMMdd HHmmss.SSS").
	*/

	public static String getTimeStamp()
	{
		return (SDF.format(new Date()));
	}


	public class SecureServiceManagerImpl implements ServiceManagerInterface
	{
		/**
		* Used to evaluate authentication; always returns true.
		*/
	
		protected boolean authenticate(Object authorization)
		{
			if (authorization != null)
			{
				debug("Authenticating: " + authorization);
			}

			return (true);
		}
	
	
		/**
		* Returns an implementation for the specified service name if one exists; null otherwise.
		* Authorization is ignored.
		*/
	
		public Object getServiceImpl(String service_name, Object authorization)
		{
			Object o = null;
	
			if (authenticate(authorization))
			{
				Props p = (Props) services.get(service_name);

				if (p == null)
				{
					debug("Not found: " + service_name + " " + getTimeStamp());
				}
				else
				{
					p.incrementInteger("access_count");
					p.setProperty("last_accessed", getTimeStamp());
					o = p.getProperty("service_impl");

					debug("Requested: " + service_name + " " + getDateTimeStr());
				}
			}
			else
			{
				error("Authentication failed for " + authorization.toString() + " (" + service_name + ") " + getDateTimeStr());
			}
	
			return (o);
		}
	
	
		/**
		 * Returns a List containing matching implementations for the specified service names.
		 */

		public List listServices(String serviceName, Object authorization)
		{
			List v = new Vector();
			Object o = getServiceImpl(serviceName, authorization);

			if (o != null) v.add(o);

			return (v);
		}


		/**
		* Registers the service implementation under the specified service name.
		* Authorization is ignored.
		* Returns true if successful; false otherwise.
		*/
	
		public boolean registerService(String service_name, Object service_impl, Object authorization)
		{
			boolean success = authenticate(authorization);
	
			if (success)
			{
				Props p = new Props();

				p.setProperty("service_name", service_name);
				p.setProperty("service_impl", service_impl);
				p.setProperty("classname", service_impl.getClass().getName());
				p.setProperty("description", service_impl.toString());
				p.setProperty("registered", getTimeStamp());

				services.put(service_name, p);

				println("Registered " + p.getString("classname") + " as " + service_name);
			}
			else
			{
				error("Authorization failed for " + authorization.toString() + " (" + service_name + ") " + getDateTimeStr());
			}

			return (success);
		}
	
	
		/**
		* Unregisters the specified service implementation under the specified service name.
		* Authorization is ignored.
		* Returns true if successful; false otherwise.
		*/
	
		public boolean unregisterService(String service_name, Object service_impl, Object authorization)
		{
			boolean success = false;
	
			if (authenticate(authorization))
			{
				Props p = (Props) services.get(service_name);

				if (p == null)
				{
					warning("Cannot unregister unknown service: " + service_name + " " + getDateTimeStr());
				}
				else if (p.getProperty("service_impl") != service_impl)
				{
					warning("Cannot unregister service implementation: " + service_name + " " + getDateTimeStr());
				}
				else
				{
					services.remove(service_name);

					println("Unregistered: " + service_name + " " + getDateTimeStr());
	
					success = true;
				}
			}
			else
			{
				error("Authorization failed for " + authorization.toString() + " (" + service_name + ") " + getDateTimeStr());
			}

			return (success);
		}
	}
}

