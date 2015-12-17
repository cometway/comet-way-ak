
package com.cometway.ak;


/**
* Abstract agent that automatically registers with the service manager
* using the service name specified by the <I>service_name</I> property.
*/

public abstract class ServiceAgent extends Agent
{
	/**
	* Registers this instance with the service manager using the service_name property.
	*/

	public void start()
	{
		register();
	}


	/**
	* Unregisters this instance with the service manager using the service_name property.
	*/

	public void stop()
	{
		unregister();
	}
}

