 
package com.cometway.xml;


import com.cometway.ak.AK;
import com.cometway.ak.AgentRequest;
import com.cometway.httpd.HTTPAgentRequest;
import com.cometway.io.StringBufferOutputStream;
import com.cometway.props.Props;
import com.cometway.props.PropsContainer;
import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Vector;


public abstract class MinRPCAgent extends XMLRequestAgent
{
	// These are tag names for primitive types.

	protected static final String STRING = "s";
	protected static final String BOOLEAN = "b";
	protected static final String INTEGER = "i";
	protected static final String DOUBLE = "d";
	protected static final String DATETIME = "t";
	protected static final String BASE64 = "base64";


	// These are tag names for a syntax response.

	protected static final String SYNTAX = "syntax";
	protected static final String METHOD = "method";
	protected static final String DESCRIPTION = "description";
	protected static final String REQUIRED = "required";
	protected static final String OPTIONAL = "optional";
	protected static final String DEFAULTS = "defaults";
	protected static final String RETURNS = "returns";


	// These are tag names for a RPC response.

	protected static final String RESPONSE = "response";


	// These are tag names for an error response.

	protected static final String ERROR = "error";
	protected static final String MESSAGE = "message";
	protected static final String STACKTRACE = "stacktrace";
	


	public void start()
	{
		setProperty("method", new PropsContainer());

		registerMethods();

		register();
	}


	public abstract void registerMethods();


	protected void registerMethod(String method)
	{
		setProperty("method:" + method, method);
	}


	protected void registerMethod(String registeredMethod, String localMethod)
	{
		setProperty("method:" + registeredMethod, localMethod);
	}


	protected void writeSyntax(AgentRequest request)
	{
		request.setContentType("text/xml");
		writeXMLHeader(request);

		writeStartTag(request, SYNTAX);
		writeEndTag(request, SYNTAX);
	}


	protected void writeErrorResponse(AgentRequest request, Exception e)
	{
		request.setContentType("text/xml");
		writeXMLHeader(request);

		StringBuffer b = new StringBuffer();
		PrintWriter out = new PrintWriter(new StringBufferOutputStream(b));
		e.printStackTrace(out);
		out.flush();

		writeStartTag(request, ERROR);
		writeElement(request, MESSAGE, e.getMessage());
		writeElement(request, STACKTRACE, b.toString());
		writeEndTag(request, ERROR);
	}


	protected void writeResponse(AgentRequest request) throws XMLParserException
	{
		request.setDefault("method", getString("default_method"));

		String method = request.getString("method");
//debug("method = " + method);

		if (method.length() > 0)
		{
			String localMethod = getString("method:" + method);
//debug("localMethod = " + localMethod);

			try
			{
				Class params[] = { AgentRequest.class };
				Method m = getClass().getMethod(localMethod, params);

				Object args[] = new Object[1];
				args[0] = request;
				m.invoke(this, args);

				request.setBoolean("method_completed", true);
			}
			catch (NoSuchMethodException e)
			{
				throw new XMLParserException("Invalid method: " + method, e);
			}
			catch (SecurityException e)
			{
				throw new XMLParserException("Access denied: " + method, e);
			}
			catch (Exception e)
			{
				error("writeResponse", e);
				throw new XMLParserException("Exception in " + method, e);
			}
		}
	}


	public void handleRequest(AgentRequest request)
	{
		println("Received request from " + request.getString("request_server_name"));

		HTTPAgentRequest req = (HTTPAgentRequest) request;
		XMLParser parser = new XMLParser(req.getRequestBody());

		StringBuffer responseBuffer = new StringBuffer();
		StringBufferOutputStream out = new StringBufferOutputStream(responseBuffer);
		AgentRequest methodRequest = new AgentRequest(new Props(), out);

		try
		{
			XMLToken token = parser.nextToken(XML.XML_10_HEADER);
			token = parser.nextToken();

			if (token.data.equals("<syntax/>"))
			{
				writeSyntax(methodRequest);
			}
			else if (token.data.equals("<request>"))
			{
				while (true)
				{
					token = parser.nextToken();
		
					if (token.data.equals("</request>"))
					{
//debug("writing response");
						writeResponse(methodRequest);
//debug("completed");
						break;
					}
					else
					{
						if (token.type == XML.START_TAG)
						{
							String key = token.data.substring(1, token.data.length() - 1);
//debug("key = " + key);
							token = parser.nextElementContent();
							
//debug("value = " + token.data);
							methodRequest.setProperty(key, token.data);
		
							token = parser.nextEndTag();
		
							String s = token.data.substring(2, token.data.length() - 1);
							
							if (s.equals(key) == false)
							{
								throw new XMLParserException("Non-matching tags: \"" + key + "\" and \"" + s + "\"");
							}
						}
						else
						{
							throw new XMLParserException("Invalid token: " + token.data);
						}
					}
				}
			}
			else
			{
				throw new XMLParserException("Unknown token: " + token.data);
			}
		}
		catch (Exception e)
		{
			if (e instanceof XMLParserException)
			{
				XMLParserException ex = (XMLParserException) e;

				if (ex.getOriginalException() instanceof EOFException)
				{
					warning("Reached End of Stream", ex);
					writeErrorResponse(methodRequest, ex);
				}
				else
				{
					error("Min-RPC Parsing Problem", ex);
					writeErrorResponse(methodRequest, ex.getOriginalException());
				}
			}
			else
			{
				error("Min-RPC Exception", e);
				writeErrorResponse(methodRequest, e);
			}
		}

		StringBuffer b = new StringBuffer();
		b.append("HTTP/1.1 200 OK");
		b.append("\nConnection: close");
		b.append("\nContent-Length: ");
		b.append(responseBuffer.length());
		b.append("\nContent-Type: text/xml");
		b.append("\nDate: ");
		b.append(getDateTimeStr());
		b.append("\nServer: ");
		b.append(AK.VERSION_INFO);
		b.append("\n\n");
		b.append(responseBuffer.toString());
//println(b.toString());
		req.println(b.toString());
	}
}


