package com.cometway.httpd;

import com.cometway.ak.*;
import com.cometway.util.*;
import com.cometway.net.*;

import java.util.*;
import java.io.*;

/**
 * This agent is used to throttle specific content to web browsers. It can be used to 
 * limit maximum connections downloading content and the amount of bandwidth each connection
 * is using.
 */
public class HTTPFileDownloadAgent extends RequestAgent
{
	protected String html_directory;

	protected int currentSessions;
	protected Object sync;
	protected FileWriter logOut;

	/**
	 * Initializes this agent's properties by providing default values for each of the following missing properties:
	 * "service_name" is used to register this agent with the ServiceManager (default: download.agent)
	 * "html_directory" points to the root directory where HTML files are served (default: public_html)
	 * "max_sessions" denotes the maximum amount of concurrent connections downloading content at a single time (default: 4)
	 * "max_bandwidth" denotes the amount of bytes per second allowed for each connection (default: 5000)
	 * "log_file" denotes the file which bandwidth usage is to be logged (default: downloads.log)
	 */
	public void initProps()
	{
		setDefault("service_name","download.agent");

		setDefault("html_directory","public_html");
		setDefault("max_sessions","4");
		setDefault("max_bandwidth","5000");

		setDefault("log_file","downloads.log");
		setDefault("default_content_type","application/octet-stream");
	}

	/**
	 * Registers this agent and initializes the log file.
	 */
	public void start()
	{
		html_directory=getString("html_directory");
		sync = new Object();

		try {
			logOut = new FileWriter(new File(getString("log_file")),true);
		}
		catch(Exception e) {
			error("Could not create Log file: "+getString("log_file"),e);
		}

		register();
	}


	public void stop()
	{
		html_directory = null;
		try {
			logOut.flush();
			logOut.close();
		}
		catch(Exception e) {
			error("Error closing stream",e);
		}
	}		


   /**
    * This method is used to internally track the number of active sessions. If the
    * maximum number of sessions has been reached, this method returns false and no session
    * counter is tracked.
    */
	protected boolean addSession()
	{
		boolean rval = false;
		try {
			synchronized(sync) {
				if(currentSessions < getInteger("max_sessions")) {
					rval = true;
					currentSessions++;
				}
			}
		}
		catch(Exception e) {;}
		return(rval);
	}

	/**
	 * This method is used to internally track the number of active sessions.
	 */
	protected void removeSession()
	{
		try {
			synchronized(sync) {
				currentSessions--;
				if(currentSessions<0) {
					error("Current Number of Sessions has reached a value less than 0");
					currentSessions = 0;
				}
			}
		}
		catch(Exception e) {;}
	}

	/**
	 * This method handles the download request. It looks for the CGI parameter "file" and
	 * throttles the contents of the file to the client. If no parameter is found or the file
	 * does not exist, a 404 is returned.
	 */
	public void handleRequest(AgentRequest agentRequest)
	{
		HTTPAgentRequest request = (HTTPAgentRequest)agentRequest;
		OutputStream socketOut = request.getOutputStream();

		if(request.hasProperty("file")) {
			String filename = request.getString("file");
			File downloadFile = new File(html_directory,filename);
			if(filename.indexOf("..")!=-1 || filename.indexOf("/")!=-1 || filename.indexOf("\\")!=-1) {
				printLog("++++++++++ Attempt to access a file outside of the html directory was made, 404 response sent: "+filename,request);
				request.print(WebServer.getHTMLByCode(WebServer.URL_NOT_FOUND));
			}
			if(downloadFile.exists()) {
				if(addSession()) {
					FileInputStream fis = null;
					int bytesSent = 0;
					boolean success = false;
					try {
						String mimeType = WebServer.getMimeType(filename);
						//						if(mimeType.startsWith("Content-Type: text")) {
						//							mimeType = "Content-Type: "+getString("default_content_type")+"\n";
						//						}
						//						println("+++++++++++ MIME TYPE = "+mimeType);
						socketOut.write(("HTTP/1.1 200 Ok.\r\nConnection: close\r\n").getBytes());
						socketOut.write(("Date: "+WebServer.dateFormat_RFC822.format(new Date())+"\r\n").getBytes());
						socketOut.write(("Content-Length: "+downloadFile.length()+"\r\n").getBytes());
						socketOut.write(("Content-Disposition: inline; filename="+filename+"\r\n").getBytes());
						socketOut.write((mimeType+"\r\n").getBytes());
						socketOut.flush();

						fis = new FileInputStream(downloadFile);
						byte[] data = new byte[getInteger("max_bandwidth")];
						int bytesRead = fis.read(data);
						while(bytesRead>0) {
							long timestamp = System.currentTimeMillis();
							socketOut.write(data,0,bytesRead);

							bytesSent = bytesSent+bytesRead;
							
							bytesRead = fis.read(data);

							long time = System.currentTimeMillis()-timestamp;
							if(time<1000) {
								try {
									//									wait(1000-time);
									Thread.sleep(1000-time);
								}
								catch(Exception e) {;}
								//								println("Slept for "+(1000-time)+" milliseconds, sent="+bytesRead);
							}
						}
						if(bytesSent==downloadFile.length()) {
							success = true;
						}
					}
					catch(Exception e) {
						error("An error occured while sending the file: "+filename,e);
					}
					try {
						fis.close();
					}
					catch(Exception e) {;}
					if(success) {
						printLog("Sent "+bytesSent+" bytes successfully", request);
					}
					else {
						printLog("Sent "+bytesSent+" bytes out of "+downloadFile.length()+", the connection was closed by the client",request);
					}
					removeSession();
				}
				else {
					request.println("HTTP/1.1 200 Ok");
					request.println("Date: "+WebServer.dateFormat_RFC822.format(new Date())+"\n");
					request.println("Connection: close");
					request.println("Content-Type: text/html\n");
					request.println("<HTML><HEAD><TITLE>Please Try again Later</TITLE></HEAD><BODY><H1>The Download Server has reached its maximum number of connections</H1>Please wait a few minutes and try again. You should be able to try again by refreshing or reloading the current page. Sorry for the inconvenience.</BODY></HTML>");
					printLog("---------- Max connections has been reached, sent try again message",request);
				}
			}
			else {
				request.print(WebServer.getHTMLByCode(WebServer.URL_NOT_FOUND));
				printLog("---------- Requested a file that does not exist, sent 404 response",request);
			}
		}
		else {
			request.print(WebServer.getHTMLByCode(WebServer.URL_NOT_FOUND));
			printLog("---------- Got a request without the 'file' parameter, 404 response",request);
		}

	}
	
	/**
    * This method is used by this Agent to log bandwidth related information to the log file.
	 */
	protected void printLog(String logString, HTTPAgentRequest request)
	{
		try {
			logOut.write((new Date())+" : "+request.getString("request_remote_addr")+" : "+logString+"\n");
			logOut.flush();
		}
		catch(Exception e) {
			error("Could not write to logfile",e);
		}
	}
}
