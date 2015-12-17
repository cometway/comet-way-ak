


package com.cometway.httpd;

import com.cometway.ak.*;
import java.util.*;
import java.io.*;
import com.cometway.util.*;
import com.cometway.net.*;

/**
* This agent is used by the WebServer to handle requests for HTML files and
* other static documents from the file system. It is a subclass of the HTTPFileServerAgent
* and used in place of the HTTPFileServerAgent when content needs to be throttled at a 
* certain bandwidth. This agent provides additional options for logging the amount of
* bandwidth used, limiting the maximum number of concurrent downloading sessions, and
* the ammount of bandwidth each connection can use.
*/

public class HTTPThrottledFileServer extends HTTPFileServerAgent
{
	protected String html_directory;


   protected int currentSessions;
   protected Object sync;
   protected FileWriter logOut;

	/** 
	*Initializes this agent's properties by providing default
	* values for each of the following missing properties:
	* "service_name" is used to register this agent with the Service Manager (default: HTTPThrottledFileServer),
	* "html_directory" points to the root directory where HTML files are served (default: ./),
	* "default_index" is the default file served when no filename is specified (default: index.html),
	* "allow_directory_lists" when enabled, allows this agent to display links to files in a directory (default: yes)
	* "max_sessions" denotes the maximum amount of concurrent connections downloading content at a single time (default: 4)
	* "max_bandwidth" denotes the amount of bytes per second allowed for each connection (default: 5000)
	* "log_file" denotes the file which bandwidth usage is to be logged (default: downloads.log)
	*/
	public void initProps()
	{
		super.initProps();

      setDefault("log_file","downloads.log");
		setDefault("service_url","http://www.cometway.com:8080/");
		setDefault("max_sessions","4");
		setDefault("max_bandwidth","5000");
	}
	
	/**
	* Registers Server and sets up directories, url's, etc.
	*/
	
	public void start()
	{
      try {
         logOut = new FileWriter(new File(getString("log_file")),true);
      }
      catch(Exception e) {
         error("Could not create Log file: "+getString("log_file"),e);
      }

		sync = new Object();

		super.start();
	}

	public void stop()
	{
		html_directory = null;
		try {
			logOut.flush();
			logOut.close();
		}
		catch(Exception e) {
			error("Could not close log file",e);
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



	protected void sendFile(File file, HTTPAgentRequest request) throws IOException
	{
		OutputStream socketOut = request.getOutputStream();
		FileInputStream fis = new FileInputStream(file);
		if(addSession()) {
			int bytesSent = 0;
			byte[] fileData = new byte[getInteger("max_bandwidth")];
			int bytesread = fis.read(fileData);
			while(bytesread>0) {
				long timestamp = System.currentTimeMillis();
				int start = 0;
				int end = 1023;
				while(end<bytesread) {
					socketOut.write(fileData,start,end-start);
					socketOut.flush();
					bytesSent = bytesSent+(end-start);
					start=start+1024;
					if(start>=bytesread) {
						break;
					}
					end=end+1024;
					if(end>=bytesread) {
						end=bytesread-1;
					}
					if(start==end) {
						break;
					}
				}
				bytesread = fis.read(fileData);
									
				long time = System.currentTimeMillis()-timestamp;
				if(time<1000) {
					try {
						Thread.sleep(1000-time);
					}
					catch(Exception e) {;}
				}
			}

			if(bytesSent==file.length()) {
				printLog("Sent "+bytesSent+" bytes successfully", request);
			}
			else {
				printLog("Sent "+bytesSent+" bytes out of "+file.length()+", the connection was closed by the client",request);
			}
			removeSession();

		}
		else {
			printTryAgain(request);
			printLog("---------- Max connections has been reached, sent try again message",request);
		}
	}


	/**
	 * This method generates the message when the maximum number of connections has been reached.
	 */
	protected void printTryAgain(HTTPAgentRequest request)
	{
		request.print("HTTP/1.1 200 Ok\r\n");
		request.print("Date: "+WebServer.dateFormat_RFC822.format(new Date())+"\r\n");
		request.print("Connection: close\r\n");
		request.print("Content-Type: text/html\r\n\r\n");
		request.print("<HTML><HEAD><TITLE>Please Try again Later</TITLE></HEAD><BODY><H1>The Download Server has reached its maximum number of connections</H1>Please wait a few minutes and try again. You should be able to try again by refreshing or reloading the current page. Sorry for the inconvenience.</BODY></HTML>\n");
	}

	/**
	 * This method is used by this Agent to log bandwidth related information to the log file.
	 *
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
