
package com.cometway.om;

import com.cometway.ak.*;
import com.cometway.props.*;
import com.cometway.util.*;


/**
 * Standard interface for managing session information that is accessed using Props.
 */

public interface SessionManagerInterface
{


	/**
	 * Creates a new session and returns its unique sessionID.
	 */

	public String createSession();


	/**
	 * Returns the Props for the specified session;
	 * null if the session does not exist.
	 */

	public Props getSessionProps(String sessionID);


	/**
	 * Destroys the specified session and returns true;
	 * false if the session did not exist or could not be destroyed.
	 */

	public boolean destroySession(String sessionID);
}

