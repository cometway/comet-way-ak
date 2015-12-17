

package com.cometway.httpd;


import java.util.*;
import java.io.*;
import java.net.*;

import com.cometway.util.*;

import com.cometway.ak.*;

/**
 * This agent handles proxies to other web servers. A file needs to be provided
 * which lists the full URLs or URIs to proxy and where they should be proxied to.
 * The proxy config file consists of name/value pairs separated by newlines. Lines
 * beginning with the # character are ignored as comments. The value of each 
 * name/value pair must be a full URL. The following are some valid name/value pairs
 * that can be used in the config file:
 * /directory=http://www.domain.com/directory.agent
 * /dir/page.html=http://www.domain2.com/index.html
 * http://www.domain.com/index.html=http://www.domain2.com/index.html
 * http://domain.com/index.html=http://www.domain2.com/index.html
 * http://www.anotherdomain.com/=http://www.domain2.com:8080/
 *
 * Both the name and the value of each name/value pair is assumed to be literal, so
 * they will cnly match the exact path/host of the http request.
 *
 * Loose proxies are ones where the entire path is passed to the proxied host. Loose
 * proxies are denoted as "*=":
 * http://www.domain.com/search/*=http://www.google.com/
 *
 * In this example, http://www.domain.com/search/ will be proxied to http://www.google.com/
 * and http://www.domain.com/search/search?q=help will be proxied to http://www.google.com/search?q=help
 * Essentially, everything after the name in the name/value pair, will be passed onto what 
 * the value is. 
 */

public class HTTPProxyAgent extends WebServerExtension implements Runnable
{
	private ProxyHash proxies;
	protected boolean stopRunning;
	protected Thread thread;
	protected Object syncObject = new Object();
	protected Hashtable socketHash;

	/** 
	*Initializes this agent's properties by providing default
	* values for each of the following missing properties: "service_name"
	* is used to register this agent with the Service Manager
	* (default: http_proxy.agent), "proxy_file" contains a list of the
	* available proxies (default: ./http.proxy), "check_interval"
	* specifies the time to wait before checking a proxy's status 
	* (default: 300000), "initial_wait_time" specifies the time to 
	* wait initially before checking a proxy (default: 60000).
	* The proxy_socket_cache_timeout property denotes how long cached sockets
	* should be considered alive (in milliseconds, default: 1000).
	*/

	public void initProps()
	{
		setDefault("service_name","extension://.*");
		setDefault("webserver_service_name","none");
		setDefault("domains","none");

		setDefault("proxy_file","./http.proxy");
		setDefault("check_interval","0");
		setDefault("initial_wait_time","60000");
		setDefault("proxy_socket_timeout","5000");
		setDefault("proxy_socket_cache_timeout","10000");

		setDefault("modify_proxied_request","true");
	}

	/**
	* Registers service with Object Manager, reads in Proxy File, and starts a
	* new thread for this object.
	*/
	
	public void start()
	{
		readProxyFile();
		socketHash = new Hashtable();

		if(getInteger("check_interval")>0) {
			try {
				thread = new Thread(this);
				thread.start();
			}
			catch(Exception e) {
				//			e.printStackTrace();
				error("Exception while starting thread.",e);
			}
		}

		super.start();
	}


	public void stop()
	{
		// stop the thread
		stopRunning = true;

		// make sure all the sockets are closed
		Enumeration e = socketHash.elements();
		while(e.hasMoreElements()) {
			Socket s = (Socket)e.nextElement();
			try {
				s.close();
			}
			catch(Exception ex) {;}
		}

		thread = null;
		proxies= null;
		socketHash = null;
	}

	/**
	* Reads in Proxy File's information into a database of proxies.
	*/
	
