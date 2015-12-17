
package com.cometway.swing;

import com.cometway.ak.*;
import com.cometway.props.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;


/**
* This agent is used by the StartupEditor agent application.
* This agent can be used to present a JFrame containing a JTable-based Agent
* Props editor. The edit_props property must be set to reference the Props
* that will be edited before this agent is started.
*/

public class PropsEditor extends AbstractJFrameAgent
{
	static final String[] EDIT_MENU = { "Edit", "Cut", "Copy", "Paste", "Clear", "-", "Select All" };
	static final String[] PROPS_MENU = { "Props", "Set Defaults", "-", "Create Property", "Duplicate Property", "Delete Property"};
	static final String[][] MENUBAR = { EDIT_MENU, PROPS_MENU };

	Props editProps;
	JTable propsTable;
	PropsTableModel model;
	Props clipboard;


	/**
	* Initializes the Props for this agent.
	*/
	
	public void initProps()
	{
		setDefault("frame_width", "800");
		setDefault("frame_height", "400");
		setDefault("frame_visible", "false");
	}


	/**
	* Creates a Agent Props Editor JFrame for the specified edit_props.
	*/
	
	public void start()
	{
		editProps = (Props) getProperty("edit_props");

		String frame_title = "Edit Agent: " + editProps.getString("agent_id") + "_" + editProps.getString("name");
		setProperty("frame_title", frame_title);

		openFrame();
	}


	/**
	* Creates the ActionListener for interpreting menu events used
	* by this agent.
	*/
	
	protected ActionListener createMenuActionListener()
	{
		ActionListener l = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				String command = event.getActionCommand();
				debug("Command: " + command);

				if (command.equals("Cut"))
				{
					copyProperty();
					deleteProperty();
				}
				else if (command.equals("Copy"))
				{
					copyProperty();
				}
				else if (command.equals("Paste"))
				{
					pasteProperty();
				}
				else if (command.equals("Select All"))
				{
					propsTable.selectAll();
				}
				else if (command.equals("Set Defaults"))
				{
					setDefaults();
				}
				else if (command.equals("Create Property"))
				{
					createProperty();
				}
				else if (command.equals("Duplicate Property"))
				{
					duplicateProperty();
				}
				else if (command.equals("Delete Property") || command.equals("Clear"))
				{
					deleteProperty();
				}
			}
		};

		return (l);
	}


	/**
	* Copies the selected properties to an internal clipboard.
	*/
	
	private void copyProperty()
	{
		clipboard = new Props();
		int rows[] = propsTable.getSelectedRows();

		for (int i = 0; i < rows.length; i++)
		{
			int row = rows[i];
			String key = (String) propsTable.getValueAt(row, 0);
			debug("Copying " + key);

			clipboard.setProperty(key, editProps);
		}
	}


	/**
	* Creates a new property and adds it to the Props editor table.
	*/

	private void createProperty()
	{
		String key = getUniqueKey("_key");
		editProps.setProperty(key, "_value");
		
		model.setProps(editProps);
	}


	/**
	* Removes selected properties from the editor table.
	*/

	private void deleteProperty()
	{
		int rows[] = propsTable.getSelectedRows();

		for (int i = 0; i < rows.length; i++)
		{
			int row = rows[i];
			String key = (String) propsTable.getValueAt(row, 0);
			debug("Deleting " + key);
			editProps.removeProperty(key);
		}

		model.setProps(editProps);
	}



	/**
	* Duplicates selected properties and adds them to the editor table.
	*/
	
	private void duplicateProperty()
	{
		int rows[] = propsTable.getSelectedRows();

		for (int i = 0; i < rows.length; i++)
		{
			int row = rows[i];
			String key = (String) propsTable.getValueAt(row, 0);
			debug("Duplicating " + key);

			String newKey = getUniqueKey(key);
			editProps.setProperty(newKey, editProps, key);
		}

		model.setProps(editProps);
	}


	/**
	* Pastes any properties from the internal clipboard to the editor table.
	*/

	private void pasteProperty()
	{
		if (clipboard != null)
		{
			Vector clipboardKeys = clipboard.getKeys();

			for (int i = 0; i < clipboardKeys.size(); i++)
			{
				String key = (String) clipboardKeys.elementAt(i);
				debug("Pasting " + key);
				String newKey = getUniqueKey(key);
				editProps.setProperty(newKey, clipboard, key);
			}
	
			model.setProps(editProps);
		}
	}


	/**
	* Creates a unique property name.
	*/
	
	private String getUniqueKey(String key)
	{
		if (editProps.hasProperty(key) == true)
		{
			String newKey;
			int i = 1;
	
			do
			{
				newKey = key + i;
				i++;
			}
			while (editProps.hasProperty(newKey));

			key = newKey;
		}

		return (key);
	}


	/**
	* Creates and initializes the Props Editor JFrame.
	*/

	protected void openFrame()
	{
		super.openFrame();
		menuActionListener = createMenuActionListener();
		setMenuBar(MENUBAR);

		model = new PropsTableModel(editProps);
		propsTable = new JTable(model);

		JScrollPane scrollPane = new JScrollPane(propsTable);
		scrollPane.getViewport().setBackground(Color.white);

		Container c = frame.getContentPane();
		c.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		c.add(scrollPane);

		frame.setVisible(true);
	}


	/**
	* Pastes any properties from the internal clipboard to the editor table.
	*/

	private void setDefaults()
	{
		AK.getAgentKernel().createAgent(editProps).destroy();
		editProps.removeProperty("current_state");
		editProps.removeProperty("next_state");
		editProps.setDefault("hide_println", "false");
		editProps.setDefault("hide_debug", "false");
		editProps.setDefault("hide_warning", "false");
		model.setProps(editProps);
	}
}


