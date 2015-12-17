
package com.cometway.util;


import com.cometway.ak.Agent;


/**
* This agent calls System.exit() with the specified exit code
* (0 by default). The VM will immediately cease to function
* and your process will be ended forever.
*/

public class SystemExitAgent extends Agent
{
	/**
	* exit_code = 0 (normal completion)
	*/

	public void initProps()
	{
		setDefault("exit_code", "0");
	}


	/**
	* Calls System.exit() with the integer value of exit_code.
	* By default, exit_code = 0 (normal completion).
	*/

	public void start()
	{
		int exit_code = getInteger("exit_code");

		println("Exiting Java Virtual Machine (exit_code = " + exit_code + ") ...");

		System.exit(exit_code);
	}
}

