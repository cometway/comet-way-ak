
package com.cometway.email;


import com.cometway.ak.AgentRequest;
import com.cometway.ak.RequestAgent;
import com.cometway.util.Pair;
import java.util.Date;


/**
* This agent sets itself up to receive messages from a ReceiveEmailAgent
* and automatically respond to the sender using the SendEmailAgent.
* In most cases, this will be subclassed and the getReply method
* should be overridden.
*/

public class AutoReplyAgent extends RequestAgent
{
	/**
	* Initializes properties for this agent:
	* <PRE>service_name - is the agent's email address,
	* reply_to - the agent's reply to email address (usually the same as service_name),
	* send_email_service_name - is the service name of the SendEmailAgent (send_email),
	* reply_message - is the static text response this agent replys with.</PRE>
	*/

	public void initProps()
	{
		setDefault("service_name", "agent@localhost");
		setDefault("reply_to", "agent@localhost");
		setDefault("send_email_service_name", "send_email");
		setDefault("reply_message", "This message was sent by an auto-reply agent.");
		setDefault("reply_subject", "Comet Way Auto Reply");
	}


	/**
	* Registers with the Service Manager to receive and respond to e-mail requests.
	*/

	public void start()
	{
		String service_name = getString("service_name");

		println("Registering as " + service_name);
		println("Reply To is " + getString("reply_to"));
		println("Sending replies using " + getString("send_email_service_name"));

		register();
	}


	/**
	* Processes an email AgentRequest by parsing out the recipient's Reply To address,
	* and responding with the text message returned by calling the getReply method.
	* The message is sent using the SendEmailInterface registered with the ServiceManager
	* as specified by the "send_email_service_name" property.
	* Note: This method will do nothing if the sender (Reply To from the message) is the same
	* as the "reply_to" property as this would cause a mail loop.
	*/

	public void handleRequest(AgentRequest request)
	{
		String send_email_service_name = getString("send_email_service_name");
		String sender = getString("reply_to");
		String reply_subject = getTrimmedString("reply_subject");


		/* Set recipient to the Reply To address */

		IMessage m = (IMessage) request.getProperty("message");
		MessageTools mt = new MessageTools(m);
		String recipient = mt.getReplyTo();
		Pair p = EmailTools.parseEmailAddress(recipient);
		recipient = (String) p.first();		
		

		/* Make sure we're not creating mail loops. */

		if (recipient.equals(sender))
		{
			error("Received self-addressed message; no reply can be sent.");
		}
		else
		{
			Date now = new Date();
			Message reply = new Message();

			reply.setHeaderInfo("from", sender);
			reply.setHeaderInfo("date", now.toString());
			reply.setHeaderInfo("subject", reply_subject);
			reply.setMessage(getReply(request));
			reply.setHeaderInfo("to", recipient);

			SendEmailInterface emailer = (SendEmailInterface) getServiceImpl(send_email_service_name);

			println("Sending reply (" + sender + " -> " + recipient + ")");

			emailer.sendEmailMessage(reply);
		}
	}


	/**
	* This method can be overridden to provide an automatic response message.
	* By default it returns the text message provided by the "reply_message" property.
	*/

	public String getReply(AgentRequest request)
	{
		return (getString("reply_message"));
	}
}


