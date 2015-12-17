package com.cometway.xml;

import java.io.*;

/**
 * This class extends the XMLParser and allows files to be parsed.
 *
 */
public class XMLFileParser extends XMLParser
{
	/**
	 * Creates an XMLParser that reads from a file
	 */
	public XMLFileParser(File f) throws IOException
	{
		super(new FileInputStream(f));
	}


	public static void main(String[] args) 
	{
		try {
			XMLFileParser parser = new XMLFileParser(new File(args[0]));

			if(args.length>1) {
				File fout = new File(args[1]);
				FileWriter out = new FileWriter(fout);
				
				while(true) {
					XMLToken x = parser.nextToken();
					out.write(x.data);
					out.flush();
				}
			}
			else {
				while(true) {
					XMLToken x = parser.nextToken();
					System.out.print("TYPE = "+x.type);
					System.out.println(" DATA = "+x.data);
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

}
