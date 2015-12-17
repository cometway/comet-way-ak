
package com.cometway.ak;


import com.cometway.props.Props;


/**
* An instance if this interface is created and managed by an
* implementation of AgentKernelInterface as a proxy to the
* AgentInterface it represents. 
*/

public interface AgentControllerInterface
{
	/**
	* Returns the Props for the controlled Agent.
	*/

	public Props getProps();


	/**
	* Starts the controlled Agent and puts it into the
	* RUNNING_STATE.
	*/

	public void start();
	
	/**
	* Starts the controlled Agent and puts it into the
	* STOPPED_STATE.
	*/

	public void stop();
	
	/**
	* Destroys the controlled Agent and puts it into the
	* DESTROYED_STATE. A destroyed agent cannot be restarted.
	*/

	public void destroy();
}


