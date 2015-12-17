
package com.cometway.swing;

import com.cometway.ak.*;
import com.cometway.props.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;


/**
* This class acts as an agent-based proxy for a swing JFrame. It creates
* an inner class called ControlledFrame whose paint method is proxied to the
* drawFrame method which is intended to be overridden by subclasses of this class.
* The createFrame method can also be overridden if more control over the JFrame is needed.
* This agent registers itself with the Service Manager and calls createFrame upon starting.
*/


public abstract class AbstractJFrameController extends AbstractJFrameAgent
{
	/** Initializes Props for this agent. */
	
	public void initProps()
	{
		setDefault("service_name", "frame_controller");
		setDefault("frame_title", "Untitled");
		setDefault("frame_width", "512");
		setDefault("frame_height", "384");
		setDefault("frame_visible", "true");
	}


	/**
	* Opens a Frame and registers this agent with the Service Manager.
	*/
	
	public void start()
	{
		openFrame();
		ServiceManager.register(getString("service_name"), this);
	}


	/**
	* Unregisters this agent with the Service Manager and closes the Frame.
	*/

	public void stop()
	{
		ServiceManager.unregister(getString("service_name"), this);
		closeFrame();
	}


	/**
	* Creates a JFrame that is controlled by this agent. By default, the
	* returned frame is a subclass of JFrame which has been overridden to
	* call the drawFrame method of this agent whenever JFrame.paint is called.
	*/

	protected JFrame createFrame()
	{
		return (new ControlledFrame());
	}


	/**
	* Called by the JFrame controlled by this agent when its paint method is called.
	*/

	public abstract void drawFrame(Graphics g);


	/**
	* Returns a reference to the JFrame instance controlled by this agent.
	*/

	public JFrame getFrame()
	{
		return (frame);
	}


	private class ControlledFrame extends JFrame
	{
		public void paint(Graphics g)
		{
			drawFrame(g);
		}
	}
}


