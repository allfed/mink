package org.ifpri_converter;

public class StringToUniqueCode {

	private int nStored  = 0;
	private int nCapacity = 0;
	
	private int nBumpInterval = 100;
	
	private String[] dictionaryList = null;

	
	public StringToUniqueCode(int initialCapacity) {
		nStored = 0;
		nCapacity = initialCapacity;
		
		dictionaryList = new String[nCapacity];
	}
	
	public StringToUniqueCode() {
		nStored = 0;
		nCapacity = nBumpInterval;
		dictionaryList = new String [nCapacity];
	}
	
	public int getCode(String stringToStore) {
		
		boolean haveSeenThisStringBefore = false;
		int indexFromBefore = -1;
		for (int dictIndex = 0; dictIndex < nStored; dictIndex++) {
			if (dictionaryList[dictIndex].equals(stringToStore)) {
				haveSeenThisStringBefore = true;
				indexFromBefore = dictIndex;
				break;
			}
		}
		
		if (haveSeenThisStringBefore) {
			return indexFromBefore;
		}
		
		// if we get here, it means that we have a new string to deal with...
		// check if we need to increase the dictionary capacity
		if (nStored == nCapacity) {
			nCapacity += nBumpInterval;
			
			String[] newDictionaryList = new String[nCapacity];
			
			// copy them over
			for (int dictIndex = 0; dictIndex < nStored; dictIndex++) {
				newDictionaryList[dictIndex] = dictionaryList[dictIndex];
			}
			
			// reset the copy...
			dictionaryList = newDictionaryList;
		}
		
		// store the new one...
		int returnValue = nStored;
		
		dictionaryList[returnValue] = stringToStore;
		
		// bump up the counter
		nStored++;
		
		return returnValue;
	}
	
	
	
	
	
	
	public int getNStored() {
		return nStored;
	}

	public int getNCapacity() {
		return nCapacity;
	}

	public void setNCapacity(int capacity) {
		if (capacity > nCapacity) {
			nCapacity = capacity;
			
			String[] newDictionaryList = new String[nCapacity];
			
			// copy them over
			for (int dictIndex = 0; dictIndex < nStored; dictIndex++) {
				newDictionaryList[dictIndex] = dictionaryList[dictIndex];
			}
			
			// reset the copy...
			dictionaryList = newDictionaryList;
		}
	}

	public int getNBumpInterval() {
		return nBumpInterval;
	}

	public void setNBumpInterval(int bumpInterval) {
		nBumpInterval = bumpInterval;
	}

	public String[] getDictionaryList() {
		String[] returnList = new String[nStored];
		for (int dictIndex = 0; dictIndex < nStored; dictIndex++) {
			returnList[dictIndex] =
				dictionaryList[dictIndex];
		}
		return returnList;
	}

	public void setDictionaryList(String[] dictionaryList) {
		this.dictionaryList = dictionaryList;
		nCapacity = dictionaryList.length;
		nStored = dictionaryList.length;
	}
	
	
	
	
}
