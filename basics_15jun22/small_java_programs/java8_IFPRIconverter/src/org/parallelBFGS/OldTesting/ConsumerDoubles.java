package org.parallelBFGS.OldTesting;

import java.util.Random;

public class ConsumerDoubles implements Runnable {
	private DeadDropDoubles dropPoint;
	private int idInt;

	public ConsumerDoubles(int idInt, DeadDropDoubles dropPoint) {
		this.idInt = idInt;  
		this.dropPoint = dropPoint;
	}

	private boolean areAllEndValue(double[] doubleList) {
		boolean bailFlag = true;
		for (int spotIndex = 0; spotIndex < doubleList.length; spotIndex++) {
			if (!Double.isNaN(doubleList[spotIndex])) {
				bailFlag = false;
				break;
			}
		}

		return bailFlag;
	}

	public void run() {
		Random random = new Random();
		for (double[] retrievedList = dropPoint.take(); !areAllEndValue(retrievedList); retrievedList = dropPoint.take()) {
			System.out.println("MESSAGE RECEIVED:");
			for (int spotIndex = 0; spotIndex < retrievedList.length; spotIndex++) {
				System.out.println("consumer #" + idInt + ", [" + spotIndex + "] = " + retrievedList[spotIndex]);
			}
			try {
				Thread.sleep(random.nextInt(2000));
			} catch (InterruptedException e) {}
		}
		System.out.println("--we now seem to be done; consumer #" + idInt + " --");
	}
}
