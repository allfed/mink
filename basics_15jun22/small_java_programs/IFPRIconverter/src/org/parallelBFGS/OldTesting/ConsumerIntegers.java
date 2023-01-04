package org.parallelBFGS.OldTesting;

import java.util.Random;

public class ConsumerIntegers implements Runnable {
	private DeadDropIntegers dropPoint;
	private int idInt;

	public ConsumerIntegers(int idInt, DeadDropIntegers dropPoint) {
		this.idInt = idInt;  
		this.dropPoint = dropPoint;
	}

	private boolean areAllEndValue(int[] intList) {
		boolean bailFlag = true;
		for (int spotIndex = 0; spotIndex < intList.length; spotIndex++) {
			if (intList[spotIndex] != -9999) {
				bailFlag = false;
				break;
			}
		}

		return bailFlag;
	}

	public void run() {
		Random random = new Random();
		for (int[] retrievedList = dropPoint.take(); !areAllEndValue(retrievedList); retrievedList = dropPoint.take()) {
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
