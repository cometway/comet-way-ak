
package com.cometway.text;


/**
* This interface defines an interface for reading and manipulating a buffer of text.
*/

public interface ITextBuffer
{
	/**
	* Adds an ITextBufferListener to this object to receive notifications of
	* changes which take place when the text buffer is changed.
	*
	* @param l a reference to an object which implements ITextBufferListener.
	*/

	public void addTextBufferListener(ITextBufferListener l);


	/**
	* Appends the specified string to the end of this object and calls the
	* <CODE>textChanged</CODE> method of its listeners with the range
	* of the appended text.
	*
	* @param str a reference to a String.
	*/

	public void append(String str);


	/**
	* Removes the specified range of text from this object and calls the
	* <CODE>textChanged</CODE> method of its listeners with the location
	* of the removed text. The <CODE>fromIndex</CODE> and <CODE>toIndex</CODE>
	* parameters may be specified in any order.
	*
	* @param fromIndex the starting index from where text will be removed.
	* @param toIndex the ending index to where text will be removed.
	*/

	public void delete(int fromIndex, int toIndex);


	/**
	* Returns the next text range in the text buffer found by
	* the specified text finder starting at the specified index.
	*
	* @param fromIndex the starting index from where search will begin.
	* @param textFinder a reference to an object implementing ITextFinder.
	* @return a reference to a TextRange if a matching range was found; null otherwise.
	*/

	public TextRange findText(int fromIndex, ITextFinder textFinder);


	/**
	* Returns the size of the text buffer managed by this object.
	*
	* @return the length of the text buffer.
	*/

	public int getLength();


	/**
	* Returns a string representing the contents of the text buffer.
	*
	* @return a reference to a String.
	*/

	public String getText();


	/**
	* Returns a string representing the contents of the text buffer
	* contained within the specified range. The <CODE>fromIndex</CODE>
	* and <CODE>toIndex</CODE> parameters may be specified in any order.
	*
	* @param fromIndex the starting index from where text will be taken.
	* @param toIndex the ending index to where text will be taken.
	* @return a reference to a String.
	*/

	public String getText(int fromIndex, int toIndex);


	/**
	* Returns the index of the first occurence of the specified character starting
	* at the specified location and continuing to the end of the buffer.
	*
	* @param ch the character to match.
	* @param fromIndex the starting index from where the search will begin.
	* @return the index of the next matching character, or -1 if no match was found.
	*/

	public int indexOf(char ch, int fromIndex);


	/**
	* Inserts a string into the text buffer at the specified location and calls the
	* <CODE>textChanged</CODE> method of its listeners with the location
	* of the inserted text.
	*
	* @param str a reference to a String.
	* @param atIndex the index of the location where the text should be inserted.
	*/

	public void insertText(String str, int atIndex);


	/**
	* Returns the index of the previous occurence of the specified character
	* starting at the specified location and continuing to the end of the buffer.
	*
	* @param ch the character to match.
	* @param fromIndex the starting index from where the search will begin.
	* @return the index of the next matching character, or -1 if no match was found.
	*/

	public int prevIndexOf(char ch, int fromIndex);


	/**
	* Removes an ITextBufferListener from this object.
	*
	* @param l a reference to an object which implements ITextBufferListener.
	*/

	public void removeTextBufferListener(ITextBufferListener l);


	/**
	* Sets the value of this text buffer to the specified String.
	*
	* @param str a reference to a String.
	*/

	public void setText(String str);
}

