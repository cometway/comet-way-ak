
package com.cometway.text;


/**
 * Listener interface for receiving notifications about changes to text buffers.
 */

public interface ITextBufferListener
{


	/**
	* Called by text buffers when a portion of their contents have changed.
	* @param b a reference to the text buffer that changed.
	* @param start the starting index of the changed text.
	* @param end the ending index of the changed text.
	*/

	public void textChanged(ITextBuffer b, int start, int end);


	/**
	* Called by text buffers when a portion of their contents have moved.
	* @param b a reference to the text buffer that moved.
	* @param start the starting index of the moved text.
	* @param offset the relative number of characters the text has moved towards the end of the buffer.
	*/

	public void textMoved(ITextBuffer b, int start, int offset);
}

