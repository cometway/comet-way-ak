
package com.cometway.util;


import java.util.Comparator;
import java.util.Map;


public class MapComparator implements Comparator
{
	protected String key;


	public MapComparator(String key)
	{
		this.key = key;
	}
	
	
	public int compare(Object o1, Object o2)
	{
		String key1 = (String) ((Map) o1).get(key);
		String key2 = (String) ((Map) o2).get(key);

		return (key1.compareTo(key2));
	}
}



