package com.cometway.httpd;

import com.cometway.util.*;
import com.cometway.io.*;
import com.cometway.ak.*;
import java.util.*;
import java.io.*;
import com.cometway.props.Props;

	/**
	* This agent registers itself into the ServiceManager and accepts http requests to view information stored 
	* in log files. Given a directory, this agent will look for all the files in that directory and treat them 
	* as log files. The agent uses a mapping which provides information about how these files should be 
	* displayed. The logfile mapping maps a name to a text pattern. The agent will display the names and the 
	* matching lines of the log files. 	
	*/

public class LogViewerPage extends RequestAgent implements Runnable
{
	Thread checkThread;

	Hashtable logMaps;
	Hashtable counts;

	Vector names;
	boolean stopRunning;

	/**
	* Initializes this agent's properties by providing default
	* values for each of the following missing properties:
	* "log_dir" specifies the directory where the log file is 
	* located (default: ./), "log_map_file" specifies the file
	* to map the logs to (default: ./log.maps), "log_file_suffix"
	* specifies the suffix of the log file (default: httpd.log),
	* "check_interval" specifies the time between checks (default:
	* 600000), "service_name" specifies the name to register this
	* agent as with the Object Manager (default: /logviewer.agent),
	* "is_logger_output" (default: TRUE), "max_show_log_length"
	* (default: 50000), "show memory_usage" (default: false).
	*/

	public void initProps()
	{
		setDefault("log_dir","./");

		setDefault("log_map_file","./log.maps");

		setDefault("log_file_suffix","httpd.log");

		setDefault("check_interval","600000");

		setDefault("service_name","/logviewer.agent");

		setDefault("is_logger_output","TRUE");

		setDefault("max_show_log_length","50000");

		setDefault("show_memory_usage","false");
	}

	/**
	* Starts new thread, gets new logMaps, counts, names list and registers this service
	* with the Object Manager.
	*/

	public void start()
	{
		checkThread = new Thread(this);
		
		logMaps = new Hashtable();
		counts = new Hashtable();

		names = new Vector();

		readLogMapping();

		ServiceManager.register(getString("service_name"),this);

		checkThread.start();
	}
	
	public void stop()
	{
		stopRunning = true;
		synchronized(checkThread) {
			checkThread.notifyAll();
		}
		checkThread = null;
		logMaps = null;
		counts = null;
		names = null;
	}


	/**
	* Handles request and gets log file information and displays as HTML data.
	*/

	public void handleRequest(AgentRequest request)
	{

		printMem("+ handleRequest()");
		if (counts.size() == 0) // We haven't scanned yet.
		{
			updateLogCache();
		}

		Props p = request.getProps();
		
		String opcode = p.getString("opcode");
		debug("Got request with opcode: "+opcode);

		if(opcode.equals("show_summary")) {
			request.println("<html><head><title>Show Log Summaries</title></head><body>");
			request.println(getForm());
			request.println("<table>");
			for(int x=0;x<names.size();x++) {
				String name = (String)names.elementAt(x);
				request.println("<TR>");
				request.println("<TD>");
				request.println(name);
				request.println("</TD><TD>");
				request.println((String)counts.get(name)+"</TD></TR>");
			}
			request.println("</TABLE>");
			request.println("</BODY></HTML>");
		}
		else if(logMaps.containsKey(opcode)) {
			request.println("<html><head><title>Show "+opcode+"</title></head><body>");
			request.println("<h1>"+opcode+"</h1>");
			request.println(getForm());

			Hashtable matches = new Hashtable();
			matches.put(opcode,new StringBuffer());
			Hashtable pattern = new Hashtable();
			pattern.put(opcode,logMaps.get(opcode));

			parseInfo(matches,pattern,true);

			if(getBoolean("is_logger_output")) {
				request.println("<font face=courier>");
				request.println("<TABLE><TR><TD NOWRAP>");
				String tmp = ((StringBuffer)matches.get(opcode)).toString();
				tmp = jGrep.grepAndReplaceText(" - - ","</TD><TD ALIGN=MIDDLE NOWRAP>- -</TD><TD ALIGN=RIGHT NOWRAP>",tmp,true);
				tmp = jGrep.grepAndReplaceText("] ","]</TD><TD NOWRAP>",tmp,true);
				tmp = jGrep.grepAndReplaceText("\\n","</TD></TR><TR><TD NOWRAP>",tmp,true);
				request.println(tmp);
				request.println("</TD></TR></TABLE>");
				request.println("</FONT>");
			}
			else {
				request.println("<font face=courier><pre>");
				StringBuffer v = (StringBuffer)matches.get(opcode);
				request.println(convert(v.toString()));
				request.println("</pre></font>");
			}

			request.println("</BODY></HTML>");
		}
		else {
			request.println("<html><head><title>Log Viewer</title></head><body>\n");
			request.println(getForm());
			request.println("</body></html>");
		}
printMem("- handleRequest()");
	}
	
	/**
	* Converts a String to HTML text.
	*/

