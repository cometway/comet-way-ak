package com.cometway.net;

import java.util.*;



/**
 * This class is a data object which stores a HTTP/1.1 response.
 * This class is used by the HTTPLoader
 */
public class HTTPResponse
{


/**
 * * These are the result codes supported
 */

	public static final String      CODE_200 = "200 OK";
	public static final String      CODE_201 = "201 Created";
	public static final String      CODE_204 = "204 No Content";
	public static final String      CODE_301 = "301 Permanantly Moved";
	public static final String      CODE_302 = "302 Temporarily Moved";
	public static final String      CODE_400 = "400 Bad Request";
	public static final String      CODE_401 = "401 Unauthorized";
	public static final String      CODE_403 = "403 Forbidden";
	public static final String      CODE_404 = "404 URL not found";
	public static final String      CODE_500 = "500 Server Error";


	/**
	 * * This is a hashtable of all the headers in the HTTP response from the web server
	 */

	public Hashtable		headers;


	/**
	 * * This is the result code of the response
	 */

	public String			resultCode;


	/**
	 * * This is the location of the redirect when the result code is 30*
	 */

	public String			redirectLocation;


	/**
	 * * This is the data that was stored in the body of the HTTP response
	 */

	public String			data;

	public HTTPResponse()
	{
		;
	}


	/**
	 * * Use this method to get the header value(s) for a given header name.
	 */


	public String[] getHeader(String headerName)
	{
		String[]	rval = null;

		headerName = headerName.toLowerCase();

		try
		{
			if (headers != null)
			{
				Object  o = headers.get(headerName);

				if (o != null)
				{
					if (o instanceof String)
					{
						rval = new String[1];
						rval[0] = (String) o;
					}
					else if (o instanceof Vector)
					{
						Vector  values = (Vector) o;

						rval = new String[values.size()];

						for (int x = 0; x < rval.length; x++)
						{
							rval[x] = (String) values.elementAt(x);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return (rval);
	}


	/**
	 * * Use this method to add a header value for a given header name.	 * If the header name already has a value, the values are stored in	 * a Vector and added to the hashtable under the header name.
	 */


	public void addHeader(String headerName, String headerValue)
	{
		if (headers == null)
		{
			headers = new Hashtable();
		}

		headerName = headerName.toLowerCase();

		try
		{
			if (headers.containsKey(headerName))
			{
				Object  o = headers.get(headerName);

				if (o instanceof String)
				{
					Vector  v = new Vector();

					v.addElement(o);
					v.addElement(headerValue);
					headers.put(headerName, v);
				}
				else if (o instanceof Vector)
				{
					Vector  v = (Vector) o;

					v.addElement(headerValue);
				}
			}
			else
			{
				headers.put(headerName, headerValue);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}


}

