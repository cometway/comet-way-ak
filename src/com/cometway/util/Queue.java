
package com.cometway.util;

import java.util.*;

/**
 * This is a jdk1.1/1.0 implementation of a Queue data object.
 */
public class Queue extends Vector
{
	public Queue()
	{
		super();
	}


	public synchronized Object nextElement() throws NoSuchElementException
	{
		try
		{
			Object  rval = firstElement();

			removeElementAt(0);

			return (rval);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			throw (new NoSuchElementException());
		}
	}


}

