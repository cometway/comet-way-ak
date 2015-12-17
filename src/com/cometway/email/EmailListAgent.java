
package com.cometway.email;

import com.cometway.email.Message;
import com.cometway.email.SendEmailInterface;
import com.cometway.net.HTTPLoader;
import com.cometway.props.Props;
import com.cometway.props.PropsListIteratorAgent;
import com.cometway.util.IExceptionHandler;


public class EmailListAgent extends PropsListIteratorAgent
{
	protected String message;


	public void initProps()
	{
		setDefault("database_name", "database");
		setDefault("name_key", "NAME");
		setDefault("email_key", "EMAIL");
		setDefault("message_url", "http://www.cometway.com/content.agent?page_name=Company");
		setDefault("reply_to", "user@host");
		setDefault("subject", "untitled");
	}


	public void start()
	{
		String message_url = getTrimmedString("message_url");
		String database_name = getString("database_name");

		println("Loading Email Message from " + message_url);

		HTTPLoader loader = new HTTPLoader();
		loader.setExceptionHandler(new EmailListExceptionHandler());
		message = loader.getURL(message_url);

		if (message.length() > 0)
		{
			debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> HTML >>>");
			debug(message);
			debug("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< HTML <<<");

			println("Iterating Email Recipients in " + database_name);

			iterate(database_name);
		}
		else
		{
			error("Could not continue. No HTML was loaded from " + message_url);
		}
	}


	public void handleRequest(Props p)
	{
		String reply_to = getTrimmedString("reply_to");
		String subject = getTrimmedString("subject");
		String name_key = getTrimmedString("name_key");
		String email_key = getTrimmedString("email_key");

		String name = p.getTrimmedString(name_key);
		String email = p.getTrimmedString(email_key);

		if (email.length() > 0)
		{
			println("Sending email to " + name + " <" + email + ">");


			Message m = new Message();
			m.setHeaderInfo("From", reply_to);
			m.setHeaderInfo("To", name + " <" + email + ">");
			m.setHeaderInfo("Subject", subject);
			m.setHeaderInfo("Content-Type", "text/html");
			m.setMessage(message);

			SendEmailInterface sendEmail = (SendEmailInterface) getServiceImpl("send_email");
			sendEmail.sendEmailMessage(m);
		}
	}


	public class EmailListExceptionHandler implements IExceptionHandler
	{
		public boolean handleException(Exception e, Object o, String message)
		{
			error(message + " (" + o + ')', e);

			return (true);
		}
	}
}


