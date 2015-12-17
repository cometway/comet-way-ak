package com.cometway.xml;

import java.io.*;
import com.cometway.io.*;

/**
 * XMLParser. This class parses min xml into one of 4 kinds of tokens:
 * XML.EMPTY_ELEMENT_TAG, XML.START_TAG, XML.END_TAG, and XML.ELEMENT_CONTENT. The XMLParser is instantiated with a source
 * and characters will be read from this source via a Stream. Successive calls
 * to the nextToken() method will return the next parsed tokens from the
 * stream. Some options can be set, to ignore whitespace or to decode xml 1.0
 * escape codes in the XML.ELEMENT_CONTENT tokens. A EOFException will be thrown if the 
 * stream has reached the end.
 *
 * @see XMLToken
 */
public class XMLParser
{
	/** Setting this value to true will cause the parse to remove the whitespace it finds around
		 tags and text. */
	protected boolean ignoreWhitespace = true;
	/** Setting this field to true will cause the parser to decode XML 1.0 escape codes in the text */
	protected boolean decodeEscapeCodes = true;
	//	public boolean singleQuotePrecedence = false;
	//	public boolean doubleQuotePrecedence = false;
	//	public boolean firstQuotePrecedence = true;

	protected InputStream dataSource;

	private char cachedChar;
	private boolean useCachedChar;
	private int charCount = 0;
	private int length = -1;

	/**
	 * Parse XML from a String. 
	 */
	public XMLParser(String source)
	{
		//		dataSource = new StringBufferInputStream(source);
		dataSource = new ReaderInputStream(new StringReader(source));
	}

	/**
	 * Parse XML from an input stream
	 */
	public XMLParser(InputStream source)
	{
		dataSource = source;
	}


	/**
	* Closes the InputStream used by the parser.
	*/

	public void close() throws XMLParserException
	{
		if (dataSource != null)
		{
			try
			{
				dataSource.close();
			}
			catch (IOException e)
			{
				throw new XMLParserException("Could not close InputStream: " + dataSource, e);
			}
			finally
			{
				dataSource = null;
			}
		}
	}


	/**
	* The parser will ignore whitespace before  and after element content when
	* this option is set to true (default is true).
	*/

	public void setIgnoreWhitespace(boolean ignoreWhitespace)
	{
		this.ignoreWhitespace = ignoreWhitespace;
	}


	/**
	* The parser will decode escape codes within element content when
	* this option is set to true (default is true).
	*/

	public void setDecodeEscapeCodes(boolean decodeEscapeCodes)
	{
		this.decodeEscapeCodes = decodeEscapeCodes;
	}



	private char nextChar() throws IOException, EOFException
	{
		if(length>0) {
			if(charCount>length) {
				throw(new EOFException("Length has been reached."));
			}
		}
		charCount++;
		int rval = dataSource.read();
		if(rval == -1) {
			throw(new EOFException());
		}
		return((char)rval);
	}
	
	/**
	 * This sets how many characters is to be read from the input
	 * source.
	 */
	public void setLength(int length)
	{
		this.length = length;
	}
	
