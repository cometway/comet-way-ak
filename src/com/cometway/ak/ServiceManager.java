
package com.cometway.ak;


import java.util.HashMap;
import java.util.List;
import java.util.Vector;


/**
* Reference implementation of the ServiceManagerInterface.
*/

public class ServiceManager extends Agent
{
	protected ServiceManagerInterface sm;


	/**
	* Starts the service manager.
	*/

	public void start()
	{
		sm = AK.getDefaultServiceManager();

		if (sm == null)
		{
			sm = new ServiceManagerImpl();

			AK.setDefaultServiceManager(sm);
		}
		else
		{
			sm = null;

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



	public class ServiceManagerImpl implements ServiceManagerInterface
	{
		private HashMap services = new HashMap();


		/**
		* Used to evaluate authentication; always returns true.
		*/
	
		protected boolean authenticate(Object authorization)
		{
			return (true);
		}
	
	
		/**
		* Returns an implementation for the specified service name if one exists; null otherwise.
		* Authorization is ignored.
		*/
	
		public Object getServiceImpl(String serviceName, Object authorization)
		{
			Object  o = null;
	
			if (authenticate(authorization))
			{
				o = services.get(serviceName);

				if (o == null)
				{
					debug("Not found: " + serviceName + " " + getDateTimeStr());
				}
				else
				{
					debug("Requested: " + serviceName + " " + getDateTimeStr());
				}
			}
			else
			{
				error("Authentication failed for " + authorization.toString() + " (" + serviceName + ") " + getDateTimeStr());
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
	
		public boolean registerService(String serviceName, Object serviceImpl, Object authorization)
		{
			boolean success = authenticate(authorization);
	
			if (success)
			{
				services.put(serviceName, serviceImpl);

				println("Registered " + serviceImpl.getClass().getName() + " as " + serviceName);
			}
			else
			{
				error("Authorization failed for " + authorization.toString() + " (" + serviceName + ") " + getDateTimeStr());
			}

			return (success);
		}
	
	
		/**
		* Unregisters the specified service implementation under the specified service name.
		* Authorization is ignored.
		* Returns true if successful; false otherwise.
		*/
	
		public boolean unregisterService(String serviceName, Object serviceImpl, Object authorization)
		{
			boolean success = false;
	
			if (authenticate(authorization))
			{
				Object o = services.get(serviceName);

				if (o == null)
				{
					warning("Cannot unregister unknown service: " + serviceName + " " + getDateTimeStr());
				}
				else if (o != serviceImpl)
				{
					warning("Cannot unregister service implementation: " + serviceName + " " + getDateTimeStr());
				}
				else if (o == serviceImpl)
				{
					services.remove(serviceName);

					println("Unregistered: " + serviceName + " " + getDateTimeStr());
	
					success = true;
				}
			}
			else
			{
				error("Authorization failed for " + authorization.toString() + " (" + serviceName + ") " + getDateTimeStr());
			}

			return (success);
		}
	}


	/* Static methods for accessing the service manager instance. */


	/**
	* Returns a reference to the service manager.
	*/

	public static ServiceManagerInterface getServiceManager()
	{
		return (AK.getDefaultServiceManager());
	}


	/**
	* Sets the service manager returned by getServiceManager.
	*/

	public static void setServiceManager(ServiceManagerInterface serviceManager)
	{
		AK.setDefaultServiceManager(serviceManager);
	}


	/**
	* Returns an implementation for the specified service name; null if none found.
	*/

	public static Object getService(String serviceName)
	{
		ServiceManagerInterface sm = AK.getDefaultServiceManager();

		if (sm == null)
		{ 
			throw new RuntimeException("Service Manager is not loaded");
		}

		return (sm.getServiceImpl(serviceName, null));
	}


	/**
	* Returns an implementation for the specified service name if the
	* service was found, and the authorization was accepted; null otherwise.
	*/

	public static Object getService(String serviceName, Object authorization)
	{
		ServiceManagerInterface sm = AK.getDefaultServiceManager();

		if (sm == null)
		{ 
			throw new RuntimeException("Service Manager is not loaded");
		}

		return (sm.getServiceImpl(serviceName, authorization));
	}


	/**
	* Registers a service implementation using the specified service name
	* and returns true if successful; false otherwise.
	*/

	public static boolean register(String serviceName, Object serviceImpl)
	{
		ServiceManagerInterface sm = AK.getDefaultServiceManager();

		if (sm == null)
		{ 
			throw new RuntimeException("Service Manager is not loaded");
		}

		return (sm.registerService(serviceName, serviceImpl, null));
	}


	/**
	* Registers a service implementation using the specified service name
	* and if authorization is accepted returns true; returns false otherwise.
	*/

	public static boolean register(String serviceName, Object serviceImpl, Object authorization)
	{
		ServiceManagerInterface sm = AK.getDefaultServiceManager();

		if (sm == null)
		{ 
			throw new RuntimeException("Service Manager is not loaded");
		}

		return (sm.registerService(serviceName, serviceImpl, authorization));
	}


	/**
	* Unregisters a service implementation using the specified service name
	* and returns true if successful; returns false otherwise.
	*/

	public static boolean unregister(String serviceName, Object serviceImpl)
	{
		ServiceManagerInterface sm = AK.getDefaultServiceManager();

		if (sm == null)
		{ 
			throw new RuntimeException("Service Manager is not loaded");
		}

		return (sm.unregisterService(serviceName, serviceImpl, null));
	}


	/**
	* Unregisters a service implementation using the specified service name
	* and if authorization is accepted returns true; returns false otherwise.
	*/

	public static boolean unregister(String serviceName, Object serviceImpl, Object authorization)
	{
		ServiceManagerInterface sm = AK.getDefaultServiceManager();

		if (sm == null)
		{ 
			throw new RuntimeException("Service Manager is not loaded");
		}

		return (sm.unregisterService(serviceName, serviceImpl, authorization));
	}
}

