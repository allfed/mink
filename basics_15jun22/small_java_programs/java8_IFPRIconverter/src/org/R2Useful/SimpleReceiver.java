package org.R2Useful;

import java.net.*;
import java.util.Date;
import java.io.*;


public class SimpleReceiver implements Runnable {
  
  private int        PortNumber = -1;
  private boolean listeningOnPort = false;
  private boolean verboseStatus = false;
  
  private Socket theSocketWeUse = null;
  private ObjectInputStream receiverIn = null;
  
  private long nObjectsReceived = 0;
  
  private String label = "";
  
  
  public SimpleReceiver(int PortNumber, boolean VerboseStatus) {
  	this.PortNumber      = PortNumber;
  	this.verboseStatus   = VerboseStatus;
  	this.listeningOnPort = false;
  	
  	this.nObjectsReceived = 0;
  }
  
  public SimpleReceiver(int PortNumber, String label) {
  	this.PortNumber      = PortNumber;
  	this.verboseStatus   = true;
  	this.listeningOnPort = false;
  	this.label           = label;

  	this.nObjectsReceived = 0;
  }
  
  public synchronized void setVerbosity(boolean status) {
  	this.verboseStatus = status;
  }
  public synchronized void resetCounter() {
  	this.nObjectsReceived = 0;
  }
  
  public synchronized boolean isListening() {
  	return this.listeningOnPort;
  }
  
  public synchronized void startListening() throws Exception {
  	if (verboseStatus) {
  		System.out.println(" [" + this.toString() + "; " + label + "] attempting to listen on " + PortNumber);
  	}
  	// start up a listening socket and accept an incoming request
  	ServerSocket basicListeningSocket = new ServerSocket(PortNumber);
  	if (verboseStatus) {
  		System.out.println(" [" + this.toString() + "; " + label+ "] set up the listener... [" + basicListeningSocket + "] at " + new Date());
  	}
  	theSocketWeUse = basicListeningSocket.accept();
  	if (verboseStatus) {
  		System.out.println("[" + this.toString() + "; " + label+ "] accepted a connection from " + theSocketWeUse.getInetAddress() +
  				" at " + new Date() );
  	}

  	// start up the input stream
  	receiverIn = new ObjectInputStream(theSocketWeUse.getInputStream());
  	listeningOnPort = true;
  	notifyAll();

  }
  
  public synchronized void stopListening() throws Exception {
  	if (verboseStatus) {
  		System.out.println(" [" + this.toString() + "; " + label+ "] about to close Socket " + theSocketWeUse);
  	}
  	if (this.listeningOnPort) {
  		receiverIn.close();
  		listeningOnPort = false;
  	}
  	notifyAll();
  }

  public synchronized Object pullObject() throws Exception {

  	// check if we can listen at all
  	while (!listeningOnPort) {
  		try {
  			wait();
  		} catch (InterruptedException f) {}
  	}
  	
  	Object theReceivedObject = null;
  	
  	theReceivedObject = receiverIn.readObject();
  	nObjectsReceived++;
  	
    if (verboseStatus) {
      System.out.println("  [" + this.toString() + "; " + label + "] received object [" + theReceivedObject +
      		"] #" + nObjectsReceived + " from " +
          theSocketWeUse.getInetAddress() + " at " + new Date());
    }
    
  	notifyAll();

  	return theReceivedObject;
  }

  public synchronized long getNObjectsReceived() {
  	return this.nObjectsReceived;
  }
  
  public void run() {
  
  	try {
  		this.startListening();
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }



}

