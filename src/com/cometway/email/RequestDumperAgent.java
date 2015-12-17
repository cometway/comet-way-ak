
package com.cometway.email;

import com.cometway.ak.*;
import com.cometway.util.*;
import com.cometway.props.Props;

/**
* This is a simple agent which dumps the contents of the requests it receives.
* This can be used for debugging messages received by the ReceiveEmailAgent.
*/

public class RequestDumperAgent extends RequestAgent
{
	/**
	* Sets the default value for the "service_name" property.
	*/

	public void initProps()
	{
		setDefault("service_name", "agent@localhost");
	}


	/**
	* Dumps the contents of the AgentRequest Props to the debug method.
	*/

	public void handleRequest(AgentRequest request)
	{
		Props rp = request.getProps();

		debug(rp.toString());
	}
}


