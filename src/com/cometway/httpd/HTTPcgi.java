package com.cometway.httpd;

import java.util.Vector;
import java.util.Date;
import java.io.File;

import com.cometway.ak.*;
import com.cometway.util.ExecuteCommand;
import com.cometway.util.ThreadPool;
import com.cometway.util.StringTools;
import com.cometway.util.jGrep;

/**
 * This extension is used to define and execute CGI scripts. It requires a directory
 * where the script(s) resides as well as a URI for accessing the script. If an attempt
 * to access just the script directory is made, a 403 Forbidden response is given. Scripts
 * are executed via a PooledThread with a timeout, if the script doesn't finish running,
 * the process is killed. This extension requires the inet address methods flag to be set
 * to true in the WebServer or else some scripts that depend on the client's IP address
 * may break. This extension currently does NOT support local redirects.
 */
public class HTTPcgi extends WebServerExtension
{
	ThreadPool threadPool;
	String[] scripts;

	/**
	 * max_exec_time_ms - The longest time to wait for a script to run before it is killed and an error returned (default: 10000)
	 * script_uri - The URI path to access the script(s) (default: /cgi-bin/)
	 * script_directory - The location of the script(s) on the local filesystem (default: ./html/cgi-bin/)
	 * scripts - a comma separated list of scripts or * for everything in the script_directory (default: *)
	 * thread_pool_size - Maximum number of threads in the thread pool (default: 20)
	 *
	 * server_name - The SERVER_NAME to pass to the script. This is either a hostname or IP address (default: 127.0.0.1)
	 * server_port - The SERVER_PORT to pass to the script. (default: 80)
	 * server_root_path - This is required for path translation, it is the location of the root HTML directory (default: ./html)
	 */
	public void initProps()
	{
		setDefault("service_name","extension://.*");
		setDefault("webserver_service_name","none");
		setDefault("domains","none");

		setDefault("max_exec_time_ms","10000");
		setDefault("script_uri","/cgi-bin/");
		setDefault("script_directory","./html/cgi-bin/");
		setDefault("scripts","*");
		setDefault("thread_pool_size","20");

		setDefault("server_name","127.0.0.1");
		setDefault("server_port","80");
		setDefault("server_root_path","./html");
	}

	public void start()
	{
		threadPool = new ThreadPool(getInteger("thread_pool_size"));
		threadPool.setName("HTTPcgi");

		scripts = StringTools.commaToArray(getString("scripts"));

		try {
			String tmp = getString("script_uri");
			if(tmp.charAt(tmp.length()-1)!='/') {
				tmp = tmp+"/";
				setProperty("script_uri",tmp);
			}
		}
		catch(Exception e) {;}

		super.start();
	}

	public void stop()
	{
		try {
			threadPool.stop();
			scripts = null;
		}
		catch(Exception e) {
			error("Could not shutdown threadpool",e);
		}
	}

