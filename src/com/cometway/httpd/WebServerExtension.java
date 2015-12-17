package com.cometway.httpd;

import com.cometway.ak.ServiceAgent;
import com.cometway.ak.ServiceManager;
import com.cometway.props.PropsListServiceManager;
import com.cometway.util.StringTools;

/**
 * Abstract class representing an extension for the com.cometway.httpd.WebServer class.
 * This class needs to set a 'domains' property  and
 * a 'webserver_service_name' property. It also needs a 'service_name'.
 * The order which extensions are applied is determined by the 'agent_id'.
 * The 'domains' property is a comma separated list of domain names that this extension
 * applies to. If the property is 'all', then all domains are applicable, if it is set
 * to 'default' then the default domain (for example, no multihoming) is used.
 *
 * If a PropsListServiceManager is used, the 'webserver_service_name' and 'domains'
 * does not need to be set, however, the domain and port needs to be in the service_name.
 * for example: 'extension://www.domain.com:80'. However, lists of domains,
 * 'all' and 'default' can no longer be used, but the service_name can be a regular expression
 * that can encompass domains/ports that match the pattern, for example:
 * 'extension://.*domain.com:[0-9]*'.
 */
public abstract class WebServerExtension extends ServiceAgent
{
	/**
	 * This method inherited from com.cometway.ak.ServiceAgent includes functionality to
	 * register itself with the WebServer (temporary).
	 */
	public void start()
	{
		super.start();

		// for now, we need to make sure we're using a PropsListServiceManager or some variant
		// otherwise, we have to register directly with the WebServer
		if(! (ServiceManager.getServiceManager() instanceof com.cometway.props.PropsListServiceManager.PropsListServiceManagerImpl)) {
			WebServer webServer = (WebServer)getServiceImpl(getString("webserver_service_name"));
			String[] domains = StringTools.commaToArray(getString("domains"));
			for(int x=0;x<domains.length;x++) {
				webServer.addExtension(domains[x], getString("agent_id"), this);
			}
		}
	}

	/**
	 * This will be called by the WebServer (specifically the ConnectionKMethod) to handle
	 * an HTTPAgentRequest. If the request has been handled, true is returned and no other
	 * WebServerExtensions will be used.
	 */
	public boolean handleRequest(HTTPAgentRequest request)
	{
		return(false);
	}
}
