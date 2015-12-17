
package com.cometway.io;


import com.cometway.ak.ScheduledAgent;
import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
* This agent uses a Scheduler to periodically wakeup when it scans the directory
* specified by scan_dir and deletes all files older than file_lifespan_ms
* (specified in milliseconds). Optionally, the property filename_suffix can be
* specified to delete only files with a specific extension (such as .txt).
* Properties used: schedule, scan_dir (./), file_lifespan_ms (300000),
* filename_suffix (optional).
*/

public class FileReaper extends ScheduledAgent
{
	protected static SimpleDateFormat SDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	
	protected FilenameFilterImpl filenameFilter = new FilenameFilterImpl();


	public void initProps()
	{
		setDefault("schedule", "between 0:0:0-23:59:59 every 1m");
		setDefault("scan_dir", "./");
		setDefault("file_lifespan_ms", "300000");
//		setDefault("filename_suffix", ".txt");
	}


	public void wakeup()
	{
		String scan_dir = getString("scan_dir");
		File f = new File(scan_dir);

		scan_dir = f.toString();

		if (scan_dir.endsWith("/") == false)
		{
			scan_dir += "/";
		}

		debug("Scanning Directory: " + scan_dir);

		String  s[] = f.list(filenameFilter);

		for (int i = 0; i < s.length; i++)
		{
			File df = new File(scan_dir, s[i]);

			if (df.exists())
			{
				long lastModified = df.lastModified();
				String dateStr = SDF.format(new Date(lastModified));

				if (df.delete() == true)
				{
					println("Deleting file: " + df + " (modified " + dateStr + ")");
				}
				else
				{
					warning("Could not delete file: " + df);
				}
			}
		}
	}


	public class FilenameFilterImpl implements FilenameFilter
	{
		public boolean accept(File dir, String name)
		{
			boolean accept;
			File f = new File(dir, name);

			long currentTime = System.currentTimeMillis();
			long lastModified = f.lastModified();
			String dateStr = SDF.format(new Date(lastModified));

			debug("Scanning file: " + dir + "/" + name + " (modified " + dateStr + ")");
			debug("  current_time = " + currentTime);
			debug("  last_modified = " + lastModified);
			debug("  age of file = " + (currentTime - lastModified));

			accept = ((currentTime - lastModified) > getLong("file_lifespan_ms"));

			if (accept)
			{
				String filename_suffix = getString("filename_suffix");

				if (filename_suffix.length() > 0)
				{
					accept = name.endsWith(filename_suffix);
				}
			}

			debug("  accept = " + accept);

			return (accept);
		}
	}
}

