
package com.cometway.om;

import com.cometway.ak.*;
import com.cometway.props.*;
import com.cometway.util.*;
import java.util.*;


/**
 * Reference implementation of the SessionManagerInterface.
 */

public class SessionManager extends ServiceAgent implements SessionManagerInterface
{
	protected PropsType		sessionType;
	protected IObjectManager	om;


	/**
	 * Initializes this agent's properties by providing default
	 * values for each of the following missing properties:
	 * <UL>
	 * <LI>session_type (default: session)
	 * <LI>service_name (default: session_manager)
	 * <LI>om_service_name (default: object_manager)
	 * </UL>
	 */

	public void initProps()
	{
		setDefault("session_type", "session");
		setDefault("service_name", "session_manager");
		setDefault("om_service_name", "object_manager");
	}


	/**
	 * Obtains a reference to the object manager specified by the
	 * <I>om_service_name</I> property, which it uses to serve session
	 * information using the SessionManagerInterface. After looking up
	 * the object manager, the agent registers itself using the name
	 * specified by the <I>service_name</I> property.
	 */

	public void start()
	{
		String  serviceName = getString("om_service_name");

		if (serviceName.length() > 0)
		{
			om = (IObjectManager) getServiceImpl(serviceName);

			if (om == null)
			{
				error("Cannot locate an IObjectManager named \"" + serviceName + "\"");
			}
		}

		if (om == null)
		{
			throw new RuntimeException("Cannot continue without a valid IObjectManager");
		}

		sessionType = new PropsType(getString("session_type"));

		println("Using " + om.getClass().getName() + " for session storage");
		println("session_type is " + sessionType);
		register();
	}


	/**
	 * Unregisters this agent from the service manager, and invalidate's its
	 * reference to the object manager.
	 */

	public void stop()
	{
		unregister();

		om = null;
	}


	/**
	 * Creates a new session and returns its sessionID.
	 */

	public String createSession()
	{
		String		sessionID = null;
		ObjectID	objectID = om.createObject(sessionType);

		if (objectID != null)
		{
			sessionID = objectID.getID();
		}

		if (objectID != null)
		{
			println("Created new session: " + sessionID);
		}
		else
		{
			error("Could not create a new session.");
		}

		return (sessionID);
	}


	/**
	 * Destroys the specified session and returns true;
	 * false if the session did not exist.
	 */

	public boolean destroySession(String sessionID)
	{
		ObjectID	objectID = new ObjectID(sessionType.getType(), sessionID);
		boolean		result = om.removeObject(objectID);

		if (result)
		{
			println("Session " + sessionID + " destroyed.");
		}
		else
		{
			error("Could not destroy session " + sessionID + ".");
		}

		return (result);
	}


	/**
	 * Returns the Props for the specified session;
	 * false if the session does not exist.
	 */

	public Props getSessionProps(String sessionID)
	{
		ObjectID	objectID = new ObjectID(sessionType.getType(), sessionID);
		Props		p = (Props) om.getObject(objectID);

		if (p != null)
		{
			println("Getting session props: " + sessionID);
			p.setProperty("timestamp", new Date());
		}
		else
		{
			warning("Session " + sessionID + " does not exist.");
		}

		return (p);
	}


	/**
	 * Returns the session manager registered as <I>session_manager</I>.
	 */

	public static SessionManagerInterface getSessionManager()
	{
		return ((SessionManagerInterface) ServiceManager.getService("session_manager"));
	}


	/**
	 * Returns the session manager registered as the specified serviceName.
	 */

	public static SessionManagerInterface getSessionManager(String serviceName)
	{
		return ((SessionManagerInterface) ServiceManager.getService(serviceName));
	}


}

