package com.cometway.jdbc;

	/**
	 * ObjectWriterThread is a thread that writes a serialized object
	 * to a PipedOutputStream. Used by the JDBCPropsContainer when 
	 * serializing java objects to binary data for storage in a database.
	 *
	 */

import java.io.*;
import java.util.*;

public class ObjectWriterThread implements Runnable{

  private  Thread  thread;                    // This thread.
  private  boolean working;
  private  Object  data;                      // The object that will be serialized to the PipedOutputStream.
  private Vector byteData;                    // The byte data that will be written to the PipedOutputStream.
  private PipedOutputStream pipedOutput;      // The PipedOutputStream we will write to.
  private ThreadGroup threadGroup;
  private boolean writeBytes;                 // If true, write bytes to PipedOutputStream otherwise write an object
  // private boolean connected;                  // Indicates if the Pipes have been connected for writing data.

  public ObjectWriterThread(ThreadGroup tgroup)
  {
        // connected = false;

    threadGroup = tgroup;
    pipedOutput = new PipedOutputStream();

  }

  public synchronized void setData(Object objToWrite, PipedOutputStream poutStream)
  {
        // connected = true;

    writeBytes = false;
    pipedOutput = poutStream;
    data = objToWrite;
  }

  public synchronized void setData(Object objToWrite)
  {
    writeBytes = false;
    data = objToWrite;
  }

  public synchronized void setByteData(Vector bytesToWrite, PipedOutputStream poutStream)
  {
        // connected = true;

    writeBytes = true;
    pipedOutput = poutStream;
    byteData = bytesToWrite;
  }

  public synchronized void setByteData(Vector bytesToWrite)
  {
    writeBytes = true;
    byteData = bytesToWrite;
  }

  /** Forks off this thread and executes it */
  public void start () {
    working = true;
    thread = new Thread (threadGroup, this);
    thread.start();
  }

  /** connects the PipedOutputStream to a PipedInputStream */
  public synchronized void connect(PipedInputStream in) {
        // connected = true;
        try{
                pipedOutput = new PipedOutputStream(in);
        } catch (IOException e) {
            System.out.println("*** WARNING: IOException connecting piped streams:"+e.toString());
        }
    }

  /** Runs this thread without forking. */
  public void run() {

    working = true;
    try{
        if (writeBytes)
        {
            int vectSize = byteData.size();

            for (int i = 0;i < vectSize;i++)
            {
                //System.out.println("*** writing bytes to output ***, byteData:"+new String((byte[])byteData.elementAt(i)));

                byte[] b = (byte[])byteData.elementAt(i);
                pipedOutput.write(b, 0, b.length);
                /*
                if ((vectSize==2)&&(i==0))
                {
                    byte[] nextBytes = (byte[])byteData.elementAt(1);
                    pipedOutput.write(nextBytes,0,nextBytes.length);
                    break;
                }
                */

                pipedOutput.flush();
            }

            pipedOutput.flush();
            pipedOutput.close();
           // System.out.println("*** done writing bytes to output ***");
        }
        else
        {
    		ObjectOutputStream	oout	= new ObjectOutputStream(pipedOutput);
    	    //System.out.println("**** Serializing object to output **** data:"+data);
    		oout.writeObject(data);
    		oout.flush();
    		oout.close();
    		//pipedOutput.flush();
            pipedOutput.close();
    	    //System.out.println("obj. writer thread finished serialization.");
        }
    } catch (Exception e) {
        System.out.println("Exception serializing data:"+e.toString());
    }
    working = false;
    // connected = false;
  }

  public synchronized boolean isBusy() {
    return working;
  }

}