	public boolean handleRequest(HTTPAgentRequest request)
	{
		boolean rval = false;

		if(request.getString("path").startsWith(getString("script_uri"))) {
			File parent = new File(getString("script_directory"));
			String script = request.getString("path").substring(getString("script_uri").length());
			String params = "";
			String pathInfo = "";
			if(request.getString("request").indexOf("?")!=-1) {
				String tmp = request.getString("request").substring(request.getString("request").indexOf("?")+1);
				if(tmp.indexOf("HTTP/1")!=-1) {
					params = tmp.substring(0,tmp.indexOf("HTTP/1")).trim();
				}
				else if (tmp.indexOf("\n")!=-1) {
					params = tmp.substring(0,tmp.indexOf("\n")).trim();
				}					
			}
			if(script.indexOf("/")!=-1) {
				pathInfo = script.substring(script.indexOf("/"));
				script = script.substring(0,script.indexOf("/"));
			}
			if(script.length()>0 && scripts.length>0) {
				boolean isScript = false;
				File scriptFile = new File(parent,script);
				for(int x=0;x<scripts.length;x++) {
					if(scripts[x].equals(script)) {
						isScript = true;
						break;
					}
				}
				if((scripts[0].equals("*") || isScript) && scriptFile.exists()) {
					if(scriptFile.isFile() && scriptFile.canRead()) {
						// Setup the script environment
						String[] environment = generateEnvironmentVariables(request, script, pathInfo, params);
						ExecuteCommand command = new ExecuteCommand(scriptFile.getAbsolutePath());
						StringBuffer out = new StringBuffer();
						StringBuffer err = new StringBuffer();
						if(request.getContentLength()>0) {
							command = new ExecuteCommand(scriptFile.getAbsolutePath(),request.getRequestBody());
						}
						command.environment = environment;
						command.workingDirectory = parent;
						command.processOut = out;
						command.processErr = err;
						command.finishedWaitTime = 100;
						//						command.waitForProcessReaders = true;

						debug("Executing CGI Script: "+scriptFile.getAbsolutePath()+" environment: "+environment+" workingDir: "+parent);
						if(threadPool.getThread(command,command,getInteger("max_exec_time_ms"))) {
							command.stopProcess();
							if(out.length()>0) {
								rval = processCGIOutput(request,out);
							}
							if(err.length()>0) {
								warning("Received stderr from script '"+scriptFile+"': "+err);
							}

							// There was an error somewhere
							if(!rval) {
								try {
									request.getOutputStream().write(WebServer.getHTMLByCode(WebServer.SERVER_ERROR).getBytes());
									request.getOutputStream().flush();
								}
								catch(Exception e) {;}
								request.returnVal = "500";
								rval = true;
							}
						}
						else {
							// Ran out of pooled threads
							try {
								request.getOutputStream().write(WebServer.getHTMLByCode(WebServer.SERVICE_UNAVAILABLE).getBytes());
								request.getOutputStream().flush();
							}
							catch(Exception e) {;}
							request.returnVal = "503";
							rval = true;
							warning("Warning, the script: '"+scriptFile+"' is not a file or could not be read");
						}
					}
					else {
						try {
							request.getOutputStream().write(WebServer.getHTMLByCode(WebServer.SERVER_ERROR).getBytes());
							request.getOutputStream().flush();
						}
						catch(Exception e) {;}
						request.returnVal = "500";
						rval = true;
						warning("Warning, the script: '"+scriptFile+"' is not a file or could not be read");
					}
				}
				else {
					try {
						request.getOutputStream().write(WebServer.getHTMLByCode(WebServer.URL_NOT_FOUND).getBytes());
						request.getOutputStream().flush();
					}
					catch(Exception e) {;}
					request.returnVal = "404";
					rval = true;
				}
			}
			else {
				try {
					request.getOutputStream().write(WebServer.getHTMLByCode(WebServer.FORBIDDEN).getBytes());
					request.getOutputStream().flush();
				}
				catch(Exception e) {;}
				request.returnVal = "403";
				rval = true;
			}
		}
		return(rval);
	}


	protected String[] generateEnvironmentVariables(HTTPAgentRequest request, String scriptName, String scriptPath, String getParams)
	{
		Vector vars = new Vector();
		String[] rval = null;
		String headers = request.getRequestHeaders();

		// set AUTH_TYPE
		int index = headers.indexOf("Authorization: ");
		if(index!=-1) {
			String tmp = headers.substring(index+15);
			index = tmp.indexOf(" ");
			if(index==-1) {
				index = tmp.length();
			}
			tmp = tmp.substring(0,index);
			vars.addElement("AUTH_TYPE="+tmp);
		}

		// set CONTENT_LENGTH
		if(request.getContentLength()>0) {
			vars.addElement("CONTENT_LENGTH="+request.getContentLength());
		}
		else {
			vars.addElement("CONTENT_LENGTH=0");
		}

		// set CONTENT_TYPE
		index = headers.toLowerCase().indexOf("content-type: ");
		if(index!=-1) {
			String tmp = headers.substring(index+14);
			index = tmp.indexOf("\n");
			if(index==-1) {
				index = tmp.length();
			}
			tmp = tmp.substring(0,index).trim();
			vars.addElement("CONTENT_TYPE="+tmp);
		}

		// set GATEWAY_INTERFACE
		vars.addElement("GATEWAY_INTERFACE=CGI/1.1");

		// set PATH_INFO and PATH_TRANSLATED
		if(scriptPath.trim().length()>0) {
			vars.addElement("PATH_INFO="+scriptPath);
			File tmpFile = new File(getString("server_root_path"));
			vars.addElement("PATH_TRANSLATED="+tmpFile.getAbsolutePath()+scriptPath);
		}

		// set QUERY_STRING
		if(getParams.length()>0) {
			vars.addElement("QUERY_STRING="+getParams);
		}

		// set REMOTE_ADDR
		if(request.hasProperty("request_remote_addr")) {
			vars.addElement("REMOTE_ADDR="+request.getString("request_remote_addr"));
		}

		// set REMOTE_HOST
		if(request.hasProperty("request_remote_host")) {
			vars.addElement("REMOTE_HOST="+request.getString("request_remote_host"));
		}
		
		// set REQUEST_METHOD
		index = request.getString("request").indexOf(" ");
		if(index!=-1) {
			vars.addElement("REQUEST_METHOD="+request.getString("request").substring(0,index).trim());
		}

		// set SCRIPT_NAME
		vars.addElement("SCRIPT_NAME="+getString("script_uri")+scriptName);

		// set SERVER_NAME
		vars.addElement("SERVER_NAME="+getString("server_name"));

		// set SERVER_PORT
		vars.addElement("SERVER_PORT="+getString("server_port"));

		// set SERVER_PROTOCOL
		index = request.getString("request").indexOf("HTTP/");
		if(index!=-1) {
			String tmp = request.getString("request").substring(index);
			index = tmp.indexOf("\n");
			if(index==-1) {
				index = tmp.length();
			}
			tmp = tmp.substring(0,index).trim();
			vars.addElement("SERVER_PROTOCOL="+tmp);
		}

		// set SERVER_SOFTWARE
		vars.addElement("SERVER_SOFTWARE="+WebServer.VERSION_STR);

		// set HTTP vars
		index = headers.indexOf("\n");
		while(index!=-1) {
			String line = headers.substring(0,index).trim();
			headers = headers.substring(index+1).trim();
			index = line.indexOf(":");
			if(index!=-1) {
				String name = "HTTP_"+line.substring(0,index).trim().toUpperCase();
				String value = line.substring(index+1).trim();
				index = name.indexOf("-");
				while(index!=-1) {
					name = name.substring(0,index)+"_"+name.substring(index+1);
					index = name.indexOf("-");
				}
				vars.addElement(name+"="+value);
			}
			index = headers.indexOf("\n");
		}
		if(headers.trim().length()>0) {
			index = headers.indexOf(":");
			if(index!=-1) {
				String name = "HTTP_"+headers.substring(0,index).trim().toUpperCase();
				String value = headers.substring(index+1).trim();
				index = name.indexOf("-");
				while(index!=-1) {
					name = name.substring(0,index)+"_"+name.substring(index+1);
					index = name.indexOf("-");
				}
				vars.addElement(name+"="+value);
			}
		}			

		rval = new String[vars.size()];
		for(int x=0;x<rval.length;x++) {
			rval[x] = (String)vars.elementAt(x);
		}
		return(rval);
	}

