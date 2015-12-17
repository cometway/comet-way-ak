
package com.cometway.om;

import com.cometway.ak.ScheduledAgent;
import com.cometway.props.Props;
import com.cometway.util.Base64Encoding;
import com.cometway.util.ObjectSerializer;
import com.cometway.xml.XML;
import com.cometway.xml.XMLFileParser;
import com.cometway.xml.XMLParserException;
import com.cometway.xml.XMLRequestAgent;
import com.cometway.xml.XMLToken;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.List;


public class ObjectManagerArchiver extends ScheduledAgent
{
	protected final String EOL = System.getProperty("line.separator");
	protected final Object[] synchObject = new Object[0];
	protected IObjectManager om;


	public void initProps()
	{
		setDefault("alphabetized_props", "true");
//		setDefault("load_file", "archive.props");
//		setDefault("save_file", "archive.props");
		setDefault("om_service_name", "object_manager");
		setDefault("schedule", "between 0:0:0-23:59:59 every 5m");
	}


	public void start()
	{
		om = (IObjectManager) getServiceImpl(getString("om_service_name"));

		loadArchive();
		schedule();
	}


	public void stop()
	{
		unschedule();
		saveArchive();

		om = null;
	}


	public void wakeup()
	{
		saveArchive();
	}


	public void loadArchive()
	{
		synchronized (synchObject)
		{
			String filename = getString("load_file");
			
			if (filename.length() == 0)
			{
				return;
			}

			debug("Loading from " + filename);

			XMLFileParser parser = null;

			try
			{
				parser = new XMLFileParser(new File(filename));
				parser.nextToken(XML.XML_10_HEADER);
				parser.nextToken("<props_list>");

				while (true)
				{
					XMLToken t = parser.nextToken();

					if (t.data.equals("<props>"))
					{
						Props p = new Props();

						while (true)
						{
							t = parser.nextToken();

							if (t.type == XML.START_TAG)
							{
								String key = t.data.substring(1, t.data.length() - 1);
								t = parser.nextToken();

								if (t.data.equals("<base64>"))
								{
									t = parser.nextElementContent();

									byte[] data = Base64Encoding.decode(t.data);
									Object o = ObjectSerializer.unserialize(data);
									p.setProperty(key, o);

									parser.nextToken("</base64>");
									parser.nextToken("</" + key + '>');
								}
								else if (t.type == XML.ELEMENT_CONTENT)
								{
									p.setProperty(key, t.data);
									parser.nextToken("</" + key + '>');
								}
								else if (t.data.equals("</" + key + '>') == false)
								{
									throw new XMLParserException("Invaid token: " + t.data);
								}
							}
							else if (t.data.equals("</props>"))
							{
								break;
							}
							else
							{
								throw new XMLParserException("Invaid token: " + t.data);
							}
						}

						p.removeProperty("id");

						ObjectType type = new PropsType(p.getString("type").substring(6));
						ObjectID id = om.createObject(type);
						Props objectProps = (Props) om.getObject(id);
						objectProps.copyFrom(p);

						debug("Loaded " + id.toString());
//						objectProps.dump();
//						debug("----------------");
					}
					else if (t.data.equals("</props_list>"))
					{
						break;
					}
					else
					{
						throw new XMLParserException("Invaid token: " + t.data);
					}
				}
			}
			catch (Exception e)
			{
				error("Could not parse: " + filename, e);
			}

			try {
				parser.close();
			}
			catch(Exception e) {;}
		}
	}


	public void saveArchive()
	{
		synchronized (synchObject)
		{
			boolean alphabetized_props = getBoolean("alphabetized_props");
			String filename = getString("save_file");

			if (filename.length() == 0)
			{
				return;
			}

			String tempfile = filename + ".temp";
			debug("Saving to " + tempfile);
	
			File file = new File(filename);
			File temp = new File(tempfile);
			BufferedWriter out = null;
	
			try
			{
				out = new BufferedWriter(new FileWriter(temp));
				out.write(XML.XML_10_HEADER);
				out.write(EOL);
				out.write("<props_list>");
				out.write(EOL);
	
				List types = om.listObjects(om.LIST_TYPES);
				int typeCount = types.size();

				debug("Writing " + typeCount + " types");

				for (int t = 0; t < typeCount; t++)
				{
					ObjectType type = (ObjectType) types.get(t);
					List v = om.listObjects(type);
					int count = v.size();

					debug("Writing " + count + ' ' + type + " records");

					for (int i = 0; i < count; i++)
					{
						ObjectID id = (ObjectID) v.get(i);
						Props p = (Props) om.getObject(id);

						if (p != null)
						{
							out.write("\t<props>");
							out.write(EOL);
	
							List keys = p.getKeys();
							int keyCount = keys.size();

							if (alphabetized_props)
							{
								Collections.sort(keys);
							}
	
							for (int x = 0; x < keyCount; x++)
							{
								String key = (String) keys.get(x);
								Object value = p.getProperty(key);

								out.write("\t\t<");
								out.write(key);
								out.write('>');

								if ((value instanceof String) || (value instanceof Integer))
								{
									String s = p.getString(key);
									out.write(XML.encode(s));
								}
								else
								{
									byte[] data = ObjectSerializer.serialize(value);
									String s = Base64Encoding.encode(data);

									out.write("<base64>");
									out.write(s);
									out.write("</base64>");
								}

								out.write("</");
								out.write(key);
								out.write('>');
								out.write(EOL);
							}
	
							out.write("\t</props>");
							out.write(EOL);
						}
					}
				}

				out.write("</props_list>");
				out.write(EOL);
				out.flush();

				if (file.exists())
				{
					debug("deleting: " + file);
					file.delete();
				}

				temp.renameTo(file);

				debug("Saved to " + file + " at " + getDateTimeStr());
			}
			catch (Exception e)
			{
				error("could not save", e);
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
						error("could not save: " + filename, e2);
					}
				}
			}
		}
	}
}


