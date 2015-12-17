
package com.cometway.om;

import com.cometway.util.Queue;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;


/**
 * This class takes a File and partitions it into FileObjectBlocks. 
 * This class will keep track of free blocks and allocate new blocks if needed.
 * @see FileObjectBlock
 */
public class FileObjectStore
{
	final static boolean debug = false;
	final static boolean verbose = true;
	final static boolean printStackTraces = true;

	final static long DEFAULT_BLOCK_SIZE = 32 * 1024;  // Default block size = 32K

	// This is the size each block will take up in this file.
	protected long BLOCK_SIZE;
	
	protected long headerLength;  // length of header, first block starts here.
	protected String headerInfo;  // This is the bit of info stored as a description for this file.

	// This the RandomAccessFile version of the storeFile
	RandomAccessFile raf;
	// This is the File version of the storeFile
	File store;
	// These are the FileObjectBlocks, in order, which are allocated in this file
	Vector blocks;
	// These are the already allocated blocks which can be reused.
	Queue freeBlocks;

	// This is the synchronizing Object when the next free block is allocated.
	Object FREE_BLOCK_SYNC;

	/**
	 * Creates an instance of FileObjectStore using an empty description and the
	 * default block size. The parameter passed in will be used to initialize
	 * any previous information in this file (NOTE that the BLOCK sizes must be the
	 * same) and for reading and writing.
	 * @param storeFile This is the File which will be used for initialization and all reads and writes.
	 * @exception IOException This is thrown if the FileObjectStore could not be created from the given parameters.
	 */
	public FileObjectStore(File storeFile) throws IOException
	{
		this(storeFile, "",DEFAULT_BLOCK_SIZE);
	}

	/**
	 * Creates an instance of FileObjectStore using the description parameter and the
	 * default block size. The File parameter passed in will be used to initialize
	 * any previous information in this file (NOTE that the BLOCK sizes must be the
	 * same) and for reading and writing.
	 * @param storeFile This is the File which will be used for initialization and all reads and writes.
	 * @param fileInfo This will be stored in the header of this store file (NOT block headers) as a description for this file.  If this is an empty String, the original fileInfo will attempt to be read in.
	 * @exception IOException This is thrown if the FileObjectStore could not be created from the given parameters.
	 */
	public FileObjectStore(File storeFile, String fileInfo) throws IOException
	{
		this(storeFile,fileInfo,DEFAULT_BLOCK_SIZE);
	}

	/**
	 * Creates an instance of FileObjectStore using the description parameter and the
	 * given block size. The File parameter passed in will be used to initialize
	 * any previous information in this file (NOTE that the BLOCK sizes must be the
	 * same) and for reading and writing. The Block size must exceed the required size 
	 * the Block's header.
	 * @see FileObjectBlock
	 * @param storeFile This is the File which will be used for initialization and all reads and writes.
	 * @param fileInfo This will be stored in the header of this store file (NOT block headers) as a description for this file. If this is an empty String, the original fileInfo will attempt to be read in.
	 * @param blockSize This will be the size each FileObjectBlock allocated by this FileObjectStore.
	 * @exception IOException This is thrown if the FileObjectStore could not be created from the given parameters.
	 */
	public FileObjectStore(File storeFile, String fileInfo, long blockSize) throws IOException
	{
		if(storeFile==null) {
			throw(new NullPointerException("Store file is null."));
		}
		if(!storeFile.isFile()) {
			try {
				FileWriter writer = new FileWriter(storeFile);
				writer.write(0);
			}
			catch(Exception e) {
				throw(new IOException("Store file is not a regular file: "+storeFile));
			}
			if(!storeFile.isFile()) {
				throw(new IOException("Store file is not a regular file: "+storeFile));
			}
		}
		else if(!storeFile.canRead()) {
			throw(new IOException("Store file is unreadable: "+storeFile));
		}
		else if(!storeFile.canWrite()) {
			throw(new IOException("Store file is not writeable: "+storeFile));
		}
		store = storeFile;
		raf = new RandomAccessFile(store,"rw");

		FREE_BLOCK_SYNC = new Object();
		
		if(fileInfo==null) {
			fileInfo = "";
		}
		if(fileInfo.equals("")) {
			try {
				readHeader();
			}
			catch(Exception e) {
				headerInfo = fileInfo;
			}
		}
		else {
			headerInfo = fileInfo;
		}

		BLOCK_SIZE = blockSize;

		blocks = new Vector();
		freeBlocks = new Queue();

		if(raf.length()==0) {
			writeHeader();
		}
		else {
			readHeader();
		}
		
		initBlocks();
	}


	/**
	 * This method reads the String info stored in the header of this Store and returns it.
	 */
	public String getFileInfo()
	{
		return(headerInfo);
	}

