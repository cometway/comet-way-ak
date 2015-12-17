
package com.cometway.util;

import java.io.*;


/**
 * This class has static methods that serialize and unserialize Objects to and from
 * byte[] arrays. This array will also contain the ObjectHeader with the magic numbers.
 * I highly suggest not modifying the byte[] array unless you know what you are doing.
 * @see java.io.Serializable
 * @version 0.1A
 * @author jonlin@andrew.cmu.edu
 */

public class ObjectSerializer
{


/**
 * This method unserializes a byte[] array into a single Object.
	 * Any other Objects will not be read.
	 * @param data This is the serialied data to unserialize. The entire length of the array is used.
	 * @return Returns the first Objects serialized in the byte[] array.
	 * @exception IOException Thrown when an I/O Exception occurs while creating the ObjectStream or reading the Object
	 * @exception OptionalDataException Thrown when unexpected data is in the byte[] array.
	 * @exception ClassNotFoundException Thrown when the unserialzed object's Class is not found.
 */

	public static Object unserialize(byte[] data) throws IOException, OptionalDataException, ClassNotFoundException
	{
		return (unserialize(data, 0, data.length));
	}


	/**
	 * This method unserializes a byte[] array into a single Object.
	 * Any other Objects will not be read.
	 * @param data This is the serialied data to unserialize. 
	 * @param length Up to this many bytes in the array are to be unserialized.
	 * @return Returns the first Objects serialized in the byte[] array.
	 * @exception IOException Thrown when an I/O Exception occurs while creating the ObjectStream or reading the Object
	 * @exception OptionalDataException Thrown when unexpected data is in the byte[] array.
	 * @exception ClassNotFoundException Thrown when the unserialzed object's Class is not found.
	 */


	public static Object unserialize(byte[] data, int offset, int length) throws IOException, OptionalDataException, ClassNotFoundException
	{
		try
		{
			ByteArrayInputStream    bais = new ByteArrayInputStream(data, offset, length);
			ObjectInputStream       ois = new ObjectInputStream(bais);
			Object			rval = ois.readObject();

			ois.close();

			return (rval);
		}
		catch (StreamCorruptedException sce)
		{
			throw (new IOException("Cannot unserialize byte array"));
		}
	}


	/**
	 * This method serializes an Object into a byte[] array. The length of the array is
	 * the number of valid bytes read from the ObjectStream.
	 * @param o This is the object to serialize.
	 * @return Returns a byte[] array containing the serialized Object.
	 * @exception IOException Thrown when an I/O Exception occurs while creating the ObjectStream or writing the Object 
	 * @exception NotSerializableException Thrown if the Object cannot be serializable.
	 */


	public static byte[] serialize(Object o) throws IOException, NotSerializableException
	{
		if (o instanceof Serializable)
		{
			ByteArrayOutputStream   baos = new ByteArrayOutputStream();
			ObjectOutputStream      oos = new ObjectOutputStream(baos);

			oos.writeObject(o);
			oos.flush();

			byte[]  rval = baos.toByteArray();

			oos.close();

			return (rval);
		}
		else
		{
			throw (new NotSerializableException(o.toString()));
		}
	}


}

