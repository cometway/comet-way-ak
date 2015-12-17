package com.cometway.email;

import com.cometway.util.*;
import com.cometway.props.*;
import com.cometway.ak.*;
import java.util.Vector;
import java.util.Date;

/**
 * This agent proxies requests to another request agent and in the
 * process, it caches the props for each request. When this agent
 * starts up, it grabs a reference to the RequestAgent that its requests
 * are proxying to from the ServiceManager, then registers itself.
 * Cached requests are filtered depending on the <b>filter_interval</b>
 * property and the requests that made it through the filter are emailed
 * to the list of email addresses provided by the <b>recipients</b>
 * property. Note: It may be a bad idea for this agent to proxy AgentRequests
 * that may contain references to resources that needs to be released.
 * The props from AgentRequests are not released for garbage collection
 * until the filtering has been done.
 */
public class RequestFilterAgent extends RequestAgent implements Runnable
{
	protected Thread runThread;
	protected Object syncObject;
	protected Vector requests;

	public boolean stopRunning;

	//	protected RequestAgentInterface agent;
	protected Agent agent;

	public void initPtops()
	{
		setDefault("send_email_service_name","send_email");
		//		setDefault("schedule","between 0:0:0-23:59:59 every 1d");
		setDefault("filter_interval",""+(360*1000*24));
		setDefault("filter_properties","path,request_remote_addr");
		setDefault("send_from","jonlin@tesuji.org");
		setDefault("send_subject","Requests");
		setDefault("ignore_properties","path=/search.agent");

		if(!hasProperty("proxy_request_agent")) {
			throw new RuntimeException("proxy_request_agent property was unspecified");
		}

		if(!hasProperty("recipients")) {
			throw new RuntimeException("recipients property was unspecified");
		}

	}


	public void start()
	{
		Object o = ServiceManager.getService(getString("proxy_request_agent"));
		//		if(o instanceof RequestAgentInterface) {
		try {
			//			agent = (RequestAgentInterface)o;
			agent = (Agent)o;
		}
		catch(Exception e) {
		//		else {
			throw new RuntimeException(getString("proxy_request_agent")+" is not a RequestAgentInterface");
			//		}
		}
																	 
		register();

		runThread = new Thread(this);

		syncObject = new Object();

		stopRunning = false;
		runThread.setName("RequestFilterAgent");
		runThread.start();
	}

	public void stop()
	{
		super.stop();
		try {
			stopRunning = false;
			synchronized(syncObject) {
				syncObject.notify();
			}
		}
		catch(Exception e) {
			error("Exception in stop()",e);
		}
	}



	public void handleRequest(AgentRequest request)
	{
		((RequestAgent)agent).handleRequest(request);
		requests.addElement(request.getProps());
	}
	

	public void run()
	{
		try {
			while(!stopRunning) {
				synchronized(syncObject) {
					requests = new Vector();
					syncObject.wait(getLong("filter_interval"));
				}
				
				String email = createReport();
				if(email!=null) {
					String[] recipients = StringTools.commaToArray(getString("recipients"));
					SendEmailInterface emailer = (SendEmailInterface) ServiceManager.getService(getString("send_email_service_name"));
					
					for(int x=0;x<recipients.length;x++) {
						Message m = new Message();
						
						m.setHeaderInfo("from", getString("send_from"));
						m.setHeaderInfo("date", (new Date()).toString());
						m.setHeaderInfo("subject", getString("send_subject"));
						m.setMessage(email);
						m.setHeaderInfo("to", recipients[x]);
						
						//					emailer.sendEmailMessage(m);
						System.out.println(m.toString());
					}
				}
			}
		}
		catch(Exception e) {
			error("Exception in run()",e);
		}
	}




	public String createReport()
	{
		StringBuffer rval = new StringBuffer();
		String[] filters = StringTools.commaToArray(getString("filter_properties"));
		String[] ignore = StringTools.commaToArray(getString("ignore_properties"));

		if(requests.size()==0 || filters.length==0) {
			return(null);
		}

		try {
			synchronized(syncObject) {
				for(int z=0;z<requests.size();z++) {
					Props request = (Props)requests.elementAt(z);
					boolean skip = false;
					for(int x=0;x<ignore.length;x++) {
						int i = ignore[x].indexOf("=");
						String name = ignore[x].substring(0,i);
						String value = ignore[x].substring(i+1);
						if(request.getString(name).equals(value)) {
							skip = true;
							break;
						}
					}
					if(!skip) {
						for(int x=0;x<filters.length;x++) {
							rval.append(filters[x]);
							rval.append("\t = ");
							rval.append(request.getString(filters[x]));
							rval.append("\n");
						}
					}
					rval.append("\n");
				}
			}
		}
		catch(Exception e) {
			error("Error synchronizing on the request Vector");
		}

		return(rval.toString());
	}

}
