package org.R2Useful;
import java.io.*;

public class SystemCallWithTimeout extends Thread {

	// the plan is to make a single deaddrop. we will then start up the system call
	// in its own thread along with a sleeper in its own thread.
	// when they finish, they will put something in the deaddrop.
	// so, we just need to wait for *anything* to show up in the deaddrop (probably a boolean
	// indicating which process left it there). then we kill off the other one and decide what to do
	
	// create a deaddrop with only one pigeonhole. 
	private DeadDropObjectArray dropPoint = new DeadDropObjectArray(1);
	
	private String commandToRun[] = null;
	private File workingDirectory = null;
	private int sleepTimeMillis = 1000;
	private int testInterval = 10;
	
	private boolean readyToGo = false;
	private boolean processTimedOut = false;
	private boolean processHasBeenAttempted = false;
	
	public final static int SYSTEM_CALL_RAN_FINE      = 0;
	public final static int SYSTEM_CALL_TIMED_OUT     = 1;
	public final static int SYSTEM_CALL_NOT_ATTEMPTED = 2;
	
	
	public int finishedCleanly() {
		int returnValue = Integer.MIN_VALUE;
		if (!processHasBeenAttempted) {
			returnValue = SYSTEM_CALL_NOT_ATTEMPTED;
		} else if (processTimedOut) {
			returnValue = SYSTEM_CALL_TIMED_OUT;
		} else {
			returnValue = SYSTEM_CALL_RAN_FINE;
		}
		
		return returnValue;
	}
	
	public void setup(String[] commandToUse, File workingDirectoryToUse, int sleepTimeMillisToUse, int testIntervalToUse) {
		dropPoint = new DeadDropObjectArray(1); // just to make sure
		
		commandToRun = commandToUse;
		workingDirectory = workingDirectoryToUse;

		sleepTimeMillis = sleepTimeMillisToUse;
		testInterval = testIntervalToUse;

		processTimedOut = false;
		processHasBeenAttempted = false;

		readyToGo = true;
	}
	
  public void run() {
  	if (readyToGo) {
  		// ok, let's run the the system call
  		SystemCall systemCall = new SystemCall();
  		systemCall.setup(commandToRun, workingDirectory, dropPoint);
//  		systemCall.run();
  		new Thread(systemCall).start();
  		processHasBeenAttempted = true;
  		
  		// and now the timer...
  		TimerThread timerThread = new TimerThread();
  		timerThread.setup(sleepTimeMillis, dropPoint, testInterval);
  		new Thread(timerThread).start();
  		
  		// grab whatever comes into the box first...
  		processTimedOut = (Boolean)dropPoint.take()[0];
  		
  		// now, kill off whatever is left
  		if (processTimedOut) {
  			systemCall.interrupt();
  		} else {
  			timerThread.interrupt();
  		}
  		
  		
  	} else {
  		System.out.println(this.toString() + " (SystemCallWithTimeout) is not ready to run; please initialize.");
  	}
  	
  }



}
