
package com.cometway.om;

import com.cometway.ak.ServiceAgent;
import com.cometway.ak.ServiceManager;
import com.cometway.props.Props;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


/**
* Reference implementation of a non-persistent IObjectManager as an agent.
* Normal operations are reported through <TT>debug</TT> for educational purposes.
* You should want to add the <TT>hide_debug=true</TT> property during normal use.
*/

public class ObjectManager extends ServiceAgent implements IObjectManager
{
	protected Hashtable types;
	protected int uniqueID;


	/**
	 * Sets the service_name property to object_manager if a value
	 * was not provided.
	 */

	public void initProps()
	{
		setDefault("service_name", "object_manager");
	}


	/**
	 * Starts and registers the object cache.
	 */

	public void start()
	{
		types = new Hashtable();

		register();
	}


	/**
	 * Stops and unregisters the object cache.
	 */

	public void stop()
	{
		unregister();

		types = null;
	}


	/* Methods for IObjectManager */


	/**
	 * This method changes an object's ObjectID.
	 */

	public boolean changeObjectID(ObjectID oldID, ObjectID newID)
	{
		boolean rval = false;

		try
		{
			Hashtable t = (Hashtable) types.get(oldID.getType());

			if (t == null)
			{
				warning("changeObjectID: Unknown type \"" + oldID.getType() + "\".");
			}
			else
			{
				Object  o = t.get(oldID.getID());

				if (!oldID.getType().equals(newID.getType()))
				{
					Hashtable       newtype = (Hashtable) types.get(newID.getType());

					if (newtype == null)
					{
						newtype = new Hashtable();

						types.put(newID.getType(), newtype);
					}

					newtype.put(newID.getID(), o);
					t.remove(oldID.getID());

					if (t.size() == 0)
					{
						types.remove(oldID.getType());
					}
				}
				else
				{
					t.remove(oldID.getID());
					t.put(newID.getID(), o);
				}

				if (o instanceof Props)
				{
					Props   p = (Props) o;

					p.setProperty("type", newID.getType());
					p.setProperty("id", newID.getID());
				}
			}

			rval = true;
		}
		catch (Exception e)
		{
			error("changeObjectID: " + e);
		}

		return (rval);
	}


	/**
	 * Creates a new object in the object manager of the specified object type.
	 * <P>
	 * <I>Note: Currently, only the PropsType is supported.</I>
	 * 
	 * @param type a reference to an ObjectType representing the type of object to create.
	 * @return a reference to a valid ObjectID if successful; null otherwise.
	 */

	public ObjectID createObject(ObjectType type)
	{
		ObjectID objectID = null;
		String typeName = type.getType();
		Hashtable t = (Hashtable) types.get(typeName);

		if (t == null)
		{
			t = new Hashtable();

			types.put(typeName, t);
			debug(typeName + " added.");
		}

		if (t == null)
		{
			error("Could not create new type: " + type);
		}
		else
		{
			if (type instanceof PropsType)
			{
				Props p = new Props();
				String id = "ID" + String.valueOf(uniqueID++);

				p.setProperty("type", typeName);
				p.setProperty("id", id);
				t.put(id, p);

				objectID = new ObjectID(typeName, id);
			}
		}

		return (objectID);
	}


	/**
	 * Retrieves the object corresponding to an object ID.
	 * 
	 * @param id a reference to an ObjectID representing a valid object in the object manager.
	 * @return a reference to an Object if one was found; null otherwise.
	 */

	public Object getObject(ObjectID id)
	{
		Object		o = null;
		Hashtable       t = (Hashtable) types.get(id.getType());

		if (t == null)
		{
			warning("getObject: Unknown type \"" + id.getType() + "\"");
		}
		else
		{
			o = t.get(id.getID());

			if (o == null)
			{
				warning("getObject: Unknown ObjectID \"" + id + "\"");
			}
		}

		return (o);
	}


