
package com.cometway.email;

import java.util.*;


/**
 * This helper class provides a suite of utility methods for accessing
 * and changing an IMessage.
 */
public class MessageTools
{
	private IMessage	message;

	public MessageTools()
	{
		message = null;
	}


	public MessageTools(IMessage message)
	{
		this.message = message;
	}


	public void addRecipient(String key, String username)
	{
		String  s = message.getHeaderInfo(key);

		if (s.length() == 0)
		{
			s = username;
		}
		else
		{
			s += ", " + username;
		}

		message.setHeaderInfo(key, s);
	}


	/**
	 * * Returns true if the char 'c' is a character commonly used for quoting.
	 */


	public static boolean isQuotingChar(char c)
	{
		return ((c == ' ') || (c == '>') || (c == ':'));
	}


	public String getHeadersString()
	{
		StringBuffer    str;
		String		s;

		str = new StringBuffer();

		Enumeration     e = message.getHeaders();

		while (e.hasMoreElements())
		{
			s = (String) e.nextElement();

			str.append(s + ": " + message.getHeaderInfo(s) + '\n');
		}

		return (str.toString());
	}


	public IMessage getMessage()
	{
		return (message);
	}


	public Enumeration getRecipients(String key)
	{
		String  s;

		if (key.equalsIgnoreCase("all"))
		{
			s = "To: " + message.getHeaderInfo("To") + ',' + message.getHeaderInfo("CC") + ',' + message.getHeaderInfo("BCC");
		}
		else
		{
			s = "To: " + message.getHeaderInfo(key);
		}

		return (EmailHeader.getSendToUsers(s));
	}


	public String getReplyTo()
	{
		String  s = message.getHeaderInfo("Reply To");

		if (s.length() == 0)
		{
			s = message.getHeaderInfo("From");
		}

		return (s);
	}


	public void setHeaderInfo(String headerInfo)
	{
		String  key;
		String  value;

		message.removeAllHeaders();

		while (headerInfo.indexOf(":") != -1)
		{
			key = headerInfo.substring(0, headerInfo.indexOf(":")).trim();		// System.out.println("Got header name:    '" + key + "'");
			headerInfo = headerInfo.substring(headerInfo.indexOf(":") + 1);
			value = headerInfo.substring(0, headerInfo.indexOf("\n")).trim();		// System.out.println("Got header content: '" + value + "'");
			headerInfo = headerInfo.substring(headerInfo.indexOf("\n") + 1);

			message.setHeaderInfo(key, value);
		}
	}


	public boolean isHeaderField(String header)
	{
		int     delimit = header.indexOf(":");

		if (delimit == -1)
		{
			return (false);
		}
		else
		{
			for (int x = 0; x < delimit; x++)
			{
				if ((header.charAt(x) == ' ') || (Character.isISOControl(header.charAt(x))))
				{
					return (false);
				}
			}
		}

		return (true);
	}


	public void setEntireMessage(String body)
	{
		int     index1 = 0;
		int     index2 = body.indexOf("\n");
		int     tempindex = 0;
		int     maxlength = body.length();
		String  line = "";

		if (body.charAt(maxlength - 1) != '\n')
		{
			body = body + "\n";
		}

		message.removeAllHeaders();

		while (index2 != -1)
		{
			tempindex = index1;

			while ((index2 < maxlength - 1) && ((body.charAt(index2 + 1) == ' ') || (body.charAt(index2 + 1) == '\t')))
			{
				line = line + body.substring(index1, index2 - 1).trim() + " ";
				index1 = index2 + 1;
				index2 = body.indexOf("\n", index1);		// Technically this shouldn't happen because body.charAt(body.length()) is always \n

				if (index2 == -1)
				{
					index2 = maxlength;
					index1 = index2 - 1;

					message.setMessage(body.substring(tempindex).trim());

					break;
				}
			}

			line = line + body.substring(index1, index2).trim();		// Check if header is a valid header

			if (isHeaderField(line))
			{
				message.setHeaderInfo(line.substring(0, line.indexOf(":")).trim(), line.substring(line.indexOf(":") + 1).trim());

				line = "";
			}
			else
			{		// either not a header or bad formatting, assume message body starts.
				message.setMessage(body.substring(tempindex).trim());

				break;
			}

			if (index2 < maxlength)
			{
				index1 = index2 + 1;
				index2 = body.indexOf("\n", index1);

				if (index1 == index2)
				{
					message.setMessage(body.substring(index2).trim());

					break;
				}
			}
			else
			{
				message.setMessage("");

				break;
			}
		}

		if (index2 == -1)
		{
			message.setMessage("");
		}
	}


	public void setMessage(IMessage message)
	{
		this.message = message;
	}


	public Vector splitMessage(int max_length, boolean preserve_newlines)
	{
		Vector  split_messages = new Vector();
		String  body = message.getMessage();
		String  subject = "";

		if (!((message.getHeaderInfo("Subject") == null) || (message.getHeaderInfo("Subject").equals(""))))
		{
			subject = message.getHeaderInfo("Subject");
		}

		if (body.length() <= max_length)
		{
			split_messages.addElement(message);
		}
		else
		{
			while (body.length() > max_length)
			{
				String  temp = body.substring(0, max_length);

				body = body.substring(max_length);

				if (preserve_newlines)
				{
					if (body.charAt(0) == '\n')
					{
						body = body.substring(1);
					}
					else if (temp.charAt(max_length - 1) != '\n')
					{
						int     i = temp.lastIndexOf("\n");

						if (i != -1)
						{
							body = temp.substring(i + 1) + body;
							temp = temp.substring(0, i);
						}
					}
				}

				Message msg = new Message();

				msg.setMessage(temp);

				Enumeration     headers = message.getHeaders();

				while (headers.hasMoreElements())
				{
					String  name = (String) headers.nextElement();
					String  val = (String) message.getHeaderInfo(name);

					msg.setHeaderInfo(name, val);
				}

				split_messages.addElement(msg);
			}
		}

		if (split_messages.size() > 1)
		{
			for (int z = 0; z < split_messages.size(); z++)
			{
				IMessage	tempmsg = (IMessage) split_messages.elementAt(z);

				tempmsg.setHeaderInfo("Message-Split", (z + 1) + "/" + split_messages.size() + " Split by MessageTools, Comet Way Inc. 2003");
				tempmsg.setHeaderInfo("Subject", subject + "  (" + (z + 1) + "/" + split_messages.size() + ")");
			}
		}

		return (split_messages);
	}


	public static Vector splitMessage(String message, int max_length, boolean preserve_newlines)
	{
		Vector  split_messages = new Vector();

		if (message.length() <= max_length)
		{
			split_messages.addElement(message);
		}
		else
		{
			while (message.length() > max_length)
			{
				String  temp = message.substring(0, max_length);

				message = message.substring(max_length);

				if (preserve_newlines)
				{
					if (message.charAt(0) == '\n')
					{
						message = message.substring(1);
					}
					else if (temp.charAt(max_length - 1) != '\n')
					{
						int     i = temp.lastIndexOf("\n");

						if (i != -1)
						{
							message = temp.substring(i + 1) + message;
							temp = temp.substring(0, i);
						}
					}
				}

				split_messages.addElement(temp);
			}
		}

		return (split_messages);
	}


}

