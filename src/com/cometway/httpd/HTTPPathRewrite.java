package com.cometway.httpd;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Vector;

import com.cometway.ak.*;
import com.cometway.util.jGrep;
import com.cometway.util.Pair;

import org.apache.oro.text.perl.Perl5Util;

/**
 * This WebServerExtension reads in a file of match and replace pairs, separated
 * by a space. These can be regular expressions similar to the s/match/replace/ used
 * by sed or perl. These pairs are applied to the 'path' property of the HTTPAgentRequest
 * but not the 'request' property. This WebServerExtension will always pass through, 
 * regardless of whether the path has been altered or not. The pairs are applied in the
 * order they were read from the file, and stops when one of the match/replace pairs
 * changes the path. Example match file:
 *
 * ^/images/(.*) /specials/all_images/$1
 * .*confidential.* /access_denied.html
 *
 * The first line will change '/images/wallpapers/1.jpg' to '/specials/all_images/wallpapers/1.jpg'
 * The second will change '/1/2/confidential_20071031.pdf' to '/access_denied.html'
 */
public class HTTPPathRewrite extends WebServerExtension
{
	Vector pairs;
	Perl5Util cache;

	public void initProps()
	{
		setDefault("service_name","extension://.*");
		setDefault("match_file","http.rewrite");

		setDefault("webserver_service_name","none");
		setDefault("domains","none");
	}

	public void start()
	{
		readMatchFile();
		cache = new Perl5Util();

		super.start();
	}

	public void stop()
	{
		cache = null;
		pairs = null;
	}

	public void readMatchFile()
	{
		File f = new File(getString("match_file"));
		pairs = new Vector();

		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(f));
			String line = in.readLine();
			while(line!=null) {
				if(!line.startsWith("#")) {
					if(line.trim().length()>0) {
						int x = line.indexOf(" ");
						if(x==-1) {
							x = line.indexOf("\t");
						}
						if(x!=-1) {
							pairs.addElement(new Pair(line.substring(0,x).trim(),line.substring(x).trim()));
						}
					}
				}
				line = in.readLine();
			}
		}
		catch(Exception e) {
			error("Error reading match file: "+getString("match_file"),e);
		}
	}

	public boolean handleRequest(HTTPAgentRequest request)
	{
		String path = request.getString("path");
		String newPath = path;
		for(int x=0;x<pairs.size();x++) {
			String match = (String)((Pair)pairs.elementAt(x)).first();
			String replace = (String)((Pair)pairs.elementAt(x)).second();

			newPath = jGrep.grepAndReplaceText(match,replace,path,false,cache);
			if(!path.equals(newPath)) {
				if(newPath.indexOf("?")!=-1) {
					setCGIParams(newPath.substring(newPath.indexOf("?")+1),request);
					newPath = newPath.substring(0,newPath.indexOf("?"));
				}
				request.setProperty("path",newPath);
				break;
			}
		}
		return(false);
	}


	protected void setCGIParams(String params, HTTPAgentRequest request)
	{
		int i = params.indexOf("&");
		while(i!=-1) {
			String tmp = params.substring(0,i);
			params = params.substring(i+1);
			
			i = tmp.indexOf("=");
			if(i!=-1) {
				String property = HTMLStringTools.decode(tmp.substring(0,i));
				if(request.hasProperty(property)) {
					Object o = request.getProperty(property);
					if(o instanceof Vector) {
						((Vector)o).addElement(HTMLStringTools.decode(tmp.substring(i+1)));
					}
					else {
						Vector v = new Vector();
						v.addElement(o);
						v.addElement(HTMLStringTools.decode(tmp.substring(i+1)));
						request.setProperty(property,v);
					}
				}
				else {
					request.setProperty(property,HTMLStringTools.decode(tmp.substring(i+1)));
				}
			}
			else {
				request.setProperty(HTMLStringTools.decode(tmp)," ");
			}
			
			i = params.indexOf("&");
		}
		if(params.length()>0) {
			i = params.indexOf("=");
			
			if(i!=-1) {
				String property = HTMLStringTools.decode(params.substring(0,i));
				if(request.hasProperty(property)) {
					Object o = request.getProperty(property);
					if(o instanceof Vector) {
						((Vector)o).addElement(HTMLStringTools.decode(params.substring(i+1)));
					}
					else {
						Vector v = new Vector();
						v.addElement(o);
						v.addElement(HTMLStringTools.decode(params.substring(i+1)));
						request.setProperty(property,v);
					}
				}
				else {
					request.setProperty(HTMLStringTools.decode(params.substring(0,i)),HTMLStringTools.decode(params.substring(i+1)));
				}
			}
			else {
				request.setProperty(HTMLStringTools.decode(params)," ");
			}
		}
	}
}
