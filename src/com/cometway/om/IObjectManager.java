
package com.cometway.om;


import java.util.*;


/**
* This is the generic interface for an Object Manager.
*/

public interface IObjectManager
{
	/**
	* This object can be passed to the listObjects method to return a Vector containing
	* the ObjectTypes which currently exist in the object manager.
	*/

	public final static String LIST_TYPES = "LIST_TYPES";


	/**
	* This object can be passed to the listObjects method to return a Vector containing
	* the full class names of ObjectTypes which are supported by the createObject method.
	*/

	public final static String LIST_SUPPORTED_TYPES = "LIST_SUPPORTED_TYPES";


	/**
	* Creates a new object in the object manager of the specified object type.
	* <P>
	* <I>Note: Currently, only the PropsType is supported.</I>
	* 
	* @param type a reference to an ObjectType representing the type of object to create.
	* @return a reference to a valid ObjectID if successful; null otherwise.
	*/

	public ObjectID createObject(ObjectType type);


	/**
	* Retrieves the object corresponding to an object ID.
	*
	* @param id a reference to an ObjectID representing a valid object in the object manager.
	* @return a reference to an Object if one was found; null otherwise.
	*/

	public Object getObject(ObjectID id);


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

	public Vector listObjects(Object objectQuery);


	/**
	* Deletes the object corresponding to an object ID from the object manager.
	*
	* @param id a reference to an ObjectID representing a valid object in the object manager.
	* @return true if the object was successfully deleted; false otherwise.
	*/

	public boolean removeObject(ObjectID id);



	/**
	 * This method changes an object's ObjectID.
	 */
	public boolean changeObjectID(ObjectID oldID, ObjectID newID);

}


