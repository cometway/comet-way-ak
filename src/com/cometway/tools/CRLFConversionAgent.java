
package com.cometway.tools;


import com.cometway.ak.*;
import com.cometway.util.*;
import java.io.*;


/**
* This agent converts the CRLF format of files recursively contained
* in the <TT>scan_dir</TT>, specified by the <TT>output_format</TT>
* (<I>system, unix, macintosh, or dos</I>). If <TT>recursive</TT> is set to
* <I>false</I> only files in the <TT>scan_dir</TT> are changed.
*/

public class CRLFConversionAgent extends Agent
{
	private static String newline = System.getProperty("line.separator");

	private int count;


	/**
	* Initializes the Props for this agent.
	*/

	public void initProps()
	{
		setDefault("scan_dir", "./");
		setDefault("output_format", "system");
		setDefault("recursive", "true");
	}


	/**
	* Starts this agent.
	*/

	public void start()
	{
                String output_format = getString("output_format");

                if (output_format.equals("unix"))
                {
                        newline = "\n";
                }
                else if (output_format.equals("macintosh"))
                {
                        newline = "\r";
                }
                else if (output_format.equals("dos"))
                {
                        newline = "\r\n";
                }

		count = 0;

		scanDirectory(new File(getString("scan_dir")));
	}


	/**
	* Scans and converts the specified directory.
	*/

	private void scanDirectory(File scanDir)
	{
		String[] files = scanDir.list();

		for (int i = 0; i < files.length; i++)
		{
			File inFile = new File(scanDir, files[i]);
			File outFile = new File(scanDir, files[i] + ".temp");

			if (inFile.isFile())
			{
				convertFile(inFile, outFile);
			}
			else if (getBoolean("recursive") && inFile.isDirectory())
			{
				scanDirectory(inFile);
			}
		}
	}


	/**
	* Converts the specified file.
	*/

	private void convertFile(File inFile, File outFile)
	{
		count++;

		println("Converting: " + inFile + " (" + count + ")");// + " >>>" + outFile);

		Reader reader = null;
		Writer writer = null;

		try
		{
			reader = new BufferedReader(new FileReader(inFile));
			writer = new FileWriter(outFile);
			int p = -1;

			while (true)
			{
				int c = reader.read();

				if (c == -1)
				{
					break;
				}
				else if (c == '\r')
				{
					writer.write(newline);
				}
				else if (c == '\n')
				{
					if (p != '\r')
					{
						writer.write(newline);
					}
				}
				else
				{
					writer.write((char) c);
//					System.out.print((char) c);
				}

				p = c;
			}

			reader.close();
			reader = null;

			writer.close();
			writer = null;

			if (inFile.exists())
			{
				inFile.delete();
			}

			outFile.renameTo(inFile);
		}
		catch (IOException e)
		{
			error("Conversion error", e);
		}
		finally
		{
			try
			{
				if (reader != null) reader.close();
				if (writer != null) writer.close();
			}
			catch (Exception x) {}

			if (outFile.exists())
			{
				outFile.delete();
			}
		}
	}
}


