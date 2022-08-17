package org.parallelBFGS;

import org.R2Useful.*;
//import java.util.Random;

public class GradientRunnerWeighted implements Runnable {
	private DeadDropObjectArray dropPoint;
	private int idInt;
//	private int[] intList;
	private MultiFormatFloat X;
	private MultiFormatFloat T;
	private MultiFormatMatrix weights;
	private MultiFormatMatrix biases;
	private int nHidden;
	private int nOptionsMinusOne;
	
	private boolean canBeReset = false;
	
	public GradientRunnerWeighted(int idInt, DeadDropObjectArray dropPoint,
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
		
		this.canBeReset = false;
	}

	public boolean getCanBeReset() {
		return canBeReset;
	}

	public boolean setDetails(int idInt, DeadDropObjectArray dropPoint,
			MultiFormatFloat X, MultiFormatFloat T,
			MultiFormatMatrix weights, MultiFormatMatrix biases,
			int nHidden, int nOptionsMinusOne) {
		
		if (canBeReset) {
			this.dropPoint = dropPoint;
			this.idInt = idInt;

			this.X = X;
			this.T = T;
			this.weights = weights;
			this.biases = biases;
			this.nHidden = nHidden;
			this.nOptionsMinusOne = nOptionsMinusOne;
		
			this.canBeReset = false;
			
			return true;
		}
		return false;
		
	}
	
	public void run() {

		Object[] gradientResults = null;
//		System.out.println("idInt = " + idInt + "; Xrows = " + X.getDimensions()[0] + "; Xcols = " + X.getDimensions()[1] + "; Tcols = " + T.getDimensions()[1]);
		try {
			gradientResults = NNHelperMethods.findSingleHiddenErrorGradientWeighted(X, T,
					weights, biases, nHidden, nOptionsMinusOne);
		} catch (Exception e) {
			System.out.println(this.getClass().getName()+ "had a problem withNNHelperMethodsWeighted.findErrorSingleHiddenGradientWeighted threw an ugly exception; id = " + idInt);
			e.printStackTrace();
		}
			dropPoint.put(gradientResults, idInt);
			this.canBeReset = true;
	}
}


