package org.parallelBFGS.OldTesting;

public class DeadDropIntegers {

	private final static int magicEmptyValue = -8888;
	private int nSpots = -1;
	private int nFull = 0;
	//Message sent from producer to consumer.
	private int[] collectedIntegers;
	//True if consumer should wait for producer to send message, false
	//if producer should wait for consumer to retrieve message.
	private boolean[] emptyFlags;

	public DeadDropIntegers(int nSpots) {
		this.nSpots            = nSpots;
		this.collectedIntegers = new int[    nSpots];
		this.emptyFlags        = new boolean[nSpots];
		this.nFull             = 0;

		// mark all spots as empty
		for (int spotIndex = 0; spotIndex < nSpots; spotIndex++) {
			this.emptyFlags[spotIndex] = true;
			this.collectedIntegers[spotIndex] = magicEmptyValue;
		}
	}

	public synchronized int[] take() {
		//Wait until message is available.
//		while ( !areAllFull() ) {
		
		int[] listToSend = new int[nSpots];
		
//		System.out.print("     take BEFORE anything: nFull = " + nFull + " ; ");
//		for (int spotIndex = 0 ; spotIndex < nSpots; spotIndex++) {
//			System.out.print(collectedIntegers[spotIndex] + "/" + emptyFlags[spotIndex] + ",");
//		}
//		System.out.print("\n");
		
		while ( nFull < nSpots ) {
//			System.out.println("    " + nFull + " of " + nSpots + " taken; take is waiting");
			try {
				wait();
			} catch (InterruptedException e) {}
		}
		//Toggle status.
//		empty = true;
		// mark all spots as empty
//		System.out.print("     take AWAKE, before changes: nFull = " + nFull + " ; ");
//		for (int spotIndex = 0 ; spotIndex < nSpots; spotIndex++) {
//			System.out.print(collectedIntegers[spotIndex] + "/" + emptyFlags[spotIndex] + ",");
//		}
//		System.out.print("\n");

		for (int spotIndex = 0; spotIndex < nSpots; spotIndex++) {
			this.emptyFlags[spotIndex] = true;
			listToSend[spotIndex] = collectedIntegers[spotIndex];
			this.collectedIntegers[spotIndex] = magicEmptyValue;
		}

		this.nFull = 0;
		//Notify producer that status has changed.

//		System.out.print("     take AWAKE, after taking: nFull = " + nFull + " ; ");
//		for (int spotIndex = 0 ; spotIndex < nSpots; spotIndex++) {
//			System.out.print(collectedIntegers[spotIndex] + "/" + emptyFlags[spotIndex] + ",");
//		}
//		System.out.print("\n");

		notifyAll();
//		return collectedIntegers;
		return listToSend;
	}

	public synchronized boolean put(int value, int spot) {

		// check if the desired spot exists and is open
		if (spot >= nSpots) {
			System.out.println("tried to store in spot " + spot + " which does not exist; nSpots = " + nSpots);
			return false;
		}

//		System.out.print("     put BEFORE anything: nFull = " + nFull + " ; ");
//		for (int spotIndex = 0 ; spotIndex < nSpots; spotIndex++) {
//			System.out.print(collectedIntegers[spotIndex] + "/" + emptyFlags[spotIndex] + ",");
//		}
//		System.out.print("\n");

		while (!emptyFlags[spot]) {
//			System.out.println("tried to store in spot which is already occupied: " + spot);
//			return false;
			try {
				wait();
			} catch (InterruptedException f) {}
		}

//		System.out.println("     put has awakened to store value [" + value + "] ...");
//
//		System.out.print("     put BEFORE changing: nFull = " + nFull + " ; ");
//		for (int spotIndex = 0 ; spotIndex < nSpots; spotIndex++) {
//			System.out.print(collectedIntegers[spotIndex] + "/" + emptyFlags[spotIndex] + ",");
//		}
//		System.out.print("\n");

		
		// Mark value as stored
		emptyFlags[spot] = false;
		// Store the value
		collectedIntegers[spot] = value;

//		System.out.println("    -> old nFull = " + nFull);

		int tempNFull = 0;
		for (int spotIndex = 0; spotIndex < nSpots ; spotIndex++) {
			if (!this.emptyFlags[spotIndex]) {
				tempNFull++;
			}
		}
		this.nFull = tempNFull;
		
//		System.out.print("     put AFTER changing: nFull = " + nFull + " ; ");
//		for (int spotIndex = 0 ; spotIndex < nSpots; spotIndex++) {
//			System.out.print(collectedIntegers[spotIndex] + "/" + emptyFlags[spotIndex] + ",");
//		}
//		System.out.print("\n");

		notifyAll();

		// signify success
		return true;
	}


}