	protected String convert(String in)
	{
		printMem("+ convert()");

		in = jGrep.grepAndReplaceText("\\&","&amp;",in,false);
		in = jGrep.grepAndReplaceText("<","&lt;",in,false);
		in = jGrep.grepAndReplaceText(">","&gt;",in,false);
		in = jGrep.grepAndReplaceText("\\\"","&quot;",in,false);

		printMem("- convert()");
		return(in);
	}
		
	/**
	* Displays log file information in a form.
	*/

	protected String getForm()
	{
		StringBuffer rval = new StringBuffer();
		
		rval.append("<form method=post action="+getString("service_name")+">\n");
		rval.append("<select name=opcode>\n");
		rval.append("<option value=show_summary>Show Summaries</option>\n");
		for(int x=0;x<names.size();x++) {
			String key = (String)names.elementAt(x);
			rval.append("<option value='"+key+"'>Show "+key+"</option>\n");
		}
		rval.append("</select>\n");
		rval.append("<input type=submit name='Show Logs' value='Show Logs'>\n");
		rval.append("</form>\n");
		
		return(rval.toString());
	}

	/**
	* Runs log parsing routines.
	*/

	public void run()
	{
		while(!stopRunning) {
printMem("+ run()");
			updateLogCache();

printMem("- run()");
			try {
				println("sleeping");
				synchronized(checkThread) {
					checkThread.wait(getInteger("check_interval"));
				}
			}
			catch(Exception e2) {
				error("Unable to sleep",e2);
			}
		}
	}

	public void updateLogCache()
	{
		try {
			Hashtable matches = new Hashtable();
			for(int x=0;x<names.size();x++) {
				String name = (String)names.elementAt(x);
				matches.put(name,new Vector());
			}
			
			parseInfo(matches,logMaps,false);
			for(int x=0;x<names.size();x++) {
				String name = (String)names.elementAt(x);
				Vector v = (Vector)matches.get(name);
				counts.put(name,v.size()+"");
			}
		}
		catch(Exception e) {
			error("Unable to parse log files",e);
		}
	}
	
	
	/**
	* Parses log file information in hash table, searches for patterns and stores matches.
	*/

	public void parseInfo(Hashtable matches, Hashtable patterns, boolean storeMatches)
	{
	    printMem("+ parseInfo()");
		try {
			File dir = new File(getString("log_dir"));
			String[] logfiles = dir.list();
			
			Hashtable tmpMatches = new Hashtable();
			if(storeMatches) {
				Enumeration keys = matches.keys();
				while(keys.hasMoreElements()) {
					tmpMatches.put(keys.nextElement(),new StringBuffer());
				}
			}

			for(int x=1;x<=logfiles.length;x++) {
				File file = new File(dir,logfiles[logfiles.length-x]);
				if(file.isFile()) {
				    if(logfiles[logfiles.length-x].endsWith(getString("log_file_suffix"))) {
					BufferedReader in = new BufferedReader(new FileReader(file));
					
					println("Looking through file: "+file);
					
					String line = in.readLine();
					while(line!=null) {
					    Enumeration keys = patterns.keys();
					    while(keys.hasMoreElements()) {
						String key = (String)keys.nextElement();
						String pattern = (String)patterns.get(key);
						
						if(line.indexOf(pattern)>-1) {
						    if(storeMatches) {
							StringBuffer buffer = (StringBuffer)tmpMatches.get(key);
							if(buffer.length()<getInteger("max_show_log_length")) {
							    buffer.insert(0,line+"\n");
							}
						    }
						    else {
							Vector v = (Vector)matches.get(key);
							v.addElement(new Object());
						    }
						}
					    }
					    
					    line = in.readLine();
					}
					
					if(storeMatches) {
					    Enumeration keys = matches.keys();
					    while(keys.hasMoreElements()) {
						String tmpString = (String)keys.nextElement();
						StringBuffer buffer = (StringBuffer)matches.get(tmpString);
						if(buffer.length()<=getInteger("max_show_log_length")) {
						    StringBuffer tmpBuffer = (StringBuffer)tmpMatches.get(tmpString);
						    buffer.append(tmpBuffer.toString());
						    tmpMatches.put(tmpString,new StringBuffer());
						}
					    }
					}
					
					try {
					    in.close();
					}
					catch(Exception e) {;}
				    }
				}
			}
		}
		catch(Exception e1) {
			error("Unable to parse log files",e1);
		}
printMem("- parseInfo()");
	}

	/**
	* Reads Log file Mappings and outputs maps read.
	*/

	protected void readLogMapping()
	{
		File file = new File(getString("log_map_file"));

		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = in.readLine();
			while(line!=null) {
				int index = line.indexOf("=");
				if(index>0) {
					String name = line.substring(0,index);
					if(!logMaps.containsKey(name)) {
						names.addElement(name);
						logMaps.put(name,line.substring(index+1));
						counts.put(name,"0");
					}
				}
				line = in.readLine();
			}

			println("Log Maps: "+logMaps);

			in.close();
		}
		catch(Exception e) {
			error("Unable to read logfile mappings",e);
		}
	}


	/**
	* Prints Memory usage to HTML output.
	*/

	private void printMem(String s)
	{
		if(getBoolean("show_memory_usage")) {
			Runtime r = Runtime.getRuntime();
			debug(s+" MEM("+r.freeMemory()+"/"+r.totalMemory()+")");
		}
	}

}




