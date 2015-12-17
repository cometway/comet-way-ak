
package com.cometway.ak;


import com.cometway.props.Props;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;


/**
* Represents a concrete data class used to pass information
* to and from agents that implement the RequestAgentInterface.
*/

public class AgentRequest extends AgentMessage
{
	protected OutputStream out;
	protected OutputStream err;
	protected PrintWriter writer;


	/**
	* Creates a request instance using its own Props.
	*/

	public AgentRequest()
	{
		props = this;
		out = System.out;
		err = System.err;
	}


	/**
	* Creates a request instance referencing the specified Props.
	*/

	public AgentRequest(Props props)
	{
		setPropsContainer(props.getPropsContainer());
		this.props = this;
		this.out = System.out;
		this.err = System.err;
	}


	/**
	* Creates a request instance referencing the specified Props and standard OutputStream.
	*/

	public AgentRequest(Props props, OutputStream out)
	{
		setPropsContainer(props.getPropsContainer());
		this.props = this;
		this.out = out;
		this.err = System.err;
	}


	/**
	* Creates a request instance referencing the specified Props, standard OutputStream,
	* and error OutputStream.
	*/

	public AgentRequest(Props props, OutputStream out, OutputStream err)
	{
		setPropsContainer(props.getPropsContainer());
		this.props = this;
		this.out = out;
		this.err = err;
	}


	/**
	* Returns a reference to the error OutputStream.
	*/

	public OutputStream getErrorStream()
	{
		return (err);
	}


	/**
	* Returns a reference to the standard OutputStream.
	*/

	public OutputStream getOutputStream()
	{
		return (out);
	}


	/**
	* Returns the <I>type</I> assigned to this request by its sender.
	*/

	public String getRequestType()
	{
		return (getString("request_type"));
	}


	/**
	* Returns a reference to the Writer assigned to this request.
	*/

	public Writer getWriter()
	{
		if (writer == null)
		{
			writer = new PrintWriter(out);
		}

		return (writer);
	}


	/**
	* Returns true if this request's type matches the specified type;
	* false otherwise.
	*/

	public boolean isRequestType(String request_type)
	{
		return (getString("request_type").equals(request_type));
	}


	/**
	* Prints the String to the agent's standard OutputStream using a PrintWriter.
	*/

	public void print(String s)
	{
		if (writer == null)
		{
			writer = new PrintWriter(out);
		}

		writer.print(s);
		writer.flush();
	}


	/**
	* Prints a newline to the agent's standard OutputStream using a PrintWriter.
	*/

	public void println()
	{
		if (writer == null)
		{
			writer = new PrintWriter(out);
		}

		writer.println();
		writer.flush();
	}


	/**
	* Prints the String followed by a newline to the agent's standard OutputStream using a PrintWriter.
	*/

	public void println(String s)
	{
		if (writer == null)
		{
			writer = new PrintWriter(out);
		}

		writer.println(s);
		writer.flush();
	}


	/**
	* Specifies the content type for output to the standard OutputStream.
	*/

	public void setContentType(String type)
	{
		setProperty("content_type", type);
	}
}

