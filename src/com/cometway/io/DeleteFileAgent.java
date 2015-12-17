
package com.cometway.io;


import com.cometway.ak.Agent;
import java.io.File;
import java.util.Vector;


/**
* This agent deletes the files specified by delete_files.
* The delete_files property is a comma separated list of filenames.
*/

public class DeleteFileAgent extends Agent
{
	public void initProps()
	{
		setDefault("delete_files", "delete_me.txt, delete_me_2.txt");
	}


	/**
	* Deletes the files specified by delete_files.
	* The delete_files property is a comma separated list of filenames.
	*/

	public void start()
	{
		Vector filenames = getTokens("delete_files");
		int count = filenames.size();

		for (int i = 0; i < count; i++)
		{
			deleteFile((String) filenames.get(i));
		}
	}


	/**
	* Deletes the specified file if possible.
	*/

	protected void deleteFile(String filename)
	{
		File file = new File(filename);
		boolean result = file.delete();

		if (result)
		{
			println("Deleted: " + filename);
		}
		else
		{
			error("Could not delete: " + filename);
		}
	}
}


