
package com.cometway.io;


import com.cometway.ak.Agent;
import java.io.File;
import java.util.Vector;


/**
* This agent deletes the files specified by delete_files.
* The delete_files property is a comma separated list of filenames.
*/

public class MoveFileAgent extends Agent
{
	public void initProps()
	{
		setDefault("from_files", "move_me.txt, move_me_2.txt");
		setDefault("to_files", "test/move_me.txt, test/move_me_2.txt");
	}


	/**
	* Moves the files specified by delete_files.
	* The delete_files property is a comma separated list of filenames.
	*/

	public void start()
	{
		String move_to_directory = getTrimmedString("move_to_directory");
		Vector from_files = getTokens("from_files");
		Vector to_files = getTokens("to_files");
		int count = from_files.size();
		int to_filesCount = to_files.size();

		if (count != to_filesCount)
		{
			error("The number of from_files doesn't match the number of to_files.");
			debug("from_files.size() = " + count);
			debug("  to_files.size() = " + to_filesCount);
		}
		else
		{
			for (int i = 0; i < count; i++)
			{
				moveFile((String) from_files.get(i), (String) to_files.get(i));
			}
		}
	}


	/**
	* Moves the specified file if possible.
	*/

	protected void moveFile(String fromFilename, String toFilename)
	{
		File fromFile = new File(fromFilename);
		File toFile = new File(toFilename);
		boolean result = fromFile.renameTo(toFile);

		if (result)
		{
			println("Moved: " + fromFile);
		}
		else
		{
			error("Could not move: " + fromFilename + " to " + toFilename);
		}
	}
}