	/**
	 * This method changes the String info stored in the header of this Store to the
	 * String parameter. True will be returned if the change was successful.
	 */
	public synchronized boolean changeFileInfo(String newInfo)
	{
		boolean rval = false;
		try {
			headerInfo = newInfo;
			writeHeader();
			rval = true;
		}
		catch(IOException e) {
			error("Exception caught while writing header.",e);
		}
		return(rval);
	}

	/** 
	 * This method retrieves the previously allocated FileObjectBlock with the
	 * index given. 
	 * @param index This is the block index of the FileObjectBlock to retrieve.
	 * @exception ArrayIndexOutOfBoundsException This is thrown if the index given not used by this Store.
	 */
	public FileObjectBlock getBlock(int index) throws ArrayIndexOutOfBoundsException
	{
		if(index>blocks.size()) {
			throw(new ArrayIndexOutOfBoundsException("Index exceeds allocated number of FileObjectBlocks: "+index));
		}
		if(index<0) {
			throw(new ArrayIndexOutOfBoundsException("Index cannot be negative: "+index));
		}
		FileObjectBlock rval = (FileObjectBlock)blocks.elementAt(index);
		//		try {
		//			if(blocks.indexOf(rval)==-1) {
		//				rval.format();
		//				blocks.addElement(rval);
		//			}
		//		}
		//		catch(Exception e) {
		//			error("Exception caught while retrieving block at index: "+index,e);
		//		}
		return(rval);
	}

	/**
	 * This method returns the next Free Block. If one does not exist (all allocated blocks 
	 * are being used), another one will be allocated. If another cannot be allocated,
	 * null is returned.
	 * @return Returns a FileObjectBlock available for storing Object in, null if no more are available.
	 */
	public FileObjectBlock getNextFreeBlock()
	{
		FileObjectBlock rval = null;
		try {
			synchronized (FREE_BLOCK_SYNC) {
				if(freeBlocks.size()==0) {
					//				rval = new FileObjectBlock(this,blocks.size());
					//				rval.format();
					//				blocks.addElement(rval);
					if(allocateNewBlocks(1)) {
						rval = (FileObjectBlock)freeBlocks.nextElement();
					}
				}
				else {
					rval = (FileObjectBlock)freeBlocks.nextElement();
				}
			}
		}
		catch(Exception e) {
			error("Exception caught while getting free block.",e);
		}



		if(debug) {
			debug("BLOCK MAP: free blocks: "+freeBlocks.size());
			for(int x=0;x<blocks.size();x++) {
				FileObjectBlock block = (FileObjectBlock)blocks.elementAt(x);
				debug(" ["+x+"] isFreeBlock="+(block.isFreeBlock())+" isEmpty="+(block.isEmpty));
				if(freeBlocks.contains(block)) {
					debug("         is a FREEBLOCK");
				}
			}
		}

		return(rval);
	}

	/**
	 * This method allocates new blocks at the end of the store file. 
	 * These blocks will have been formatted and are stored in the freeBlocks
	 * Queue.
	 * @param number The number of new blocks to allocate.
	 * @return Returns true IFF the specified number of new blocks were allocated successfully.
	 */
	public synchronized boolean allocateNewBlocks(int number)
	{
		try {
			int index = blocks.size();
			for(int x=0;x<number;x++) {
				FileObjectBlock block = new FileObjectBlock(this,index++);
				block.format();
				blocks.addElement(block);
				freeBlocks.addElement(block);
			}
		}
		catch(Exception e) {
			error("Exception caught while allocating new blocks.",e);
			return(false);
		}
		return(true);
	}			

	/**
	 * This method removes this file from the filesystem and frees resources that it was using.
	 */
	public void dispose()
	{
		try {
			raf.close();
			store.delete();
		}
		catch(Exception e) {;}
	}

	


		
	/**
	 * This method attempts to initialize all the blocks stored in the file.
	 * Here is where the chains are rebuilt. This method calls FileObjectBlock.updateFromFile().
	 */
	protected synchronized void initBlocks() throws IOException
	{
		try {
			raf.seek(headerLength);
		}
		catch(IOException e) {
			raf.close();
			raf = new RandomAccessFile(store,"rw");
			raf.seek(headerLength);
		}
				
		int estimatedBlocks = (int)((raf.length()-headerLength)/BLOCK_SIZE);
		for(int x=0;x<estimatedBlocks;x++) {
			FileObjectBlock block = new FileObjectBlock(this,x);
			if(block.isFreeBlock()) {
				freeBlocks.addElement(block);
			}
			blocks.addElement(block);
		}
		
		// Need to have the Blocks map filled before connecting them together.
		for(int x=0;x<estimatedBlocks;x++) {
			FileObjectBlock block = (FileObjectBlock)blocks.elementAt(x);
			if(!block.isFreeBlock()) {
				block.isEmpty=false;
				block.updateFromFile();
				//				block.readObject();
			}
		}
	}
	
