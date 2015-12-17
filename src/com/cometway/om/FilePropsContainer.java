
package com.cometway.om;

import com.cometway.props.IPropsContainer;
import com.cometway.util.*;
import java.util.*;
import java.io.*;

/**
 * The FilePropsContainer is a PropsContainer that keeps a file of all the props. When props
 * are changed, a new file is written. 
 */
public class FilePropsContainer implements IPropsContainer
{
	/** This toggles debug output for all FilePropsContainers */
	private final static boolean debug = false;


	/** This is a local copy of each property */
	Hashtable props = new Hashtable();

	/** This is the file that the props are stored */
	File rootfile;

	/** This is the FileObjectStore used for this PropsContainer */
	FileObjectStore store;
	
	/** This flag lets the FilePropsContainer know whether its props are valid */
	boolean listed = false;


	/** 
	 * This creates a new instance of FilePropsContainer that uses the fully 
	 * qualifed file pathname given by the parameter.
	 * @param rootdir This is the fully qualified file path to store properties.
	 */
	public FilePropsContainer(String rootdir)
	{
		this(new File(rootdir));
	}

	/** 
	 * This creates a new instance of FilePropsContainer that uses the File
	 * object given as a parameter.
	 * @param rootfile This is the File which properties will be stored.
	 */
	public FilePropsContainer(File rootfile)
	{
		this.rootfile = rootfile;
		try {
			store = new FileObjectStore(rootfile,"",1024*5);
		}
		catch(Exception e) {
			error("Exception caught while creating ObjectStore for file: "+rootfile+".",e);
		}
		props = new Hashtable();
		init();
	}
	
	/** 
	 * This method gets a the cached value of the property
	 * name given as the parameter.
	 * @param s This is the name of the property whose value to retrieve.
	 * @return Returns the value of the property whose name is the parameter.
	 */
	public Object getProperty(String s) 
	{
		Object rval = null;
		PropertyCache cache = (PropertyCache)props.get(s);
		if(cache!=null) {
			rval = cache.getValue();
		}
		return(rval);
	}

	/** 
	 * This method removes the property name and the associated value
	 * given by the parameter.
	 * @param s This is the name of the property to remove.
	 * @return Returns true IFF the property name and value where removed successfully.
	 */
	public boolean removeProperty(String s) 
	{
		boolean rval = false;
		print("removing: "+s);
		PropertyCache cache = (PropertyCache)props.remove(s);
		if(cache!=null) {
			cache.dispose();
			rval = true;
		}
		return(rval);
	}

	/** 
	 * This method sets a property value whose name is given by the first parameter.
	 * If this name had a previous value, it will be lost. If there was no
	 * property whose name is given by the first parameter, a new cache is created
	 * for this property and the value written to it.
	 * @param s This is the name of the property whose value is to be set.
	 * @param o This is the value which to set the property to.
	 */
	public void setProperty(String s, Object o) 
	{
		if(o == null) {
			removeProperty(s);
		}
		else	{
			print("setting cache: "+s+" to "+o);
			PropertyCache cache = (PropertyCache)props.get(s);
			if(cache!=null) {
				cache.writeValue(o);
			}
			else {
				cache = new PropertyCache(store.getNextFreeBlock());
				cache.writeName(s);
				props.put(s,cache);
				cache.writeValue(o);
			}
		}
	}

	/** 
	 * This method copies the property name and value pairs to the IPropsContainer
	 * given as a parameter.
	 * @param ipc The IPropsContainer to copy the properties TO.
	 */
	public void copy(IPropsContainer ipc)
	{
		String s;
		Enumeration e = props.keys();
		while(e.hasMoreElements())	{
			s = e.nextElement().toString();
			ipc.setProperty(s,getProperty(s));
		}
	}

	/**
	 * This method returns all the property names of the properties in this container.
	 */
	public Enumeration enumerateProps()
	{
		return(props.keys());
	}


	/**
	 * This method removes the file that stored properties of this Container.
	 */
	protected void dispose()
	{
		Enumeration e = enumerateProps();
		while(e.hasMoreElements()) {
			String key = e.nextElement().toString();
			PropertyCache cache = (PropertyCache)props.get(key);
			cache.dispose();
		}
		store.dispose();
	}



	// This is called by the FileObjectManager to Write the ID into the Store file header
	protected void setObjectID(String newID)
	{
		store.changeFileInfo(newID);
	}		

	protected String getObjectID()
	{
		return(store.getFileInfo());
	}



	/**
	 * This method initializes this FilePropsContainer with a file.
	 */
	void init()
	{
		FileObjectBlock block = null;
		int x=0;
		try {
			while(true) {
				block = store.getBlock(x++);
				if(!block.isFreeBlock()) {
					Object headerData = block.readHeader();
					if((headerData!=null) && (headerData instanceof String)) {
						PropertyCache cache = new PropertyCache(block);
						props.put((String)headerData,cache);
					}
				}
			}
		}
		catch(ArrayIndexOutOfBoundsException sowhat) {
			;
		}
	}	


	private void print(String s) 
	{
		if (debug) System.out.println("[FilePropsContainer("+toString()+")] "+s);
	}  
	private void error(String s) 
	{
		System.err.println("{FilePropsContainer} ERROR: "+s);
	}
	private void error(String s, Exception e)
	{
		System.err.println("{FilePropsContainer} "+e+" : "+s);
		e.printStackTrace();
	}


	/**
	 * This class is a placeholder for the value of a property. 
	 * When the property is 'set', the FileObjectBlock is written.
	 * When the property is read, the cached value is returned.
	 */
	class PropertyCache
	{
		Object value;
		FileObjectBlock block;

		public PropertyCache(FileObjectBlock block)
		{
			this.block = block;
		}

		public Object getName()
		{
			return(block.readHeader());
		}

		public synchronized boolean writeName(String name)
		{
			return(block.writeHeader(name));
		}

		public Object getValue()
		{
			if(value==null) {
				value = block.readObject();
			}
			return(value);
		}

		public synchronized boolean writeValue(Object newValue)
		{
			value = newValue;
			return(block.writeObject(newValue));
		}

		public void dispose()
		{
			block.dispose();
		}
	}
		




	public static void main(String[] args)
	{
		if(args.length==0) {
			System.out.println("Usage: java FilePropsContainer <files>");
			System.exit(-1);
		}
	
		for(int x=0;x<args.length;x++) {
			try {
				System.out.println("-------------------------------------");
				FilePropsContainer container = new FilePropsContainer(new File(args[x]));
				System.out.println("ObjectID = "+container.getObjectID());
				Enumeration keys = container.enumerateProps();
				while(keys.hasMoreElements()) {
					try {
						String key = (String)keys.nextElement();
						System.out.print(args[x]+":  '"+key+"' = ");
						System.out.print("'"+container.getProperty(key)+"'\n");
					}
					catch(Exception e) {
						System.out.println();
					}
				}
			}
			catch(Exception e) {
				System.out.println("-------------------------------------");
			}
		}
	}								

}
