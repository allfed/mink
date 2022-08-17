package org.R2Useful;
import java.io.*;

public class SystemCall extends Thread {

	// totally copying something outlined in http://www.javaspecialists.eu/archive/Issue056.html
	// ok, this is somewhat different, but definitely inspired by that.
	
	private DeadDropObjectArray dropPoint;
	
	private boolean readyToRun = false;
	

	private Process theRunningProcess = null;

	private String commandToRun[] = null;
	private File workingDirectory = null;
		
	public void setup(String[] commandToUse, File workingDirectoryToUse, DeadDropObjectArray dropPointToUse) {
		dropPoint = dropPointToUse;
		commandToRun = commandToUse;
		workingDirectory = workingDirectoryToUse;
		readyToRun = true;
	}
  public void run() {
  	if (readyToRun) {
  	try {
  		
  		theRunningProcess = Runtime.getRuntime().exec(commandToRun , null , workingDirectory);
  		
  		// now, what i want to do is like have something checking for the timeout
  		// so, we'll try a naive check of whether the thing has finished or not
  		// and also check whether the overall time has passed. grrr... back to where we started
  		// with inefficiency
  		
  		
    	theRunningProcess.waitFor();
    	dropPoint.put(false, 0);
  	} catch (Exception e) {
  		System.out.println("Failure to run:");
  		e.printStackTrace();
  	}
  	} else {
  		System.out.println(this.toString() + " (SystemCall) is not ready to run; please initialize.");
  	}
  }
  
  public void interrupt() {
  	// sometimes, the process seems to finish up between when the timeout occurs and the attempted destruction
  	if (theRunningProcess != null) {
  		theRunningProcess.destroy();
  	}
  }


}
