
package com.cometway.net;


import com.cometway.ak.Agent;
import com.cometway.net.HTTPLoader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;


public class HTTPDownloaderAgent extends Agent
{
	public void initProps()
	{
		setDefault("download_url", "http://localhost/index.html");
		setDefault("http_timeout", "120000");
		setDefault("output_file", "index.html");
		setDefault("password", "none");
		setDefault("replace_existing_file", "true");
		setDefault("user_agent", "Mozilla/4.0 (compatible; MSIE 5.12; Mac_PowerPC)");
		setDefault("username", "none");
		setDefault("verbose", "false");
	}


	public void start()
	{
		String download_url = getTrimmedString("download_url");
		String output_file = getTrimmedString("output_file");
		String username = getTrimmedString("username");
		String password = getTrimmedString("password");
		int http_timeout = getInteger("http_timeout");
		boolean replace_existing_file = getBoolean("replace_existing_file");
		String user_agent = getTrimmedString("user_agent");
		boolean verbose = getBoolean("verbose");
		boolean debug = (getBoolean("hide_debug") == false);

		println("Downloading image at " + download_url);

		HTTPLoader loader = new HTTPLoader();
		loader.setVerbose(verbose);
		loader.setRequestTimeout(http_timeout);
		loader.setUserAgent(user_agent);
		loader.setCookiesSupported(true);
		loader.setDebug(debug);

		if ((username.length() > 0) && (password.length() > 0))
		{
			if ((username.equals("none") == false) && (password.equals("none") == false))
			{
				loader.useBasicAuthentication(username, password);
			}
		}

		File f = new File(output_file);
		debug("-> Saving to: " + f);

		if (f.exists())
		{
			if (replace_existing_file)
			{
				f.delete();
			}
			else
			{
				throw new RuntimeException("Cannot delete existing output_file: " + f);
			}
		}

		OutputStream out = null;

		try
		{
			out = new FileOutputStream(f);
			loader.getURL(download_url, out);
		}
		catch (Exception e)
		{
			error("Could not download file: " + download_url, e);
		}
		finally
		{
			if (out != null)
			{
				try
				{
					out.flush();
					out.close();
				}
				catch (Exception e2)
				{
					error("Could not save file: " + output_file, e2);
				}
			}
		}
	}
}