	/**
	 * This method writes the store file header information to the physical file.
	 */
 	protected synchronized void writeHeader() throws IOException
	{
		if(raf.getFilePointer()!=0) {
			try {
				raf.seek(0);
			}
			catch(IOException e) {
				raf.close();
				raf = new RandomAccessFile(store,"rw");
				raf.seek(0);
			}
		}
		raf.writeUTF(headerInfo);
		
		debug("writeHeader(): file pointer = "+raf.getFilePointer());

		headerLength = raf.getFilePointer();
	}
	
	/**
	 * This method reads the store file header information from the physical file.
	 * The local field is updated.
	 */
	protected synchronized void readHeader() throws IOException
	{
		if(raf.getFilePointer()!=0) {
			try {
				raf.seek(0);
			}
			catch(IOException e) {
				raf.close();
				raf = new RandomAccessFile(store,"rw");
				raf.seek(0);
			}
		}
		
		headerInfo = raf.readUTF();

		debug("writeHeader(): file pointer = "+raf.getFilePointer());

		headerLength = raf.getFilePointer();
	}
	
	
	/**
	 * This method reads a long at the position in the file given as the parameter.
	 */
	protected synchronized long readLong(long start) throws IOException
	{
		if(start<0) {
			throw(new IOException("Cannot read long starting at: "+start));
		}
		try {
			raf.seek(start);
		}
		catch(IOException e) {
			raf.close();
			raf = new RandomAccessFile(store,"rw");
			raf.seek(start);
		}
		return(raf.readLong());
	}

	/**
	 * This method writes a long at the position in the file given as the parameter.
	 */
	protected synchronized void writeLong(long data, long start) throws IOException
	{
		if(start<0) {
			throw(new IOException("Cannot write long starting at: "+start));
		}
		try {
			raf.seek(start);
		}
		catch(IOException e) {
			raf.close();
			raf = new RandomAccessFile(store,"rw");
			raf.seek(start);
		}
		raf.writeLong(data);
	}

	/**
	 * This method reads an int at the position in the file given as the parameter.
	 */
	protected synchronized int readInt(long start) throws IOException
	{
		if(start<0) {
			throw(new IOException("Cannot read int starting at: "+start));
		}
		try {
			raf.seek(start);
		}
		catch(IOException e) {
			raf.close();
			raf = new RandomAccessFile(store,"rw");
			raf.seek(start);
		}
		return(raf.readInt());
	}
	
	/**
	 * This method writes an int at the position in the file given as the parameter.
	 */
	protected synchronized void writeInt(int data, long start) throws IOException
	{
		if(start<0) {
			throw(new IOException("Cannot write long starting at: "+start));
		}
		try {
			raf.seek(start);
		}
		catch(IOException e) {
			raf.close();
			raf = new RandomAccessFile(store,"rw");
			raf.seek(start);
		}
		raf.writeInt(data);
	}

	/**
	 * This method reads a byte array from the file at the positions given as the
	 * parameters. 
	 */
	protected synchronized byte[] readBytes(long start, long stop) throws IOException
	{
		if((stop<0) || (start<0)) {
			throw(new IOException("Cannot read bytes starting from "+start+" to "+stop+"."));
		}
		if(stop<start) {
			throw(new IOException("Cannot read bytes starting from "+start+" to "+stop+"."));
		}

		byte[] rval = new byte[(int)(stop-start)];
		try {
			raf.seek(start);
		}
		catch(IOException e) {
			raf.close();
			raf = new RandomAccessFile(store,"rw");
			raf.seek(start);
		}
		raf.read(rval);

		return(rval);
	}

	/**
	 * This method writes a byte array to the file starting at the position given
	 * by the parameter.
	 */
	protected synchronized void writeBytes(byte[] data, long start) throws IOException
	{
		if((data==null) || (start<0)) {
			throw(new IOException("Cannot write data starting from: "+start));
		}

		try {
			raf.seek(start);
		}
		catch(IOException e) {
			raf.close();
			raf = new RandomAccessFile(store,"rw");
			raf.seek(start);
		}
		raf.write(data);
	}
	










	/**
	 * Overrides Object.equals(Object).
	 * Makes the Store File (java.io.File) object an equavalence relation.
	 */
	public boolean equals(Object o)
	{
		return(store.equals(o));
	}

	/**
	 * Overrides Object.hashCode().
	 * Uses the java.io.File as the hashCode.
	 */
	public int hashCode()
	{
		return(store.hashCode());
	}



	// Runtime Status methods...

	protected void print(String s)
	{
		if(verbose) {
			System.out.println("[FileObjectStore] "+s);
		}
	}

	protected void error(String s)
	{
		System.err.println("{FileObjectStore} ERROR: "+s);
	}

	protected void error(String s, Exception e)
	{
		System.err.println("{FileObjectStore} "+e+" : "+s);
		if(printStackTraces) {
			e.printStackTrace(System.err);
		}
	}

	protected void debug(String s)
	{
		if(debug) {
			System.out.println("[FileObjectStore] DEBUG: "+s);
		}
	}

}
