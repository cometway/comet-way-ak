
package com.cometway.props;


import com.cometway.ak.AK;
import com.cometway.ak.SecureServiceManager;
import com.cometway.ak.ServiceManagerInterface;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;


/**
* Reference implementation of the ServiceManagerInterface.
* There's nothing implicitly "secure" about this implementation.
* This agent will generate HTML detailing its registered directory if
* service_name is setup (default: /admin/services.agent).
*/

public class PropsListServiceManager extends SecureServiceManager
{
	protected PropsList services;
	protected HashMap servicesMap;


	public void initProps()
	{
		setDefault("database_name", "database/services");
		setDefault("service_name", "/admin/services.agent");
	}


	/**
	* Starts the service manager.
	*/

	public void start()
	{
		String database_name = getTrimmedString("database_name");
		boolean publish_services = ((database_name.length() > 0) && (database_name.equals("none") == false));

		sm = AK.getDefaultServiceManager();

		if (sm == null)
		{
			services = new PropsList();
			servicesMap = new HashMap();

			sm = new PropsListServiceManagerImpl();

			AK.setDefaultServiceManager(sm);

			// register the AgentRequestInterface

			register();


			if (publish_services)
			{
				registerService(database_name, services);
			}
		}
		else
		{
			String message = "A ServiceManager instance is already running.";

			error(message);

			throw new RuntimeException(message);
		}
	}


	/**
	* Returns a List containing the Props of currently registered services.
	*/

	protected List getServiceList()
	{
		return (services.listProps());
	}


	public class PropsListServiceManagerImpl implements ServiceManagerInterface
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
			String timestamp = getTimeStamp();
	
			if (authenticate(authorization))
			{
				Props p = (Props) servicesMap.get(service_name);

				if (p == null)
				{
					// If there's nothing in the index, 
					// query the database for regular expression matches.
					// Return the first one that does.

					List v = services.listPropsRegExMatching("service_name", service_name);

					if (v.size() > 0)
					{
						p = (Props) v.get(0);
					}
				}

				if (p == null)
				{
					debug("Not found: " + service_name + " " + timestamp);
				}
				else
				{
					p.incrementInteger("access_count");
					p.setProperty("last_accessed", timestamp);
					o = p.getProperty("service_impl");

					debug("Requested: " + service_name + " " + timestamp);
				}
			}
			else
			{
				error("Authentication failed for " + authorization.toString() + " (" + service_name + ") " + timestamp);
			}
	
			return (o);
		}
	
	
		/**
		* Returns an implementation for the specified service name if one exists; null otherwise.
		* Authorization is ignored.
		*/
	
		public List listServices(String service_name, Object authorization)
		{
			List serviceList = new Vector();
			String timestamp = getTimeStamp();
	
			if (authenticate(authorization))
			{
				List v = services.listPropsRegExMatching("service_name", service_name);
				int count = v.size();

				if (count == 0)
				{
					debug("Not found: " + service_name + " " + timestamp);
				}
				else
				{
					for (int i = 0; i < count; i++)
					{
						Props p = (Props) v.get(i);

						p.incrementInteger("access_count");
						p.setProperty("last_accessed", timestamp);

						Object o = p.getProperty("service_impl");

						serviceList.add(o);
					}

					debug("Requested: " + service_name + " " + timestamp);
				}
			}
			else
			{
				error("Authentication failed for " + authorization.toString() + " (" + service_name + ") " + timestamp);
			}
	
			return (serviceList);
		}
	
	
		/**
		* Registers the service implementation under the specified service name.
		* Authorization is ignored.
		* Returns true if successful; false otherwise.
		*/
	
		public boolean registerService(String service_name, Object service_impl, Object authorization)
		{
			boolean success = authenticate(authorization);
			String timestamp = getTimeStamp();

			if (success)
			{
				Props p = services.createProps();

				p.setProperty("service_name", service_name);
				p.setProperty("service_impl", service_impl);
				p.setProperty("classname", service_impl.getClass().getName());
				p.setProperty("description", service_impl.toString());
				p.setProperty("registered", timestamp);

				servicesMap.put(service_name, p);

				println("Registered " + p.getString("classname") + " as " + service_name);
			}
			else
			{
				error("Authorization failed for " + authorization.toString() + " (" + service_name + ") " + timestamp);
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
			String timestamp = getTimeStamp();
	
			if (authenticate(authorization))
			{
				Props p = (Props) servicesMap.get(service_name);

				if (p == null)
				{
					warning("Cannot unregister unknown service: " + service_name + " " + timestamp);
				}
				else if (p.getProperty("service_impl") != service_impl)
				{
					warning("Cannot unregister service implementation: " + service_name + " " + timestamp);
				}
				else
				{
					services.removeProps("service_name", service_name);
					servicesMap.remove(service_name);

					println("Unregistered: " + service_name + " " + timestamp);
	
					success = true;
				}
			}
			else
			{
				error("Authorization failed for " + authorization.toString() + " (" + service_name + ") " + timestamp);
			}

			return (success);
		}
	}
}

