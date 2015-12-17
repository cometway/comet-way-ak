
package com.cometway.ak;


import java.util.List;


/**
 * A standard service manager interface for registering, retrieving, and unregistering service implementations.
 */

public interface ServiceManagerInterface
{
	/**
	 * Registers the specified service implementation under the specified service name.
	 * If the authorization is not null, it can be used by the implementation to authenticate
	 * and/or route service manager requests. Returns true if successful; false otherwise.
	 */

	public boolean registerService(String serviceName, Object serviceImpl, Object authorization);


	/**
	 * Returns an implementation for the specified service name if one exists; null otherwise.
	 */

	public Object getServiceImpl(String serviceName, Object authorization);


	/**
	 * Returns a List containing matching implementations for the specified service names.
	 */

	public List listServices(String serviceName, Object authorization);


	/**
	 * Unregisters the specified service implementation under the specified service name.
	 * If the authorization is not null, it can be used by the implementation to authenticate
	 * and/or route service manager requests. Returns true if successful; false otherwise.
	 */

	public boolean unregisterService(String serviceName, Object serviceImpl, Object authorization);
}

