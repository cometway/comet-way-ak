
package com.cometway.tools;

import com.cometway.ak.*;
import com.cometway.props.Props;
import com.cometway.util.*;
import java.io.*;
import java.util.*;


/**
* This is an alternate StartupAgent which can be used with the AK -startup_agent
* command line option. Upon running, it scans the startup directory, and creates a
* source file named StaticStartupAgent.java which is hard coded to startup the
* agents as specified in the startup directory.
*/

public class StartupAgentBuilder extends StartupAgent
{
	public void initProps()
	{
		setDefault("output_classname", "StaticStartupAgent");
		setDefault("startup_dir", "./");
	}


        public void start()
        {
                String  startupDir = getString("startup_dir");
                File    f = new File(startupDir);

                println("Loading agents from " + f);

                startupDir = f.toString();

                if (startupDir.endsWith("/") == false)
                {
                        startupDir += "/";
                }

                /* Get a list of .startup files from the startup directory */

                String  s[] = f.list(this);


                /* Sort the filenames alphabetically using a swap sort algorithm */

                for (int start = 0; start < s.length - 1; start++)
                {
                        for (int x = start + 1; x < s.length; x++)
                        { 
                                if (s[start].compareTo(s[x]) > 0)
                                { 
                                        String temp = s[start];
                                        s[start] = s[x];
                                        s[x] = temp;
                                }
                        }
                }
        
		/* Write out the Java source for an agent that loads this configuration. */

		try
		{
			String output_classname = getString("output_classname");
			String output_file = output_classname + ".java";
	
			println("Writing static startup class to " + output_file);
	
			File outFile = new File(output_file);
			FileWriter w = new FileWriter(outFile);
	
			w.write("\n");
			w.write("import com.cometway.ak.AK;\n");
			w.write("import com.cometway.ak.Agent;\n");
			w.write("import com.cometway.ak.AgentControllerInterface;\n");
			w.write("import com.cometway.props.Props;\n");
			w.write("\n");
			w.write("public class " + output_classname + " extends Agent\n");
			w.write("{\n");
			w.write("	public void start()\n");
			w.write("	{\n");
			w.write("		Props p;\n");
			w.write("		AgentControllerInterface a;\n");
	
			for (int i = 0; i < s.length; i++)
			{
				String  str = startupDir + s[i];
	
				println("Loading " + str);
	
				Props   agentProps = Props.loadProps(str);
	
				if (agentProps == null)
				{
					error("Could not load agent: " + str);
				}
				else
				{
					w.write("\n");
					w.write("		// " + str + "\n\n");
					w.write("		p = new Props();\n");

					Vector v = agentProps.getKeys();

					for (int x = 0; x < v.size(); x++)
					{
						String key = (String) v.elementAt(x);

						if (key.startsWith("#") == false)
						{
							String value = agentProps.getString(key);
	
							w.write("		p.setProperty(\"");
							w.write(key);
							w.write("\", \"");
							w.write(value);
							w.write("\");\n");
						}
					}

					w.write("		a = AK.getAgentKernel.createAgent(p);\n");
					w.write("		if (a != null) a.start();\n");
				}
			}

			w.write("	}\n");
			w.write("}\n\n");
			w.flush();
			w.close();
		}
		catch (Exception e)
		{
			error("Could not write the startup class", e);
		}

		println("Completed");
	}
}







