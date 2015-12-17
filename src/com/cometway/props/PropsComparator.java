
package com.cometway.props;


import java.util.Comparator;
import java.util.Date;


/**
* Enabled Props to be compared to other Props based on key values
* using the Collections API.
*/

public class PropsComparator implements Comparator
{
	protected String key;


	public PropsComparator(String key)
	{
		this.key = key;
	}
	
	
	public int compare(Object o1, Object o2)
	{
		int result;

		if (o1 == null)
		{
			result = 1;
		}
		else if (o2 == null)
		{
			result = -1;
		}
		else if ((o1 instanceof Props) && (o2 instanceof Props))
		{
			Props p1 = (Props) o1;
			Props p2 = (Props) o2;

			o1 = p1.getProperty(key);
			o2 = p2.getProperty(key);

			if ((o1 instanceof String) && (o2 instanceof String))
			{
				result = ((String) o1).compareTo((String) o2);
			}
			else if ((o1 instanceof Integer) && (o2 instanceof Integer))
			{
				result = ((Integer) o1).compareTo((Integer) o2);
			}
			else if ((o1 instanceof Boolean) && (o2 instanceof Boolean))
			{
				boolean b1 = ((Boolean) o1).booleanValue();
				boolean b2 = ((Boolean) o2).booleanValue();

				if (b1 == b2)
				{
					result = 0;
				}
				else
				{
					if (b1)
					{
						result = 1;
					}
					else
					{
						result = -1;
					}
				}

				// this works only in JDK 1.5 and higher.

//				result = ((Boolean) o1).compareTo(o2);
			}
			else if ((o1 instanceof Long) && (o2 instanceof Long))
			{
				result = ((Long) o1).compareTo((Long) o2);
			}
			else if ((o1 instanceof Double) && (o2 instanceof Double))
			{
				result = ((Double) o1).compareTo((Double) o2);
			}
			else if ((o1 instanceof Float) && (o2 instanceof Float))
			{
				result = ((Float) o1).compareTo((Float) o2);
			}
			else if ((o1 instanceof Character) && (o2 instanceof Character))
			{
				result = ((Character) o1).compareTo((Character) o2);
			}
			else if ((o1 instanceof Date) && (o2 instanceof Date))
			{
				result = ((Date) o1).compareTo((Date) o2);
			}
			else
			{
				String s1 = p1.getString(key);
				String s2 = p2.getString(key);

				result = s1.compareTo(s2);
			}
		}
		else
		{
			throw new IllegalArgumentException("Cannot compare non-Props arguments: "
			 + "\n" + o1.getClass().getName() + " o1 = " + o1
			 + "\n" + o2.getClass().getName() + " o2 = " + o2);
		}

		return (result);
	}


	public boolean equals(Object o)
	{
		boolean result = false;

		if (o instanceof PropsComparator)
		{
			result = key.equals(((PropsComparator) o).key);
		}

		return (result);
	}
}



