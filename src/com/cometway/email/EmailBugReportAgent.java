
package com.cometway.email;

import com.cometway.ak.*;
import com.cometway.net.*;
import com.cometway.props.Props;
import com.cometway.util.*;
import java.util.*;


/**
* This agent registers itself with the Service Manager as a BugReportInterface
* and periodically submits the bug reports collects as an email message
* to a list of recipients using the SendEmailInterface registered with the
* Service Manager as "send_email".
*/

public class EmailBugReportAgent extends ServiceAgent implements ISchedulable, BugReportInterface
{
	Schedule		schedule;
	Vector			history;
	Vector			recipients;
	String			reply_to;


	/**
	* Initializes the default Props for this agent:
	* "service_name" is the name this agent uses to register with the Service Manager (default: bug_report),
	* "schedule" describes the schedule used to wake up this agent and email bug reports (default: every 6 hours),
	* "report_title" is used to prefix the subject line of the emails that are sent (default: Bug Report),
	* "reply_to" is used to set the Reply To field of emails that are sent,
	* "recipients" is a comma separated list of email addresses used to send the emails.
	*/

	public void initProps()
	{
		setDefault("service_name", "bug_report");
		setDefault("schedule", "between 0:0:0-23:59:59 every 6h");
		setDefault("send_email_service_name", "send_email");
		setDefault("report_title", "Bug Report");

		if (hasProperty("reply_to") == false)
		{
                        throw new RuntimeException("reply_to property was unspecified.");
		}

		if (hasProperty("recipients") == false)
		{
			throw new RuntimeException("recipients property was unspecified.");
		}
	}


	/**
	* Schedules the bug reporting mechanism using the schedule property
	* and registers this agent with the Service Manager.
	*/

	public void start()
	{
		register();

		StringTokenizer t = new StringTokenizer(getString("recipients"), ", \n");

		recipients = new Vector();

		while (t.hasMoreTokens())
		{
			recipients.addElement(t.nextToken());
		}

		schedule = new Schedule(getString("schedule"));

		Scheduler.getScheduler().schedule(this);

		history = new Vector();

		println("reply_to: " + getString("reply_to"));
		println("recipients: " + recipients.toString());
	}


	/**
	 * Unschedules the bug reporter, and unregisters this agent with the Service Manager.
	 */

	public void stop()
	{
		Scheduler.getScheduler().unschedule(this);

		schedule = null;

		unregister();
	}


	/**
	 * Called by Scheduler to add a listener to our Schedule.
	 * Returns false to indicate that we don't need this feature.
	 */

	public boolean addScheduleChangeListener(IScheduleChangeListener l)
	{
		return (false);
	}


	/**
	 * Returns the instance to the schedule.
	 */

	public ISchedule getSchedule()
	{
		return (schedule);
	}


	/**
	 * Called by Scheduler to remove a listener from our Schedule.
	 * Returns false to indicate that we don't need this feature.
	 */

	public boolean removeScheduleChangeListener(IScheduleChangeListener l)
	{
		return (false);
	}


	/**
	 * Called by Scheduler based on the Schedule returned by getSchedule method.
	 * When called it calls the resetHistory method.
	 */

	public void wakeup()
	{
		resetHistory();
	}


	/**
	* Submits a bug report from the specified author (or entity) using the description.
	*/

	public void submitBugReport(String author, String description)
	{
		String  dateStr = getDateTimeStr();

		println("Bug report submitted at " + dateStr + " by " + author);

		StringBuffer    b = new StringBuffer(author);

		b.append(" - ");
		b.append(dateStr);
		b.append('\n');
		b.append(description);
		history.addElement(b.toString());
	}


	/**
	 * Checks current bug history and emails any pending reports to
	 * the recipients, then resets the history.
	 */

	public void resetHistory()
	{
		Date    now = new Date();

		if (history.size() > 0)
		{
			Props		vp = this;
			String		report_title = getString("report_title");
			String		reply_to = getString("reply_to");
			String		title = report_title + " - " + getDateTimeStr();
			StringBuffer    b = new StringBuffer();

			b.append(title);
			b.append("\n\n");

			Enumeration     e = history.elements();

			while (e.hasMoreElements())
			{
				b.append(e.nextElement());
				b.append("\n\n");
			}

			Enumeration     e2 = recipients.elements();

			while (e2.hasMoreElements())
			{
				String  to = e2.nextElement().toString();

				println("Sending bug report to " + to);

				Message m = new Message();

				m.setHeaderInfo("from", reply_to);
				m.setHeaderInfo("date", now.toString());
				m.setHeaderInfo("subject", title);
				m.setMessage(b.toString());
				m.setHeaderInfo("to", to);


				SendEmailInterface emailer = (SendEmailInterface) getServiceImpl(getString("send_email_service_name"));
				emailer.sendEmailMessage(m);
			}

			history = new Vector();

			Date    nextDate = schedule.getNextDate(now);

//			println("Next scheduled history reset: " + DateTools.getShortDateTime(nextDate, true, true));
		}
	}
}

