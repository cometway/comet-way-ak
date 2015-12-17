
package com.cometway.text;


/**
 * A pointer to a position in an ITextBuffer.
 *
 */

public final class TextPointer implements ITextBufferListener
{
	private ITextBuffer		b;
	private int			pos;
	private ITextPointerListener    l;


	/**
	 * Creates an empty TextPointer.
	 */

	public TextPointer() {}


	/**
	 * Creates a TextPointer that points to the beginning of the given ITextBuffer.
	 */


	public TextPointer(ITextBuffer b)
	{
		setTextBuffer(b);
	}


	/**
	 * Creates a TextPointer that points to the given position in the given ITextBuffer.
	 */


	public TextPointer(ITextBuffer b, int pos)
	{
		this.pos = pos;

		setTextBuffer(b);
	}


	/**
	 * Deletes all the text from the position pointed to by this instance of TextPointer
	 * to the position given as the parameter in the ITextBuffer which this TextPointer is
	 * pointing to.
	 */


	public void deleteTo(int pos)
	{
		b.delete(this.pos, pos);
	}


	/**
	 * Deletes all the text from the position pointed to by this instance of TextPointer
	 * to the position pointed to by the TextPointer given as the parameter in the
	 * ITextBuffer which this instance of TextPointer is pointing to.
	 * ?
	 * public void deleteTo(TextPointer p)
	 * {
	 * b.delete(this.pos, p.pos);
	 * }
	 * protected void finalize() throws Throwable
	 * {
	 * dispose();
	 * }
	 * 
	 * Disposes this TextPointer.
	 * This must be called in order for this object to be garbage collected.
	 */


	public void dispose()
	{
		if (b != null)
		{
			b.removeTextBufferListener(this);

			b = null;
		}

		pos = 0;
		l = null;
	}


	/**
	 * @return Returns the current position.
	 */


	public int getPosition()
	{
		return (pos);
	}


	/**
	 * @return Returns the current ITextBuffer which this TextPointer is pointing to.
	 */


	public ITextBuffer getTextBuffer()
	{
		return (b);
	}


	/**
	 * Returns the text from the position pointed to by this TextPointer to the position
	 * given by the parameter in the ITextBuffer which this TextPointer is pointing to.
	 */


	public String getTextTo(int pos)
	{
		return (b.getText(this.pos, pos));
	}


	/**
	 * Returns the text from the position pointed to by this TextPointer to the position
	 * pointed to by the TextPointer passed as the parameter in the ITextBuffer which this
	 * instance of TextPointer is pointing to.
	 */


	public String getTextTo(TextPointer p)
	{
		return (b.getText(this.pos, p.pos));
	}


	/**
	 * Moves the position of this TextPointer to the next instance of the
	 * character given as the parameter, starting from the current position.
	 * @return Returns true IFF the character was found.
	 */


	public boolean findNext(char ch)
	{
		int     index = b.indexOf(ch, pos);

		if (index >= 0)
		{
			pos = index;
		}

		return (index >= 0);
	}


	/**
	 * Moves the position of this TextPointer to the next instance of the
	* String given as the parameter, starting from the current position.
	* @return Returns true IFF the character was found.
	 */


	public boolean findNext(String s)
	{
		if (s.length() > 0)
		{
			char    c = s.charAt(0);
			int     index = b.indexOf(c, pos);

			while (index >= 0)
			{
				String  match = b.getText(index, index + s.length());

				if (match.equals(s))
				{
					pos = index;

					return (true);
				}

				index = b.indexOf(c, index + 1);
			}
		}

		return (false);
	}


	/**
	 * Moves the position of this TextPointer to the previous instance of the
	 * character given as the parameter, starting from the current position.
	 * @return Returns true IFF the character was found.
	 */


	public boolean findPrev(char ch)
	{
		int     index = b.prevIndexOf(ch, pos);

		if (index >= 0)
		{
			pos = index;
		}

		return (index >= 0);
	}


	/**
	 * Inserts the String passed in as the parameter in the position of this
	 * TextPointer into the ITextBuffer which this TextPointer points to.
	 */


	public void insertText(String s)
	{
		b.insertText(s, pos);
	}


	/**
	 * Offsets the current position of this TextPointer by the given parameter.
	 */


	public void offset(int relativePos)
	{
		setPosition(pos + relativePos);
	}


	/**
	 * Sets the ITextPointerListener to be notified of changes in this TextPointer.
	 */


	public void setListener(ITextPointerListener l)
	{
		this.l = l;
	}


	/**
	 * Sets the position of this TextPointer.
	 */


	public void setPosition(int pos)
	{
		this.pos = pos;

		if (this.pos < 0)
		{
			this.pos = 0;
		}

		if (l != null)
		{
			l.textPointerChanged(this);
		}
	}


	/**
	 * Sets the position of this TextPointer to the position of the TextPointer
	 * passed in as the parameter.
	 */


	public void setPosition(TextPointer p)
	{
		pos = p.pos;

		if (pos < 0)
		{
			pos = 0;
		}

		if (l != null)
		{
			l.textPointerChanged(this);
		}
	}


	/**
	 * Sets the TextBuffer which this TextPointer is to be pointing to, and adds itself
	 * as an ITextBufferListener with the new ITextBuffer.
	 */


	public void setTextBuffer(ITextBuffer b)
	{
		dispose();

		this.b = b;

		b.addTextBufferListener(this);
	}


	/**
	 * implementation of ITextBufferListener.textChanged()
	 */


	public void textChanged(ITextBuffer b, int start, int end)
	{		// We don't care about this.
	}


	/**
	 * implementation of ITextBufferListener.textMoved()
	 */


	public void textMoved(ITextBuffer b, int start, int offset)
	{
		if (start < pos)
		{
			setPosition(pos + offset);
		}
	}


}

