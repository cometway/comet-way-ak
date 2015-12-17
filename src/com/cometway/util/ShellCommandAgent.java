
package com.cometway.util;


import com.cometway.ak.Agent;
import com.cometway.util.ExecuteCommand;


/**
* This agent executes a shell command and outputs the result.
* The property command_line contains the shell command and related variables.
* An input stream is currently not supported.
* Upon completion result_code contains the result returned by the process when it exited,
* std_output contains any text outputed by the application,
* error_output contains any text outputed by application errors.
*/

public class ShellCommandAgent extends Agent
{
	public void initProps()
	{
		setDefault("command_line", "ls -l");
	}


	public void start()
	{
		String command_line = getString("command_line");
		String message = "Executing: " + command_line;

		try
		{
			debug(message);

			ExecuteCommand cmd = new ExecuteCommand(command_line);
			cmd.processOut = new StringBuffer();
			cmd.processErr = new StringBuffer();

			debug("Waiting for command execution to complete.");
			cmd.execute();

			if (cmd.processOut.length() > 0)
			{
				println(cmd.processOut.toString());
			}

			if (cmd.processErr.length() > 0)
			{
				println(cmd.processErr.toString());
			}

			if (cmd.returnValue == 0)
			{
				println("Completed successfully. (result code = 0)");
			}
			else
			{
				error("Error during execution.  (result code = " + cmd.returnValue + ')');
			}

			setProperty("std_output", cmd.processOut.toString());
			setProperty("error_output", cmd.processErr.toString());
			setInteger("result_code", cmd.returnValue);
		}
		catch (Exception e)
		{
			error(message, e);
		}
	}
}


