package com.cometway.net;



/**
 * This exception is thrown by the ESMTPSender in the event that the 
 * message that is to be sent is refused by the SMTP server.
 */
public class ESMTPException extends RuntimeException
{
	public static final int CONNECT = 2;
	public static final int HELO    = 3;
	public static final int EHLO    = 5;
	public static final int FROM    = 7;
	public static final int TO      = 11;
	public static final int DATA    = 13;
	public static final int RSET    = 17;
	public static final int AUTH    = 20;
	public static final int EXPN    = 19;
	public static final int VRFY    = 23;
	public static final int NOOP    = 29;
	public static final int QUIT    = 31;

	private int type;
	private Object info;

	public ESMTPException(int type)
   {
		this.type = type;
   }

	public ESMTPException(int type, Object info)
   {
		this.type = type;
		this.info = info;
   }

	public int getType()
   {
		return type;
	}

	public Object getInfo()
   {
		return info;
   }

	public String toString()
   {
		String s = "ESMTPException ";

		switch (type)
		{
		case CONNECT:
			s += "CONNECT (" + CONNECT + ")";
			break;
		case HELO:
			s += "HELO (" + HELO + ")";
			break;
		case EHLO:
			s += "EHLO (" + EHLO + ")";
			break;
		case FROM:
			s += "FROM (" + FROM + ")";
			break;
		case TO:
			s += "TO (" + TO + ")";
			break;
		case DATA:
			s += "DATA (" + DATA + ")";
			break;
		case RSET:
			s += "RSET (" + RSET + ")";
			break;
		case AUTH:
			s += "AUTH (" + AUTH + ")";
			break;
		case EXPN:
			s += "EXPN (" + EXPN + ")";
			break;
		case VRFY:
			s += "VRFY (" + VRFY + ")";
			break;
		case NOOP:
			s += "NOOP (" + NOOP + ")";
			break;
		case QUIT:
			s += "QUIT (" + QUIT + ")";
			break;
		default:
			break;
		}

		if (info != null)
			s += "; info: " + info;

		return s;
	}
}
