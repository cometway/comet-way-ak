
package com.cometway.swing;

import com.cometway.ak.*;
import com.cometway.props.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;



/**
* This abstract Agent contains basic functionality useable
* by an agent that manages a JFrame. Methods in this abstract
* implementation can be overridden to create and manage any subclass of JFrame.
* Convenience methods are provided for handling menus and the menubar,
* as well as the Look and Feel used by Swing.
*/

public abstract class AbstractJFrameAgent extends Agent
{
	protected JFrame frame;
	protected JMenuBar menuBar;
	protected JMenu[] menu;
	protected WindowListener windowListener;
	protected ActionListener menuActionListener;


	/**
	* Initializes the Props for this agent.
	* The created JFrame will be given the attributes specified
	* by the frame_title, frame_width, frame_height, and frame_visible
	* properties.
	*/
	
	public void initProps()
	{
		setDefault("frame_title", "Untitled");
		setDefault("frame_width", "512");
		setDefault("frame_height", "384");
		setDefault("frame_visible", "true");
	}


	/**
	* Initializes this agent and pens the JFrame managed by this agent.
	*/
	
	public void start()
	{
		openFrame();
	}


	/**
	* Closes the JFrame managed by this agent, and stops this agent.
	*/
	
	public void stop()
	{
		closeFrame();
	}


	/**
	* Closes the JFrame managed by this agent.
	*/
	
	protected void closeFrame()
	{
		frame.dispose();

		frame = null;
		menuBar = null;
		menu = null;
		menuActionListener = null;
	}


	/**
	* Creates a JMenu from the specified String array of menu data.
	* The first item is the menu title, the following items are menu items.
	* Items that are set to "-" are interpreted as menu item separators.
	*/
	
	protected JMenu createMenu(String[] menuData)
	{
		JMenu menu = new JMenu(menuData[0]);

		for (int i = 1; i < menuData.length; i++)
		{
			if (menuData[i].equals("-"))
			{
				menu.addSeparator();
			}
			else
			{
				JMenuItem item = new JMenuItem(menuData[i]);
				item.addActionListener(menuActionListener);
				menu.add(item);
			}
		}

		return (menu);
	}


	/**
	* Creates the ActionListener for handling menu events.
	*/
	
	protected ActionListener createMenuActionListener()
	{
		ActionListener l = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				String command = event.getActionCommand();
				debug("Command: " + command);
			}
		};

		return (l);
	}


	/**
	* Creates the WindowListener for handling window events.
	*/
	
	protected WindowListener createWindowListener()
	{
		WindowListener l = new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				getAgentController().stop();
			}
		};

		return (l);
	}


	/**
	* Creates the JFrame instance managed by this agent.
	*/

	protected JFrame createFrame()
	{
		return (new JFrame());
	}


	/**
	* Creates, initializes, and opens a JFrame instance managed by this agent.
	* The frame_title, frame_width, frame_height, and frame_visible Props
	* are used accordingly by this method.
	*/
	
	protected void openFrame()
	{
		frame = createFrame();

		windowListener = createWindowListener();

		frame.addWindowListener(windowListener);
		frame.setTitle(getString("frame_title"));

		int frame_width = getInteger("frame_width");
		int frame_height = getInteger("frame_height");

		if ((frame_width == 0) || (frame_height == 0))
		{
			frame.pack();
		}
		else
		{
			frame.setSize(frame_width, frame_height);
		}

		frame.setVisible(getBoolean("frame_visible"));
	}


	/**
	* Creates and sets the JMenuBar for this frame using an
	* array of menu description String arrays. See createMenu
	* for a description of the menu String array.
	*/
	
	protected void setMenuBar(String[][] menuBarDesc)
	{
		int menuCount = menuBarDesc.length;

		menuBar = new JMenuBar();
		menu = new JMenu[menuCount];

		for (int i = 0; i < menuCount; i++)
		{
			menu[i] = createMenu(menuBarDesc[i]);
			menuBar.add(menu[i]);
		}

		frame.setJMenuBar(menuBar);
	}


	/**
	* The Look and Feel can be specified globally by specifying the
	* lnf_classname Props. If lnf_classname is set to "java", the
	* standard cross-platform look and feel (Metal) will be used.
	* If lnf_classname is set to "system", the system native look and
	* feel will be used.
	*/
	
	protected void setLookAndFeel()
	{
		String lnf_classname = getString("lnf_classname");

		try
		{
			if (lnf_classname.equals("java"))
			{
				lnf_classname = UIManager.getCrossPlatformLookAndFeelClassName();
			}
			else if (lnf_classname.equals("system"))
			{
				lnf_classname = UIManager.getSystemLookAndFeelClassName();
			}

			println("Setting look and feel to " + lnf_classname);

			UIManager.setLookAndFeel(lnf_classname);
		}
		catch (Exception e)
		{
			error("Could not set Look and Feel: " + lnf_classname, e);
		}
	}
}


