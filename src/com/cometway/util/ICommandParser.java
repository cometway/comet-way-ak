
package com.cometway.util;


/**
* classes that might provide a command string parse method implement ICommandParser
*/

public interface ICommandParser
{


	/**
	* parse: method parses a command represented as a String of tokens, returning a
	*   String response.  the response might be used to indicate success or failure of
	*   different sorts, or state as a result of command
	*/

	public String parseCommand(String command);


	/**
	* addCommandParser adds an ICommandParser to extend the commands that it can parse
	*   by adding ICommandParsers for new commands.  the String represents the new
	*   command to be parsed by the given ICommandParser
	*/

	public void addCommandParser(String command, ICommandParser parser);
}

