package org.R2Useful;

public class TimerThread extends Thread {

	// totally copying something outlined in http://www.javaspecialists.eu/archive/Issue056.html
	
	private DeadDropObjectArray dropPoint;
	
	private int sleepTimeMillis = 1000;

	private long startTime = System.currentTimeMillis();
	private int testInterval = 10;
	private long targetEndTime = -1;

	private boolean breakOut = false;
	
	public void setup(int sleepTimeMillisToUse, DeadDropObjectArray dropPointToUse, int testIntervalToUse) {
		dropPoint = dropPointToUse;
		sleepTimeMillis = sleepTimeMillisToUse;
		
		testInterval = testIntervalToUse;

		breakOut = false;
	}
	
  public void run() {
  	startTime = System.currentTimeMillis();
  	targetEndTime = startTime + sleepTimeMillis;
  	
  	while (System.currentTimeMillis() < targetEndTime && !breakOut) {
  		try {
  			Thread.sleep(testInterval);
  		} catch (Exception e) {
  			System.out.println("Timer Failure:");
  			e.printStackTrace();
  		}
  	}
  	dropPoint.put(true, 0);
  }
  
  public void interrupt() {
  	breakOut = true;
  }
  


}
