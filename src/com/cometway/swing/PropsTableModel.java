
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
* This is a swing TableModel implementation for editing Props.
*/

public class PropsTableModel extends AbstractTableModel
{
	Vector columnNames;
	Vector keys;
	Props props;


	/**
	* Constructor for PropsTableModel. There is one column for
	* Key, and another for Value.
	* @param p The Props to be rendered by a JTable.
	*/
	
	public PropsTableModel(Props p)
	{
		columnNames = new Vector();
		columnNames.addElement("Key");
		columnNames.addElement("Value");

		setProps(p);
	}


	/**
	* Returns the number of columns (2).
	*/
	
	public int getColumnCount()
	{
		return (columnNames.size());
	}


	/**
	* Returns the Class of data in the specified Column (java.lang.String).
	*/

	public Class getColumnClass(int column)
	{
		return (String.class);
	}


	/**
	* Returns the name of the specified column.
	*/
	
	public String getColumnName(int column)
	{
		return ((String) columnNames.elementAt(column));
	}


	/**
	* Returns the number of rows in this TableModel.
	*/
	
	public int getRowCount()
	{
		return (keys.size());
	}


	/**
	* Returns the value of the specified row and column.
	*/

	public Object getValueAt(int row, int col)
	{
		Object value = null;

		switch (col)
		{
			case 0:
				value = keys.elementAt(row);
			break;

			default:
				value = props.getProperty((String) keys.elementAt(row));
			break;
		}

		return (value);
	}


	/**
	* Returns true if the specified cell is editable; false otherwise.
	*/
	
	public boolean isCellEditable(int row, int column)
	{
		String key = (String) keys.elementAt(row);

		return (isReservedKey(key) == false);
	}


	/**
	* Returns true if the specified key is "agent_id", "name", "classname",
	* "startup", "hide_println", "hide_warning" or "hide_debug".
	*/

	public boolean isReservedKey(String key)
	{
		boolean result = (key.equals("agent_id") || key.equals("name")
			|| key.equals("classname") || key.equals("service_name")
			|| key.equals("startup") || key.equals("hide_println")
			|| key.equals("hide_warning") || key.equals("hide_debug"));

		return (result);
	}


	/**
	* Call this method to indicate that the values in the table have changed.
	*/

	public void notifyPropsChanged()
	{
		fireTableDataChanged();
	}


	/**
	* Sets the Props this TableModel is rendering.
	*/
	
	public void setProps(Props p)
	{
		props = p;
		keys = props.getKeys();

		sortKeys();
		fireTableDataChanged();
	}


	/**
	* Sets the value of the specified cell.
	*/
	
	public void setValueAt(Object value, int row, int col)
	{
		String key = (String) keys.elementAt(row);

		if (col == 0)
		{
			String newKey = value.toString();

			if (props.hasProperty(newKey) == false)
			{
				Object o = props.getProperty(key);
				props.removeProperty(key);
				props.setProperty(newKey, o);
	
				keys.setElementAt(value, row);
				sortKeys();
				fireTableDataChanged();
			}
		}
		else if (col == 1)
		{
			props.setProperty(key, value);
			fireTableCellUpdated(row, col);
		}
	}


	/**
	* Sorts the keys in alphabetical order.
	*/

	public void sortKeys()
	{
		for (int a = 0; a < keys.size() - 1; a++)
		{
			String first = (String) keys.elementAt(a);

			for (int b = a + 1; b < keys.size(); b++)
			{
				String second = (String) keys.elementAt(b);

				if (first.compareTo(second) > 0)
				{
					keys.setElementAt(second, a);
					keys.setElementAt(first, b);
					first = second;
				}
			}
		}
	}
}


