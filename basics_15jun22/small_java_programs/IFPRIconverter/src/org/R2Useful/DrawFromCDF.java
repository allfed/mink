package org.R2Useful;

import java.io.*;
import java.util.Random;

public class DrawFromCDF {

	private double[] quantileFractions;
	private double[] quantileValues;
	private int nQuantiles = -1;
	
	private long randomSeed = 0;
	private Random randomNumberGenerator = new Random(randomSeed);
  
//	private boolean readyToGo = false;
	
	private double thisRandomDraw = -5;
	
	
	public DrawFromCDF(double[] fractions, double[] values) throws Exception {
		if (fractions.length == values.length) {
			nQuantiles = fractions.length;
		} else {
			System.out.println("arrays different lengths: fractions [" + fractions.length + "] != [" + values.length + "]");
			throw new Exception();
		}
		quantileFractions = new double[nQuantiles];
		quantileValues = new double[nQuantiles];
		
		for (int quantileIndex = 0; quantileIndex < nQuantiles; quantileIndex++) {
			quantileFractions[quantileIndex] = fractions[quantileIndex];
			quantileValues[quantileIndex] = values[quantileIndex];
		}
		
//		readyToGo = true;
	}
	
	public DrawFromCDF(String percentileValueFilename, String delimiter) throws IOException {
		String[] fileContents = FunTricks.readTextFileToArray(percentileValueFilename);

		nQuantiles = fileContents.length;
		
		quantileFractions = new double[nQuantiles];
		quantileValues = new double[nQuantiles];
		
		for (int quantileIndex = 0; quantileIndex < nQuantiles; quantileIndex++) {
//			System.out.println("  frac = " + fileContents[quantileIndex].split(delimiter)[0]);
//			System.out.println("   val = " + fileContents[quantileIndex].split(delimiter)[1]);
			quantileFractions[quantileIndex] = Double.parseDouble(fileContents[quantileIndex].split(delimiter)[0]) / 100.0;
			quantileValues[quantileIndex] = Double.parseDouble(fileContents[quantileIndex].split(delimiter)[1]);
		}
		
//		readyToGo = true;
	}

	public void setRandomSeed(long newSeed) {
		randomSeed = newSeed;
		
		randomNumberGenerator = new Random(randomSeed);
	}

	public void dumpcheck() {
		for (int quantileIndex = 0; quantileIndex < nQuantiles; quantileIndex++) {
			System.out.println(quantileIndex + "\t" + quantileFractions[quantileIndex] + "\t" + quantileValues[quantileIndex]);
		}
	}
	
	public double provideSingleDraw() {
		
		thisRandomDraw = randomNumberGenerator.nextDouble();

		// do brute force search for which places to interpolate between... this will accomodate irregular CDF definitions
		int bottomQuantileIndex = -1;
		for (int quantileIndex = 0; quantileIndex < nQuantiles; quantileIndex++) {
			if (quantileFractions[quantileIndex] > thisRandomDraw) {
				bottomQuantileIndex = quantileIndex - 1;
				break;
			}
		}

		// now we need to do the interpolation...
		double finalValue = (thisRandomDraw - quantileFractions[bottomQuantileIndex]) /
		(quantileFractions[bottomQuantileIndex + 1] - quantileFractions[bottomQuantileIndex]) *
		(quantileValues[bottomQuantileIndex + 1] - quantileValues[bottomQuantileIndex]) +
		quantileValues[bottomQuantileIndex];

		
		return finalValue;
	}
	
}