package org.parallelBFGS.OldTesting;

import java.util.Random;

public class ProducerDoubles implements Runnable {
	private DeadDropDoubles dropPoint;
	private int idInt;
	private double[] doubleList;

	public ProducerDoubles(int idInt, DeadDropDoubles dropPoint) {
		this.dropPoint = dropPoint;
		this.idInt = idInt;
	}

	public void setDoubleList(double[] doubleList) {
		this.doubleList = doubleList;
	}

	public void run() {
		// check for intList
		if (doubleList == null) {
			System.out.println(this.toString() + ": intList not set");
			return;
		}

		Random random = new Random();

//		boolean successfulDrop = false;
		for (int i = 0; i < doubleList.length; i++) {
//			System.out.println("gonna try to put " + intList[i] + " into spot/thread # " + idInt);
			dropPoint.put(doubleList[i], idInt);
//			System.out.println(intList[i] + " -> " + idInt);
			try {
				Thread.sleep(random.nextInt(9000));
			} catch (InterruptedException e) {}
		}
//		System.out.println("gonna try to put " + -9999 + " into spot/thread #"  + idInt);
		dropPoint.put(Double.NaN, idInt);
//		System.out.println(-9999 + " -> " + idInt);
		System.out.println("exiting producer thread #" + idInt);
	}
}


