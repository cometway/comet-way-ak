
package com.cometway.props;


import com.cometway.ak.ServiceAgent;


/**
* This agent creates and registers a PropsList to the service manager.
* It can be extended to provide alternate storage layers,
* or used externally with associated Import and Export agents.
*/

public class PropsListAgent extends ServiceAgent
{
	protected PropsList propsList;


	/**
	* <PRE>Default properties:
	* service_name = database (name this PropsList registers with the Service Manager).</PRE>
	*/

	public void initProps()
	{
		setDefault("service_name", "database");
	}


	/**
	* Creates a PropsChangeListener for this agent and
	* registers with the Service Manager using service_name.
	*/

	public void start()
	{
		String service_name = getTrimmedString("service_name");

		propsList = new PropsList();

		registerService(service_name, propsList);
	}


	/**
	* Unregister this agent with the Service Manager
	* and sets internal state variables to null.
	* All Props references are permanently lost.
	*/

	public void stop()
	{
		String service_name = getTrimmedString("service_name");

		unregisterService(service_name, propsList);

		propsList = null;
	}
}


