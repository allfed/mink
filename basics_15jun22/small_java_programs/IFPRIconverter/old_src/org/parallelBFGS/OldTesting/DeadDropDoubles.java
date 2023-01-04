package org.parallelBFGS.OldTesting;

public class DeadDropDoubles {

	private final static double magicEmptyValue = Double.NaN;
	private int       nSpots = -1;
	private int       nFull  = 0;
	private double[]  collectedDoubles;
	private boolean[] emptyFlags;

	public DeadDropDoubles(int nSpots) {
		this.nSpots            = nSpots;
		this.collectedDoubles  = new double[ nSpots];
		this.emptyFlags        = new boolean[nSpots];
		this.nFull             = 0;

		// mark all spots as empty
		for (int spotIndex = 0; spotIndex < nSpots; spotIndex++) {
			this.emptyFlags[spotIndex] = true;
			this.collectedDoubles[spotIndex] = magicEmptyValue;
		}
	}

	public synchronized double[] take() {

		// make a clean copy of what we want to take
		double[] listToSend = new double[nSpots];
				
		while ( nFull < nSpots ) {
			try {
				wait();
			} catch (InterruptedException e) {}
		}

		for (int spotIndex = 0; spotIndex < nSpots; spotIndex++) {
			this.emptyFlags[spotIndex] = true;
			listToSend[spotIndex] = collectedDoubles[spotIndex];
			this.collectedDoubles[spotIndex] = magicEmptyValue;
		}

		this.nFull = 0;

		// Notify anybody waiting that we're done

		notifyAll();

		return listToSend;
	}

	public synchronized boolean put(double value, int spot) {

		// check if the desired spot exists and is open
		if (spot >= nSpots) {
			System.out.println("tried to store in spot " + spot + " which does not exist; nSpots = " + nSpots);
			return false;
		}

		// wait 'til that spot is available
		while (!emptyFlags[spot]) {
			try {
				wait();
			} catch (InterruptedException f) {}
		}
		
		// Mark value as stored
		emptyFlags[spot] = false;
		// Store the value
		collectedDoubles[spot] = value;

		// reset the number full counter
		int tempNFull = 0;
		for (int spotIndex = 0; spotIndex < nSpots ; spotIndex++) {
			if (!this.emptyFlags[spotIndex]) {
				tempNFull++;
			}
		}
		this.nFull = tempNFull;

		// Notify anybody waiting that we're done

		notifyAll();

		// signify success
		return true;
	}


}
