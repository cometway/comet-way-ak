
package com.cometway.om;

import com.cometway.ak.*;
import com.cometway.util.*;
import com.cometway.props.*;
import java.util.*;


/**
* The Session Reaper is a Scheduled Agent which periodically checks for expired
* Sessions that were created by the Session Manager. Although this agent has no
* direct interactions with the Session Manager itself, it is aware that session
* information is stored in the Object Manager tagged with a property called
* <TT>timestamp</TT>. Any session object that has a timestamp value (in milliseconds)
* that is greater than the <TT>session_timeout_ms</TT> property is removed
* from the Object Manager. The Session Reaper check for expired objects using
* the specified schedule, Object Manager service, and session object type. 
*/

public class SessionReaper extends ScheduledAgent
{
	/**
	* Initializes the properties for this agent:
	* <DL>
	* <DT>schedule
	* <DD>a schedule which describes how often to check for expired sessions.
	* <DT>om_service_name
	* <DD>the service name of the object manager where session objects are stored.
	* <DT>session_type
	* <DD>the object type that should be used to look for session objects.
	*/

	public void initProps()
	{
		if (!hasProperty("session_timeout_ms"))
		{
			setInteger("session_timeout_ms", 1000 * 60 * 5);
		}

		setDefault("schedule", "between 0:0:0-23:59:59 every 5m");
		setDefault("session_type", "session");
		setDefault("om_service_name", "object_manager");
	}


	/**
	* Called by the Scheduler when it is time to remove expired sessions.
	*/

	public void wakeup()
	{
		IObjectManager  om = null;
		int		reapedCount = 0;
		PropsType       sessionType = new PropsType(getString("session_type"));
		String		serviceName = getString("om_service_name");

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

		println("Reaping inactive sessions from " + serviceName + " (" + sessionType + ")");

		Vector  v = om.listObjects(sessionType);
		int     size = v.size();

		for (int i = 0; i < size; i++)
		{
			ObjectID	id = (ObjectID) v.elementAt(i);
			Props		p = (Props) om.getObject(id);
			Date		prev = (Date) p.getProperty("timestamp");
			Date		now = new Date();
			long		timeout = getInteger("session_timeout_ms");

			if ((now.getTime() - prev.getTime()) > timeout)
			{
				println("Session " + p.getString("id") + " has expired");
				om.removeObject(id);

				reapedCount++;
			}
		}

		println("Sessions reaped: " + reapedCount + " (" + (size - reapedCount) + " active)");
	}


}