	protected boolean processCGIOutput(HTTPAgentRequest request, StringBuffer out)
	{
		boolean rval = false;
		String outstr = out.toString();
		boolean isHead = false;

		if(request.getString("request").startsWith("HEAD")) {
			isHead = true;
		}

		if(out.length()>15) {
		//		if(outstr.startsWith("content-type:") || outstr.startsWith("Content-Type:") || outstr.startsWith("Content-type:") ||
		//			outstr.startsWith("location:") || outstr.startsWith("Location:") ||
		//			outstr.startsWith("status:") || outstr.startsWith("Status:")) {
			int index = -1;
			if(!isHead) {
				index = outstr.indexOf("\n\n");
				if(index!=-1) {
					if(outstr.indexOf("\r\n\r\n")!=-1 && outstr.indexOf("\r\n\r\n")<index) {
						index = outstr.indexOf("\r\n\r\n");
						out.insert(0,"Content-Length: "+outstr.substring(index+4).length()+"\r\n");
					}
					else {
						out.insert(0,"Content-Length: "+outstr.substring(index+2).length()+"\r\n");
					}
				}
				else {
					index = outstr.indexOf("\r\n\r\n");
					if(index!=-1) {
						out.insert(0,"Content-Length: "+outstr.substring(index+4).length()+"\r\n");
					}
				}
			}
			index = outstr.indexOf("Date:");
			if(index==-1) {
				out.insert(0,"Date: "+WebServer.dateFormat_RFC822.format(new Date())+"\r\n");
			}
			out.insert(0,"Connection: close\r\n");
			index = outstr.indexOf("Status:");
			if(index==-1) {
				if(outstr.startsWith("Location:") || outstr.startsWith("location:")) {
					out.insert(0,"HTTP/1.1 302 Found\r\n");
				}
				else {
					out.insert(0,"HTTP/1.1 200 Ok\r\n");
				}
			}
			else {
				String status = outstr.substring(index+7);
				index = status.indexOf("\n");
				if(index==-1) {
					index = status.length();
				}
				status = status.substring(0,index);
				out.insert(0,"HTTP/1.1 "+status.trim()+"\r\n");
			}

			out = new StringBuffer(jGrep.grepAndReplaceText("([^\\r])\\n","$1\\r\\n",out.toString(),false));

			if(isHead) {
				outstr = out.toString();
				if(outstr.indexOf("\n\n")!=-1) {
					outstr = outstr.substring(0,outstr.indexOf("\n\n")+2);
				}
				if(outstr.indexOf("\r\n\r\n")!=-1) {
					outstr = outstr.substring(0,outstr.indexOf("\r\n\r\n")+4);
				}
				
				request.print(outstr);
			}
			else {
				request.print(out.toString());
			}
			rval = true;
		}

		return(rval);
	}
}
