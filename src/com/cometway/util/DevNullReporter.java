
package com.cometway.util;


/** This implementation of ReporterInterface does absolutely positively nothing. */

public class DevNullReporter implements ReporterInterface
{
	/** Does nothing. */

	public void debug(Object objectRef, String message) {}

	/** Does nothing. */

	public void error(Object objectRef, String message) {}

	/** Does nothing. */

	public void error(Object objectRef, String message, Exception e) {}

	/** Does nothing. */

	public void println(Object objectRef, String message) {}

	/** Does nothing. */

	public void warning(Object objectRef, String message) {}

	/** Does nothing. */

	public void warning(Object objectRef, String message, Exception e) {}
}