	/**
	 * Returns a Vector of objects designated by the <TT>objectQuery</TT> parameter.
	 * Valid objects for this parameter are:
	 * <TABLE>
	 * <TR><TD>IObjectManager.LIST_TYPES            <TD>Lists object types that already exist.
	 * <TR><TD>IObjectManager.LIST_SUPPORTED_TYPES  <TD>Lists objects which can be passed to createObject.
	 * <TR><TD>ObjectType                           <TD>Lists all existing objects of the same ObjectType.
	 * <TR><TD>PropsQuery                           <TD>Lists Props based on data from the PropsQuery object.
	 * </TABLE>
	 * <I>Note: Some object managers may support additional values for access to non-standard features.<I>
	 * 
	 * @param objectQuery any valid object from the list above.
	 * @return a Vector containing Objects based on the query; null if the query wasn't recognized.
	 */

	public Vector listObjects(Object objectQuery)
	{
		Vector  v = new Vector();

		if (objectQuery == LIST_TYPES)
		{
			Enumeration     e = types.keys();

			while (e.hasMoreElements())
			{
				v.addElement(new ObjectType((String) e.nextElement()));
			}
		}
		else if (objectQuery == LIST_SUPPORTED_TYPES)
		{
			v.addElement("com.cometway.om.PropsType");
		}
		else if (objectQuery instanceof ObjectType)
		{
			ObjectType      type = (ObjectType) objectQuery;
			Hashtable       t = (Hashtable) types.get(type.getType());

			if (t == null)
			{
				warning("listObjects: Unknown type \"" + type.getType() + "\"");
			}
			else
			{
				Enumeration     e = t.keys();

				while (e.hasMoreElements())
				{
					v.addElement(new ObjectID(type.getType(), (String) e.nextElement()));
				}
			}
		}
		else if (objectQuery instanceof PropsQuery)
		{
			PropsQuery      q = (PropsQuery) objectQuery;
			Hashtable       t = (Hashtable) types.get(q.typeName);

			if (t == null)
			{
				warning("listObjects: Unknown type \"" + q.typeName + "\"");
			}
			else
			{
				Enumeration     e = t.elements();

				while (e.hasMoreElements())
				{
					Props   p = (Props) e.nextElement();

					if (q.value == null)
					{
						if (p.hasProperty(q.key))
						{
							v.addElement(new ObjectID(q.typeName, p.getString("id")));
						}
					}
					else
					{
						if (p.hasProperty(q.key) && (p.getProperty(q.key).equals(q.value)))
						{
							{
								v.addElement(new ObjectID(q.typeName, p.getString("id")));
							}
						}
					}
				}
			}
		}

		return (v);
	}


	/**
	 * Deletes the object corresponding to an object ID from the object manager.
	 * 
	 * @param id a reference to an ObjectID representing a valid object in the object manager.
	 * @return true if the object was successfully deleted; false otherwise.
	 */

	public boolean removeObject(ObjectID id)
	{
		boolean		result = false;
		Hashtable       t = (Hashtable) types.get(id.getType());

		if (t == null)
		{
			warning("removeObject: Unknown type \"" + id.getType() + "\"");
		}
		else
		{
			result = (t.remove(id.getID()) != null);

			if (result)
			{
				debug("removeObject: " + id + " removed.");

				if (t.isEmpty())
				{
					types.remove(id.getType());
					debug("removeObject: " + id.getType() + " removed.");
				}
			}
		}

		return (result);
	}


	/* Static methods for accessing the service manager instance. */


	/**
	 * Returns a reference to the IObjectManager registered as <I>object_manager</I>.
	 */

	public static IObjectManager getObjectManager()
	{
		return ((IObjectManager) ServiceManager.getService("object_manager"));
	}


	/**
	 * Returns a reference to the IObjectManager registered as the specified service name.
	 */

	public static IObjectManager getObjectManager(String serviceName)
	{
		return ((IObjectManager) ServiceManager.getService(serviceName));
	}


}

