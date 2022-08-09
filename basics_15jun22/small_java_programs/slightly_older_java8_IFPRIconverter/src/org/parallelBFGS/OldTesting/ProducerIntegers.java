package org.parallelBFGS.OldTesting;

import java.util.Random;

public class ProducerIntegers implements Runnable {
	private DeadDropIntegers dropPoint;
	private int idInt;
	private int[] intList;

	public ProducerIntegers(int idInt, DeadDropIntegers dropPoint) {
		this.dropPoint = dropPoint;
		this.idInt = idInt;
	}

	public void setIntList(int[] intList) {
		this.intList = intList;
	}

	public void run() {
		// check for intList
		if (intList == null) {
			System.out.println(this.toString() + ": intList not set");
			return;
		}

		Random random = new Random();

		boolean successfulDrop = false;
		for (int i = 0; i < intList.length; i++) {
//			System.out.println("gonna try to put " + intList[i] + " into spot/thread # " + idInt);
			successfulDrop = dropPoint.put(intList[i], idInt);
//			System.out.println(intList[i] + " -> " + idInt);
			try {
				Thread.sleep(random.nextInt(9000));
			} catch (InterruptedException e) {}
		}
//		System.out.println("gonna try to put " + -9999 + " into spot/thread #"  + idInt);
		successfulDrop = dropPoint.put(-9999, idInt);
//		System.out.println(-9999 + " -> " + idInt);
		System.out.println("exiting producer thread #" + idInt);
	}
}