	/**
	 * This method returns the next parsed token from the source Stream.
	 * The tokens can be one of 4 types: a XML.EMPTY_ELEMENT_TAG, XML.START_TAG, XML.END_TAG,
	 * or XML.ELEMENT_CONTENT.
	 * This method will read as many characters as needed until its buffer
	 * satisfies one of the 4 tokens. If the end of the stream is reached,
	 * an EOFException will be thrown. If an error in the stream is caught, 
	 * an IOException will be thrown.
	 */
	public XMLToken nextToken() throws XMLParserException
	{
		int type = 0;
		StringBuffer tmpData = new StringBuffer();

		try
		{
			char tmpChar = '0';
			if(!useCachedChar) {
				tmpChar = nextChar();
			}
			else {
				tmpChar = cachedChar;
				useCachedChar = false;
			}
	
			if(tmpChar == '<') {
				tmpData.append(tmpChar);
				boolean dquote = false;
				boolean squote = false;
				tmpChar = nextChar();
	
				while(tmpChar==' ' || tmpChar=='\n' || tmpChar=='\t') {
					if(!ignoreWhitespace) {
						tmpData.append(tmpChar);
					}
					tmpChar = nextChar();
				}
	
				if(tmpChar == '/') {
					type = XML.END_TAG;
					tmpData.append(tmpChar);
					tmpChar = nextChar();
				}
				else {
					type = XML.START_TAG;
				}
	
				boolean empty = false;

				while(squote || dquote || tmpChar!='>') {
					//				if(firstQuotePrecedence) {
						if(tmpChar == '"') {
							if(!squote) {
								dquote = !dquote;
							}
							empty = false;
							tmpData.append(tmpChar);
						}
						else if(tmpChar == '\'') {
							if(!dquote) {
								squote = !squote;
							}
							empty = false;
							tmpData.append(tmpChar);
						}
						else if(tmpChar == '/') {
							if(!dquote && !squote) {
								empty = true;
							}
							tmpData.append(tmpChar);
						}
						else {
							if(tmpChar!=' ' && tmpChar!='\n' && tmpChar!='\t') {
								empty = false;
							}
							tmpData.append(tmpChar);
						}
						//				}
					tmpChar = nextChar();
				}
				tmpData.append(tmpChar);
				if(empty) {
					type = XML.EMPTY_ELEMENT_TAG;
				}
			}
			else {
				type = XML.ELEMENT_CONTENT;
				tmpData.append(tmpChar);
				tmpChar = nextChar();
				while(tmpChar != '<') {
					tmpData.append(tmpChar);
					tmpChar = nextChar();
				}
				cachedChar = tmpChar;
				useCachedChar = true;
	
				if(decodeEscapeCodes) {
					tmpData = new StringBuffer(XML.decode(tmpData.toString()));
				}
			}			
	
			if(ignoreWhitespace) {
				if(tmpData.toString().trim().length()==0) {
					return(nextToken());
				}
				else {
					tmpData = new StringBuffer(tmpData.toString().trim());
				}
			}
		}
		catch (Exception e)
		{
			throw new XMLParserException(e.getMessage(), e);
		}
		
		return(new XMLToken(type,tmpData.toString()));
	}


	/**
	* Returns the next token if it matches the specified data.
	* @throws XMLParserException if it does not match the specified data or there is a problem.
	*/

	public XMLToken nextToken(String data) throws XMLParserException
	{
		XMLToken x = nextToken();

		if (x.data.equals(data) == false)
		{
			throw new XMLParserException("Unexpected token: " + x.data);
		}

		return (x);
	}


	/**
	* Returns the next token if it's type is XML.START_TAG.
	* @throws XMLParserException if it's type is not XML.START_TAG or there is a problem.
	*/

	public XMLToken nextStartTag() throws XMLParserException
	{
		XMLToken x = nextToken();

		if (x.type != XML.START_TAG)
		{
			throw new XMLParserException("Unexpected token: " + x.data);
		}

		return (x);
	}


	/**
	* Returns the next token if it's type is XML.END_TAG.
	* @throws XMLParserException if it's type is not XML.END_TAG or there is a problem.
	*/

	public XMLToken nextEndTag() throws XMLParserException
	{
		XMLToken x = nextToken();

		if (x.type != XML.END_TAG)
		{
			throw new XMLParserException("Unexpected token: " + x.data);
		}

		return (x);
	}


	/**
	* Returns the next token if it's type is XML.ELEMENT_CONTENT.
	* @throws XMLParserException if it's type is not XML.ELEMENT_CONTENT or there is a problem.
	*/

	public XMLToken nextElementContent() throws XMLParserException
	{
		XMLToken x = nextToken();

		if (x.type != XML.ELEMENT_CONTENT)
		{
			throw new XMLParserException("Unexpected token: " + x.data);
		}

		return (x);
	}


	/**
	* Returns the next token if it's type is XML.EMPTY_ELEMENT_TAG.
	* @throws XMLParserException if it's type is not XML.EMPTY_ELEMENT_TAG or there is a problem.
	*/

	public XMLToken nextEmptyElementTag() throws XMLParserException
	{
		XMLToken x = nextToken();

		if (x.type != XML.EMPTY_ELEMENT_TAG)
		{
			throw new XMLParserException("Unexpected token: " + x.data);
		}

		return (x);
	}
}
