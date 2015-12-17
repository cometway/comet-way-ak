package com.cometway.httpd;

/**
 * This interface defines an Object that can return the full
 * HTTP response header and body (as HTML), based off of the
 * constants in com.cometway.httpd.WebServer
 */
public interface HTMLResponseInterface
{
	/**
	 * Returns default HTTP response header and body based off of the 
	 * response code constant and include the given keep alive field 
	 * and given extra headers in the header. If the keepAliveField
	 * and/or extraHeaders are null, they are not to be included in the
	 * response header.
	 */
	public String getHTMLByCode(int code, String keepAliveField, String extraHeaders);
}
