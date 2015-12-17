package com.cometway.httpd;

import java.net.*;
import java.io.*;

import com.cometway.util.*;

/**
 * This KMethod checks a web server by sending it a request for a URL.
 * After the KMethod has been executed, the connection, request, and 
 * request latency is recorded in public fields. This method requires a
 * host, port and request. An optional timeout field can be set if needed.
 */
public class HTTPCheckKMethod extends KMethod
{
	/** A connection error occured */
	public static final int CONNECT_ERROR = 0;
	/** The connection to the host and port was refused */
	public static final int CONNECTION_REFUSED = 1;
	/** No route was available to the host and port */
	public static final int NO_ROUTE_TO_HOST = 2;
	/** The host was unknown, there was no dns entry for this host */
	public static final int UNKNOWN_HOST = 3;
	/** The connection to the host and port was established */
	public static final int CONNECTED = 4;

	/** A request error occured after connecting. An error may have occured on the server */
	public static final int REQUEST_ERROR = 0;
	/** A request was successful and a page was returned */
	public static final int REQUEST_SUCCESSFUL = 1;
	/** A request timed out. The server did not respond in time */
	public static final int REQUEST_TIMEOUT = 2;
	/** A request was successful but an empty page was returned */
	public static final int REQUESTED_EMPTY_PAGE = 3;
	/** A request was successful but the server returned a redirect */
	public static final int REQUEST_REDIRECTED = 4;
	/** A request was successful but the page was not found, this is a 404 HTTP error */
	public static final int REQUEST_NOT_FOUND = 5;
	/** A request was successful but the server denied access */
	public static final int REQUEST_DENIED = 6;

	/** This is set after the method has been executed and will contain the connection result */
	public int connectionResult;
	/** This is set after the method has been executed and will contain the request result */
	public int requestResult;
	/** This is set after the method has been executed and will contain the latency of the request */
	public long requestLatency = -1;
	/** This is set after the method has been executed and will contain the page returned from the server */
	public String requestPage;

	public String request;
	public String host;
	public int port;
	/** Set this variable if you care about the timeout (in milliseconds), default is 60 seconds */
	public int timeout = 60000;

	/**
	* Sets host, port, and request fields.
	*/

	public HTTPCheckKMethod(String host, int port, String request)
	{
		this.host = host;
		this.port = port;
		this.request = request;
	}

	/**
	* Returns a String containing connection, request, and latency information.
	*/

	public String toString()
	{
		StringBuffer rval = new StringBuffer();

		rval.append("Checking: http://"+host+":"+port+request+"\n");
		rval.append("timeout: "+timeout+"\n");
		if(connectionResult == CONNECTED) {
			rval.append("connection: CONNECTED\n");
		}
		else if(connectionResult == CONNECTION_REFUSED) {
			rval.append("connection: CONNECTION REFUSED\n");
		}
		else if(connectionResult == NO_ROUTE_TO_HOST) {
			rval.append("connection: No Route To Host\n");
		}
		else if(connectionResult == UNKNOWN_HOST) {
			rval.append("connection: Unknown host\n");
		}
		else if(connectionResult == CONNECT_ERROR) {
			rval.append("connection: CONNECTION ERROR\n");
		}
		if(requestResult == REQUEST_ERROR) {
			rval.append("request: REQUEST ERROR\n");
		}
		else if(requestResult == REQUEST_SUCCESSFUL) {
			rval.append("request: REQUEST SUCCESSFUL\n");
		}
		else if(requestResult == REQUEST_TIMEOUT) {
			rval.append("request: REQUEST TIMED OUT\n");
		}
		else if(requestResult == REQUESTED_EMPTY_PAGE) {
			rval.append("request: Requested an empty page\n");
		}
		else if(requestResult == REQUEST_REDIRECTED) {
			rval.append("request: Requested a redirect\n");
		}
		else if(requestResult == REQUEST_NOT_FOUND) {
			rval.append("request: URL NOT FOUND\n");
		}
		else if(requestResult == REQUEST_DENIED) {
			rval.append("request: Access Denied\n");
		}
		if(requestLatency!=-1) {
			rval.append("latency: "+requestLatency+"\n");
		}
		if(requestPage!=null) {
			rval.append("page length: "+requestPage.length()+"\n");
		}

		return(rval.toString());
	}

	/**
	* Tests website for connection, request, and latency information.
	*/

	public void execute()
	{
		Socket socket = null;
		BufferedWriter out = null;
		BufferedReader in = null;
		try {
			socket = new Socket(host,port);
			socket.setSoTimeout(timeout);

			try {
				out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				out.write("GET "+request+" HTTP/1.0\nConnection: close\n\n");
				out.flush();

				requestLatency = System.currentTimeMillis();

				StringBuffer response = new StringBuffer();
				String line = in.readLine();
				String resultCode = null;
				boolean header = true;
				while(line!=null) {
					if(header) {
						if(line.startsWith("HTTP")) {
							String temp = line.substring(line.indexOf(" "));
							if(temp.indexOf(" ")>0) {
								temp = temp.substring(0,temp.indexOf(" "));
							}
							resultCode = temp.trim();
						}						
						else if(line.equals("")) {
							header = false;
						}
					}
					else {
						response.append(line+"\n");
					}
					line = in.readLine();
				}

				requestPage = response.toString();

				if(resultCode==null) {
					requestResult = REQUEST_ERROR;
				}
				else if(resultCode.startsWith("2")) {
					if(response.toString().trim().length()>0) {
						requestResult = REQUEST_SUCCESSFUL;
					}
					else {
						requestResult = REQUESTED_EMPTY_PAGE;
					}
				}
				else if(resultCode.startsWith("3")) {
					requestResult = REQUEST_REDIRECTED;
				}
				else if(resultCode.startsWith("4")) {
					if(resultCode.startsWith("404")) {
						requestResult = REQUEST_NOT_FOUND;
					}
					else {
						requestResult = REQUEST_DENIED;
					}
				}
				else {
					requestResult = REQUEST_ERROR;
				}

				requestLatency = System.currentTimeMillis() - requestLatency;
			}
			catch(InterruptedIOException iie) {
				requestResult = REQUEST_TIMEOUT;
			}
			catch(Exception e) {
				requestResult = REQUEST_ERROR;
			}

			connectionResult = CONNECTED;
		}
		catch(java.net.ConnectException ex1) {
			connectionResult = CONNECTION_REFUSED;
		}
		catch(java.net.NoRouteToHostException ex2) {
			connectionResult = NO_ROUTE_TO_HOST;
		}
		catch(java.net.UnknownHostException ex4) {
			connectionResult = UNKNOWN_HOST;
		}
		catch(Exception e) {
			e.printStackTrace();
			connectionResult = CONNECT_ERROR;
		}

		if(out!=null) {
			try {
				out.close();
			}
			catch(Exception e) {;}
		}		
		if(in!=null) {
			try {
				in.close();
			}
			catch(Exception e) {;}
		}
		if(socket!=null) {
			try {
				socket.close();
			}
			catch(Exception e) {;}
		}
	}
}
