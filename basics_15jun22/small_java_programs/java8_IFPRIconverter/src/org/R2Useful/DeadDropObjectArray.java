package org.R2Useful;

public class DeadDropObjectArray {

  private static final int magicEmptyValue = -8888;
  private int nSpots = -1;
  private int nFull = 0;
  // Message sent from producer to consumer.
  private Object[] collectedObjects;
  // True if consumer should wait for producer to send message, false
  // if producer should wait for consumer to retrieve message.
  private boolean[] emptyFlags;

  public DeadDropObjectArray(int nSpots) {
    this.nSpots = nSpots;
    this.collectedObjects = new Object[nSpots];
    this.emptyFlags = new boolean[nSpots];
    this.nFull = 0;

    // mark all spots as empty
    for (int spotIndex = 0; spotIndex < nSpots; spotIndex++) {
      this.emptyFlags[spotIndex] = true;
      this.collectedObjects[spotIndex] = magicEmptyValue;
    }
  }

  public synchronized Object[] take() {

    Object[] listToSend = new Object[nSpots];

    while (nFull < nSpots) {
      try {
        wait();
      } catch (InterruptedException e) {
      }
    }
    // mark all spots as empty

    for (int spotIndex = 0; spotIndex < nSpots; spotIndex++) {
      this.emptyFlags[spotIndex] = true;
      listToSend[spotIndex] = collectedObjects[spotIndex];
      this.collectedObjects[spotIndex] = magicEmptyValue;
    }

    this.nFull = 0;

    // Notify producer that status has changed.

    notifyAll();

    //		return collected Objects
    return listToSend;
  }

  public synchronized boolean put(Object value, int spot) {

    // check if the desired spot exists and is open
    if (spot >= nSpots) {
      System.out.println(
          "tried to store in spot " + spot + " which does not exist; nSpots = " + nSpots);
      return false;
    }

    while (!emptyFlags[spot]) {
      try {
        wait();
      } catch (InterruptedException f) {
      }
    }

    // Mark value as stored
    emptyFlags[spot] = false;

    // Store the value
    collectedObjects[spot] = value;

    int tempNFull = 0;
    for (int spotIndex = 0; spotIndex < nSpots; spotIndex++) {
      if (!this.emptyFlags[spotIndex]) {
        tempNFull++;
      }
    }
    this.nFull = tempNFull;

    notifyAll();

    // signify success
    return true;
  }
}
