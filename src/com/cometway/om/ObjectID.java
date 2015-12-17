
package com.cometway.om;

/**
* This class uniquely identifies an Object contained within an ObjectManager.
*/

public class ObjectID extends ObjectType
{
	protected String id;

	/**
	* Object type and id parameters must be only alphanumeric
	* characters including underscores ('_').
	* Use of the pipe ('|') symbol is strictly prohibited.
	*/

	public ObjectID(String type, String id)
	{
		if (type == null || (type.length() == 0))
		{
			throw new NullPointerException("Invalid ObjectID: type cannot be null or zero length.");
		}

		if (id == null || (id.length() == 0))
		{
			throw new NullPointerException("Invalid ObjectID: id cannot be null or zero length.");
		}

		this.type	= type;
		this.id		= id;
	}


	/**
	* Takes a fully qualified type/id string using the format "type|id".
	*/

	public ObjectID(String objectID)
	{
		int l;
		int i;

		if (objectID == null) throw new NullPointerException("Invalid objectID: objectID cannot be null.");

		l = objectID.length();

		if (l == 0) throw new RuntimeException("Invalid objectID: objectID is zero length.");

		i = objectID.indexOf('|');

		if ((i < 1) || (i > (l - 2))) throw new RuntimeException("Invalid objectID: " + objectID);

		type	= objectID.substring(0, i);
		id		= objectID.substring(i + 1, l);
	}


	/**
	* Returns the ID for this class.
	*/

	public String getID()
	{
		return (id);
	}

	/**
	* Returns the hashcode for this class.
	*/

        public int hashCode()
        {
                return toString().hashCode();
        }


        public boolean equals(Object o)
        {
                boolean same = false;

                if (o instanceof ObjectID)
                        same = toString().equals(((ObjectID) o).toString());
                else if (o instanceof String)
                        same = toString().equals(o);

                return same;
        }


	public String toString()
	{
		return (type + '|' + id);
	}
}


