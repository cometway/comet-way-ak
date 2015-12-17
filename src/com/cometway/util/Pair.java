
package com.cometway.util;

import java.lang.*;


/**
 * This class implements a tuple. Pairs keep related objects together. 
 * Pairs are essentially nodes of a b-tree.
 * N-tuples can be implemented by an unbalanced b-tree.
 */

public class Pair
{


/**
 * This is the first object of the pair
 */

	private Object  first;


	/**
	 * This is the second object of the pair
	 */

	private Object  second;

	public Pair() {}


	/**
	 * This constructor sets the first and second objects with the parameters.
	 */


	public Pair(Object theFirst, Object theSecond)
	{
		first = theFirst;
		second = theSecond;
	}


	/**
	 * Returns the first object in the pair
	 */


	public Object first()
	{
		return (first);
	}


	/**
	 * Returns the second object in the pair
	 */


	public Object second()
	{
		return (second);
	}


	/**
	 * Sets the first object of the pair
	 */


	public void setFirst(Object theObject)
	{
		first = theObject;
	}


	/**
	 * Sets the second object of the pair
	 */


	public void setSecond(Object theObject)
	{
		second = theObject;
	}


	/**
	 * overrides Object.toString(), prints: (first,second)
	 */


	public String toString()
	{
		return ("(" + first + ", " + second + ")");
	}


	/**
	 * tries to construct the pair (first and second) from a string
	 */


	public static String makeString(Pair thePair)
	{
		String  str;

		if (thePair == null)
		{
			str = "(null, null)";
		}
		else
		{
			str = "(" + thePair.first + ", " + thePair.second + ")";
		}

		return (str);
	}


}

