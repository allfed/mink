package org.R2Useful;

import java.util.Date;

public class TimerUtility {

	private long overallStartNanos = -3;
	private long overallEndNanos = -4;
	
	private long endNanos = -1;
	private long startNanos = -2;

	private long diffNanos = -3;
	
	
	public TimerUtility() {
		startNanos        = System.nanoTime();
		overallStartNanos = startNanos;
	}
	
	
	
	public void tic() {
		startNanos = System.nanoTime();
	}
	
	public long tocNanos() {
		endNanos = System.nanoTime();
		
		return endNanos - startNanos;
	}

	public double tocMillis() {
		endNanos = System.nanoTime();
		
		return (endNanos - startNanos) / 1000000.0;
	}

	public double tocSeconds() {
		endNanos = System.nanoTime();
		
		return (endNanos - startNanos) / 1000000000.0;
	}

	public double tocMinutes() {
		endNanos = System.nanoTime();
		
		return (endNanos - startNanos) / 1000000000.0 / 60.0;
	}

	public long TOCNanos() {
		endNanos = System.nanoTime();
		diffNanos = endNanos - startNanos;
		startNanos = System.nanoTime();
		
		return diffNanos;
	}

	public double TOCMillis() {
		endNanos = System.nanoTime();

		diffNanos = endNanos - startNanos;
		startNanos = System.nanoTime();

		return diffNanos / 1000000.0;
	}

	public double TOCSeconds() {
		endNanos = System.nanoTime();

		diffNanos = endNanos - startNanos;
		startNanos = System.nanoTime();

		return diffNanos / 1000000000.0;
	}

	public double TOCMinutes() {
		endNanos = System.nanoTime();
		diffNanos = endNanos - startNanos;
		startNanos = System.nanoTime();
		
		return diffNanos / 1000000000.0 / 60.0;
	}

	public long sinceStartNanos() {
		overallEndNanos = System.nanoTime();
		
		return overallEndNanos - overallStartNanos;
	}

	public double sinceStartMillis() {
		overallEndNanos = System.nanoTime();
		
		return (overallEndNanos - overallStartNanos) / 1000000.0;
		
	}

	public double sinceStartSeconds() {
		overallEndNanos = System.nanoTime();
		
		return (overallEndNanos - overallStartNanos) / 1000000000.0;
	}

	public double sinceStartMinutess() {
		overallEndNanos = System.nanoTime();
		
		return (overallEndNanos - overallStartNanos) / 1000000000.0 / 60.0;
	}

	public String sinceStartMessage(String message) {
		
		double totalSeconds = sinceStartSeconds();
		float totalMinutes = (float)(totalSeconds / 60.0);
		float totalHours   = (float)(totalSeconds / 3600.0);
		
		String outputMessage = "  == time since start for " + message + " = " + totalSeconds + "s or " +
				 totalMinutes + "m or " + totalHours + "h (current = " + new Date() + ") ==";
		
		return outputMessage;
		
	}
	
	public String sinceStartMessage() {
		return sinceStartMessage("");
	}

}

