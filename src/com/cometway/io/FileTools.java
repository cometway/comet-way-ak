
package com.cometway.io;


import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
* A utility class for common file related operations.
*/

public abstract class FileTools
{
	/**
	* Returns a String describing the modification of the specified File
	* using the specified SimpleDateFormat as a template.
	*/

	static public String getFileModifiedString(File f, SimpleDateFormat sdf)
	{
		String fileDate = sdf.format(new Date(f.lastModified()));

		return (fileDate);
	}


	/**
	* Returns a string describing the size of the specified File.
	* If the size is less than 1024 bytes, the size is given in bytes.
	* If the size is less then 1024 K bytes, the size is given in K.
	* If the size is 1024 K bytes or more, the size is given in M.
	*/

	static public String getFileSizeString(File f)
	{
		StringBuffer b = new StringBuffer();
		long fileSize = f.length();

		if (fileSize < 1024)
		{
			b.append(fileSize);
			b.append(" bytes");
		}
		else
		{
			fileSize /= 1024;

			if (fileSize < 1024)
			{
				b.append(fileSize);
				b.append(" K");
			}
			else
			{
				fileSize /= 1024;

				b.append(fileSize);
				b.append(" M");
			}
		}

		return (b.toString());
	}


	/**
	* Writes the bytes contained in filedata to the specified file.
	* The file will be written to a temp file (filename + ".temp").
	* If necessary the file specified by filename will be deleted,
	* before renaming the temp file as filename.
	*/

	static public void writeFile(String filename, byte[] filedata) throws FileToolsException
	{
		if (filename.length() == 0)
		{
			throw new FileToolsException("Zero length filename");
		}

		writeFile(new File(filename), new File(filename + ".temp"), filedata);
	}


	/**
	* Writes the bytes contained in filedata to the specified File.
	* The file will be written to the specified File.
	* If necessary the File specified by file will be deleted,
	* before renaming the tempFile as file.
	*/

	static public void writeFile(File file, File tempFile, byte[] filedata) throws FileToolsException
	{
		FileOutputStream out = null;

		try
		{
			out = new FileOutputStream(tempFile);
			out.write(filedata);
			out.flush();
			out.close();
			out = null;

			if (file.exists())
			{
				file.delete();
			}

			tempFile.renameTo(file);
		}
		catch (Exception e)
		{
			throw new FileToolsException("Could not write file: " + tempFile, e);
		}
		finally
		{
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (Exception e2)
				{
					throw new FileToolsException("Could not close FileOutputStream: " + tempFile, e2);
				}
			}
		}
	}
}



