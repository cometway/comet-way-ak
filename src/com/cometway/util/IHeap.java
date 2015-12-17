
package com.cometway.util;

import java.util.*;


/**
 * IHeap is an interface for a heap
 */

public interface IHeap
{
	/**
	* Adds a IHeapItem to this heap.
	*
	* @returns true if the insert() resulted in a new minimum heap element
	*/

	public boolean insert(IHeapItem newItem);


	/**
	 * Deletes a specific heap item from this heap
	 *
	 * @returns true if the delete() resulted in a new minimum heap element
	 */

	public boolean delete(IHeapItem deleteItem);


	/**
	 * Method returns the minimum IHeapItem in this heap
	 *
	 *  @returns the minimum IHeapItem in this heap
	 */

	public IHeapItem findMin();


	/**
	 * Removes and returns the minimum IHeapItem in this heap
	 *
	 *  @returns the minimum IHeapItem in this heap
	 */

	public IHeapItem deleteMin();


	/**
	* Method returns true if this heap contains no elements and false
	*   otherwise
	*
	*  @returns true if this heap contains no elements and false
	*   otherwise
	*/

	public boolean isEmpty();


	/**
	 * size: method returns the number of elements in this heap
	 */

	public int size();
}

