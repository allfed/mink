package org.parallelBFGS;

import org.R2Useful.*;

//import java.util.Random;

public class ErrorRunner implements Runnable {
	private DeadDropObjectArray dropPoint;
	private int idInt;
//	private int[] intList;
	private MultiFormatFloat X;
	private MultiFormatFloat T;
	private MultiFormatMatrix weights;
	private MultiFormatMatrix biases;
	private int nHidden;
	private int nOptionsMinusOne;
	
//	private boolean canBeReset = false;

	public ErrorRunner(int idInt, DeadDropObjectArray dropPoint,
			MultiFormatFloat X, MultiFormatFloat T,
			MultiFormatMatrix weights, MultiFormatMatrix biases,
			int nHidden, int nOptionsMinusOne
			) {
		this.dropPoint = dropPoint;
		this.idInt = idInt;
		
		this.X = X;
		this.T = T;
		this.weights = weights;
		this.biases = biases;
		this.nHidden = nHidden;
		this.nOptionsMinusOne = nOptionsMinusOne;
		
//		this.canBeReset = false;
	}

//	public boolean getCanBeReset() {
//		return true;
//	}

	public boolean setDetails(int idInt, DeadDropObjectArray dropPoint,
			MultiFormatFloat X, MultiFormatFloat T,
			MultiFormatMatrix weights, MultiFormatMatrix biases,
			int nHidden, int nOptionsMinusOne) {

//		if (canBeReset) {
			this.dropPoint = dropPoint;
			this.idInt = idInt;

			this.X = X;
			this.T = T;
			this.weights = weights;
			this.biases = biases;
			this.nHidden = nHidden;
			this.nOptionsMinusOne = nOptionsMinusOne;

//			this.canBeReset = false;

			return true;
//		}
//		return false;
	}

	
	public void run() {

		double[] errorResults = null;
		try {
			errorResults = NNHelperMethods.findErrorSingleHidden(X, T,
					weights, biases, nHidden, nOptionsMinusOne);
		} catch (Exception e) {
			System.out.println("NNHelperMethods.findErrorSingleHidden threw an ugly exception in ErrorRunner; id = " + idInt);
			e.printStackTrace();
		}

			dropPoint.put(errorResults, idInt);
//			canBeReset = true;
	}
}


