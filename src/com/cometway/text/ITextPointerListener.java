
package com.cometway.text;


/**
 * Listener interface for receiving notifications about changes to a text pointer.
 */

public interface ITextPointerListener
{


/**
 * Called by text pointers when they have moved.
	* @param p a reference to the text pointer that changed.
 */

	public void textPointerChanged(TextPointer p);
}