	public void readProxyFile()
	{
		proxies = new ProxyHash();

		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(new File(getString("proxy_file"))));
			String line = in.readLine();
			while(line!=null) {
				if(!line.startsWith("#")) {
					int index = line.indexOf("=");
					if(index!=-1) {
						String lval = line.substring(0,index).trim();
						if(lval.length()>0) {
							proxies.put(lval,line.substring(index+1));
						}
					}
				}
				line = in.readLine();
			}
		}
		catch(Exception e) {
			error("Cannot read HTTP proxy file: "+getString("proxy_file"),e);
			//			e.printStackTrace();
			//			error("",e);
		}					

		try {
			in.close();
		}
		catch(Exception e) {;}

		println(proxies.toString());
	}

	/**
	* Handles a request and routes through appropriate proxy.
	*/
	public boolean handleRequest(HTTPAgentRequest request)
	{
		boolean rval = false;
		String path = request.getProps().getString("path");
		String host = request.getProps().getString("host");
		Socket sock = null;
		String hostname = "";
		int port = 80;

		if(path.equals("/")) {
			path = "";
		}
		else if(path.startsWith("/")) {
			path = path.substring(1);
		}
		if(isProxied(path,host)) {
			String httpRequest = request.getProps().getString("request");
			boolean keepAlive = request.hasProperty(ConnectionKMethod.KEEP_ALIVE_FIELD);
			
			BufferedOutputStream out = null;
			InputStream in = null;
			try {
				// extract the proxy path, the hostname and port
				String proxy = "";
				if(proxies.containsKey("http://"+host+"/"+path)) {
					proxy = proxies.get("http://"+host+"/"+path);
				}
				else if(proxies.containsKey("https://"+host+"/"+path)) {
					proxy = proxies.get("https://"+host+"/"+path);
				}
				else {
					path = "/"+path;
					proxy = proxies.get(path);
				}
				
				int mark1 = proxy.indexOf("://");
				int mark2 = proxy.indexOf("/",mark1+3);
				if(mark2==-1) {
					mark2 = proxy.length();
				}
				int submark = proxy.indexOf(":",mark1+3);
				if((submark!=-1) && (submark<mark2)) {
					hostname = proxy.substring(mark1+3,submark);
					port = Integer.parseInt(proxy.substring(submark+1,mark2));
				}
				else {
					hostname = proxy.substring(mark1+3,mark2);
				}

				// Modify request as needed
				String newPath = proxy.substring(mark2);
				if(newPath.length()==0) {
					newPath = "/";
				}
				mark1 = httpRequest.indexOf("\n");
				if(mark1!=-1) {
					String firstLine = httpRequest.substring(0,mark1);
					String theRest = httpRequest.substring(mark1);
					mark1 = firstLine.indexOf(" ");
					mark2 = firstLine.indexOf(" ",mark1+1);
					if(mark2==-1) {
						mark2 = firstLine.length();
					}
					// check if there are CGI params
					if(firstLine.indexOf("?")!=-1) {
						httpRequest = firstLine.substring(0,mark1+1)+newPath+firstLine.substring(firstLine.indexOf("?"))+theRest;
					}
					else {
						httpRequest = firstLine.substring(0,mark1+1)+newPath+firstLine.substring(mark2)+theRest;
					}
				}
				if(getBoolean("modify_proxied_request")) {
					mark1 = httpRequest.toLowerCase().indexOf("\nhost:");
					if(mark1!=-1) {
						mark1++;
						mark2 = httpRequest.indexOf("\n",mark1);
						if(port!=80) {
							httpRequest = httpRequest.substring(0,mark1)+"Host: "+hostname+httpRequest.substring(mark2);
						}
						else {
							httpRequest = httpRequest.substring(0,mark1)+"Host: "+hostname+":"+port+httpRequest.substring(mark2);
						}
					}
				}
				// Sometimes requests sent by certain browsers don't conform to the \r\n standard
				mark1 = 0;
				mark2 = httpRequest.indexOf("\n",mark1);
				while(mark2!=-1) {
					if(httpRequest.charAt(mark2-1)!='\r') {
						httpRequest = httpRequest.substring(0,mark2)+"\r"+httpRequest.substring(mark2);
						if(mark1 + 1 == mark2) {
								break;
						}
						mark1 = mark2+1;
					}
					else {
						if(mark1 + 2 == mark2) {
							break;
						}
						mark1 = mark2+1;
					}
					mark2 = httpRequest.indexOf("\n",mark1);
				}
				
				// fetch a Socket, it's possible a keep-alive was used before so we should use the same Socket as before
				sock = getProxySocket(hostname,port,httpRequest);
				if(sock==null) {
					try {
						request.getOutputStream().write(WebServer.getHTMLByCode(WebServer.BAD_GATEWAY).getBytes());
						request.getOutputStream().flush();
					}
					catch(Exception e) {;}
				}
				else {
					in = sock.getInputStream();
					out = new BufferedOutputStream(sock.getOutputStream());
					
					byte[] buffer = new byte[1024];
					int bytesRead = 0;
					int contentLength = -1;
					int contentCount = 0;
					StringBuffer headerBuffer = new StringBuffer();
					try {
						bytesRead = in.read(buffer);
					}
					catch(SocketTimeoutException ste) {
						// proxy socket timed out, need to respond
						request.getOutputStream().write(WebServer.getHTMLByCode(WebServer.GATEWAY_TIMEOUT).getBytes());
						request.getOutputStream().flush();
						bytesRead = -1;
					}
					if(bytesRead!=-1) {
						while(bytesRead>0) {
							request.getOutputStream().write(buffer,0,bytesRead);
							request.getOutputStream().flush();
							if(contentLength==-1) {
								headerBuffer.append(new String(buffer,0,bytesRead));
								try {
									((HTTPAgentRequest)request).returnVal = headerBuffer.toString().substring(headerBuffer.indexOf(" "),headerBuffer.indexOf(" ")+4).trim();
									println("Return value is: "+((HTTPAgentRequest)request).returnVal);
								}
								catch(Exception e) {;}
								if(headerBuffer.toString().startsWith("HTTP/1.1 ")) {
									if(headerBuffer.indexOf("\r\n\r\n")!=-1) {
										contentCount = headerBuffer.length() - (headerBuffer.indexOf("\r\n\r\n")+4);
										String tmp = headerBuffer.toString();
										if(tmp.toLowerCase().indexOf("content-length:")!=-1) {
											tmp = tmp.substring(tmp.toLowerCase().indexOf("content-length:")+15).trim();
											contentLength = Integer.parseInt(tmp.substring(0,tmp.indexOf("\n")).trim());
											if(contentCount>=contentLength) {
												break;
											}
										}
										else {
											contentLength=-2;
										}
									}
									else if(headerBuffer.indexOf("\n\n")!=-1) {
										contentCount = headerBuffer.length() - (headerBuffer.indexOf("\n\n")+2);
										String tmp = headerBuffer.toString();
										if(tmp.toLowerCase().indexOf("content-length:")!=-1) {
											tmp = tmp.substring(tmp.toLowerCase().indexOf("content-length:")+15).trim();
											contentLength = Integer.parseInt(tmp.substring(0,tmp.indexOf("\n")).trim());
											if(contentCount>=contentLength) {
												break;
											}
										}
										else {
											contentLength=-2;
										}
									}
								}
								else {
									// Well, dunno what the response is, but we'll read it anyways
									contentLength=-2;
									//									break;
								}
							}
							else if(contentLength>0) {
								contentCount = contentCount + bytesRead;
								if(contentCount>=contentLength) {
									break;
								}
							}
							bytesRead = in.read(buffer);
						}
						if(keepAlive && headerBuffer.toString().toLowerCase().indexOf("connection: keep-alive")!=-1) {
							println("Proxied response is keep-alive");
							request.setProperty(ConnectionKMethod.KEEP_ALIVE,"true");
						}
						if(contentLength==-2) {
							request.setProperty(ConnectionKMethod.KEEP_ALIVE,"false");
						}
					}
				}
			}
			catch(Exception e) {
				//			e.printStackTrace();
				error("Exception handling request",e);
			}
			
			if(!request.getBoolean(ConnectionKMethod.KEEP_ALIVE)) {
				try {
					request.getOutputStream().close();
				}
				catch(Exception e) {;}

				try {
					in.close();
				}
				catch(Exception e) {;}
				try {
					out.close();
				}
				catch(Exception e) {;}
				try {
					sock.close();
				}
				catch(Exception e) {;}
			}
			else {
				returnProxySocket(hostname,port,sock);
			}

			rval = true;
		}

		return(rval);
	}

	/**
	 * This method gets or creates a Socket to a proxy host and sends the HTTP request.
	 * If a proxy host could not be reached, the socket will be null.
	 */
	protected Socket getProxySocket(String hostname, int port, String request)
	{
		Socket rval = null;
		boolean socketOK = false;

		// grab a Socket if one's in the hash
		Pair p = (Pair)socketHash.remove(hostname+":"+port);
		if(p!=null) {
			// Check to see if it's too old
			if(Long.valueOf((String)p.first()).longValue() + getInteger("proxy_socket_cache_timeout") > System.currentTimeMillis()) {
				rval = (Socket)p.second();
			}
			else {
				// too old
				try {
					rval.close();
				}
				catch(Exception e) {;}
				rval = null;
			}				
		}


		if(rval==null) {
			// There was no previous Socket for this hostname and port, create a new one
			try {
				rval = new Socket(hostname,port);
				//				System.out.println("Socket is new");
			}
			catch(IOException ioe) {
				// Proxy not there
				try {
					rval.close();   // paranoid
				}
				catch(Exception e) {;}
				rval = null;
			}
		}
		else {
			// There was a Socket in the old Socket in the Hashtable
			try {
				rval.getOutputStream().write(request.getBytes());
				rval.getOutputStream().write(("\r\n\r\n").getBytes());
				rval.getOutputStream().flush();

				// Socket is OK, don't send the request again
				socketOK = true;
				//				System.out.println("Socket is ok");
			}
			catch(Exception e) {
				// Socket has been closed, make a new one
				try {
					rval.close();
				}
				catch(Exception ex) {;}
				try {
					rval = new Socket(hostname,port);
					//					System.out.println("Socket is old");
				}
				catch(IOException ioe) {
					// Proxy not there
					try {
						rval.close();   // paranoid
					}
					catch(Exception ex) {;}
					rval = null;
				}
			}
		}

		// If Socket is OK and the request hasn't already been sent
		if(rval!=null && !socketOK) {
			try {
				rval.setSoTimeout(getInteger("proxy_socket_timeout"));
			}
			catch(Exception e) {;}
			try {
				rval.getOutputStream().write(request.getBytes());
				rval.getOutputStream().write(("\r\n\r\n").getBytes());
				rval.getOutputStream().flush();
			}
			catch(Exception e) {
				try {
					rval.close();
				}
				catch(Exception ex) {;}
				rval = null;
			}
		}

		return(rval);
	}


	/**
	 * Returns a keep-alive connection to the SocketHash
	 */ 
	public void returnProxySocket(String hostname, int port, Socket s)
	{
		if(s!=null) {
			Pair p = (Pair)socketHash.put(hostname+":"+port,new Pair(""+System.currentTimeMillis(),s));
			if(p!=null) {
				// If there was an old one, close it
				Socket oldSock = (Socket)p.second();
				if(oldSock!=null) {
					try {
						oldSock.close();
					}
					catch(Exception e) {;}
				}
			}
		}
	}


	/**
	* Checks to see if a path is Proxied.
	*/
	public boolean isProxied(String path, String host)
	{
	    boolean rval = false;

		 if(path.equals("/")) {
			 path = "";
		 }
		 else if(path.startsWith("/")) {
			 path = path.substring(1);
		 }

	    if(proxies.containsKey("http://"+host+"/"+path)) {
			 rval = true;
	    }
	    else if(proxies.containsKey("https://"+host+"/"+path)) {
			 rval = true;
	    }

		 if(!rval) {
			 path = "/"+path;

			 if(proxies.containsKey(path)) {
				 rval = true;
			 }
	    }
	    return(rval);
	}

	/**
	* Runs and synchronizes object, then checks proxies.
	*/
	
	public void run()
	{
		try {
			synchronized(syncObject) {
				syncObject.wait(getInteger("initial_wait_time"));
			}
			while(!stopRunning) {
				proxies.checkAll();
				try {
					synchronized(syncObject) {
						if(getInteger("check_interval")>0) {
							syncObject.wait(getInteger("check_interval"));
						}
					}
				}
				catch(Exception e) {;}
			}
		}
		catch(Exception e) {
			//			e.printStackTrace();
			error("Exception in run()",e);
		}

	}


	/**
	* Class for Proxy Hash table.  Methods define how to place an object in or take out of the hash table.
	*/
	
	class ProxyHash
	{
		Hashtable strictProxies;
		Hashtable looseProxies;

		Hashtable status;

		public ProxyHash()
		{
			strictProxies = new Hashtable();
			looseProxies = new Hashtable();
			status = new Hashtable();
		}

		/**
		* Method checks all entries in hash table.
		*/
		
		public void checkAll()
		{
			try {
				Enumeration keys = looseProxies.keys();
				while(keys.hasMoreElements()) {
					Object o = looseProxies.get(keys.nextElement());
					if(o instanceof Vector) {
						Vector v = (Vector)o;
						for(int x=0;x<v.size();x++) {
							check((String)v.elementAt(x));
						}
					}
					else {
						check((String)o);
					}
				}

				keys = strictProxies.keys();
				while(keys.hasMoreElements()) {
					Object o = strictProxies.get(keys.nextElement());
					if(o instanceof Vector) {
						Vector v = (Vector)o;
						for(int x=0;x<v.size();x++) {
							check((String)v.elementAt(x));
						}
					}
					else {
						check((String)o);
					}
				}
			}
			catch(Exception e) {
				//				e.printStackTrace();
				error("Exception while checking if proxy is up",e);
			}
		}

	/**
	* Checks a single proxy String in the hash table.
	*/
	
		public void check(String proxy)
		{
			if(getInteger("check_interval")>0) {
				try {
					int index = proxy.indexOf("://");
					if(index!=-1) {
						proxy = proxy.substring(index+3);
						index = proxy.indexOf("/");
						if(index!=-1) {
							proxy = proxy.substring(0,index);
						}
						index = proxy.indexOf(":");
						int port = 80;
						if(index!=-1) {
							port = Integer.parseInt(proxy.substring(index+1));
							proxy = proxy.substring(0,index);
						}
						
						java.net.Socket s = null;
						try {
							s = new java.net.Socket(proxy,port);
							status.put(proxy+":"+port, "UP");
						}
						catch(Exception e) {
							status.put(proxy+":"+port, "DOWN");
						}
						try {
							s.close();
						}
						catch(Exception e) {;}
					}
				}
				catch(Exception e) {
					//				e.printStackTrace();
					error("Exception while checking if proxy is up",e);
				}
			}
		}

	/**
	* Checks if a proxy entry is currently running/available.
	*/
	
		public boolean isUp(String proxy)
		{
			boolean rval = false;
  			try {
				int index = proxy.indexOf("://");
				if(index!=-1) {
					proxy = proxy.substring(index+3);
					index = proxy.indexOf("/");
					if(index!=-1) {
						proxy = proxy.substring(0,index);
					}
					index = proxy.indexOf(":");
					int port = 80;
					if(index!=-1) {
						port = Integer.parseInt(proxy.substring(index+1));
						proxy = proxy.substring(0,index);
					}

					if(getInteger("check_interval") > 0) {
						if(status.get(proxy+":"+port).equals("UP")) {
							rval = true;
						}
					}
					else {
						rval = true;
					}
				}
			}
			catch(Exception e) {
				//				e.printStackTrace();
				error("Exception while setting proxy isUp()",e);
			}

			return(rval);
		}


	/**
	* Places an entry into the proxy hash table.
	*/

		public synchronized void put(String key, String object)
		{
			key = key.trim();
			object = object.trim();
			if(key.charAt(key.trim().length()-1)=='*') {
				key = key.substring(0,key.length()-1);
				if(looseProxies.containsKey(key)) {
					Object o = looseProxies.get(key);
					if(o instanceof Vector) {
						Vector v = (Vector)o;
						check((String)object);
						v.addElement(object);
					}
					else {
						Vector v = new Vector();
						v.addElement(o);
						v.addElement(object);
						check((String)object);
						looseProxies.put(key,v);
					}
				}
				else {
					check((String)object);
					looseProxies.put(key,object);
				}
			}
			else {
				if(strictProxies.containsKey(key)) {
					Object o = strictProxies.get(key);
					if(o instanceof Vector) {
						Vector v = (Vector)o;
						check((String)object);
						v.addElement(object);
					}
					else {
						Vector v = new Vector();
						v.addElement(o);
						v.addElement(object);
						check((String)object);
						strictProxies.put(key,v);
					}
				}
				else {
					check((String)object);
					strictProxies.put(key,object);
				}
			}
		}

	/**
	* Gets a String out of the hash table given a String key.
	*/

		public synchronized String get(String key)
		{
			String rval = null;
			if(strictProxies.containsKey(key)) {
				Object o = strictProxies.get(key);
				if(o instanceof Vector) {
					synchronized(o) {
						Vector v = (Vector)o;
						for(int x=0;x<v.size();x++) {
							rval = (String)v.elementAt(0);
							v.removeElementAt(0);
							v.addElement(rval);
							if(getInteger("check_interval")>0) {
								if(!isUp(rval)) {
									rval = null;
								}
								else {
									break;
								}
							}
							else {
								break;
							}
						}
					}
				}
				else {
					rval = (String)o;
					if(getInteger("check_interval")>0) {
						if(!isUp(rval)) {
							rval = null;
						}
					}
				}
			}
			else {
				Enumeration e = looseProxies.keys();
				String looseKey = null;
				while(e.hasMoreElements()) {
					looseKey = (String)e.nextElement();
					if(key.startsWith(looseKey)) {
						Object o = looseProxies.get(looseKey);
						if(o instanceof Vector) {
							Vector v = (Vector)o;
							synchronized(v) {
								for(int x=0;x<v.size();x++) {
									rval = (String)v.elementAt(0);
									v.removeElementAt(0);
									v.addElement(rval);
									if(getInteger("check_interval")>0) {
										if(!isUp(rval)) {
											rval = null;
										}
										else {
											break;
										}
									}
									else {
										break;
									}
								}
							}
						}
						else {
							rval = (String)o;
							if(getInteger("check_interval")>0) {
								if(!isUp(rval)) {
									rval = null;
								}
							}
						}

						break;
					}
				}
				if(rval!=null && looseKey!=null) {
					println("loose proxy value is: "+rval);
					rval = rval+key.substring(looseKey.length());
					println("loose proxy value changed to: "+rval);
				}
			}
			return(rval);
		}

	/**
	* Gets the size of the proxy hash table.
	*/

		public int size()
		{
			return(strictProxies.size()+looseProxies.size());
		}

	/**
	* Looks to see if the proxy hash table contains a String key.
	*/

		public boolean containsKey(String key)
		{
			boolean rval = false;
			if(strictProxies.containsKey(key)) {
				rval = true;
			}
			else {
				Enumeration e = looseProxies.keys();
				while(e.hasMoreElements()) {
					if(key.startsWith(e.nextElement().toString())) {
						rval = true;
						break;
					}
				}
			}
			return(rval);
		}

	/**
	* Converts hash table entries to String.
	*/

		public String toString()
		{
			StringBuffer rval = new StringBuffer();
		
			Enumeration e = strictProxies.keys();
			while(e.hasMoreElements()) {
				String key = (String)e.nextElement();
				Object o = strictProxies.get(key);
				rval.append("["+key+"]\n");
				if(o instanceof Vector) {
					for(int x=0;x<((Vector)o).size();x++) {
						rval.append("     => "+((Vector)o).elementAt(x)+"    is Up("+isUp((String)((Vector)o).elementAt(x))+")\n");
					}
				}
				else {
					rval.append("     => "+o+"    is Up("+isUp((String)o)+")\n");
				}
			}

			e = looseProxies.keys();
			while(e.hasMoreElements()) {
				String key = (String)e.nextElement();
				Object o = looseProxies.get(key);
				rval.append("["+key+"]\n");
				if(o instanceof Vector) {
					for(int x=0;x<((Vector)o).size();x++) {
						rval.append("     *=> "+((Vector)o).elementAt(x)+"    is Up("+isUp((String)((Vector)o).elementAt(x))+")\n");
					}
				}
				else {
					rval.append("     *=> "+o+"    is Up("+isUp((String)o)+")\n");
				}
			}

			return(rval.toString());
		}
	}
}
