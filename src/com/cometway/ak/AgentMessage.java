
package com.cometway.ak;


import com.cometway.props.Props;


/**
* This data class represents a set of Properties that
* can be passed from one Agent to another Agent.
*/

public class AgentMessage extends Props
{
	protected Props props;


	/**
	* Creates a request instance using its own Props.
	*/

	public AgentMessage()
	{
		props = this;
	}


	/**
	* Creates a request instance referencing the specified Props.
	*/

	public AgentMessage(Props props)
	{
		setPropsContainer(props.getPropsContainer());

		this.props = this;
	}



	/**
	* Returns a reference to the Props assigned to this request.
	*/

	public Props getProps()
	{
		return (props);
	}
}


