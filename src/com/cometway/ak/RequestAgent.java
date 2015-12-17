
package com.cometway.ak;


/**
* A Request Agent is a Service Agent which implements the <TT>RequestAgentInterface</TT>.
* Extending this class provides the basic functionality which registers the agent
* with the Service Manager upon startup making it available to other agents as
* a request service.
*/

public abstract class RequestAgent extends ServiceAgent implements RequestAgentInterface
{
}

