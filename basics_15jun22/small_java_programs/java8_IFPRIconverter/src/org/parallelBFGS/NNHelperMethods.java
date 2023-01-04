package org.parallelBFGS;

import java.io.BufferedReader;
import java.util.Date;
import java.io.FileReader;

import org.R2Useful.*;

//import org.parallelBFGS.MultiFormatMatrix;
//import org.parallelBFGS.MultiFormatFloat;
//import org.parallelBFGS.MatrixOperations;

public class NNHelperMethods {

	public static final int  ONE_TIME_PARAMETERS_PORT = 7007;

	public static final int       WEIGHTS_BIASES_PORT = 7008;
	public static final int             GRADIENT_PORT = 7009;

	public static final int       TRAINING_ERROR_PORT = 7010;
	public static final int     VALIDATION_ERROR_PORT = 7011;
	public static final int           TRAINING_N_PORT = 7012;
	public static final int         VALIDATION_N_PORT = 7013;
	public static final int   TRAINING_N_CORRECT_PORT = 7014;
	public static final int VALIDATION_N_CORRECT_PORT = 7015;

	public static final int      CONTINUE_SEARCH_PORT = 7016;
	public static final int       LINE_SEARCH_WB_PORT = 7017;
	public static final int    LINE_SEARCH_ERROR_PORT = 7018;

	public static final int       CONTINUE_EPOCH_PORT = 7019;
	public static final int        DO_VALIDATION_PORT = 7020;



	public static Object[] readInitializationFile(String filename) throws Exception {


		// filter the input stream, buffers characters for efficiency
		BufferedReader InitReader = new BufferedReader(new FileReader(filename));

		// read the first line
		int nExplanatoryVariables = Integer.parseInt(InitReader.readLine());
		int nHidden               = Integer.parseInt(InitReader.readLine());
		int nOptionsMinusOne      = Integer.parseInt(InitReader.readLine());
		long nElementsThreshold   = Long.parseLong(InitReader.readLine());
		double FractionInTraining = Double.parseDouble(InitReader.readLine());
		double LearningRate = Double.parseDouble(InitReader.readLine());

		String WeightSeedBaseName = InitReader.readLine();
		String BiasSeedBaseName = InitReader.readLine();

		int nEpochs = Integer.parseInt(InitReader.readLine());

		int CheckNumber = Integer.parseInt(InitReader.readLine());

		String WeightBaseName = InitReader.readLine();
		String BiasBaseName = InitReader.readLine();
		String GradientBaseName = InitReader.readLine();
		String HessianBaseName = InitReader.readLine();
		String ReportBaseName = InitReader.readLine();
		String Delimiter = InitReader.readLine();

		long aRandomSeed = Long.parseLong(InitReader.readLine());

		InitReader.close();

		Object[] returnBundle = new Object[] {
				nExplanatoryVariables,
				nHidden,
				nOptionsMinusOne,
				nElementsThreshold,
				FractionInTraining,
				LearningRate,
				WeightSeedBaseName,
				BiasSeedBaseName,
				nEpochs,
				CheckNumber,
				WeightBaseName,
				BiasBaseName,
				GradientBaseName,
				HessianBaseName,
				ReportBaseName,
				Delimiter,
				aRandomSeed,
		};

		return returnBundle;
	}



	public static double findErrorSingleHiddenSlowALTEXP(
			MultiFormatFloat explanatoryVariables,
			MultiFormatFloat targetValues,
			MultiFormatMatrix weights,
			MultiFormatMatrix biases,
			int nHidden,
			int nOutputsMinusOne
	) throws Exception {

		// a magic number
		double minSumAllowed = -MatrixOperations.altLn(Double.MAX_VALUE); // This actually needs to be what it is...

		// constants derived from the data...
//		int nNeurons = (int)biases.getDimensions()[0];
		long nExamples = explanatoryVariables.getDimensions()[0];
		int nInputs = (int)explanatoryVariables.getDimensions()[1];
		int nWeights = (int) weights.getDimensions()[0];
		int nOutputs = nOutputsMinusOne + 1;

		// declare useful variables
//		int appropriateWeightIndexToOutput = -2;
//		double errorValue = Double.NaN;
//		double internalSum = Double.NaN;
		int targetThisExample = -3;
		double[] xDouble = new double[nInputs];
//		double expedThing = Double.NaN;
		double[] expedThings = new double[nHidden];
//		double thisError = Double.NaN;
		double[] internalSumsForHidden = new double[nHidden];
		long[] getBias = new long[2];
		long[] getWeight = new long[2];
		long[] getX = new long[2];
		long[] getY = new long[2];


		double[] outputSums = new double[nOutputs];
		double[] outputExped = new double[nOutputs];
		double sumOutputsExped = Double.NaN;
//		double[] outputProbabilities = new double[nOutputs];

		// copy the weights and biases to a double array...
		double[] weightsDouble = new double[nWeights];
		double[] biasesDouble = new double[(int)biases.getDimensions()[0]];
		for (int weightIndex = 0; weightIndex < nWeights; weightIndex++) {
			weightsDouble[weightIndex] = weights.getValue(weightIndex,0);
		}
		for (int biasIndex = 0; biasIndex < biasesDouble.length; biasIndex++) {
			biasesDouble[biasIndex] = biases.getValue(biasIndex,0);
		}

		int nInputsPlusNHidden  = nInputs  + nHidden;
		int nInputsTimesNHidden = nInputs  * nHidden;


		double totalError = 0.0; // initialize the total error
		getBias[1] = 0;
		getWeight[1] = 0;
		getY[1] = 0;
		outputExped[nOutputsMinusOne] = 1.0;
		for (long exampleIndex = 0; exampleIndex < nExamples ; exampleIndex++) {
			getX[0] = exampleIndex;
			getY[0] = exampleIndex;
			targetThisExample = (int)targetValues.getValue(getY);
			for (int xIndex = 0; xIndex < nInputs; xIndex++) {
				getX[1] = xIndex;
				xDouble[xIndex] = explanatoryVariables.getValue(getX);
			}

			// initialize output sums with bias value (last bias/neuron is the output)
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne ; outputIndex++) {
//				getBias[0] = nInputs + nHidden + outputIndex;
//				outputSums[outputIndex] = biases.getValue(getBias);
				outputSums[outputIndex] = biasesDouble[nInputsPlusNHidden + outputIndex];
			}
			outputSums[nOutputsMinusOne] = 0.0;

			// supposing there are hidden neurons
			for (int hiddenIndex = 0; hiddenIndex < nHidden ; hiddenIndex++) {
				// initialize with bias value
//				getBias[0] = nInputs + hiddenIndex;
//				internalSumsForHidden[hiddenIndex] = biases.getValue(getBias);
				internalSumsForHidden[hiddenIndex] = biasesDouble[nInputs + hiddenIndex];
			}
			for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
				for (int hiddenIndex = 0; hiddenIndex < nHidden ; hiddenIndex++) {
//					getWeight[0] = hiddenIndex + nHidden * inputIndex;
//					System.out.println("iI = " + inputIndex + " ; hI = " + hiddenIndex + " ; wI = " + getWeight[0]);
//					getX[0] = exampleIndex;
//					getX[1] = inputIndex;
//					appropriateWeightIndex = hiddenIndex  + nHidden * inputIndex;
//					internalSumsForHidden[hiddenIndex] += weights.getValue(getWeight) *	explanatoryVariables.getValue(getX);
					internalSumsForHidden[hiddenIndex] += weightsDouble[hiddenIndex + nHidden * inputIndex] *	xDouble[inputIndex];


//					internalSum = internalSumsForHidden[hiddenIndex];
					if (internalSumsForHidden[hiddenIndex] < minSumAllowed) {
						internalSumsForHidden[hiddenIndex] = minSumAllowed;
						expedThings[hiddenIndex] = Double.MAX_VALUE;
					} else {
//						expedThings[hiddenIndex] = java.lang.Math.exp(-internalSumsForHidden[hiddenIndex]);
						expedThings[hiddenIndex] = MatrixOperations.altExp(-internalSumsForHidden[hiddenIndex]);
					}
				}
			}

			// if there aren't, just connect inputs directly to outputs
			if (nHidden == 0) {
				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					//getWeight[0] = inputIndex;
//					getX[0] = exampleIndex;
//					getX[1] = inputIndex;
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne ; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the output)
//						getWeight[0] = inputIndex * nOutputsMinusOne + outputIndex;
//						outputSums[outputIndex] += weights.getValue(getWeight) / (1.0 + expedThing); 
//						outputSums[outputIndex] += weights.getValue(getWeight) * explanatoryVariables.getValue(getX); 
						outputSums[outputIndex] += weightsDouble[inputIndex * nOutputsMinusOne + outputIndex] * xDouble[inputIndex]; 
					}
				}
			} else {
				// build up the y output values
//				appropriateWeightIndexToOutput = nHidden * nInputs + hiddenIndex;
				for (int hiddenIndex = 0; hiddenIndex < nHidden ; hiddenIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne ; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the output)
						//getWeight[0] = nHidden * nInputs + hiddenIndex + outputIndex * nHidden;
						getWeight[0] = nHidden * nInputs + nOutputsMinusOne * hiddenIndex + outputIndex;
//						outputSums[outputIndex] += weights.getValue(getWeight) / (1.0 + expedThings[hiddenIndex]); 
						outputSums[outputIndex] += weightsDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex + outputIndex] / (1.0 + expedThings[hiddenIndex]); 
					}
				}

			}

			// find the probabilities
			sumOutputsExped = 1.0; // Beware the MAGIC INITIALIZATION!!! this is for the standardized output
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne ; outputIndex++) {
				// initialize output value with bias value (last bias/neuron is the output)
//				outputExped[outputIndex] = Math.exp(outputSums[outputIndex]);
				outputExped[outputIndex] = MatrixOperations.altExp(outputSums[outputIndex]);
				sumOutputsExped += outputExped[outputIndex]; 
			}

			// this could be a candidate for a shortcut, but first i'd have to look it up and
			// second, we might want this part to be reasonable accurate...
//			thisError = - Math.log(outputExped[(int)targetValues.getValue(getY)] / sumOutputsExped);

//			thisError = - Math.log(outputExped[targetThisExample] / sumOutputsExped);
//			thisError = - altLn(outputExped[targetThisExample] / sumOutputsExped);

//			totalError -= Math.log(outputExped[targetThisExample] / sumOutputsExped);
			totalError -= MatrixOperations.altLn(outputExped[targetThisExample] / sumOutputsExped);

			// my hunch (n = 1 datapoints) is that the approximation tends to send us off on
			// wild goose chases sometimes which can lead to a few very time consuming line searches.
			// this should be revisited, but for the moment, i'm gonna leave it as is.
//			totalError -= altLn(outputExped[targetThisExample] / sumOutputsExped);

		} // end example index

		return totalError;
	}


	public static double findErrorSingleHiddenSlowJAVAEXP(
			MultiFormatFloat explanatoryVariables,
			MultiFormatFloat targetValues,
			MultiFormatMatrix weights,
			MultiFormatMatrix biases,
			int nHidden,
			int nOutputsMinusOne
	) throws Exception {

		// a magic number
		double minSumAllowed = -Math.log(Double.MAX_VALUE); // This actually needs to be what it is...

		// constants derived from the data...
//		int nNeurons = (int)biases.getDimensions()[0];
		long nExamples = explanatoryVariables.getDimensions()[0];
		int nInputs = (int)explanatoryVariables.getDimensions()[1];
		int nWeights = (int) weights.getDimensions()[0];
		int nOutputs = nOutputsMinusOne + 1;

		// declare useful variables
//		int appropriateWeightIndexToOutput = -2;
//		double errorValue = Double.NaN;
//		double internalSum = Double.NaN;
		int targetThisExample = -3;
		double[] xDouble = new double[nInputs];
//		double expedThing = Double.NaN;
		double[] expedThings = new double[nHidden];
//		double thisError = Double.NaN;
		double[] internalSumsForHidden = new double[nHidden];
		long[] getBias = new long[2];
		long[] getWeight = new long[2];
		long[] getX = new long[2];
		long[] getY = new long[2];


		double[] outputSums = new double[nOutputs];
		double[] outputExped = new double[nOutputs];
		double sumOutputsExped = Double.NaN;
//		double[] outputProbabilities = new double[nOutputs];

		// copy the weights and biases to a double array...
		double[] weightsDouble = new double[nWeights];
		double[] biasesDouble = new double[(int)biases.getDimensions()[0]];
		for (int weightIndex = 0; weightIndex < nWeights; weightIndex++) {
			weightsDouble[weightIndex] = weights.getValue(weightIndex,0);
		}
		for (int biasIndex = 0; biasIndex < biasesDouble.length; biasIndex++) {
			biasesDouble[biasIndex] = biases.getValue(biasIndex,0);
		}

		int nInputsPlusNHidden  = nInputs  + nHidden;
		int nInputsTimesNHidden = nInputs  * nHidden;


		double totalError = 0.0; // initialize the total error
		getBias[1] = 0;
		getWeight[1] = 0;
		getY[1] = 0;
		outputExped[nOutputsMinusOne] = 1.0;
		for (long exampleIndex = 0; exampleIndex < nExamples ; exampleIndex++) {
			getX[0] = exampleIndex;
			getY[0] = exampleIndex;
			targetThisExample = (int)targetValues.getValue(getY);
			for (int xIndex = 0; xIndex < nInputs; xIndex++) {
				getX[1] = xIndex;
				xDouble[xIndex] = explanatoryVariables.getValue(getX);
			}

			// initialize output sums with bias value (last bias/neuron is the output)
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne ; outputIndex++) {
//				getBias[0] = nInputs + nHidden + outputIndex;
//				outputSums[outputIndex] = biases.getValue(getBias);
				outputSums[outputIndex] = biasesDouble[nInputsPlusNHidden + outputIndex];
			}
			outputSums[nOutputsMinusOne] = 0.0;

			// supposing there are hidden neurons
			for (int hiddenIndex = 0; hiddenIndex < nHidden ; hiddenIndex++) {
				// initialize with bias value
//				getBias[0] = nInputs + hiddenIndex;
//				internalSumsForHidden[hiddenIndex] = biases.getValue(getBias);
				internalSumsForHidden[hiddenIndex] = biasesDouble[nInputs + hiddenIndex];
			}
			for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
				for (int hiddenIndex = 0; hiddenIndex < nHidden ; hiddenIndex++) {
//					getWeight[0] = hiddenIndex + nHidden * inputIndex;
//					System.out.println("iI = " + inputIndex + " ; hI = " + hiddenIndex + " ; wI = " + getWeight[0]);
//					getX[0] = exampleIndex;
//					getX[1] = inputIndex;
//					appropriateWeightIndex = hiddenIndex  + nHidden * inputIndex;
//					internalSumsForHidden[hiddenIndex] += weights.getValue(getWeight) *	explanatoryVariables.getValue(getX);
					internalSumsForHidden[hiddenIndex] += weightsDouble[hiddenIndex + nHidden * inputIndex] *	xDouble[inputIndex];


//					internalSum = internalSumsForHidden[hiddenIndex];
					if (internalSumsForHidden[hiddenIndex] < minSumAllowed) {
						internalSumsForHidden[hiddenIndex] = minSumAllowed;
						expedThings[hiddenIndex] = Double.MAX_VALUE;
					} else {
						expedThings[hiddenIndex] = java.lang.Math.exp(-internalSumsForHidden[hiddenIndex]);
//						expedThings[hiddenIndex] = altExp(-internalSumsForHidden[hiddenIndex]);
					}
				}
			}

			// if there aren't, just connect inputs directly to outputs
			if (nHidden == 0) {
				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					//getWeight[0] = inputIndex;
//					getX[0] = exampleIndex;
//					getX[1] = inputIndex;
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne ; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the output)
//						getWeight[0] = inputIndex * nOutputsMinusOne + outputIndex;
//						outputSums[outputIndex] += weights.getValue(getWeight) / (1.0 + expedThing); 
//						outputSums[outputIndex] += weights.getValue(getWeight) * explanatoryVariables.getValue(getX); 
						outputSums[outputIndex] += weightsDouble[inputIndex * nOutputsMinusOne + outputIndex] * xDouble[inputIndex]; 
					}
				}
			} else {
				// build up the y output values
//				appropriateWeightIndexToOutput = nHidden * nInputs + hiddenIndex;
				for (int hiddenIndex = 0; hiddenIndex < nHidden ; hiddenIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne ; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the output)
						//getWeight[0] = nHidden * nInputs + hiddenIndex + outputIndex * nHidden;
						getWeight[0] = nHidden * nInputs + nOutputsMinusOne * hiddenIndex + outputIndex;
//						outputSums[outputIndex] += weights.getValue(getWeight) / (1.0 + expedThings[hiddenIndex]); 
						outputSums[outputIndex] += weightsDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex + outputIndex] / (1.0 + expedThings[hiddenIndex]); 
					}
				}

			}

			// find the probabilities
			sumOutputsExped = 1.0; // Beware the MAGIC INITIALIZATION!!! this is for the standardized output
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne ; outputIndex++) {
				// initialize output value with bias value (last bias/neuron is the output)
				outputExped[outputIndex] = Math.exp(outputSums[outputIndex]);
//				outputExped[outputIndex] = altExp(outputSums[outputIndex]);
				sumOutputsExped += outputExped[outputIndex]; 
			}

			// this could be a candidate for a shortcut, but first i'd have to look it up and
			// second, we might want this part to be reasonable accurate...
//			thisError = - Math.log(outputExped[(int)targetValues.getValue(getY)] / sumOutputsExped);

//			thisError = - Math.log(outputExped[targetThisExample] / sumOutputsExped);
//			thisError = - altLn(outputExped[targetThisExample] / sumOutputsExped);

			totalError -= Math.log(outputExped[targetThisExample] / sumOutputsExped);

			// my hunch (n = 1 datapoints) is that the approximation tends to send us off on
			// wild goose chases sometimes which can lead to a few very time consuming line searches.
			// this should be revisited, but for the moment, i'm gonna leave it as is.
//			totalError -= altLn(outputExped[targetThisExample] / sumOutputsExped);

		} // end example index

		return totalError;
	}


	public static Object[] findSingleHiddenErrorGradientOriginal(MultiFormatFloat explanatoryVariables,
			MultiFormatFloat targetValues, MultiFormatMatrix weights,
			MultiFormatMatrix biases, int nHidden, int nOutputsMinusOne)
	throws Exception {


		// a magic number
//		double minSumAllowed = -Math.log(Double.MAX_VALUE); // This actually needs
		// to be what it is...

		// constants derived from the data...
		// int nNeurons = (int)biases.getDimensions()[0];
		long nExamples = explanatoryVariables.getDimensions()[0];
		int nInputs = (int) explanatoryVariables.getDimensions()[1];
		int nWeights = (int) weights.getDimensions()[0];

		int nOutputs = nOutputsMinusOne + 1;

		int targetThisExample = -1;

		// declare useful variables
		// double expedThing = Double.NaN;

		long[] getBias = new long[2];
		long[] getWeight = new long[2];
		long[] getX = new long[2];
		long[] getY = new long[2];


		double[] xDouble = new double[nInputs];
		double[] expedThings = new double[nHidden];
		double[] internalSumsForHidden = new double[nHidden];
		double[] lambdaHidden = new double[nHidden];

		double[] outputSums = new double[nOutputs];
		double[] outputExped = new double[nOutputs];
		double sumOutputsExped = Double.NaN;
		double[] outputProbabilities = new double[nOutputs];

		double[] hiddenDeltas = new double[nHidden];
		double[] outputDeltas = new double[nOutputs];

		int highestOutputIndex = -2;
		double highestOutputProbability = Double.NEGATIVE_INFINITY;

		// copy the weights and biases to a double array...
		double[] weightsDouble = new double[nWeights];
		double[] biasesDouble = new double[(int)biases.getDimensions()[0]];
		for (int weightIndex = 0; weightIndex < nWeights; weightIndex++) {
			weightsDouble[weightIndex] = weights.getValue(weightIndex,0);
		}
		for (int biasIndex = 0; biasIndex < biasesDouble.length; biasIndex++) {
			biasesDouble[biasIndex] = biases.getValue(biasIndex,0);
		}


		double[] gradientDouble = new double[nWeights + (int)biases.getDimensions()[0] - nInputs];
		MultiFormatMatrix stackedGradient = new MultiFormatMatrix(1,
				new long[] {(nWeights + biases.getDimensions()[0] - nInputs),1});


		double totalError = 0.0; // initialize the total error
		long nCorrect = 0; // initialize the counter for total hit correctly
		getBias[1] = 0;
		getWeight[1] = 0;
		getY[1] = 0;
//		gradientIndices[1] = 0;
		outputExped[nOutputsMinusOne] = 1.0;
		int nInputsPlusNHidden  = nInputs  + nHidden;
		int nInputsTimesNHidden = nInputs  * nHidden;
		int nWeightsPlusNHidden = nWeights + nHidden;
		for (long exampleIndex = 0; exampleIndex < nExamples; exampleIndex++) {
			getX[0] = exampleIndex;
			getY[0] = exampleIndex;
			targetThisExample = (int) targetValues.getValue(getY);
			for (int xIndex = 0; xIndex < nInputs; xIndex++) {
				getX[1] = xIndex;
				xDouble[xIndex] = explanatoryVariables.getValue(getX);
			}

			hiddenDeltas = new double[nHidden];
			outputDeltas = new double[nOutputs];

			// initialize output sums with bias value (last bias/neuron is the output)
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				outputSums[outputIndex] = biasesDouble[nInputsPlusNHidden + outputIndex];
			}
			outputSums[nOutputsMinusOne] = 0.0;

			// supposing there are hidden neurons
			for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
				// initialize with bias value
				internalSumsForHidden[hiddenIndex] = biasesDouble[nInputs + hiddenIndex];

				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					internalSumsForHidden[hiddenIndex] += weightsDouble[hiddenIndex + nHidden * inputIndex]
					                                                    * xDouble[inputIndex];

				}

				expedThings[hiddenIndex] = java.lang.Math
				.exp(-internalSumsForHidden[hiddenIndex]);
				lambdaHidden[hiddenIndex] = 1 / (1 + expedThings[hiddenIndex]);


			}

			// if there aren't, just connect inputs directly to outputs
			if (nHidden == 0) {
				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[inputIndex * nOutputsMinusOne + outputIndex]
						                                         * xDouble[inputIndex];
					}
				}
			} else {
				// build up the y output values
				for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex + outputIndex]
						                                         / (1.0 + expedThings[hiddenIndex]);
					}
				}

			}




			// find the probabilities
			sumOutputsExped = 1.0; // Beware the MAGIC INITIALIZATION!!! this is for
			// the standardized output
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				// initialize output value with bias value (last bias/neuron is the
				// output)
				outputExped[outputIndex] = Math.exp(outputSums[outputIndex]);
				sumOutputsExped += outputExped[outputIndex];






			}

			// initialize the standardized output
//			outputProbabilities[nOutputsMinusOne] = 1.0 / sumOutputsExped;
			highestOutputIndex = -1;
			highestOutputProbability = Double.NEGATIVE_INFINITY;
			for (int outputIndex = 0; outputIndex < nOutputs; outputIndex++) {
				outputProbabilities[outputIndex] = outputExped[outputIndex] / sumOutputsExped;
				if (outputProbabilities[outputIndex] > highestOutputProbability) {
					highestOutputIndex = outputIndex;
					highestOutputProbability = outputProbabilities[outputIndex];
				}
			}

			// check if the highest probability matches the target
			if (highestOutputIndex == targetThisExample) {
				nCorrect++;
			}

			// //////////////////
			// delta attempts //
			// //////////////////

			// final deltas; with the precision problem
			for (int outputIndex = 0; outputIndex < nOutputs; outputIndex++) {
				if (targetThisExample == outputIndex) {
					// This is the realized outcome...

					outputDeltas[outputIndex] = outputProbabilities[outputIndex] - 1.0;
					totalError -= Math.log(outputProbabilities[outputIndex]);

				} else { // This outcome was not realized...

					outputDeltas[outputIndex] = outputProbabilities[outputIndex];
				}
			} // for outputIndex


			// hidden deltas; with the precision problem
			for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
				//delta_this = lambda_this * (1 - lambda_this) * sum_next ( delta_next * w_this/next )
				// build up the part before the multiplier
				for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
					hiddenDeltas[hiddenIndex] += outputDeltas[outputIndex] * weightsDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex + outputIndex];
				}

				// multiply by the derivative
				hiddenDeltas[hiddenIndex] *= lambdaHidden[hiddenIndex] * (1.0 - lambdaHidden[hiddenIndex]); 

			}


			// accumulate the gradient
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				// gradient for bias for neuron j = delta_j ; i.e., activation_bias == 1
				gradientDouble[nWeightsPlusNHidden + outputIndex] += outputDeltas[outputIndex];
			}

			for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
				// biases for hidden layer
				// gradient for bias for neuron j = delta_j ; i.e., activation_bias == 1
				gradientDouble[nWeights + hiddenIndex] += hiddenDeltas[hiddenIndex];

				// and the weights
				// gradient for weight from i to j = delta_j * activation_i
				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					gradientDouble[hiddenIndex + nHidden * inputIndex] += hiddenDeltas[hiddenIndex] * xDouble[inputIndex];
				}
				for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
					gradientDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex	+ outputIndex] +=
						outputDeltas[outputIndex] * lambdaHidden[hiddenIndex];
				}
			}


			if (nHidden == 0) {
				//delta_this = lambda_this * (1 - lambda_this) * sum_next ( delta_next * w_this/next )
				for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
					for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
						gradientDouble[inputIndex * nOutputsMinusOne + outputIndex] += outputDeltas[outputIndex] * xDouble[inputIndex];
					}
				}
			}

		} // end example index

		Object[] returnObject = new Object[3];
		returnObject[0] = new Double(totalError);
		returnObject[1] = new Double(nCorrect);

		for (int gradientIndex = 0; gradientIndex < gradientDouble.length; gradientIndex++) {
			stackedGradient.setValue(gradientIndex,0, gradientDouble[gradientIndex]);
		}
		returnObject[2] = stackedGradient;

		return returnObject;
	}

	public static Object[] findSingleHiddenErrorGradient(MultiFormatFloat explanatoryVariables,
			MultiFormatFloat targetValues, MultiFormatMatrix weights,
			MultiFormatMatrix biases, int nHidden, int nOutputsMinusOne)
	throws Exception {


		// constants derived from the data...
		// int nNeurons = (int)biases.getDimensions()[0];
		long nExamples = explanatoryVariables.getDimensions()[0];
		int  nInputs   = (int) explanatoryVariables.getDimensions()[1];
		int  nWeights  = (int) weights.getDimensions()[0];

		int nOutputs = nOutputsMinusOne + 1;

		int targetThisExample = -1;

		// declare useful variables
		// double expedThing = Double.NaN;

		long[] getBias = new long[2];
		long[] getWeight = new long[2];
		long[] getX = new long[2];
		long[] getY = new long[2];


		double[] xDouble = new double[nInputs];
		double[] expedThings = new double[nHidden];
		double[] internalSumsForHidden = new double[nHidden];
		double[] lambdaHidden = new double[nHidden];
		double[] lambdaHiddenComplements = new double[nHidden];

		double[] outputSums = new double[nOutputs];
		double[] outputExped = new double[nOutputs];
//		double sumOutputsExped = Double.NaN;
		double[] outputProbabilities = new double[nOutputs];

		double[] centeredOutputSums = new double[nOutputs];
		double[] expedCenteredOutputSums = new double[nOutputs];
//		double[] outputExpedSums = new double[nOutputs];

//		double[] outputProbabilitiesAlt = new double[nOutputs];

		double[] hiddenDeltas = new double[nHidden];
		double[] outputDeltas = new double[nOutputs];

//		int highestOutputIndex = -2;
//		double highestOutputProbability = Double.NEGATIVE_INFINITY;

		double emm = Double.NaN;
		int emmIndex = Integer.MIN_VALUE;
		double capitalCue = Double.NEGATIVE_INFINITY;
		double finalTemp = Double.NaN;
		double alternateSumTemp = Double.POSITIVE_INFINITY;

		double finalStandardized = Double.NaN;
		double alternateStandardized = -1232145.231553;

//		double toleranceBetweenPs = 0.001;

//		boolean mismatch = false;



		// copy the weights and biases to a double array...
		double[] weightsDouble = new double[nWeights];
		double[] biasesDouble = new double[(int)biases.getDimensions()[0]];
		for (int weightIndex = 0; weightIndex < nWeights; weightIndex++) {
			weightsDouble[weightIndex] = weights.getValue(weightIndex,0);
		}
		for (int biasIndex = 0; biasIndex < biasesDouble.length; biasIndex++) {
			biasesDouble[biasIndex] = biases.getValue(biasIndex,0);
		}


		double[] gradientDouble = new double[nWeights + (int)biases.getDimensions()[0] - nInputs];
		MultiFormatMatrix stackedGradient = new MultiFormatMatrix(1,
				new long[] {(nWeights + biases.getDimensions()[0] - nInputs),1});


		double totalError = 0.0; // initialize the total error
		long nCorrect = 0; // initialize the counter for total hit correctly
		getBias[1] = 0;
		getWeight[1] = 0;
		getY[1] = 0;
//		gradientIndices[1] = 0;
		outputExped[nOutputsMinusOne] = 1.0;
		int nInputsPlusNHidden  = nInputs  + nHidden;
		int nInputsTimesNHidden = nInputs  * nHidden;
		int nWeightsPlusNHidden = nWeights + nHidden;
		for (long exampleIndex = 0; exampleIndex < nExamples; exampleIndex++) {		
			getX[0] = exampleIndex;
			getY[0] = exampleIndex;
			targetThisExample = (int) targetValues.getValue(getY);
			for (int xIndex = 0; xIndex < nInputs; xIndex++) {
				getX[1] = xIndex;
				xDouble[xIndex] = explanatoryVariables.getValue(getX);
			}

			hiddenDeltas = new double[nHidden];
			outputDeltas = new double[nOutputs];

			// initialize output sums with bias value (last bias/neuron is the output)
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
//				System.out.println("   ++> nI = " + nInputs + "; nHidden = " + nHidden + "; nO = " + nOutputs);
//				System.out.println("   --> oI = " + outputIndex + "; nIPNH = " + nInputsPlusNHidden + "; oI = " + outputIndex + "; overall = " + (nInputsPlusNHidden + outputIndex) );
//				System.out.println("   --> oS.length = " + outputSums.length + "; bD.length = " + biasesDouble.length);
				outputSums[outputIndex] = biasesDouble[nInputsPlusNHidden + outputIndex];
			}
			outputSums[nOutputsMinusOne] = 0.0;

			// supposing there are hidden neurons
			for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
				// initialize with bias value
				internalSumsForHidden[hiddenIndex] = biasesDouble[nInputs + hiddenIndex];

				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					internalSumsForHidden[hiddenIndex] += weightsDouble[hiddenIndex + nHidden * inputIndex]
					                                                    * xDouble[inputIndex];

				}

				expedThings[hiddenIndex] = java.lang.Math
				.exp(-internalSumsForHidden[hiddenIndex]);
				lambdaHidden[hiddenIndex] = 1.0 / (1.0 + expedThings[hiddenIndex]);
				lambdaHiddenComplements[hiddenIndex] = expedThings[hiddenIndex] / (1 + expedThings[hiddenIndex]);

			}

			// if there aren't, just connect inputs directly to outputs
			if (nHidden == 0) {
				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[inputIndex * nOutputsMinusOne + outputIndex]
						                                         * xDouble[inputIndex];
					}
				}
			} else {
				// build up the y output values
				for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex + outputIndex]
						                                         / (1.0 + expedThings[hiddenIndex]);
					}
				}

			}

//			emm = 0; // initialized the thing to find the maximum internal activation among the outputs;
//			emmIndex = nOutputsMinusOne ; // nOutputs - 1 or nOutputs???

			// find the probabilities
//			sumOutputsExped = 1.0; // Beware the MAGIC INITIALIZATION!!! this is for
//			// the standardized output

			emm = 0.0;
			emmIndex = nOutputsMinusOne;
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				if (outputSums[outputIndex] > emm) {
					emm = outputSums[outputIndex];
					emmIndex = outputIndex;
				}
			}

			// doing exp(0) = 1 manually...
			if (emmIndex == nOutputsMinusOne) {
				capitalCue = 1;
			} else {
				capitalCue = Math.exp(-emm); // don't forget the standardized output which is always zero: exp(0) = 1
			}

			// check if the highest probability matches the target
			if (emmIndex == targetThisExample) {
				nCorrect++;
			}

//			expedCenteredOutputSums

			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				centeredOutputSums[outputIndex] = outputSums[outputIndex] - emm;

				if (outputIndex == emmIndex) {
					expedCenteredOutputSums[outputIndex] = 1.0;
				} else {
					expedCenteredOutputSums[outputIndex] = Math.exp(centeredOutputSums[outputIndex]);
				}

				capitalCue += expedCenteredOutputSums[outputIndex];
			}

			// write down the final results...
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				finalTemp = expedCenteredOutputSums[outputIndex] / capitalCue;

//				if (outputIndex == nOutputsMinusOne) {
//				mismatch = false; // just do something so we can break on it...
//				}

				if (finalTemp <= 0.5) {
					if (targetThisExample == outputIndex) {
						outputDeltas[outputIndex] = finalTemp - 1.0;
						totalError -= Math.log(finalTemp);
					} else {
						outputDeltas[outputIndex] = finalTemp;
					}
					outputProbabilities[outputIndex] = finalTemp;
				}  else {
					// initialize an alternate sum...
					if (emmIndex == nOutputsMinusOne) {
						alternateSumTemp = 0;
					} else {
						alternateSumTemp = Math.exp(-emm);
					}
					for (int outputIndexAlt = 0; outputIndexAlt < nOutputsMinusOne; outputIndexAlt++) {
						if (outputIndexAlt != outputIndex) {
							alternateSumTemp += expedCenteredOutputSums[outputIndexAlt];
						}
					}
					alternateSumTemp /= capitalCue;

					outputProbabilities[outputIndex] = 1.0 - alternateSumTemp;

					if (targetThisExample == outputIndex) {
						outputDeltas[outputIndex] = - alternateSumTemp;
						totalError -= Math.log1p(-alternateSumTemp);
//						totalError -= Math.log(1.0 - alternateSumTemp);

					} else {
						outputDeltas[outputIndex] = 1 - alternateSumTemp;
					}

				}
			}
			// do the final/standardize output (it gets no deltas, remember... so in that sense, it is irrelevant...)

			// we don't really care about this unless we need it for the error function...
			if (targetThisExample == nOutputsMinusOne) {
				finalStandardized = Math.exp(-emm) / capitalCue;
				if (finalStandardized <= 0.5) {
					if (targetThisExample == nOutputsMinusOne) {
						totalError -= Math.log(finalStandardized);
					}
					outputProbabilities[nOutputsMinusOne] = finalStandardized;
				} else {
					alternateStandardized = 0;
					for (int outputIndexAlt = 0; outputIndexAlt < nOutputsMinusOne; outputIndexAlt++) {
						alternateStandardized += expedCenteredOutputSums[outputIndexAlt];
					}
					alternateStandardized /= capitalCue;

					outputProbabilities[nOutputsMinusOne] = 1.0 - alternateStandardized;
					if (targetThisExample == nOutputsMinusOne) {
						totalError -= Math.log1p(-alternateStandardized);
//						totalError -= Math.log(1.0-alternateStandardized);
					}
				}
			}



			// replicate old short style...



//			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
//			outputExped[outputIndex] = Math.exp(outputSums[outputIndex]);
//			sumOutputsExped += outputExped[outputIndex];
//			}


			// double check against old way...
//			outputProbabilitiesAlt = new double[nOutputs];
			// initialize the standardized output
//			highestOutputIndex = -1;
//			highestOutputProbability = Double.NEGATIVE_INFINITY;
//			for (int outputIndex = 0; outputIndex < nOutputs; outputIndex++) {
//			outputProbabilitiesAlt[outputIndex] = outputExped[outputIndex] / sumOutputsExped;
//			if (outputProbabilitiesAlt[outputIndex] > highestOutputProbability) {
//			highestOutputIndex = outputIndex;
//			highestOutputProbability = outputProbabilitiesAlt[outputIndex];
//			}
//			}


//			toleranceBetweenPs = 1E-2;

//			mismatch = false;
//			for (int outputIndex=0; outputIndex < nOutputs; outputIndex++) {
//			if (Math.abs(outputProbabilitiesAlt[outputIndex] - outputProbabilities[outputIndex]) > toleranceBetweenPs) {
//			mismatch = true;
//			break;
//			}
//			}

//			if (mismatch) {
//			double roundSum = 0.0;
//			double originalSum = 0.0;
//			for (int outputIndex=0; outputIndex < nOutputs; outputIndex++) {
//			System.out.println("example: " + exampleIndex + " [" + outputIndex + "] old = " + 
//			outputProbabilitiesAlt[outputIndex] + " ; new = " + 
//			outputProbabilities[outputIndex] + " ; difference = " +
//			(outputProbabilitiesAlt[outputIndex] - outputProbabilities[outputIndex]) );

//			roundSum    += outputProbabilities[outputIndex];
//			originalSum += outputProbabilitiesAlt[outputIndex];
//			}
//			System.out.println("original Sum = " + originalSum + " ; fancy sum = " + roundSum);
//			System.out.println("capitalCue = " + capitalCue);
//			}





			// //////////////////
			// delta attempts //
			// //////////////////

			// final deltas; with the precision problem
			for (int outputIndex = 0; outputIndex < nOutputs; outputIndex++) {
				if (targetThisExample == outputIndex) {
					// This is the realized outcome...

					outputDeltas[outputIndex] = outputProbabilities[outputIndex] - 1.0;
//					totalError -= Math.log(outputProbabilities[outputIndex]);

				} else { // This outcome was not realized...

					outputDeltas[outputIndex] = outputProbabilities[outputIndex];
				}
			} // for outputIndex


			// hidden deltas; with the precision problem
			for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
				//delta_this = lambda_this * (1 - lambda_this) * sum_next ( delta_next * w_this/next )
				// build up the part before the multiplier
				for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
					hiddenDeltas[hiddenIndex] += outputDeltas[outputIndex] * weightsDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex + outputIndex];
				}

				// multiply by the derivative
//				hiddenDeltas[hiddenIndex] *= lambdaHidden[hiddenIndex] * (1.0 - lambdaHidden[hiddenIndex]); 
				hiddenDeltas[hiddenIndex] *= lambdaHidden[hiddenIndex] * (lambdaHiddenComplements[hiddenIndex]); 
			}


			// accumulate the gradient
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				// gradient for bias for neuron j = delta_j ; i.e., activation_bias == 1
				gradientDouble[nWeightsPlusNHidden + outputIndex] += outputDeltas[outputIndex];
//				System.out.println("output gradient bit [" + outputIndex + "] = " + outputDeltas[outputIndex]);
			}

			for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
				// biases for hidden layer
				// gradient for bias for neuron j = delta_j ; i.e., activation_bias == 1
				gradientDouble[nWeights + hiddenIndex] += hiddenDeltas[hiddenIndex];
//				System.out.println("hidden gradient bit [" + hiddenIndex + "] = " + hiddenDeltas[hiddenIndex]);

				// and the weights
				// gradient for weight from i to j = delta_j * activation_i
				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					gradientDouble[hiddenIndex + nHidden * inputIndex] += hiddenDeltas[hiddenIndex] * xDouble[inputIndex];
				}
				for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
					gradientDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex	+ outputIndex] +=
						outputDeltas[outputIndex] * lambdaHidden[hiddenIndex];
				}
			}


			if (nHidden == 0) {
				//delta_this = lambda_this * (1 - lambda_this) * sum_next ( delta_next * w_this/next )
				for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
					for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
						gradientDouble[inputIndex * nOutputsMinusOne + outputIndex] +=
							outputDeltas[outputIndex] * xDouble[inputIndex];
					}
				}
			}

		} // end example index

		Object[] returnObject = new Object[3];
		returnObject[0] = new Double(totalError);
		returnObject[1] = new Double(nCorrect);

		for (int gradientIndex = 0; gradientIndex < gradientDouble.length; gradientIndex++) {
			stackedGradient.setValue(gradientIndex,0, gradientDouble[gradientIndex]);
		}
		returnObject[2] = stackedGradient;

		return returnObject;
	}

	public static Object[] findSingleHiddenErrorGradientWeighted(MultiFormatFloat explanatoryVariables,
			MultiFormatFloat targetValues, MultiFormatMatrix weights,
			MultiFormatMatrix biases, int nHidden, int nOutputsMinusOne)
	throws Exception {


		// constants derived from the data...
		// int nNeurons = (int)biases.getDimensions()[0];
		long nExamples = explanatoryVariables.getDimensions()[0];
		int  nInputs   = (int) explanatoryVariables.getDimensions()[1];
		int  nWeights  = (int) weights.getDimensions()[0];

		int nOutputs = nOutputsMinusOne + 1;

		int targetThisExample = -1;
		double importanceThisExample = 0.0;

		// declare useful variables
		// double expedThing = Double.NaN;

		long[] getBias = new long[2];
		long[] getWeight = new long[2];
		long[] getX = new long[2];
		long[] getY = new long[2];
		long[] getImportance = new long[2];


		double[] xDouble = new double[nInputs];
		double[] expedThings = new double[nHidden];
		double[] internalSumsForHidden = new double[nHidden];
		double[] lambdaHidden = new double[nHidden];
		double[] lambdaHiddenComplements = new double[nHidden];

		double[] outputSums = new double[nOutputs];
		double[] outputExped = new double[nOutputs];
//		double sumOutputsExped = Double.NaN;
		double[] outputProbabilities = new double[nOutputs];

		double[] centeredOutputSums = new double[nOutputs];
		double[] expedCenteredOutputSums = new double[nOutputs];
//		double[] outputExpedSums = new double[nOutputs];

//		double[] outputProbabilitiesAlt = new double[nOutputs];

		double[] hiddenDeltas = new double[nHidden];
		double[] outputDeltas = new double[nOutputs];

//		int highestOutputIndex = -2;
//		double highestOutputProbability = Double.NEGATIVE_INFINITY;

		double emm = Double.NaN;
		int emmIndex = Integer.MIN_VALUE;
		double capitalCue = Double.NEGATIVE_INFINITY;
		double finalTemp = Double.NaN;
		double alternateSumTemp = Double.POSITIVE_INFINITY;

		double finalStandardized = Double.NaN;
		double alternateStandardized = -1232145.231553;

//		double toleranceBetweenPs = 0.001;

//		boolean mismatch = false;



		// copy the weights and biases to a double array...
		double[] weightsDouble = new double[nWeights];
		double[] biasesDouble = new double[(int)biases.getDimensions()[0]];
		for (int weightIndex = 0; weightIndex < nWeights; weightIndex++) {
			weightsDouble[weightIndex] = weights.getValue(weightIndex,0);
		}
		for (int biasIndex = 0; biasIndex < biasesDouble.length; biasIndex++) {
			biasesDouble[biasIndex] = biases.getValue(biasIndex,0);
		}


		double[] gradientDouble = new double[nWeights + (int)biases.getDimensions()[0] - nInputs];
		MultiFormatMatrix stackedGradient = new MultiFormatMatrix(1,
				new long[] {(nWeights + biases.getDimensions()[0] - nInputs),1});


		double totalError = 0.0; // initialize the total error
//		long nCorrect = 0; // initialize the counter for total hit correctly
		double nCorrectImportance = 0.0; // initialize the counter for total hit correctly
		getBias[1] = 0;
		getWeight[1] = 0;
//		getY[1] = 0; // original way was just the target category. now, i am weighting, so the category is the 2nd column and the "weight" is the first...
		getY[1] = 1;
		getImportance[1] = 0;
//		gradientIndices[1] = 0;
		outputExped[nOutputsMinusOne] = 1.0;
		int nInputsPlusNHidden  = nInputs  + nHidden;
		int nInputsTimesNHidden = nInputs  * nHidden;
		int nWeightsPlusNHidden = nWeights + nHidden;
		
//		System.out.println("       === top of example loop: nInputsPlusNHidden = " + nInputsPlusNHidden + "; nInputs = " + nInputs + "; nHidden = " + nHidden);
		for (long exampleIndex = 0; exampleIndex < nExamples; exampleIndex++) {		
			getX[0] = exampleIndex;
			getY[0] = exampleIndex;
			getImportance[0] = exampleIndex;
			targetThisExample = (int) targetValues.getValue(getY);
			importanceThisExample = targetValues.getValue(getImportance);
			for (int xIndex = 0; xIndex < nInputs; xIndex++) {
				getX[1] = xIndex;
				xDouble[xIndex] = explanatoryVariables.getValue(getX);
			}

			hiddenDeltas = new double[nHidden];
			outputDeltas = new double[nOutputs];

			// initialize output sums with bias value (last bias/neuron is the output)
			
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
//				System.out.println("   ++> nI = " + nInputs + "; nHidden = " + nHidden + "; nO = " + nOutputs);
//				System.out.println("   --> oI = " + outputIndex + "; nIPNH = " + nInputsPlusNHidden + "; oI = " + outputIndex + "; overall = " + (nInputsPlusNHidden + outputIndex) );
//				System.out.println("   --> oS.length = " + outputSums.length + "; bD.length = " + biasesDouble.length);
				try {
					outputSums[outputIndex] = biasesDouble[nInputsPlusNHidden + outputIndex];
				} catch (Exception eee) {
					System.out.println("   +++ nInputsPlusNHidden = " + nInputsPlusNHidden + "; nOutputsMinusOne = " + nOutputsMinusOne + "; size of biases = " + biasesDouble.length + "; outputIndex = " + outputIndex + "; exampleIndex = " + exampleIndex);
					eee.printStackTrace();
				}
			}
			outputSums[nOutputsMinusOne] = 0.0;

			// supposing there are hidden neurons
			for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
				// initialize with bias value
				internalSumsForHidden[hiddenIndex] = biasesDouble[nInputs + hiddenIndex];

				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					internalSumsForHidden[hiddenIndex] += weightsDouble[hiddenIndex + nHidden * inputIndex]
					                                                    * xDouble[inputIndex];

				}

				expedThings[hiddenIndex] = java.lang.Math
				.exp(-internalSumsForHidden[hiddenIndex]);
				lambdaHidden[hiddenIndex] = 1.0 / (1.0 + expedThings[hiddenIndex]);
				lambdaHiddenComplements[hiddenIndex] = expedThings[hiddenIndex] / (1 + expedThings[hiddenIndex]);

			}

			// if there aren't, just connect inputs directly to outputs
			if (nHidden == 0) {
				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[inputIndex * nOutputsMinusOne + outputIndex]
						                                         * xDouble[inputIndex];
					}
				}
			} else {
				// build up the y output values
				for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex + outputIndex]
						                                         / (1.0 + expedThings[hiddenIndex]);
					}
				}

			}

//			emm = 0; // initialized the thing to find the maximum internal activation among the outputs;
//			emmIndex = nOutputsMinusOne ; // nOutputs - 1 or nOutputs???

			// find the probabilities
//			sumOutputsExped = 1.0; // Beware the MAGIC INITIALIZATION!!! this is for
//			// the standardized output

			emm = 0.0;
			emmIndex = nOutputsMinusOne;
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				if (outputSums[outputIndex] > emm) {
					emm = outputSums[outputIndex];
					emmIndex = outputIndex;
				}
			}

			// doing exp(0) = 1 manually...
			if (emmIndex == nOutputsMinusOne) {
				capitalCue = 1;
			} else {
				capitalCue = Math.exp(-emm); // don't forget the standardized output which is always zero: exp(0) = 1
			}

			// check if the highest probability matches the target
			if (emmIndex == targetThisExample) {
//				nCorrect++;
				nCorrectImportance += importanceThisExample;
			}

//			expedCenteredOutputSums

			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				centeredOutputSums[outputIndex] = outputSums[outputIndex] - emm;

				if (outputIndex == emmIndex) {
					expedCenteredOutputSums[outputIndex] = 1.0;
				} else {
					expedCenteredOutputSums[outputIndex] = Math.exp(centeredOutputSums[outputIndex]);
				}

				capitalCue += expedCenteredOutputSums[outputIndex];
			}

			// write down the final results...
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				finalTemp = expedCenteredOutputSums[outputIndex] / capitalCue;

//				if (outputIndex == nOutputsMinusOne) {
//				mismatch = false; // just do something so we can break on it...
//				}

				if (finalTemp <= 0.5) {
					if (targetThisExample == outputIndex) {
						outputDeltas[outputIndex] = finalTemp - 1.0;
						totalError -= importanceThisExample * Math.log(finalTemp);
					} else {
						outputDeltas[outputIndex] = finalTemp;
					}
					outputProbabilities[outputIndex] = finalTemp;
				}  else {
					// initialize an alternate sum...
					if (emmIndex == nOutputsMinusOne) {
						alternateSumTemp = 0;
					} else {
						alternateSumTemp = Math.exp(-emm);
					}
					for (int outputIndexAlt = 0; outputIndexAlt < nOutputsMinusOne; outputIndexAlt++) {
						if (outputIndexAlt != outputIndex) {
							alternateSumTemp += expedCenteredOutputSums[outputIndexAlt];
						}
					}
					alternateSumTemp /= capitalCue;

					outputProbabilities[outputIndex] = 1.0 - alternateSumTemp;

					if (targetThisExample == outputIndex) {
						outputDeltas[outputIndex] = - alternateSumTemp;
						totalError -= importanceThisExample * Math.log1p(-alternateSumTemp);
//						totalError -= importanceThisExample * Math.log(1.0 - alternateSumTemp);

					} else {
						outputDeltas[outputIndex] = 1 - alternateSumTemp;
					}

				}
			}
			// do the final/standardize output (it gets no deltas, remember... so in that sense, it is irrelevant...)

			// we don't really care about this unless we need it for the error function...
			if (targetThisExample == nOutputsMinusOne) {
				finalStandardized = Math.exp(-emm) / capitalCue;
				if (finalStandardized <= 0.5) {
					if (targetThisExample == nOutputsMinusOne) {
						totalError -= importanceThisExample * Math.log(finalStandardized);
					}
					outputProbabilities[nOutputsMinusOne] = finalStandardized;
				} else {
					alternateStandardized = 0;
					for (int outputIndexAlt = 0; outputIndexAlt < nOutputsMinusOne; outputIndexAlt++) {
						alternateStandardized += expedCenteredOutputSums[outputIndexAlt];
					}
					alternateStandardized /= capitalCue;

					outputProbabilities[nOutputsMinusOne] = 1.0 - alternateStandardized;
					if (targetThisExample == nOutputsMinusOne) {
						totalError -= importanceThisExample * Math.log1p(-alternateStandardized);
//						totalError -= importanceThisExample * Math.log(1.0-alternateStandardized);
					}
				}
			}


			// //////////////////
			// delta attempts //
			// //////////////////

			// final deltas; with the precision problem
			for (int outputIndex = 0; outputIndex < nOutputs; outputIndex++) {
				if (targetThisExample == outputIndex) {
					// This is the realized outcome...

					outputDeltas[outputIndex] = outputProbabilities[outputIndex] - 1.0;
//					totalError -= Math.log(outputProbabilities[outputIndex]);

				} else { // This outcome was not realized...

					outputDeltas[outputIndex] = outputProbabilities[outputIndex];
				}
			} // for outputIndex


			// hidden deltas; with the precision problem
			for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
				//delta_this = lambda_this * (1 - lambda_this) * sum_next ( delta_next * w_this/next )
				// build up the part before the multiplier
				for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
					hiddenDeltas[hiddenIndex] += outputDeltas[outputIndex] * weightsDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex + outputIndex];
				}

				// multiply by the derivative
//				hiddenDeltas[hiddenIndex] *= lambdaHidden[hiddenIndex] * (1.0 - lambdaHidden[hiddenIndex]); 
				hiddenDeltas[hiddenIndex] *= lambdaHidden[hiddenIndex] * (lambdaHiddenComplements[hiddenIndex]); 
			}


			// accumulate the gradient
			// HERE IS WHERE WE THROW IN THE IMPORTANCE WEIGHTING!!!!
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				// gradient for bias for neuron j = delta_j ; i.e., activation_bias == 1
				gradientDouble[nWeightsPlusNHidden + outputIndex] += importanceThisExample * outputDeltas[outputIndex];
//				System.out.println("output gradient bit [" + outputIndex + "] = " + outputDeltas[outputIndex]);
			}

			for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
				// biases for hidden layer
				// gradient for bias for neuron j = delta_j ; i.e., activation_bias == 1
				gradientDouble[nWeights + hiddenIndex] += importanceThisExample * hiddenDeltas[hiddenIndex];
//				System.out.println("hidden gradient bit [" + hiddenIndex + "] = " + hiddenDeltas[hiddenIndex]);

				// and the weights
				// gradient for weight from i to j = delta_j * activation_i
				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					gradientDouble[hiddenIndex + nHidden * inputIndex] += importanceThisExample * hiddenDeltas[hiddenIndex] * xDouble[inputIndex];
				}
				for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
					gradientDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex	+ outputIndex] +=
						importanceThisExample * outputDeltas[outputIndex] * lambdaHidden[hiddenIndex];
				}
			}


			if (nHidden == 0) {
				//delta_this = lambda_this * (1 - lambda_this) * sum_next ( delta_next * w_this/next )
				for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
					for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
						gradientDouble[inputIndex * nOutputsMinusOne + outputIndex] +=
							importanceThisExample * outputDeltas[outputIndex] * xDouble[inputIndex];
					}
				}
			}

		} // end example index

		Object[] returnObject = new Object[3];
		returnObject[0] = new Double(totalError);
		returnObject[1] = new Double(nCorrectImportance);

		for (int gradientIndex = 0; gradientIndex < gradientDouble.length; gradientIndex++) {
			stackedGradient.setValue(gradientIndex,0, gradientDouble[gradientIndex]);
		}
		returnObject[2] = stackedGradient;

		return returnObject;
	}

	public static Object[] makeReferenceTablesInt(int[][]LayerTable) throws Exception {


//		MultiFormatMatrix LayerTable = (MultiFormatMatrix)this.pullInput(0);
		long NumberOfElementsThreshold = 99999999999999L;

//		initialize some goods
		long NumElements = -1;
		int FormatIndex = -1;

//		calculate some constants. very similar to what is in MakeLayerTable...
		// number of layers
		long nLayers = LayerTable.length;

		// number of neurons
		long nNeurons = 0;
//		double nNeuronsDoubleTemp = 5;
		for (int RowIndex = 0; RowIndex < nLayers; RowIndex++) {
//			nNeuronsDoubleTemp = new Double(LayerTable.getValue(RowIndex,0));
			nNeurons += LayerTable[RowIndex][0];
		}

		// number of weights
		long nWeights = 0;
//		double nWeightsDoubleTemp = new Double(0.0);
		// Beware the MAGIC NUMBER!!! the "nLayers - 1" gets us to
		// the next to the last element...
		for (int RowIndex = 0; RowIndex < nLayers - 1; RowIndex++) {
//			nWeightsDoubleTemp = new Double(LayerTable.getValue(RowIndex, 0) *
//			LayerTable.getValue(RowIndex + 1, 0)
//			);
			nWeights += LayerTable[RowIndex][0] * LayerTable[RowIndex + 1][0];
		}

//		Neuron to Layer table
		// determine the proper format.
		NumElements = nNeurons;
		if (NumElements < NumberOfElementsThreshold) {
			// small means keep it in core; single dimensional in memory is best
			FormatIndex = 1; // Beware the MAGIC NUMBER!!!
		}
		else { // not small means big, so write it out of core; serialized blocks
			// on disk are best
			FormatIndex = 3; // Beware the MAGIC NUMBER!!!
		}
		// initialize some more goods
		long PreviousEnd = -1;
		long NewStart = -13;
		long NewEnd = -14;
		MultiFormatMatrix NeuronToLayerTable = new MultiFormatMatrix(FormatIndex,
				new long[] {nNeurons, 1});

		for (int LayerIndex = 0; LayerIndex < nLayers; LayerIndex++) {
			NewStart = PreviousEnd + 1;
			NewEnd = NewStart + LayerTable[LayerIndex][0];
			for (long InnerRowIndex = NewStart; InnerRowIndex < NewEnd; InnerRowIndex++) {
//				System.out.println("InnerRowIndex = " + InnerRowIndex + "; LayerIndex = " + LayerIndex);
				NeuronToLayerTable.setValue(InnerRowIndex, 0, LayerIndex);
			}
			PreviousEnd = NewEnd - 1;
		}

//		Layer Starting/Finishing Neuron Table
		// determine the proper format.
		NumElements = nLayers*2;
		if (NumElements < NumberOfElementsThreshold) {
			// small means keep it in core; single dimensional in memory is best
			FormatIndex = 1; // Beware the MAGIC NUMBER!!!
		}
		else { // not small means big, so write it out of core; serialized blocks
			// on disk are best
			FormatIndex = 3; // Beware the MAGIC NUMBER!!!
		}
		// initialize some more goods
		MultiFormatMatrix LayerStartFinishNeuronTable = new MultiFormatMatrix(FormatIndex,
				new long[] {nLayers, 2});
		double FirstSum = 0;
		double SecondSum = -1;
		// do the deed
		LayerStartFinishNeuronTable.setValue(0, 0, 0);
		LayerStartFinishNeuronTable.setValue(0, 1, LayerTable[0][0] - 1);
		for (int LayerIndex = 1; LayerIndex < nLayers; LayerIndex++) {
			for (int RowIndex = 0; RowIndex < LayerIndex; RowIndex++) {
				FirstSum += LayerTable[RowIndex][0];
			}
			for (int RowIndex = 0; RowIndex < LayerIndex + 1; RowIndex++) {
				SecondSum += LayerTable[RowIndex][0];
			}
			LayerStartFinishNeuronTable.setValue(LayerIndex, 0, FirstSum);
			LayerStartFinishNeuronTable.setValue(LayerIndex, 1, SecondSum);
			FirstSum = 0;
			SecondSum = -1; // remember, this is the last one in the set, not necessarily the boundary... confusing.
		}

//		Weight Number to Neuron From/To Table
//		WeightNumberFromToNeuronTable
		// determine the proper format.
		NumElements = nWeights * 2;
		if (NumElements < NumberOfElementsThreshold) {
			// small means keep it in core; single dimensional in memory is best
			FormatIndex = 1; // Beware the MAGIC NUMBER!!!
		}
		else { // not small means big, so write it out of core; serialized blocks
			// on disk are best
			FormatIndex = 3; // Beware the MAGIC NUMBER!!!
		}
		// initialize some more goods
		long StorageNumber = 0;
		MultiFormatMatrix WeightNumberFromToNeuronTable = new MultiFormatMatrix(FormatIndex,
				new long[] {nWeights, 2});
		// do the deed
		for (long LayerIndex = 0; LayerIndex < nLayers - 1; LayerIndex++) {
			for (long NeuronFrom = (long) LayerStartFinishNeuronTable.getValue(LayerIndex, 0);
			NeuronFrom < (long) LayerStartFinishNeuronTable.getValue(LayerIndex, 1) + 1;
			NeuronFrom++) {
				for (long NeuronTo = (long) LayerStartFinishNeuronTable.getValue(LayerIndex + 1, 0);
				NeuronTo < (long) LayerStartFinishNeuronTable.getValue(LayerIndex + 1, 1) + 1;
				NeuronTo++) {
					WeightNumberFromToNeuronTable.setValue(StorageNumber,0,NeuronFrom);
					WeightNumberFromToNeuronTable.setValue(StorageNumber,1,NeuronTo);

					StorageNumber++;
				}
			}
		}

		int[][] neuronToLayerTableInt = new int[(int)NeuronToLayerTable.getDimensions()[0]][(int)NeuronToLayerTable.getDimensions()[1]];
		int[][] layerStartFinishNeuronTableInt = new int[(int)LayerStartFinishNeuronTable.getDimensions()[0]][(int)LayerStartFinishNeuronTable.getDimensions()[1]];
		int[][] weightNumberFromToNeuronTableInt = new int[(int)WeightNumberFromToNeuronTable.getDimensions()[0]][(int)WeightNumberFromToNeuronTable.getDimensions()[1]];

		for (int rowIndex = 0; rowIndex < neuronToLayerTableInt.length; rowIndex++) {
			for (int colIndex = 0; colIndex < neuronToLayerTableInt[0].length; colIndex++) {
				neuronToLayerTableInt[rowIndex][colIndex] = (int)NeuronToLayerTable.getValue(rowIndex,colIndex);
//				System.out.println("nTLT [" + rowIndex + "][" + colIndex + "] = " + neuronToLayerTableInt[rowIndex][colIndex]);
			}
		}

		for (int rowIndex = 0; rowIndex < layerStartFinishNeuronTableInt.length; rowIndex++) {
			for (int colIndex = 0; colIndex < layerStartFinishNeuronTableInt[0].length; colIndex++) {
				layerStartFinishNeuronTableInt[rowIndex][colIndex] = (int)LayerStartFinishNeuronTable.getValue(rowIndex,colIndex);
//				System.out.println("lSFNT [" + rowIndex + "][" + colIndex + "] = " + layerStartFinishNeuronTableInt[rowIndex][colIndex]);
			}
		}

		for (int rowIndex = 0; rowIndex < weightNumberFromToNeuronTableInt.length; rowIndex++) {
			for (int colIndex = 0; colIndex < weightNumberFromToNeuronTableInt[0].length; colIndex++) {
				weightNumberFromToNeuronTableInt[rowIndex][colIndex] = (int)WeightNumberFromToNeuronTable.getValue(rowIndex,colIndex);
//				System.out.println("wNFTNT [" + rowIndex + "][" + colIndex + "] = " + weightNumberFromToNeuronTableInt[rowIndex][colIndex]);
			}
		}


		return new Object[] {neuronToLayerTableInt , layerStartFinishNeuronTableInt, weightNumberFromToNeuronTableInt};
	}

	public static Object[] findSingleHiddenErrorGradientOldieMoldie(MultiFormatFloat explanatoryVariables,
			MultiFormatFloat targetValues, MultiFormatMatrix weights,
			MultiFormatMatrix biases, int nHidden, int nOutputsMinusOne)
	throws Exception {

		// pull in the supplied data...
		MultiFormatFloat ExplanatoryVariables   = explanatoryVariables;
		MultiFormatFloat Targets                = targetValues;
		MultiFormatMatrix RawSerializedWeights  = weights;
		MultiFormatMatrix RawBiases             = biases;
//		double RoundingThreshold = ((Double) this.pullInput(8)).doubleValue();
		boolean CalculateGradientFlag           = true;
		boolean CalculateHessianFlag            = false;
		boolean CalculateErrorFlag              = true;
		boolean ReportAllActivationsFlag        = false;
		boolean DoNotReportAllActivationsFlag   = !ReportAllActivationsFlag;
		long nElementsThreshold          = 99999999999L;


		int[][] LayerTable = null;
		if (nHidden == 0) {
			LayerTable = new int[][] {{(int)explanatoryVariables.getDimensions()[1]},{nOutputsMinusOne}};
		} else {
			LayerTable = new int[][] {{(int)explanatoryVariables.getDimensions()[1]},{nHidden},{nOutputsMinusOne}};
		}


		Object[] referenceTables = makeReferenceTablesInt(LayerTable);
//		int[][] LayerTable                      = (int[][])((int[][]) this.pullInput(2)).clone();
//		int[][] LayerStartFinishNeuronTable     = (int[][])((int[][]) this.pullInput(5)).clone();
//		int[][] NeuronToLayerTable              = (int[][])((int[][]) this.pullInput(6)).clone();
//		int[][] WeightNumberFromToNeuronTable   = (int[][])((int[][]) this.pullInput(7)).clone();
		int[][] NeuronToLayerTable              = (int[][]) referenceTables[0];
		int[][] LayerStartFinishNeuronTable     = (int[][]) referenceTables[1];
		int[][] WeightNumberFromToNeuronTable   = (int[][]) referenceTables[2];







		long[] coordGetOne = new long[] {-1};
		long[] coordGetAnother = new long[] {-1};
		long[] coordGetTwo = new long[] {-1,-2};
		long[] coordSetOne = new long[] {-1};
		long[] coordSetTwo = new long[] {-1,-2};

		// create the local copies...
		MultiFormatMatrix SerializedWeights      = new MultiFormatMatrix(1, new long[] {RawSerializedWeights.getDimensions()[0], 1});
		MultiFormatMatrix Biases                 = new MultiFormatMatrix(1, new long[] {RawBiases.getDimensions()[0],
				RawBiases.getDimensions()[1]});

		// initialize the local copies...
		coordGetTwo[1] = 0;
		coordSetTwo[1] = 0;
		for (long rowIndex = 0; rowIndex < RawSerializedWeights.getDimensions()[0]; rowIndex++){
			coordGetTwo[0] = rowIndex;
			coordSetTwo[0] = rowIndex;
//			SerializedWeights.setValue(rowIndex,0,RawSerializedWeights.getValue(rowIndex,0));
			SerializedWeights.setValue(coordSetTwo,RawSerializedWeights.getValue(coordGetTwo));
		}
		coordSetTwo[1] = 0;
		for (long rowIndex = 0; rowIndex < RawBiases.getDimensions()[0]; rowIndex++){
			coordGetTwo[0] = rowIndex;
			coordSetTwo[0] = rowIndex;
//			Biases.setValue(rowIndex,0,RawBiases.getValue(rowIndex,0));
			Biases.setValue(coordSetTwo,RawBiases.getValue(coordGetTwo));
		}



		// determine the proper format
		// NumElements = (Number of Neurons) * (Number of Explanatory Variables)
		// the Number of Neurons is the number of biases present + 1 because of
		// the extra standardized-to-zero output neuron.
		long nElements = (Biases.getDimensions()[0] + 1) * (ExplanatoryVariables.getDimensions()[0]);
		int formatIndex = -1; // initialize
		if (nElements < nElementsThreshold) {
			// small means keep it in core; single dimensional in memory is best
			formatIndex = 1; // Beware the MAGIC NUMBER!!!
		} else { // not small means big, so go out of core; serialized blocks on
			// disk are best
			formatIndex = 3; // Beware the MAGIC NUMBER!!!
		}

		////// pull out some relevant constants
		int   nLayers         = LayerTable.length;
		int   nWeights        = WeightNumberFromToNeuronTable.length;
		long  nExamples       = ExplanatoryVariables.getDimensions()[0];
		int   nInputs         = (int)ExplanatoryVariables.getDimensions()[1];
		int   nOutputs        = LayerTable[nLayers - 1][0];
		int   outputFirst     = LayerStartFinishNeuronTable[nLayers - 1][0];
		int   layerIndexFinal = nLayers - 1;

		if (nInputs != LayerTable[0][0]) {
			System.out.println("(nInputs {" + nInputs + "} != LayerTable[0][0]) {"
					+ LayerTable[0][0] + "} -> "
					+ "number of inputs/explanatory variables does not match " + "number of input neurons.");
			throw new Exception();
		}
		// number of neurons
		int nNeurons = 0;
		for (int RowIndex = 0; RowIndex < nLayers; RowIndex++) {
			nNeurons += LayerTable[RowIndex][0];
		}
		if (nNeurons != (int)Biases.getDimensions()[0]) {
			System.out.println("nNeurons {" + nNeurons + "} != (int)Biases.getDimensions()[0] {"
					+ (int)Biases.getDimensions()[0] + "}");
			throw new Exception();
		}

		// i need to build a table that relates the layer to its first incoming weight number...
		// Beware the MAGIC NUMBER!!! i am using format #1 (SDIM) and a single dimensional array
		int[] firstWeightToLayer = new int [nLayers]; // {nLayers};

		int firstWeightNumberTemp = -18;
		for (int LayerIndex = 1; LayerIndex < nLayers; LayerIndex++) {
			firstWeightNumberTemp = 0;
			for (int LayerFunnyIndex = 0; LayerFunnyIndex < LayerIndex - 1; LayerFunnyIndex++) {
				firstWeightNumberTemp += LayerTable[LayerFunnyIndex][0]
				                                                     * LayerTable[LayerFunnyIndex + 1][0];
			}
			firstWeightToLayer[LayerIndex] = firstWeightNumberTemp;
		}


		// ************************begin NEW STUFF....
		// The idea here is to go through one example at a time and do
		// everything...
		/*
		 * in defining the NeuronActivations table, we follow a new convention. we want to avoid writing down the inputs every time, so we
		 * drop them. that means instead of having as many columns as neurons and using the neuron index as the column index we do it this
		 * way. the first column corresponds to the first non-input neuron. thus, the relationship between the "true" neuron index and its
		 * column is: NeuronTableIndex = NeuronIndex - nInputs we will need to remember to do this whenever we are reading or writing the
		 * NeuronActivations or Deltas...
		 */

		// DO A PILE OF INITIALIZATIONS...
		// the table of activations...; nNeurons + 1 because of the extra un-notated output neuron
		MultiFormatMatrix neuronActivationsOneExample = null;
		MultiFormatFloat neuronActivations = null;
		// we are gonna always use this because internally, we will use doubles for calculated quantities...
		neuronActivationsOneExample = new MultiFormatMatrix(1, new long[] { nNeurons - nInputs });
		// if not reporting all of them, we never use the activation of the standardized output...
		if (ReportAllActivationsFlag) {
			nElements = nExamples * (nNeurons - nInputs + 1);
			if (nElements < nElementsThreshold) {
				formatIndex = 1;
			} else {
				formatIndex = 3;
			}
			neuronActivations = new MultiFormatFloat(formatIndex,
					new long[] { nExamples, nNeurons - nInputs + 1 });
//			System.out.println("nExamples here = " + nExamples);
//			System.out.println("created neuronActivations [" + neuronActivations.getDimensions()[0] + "]x[" + neuronActivations.getDimensions()[1] + "]");
		}
		/*    else {
    neuronActivationsOneExample = new MultiFormatMatrix(1, new long[] { nNeurons - nInputs });
    // if not reporting all of them, we never use the activation of the standardized output...
  }
		 */    
		// this is a set of flags that says whether we need to use the complements form for the derivative to avoid rounding errors...
		MultiFormatMatrix activationComplements = new MultiFormatMatrix(1, new long[] { nNeurons - nInputs + 1 });
		/*
		 * Beware the MAGIC NUMBER!!! and assumptions... i am making this a one dimensional array that is only used and re-used when
		 * computing the deltas and derivatives... thus, i am also specifying the format to be #1 SDIM
		 */
		//		 again, nNeurons + 1 because of the extra un-notated output neuron
		MultiFormatMatrix outputSums                    = new MultiFormatMatrix(1, new long[] { (long) LayerTable[nLayers - 1][0] + 1 });
		MultiFormatMatrix outputExpedSums               = new MultiFormatMatrix(1, new long[] { (long) LayerTable[nLayers - 1][0] + 1 });
		// Beware the MAGIC NUMBER!!! these things are in format #1 (SDIM) and also is one dimensional...
		MultiFormatMatrix NNDeltas                      = new MultiFormatMatrix(1, new long[] { 0 });
		MultiFormatMatrix biasesGradient                = new MultiFormatMatrix(1, new long[] { 0 });
		MultiFormatMatrix serializedWeightsGradient     = new MultiFormatMatrix(1, new long[] { 0 });
		MultiFormatMatrix stackedWeightsBiasesGradient  = new MultiFormatMatrix(1, new long[] { 0 });

		long nParameters = nWeights + nNeurons - nInputs; // weights and meaningful biases
		MultiFormatMatrix hessianMatrix = null;

		if (CalculateGradientFlag) {
			NNDeltas = new MultiFormatMatrix(1, new long[] { nNeurons - nInputs });
			nElements = (nNeurons);
			if (nElements < nElementsThreshold) {
				formatIndex = 1;
			} else {
				formatIndex = 3;
			}
			biasesGradient = new MultiFormatMatrix(formatIndex, new long[] { nNeurons, 1 }); // ignoring the extra un-notated
			// output neuron
			nElements = (nWeights);
			if (nElements < nElementsThreshold) {
				formatIndex = 1;
			} else {
				formatIndex = 3;
			}
			serializedWeightsGradient = new MultiFormatMatrix(formatIndex, new long[] { nWeights, 1 }); // ignoring the extra
			// un-notated output neuron
			stackedWeightsBiasesGradient = new MultiFormatMatrix(1,
					new long[] { nWeights + nNeurons - nInputs, 1 }); // Beware the MAGIC NUMBER!!! SDIM regardless

			if (CalculateHessianFlag) {
				hessianMatrix = new MultiFormatMatrix(1, new long[] {nParameters, nParameters});
			}

		}



		// stuff for the activations and gradient
//		double fracCorrect = -59.1;
		boolean errorDoneForThisExample = false;
		double finalStandardized = -54.1;
		double alternateStandardized = -55.1;
		long targetThisExample = -53;
		double errorFunction = 0;
		long thisLayerFirst = -51;
		long previousLayerFirst = -1;
		long nNeuronsInPreviousLayer = -3;
		long nNeuronsInThisLayer = -4;
		int firstWeightNumber = -5;
		long neuronUnderConsideration = -6;
		long weightUnderConsideration = -7;
		double previousActivationUnderConsideration = -8.0;
		double expTemp = -3;
		double squashedSum = -9.0;
		double squashedSumComplement = -9.0;
		double sumTemp = -528.3;
		double finalTemp = -32.52;
		double emm = -23.51;
		long emmIndex = -55;
		double capitalCue = -23.52;
		double alternateSumTemp = -53.21;
		int myLayer = -1;
		long myLayerFirst = -2;
		long nextLayerFirst = -3;
		double derivativeTemp = -5.0;
		double thisActivation = -6.0;
		double posteriorSum = -7.0;
		long lookupIndex = -58;
		double biasGradientTemp = -1.0;
		double weightsGradientTemp = -2.0;
		long neuronFromLookup = -3;
		long neuronToLookup = -4;

		long nCorrect = 0;

		// stuff for the hessian
		// from Ripley...
		long i = -1; // neuron number "from" for the WeightRow connection, has to be the input layer
		long j = -2; // neuron number "to" for the WeightRow connection, has to terminate in the output layer
		long k = -3; // neuron number "from" from the WeightCol connection, has to begin in the input layer
		long ell = -4; // neuron number "to" for the WeightCol connection, has to terminate in the output layer

		double hessianTempDouble = -3.2;
		long FirstBias = nInputs; // this really needs to be this and won't get changed..
		long nBiases = nNeurons; // this is the total number of biases listed in the case of no-hidden-layers; those for the inputs are irrelevant
		long hessianColumnToStore = -3; // this is a thing to figure out what column of the Hessian we want to store this in...
		long hessianRowToStore = -4; // this is a thing to figure out what row of the Hessian we want to store this in...

		// %%%%%%%%%%% the beginning of the big EXAMPLES loop %%%%%%%%%%%%%%%%%%
		for (long ExampleIndex = 0; ExampleIndex < nExamples; ExampleIndex++) {

			////// pull out the target. will be used much later, but alas, let's do it here.
			if (CalculateGradientFlag || CalculateErrorFlag) {
				errorDoneForThisExample = false;
				coordGetTwo[0] = ExampleIndex;
				coordGetTwo[1] = 0;
//				TargetThisExample = (long) Targets.getValue(ExampleIndex, 0);
				targetThisExample = (long) Targets.getValue(coordGetTwo);
			}

			////// consider the input neurons: i think i will try to skip them
			// this time and save all that writing...

			////// consider the hidden neurons
			for (int LayerIndex = 1; LayerIndex < nLayers - 1; LayerIndex++) {
				nNeuronsInPreviousLayer = LayerTable[LayerIndex - 1][0];
				nNeuronsInThisLayer = LayerTable[LayerIndex][0];
				// remember to subtract off the number of inputs whenever
				// you're using a neuron index for storage...

				if (LayerIndex == 1) {
					thisLayerFirst = 0;
					previousLayerFirst = 0;
				} else {
					thisLayerFirst = (LayerStartFinishNeuronTable[LayerIndex][0] - nInputs);
					previousLayerFirst = (LayerStartFinishNeuronTable[LayerIndex - 1][0] - nInputs);
				}
				// let us now determine the weight number for the first incoming
				// connection. then we can just do a regular spacing to figure out the others...
				// gonna figure this as a table outright before this big loop...

				firstWeightNumber = firstWeightToLayer[LayerIndex];
				// this is the weight number for the first connection to this layer...
				//      System.out.println("This layer's first incoming weight number is: " + FirstWeightNumber);
				// do the actual multiplication/summation/activation brute force style...
				for (long withinIndex = 0; withinIndex < LayerTable[LayerIndex][0]; withinIndex++) {
					// ThisLayerLast + 1 due to ThisLayerLast being the index...
					neuronUnderConsideration = thisLayerFirst + withinIndex;
					// ok, here is where the shifting of the indices bites back:
					// the biases are still coded as neuron indices, so we have
					// to add the number of inputs back in...

					coordGetTwo[0] = (neuronUnderConsideration + nInputs);
					coordGetTwo[1] = 0;
					sumTemp = Biases.getValue(coordGetTwo);
					for (long incomingIndex = 0; incomingIndex < nNeuronsInPreviousLayer; incomingIndex++) {
						weightUnderConsideration = firstWeightNumber + withinIndex + incomingIndex
						* nNeuronsInThisLayer;
						if (LayerIndex == 1) {
							coordGetTwo[0] = ExampleIndex;
							coordGetTwo[1] = incomingIndex;
							previousActivationUnderConsideration = ExplanatoryVariables.getValue(coordGetTwo);
						} else {
							if (DoNotReportAllActivationsFlag) {
//								PreviousActivationUnderConsideration = NeuronActivations
//								.getValue(PreviousLayerFirst + IncomingIndex);
								coordGetOne[0] = previousLayerFirst + incomingIndex;
								previousActivationUnderConsideration = neuronActivationsOneExample.getValue(coordGetOne);
							} else {
//								PreviousActivationUnderConsideration = NeuronActivations.getValue(ExampleIndex,
//								PreviousLayerFirst + IncomingIndex);
//								coordGetTwo[0] = ExampleIndex;
//								coordGetTwo[1] = previousLayerFirst + incomingIndex;
//								previousActivationUnderConsideration = neuronActivations.getValue(coordGetTwo);
								// changing this to always use the internal representation in doubles
								coordGetOne[0] = previousLayerFirst + incomingIndex;
								previousActivationUnderConsideration = neuronActivationsOneExample.getValue(coordGetOne);
							}
						}
						coordGetTwo[0] = weightUnderConsideration;
						coordGetTwo[1] = 0;
//						SumTemp += PreviousActivationUnderConsideration
//						* SerializedWeights.getValue(WeightUnderConsideration, 0);
						sumTemp += previousActivationUnderConsideration * SerializedWeights.getValue(coordGetTwo);
					}
					expTemp = java.lang.Math.exp(-sumTemp);
					squashedSum = 1.0 / (1.0 + expTemp);
					if (DoNotReportAllActivationsFlag) {
//						NeuronActivations.setValue(NeuronUnderConsideration, SquashedSum);
						coordSetOne[0] = neuronUnderConsideration;
						neuronActivationsOneExample.setValue(coordSetOne, squashedSum);
					} else {
						// do the internal representation
//						NeuronActivations.setValue(NeuronUnderConsideration, SquashedSum);
						coordSetOne[0] = neuronUnderConsideration;
						neuronActivationsOneExample.setValue(coordSetOne, squashedSum);
						// and write a copy down for the outside world...
//						NeuronActivations.setValue(ExampleIndex, NeuronUnderConsideration, SquashedSum);
						coordSetTwo[0] = ExampleIndex;
						coordSetTwo[1] = neuronUnderConsideration;
						neuronActivations.setValue(coordSetTwo, (float)squashedSum);
					}
					squashedSumComplement = expTemp / (1 + expTemp);
					//                        System.out.println("^^^ Example " + ExampleIndex + ": ExpTemp = " + ExpTemp + "; SSC = " +
					// SquashedSumComplement);
					coordSetOne[0] = neuronUnderConsideration;
//					ActivationComplements.setValue(NeuronUnderConsideration, SquashedSumComplement);
					activationComplements.setValue(coordSetOne, squashedSumComplement);
				}

			}
			////// consider the output neurons: SoftMax activations...
			nNeuronsInPreviousLayer = LayerTable[layerIndexFinal - 1][0];
			nNeuronsInThisLayer = LayerTable[layerIndexFinal][0];
			// remember to subtract off the number of inputs whenever
			// you're using a neuron index for storage...
			thisLayerFirst = (LayerStartFinishNeuronTable[layerIndexFinal][0] - nInputs);

//			for (int rowIndex = 0; rowIndex < LayerStartFinishNeuronTable.length; rowIndex++) {
//			for (int colIndex = 0; colIndex < LayerStartFinishNeuronTable[0].length; colIndex++) {
//			System.out.println("lSFNT [" + rowIndex + "][" + colIndex + "] = " + LayerStartFinishNeuronTable[rowIndex][colIndex]);
//			}
//			}
//			System.out.println("layerIndexFinal = " + layerIndexFinal);
//			System.out.println("LayerStartFinishNeuronTable[1][0] = " + LayerStartFinishNeuronTable[1][0]);


			//  ThisLayerLast = (LayerStartFinishNeuronTable[LayerIndexFinal][1] - nInputs);
			previousLayerFirst = (LayerStartFinishNeuronTable[layerIndexFinal - 1][0] - nInputs);
//			PreviousLayerLast = (LayerStartFinishNeuronTable[LayerIndexFinal - 1][1] - nInputs);
			firstWeightNumber = firstWeightToLayer[layerIndexFinal];

//			Denominator = 1; // "one" because of the dead output which is always
			// zero: exp(0) = 1
			emm = 0; // initialized the thing to find the maximum internal activation among the outputs;
			emmIndex = nOutputs ; // nOutputs - 1 or nOutputs???
			// we start at zero because that would mean the final standardized output is the largest

			for (long WithinIndex = 0; WithinIndex < nNeuronsInThisLayer; WithinIndex++) {
				// ThisLayerLast + 1 due to ThisLayerLast being the index...
				neuronUnderConsideration = thisLayerFirst + WithinIndex;
				//        System.out.println(" ---> NeuronUnderConsideration = " +
				// NeuronUnderConsideration + "; nNeurons = " + nNeurons);
				// ok, here is where the shifting of the indices bites back: the
				// biases are still coded as neuron indices, so we have to add
				// the number of inputs back in...

//				SumTemp = Biases.getValue(NeuronUnderConsideration + nInputs, 0);
				coordGetTwo[0] = neuronUnderConsideration + nInputs;
				coordGetTwo[1] = 0;
				sumTemp = Biases.getValue(coordGetTwo);
				for (long IncomingIndex = 0; IncomingIndex < nNeuronsInPreviousLayer; IncomingIndex++) {
					weightUnderConsideration = firstWeightNumber + WithinIndex + IncomingIndex
					* nNeuronsInThisLayer;
					if (previousLayerFirst < 0) {
//						PreviousActivationUnderConsideration = ExplanatoryVariables.getValue(ExampleIndex,
//						PreviousLayerFirst + IncomingIndex + nInputs);
						coordGetTwo[0] = ExampleIndex;
						coordGetTwo[1] = previousLayerFirst + IncomingIndex + nInputs;
						previousActivationUnderConsideration = ExplanatoryVariables.getValue(coordGetTwo);
					} else {
						if (DoNotReportAllActivationsFlag) {
//							PreviousActivationUnderConsideration = NeuronActivations.getValue(PreviousLayerFirst
//							+ IncomingIndex);
							coordGetOne[0] = previousLayerFirst + IncomingIndex;
							previousActivationUnderConsideration = neuronActivationsOneExample.getValue(coordGetOne);
						} else {
//							PreviousActivationUnderConsideration = NeuronActivations.getValue(ExampleIndex,
//							PreviousLayerFirst + IncomingIndex);
//							coordGetTwo[0] = ExampleIndex;
//							coordGetTwo[1] = previousLayerFirst + IncomingIndex;
//							previousActivationUnderConsideration = neuronActivations.getValue(coordGetTwo);
							// stick with the internal double representation for calculations
							coordGetOne[0] = previousLayerFirst + IncomingIndex;
							previousActivationUnderConsideration = neuronActivationsOneExample.getValue(coordGetOne);
						}
					}

//					SumTemp += PreviousActivationUnderConsideration
//					* SerializedWeights.getValue(WeightUnderConsideration, 0);
					coordGetTwo[0] = weightUnderConsideration;
					coordGetTwo[1] = 0;
					sumTemp += previousActivationUnderConsideration
					* SerializedWeights.getValue(coordGetTwo);
				}
//				StretchedSum = java.lang.Math.exp(SumTemp);
				//        System.out.println("SumTemp = " + SumTemp + "; StretchedSum = " + StretchedSum);
				outputSums.setValue(WithinIndex, sumTemp);
				if (sumTemp > emm) {
					emm = sumTemp;
					emmIndex = WithinIndex;
				}
//				System.out.println("   -> emm = " + emm + "; emmIndex = " + emmIndex + "; for #"+ExampleIndex);
			}
			// go back through and find the centered intermediate sums and the denominator
			if (emmIndex == nOutputs) {
				capitalCue = 1;
			} else {
				capitalCue = Math.exp(-emm); // don't forget the standardized output which is always zero: exp(0) = 1
			}
			//System.out.println("*** emm = " + emm + "; emmIndex = " + emmIndex);
			for (long WithinIndex = 0; WithinIndex < nNeuronsInThisLayer; WithinIndex++) {
//				System.out.println("--> before centering: OutputSums("+WithinIndex+")="+OutputSums.getValue(WithinIndex));
//				OutputSums.setValue(WithinIndex, OutputSums.getValue(WithinIndex) - emm);
				coordSetOne[0] = WithinIndex;
				coordGetOne[0] = WithinIndex;
				outputSums.setValue(coordSetOne, outputSums.getValue(coordGetOne) - emm);
//				System.out.println("---> after centering: OutputSums("+WithinIndex+")="+OutputSums.getValue(WithinIndex));
				if (WithinIndex == emmIndex) {
//					OutputExpedSums.setValue(WithinIndex, 1); // record the numerator
					coordSetOne[0] = WithinIndex;
					outputExpedSums.setValue(coordSetOne, 1); // record the numerator
				} else {
//					OutputExpedSums.setValue(WithinIndex, Math.exp(OutputSums.getValue(WithinIndex))); // record the numerator
					coordGetOne[0] = WithinIndex;
					coordSetOne[0] = WithinIndex;
					outputExpedSums.setValue(coordSetOne, Math.exp(outputSums.getValue(coordGetOne))); // record the numerator
				}
//				CapitalCue += OutputExpedSums.getValue(WithinIndex); // accumulate the denominator
				coordGetOne[0] = WithinIndex;
				capitalCue += outputExpedSums.getValue(coordGetOne); // accumulate the denominator
//				System.out.println(" ---> after exp: OutputExpedSums("+WithinIndex+")="+OutputExpedSums.getValue(WithinIndex));
			}

			// bump up the counter for the number correct
			if (emmIndex == targetThisExample) {
				nCorrect++;
			}
			// go through yet again to write down the final results...
			for (long WithinIndex = 0; WithinIndex < LayerTable[layerIndexFinal][0]; WithinIndex++) {
				neuronUnderConsideration = thisLayerFirst + WithinIndex;
//				FinalTemp = OutputExpedSums.getValue(WithinIndex) / CapitalCue;
				coordGetOne[0] = WithinIndex;
				finalTemp = outputExpedSums.getValue(coordGetOne) / capitalCue;
				if (DoNotReportAllActivationsFlag) {
					coordSetOne[0] = neuronUnderConsideration;
//					NeuronActivations.setValue(NeuronUnderConsideration, FinalTemp);
					if (coordSetOne[0] < 0) {
						System.out.println("cSO[0] = " + coordSetOne[0]);
						System.out.println("WithinIndex = " + WithinIndex);
						System.out.println("thisLayerFirst = " + thisLayerFirst);
					}
					neuronActivationsOneExample.setValue(coordSetOne, finalTemp);
				} else {
					// make a copy for the internal representation
//					NeuronActivations.setValue(NeuronUnderConsideration, FinalTemp);
					coordSetOne[0] = neuronUnderConsideration;
					neuronActivationsOneExample.setValue(coordSetOne, finalTemp);
					// and another for the outside world...
//					NeuronActivations.setValue(ExampleIndex, NeuronUnderConsideration, FinalTemp);
					coordSetTwo[0] = ExampleIndex;
					coordSetTwo[1] = neuronUnderConsideration;
					neuronActivations.setValue(coordSetTwo, (float)finalTemp);
				}

				if (CalculateErrorFlag || CalculateGradientFlag) {
					if (finalTemp <= 0.5) {
						if (targetThisExample == WithinIndex) {
							// This is the realized outcome...
							if (CalculateGradientFlag) {
//								NNDeltas.setValue(OutputFirst + WithinIndex - nInputs, FinalTemp - 1.0);
								coordSetOne[0] = outputFirst + WithinIndex - nInputs;
								NNDeltas.setValue(coordSetOne, finalTemp - 1.0);
							}
							if (CalculateErrorFlag) {
								errorFunction -= Math.log(finalTemp);
								errorDoneForThisExample = true;
							}
						} else { // This outcome was not realized...
							if (CalculateGradientFlag) {
//								NNDeltas.setValue(OutputFirst + WithinIndex - nInputs, FinalTemp);
								coordSetOne[0] = outputFirst + WithinIndex - nInputs;
								NNDeltas.setValue(coordSetOne, finalTemp);
							}
						}
					} else {
						if (emmIndex == nOutputs) {
							alternateSumTemp = 0;
						}
						else {
							alternateSumTemp = Math.exp(-emm);
						}
						for (long WithinIndexAlt = 0; WithinIndexAlt < LayerTable[layerIndexFinal][0]; WithinIndexAlt++) {
							if (WithinIndexAlt != WithinIndex) {
//								AlternateSumTemp += OutputExpedSums.getValue(WithinIndexAlt);
								coordGetOne[0] = WithinIndexAlt;
								alternateSumTemp += outputExpedSums.getValue(coordGetOne);
							}
						}
						alternateSumTemp /= capitalCue;

						if (targetThisExample == WithinIndex) {
							// This is the realized outcome...
							if (CalculateGradientFlag) {
//								NNDeltas.setValue(OutputFirst + WithinIndex - nInputs, -AlternateSumTemp);
								coordSetOne[0] = outputFirst + WithinIndex - nInputs;
								NNDeltas.setValue(coordSetOne, -alternateSumTemp);
//								System.out.println(" --> Option realized has P>1/2 AltTempSum = " + AlternateSumTemp + "; Option=" + WithinIndex +"; ExampleIndex = " + ExampleIndex );
							}
							if (CalculateErrorFlag) {
//								ErrorFunction -= java.lang.Math.log1p(-AlternateSumTemp);
								errorFunction -= java.lang.Math.log(1.0-alternateSumTemp);
								errorDoneForThisExample = true;
							}
						} else {
							// This outcome was not realized...
							if (CalculateGradientFlag) {
//								NNDeltas.setValue(OutputFirst + WithinIndex - nInputs, 1 - AlternateSumTemp);
								coordSetOne[0] = outputFirst + WithinIndex - nInputs;
								NNDeltas.setValue(coordSetOne, 1 - alternateSumTemp);
							}
						}

					}
				}
			}

			// do the final "permanently-zeroed" neuron and any error function associated with it.
			if (ReportAllActivationsFlag || (CalculateErrorFlag && !errorDoneForThisExample)) {
//				System.out.println("--We made it to calculate a FinalStandardized in Example [" + ExampleIndex +
//				"] and the Error Flag is: " + ErrorDoneForThisExample);
				finalStandardized = Math.exp(-emm) / capitalCue;
				if (ReportAllActivationsFlag) {
//					NeuronActivations.setValue(ExampleIndex, nNeurons - nInputs, FinalStandardized);
					coordSetTwo[0] = ExampleIndex;
					coordSetTwo[1] = nNeurons - nInputs;
					neuronActivations.setValue(coordSetTwo, (float)finalStandardized);
				}
				if (CalculateErrorFlag && !errorDoneForThisExample) {
//					if (TargetThisExample == (nNeurons - nInputs)) {
					if (finalStandardized <= 0.5) {
						errorFunction -= Math.log(finalStandardized);
						errorDoneForThisExample = true;
					} else {
						alternateStandardized = 0;
						for (long withinIndexAlt = 0; withinIndexAlt < LayerTable[layerIndexFinal][0]; withinIndexAlt++) {
//							AlternateStandardized += OutputExpedSums.getValue(WithinIndexAlt);
							coordGetOne[0] = withinIndexAlt;
							alternateStandardized += outputExpedSums.getValue(coordGetOne);
						}
						alternateStandardized /= capitalCue;
//						ErrorFunction -= Math.log1p(-AlternateStandardized);
						errorFunction -= Math.log(1.0-alternateStandardized);
						errorDoneForThisExample = true;
					}
				}
//				}
			}
//			if (!ErrorDoneForThisExample) {
//			System.out.println("The Error has not been added in for this example [" + ExampleIndex + "]");
//			}
			////// calculate the deltas

			// the deltas for the output layer have already been done...
			// now for the hidden layers...
			if (CalculateGradientFlag) {
				for (long NeuronIndex = (outputFirst - 1); NeuronIndex >= nInputs; NeuronIndex--) {
					// NOTICE: skipping the inputs....
					//              System.out.println("NeuronIndex = " + NeuronIndex);
					// going backwards through. hence my non-standard break criterion...
					// find the list of posterior nodes for a particular neuron.
					// that is, what neurons does this one feed into...
					// under the assumption of full connectivity, it will feed into
					// all of the neurons in the next layer...
					// so, i need to know what layer i'm in, and what layer comes after...
					lookupIndex = NeuronIndex - nInputs;
					//                System.out.println("---> NeuronIndex="+NeuronIndex+";
					// nInputs="+nInputs+"; LookupIndex="+LookupIndex);
					myLayer = NeuronToLayerTable[(int)NeuronIndex][0];
					myLayerFirst = (LayerStartFinishNeuronTable[myLayer][0] - nInputs);
					nextLayerFirst = LayerStartFinishNeuronTable[myLayer + 1][0] - nInputs;

					if (DoNotReportAllActivationsFlag) {
//						ThisActivation = NeuronActivations.getValue(LookupIndex);
						coordGetOne[0] = lookupIndex;
						thisActivation = neuronActivationsOneExample.getValue(coordGetOne);
					} else {
//						ThisActivation = NeuronActivations.getValue(ExampleIndex, LookupIndex);
//						coordGetTwo[0] = ExampleIndex;
//						coordGetTwo[1] = lookupIndex;
//						thisActivation = neuronActivations.getValue(coordGetTwo);
						// always use the internal double representation for computations
						coordGetOne[0] = lookupIndex;
						thisActivation = neuronActivationsOneExample.getValue(coordGetOne);
					}
					//                System.out.println("#---> MyLayer="+MyLayer+";
					// MyLayerFirst="+MyLayerFirst+";
					// NextLayerFirst="+NextLayerFirst);


//					DerivativeTemp = ThisActivation * ActivationComplements.getValue(LookupIndex);
					coordGetOne[0] = lookupIndex;
					derivativeTemp = thisActivation * activationComplements.getValue(coordGetOne);


					// run through the posterior nodes and their weights. trying to
					// calculate sum [over posterior neurons]
					// delta_posterior*w_j_to_posterior

					// find the weight number for the first connection under
					// consideration...
					firstWeightNumber = firstWeightToLayer[myLayer + 1]
					                                       + (((int)lookupIndex - (int)myLayerFirst) * LayerTable[myLayer + 1][0]);
					posteriorSum = 0;
					//                System.out.println("
					// ->FirstWeightNumber="+FirstWeightNumber);
					for (long PosteriorIndex = 0; PosteriorIndex < LayerTable[myLayer + 1][0]; PosteriorIndex++) {
						// that is, from 0 to the # of neurons in the next layer (minus one for index's sake)
						//System.out.println("** -> PosteriorIndex="+PosteriorIndex+"; NextLayerFirst="+NextLayerFirst+";
						// FirstWeightNumber="+FirstWeightNumber);

//						PosteriorSum += (NNDeltas.getValue(PosteriorIndex + NextLayerFirst) * SerializedWeights
//						.getValue(PosteriorIndex + FirstWeightNumber, 0));
						coordGetOne[0] = PosteriorIndex + nextLayerFirst;
						coordGetTwo[0] = PosteriorIndex + firstWeightNumber;
						coordGetTwo[1] = 0;
						posteriorSum += (NNDeltas.getValue(coordGetOne) * SerializedWeights.getValue(coordGetTwo));


						// i can do this because of the way the weights are encoded. the
						// outgoing weights from a particular neuron are sequential..
					}
					coordSetOne[0] = lookupIndex;
					NNDeltas.setValue(coordSetOne, derivativeTemp * posteriorSum);
//					NNDeltas.setValue(LookupIndex, DerivativeTemp * PosteriorSum);
				}

				// calculate the gradient
				// figure the gradient for the biases
				for (long NeuronIndex = LayerTable[0][0]; NeuronIndex < nNeurons; NeuronIndex++) {
					biasGradientTemp = biasesGradient.getValue(NeuronIndex, 0);
					lookupIndex = NeuronIndex - nInputs;
//					BiasGradientTemp += NNDeltas.getValue(LookupIndex);
					coordGetOne[0] = lookupIndex;
					biasGradientTemp += NNDeltas.getValue(lookupIndex);

//					BiasesGradient.setValue(NeuronIndex, 0, BiasGradientTemp);
					coordSetTwo[0] = NeuronIndex;
					coordSetTwo[1] = 0;
					biasesGradient.setValue(coordSetTwo, biasGradientTemp);
				}
				// figure the gradient for the weights
				for (int WeightIndex = 0; WeightIndex < nWeights; WeightIndex++) {
//					WeightsGradientTemp = SerializedWeightsGradient.getValue(WeightIndex, 0);
					coordGetTwo[0] = WeightIndex;
					coordGetTwo[1] = 0;
					weightsGradientTemp = serializedWeightsGradient.getValue(coordGetTwo);

					//                System.out.println("WeightIndex = " + WeightIndex);
					neuronFromLookup = (WeightNumberFromToNeuronTable[WeightIndex][0] - nInputs);
					neuronToLookup = (WeightNumberFromToNeuronTable[WeightIndex][1] - nInputs);
					if (neuronFromLookup < 0) { // we have a weight originating in an input...

//						WeightsGradientTemp += (ExplanatoryVariables.getValue(ExampleIndex, NeuronFromLookup
//						+ nInputs) * NNDeltas.getValue(NeuronToLookup));
						coordGetTwo[0] = ExampleIndex;
						coordGetTwo[1] = neuronFromLookup + nInputs;
						coordGetOne[0] = neuronToLookup;
						weightsGradientTemp += (ExplanatoryVariables.getValue(coordGetTwo) * NNDeltas.getValue(coordGetOne));
					} else { // the weight is *not* originating in an input
////					always use the internal double representation
						//            if (DoNotReportAllActivationsFlag) {
//						WeightsGradientTemp += (NeuronActivations.getValue(NeuronFromLookup) * NNDeltas
//						.getValue(NeuronToLookup));
						coordGetOne[0] = neuronFromLookup;
						coordGetAnother[0] = neuronToLookup;

						weightsGradientTemp += (neuronActivationsOneExample.getValue(coordGetOne) * NNDeltas
								.getValue(coordGetAnother));
//						} else {
//	WeightsGradientTemp += (NeuronActivations.getValue(ExampleIndex, NeuronFromLookup) * NNDeltas
//						.getValue(NeuronToLookup));
//						coordGetTwo[0] = ExampleIndex;
//						coordGetTwo[1] = neuronFromLookup;
//						coordGetOne[0] = neuronToLookup;
//						weightsGradientTemp += (neuronActivations.getValue(coordGetTwo) * NNDeltas
//						.getValue(coordGetOne));
//						}
					}
					coordSetTwo[0] = WeightIndex;
					coordSetTwo[1] = 0;
//					SerializedWeightsGradient.setValue(WeightIndex, 0, WeightsGradientTemp);
					serializedWeightsGradient.setValue(coordSetTwo, weightsGradientTemp);
				}



				////////////////////////////////////////////////////////////////////////////////////////////////////
				// do the hessian                                                                                 //
				////////////////////////////////////////////////////////////////////////////////////////////////////
				if (CalculateHessianFlag) {

					// all of the connections terminate in the output layer and originate in
					// the input layer (or biases which come later)

					// These are the weight/weight elements...
					for (int weightRow = 0; weightRow < nWeights ; weightRow++ ) {
						for (int weightCol = weightRow; weightCol < nWeights ; weightCol++){
							i = WeightNumberFromToNeuronTable[weightRow][0]; // this will be an explanatory variable
							j = (WeightNumberFromToNeuronTable[weightRow][1] - nInputs);
							k = WeightNumberFromToNeuronTable[weightCol][0]; // this will be an explanatory variable
							ell = (WeightNumberFromToNeuronTable[weightCol][1] - nInputs);
							hessianTempDouble = hessianMatrix.getValue(weightRow,weightCol);
							if (j == ell) { // then we do it with a one in that spot...
								hessianTempDouble += ( ExplanatoryVariables.getValue(ExampleIndex,i) * ExplanatoryVariables.getValue(ExampleIndex,k) * 
										neuronActivationsOneExample.getValue(j) * (1 - neuronActivationsOneExample.getValue(ell)) );
								hessianMatrix.setValue(weightRow,weightCol,hessianTempDouble);
							}
							else { // then we do it with a zero in that spot, that is, without it altogether...
								hessianTempDouble -= ExplanatoryVariables.getValue(ExampleIndex,i) * ExplanatoryVariables.getValue(ExampleIndex,k) * 
								neuronActivationsOneExample.getValue(j) * ( neuronActivationsOneExample.getValue(ell));
								hessianMatrix.setValue(weightRow,weightCol,hessianTempDouble);
							}
						}
					}

					// These are the weight/bias combinations...
//					FirstBias = nInputs;
//					nBiases = nNeurons; // this is the total number of biases listed; those for the inputs are irrelevant
//					ColumnToStore = -3; // this is a thing to figure out what column of the Hessian we want to store this in...
					for (int weightRow = 0; weightRow < nWeights; weightRow++) {
						for (long biasIndex = FirstBias; biasIndex < nBiases; biasIndex++){
							i = WeightNumberFromToNeuronTable[weightRow][0]; // this will be an input neuron
							j = (WeightNumberFromToNeuronTable[weightRow][1] - nInputs);
							// k is absent because it would be a "+1 permanently" neuron, but i don't list them, i hardcode them...
							ell = biasIndex - nInputs; // here, the biases are "terminating" in the neuron with the same number...
							hessianColumnToStore = nWeights + biasIndex - nInputs;
							hessianTempDouble = hessianMatrix.getValue(weightRow,hessianColumnToStore);
							if (j == ell) { // then we do it with a one in that spot...
								// same idea as above, just the activation of "k" is 1
								hessianTempDouble += ( ExplanatoryVariables.getValue(ExampleIndex,i) *  
										neuronActivationsOneExample.getValue(j) * (1 - neuronActivationsOneExample.getValue(ell)) );

								hessianMatrix.setValue(weightRow,hessianColumnToStore,hessianTempDouble);
								hessianMatrix.setValue(hessianColumnToStore,weightRow,hessianTempDouble); // less apparent, but still living for symmetry...
							}
							else { // then we do it wiht a zero in that spot, that is, without it altogether...
//								HessTemp += NeuronActivations.getValue(Example,i) *
//								NeuronActivations.getValue(Example,j) * ( - NeuronActivations.getValue(Example,ell));
								// moving the minus sign out of multiplication to the front as a "-="
								hessianTempDouble -= ( ExplanatoryVariables.getValue(ExampleIndex,i) *  
										neuronActivationsOneExample.getValue(j) * ( neuronActivationsOneExample.getValue(ell)) );
							}
							hessianMatrix.setValue(weightRow,hessianColumnToStore,hessianTempDouble);
						}
					}

					// These are the bias/bias combinations...
//					long RowToStore = -4;
//					ColumnToStore = -3; // this is a thing to figure out what column of the Hessian we want to store this in...
					for (long biasIndexRow = FirstBias; biasIndexRow < nBiases; biasIndexRow++) {
						for (long biasIndexCol = biasIndexRow; biasIndexCol < nBiases; biasIndexCol++){
							// i is absent because it would be a "+1 permanently" neuron, but i don't list them, i hardcode them...
							j = biasIndexRow - nInputs;
							// k is absent because it would be a "+1 permanently" neuron, but i don't list them, i hardcode them...
							ell = biasIndexCol - nInputs; // here, the biases are "terminating" in the neuron with the same number...
							hessianRowToStore = nWeights + biasIndexRow - nInputs;
							hessianColumnToStore = nWeights + biasIndexCol - nInputs;
							hessianTempDouble = hessianMatrix.getValue(hessianRowToStore,hessianColumnToStore);
							if (j == ell) { // then we do it with a one in that spot...
								// same idea as above, just the activation of "k" is 1
								hessianTempDouble += neuronActivationsOneExample.getValue(j) * (1 - neuronActivationsOneExample.getValue(ell));
								hessianMatrix.setValue(hessianRowToStore,hessianColumnToStore,hessianTempDouble);
							}
							else { // then we do it with a zero in that spot, that is, without it altogether...
								hessianTempDouble -= neuronActivationsOneExample.getValue(j) * (neuronActivationsOneExample.getValue(ell));
								hessianMatrix.setValue(hessianRowToStore,hessianColumnToStore,hessianTempDouble);
							}
						}
					}

					// now do the symmetry thing...
					for (long hessianIndexA = 0; hessianIndexA < nParameters; hessianIndexA++) {
						for (long hessianIndexB = hessianIndexA + 1; hessianIndexB < nParameters; hessianIndexB++) {
							hessianMatrix.setValue(hessianIndexB,hessianIndexA,
									hessianMatrix.getValue(hessianIndexA,hessianIndexB));
						}
					}










				}
				////////////////////////////////////////////////////////////////////////////////////////////////////
				// end the hessian                                                                                 //
				////////////////////////////////////////////////////////////////////////////////////////////////////

			} // end of if(CalculateGradientFlag)

		} // end of examples loop
		//      %%%%%%%%%%% the end of the big EXAMPLES loop %%%%%%%%%%%%%%%%%%

//		if (CalculateErrorFlag) {
//		fracCorrect = (double)nCorrect/(double)nExamples;
//		}
		if (CalculateGradientFlag) {
			// now put together the stacked gradient...
			for (long NeuronIndex = LayerTable[0][0]; NeuronIndex < nNeurons; NeuronIndex++) {
				stackedWeightsBiasesGradient.setValue(NeuronIndex - nInputs + nWeights, 0, biasesGradient
						.getValue(NeuronIndex, 0));
			}
			for (long WeightIndex = 0; WeightIndex < nWeights; WeightIndex++) {
				stackedWeightsBiasesGradient.setValue(WeightIndex, 0, serializedWeightsGradient.getValue(
						WeightIndex, 0));
			}
		}

		// ********************** end NEW STUFF...

//		if (ReportAllActivationsFlag) {
//		this.pushOutput(neuronActivations,          0);
//		}
//		this.pushOutput(new Double(errorFunction),    1);
//		this.pushOutput(new Double(nCorrect),         2);
//		this.pushOutput(new Double(nExamples),        3);
//		this.pushOutput(biasesGradient,               4);
//		this.pushOutput(serializedWeightsGradient,    5);
//		this.pushOutput(stackedWeightsBiasesGradient, 6);
//		this.pushOutput(hessianMatrix,                7);

//		long endTime = System.currentTimeMillis();
//		System.out.println("\t---[ " + funnyName 
//		+ " ]\te = " + new Date() + "; delta = " + ((endTime - startTime)/1000f));


		Object[] returnObject = new Object[3];
		double totalError = errorFunction;
		returnObject[0] = new Double(totalError);
		returnObject[1] = new Double(nCorrect);

		MultiFormatMatrix stackedGradient = stackedWeightsBiasesGradient;
//		for (int gradientIndex = 0; gradientIndex < gradientDouble.length; gradientIndex++) {
//		stackedGradient.setValue(gradientIndex,0, gradientDouble[gradientIndex]);
//		}
		returnObject[2] = stackedGradient;

		return returnObject;


	}


	public static Object[] findSingleHiddenErrorGradientALT(MultiFormatFloat explanatoryVariables,
			MultiFormatFloat targetValues, MultiFormatMatrix weights,
			MultiFormatMatrix biases, int nHidden, int nOutputsMinusOne)
	throws Exception {


		// a magic number
		double minSumAllowed = -Math.log(Double.MAX_VALUE); // This actually needs
		// to be what it is...

		// constants derived from the data...
		// int nNeurons = (int)biases.getDimensions()[0];
		long nExamples = explanatoryVariables.getDimensions()[0];
		int nInputs = (int) explanatoryVariables.getDimensions()[1];
		int nWeights = (int) weights.getDimensions()[0];

		int nOutputs = nOutputsMinusOne + 1;

		int targetThisExample = -1;

		// declare useful variables
		// double expedThing = Double.NaN;

		long[] getBias = new long[2];
		long[] getWeight = new long[2];
		long[] getX = new long[2];
		long[] getY = new long[2];


		double[] xDouble = new double[nInputs];
		double[] expedThings = new double[nHidden];
		double[] internalSumsForHidden = new double[nHidden];
		double[] lambdaHidden = new double[nHidden];

		double[] outputSums = new double[nOutputs];
		double[] outputExped = new double[nOutputs];
		double sumOutputsExped = Double.NaN;
		double[] outputProbabilities = new double[nOutputs];

		double[] hiddenDeltas = new double[nHidden];
		double[] outputDeltas = new double[nOutputs];

		int highestOutputIndex = -2;
		double highestOutputProbability = Double.NEGATIVE_INFINITY;

		// copy the weights and biases to a double array...
		double[] weightsDouble = new double[nWeights];
		double[] biasesDouble = new double[(int)biases.getDimensions()[0]];
		for (int weightIndex = 0; weightIndex < nWeights; weightIndex++) {
			weightsDouble[weightIndex] = weights.getValue(weightIndex,0);
		}
		for (int biasIndex = 0; biasIndex < biasesDouble.length; biasIndex++) {
			biasesDouble[biasIndex] = biases.getValue(biasIndex,0);
		}


		double[] gradientDouble = new double[nWeights + (int)biases.getDimensions()[0] - nInputs];
		MultiFormatMatrix stackedGradient = new MultiFormatMatrix(1,
				new long[] {(nWeights + biases.getDimensions()[0] - nInputs),1});


		double totalError = 0.0; // initialize the total error
		long nCorrect = 0; // initialize the counter for total hit correctly
		getBias[1] = 0;
		getWeight[1] = 0;
		getY[1] = 0;
//		gradientIndices[1] = 0;
		outputExped[nOutputsMinusOne] = 1.0;
		int nInputsPlusNHidden  = nInputs  + nHidden;
		int nInputsTimesNHidden = nInputs  * nHidden;
		int nWeightsPlusNHidden = nWeights + nHidden;
		for (long exampleIndex = 0; exampleIndex < nExamples; exampleIndex++) {
			getX[0] = exampleIndex;
			getY[0] = exampleIndex;
			targetThisExample = (int) targetValues.getValue(getY);
			for (int xIndex = 0; xIndex < nInputs; xIndex++) {
				getX[1] = xIndex;
				xDouble[xIndex] = explanatoryVariables.getValue(getX);
			}

			hiddenDeltas = new double[nHidden];
			outputDeltas = new double[nOutputs];

			// initialize output sums with bias value (last bias/neuron is the output)
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				outputSums[outputIndex] = biasesDouble[nInputsPlusNHidden + outputIndex];
			}
			outputSums[nOutputsMinusOne] = 0.0;

			// supposing there are hidden neurons
			for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
				// initialize with bias value
				internalSumsForHidden[hiddenIndex] = biasesDouble[nInputs + hiddenIndex];

				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					internalSumsForHidden[hiddenIndex] += weightsDouble[hiddenIndex + nHidden * inputIndex]
					                                                    * xDouble[inputIndex];

				}

				if (internalSumsForHidden[hiddenIndex] < minSumAllowed) {
					internalSumsForHidden[hiddenIndex] = minSumAllowed;
					expedThings[hiddenIndex] = Double.MAX_VALUE;
				} else {
					expedThings[hiddenIndex] = MatrixOperations.altExp(-internalSumsForHidden[hiddenIndex]);
					lambdaHidden[hiddenIndex] = 1 / (1 + expedThings[hiddenIndex]);
				}

			}

			// if there aren't, just connect inputs directly to outputs
			if (nHidden == 0) {
				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[inputIndex * nOutputsMinusOne + outputIndex]
						                                         * xDouble[inputIndex];
					}
				}
			} else {
				// build up the y output values
				for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex + outputIndex]
						                                         / (1.0 + expedThings[hiddenIndex]);
					}
				}

			}

			// find the probabilities
			sumOutputsExped = 1.0; // Beware the MAGIC INITIALIZATION!!! this is for
			// the standardized output
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				// initialize output value with bias value (last bias/neuron is the
				// output)
				outputExped[outputIndex] = MatrixOperations.altExp(outputSums[outputIndex]);
				sumOutputsExped += outputExped[outputIndex];
			}
			highestOutputIndex = -1;
			highestOutputProbability = Double.NEGATIVE_INFINITY;
			for (int outputIndex = 0; outputIndex < nOutputs; outputIndex++) {
				outputProbabilities[outputIndex] = outputExped[outputIndex] / sumOutputsExped;
				if (outputProbabilities[outputIndex] > highestOutputProbability) {
					highestOutputIndex = outputIndex;
					highestOutputProbability = outputProbabilities[outputIndex];
				}
			}

			// check if the highest probability matches the target
			if (highestOutputIndex == targetThisExample) {
				nCorrect++;
			}

			// //////////////////
			// delta attempts //
			// //////////////////

			// final deltas; with the precision problem
			for (int outputIndex = 0; outputIndex < nOutputs; outputIndex++) {
				if (targetThisExample == outputIndex) {
					// This is the realized outcome...

					outputDeltas[outputIndex] = outputProbabilities[outputIndex] - 1.0;
					totalError -= MatrixOperations.altLn(outputProbabilities[outputIndex]);

				} else { // This outcome was not realized...

					outputDeltas[outputIndex] = outputProbabilities[outputIndex];
				}
			} // for outputIndex

			// hidden deltas; with the precision problem
			for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
				//delta_this = lambda_this * (1 - lambda_this) * sum_next ( delta_next * w_this/next )
				// build up the part before the multiplier
				for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
					hiddenDeltas[hiddenIndex] += outputDeltas[outputIndex] * weightsDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex + outputIndex];
				}

				// multiply by the derivative
				hiddenDeltas[hiddenIndex] *= lambdaHidden[hiddenIndex] * (1.0 - lambdaHidden[hiddenIndex]); 
			}

			// accumulate the gradient
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				// gradient for bias for neuron j = delta_j ; i.e., activation_bias == 1
				gradientDouble[nWeightsPlusNHidden + outputIndex] += outputDeltas[outputIndex];
			}

			for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
				// biases for hidden layer
				// gradient for bias for neuron j = delta_j ; i.e., activation_bias == 1
				gradientDouble[nWeights + hiddenIndex] += hiddenDeltas[hiddenIndex];

				// and the weights
				// gradient for weight from i to j = delta_j * activation_i
				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					gradientDouble[hiddenIndex + nHidden * inputIndex] += hiddenDeltas[hiddenIndex] * xDouble[inputIndex];
				}
				for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
					gradientDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex	+ outputIndex] +=
						outputDeltas[outputIndex] * lambdaHidden[hiddenIndex];
				}
			}

			if (nHidden == 0) {
				//delta_this = lambda_this * (1 - lambda_this) * sum_next ( delta_next * w_this/next )
				for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
					for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
						gradientDouble[inputIndex * nOutputsMinusOne + outputIndex] += outputDeltas[outputIndex] * xDouble[inputIndex];
					}
				}
			}

		} // end example index

		Object[] returnObject = new Object[3];
		returnObject[0] = new Double(totalError);
		returnObject[1] = new Double(nCorrect);

		for (int gradientIndex = 0; gradientIndex < gradientDouble.length; gradientIndex++) {
			stackedGradient.setValue(gradientIndex,0, gradientDouble[gradientIndex]);
		}
		returnObject[2] = stackedGradient;

		return returnObject;
	}


	public static MultiFormatMatrix numericalGradient(MultiFormatFloat explanatoryVariables,
			MultiFormatFloat targetValues, MultiFormatMatrix weights,
			MultiFormatMatrix biases, int nHidden, int nOutputsMinusOne, double stepSize) throws Exception {


//		int nRows = (int)explanatoryVariables.getDimensions()[0];
		int nXs = (int)explanatoryVariables.getDimensions()[1];
		int nWeights = (int)weights.getDimensions()[0];
		int nBiases  = (int)biases.getDimensions()[0] - nXs;
		int nParameters = nWeights + nBiases;

		MultiFormatMatrix numericalGradient = new MultiFormatMatrix(1,new long[] {nParameters,1});

		double errorUp = Double.POSITIVE_INFINITY;
		double errorDown = Double.NEGATIVE_INFINITY;

		double gradientElement = -3;

		MultiFormatMatrix weightsToUse = null;
		MultiFormatMatrix biasesToUse = null;

		long magicTimeInterval = 1500;
		long previousTime = 0;
		long currentTime = -5;
		for (int weightIndex = 0; weightIndex < nWeights; weightIndex++) {
			currentTime = System.currentTimeMillis();
			if (currentTime - previousTime > magicTimeInterval) {
				System.out.println("W: " + (weightIndex + 1) + "/" + nWeights + " at " + new Date());
				previousTime = currentTime;
			}
			weightsToUse = MatrixOperations.newCopy(weights);
			weightsToUse.setValue(weightIndex,0, weights.getValue(weightIndex,0) + stepSize);
			errorUp = NNHelperMethods.findErrorSingleHidden(explanatoryVariables, targetValues,
					weightsToUse, biases,
					nHidden, nOutputsMinusOne)[0];
			weightsToUse.setValue(weightIndex,0, weights.getValue(weightIndex,0) - stepSize);
			errorDown = NNHelperMethods.findErrorSingleHidden(explanatoryVariables, targetValues,
					weightsToUse, biases,
					nHidden, nOutputsMinusOne)[0];

			gradientElement = (errorUp - errorDown) / (2 * stepSize);
//			System.out.println("error diff = " + (errorUp - errorDown) + " total step = " + (2 * stepSize) + " errorUp = " + errorUp);
			numericalGradient.setValue(weightIndex,0, gradientElement);
		}

		for (int biasIndex = 0; biasIndex < nBiases; biasIndex++) {
			currentTime = System.currentTimeMillis();
			if (currentTime - previousTime > magicTimeInterval) {
				System.out.println("B: " + (biasIndex + 1) + "/" + nBiases + " at " + new Date());
				previousTime = currentTime;
			}
			biasesToUse = MatrixOperations.newCopy(biases);
			biasesToUse.setValue(biasIndex + nXs,0, biases.getValue(biasIndex + nXs,0) + stepSize);
			errorUp = NNHelperMethods.findErrorSingleHidden(explanatoryVariables, targetValues,
					weights, biasesToUse,
					nHidden, nOutputsMinusOne)[0];
			biasesToUse.setValue(biasIndex + nXs,0, biases.getValue(biasIndex + nXs,0) - stepSize);
			errorDown = NNHelperMethods.findErrorSingleHidden(explanatoryVariables, targetValues,
					weights, biasesToUse,
					nHidden, nOutputsMinusOne)[0];

			gradientElement = (errorUp - errorDown) / (2 * stepSize);
//			System.out.println("error diff = " + (errorUp - errorDown) + " total step = " + (2 * stepSize) + " errorUp = " + errorUp);
			numericalGradient.setValue(nWeights + biasIndex,0, gradientElement);
		}


		return numericalGradient;
	}



	public static double[] findErrorSingleHiddenOriginal(MultiFormatFloat explanatoryVariables,
			MultiFormatFloat targetValues, MultiFormatMatrix weights,
			MultiFormatMatrix biases, int nHidden, int nOutputsMinusOne)
	throws Exception {


		// a magic number
		double minSumAllowed = -Math.log(Double.MAX_VALUE); // This actually needs
		// to be what it is...

		// constants derived from the data...
		// int nNeurons = (int)biases.getDimensions()[0];
		long nExamples = explanatoryVariables.getDimensions()[0];
		int nInputs = (int) explanatoryVariables.getDimensions()[1];
		int nWeights = (int) weights.getDimensions()[0];

		int nOutputs = nOutputsMinusOne + 1;

		int targetThisExample = -1;

		// declare useful variables
		// double expedThing = Double.NaN;

		long[] getBias = new long[2];
		long[] getWeight = new long[2];
		long[] getX = new long[2];
		long[] getY = new long[2];


		double[] xDouble = new double[nInputs];
		double[] expedThings = new double[nHidden];
		double[] internalSumsForHidden = new double[nHidden];
		double[] lambdaHidden = new double[nHidden];

		double[] outputSums = new double[nOutputs];
		double[] outputExped = new double[nOutputs];
		double sumOutputsExped = Double.NaN;
		double[] outputProbabilities = new double[nOutputs];

		int highestOutputIndex = -2;
		double highestOutputProbability = Double.NEGATIVE_INFINITY;

		// copy the weights and biases to a double array...
		double[] weightsDouble = new double[nWeights];
		double[] biasesDouble = new double[(int)biases.getDimensions()[0]];
		for (int weightIndex = 0; weightIndex < nWeights; weightIndex++) {
			weightsDouble[weightIndex] = weights.getValue(weightIndex,0);
		}
		for (int biasIndex = 0; biasIndex < biasesDouble.length; biasIndex++) {
			biasesDouble[biasIndex] = biases.getValue(biasIndex,0);
		}


		double totalError = 0.0; // initialize the total error
		long nCorrect = 0; // initialize the counter for total hit correctly
		getBias[1] = 0;
		getWeight[1] = 0;
		getY[1] = 0;
		outputExped[nOutputsMinusOne] = 1.0;
		int nInputsPlusNHidden  = nInputs  + nHidden;
		int nInputsTimesNHidden = nInputs  * nHidden;
		for (long exampleIndex = 0; exampleIndex < nExamples; exampleIndex++) {
			getX[0] = exampleIndex;
			getY[0] = exampleIndex;
			targetThisExample = (int) targetValues.getValue(getY);
			for (int xIndex = 0; xIndex < nInputs; xIndex++) {
				getX[1] = xIndex;
				xDouble[xIndex] = explanatoryVariables.getValue(getX);
			}

			// initialize output sums with bias value (last bias/neuron is the output)
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				outputSums[outputIndex] = biasesDouble[nInputsPlusNHidden + outputIndex];
			}
			outputSums[nOutputsMinusOne] = 0.0;

			// supposing there are hidden neurons
			for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
				// initialize with bias value
				internalSumsForHidden[hiddenIndex] = biasesDouble[nInputs + hiddenIndex];

				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					internalSumsForHidden[hiddenIndex] += weightsDouble[hiddenIndex + nHidden * inputIndex]
					                                                    * xDouble[inputIndex];

				}

				if (internalSumsForHidden[hiddenIndex] < minSumAllowed) {
					internalSumsForHidden[hiddenIndex] = minSumAllowed;
					expedThings[hiddenIndex] = Double.MAX_VALUE;
				} else {
					expedThings[hiddenIndex] = java.lang.Math
					.exp(-internalSumsForHidden[hiddenIndex]);
					lambdaHidden[hiddenIndex] = 1 / (1 + expedThings[hiddenIndex]);
				}

			}

			// if there aren't, just connect inputs directly to outputs
			if (nHidden == 0) {
				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[inputIndex * nOutputsMinusOne + outputIndex]
						                                         * xDouble[inputIndex];
					}
				}
			} else {
				// build up the y output values
				for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex + outputIndex]
						                                         / (1.0 + expedThings[hiddenIndex]);
					}
				}

			}

			// find the probabilities
			sumOutputsExped = 1.0; // Beware the MAGIC INITIALIZATION!!! this is for
			// the standardized output
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				// initialize output value with bias value (last bias/neuron is the
				// output)
				outputExped[outputIndex] = Math.exp(outputSums[outputIndex]);
				sumOutputsExped += outputExped[outputIndex];
			}
			highestOutputIndex = -1;
			highestOutputProbability = Double.NEGATIVE_INFINITY;
			for (int outputIndex = 0; outputIndex < nOutputs; outputIndex++) {
				outputProbabilities[outputIndex] = outputExped[outputIndex] / sumOutputsExped;
				if (outputProbabilities[outputIndex] > highestOutputProbability) {
					highestOutputIndex = outputIndex;
					highestOutputProbability = outputProbabilities[outputIndex];
				}
			}

			// check if the highest probability matches the target
			if (highestOutputIndex == targetThisExample) {
				nCorrect++;
			}

			totalError -= Math.log(outputProbabilities[targetThisExample]);

//			for (int outputIndex = 0; outputIndex < nOutputs; outputIndex++) {
//			if (targetThisExample == outputIndex) {
//			// This is the realized outcome...

//			totalError -= Math.log(outputProbabilities[outputIndex]);

//			}			
//			} // for outputIndex

		} // end example index

		double[] returnObject = new double[] {totalError , nCorrect};

		return returnObject;
	}

	public static void predictSingleHiddenToFile(MultiFormatFloat explanatoryVariables,
			boolean hasTargetsColumn, MultiFormatMatrix weights,
			MultiFormatMatrix biases, int nHidden, int nOutputsMinusOne, String fileBaseName, String delimiter,
			int nDecimals)
	throws Exception {

		System.out.println("predictSingleHiddenToFile trying for " + nDecimals + " decimal places.");
		MultiFormatMatrix[] predictionPair = predictSingleHidden(explanatoryVariables, hasTargetsColumn, 
				weights, biases,  nHidden,  nOutputsMinusOne);

		MatrixOperations.write2DMFMtoTextAsInt(predictionPair[0], fileBaseName + "_C", delimiter);
//		MatrixOperations.write2DMFMtoTextDecimals(predictionPair[1], fileBaseName + "_P", delimiter, nDecimals);
		MatrixOperations.write2DMFMtoTextDecimalsAlt(predictionPair[1], fileBaseName + "_P", delimiter, nDecimals);
	}

	public static MultiFormatMatrix[] predictSingleHidden(MultiFormatFloat explanatoryVariables,
			boolean hasTargetsColumn, MultiFormatMatrix weights,
			MultiFormatMatrix biases, int nHidden, int nOutputsMinusOne)
	throws Exception {



		// constants derived from the data...
		// int nNeurons = (int)biases.getDimensions()[0];

		long nExamples = explanatoryVariables.getDimensions()[0];
		
		long magicCheckNumber = nExamples / 100; // Beware the MAGIC NUMBER!!! update every 1%
		
		int  nInputs   = -1;
		if (hasTargetsColumn) {
			nInputs   = (int) explanatoryVariables.getDimensions()[1] - 1;
		} else {
			nInputs   = (int) explanatoryVariables.getDimensions()[1];
		}

		int  nWeights  = (int) weights.getDimensions()[0];

		int nOutputs = nOutputsMinusOne + 1;

//		int targetThisExample = -1;

		// declare useful variables
		// double expedThing = Double.NaN;

		int formatForOutputs = 3; // Beware the MAGIC NUMBER!!! on disk format
		MultiFormatMatrix winningCategory  = new MultiFormatMatrix(formatForOutputs, new long[]{nExamples,1});
		MultiFormatMatrix allProbabilities = new MultiFormatMatrix(formatForOutputs, new long[]{nExamples,nOutputs});

		long[] getBias = new long[2];
		long[] getWeight = new long[2];
		long[] getX = new long[2];
		long[] getY = new long[2];


		double[] xDouble = new double[nInputs];
		double[] expedThings = new double[nHidden];
		double[] internalSumsForHidden = new double[nHidden];
		double[] lambdaHidden = new double[nHidden];
		double[] lambdaHiddenComplements = new double[nHidden];

		double[] outputSums = new double[nOutputs];
		double[] outputExped = new double[nOutputs];
		double[] outputProbabilities = new double[nOutputs];

		double[] centeredOutputSums = new double[nOutputs];
		double[] expedCenteredOutputSums = new double[nOutputs];

		double emm = Double.NaN;
		int emmIndex = Integer.MIN_VALUE;
		double capitalCue = Double.NEGATIVE_INFINITY;
		double finalTemp = Double.NaN;
		double alternateSumTemp = Double.POSITIVE_INFINITY;

		double finalStandardized = Double.NaN;
		double alternateStandardized = -1232145.231553;

		// copy the weights and biases to a double array...
		double[] weightsDouble = new double[nWeights];
		double[] biasesDouble = new double[(int)biases.getDimensions()[0]];
		for (int weightIndex = 0; weightIndex < nWeights; weightIndex++) {
			weightsDouble[weightIndex] = weights.getValue(weightIndex,0);
		}
		for (int biasIndex = 0; biasIndex < biasesDouble.length; biasIndex++) {
			biasesDouble[biasIndex] = biases.getValue(biasIndex,0);
		}


		getBias[1] = 0;
		getWeight[1] = 0;
		getY[1] = 0;
		outputExped[nOutputsMinusOne] = 1.0;
		int nInputsPlusNHidden  = nInputs  + nHidden;
		int nInputsTimesNHidden = nInputs  * nHidden;
		
		for (long exampleIndex = 0; exampleIndex < nExamples; exampleIndex++) {		
			getX[0] = exampleIndex;
			getY[0] = exampleIndex;
			for (int xIndex = 0; xIndex < nInputs; xIndex++) {
				getX[1] = xIndex;
				xDouble[xIndex] = explanatoryVariables.getValue(getX);
			}

			// initialize output sums with bias value (last bias/neuron is the output)
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				outputSums[outputIndex] = biasesDouble[nInputsPlusNHidden + outputIndex];
			}
			outputSums[nOutputsMinusOne] = 0.0;

			// supposing there are hidden neurons
			for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
				// initialize with bias value
				internalSumsForHidden[hiddenIndex] = biasesDouble[nInputs + hiddenIndex];

				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					internalSumsForHidden[hiddenIndex] += weightsDouble[hiddenIndex + nHidden * inputIndex]
					                                                    * xDouble[inputIndex];

				}

				expedThings[hiddenIndex] = java.lang.Math
				.exp(-internalSumsForHidden[hiddenIndex]);
				lambdaHidden[hiddenIndex] = 1.0 / (1.0 + expedThings[hiddenIndex]);
				lambdaHiddenComplements[hiddenIndex] = expedThings[hiddenIndex] / (1 + expedThings[hiddenIndex]);

			}

			// if there aren't, just connect inputs directly to outputs
			if (nHidden == 0) {
				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[inputIndex * nOutputsMinusOne + outputIndex]
						                                         * xDouble[inputIndex];
					}
				}
			} else {
				// build up the y output values
				for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex + outputIndex]
						                                         / (1.0 + expedThings[hiddenIndex]);
					}
				}

			}


			emm = 0.0; // initialized the thing to find the maximum internal activation among the outputs;
			emmIndex = nOutputsMinusOne;
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				if (outputSums[outputIndex] > emm) {
					emm = outputSums[outputIndex];
					emmIndex = outputIndex;
				}
			}

			// doing exp(0) = 1 manually...
			if (emmIndex == nOutputsMinusOne) {
				capitalCue = 1;
			} else {
				capitalCue = Math.exp(-emm); // don't forget the standardized output which is always zero: exp(0) = 1
			}


			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				centeredOutputSums[outputIndex] = outputSums[outputIndex] - emm;

				if (outputIndex == emmIndex) {
					expedCenteredOutputSums[outputIndex] = 1.0;
				} else {
					expedCenteredOutputSums[outputIndex] = Math.exp(centeredOutputSums[outputIndex]);
				}

				capitalCue += expedCenteredOutputSums[outputIndex];
			}

			// write down the final results...
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				finalTemp = expedCenteredOutputSums[outputIndex] / capitalCue;

				if (finalTemp <= 0.5) {
					outputProbabilities[outputIndex] = finalTemp;
				}  else {
					// initialize an alternate sum...
					if (emmIndex == nOutputsMinusOne) {
						alternateSumTemp = 0;
					} else {
						alternateSumTemp = Math.exp(-emm);
					}
					for (int outputIndexAlt = 0; outputIndexAlt < nOutputsMinusOne; outputIndexAlt++) {
						if (outputIndexAlt != outputIndex) {
							alternateSumTemp += expedCenteredOutputSums[outputIndexAlt];
						}
					}
					alternateSumTemp /= capitalCue;

					outputProbabilities[outputIndex] = 1.0 - alternateSumTemp;

				}
			}
			// do the final/standardize output (it gets no deltas, remember... so in that sense, it is irrelevant...)

			// we don't really care about this unless we need it for the error function...

			finalStandardized = Math.exp(-emm) / capitalCue;
			if (finalStandardized <= 0.5) {
				outputProbabilities[nOutputsMinusOne] = finalStandardized;
			} else {
				alternateStandardized = 0;
				for (int outputIndexAlt = 0; outputIndexAlt < nOutputsMinusOne; outputIndexAlt++) {
					alternateStandardized += expedCenteredOutputSums[outputIndexAlt];
				}
				alternateStandardized /= capitalCue;

				outputProbabilities[nOutputsMinusOne] = 1.0 - alternateStandardized;
			}


			winningCategory.setValue(exampleIndex,0, emmIndex);

			for (int outputIndex = 0; outputIndex < nOutputs; outputIndex++) {
				allProbabilities.setValue(exampleIndex,outputIndex, outputProbabilities[outputIndex]);
			}

			// notify about progress
			if (exampleIndex % magicCheckNumber == 0) {
				System.out.print("[" + (100 * exampleIndex) / nExamples + "%]");
			}


		} // end example index

		// artificially set these to not be garbage collected so they can get finalized without disappearing
		winningCategory.setGarbageCollectionMode(false);
		allProbabilities.setGarbageCollectionMode(false);
		
		winningCategory.finishRecordingMatrix();
		allProbabilities.finishRecordingMatrix();

		// reset them to garbage collect-ible
		winningCategory.setGarbageCollectionMode(true);
		allProbabilities.setGarbageCollectionMode(true);
		
		System.out.println();

		return new MultiFormatMatrix[] {winningCategory,allProbabilities};

	}



	public static double[] findErrorSingleHidden(MultiFormatFloat explanatoryVariables,
			MultiFormatFloat targetValues, MultiFormatMatrix weights,
			MultiFormatMatrix biases, int nHidden, int nOutputsMinusOne)
			throws Exception {


		
		// constants derived from the data...
		// int nNeurons = (int)biases.getDimensions()[0];
		long nExamples = explanatoryVariables.getDimensions()[0];
		int  nInputs   = (int) explanatoryVariables.getDimensions()[1];
		int  nWeights  = (int) weights.getDimensions()[0];

		int nOutputs = nOutputsMinusOne + 1;

		int targetThisExample = -1;

		// declare useful variables
		// double expedThing = Double.NaN;
		
		long[] getBias = new long[2];
		long[] getWeight = new long[2];
		long[] getX = new long[2];
		long[] getY = new long[2];
		
		
		double[] xDouble = new double[nInputs];
		double[] expedThings = new double[nHidden];
		double[] internalSumsForHidden = new double[nHidden];
		double[] lambdaHidden = new double[nHidden];
		double[] lambdaHiddenComplements = new double[nHidden];
		
		double[] outputSums = new double[nOutputs];
		double[] outputExped = new double[nOutputs];
//		double sumOutputsExped = Double.NaN;
		double[] outputProbabilities = new double[nOutputs];
		
		double[] centeredOutputSums = new double[nOutputs];
		double[] expedCenteredOutputSums = new double[nOutputs];
//		double[] outputExpedSums = new double[nOutputs];
		
//		double[] outputProbabilitiesAlt = new double[nOutputs];

//		double[] hiddenDeltas = new double[nHidden];
//		double[] outputDeltas = new double[nOutputs];

//		int highestOutputIndex = -2;
//		double highestOutputProbability = Double.NEGATIVE_INFINITY;
				
		double emm = Double.NaN;
		int emmIndex = Integer.MIN_VALUE;
		double capitalCue = Double.NEGATIVE_INFINITY;
		double finalTemp = Double.NaN;
		double alternateSumTemp = Double.POSITIVE_INFINITY;

		double finalStandardized = Double.NaN;
		double alternateStandardized = -1232145.231553;

//		double toleranceBetweenPs = 0.001;

//		boolean mismatch = false;

		
		
		// copy the weights and biases to a double array...
		double[] weightsDouble = new double[nWeights];
		double[] biasesDouble = new double[(int)biases.getDimensions()[0]];
		for (int weightIndex = 0; weightIndex < nWeights; weightIndex++) {
			weightsDouble[weightIndex] = weights.getValue(weightIndex,0);
		}
		for (int biasIndex = 0; biasIndex < biasesDouble.length; biasIndex++) {
			biasesDouble[biasIndex] = biases.getValue(biasIndex,0);
		}
		

		double totalError = 0.0; // initialize the total error
		long nCorrect = 0; // initialize the counter for total hit correctly
		getBias[1] = 0;
		getWeight[1] = 0;
		getY[1] = 0;
//		gradientIndices[1] = 0;
		outputExped[nOutputsMinusOne] = 1.0;
		int nInputsPlusNHidden  = nInputs  + nHidden;
		int nInputsTimesNHidden = nInputs  * nHidden;
//		int nWeightsPlusNHidden = nWeights + nHidden;
		for (long exampleIndex = 0; exampleIndex < nExamples; exampleIndex++) {		
			getX[0] = exampleIndex;
			getY[0] = exampleIndex;
			targetThisExample = (int) targetValues.getValue(getY);
			for (int xIndex = 0; xIndex < nInputs; xIndex++) {
				getX[1] = xIndex;
				xDouble[xIndex] = explanatoryVariables.getValue(getX);
			}

			// initialize output sums with bias value (last bias/neuron is the output)
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				outputSums[outputIndex] = biasesDouble[nInputsPlusNHidden + outputIndex];
			}
			outputSums[nOutputsMinusOne] = 0.0;

			// supposing there are hidden neurons
			for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
				// initialize with bias value
				internalSumsForHidden[hiddenIndex] = biasesDouble[nInputs + hiddenIndex];

				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					internalSumsForHidden[hiddenIndex] += weightsDouble[hiddenIndex + nHidden * inputIndex]
					                                                    * xDouble[inputIndex];
					
				}

					expedThings[hiddenIndex] = java.lang.Math
							.exp(-internalSumsForHidden[hiddenIndex]);
					lambdaHidden[hiddenIndex] = 1.0 / (1.0 + expedThings[hiddenIndex]);
					lambdaHiddenComplements[hiddenIndex] = expedThings[hiddenIndex] / (1 + expedThings[hiddenIndex]);

			}

			// if there aren't, just connect inputs directly to outputs
			if (nHidden == 0) {
				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[inputIndex * nOutputsMinusOne + outputIndex]
						                                         * xDouble[inputIndex];
					}
				}
			} else {
				// build up the y output values
				for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex + outputIndex]
						                                         / (1.0 + expedThings[hiddenIndex]);
					}
				}

			}

//			emm = 0; // initialized the thing to find the maximum internal activation among the outputs;
//			emmIndex = nOutputsMinusOne ; // nOutputs - 1 or nOutputs???

			// find the probabilities
//			sumOutputsExped = 1.0; // Beware the MAGIC INITIALIZATION!!! this is for
//															// the standardized output

			emm = 0.0;
			emmIndex = nOutputsMinusOne;
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				if (outputSums[outputIndex] > emm) {
					emm = outputSums[outputIndex];
					emmIndex = outputIndex;
				}
			}

			// doing exp(0) = 1 manually...
      if (emmIndex == nOutputsMinusOne) {
        capitalCue = 1;
      } else {
        capitalCue = Math.exp(-emm); // don't forget the standardized output which is always zero: exp(0) = 1
      }

			// check if the highest probability matches the target
			if (emmIndex == targetThisExample) {
				nCorrect++;
			}

//			expedCenteredOutputSums

			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				centeredOutputSums[outputIndex] = outputSums[outputIndex] - emm;
				
				if (outputIndex == emmIndex) {
					expedCenteredOutputSums[outputIndex] = 1.0;
				} else {
					expedCenteredOutputSums[outputIndex] = Math.exp(centeredOutputSums[outputIndex]);
				}
				
				capitalCue += expedCenteredOutputSums[outputIndex];
			}
			
			// write down the final results...
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				finalTemp = expedCenteredOutputSums[outputIndex] / capitalCue;
				
//				if (outputIndex == nOutputsMinusOne) {
//					mismatch = false; // just do something so we can break on it...
//				}
				
				if (finalTemp <= 0.5) {
					if (targetThisExample == outputIndex) {
						totalError -= Math.log(finalTemp);
					}
					outputProbabilities[outputIndex] = finalTemp;
				}  else {
					// initialize an alternate sum...
					if (emmIndex == nOutputsMinusOne) {
						alternateSumTemp = 0;
					} else {
						alternateSumTemp = Math.exp(-emm);
					}
					for (int outputIndexAlt = 0; outputIndexAlt < nOutputsMinusOne; outputIndexAlt++) {
						if (outputIndexAlt != outputIndex) {
							alternateSumTemp += expedCenteredOutputSums[outputIndexAlt];
						}
					}
					alternateSumTemp /= capitalCue;

					outputProbabilities[outputIndex] = 1.0 - alternateSumTemp;

					if (targetThisExample == outputIndex) {
						totalError -= Math.log1p(-alternateSumTemp);
//						totalError -= Math.log(1.0 - alternateSumTemp);
					}
					
				}
			}
			// do the final/standardize output (it gets no deltas, remember... so in that sense, it is irrelevant...)

			// we don't really care about this unless we need it for the error function...
			if (targetThisExample == nOutputsMinusOne) {
				finalStandardized = Math.exp(-emm) / capitalCue;
				if (finalStandardized <= 0.5) {
					if (targetThisExample == nOutputsMinusOne) {
						totalError -= Math.log(finalStandardized);
					}
					outputProbabilities[nOutputsMinusOne] = finalStandardized;
				} else {
					alternateStandardized = 0;
					for (int outputIndexAlt = 0; outputIndexAlt < nOutputsMinusOne; outputIndexAlt++) {
						alternateStandardized += expedCenteredOutputSums[outputIndexAlt];
					}
					alternateStandardized /= capitalCue;

					outputProbabilities[nOutputsMinusOne] = 1.0 - alternateStandardized;
					if (targetThisExample == nOutputsMinusOne) {
						totalError -= Math.log1p(-alternateStandardized);
//						totalError -= Math.log(1.0-alternateStandardized);
					}
				}
			}
			
			
		} // end example index

		
		
		return new double[] {totalError , nCorrect};
		
	}
	

	public static double[] findErrorSingleHiddenWeighted(MultiFormatFloat explanatoryVariables,
			MultiFormatFloat targetValues, MultiFormatMatrix weights,
			MultiFormatMatrix biases, int nHidden, int nOutputsMinusOne)
			throws Exception {


		
		// constants derived from the data...
		// int nNeurons = (int)biases.getDimensions()[0];
		long nExamples = explanatoryVariables.getDimensions()[0];
		int  nInputs   = (int) explanatoryVariables.getDimensions()[1];
		int  nWeights  = (int) weights.getDimensions()[0];

		int nOutputs = nOutputsMinusOne + 1;

		int targetThisExample = -1;
		double importanceThisExample = -1;

		// declare useful variables
		// double expedThing = Double.NaN;
		
		long[] getBias = new long[2];
		long[] getWeight = new long[2];
		long[] getX = new long[2];
		long[] getY = new long[2];
		long[] getImportance = new long[2];
		
		
		double[] xDouble = new double[nInputs];
		double[] expedThings = new double[nHidden];
		double[] internalSumsForHidden = new double[nHidden];
		double[] lambdaHidden = new double[nHidden];
		double[] lambdaHiddenComplements = new double[nHidden];
		
		double[] outputSums = new double[nOutputs];
		double[] outputExped = new double[nOutputs];
//		double sumOutputsExped = Double.NaN;
		double[] outputProbabilities = new double[nOutputs];
		
		double[] centeredOutputSums = new double[nOutputs];
		double[] expedCenteredOutputSums = new double[nOutputs];
//		double[] outputExpedSums = new double[nOutputs];
		
//		double[] outputProbabilitiesAlt = new double[nOutputs];

//		double[] hiddenDeltas = new double[nHidden];
//		double[] outputDeltas = new double[nOutputs];

//		int highestOutputIndex = -2;
//		double highestOutputProbability = Double.NEGATIVE_INFINITY;
				
		double emm = Double.NaN;
		int emmIndex = Integer.MIN_VALUE;
		double capitalCue = Double.NEGATIVE_INFINITY;
		double finalTemp = Double.NaN;
		double alternateSumTemp = Double.POSITIVE_INFINITY;

		double finalStandardized = Double.NaN;
		double alternateStandardized = -1232145.231553;

//		double toleranceBetweenPs = 0.001;

//		boolean mismatch = false;

		
		
		// copy the weights and biases to a double array...
		double[] weightsDouble = new double[nWeights];
		double[] biasesDouble = new double[(int)biases.getDimensions()[0]];
		for (int weightIndex = 0; weightIndex < nWeights; weightIndex++) {
			weightsDouble[weightIndex] = weights.getValue(weightIndex,0);
		}
		for (int biasIndex = 0; biasIndex < biasesDouble.length; biasIndex++) {
			biasesDouble[biasIndex] = biases.getValue(biasIndex,0);
		}
		

		double totalError = 0.0; // initialize the total error
//		long nCorrect = 0; // initialize the counter for total hit correctly
		double nCorrectImportance = 0.0; // initialize the counter for total hit correctly
		getBias[1] = 0;
		getWeight[1] = 0;
		getY[1] = 1; // the target is now the second column and the importance weight is the first column
		getImportance[1] = 0;
//		gradientIndices[1] = 0;
		outputExped[nOutputsMinusOne] = 1.0;
		int nInputsPlusNHidden  = nInputs  + nHidden;
		int nInputsTimesNHidden = nInputs  * nHidden;
//		int nWeightsPlusNHidden = nWeights + nHidden;
		for (long exampleIndex = 0; exampleIndex < nExamples; exampleIndex++) {		
			getX[0] = exampleIndex;
			getY[0] = exampleIndex;
			getImportance[0] = exampleIndex;
			targetThisExample = (int) targetValues.getValue(getY);
			importanceThisExample = targetValues.getValue(getImportance);
//			if (importanceThisExample != 1.0) {
//				System.out.println("exampleIndex = " + exampleIndex + " has a non-unitary importance of: " + importanceThisExample + "; target = " + targetThisExample);
//				throw new Exception();
//			}
			for (int xIndex = 0; xIndex < nInputs; xIndex++) {
				getX[1] = xIndex;
				xDouble[xIndex] = explanatoryVariables.getValue(getX);
			}

			// initialize output sums with bias value (last bias/neuron is the output)
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				outputSums[outputIndex] = biasesDouble[nInputsPlusNHidden + outputIndex];
			}
			outputSums[nOutputsMinusOne] = 0.0;

			// supposing there are hidden neurons
			for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
				// initialize with bias value
				internalSumsForHidden[hiddenIndex] = biasesDouble[nInputs + hiddenIndex];

				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					internalSumsForHidden[hiddenIndex] += weightsDouble[hiddenIndex + nHidden * inputIndex]
					                                                    * xDouble[inputIndex];
					
				}

					expedThings[hiddenIndex] = java.lang.Math
							.exp(-internalSumsForHidden[hiddenIndex]);
					lambdaHidden[hiddenIndex] = 1.0 / (1.0 + expedThings[hiddenIndex]);
					lambdaHiddenComplements[hiddenIndex] = expedThings[hiddenIndex] / (1 + expedThings[hiddenIndex]);

			}

			// if there aren't, just connect inputs directly to outputs
			if (nHidden == 0) {
				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[inputIndex * nOutputsMinusOne + outputIndex]
						                                         * xDouble[inputIndex];
					}
				}
			} else {
				// build up the y output values
				for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex + outputIndex]
						                                         / (1.0 + expedThings[hiddenIndex]);
					}
				}

			}

//			emm = 0; // initialized the thing to find the maximum internal activation among the outputs;
//			emmIndex = nOutputsMinusOne ; // nOutputs - 1 or nOutputs???

			// find the probabilities
//			sumOutputsExped = 1.0; // Beware the MAGIC INITIALIZATION!!! this is for
//															// the standardized output

			emm = 0.0;
			emmIndex = nOutputsMinusOne;
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				if (outputSums[outputIndex] > emm) {
					emm = outputSums[outputIndex];
					emmIndex = outputIndex;
				}
			}

			// doing exp(0) = 1 manually...
      if (emmIndex == nOutputsMinusOne) {
        capitalCue = 1;
      } else {
        capitalCue = Math.exp(-emm); // don't forget the standardized output which is always zero: exp(0) = 1
      }

			// check if the highest probability matches the target
			if (emmIndex == targetThisExample) {
				nCorrectImportance += importanceThisExample;
//				nCorrect++;
			}

//			expedCenteredOutputSums

			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				centeredOutputSums[outputIndex] = outputSums[outputIndex] - emm;
				
				if (outputIndex == emmIndex) {
					expedCenteredOutputSums[outputIndex] = 1.0;
				} else {
					expedCenteredOutputSums[outputIndex] = Math.exp(centeredOutputSums[outputIndex]);
				}
				
				capitalCue += expedCenteredOutputSums[outputIndex];
			}
			
			// write down the final results...
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				finalTemp = expedCenteredOutputSums[outputIndex] / capitalCue;
				
//				if (outputIndex == nOutputsMinusOne) {
//					mismatch = false; // just do something so we can break on it...
//				}
				
				if (finalTemp <= 0.5) {
					if (targetThisExample == outputIndex) {
//						totalError -= Math.log(finalTemp);
						totalError -= importanceThisExample * Math.log(finalTemp);
					}
					outputProbabilities[outputIndex] = finalTemp;
				}  else {
					// initialize an alternate sum...
					if (emmIndex == nOutputsMinusOne) {
						alternateSumTemp = 0;
					} else {
						alternateSumTemp = Math.exp(-emm);
					}
					for (int outputIndexAlt = 0; outputIndexAlt < nOutputsMinusOne; outputIndexAlt++) {
						if (outputIndexAlt != outputIndex) {
							alternateSumTemp += expedCenteredOutputSums[outputIndexAlt];
						}
					}
					alternateSumTemp /= capitalCue;

					outputProbabilities[outputIndex] = 1.0 - alternateSumTemp;

					if (targetThisExample == outputIndex) {
						totalError -= importanceThisExample * Math.log1p(-alternateSumTemp);
//						totalError -= importanceThisExample * Math.log(1.0 - alternateSumTemp);
					}
					
				}
			}
			// do the final/standardize output (it gets no deltas, remember... so in that sense, it is irrelevant...)

			// we don't really care about this unless we need it for the error function...
			if (targetThisExample == nOutputsMinusOne) {
				finalStandardized = Math.exp(-emm) / capitalCue;
				if (finalStandardized <= 0.5) {
					if (targetThisExample == nOutputsMinusOne) {
						totalError -= importanceThisExample * Math.log(finalStandardized);
					}
					outputProbabilities[nOutputsMinusOne] = finalStandardized;
				} else {
					alternateStandardized = 0;
					for (int outputIndexAlt = 0; outputIndexAlt < nOutputsMinusOne; outputIndexAlt++) {
						alternateStandardized += expedCenteredOutputSums[outputIndexAlt];
					}
					alternateStandardized /= capitalCue;

					outputProbabilities[nOutputsMinusOne] = 1.0 - alternateStandardized;
					if (targetThisExample == nOutputsMinusOne) {
						totalError -= importanceThisExample * Math.log1p(-alternateStandardized);
//						totalError -= Math.log(1.0-alternateStandardized);
					}
				}
			}
			
			
		} // end example index

		
		
		return new double[] {totalError , nCorrectImportance};
		
	}
	
	

	public static double[] findErrorSingleHiddenZZZ(MultiFormatFloat explanatoryVariables,
			MultiFormatFloat targetValues, MultiFormatMatrix weights,
			MultiFormatMatrix biases, int nHidden, int nOutputsMinusOne)
			throws Exception {
		
		// constants derived from the data...
		// int nNeurons = (int)biases.getDimensions()[0];
		long nExamples = explanatoryVariables.getDimensions()[0];
		int  nInputs   = (int) explanatoryVariables.getDimensions()[1];
		int  nWeights  = (int) weights.getDimensions()[0];

		int nOutputs = nOutputsMinusOne + 1;

		int targetThisExample = -1;

		// declare useful variables
		// double expedThing = Double.NaN;
		
		long[] getBias = new long[2];
		long[] getWeight = new long[2];
		long[] getX = new long[2];
		long[] getY = new long[2];
		
		
		double[] xDouble = new double[nInputs];
		double[] expedThings = new double[nHidden];
		double[] internalSumsForHidden = new double[nHidden];
		
		double[] outputSums = new double[nOutputs];
		double[] outputExped = new double[nOutputs];
		double[] outputProbabilities = new double[nOutputs];
		
		double[] centeredOutputSums = new double[nOutputs];
		double[] expedCenteredOutputSums = new double[nOutputs];
				
		double emm = Double.NaN;
		int emmIndex = Integer.MIN_VALUE;
		double capitalCue = Double.NEGATIVE_INFINITY;
		double finalTemp = Double.NaN;
		double alternateSumTemp = Double.POSITIVE_INFINITY;

		double finalStandardized = Double.NaN;
		double alternateStandardized = -1232145.231553;

		
		
		// copy the weights and biases to a double array...
		double[] weightsDouble = new double[nWeights];
		double[] biasesDouble = new double[(int)biases.getDimensions()[0]];
		for (int weightIndex = 0; weightIndex < nWeights; weightIndex++) {
			weightsDouble[weightIndex] = weights.getValue(weightIndex,0);
		}
		for (int biasIndex = 0; biasIndex < biasesDouble.length; biasIndex++) {
			biasesDouble[biasIndex] = biases.getValue(biasIndex,0);
		}
		
		
		

		double totalError = 0.0; // initialize the total error
		long nCorrect = 0; // initialize the counter for total hit correctly
		getBias[1] = 0;
		getWeight[1] = 0;
		getY[1] = 0;
		outputExped[nOutputsMinusOne] = 1.0;
		int nInputsPlusNHidden  = nInputs  + nHidden;
		int nInputsTimesNHidden = nInputs  * nHidden;
		for (long exampleIndex = 0; exampleIndex < nExamples; exampleIndex++) {		
			getX[0] = exampleIndex;
			getY[0] = exampleIndex;
			targetThisExample = (int) targetValues.getValue(getY);
			for (int xIndex = 0; xIndex < nInputs; xIndex++) {
				getX[1] = xIndex;
				xDouble[xIndex] = explanatoryVariables.getValue(getX);
			}

			// initialize output sums with bias value (last bias/neuron is the output)
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				outputSums[outputIndex] = biasesDouble[nInputsPlusNHidden + outputIndex];
			}
			outputSums[nOutputsMinusOne] = 0.0;

			// supposing there are hidden neurons
			for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
				// initialize with bias value
				internalSumsForHidden[hiddenIndex] = biasesDouble[nInputs + hiddenIndex];

				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					internalSumsForHidden[hiddenIndex] += weightsDouble[hiddenIndex + nHidden * inputIndex]
					                                                    * xDouble[inputIndex];
					
				}

					expedThings[hiddenIndex] = Math.exp(-internalSumsForHidden[hiddenIndex]);
			}

			// if there aren't, just connect inputs directly to outputs
			if (nHidden == 0) {
				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[inputIndex * nOutputsMinusOne + outputIndex]
						                                         * xDouble[inputIndex];
					}
				}
			} else {
				// build up the y output values
				for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex + outputIndex]
						                                         / (1.0 + expedThings[hiddenIndex]);
					}
				}

			}

			emm = 0.0;
			emmIndex = nOutputsMinusOne;
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				if (outputSums[outputIndex] > emm) {
					emm = outputSums[outputIndex];
					emmIndex = outputIndex;
				}
			}

			// doing exp(0) = 1 manually...
      if (emmIndex == nOutputsMinusOne) {
        capitalCue = 1;
      } else {
        capitalCue = Math.exp(-emm); // don't forget the standardized output which is always zero: exp(0) = 1
      }

			// check if the highest probability matches the target
			if (emmIndex == targetThisExample) {
				nCorrect++;
			}

			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				centeredOutputSums[outputIndex] = outputSums[outputIndex] - emm;
				
				if (outputIndex == emmIndex) {
					expedCenteredOutputSums[outputIndex] = 1.0;
				} else {
					expedCenteredOutputSums[outputIndex] = Math.exp(centeredOutputSums[outputIndex]);
				}
				
				capitalCue += expedCenteredOutputSums[outputIndex];
			}
			
			// write down the final results...
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				finalTemp = expedCenteredOutputSums[outputIndex] / capitalCue;

				if (finalTemp <= 0.5) {
					if (targetThisExample == outputIndex) {
						totalError -= Math.log(finalTemp);
					}
					outputProbabilities[outputIndex] = finalTemp;
				}  else {
					// initialize an alternate sum...
					if (emmIndex == nOutputsMinusOne) {
						alternateSumTemp = 0;
					} else {
						alternateSumTemp = Math.exp(-emm);
					}
					for (int outputIndexAlt = 0; outputIndexAlt < nOutputsMinusOne; outputIndexAlt++) {
						if (outputIndexAlt != outputIndex) {
							alternateSumTemp += expedCenteredOutputSums[outputIndexAlt];
						}
					}
					alternateSumTemp /= capitalCue;

					outputProbabilities[outputIndex] = 1.0 - alternateSumTemp;

					if (targetThisExample == outputIndex) {
						totalError -= Math.log1p(-alternateSumTemp);
//						totalError -= Math.log(1.0 - alternateSumTemp);
					}
				}
			}
			// do the final/standardize output (it gets no deltas, remember... so in that sense, it is irrelevant...)

			// we don't really care about this unless we need it for the error function...
			if (targetThisExample == nOutputsMinusOne) {
				finalStandardized = Math.exp(-emm) / capitalCue;
				if (finalStandardized <= 0.5) {
					if (targetThisExample == nOutputsMinusOne) {
						totalError -= Math.log(finalStandardized);
					}
					outputProbabilities[nOutputsMinusOne] = finalStandardized;
				} else {
					alternateStandardized = 0;
					for (int outputIndexAlt = 0; outputIndexAlt < nOutputsMinusOne; outputIndexAlt++) {
						alternateStandardized += expedCenteredOutputSums[outputIndexAlt];
					}
					alternateStandardized /= capitalCue;

					outputProbabilities[nOutputsMinusOne] = 1.0 - alternateStandardized;
					if (targetThisExample == nOutputsMinusOne) {
						totalError -= Math.log1p(-alternateStandardized);
//						totalError -= Math.log(1.0-alternateStandardized);
					}
				}
			}
			


		} // end example index

		double[] returnObject = new double[] {totalError , nCorrect};

		return returnObject;
	}
	


	
	
	
	public static MultiFormatMatrix[] newWeightsBiases(
			MultiFormatMatrix oldWeights, 
			MultiFormatMatrix oldBiases, 
			MultiFormatMatrix stackedStep)
	throws Exception {
	
		long nWeights = oldWeights.getDimensions()[0];
		long nBiases = oldBiases.getDimensions()[0];
		long nGoodBiases = stackedStep.getDimensions()[0] - nWeights;
		long firstGoodBias = nBiases - nGoodBiases;
		
		MultiFormatMatrix[] outputBundle = new MultiFormatMatrix[2];
		outputBundle[0] = new MultiFormatMatrix(oldWeights.getDataFormat(),oldWeights.getDimensions());
		outputBundle[1] = new MultiFormatMatrix(oldBiases.getDataFormat(),oldBiases.getDimensions());
		
		// do the weights, this is easy
		long[] getSetCoords = new long[2];
		getSetCoords[1] = 0;
		for (getSetCoords[0] = 0; getSetCoords[0] < nWeights; getSetCoords[0]++) {
			outputBundle[0].setValue(getSetCoords,
					(oldWeights.getValue(getSetCoords) + stackedStep.getValue(getSetCoords) )
					);
		}
		
		// do the biases, a bit more tricky
		long indexInStep = -1;
		long indexInBiases = -2;
		for (long goodBiasIndex = 0; goodBiasIndex < nGoodBiases ; goodBiasIndex++) {
			  indexInStep = goodBiasIndex + nWeights;
			indexInBiases = goodBiasIndex + firstGoodBias;
			
			outputBundle[1].setValue(indexInBiases,0, 
					(oldBiases.getValue(indexInBiases,0) + stackedStep.getValue(indexInStep,0) )
					);
		}
		
		
		return outputBundle;
	}



	public static double[] findErrorSingleHiddenALT(MultiFormatFloat explanatoryVariables,
			MultiFormatFloat targetValues, MultiFormatMatrix weights,
			MultiFormatMatrix biases, int nHidden, int nOutputsMinusOne)
			throws Exception {

		
		// a magic number
		double minSumAllowed = -Math.log(Double.MAX_VALUE); // This actually needs
																												// to be what it is...

		// constants derived from the data...
		// int nNeurons = (int)biases.getDimensions()[0];
		long nExamples = explanatoryVariables.getDimensions()[0];
		int nInputs = (int) explanatoryVariables.getDimensions()[1];
		int nWeights = (int) weights.getDimensions()[0];

		int nOutputs = nOutputsMinusOne + 1;

		int targetThisExample = -1;

		// declare useful variables
		// double expedThing = Double.NaN;
		
		long[] getBias = new long[2];
		long[] getWeight = new long[2];
		long[] getX = new long[2];
		long[] getY = new long[2];
		
		
		double[] xDouble = new double[nInputs];
		double[] expedThings = new double[nHidden];
		double[] internalSumsForHidden = new double[nHidden];
		double[] lambdaHidden = new double[nHidden];
		
		double[] outputSums = new double[nOutputs];
		double[] outputExped = new double[nOutputs];
		double sumOutputsExped = Double.NaN;
		double[] outputProbabilities = new double[nOutputs];

		int highestOutputIndex = -2;
		double highestOutputProbability = Double.NEGATIVE_INFINITY;
				
		// copy the weights and biases to a double array...
		double[] weightsDouble = new double[nWeights];
		double[] biasesDouble = new double[(int)biases.getDimensions()[0]];
		for (int weightIndex = 0; weightIndex < nWeights; weightIndex++) {
			weightsDouble[weightIndex] = weights.getValue(weightIndex,0);
		}
		for (int biasIndex = 0; biasIndex < biasesDouble.length; biasIndex++) {
			biasesDouble[biasIndex] = biases.getValue(biasIndex,0);
		}
		

		double totalError = 0.0; // initialize the total error
		long nCorrect = 0; // initialize the counter for total hit correctly
		getBias[1] = 0;
		getWeight[1] = 0;
		getY[1] = 0;
		outputExped[nOutputsMinusOne] = 1.0;
		int nInputsPlusNHidden  = nInputs  + nHidden;
		int nInputsTimesNHidden = nInputs  * nHidden;
		for (long exampleIndex = 0; exampleIndex < nExamples; exampleIndex++) {
			getX[0] = exampleIndex;
			getY[0] = exampleIndex;
			targetThisExample = (int) targetValues.getValue(getY);
			for (int xIndex = 0; xIndex < nInputs; xIndex++) {
				getX[1] = xIndex;
				xDouble[xIndex] = explanatoryVariables.getValue(getX);
			}

			// initialize output sums with bias value (last bias/neuron is the output)
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				outputSums[outputIndex] = biasesDouble[nInputsPlusNHidden + outputIndex];
			}
			outputSums[nOutputsMinusOne] = 0.0;

			// supposing there are hidden neurons
			for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
				// initialize with bias value
				internalSumsForHidden[hiddenIndex] = biasesDouble[nInputs + hiddenIndex];

				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					internalSumsForHidden[hiddenIndex] += weightsDouble[hiddenIndex + nHidden * inputIndex]
					                                                    * xDouble[inputIndex];
					
				}

				if (internalSumsForHidden[hiddenIndex] < minSumAllowed) {
					internalSumsForHidden[hiddenIndex] = minSumAllowed;
					expedThings[hiddenIndex] = Double.MAX_VALUE;
				} else {
					expedThings[hiddenIndex] = MatrixOperations.altExp(-internalSumsForHidden[hiddenIndex]);
					lambdaHidden[hiddenIndex] = 1 / (1 + expedThings[hiddenIndex]);
				}

			}

			// if there aren't, just connect inputs directly to outputs
			if (nHidden == 0) {
				for (int inputIndex = 0; inputIndex < nInputs; inputIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[inputIndex * nOutputsMinusOne + outputIndex]
						                                         * xDouble[inputIndex];
					}
				}
			} else {
				// build up the y output values
				for (int hiddenIndex = 0; hiddenIndex < nHidden; hiddenIndex++) {
					for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
						// initialize output value with bias value (last bias/neuron is the
						// output)
						outputSums[outputIndex] += weightsDouble[nInputsTimesNHidden + nOutputsMinusOne * hiddenIndex + outputIndex]
						                                         / (1.0 + expedThings[hiddenIndex]);
					}
				}

			}

			// find the probabilities
			sumOutputsExped = 1.0; // Beware the MAGIC INITIALIZATION!!! this is for
															// the standardized output
			for (int outputIndex = 0; outputIndex < nOutputsMinusOne; outputIndex++) {
				// initialize output value with bias value (last bias/neuron is the
				// output)
				outputExped[outputIndex] = MatrixOperations.altExp(outputSums[outputIndex]);
				sumOutputsExped += outputExped[outputIndex];
			}
			highestOutputIndex = -1;
			highestOutputProbability = Double.NEGATIVE_INFINITY;
			for (int outputIndex = 0; outputIndex < nOutputs; outputIndex++) {
				outputProbabilities[outputIndex] = outputExped[outputIndex] / sumOutputsExped;
				if (outputProbabilities[outputIndex] > highestOutputProbability) {
					highestOutputIndex = outputIndex;
					highestOutputProbability = outputProbabilities[outputIndex];
				}
			}
			
			// check if the highest probability matches the target
			if (highestOutputIndex == targetThisExample) {
				nCorrect++;
			}
			
			// //////////////////
			// delta attempts //
			// //////////////////

			// final deltas; with the precision problem
			for (int outputIndex = 0; outputIndex < nOutputs; outputIndex++) {
				if (targetThisExample == outputIndex) {
					// This is the realized outcome...

					totalError -= MatrixOperations.altLn(outputProbabilities[outputIndex]);

				}			
			} // for outputIndex

		} // end example index

		double[] returnObject = new double[] {totalError , nCorrect};
		
		return returnObject;
	}
	
	
	public static MultiFormatMatrix[] findBFGSDirection(
				MultiFormatMatrix previousWeights, MultiFormatMatrix previousBiases, MultiFormatMatrix previousGradient,
				MultiFormatMatrix previousBigG, MultiFormatMatrix currentWeights,	MultiFormatMatrix currentBiases,   
				MultiFormatMatrix currentGradient) throws Exception {
	
	//		MultiFormatMatrix testRow = null;
			
			if (currentGradient.getDimensions()[0] != previousBigG.getDimensions()[0]) {
				System.out.println("CurrentGradient.getDimensions()[0] != PreviousBigG.getDimensions()[0]: " 
						+ currentGradient.getDimensions()[0] + " != " + previousBigG.getDimensions()[0]);
				throw new Exception();
			}
	
			// we need to stack up the current weights and biases into a single column vector
			long nWeights = previousWeights.getDimensions()[0];
			long nGoodBiases = previousGradient.getDimensions()[0] - nWeights;
			long firstGoodBias = previousBiases.getDimensions()[0] - nGoodBiases;

			// changing to use on-disk so that will carry through... i.e., changing 1 to 3
			int onDiskFormatIndex = 3;
			MultiFormatMatrix previousStackedParameters = new MultiFormatMatrix(onDiskFormatIndex,previousGradient.getDimensions());
			MultiFormatMatrix currentStackedParameters  = new MultiFormatMatrix(onDiskFormatIndex,previousGradient.getDimensions());
			// put in the weights
			for (long weightIndex = 0; weightIndex < nWeights; weightIndex++) {
				previousStackedParameters.setValue(weightIndex,0, previousWeights.getValue(weightIndex,0));
				currentStackedParameters.setValue( weightIndex,0, currentWeights.getValue( weightIndex,0));
			}
			
			// put in the good biases
			long actualBiasIndex = -1;
			long storageIndex = -2;
			for (long goodBiasIndex = 0; goodBiasIndex < nGoodBiases; goodBiasIndex++) {
				actualBiasIndex = goodBiasIndex + firstGoodBias;
				storageIndex = goodBiasIndex + nWeights;
				
				previousStackedParameters.setValue(storageIndex,0, previousBiases.getValue(actualBiasIndex,0));
				currentStackedParameters.setValue( storageIndex,0, currentBiases.getValue( actualBiasIndex,0));
			}
			
					
			MultiFormatMatrix p = MatrixOperations.subtractMatrices(currentStackedParameters, previousStackedParameters);
			
			
			MultiFormatMatrix v = MatrixOperations.newFormat(MatrixOperations.subtractMatrices(currentGradient, previousGradient),onDiskFormatIndex);
			
			//System.out.println("G is [" + PreviousBigG.getDimensions()[0] + "]x[" + PreviousBigG.getDimensions()[1] + "]");
			//System.out.println("v is [" + v.getDimensions()[0] + "]x[" + v.getDimensions()[1] + "]");
			
			MultiFormatMatrix Gv = MatrixOperations.multiplyMatrices(previousBigG, v);
			// probably unnecessary if G is symmetric, but just in case...
			MultiFormatMatrix vprimeG = MatrixOperations.multiplyMatrices(MatrixOperations.transposeMatrix(v),previousBigG);
			
			// the following are singleton matrices
			MultiFormatMatrix vprimeGv = MatrixOperations.multiplyMatrices(MatrixOperations.transposeMatrix(v),Gv);
			double vprimeGvDouble = vprimeGv.getValue(0,0);
	
			MultiFormatMatrix pprimev = MatrixOperations.multiplyMatrices(MatrixOperations.transposeMatrix(p), v);
			double oneOverpprimevDouble = 1.0 / pprimev.getValue(0,0);
					
			// back to an intermediate result
			MultiFormatMatrix uFirstTerm = MatrixOperations.multiplyByConstant(oneOverpprimevDouble, p);
			MultiFormatMatrix uSecondTerm = MatrixOperations.multiplyByConstant(1.0 / vprimeGvDouble,Gv);
			MultiFormatMatrix u = MatrixOperations.subtractMatrices(uFirstTerm, uSecondTerm);
			
			// now, for the final update...
			
			MultiFormatMatrix GSecondTerm = MatrixOperations.multiplyByConstant(oneOverpprimevDouble,
					MatrixOperations.multiplyMatrices(p, MatrixOperations.transposeMatrix(p)));
	
			MultiFormatMatrix GThirdTerm = MatrixOperations.multiplyByConstant(-1.0 / vprimeGvDouble, 
					MatrixOperations.multiplyMatrices(Gv, vprimeG));
			
			MultiFormatMatrix GFourthTerm = MatrixOperations.multiplyByConstant(vprimeGvDouble,
					MatrixOperations.multiplyMatrices(u, MatrixOperations.transposeMatrix(u)));
			
			MultiFormatMatrix GOneTwo = MatrixOperations.addMatrices(previousBigG, GSecondTerm);
			MultiFormatMatrix GThreeFour = MatrixOperations.addMatrices(GThirdTerm, GFourthTerm);
	
	
			
			MultiFormatMatrix currentBigG = MatrixOperations.addMatrices(GOneTwo, GThreeFour);
	
			MultiFormatMatrix currentBFGSDirection = MatrixOperations.multiplyMatrices(currentBigG, currentGradient);
			
			return new MultiFormatMatrix[] {currentBFGSDirection , currentBigG};
		}



	public static MultiFormatMatrix[] categoricalLineSearch(MultiFormatFloat explanatoryVariables,
			MultiFormatFloat targets,	int nHidden, int nOutputsMinusOne,
			MultiFormatMatrix serializedWeights, MultiFormatMatrix biases,
			MultiFormatMatrix stackedGradient, double euclideanStepSize,
			double stoppingTolerance, double fractionAboveMinGuess) throws Exception {

		int magicNumberOfSteps = 4;
		double magicFractionAboveMinGuess = fractionAboveMinGuess; // 0.1 seems to work well

		
		// normalize the direction of the gradient...
		MultiFormatMatrix normalizedDirection = new MultiFormatMatrix(1,stackedGradient.getDimensions());
		double gradientLengthSquared = 0.0;
		double inverseGradientLength = 0.0;
		for (long rowIndex = 0; rowIndex < stackedGradient.getDimensions()[0]; rowIndex++) {
			gradientLengthSquared += stackedGradient.getValue(rowIndex,0) * stackedGradient.getValue(rowIndex,0);
		}
		inverseGradientLength = 1.0 / Math.sqrt(gradientLengthSquared);

		normalizedDirection = MatrixOperations.multiplyByConstant(inverseGradientLength,stackedGradient);
		
		// let's try a very simple line search
		
		double[] stepParameters = new double[magicNumberOfSteps];
		double[] stepErrors     = new double[magicNumberOfSteps];
		MultiFormatMatrix[] stepWeights = new MultiFormatMatrix[magicNumberOfSteps];
		MultiFormatMatrix[] stepBiases  = new MultiFormatMatrix[magicNumberOfSteps];
		
		MultiFormatMatrix parameterStep = null; 
		MultiFormatMatrix[] newPair = new MultiFormatMatrix[2];
		MultiFormatMatrix[] finalPair = new MultiFormatMatrix[2];
		
		double errorRange = Double.NaN;
		double errorMax = -1;
		double errorMin = -2;
		
		double a = -1, b = -2, c = -3, A =-4, B = -5;
		double x_1, x_2, x_3, y_1, y_2, y_3;
		double minGuess = Double.NaN;
		double lowerEdgeHeight = -7;
		double newEdgeTargetHeight = -8;
		double newXLower = -8;
		double newXUpper = -8;
		double finalMultiplier = Double.NaN;
		
		// section until we are within some tolerance
		boolean withinTolerance = false;
		boolean breakDueToNaNs = false;
		double startingMultiplierBoundary = 0.0; // this actually needs to be zero
		double stepSizeToUse = euclideanStepSize;
		
		int nSearches = 0;
		while (!withinTolerance) {
			nSearches++;
			for (int stepIndex = 0; stepIndex < magicNumberOfSteps; stepIndex++) {
				stepParameters[stepIndex] = startingMultiplierBoundary - stepIndex * stepSizeToUse; // negative one because we want to go downhill

				parameterStep = MatrixOperations.multiplyByConstant(stepParameters[stepIndex],normalizedDirection);

				newPair = newWeightsBiases(serializedWeights,biases,parameterStep);
				stepWeights[stepIndex] = newPair[0];
				stepBiases[stepIndex]  = newPair[1];

				stepErrors[stepIndex] = NNHelperMethods.findErrorSingleHidden(explanatoryVariables,
						targets, stepWeights[stepIndex], stepBiases[stepIndex], nHidden, nOutputsMinusOne
						)[0];

				if (Double.isNaN(stepErrors[stepIndex])) {
					breakDueToNaNs = true;
					break;
				}
//				System.out.println("[" + stepIndex + "] " + stepParameters[stepIndex] + " -> " + stepErrors[stepIndex]);

				// check if we are going uphill. but, make sure we have stuff to compare to...
				if (stepIndex > 1) {
					if (stepErrors[stepIndex] > stepErrors[stepIndex - 1]) {
						// reset step size & bounds; breakout; and restart

						// this will be before the minimum because either we went down and came back up
						// or, we are only on the third point, so we should start at the beginning again, and just look more finely
						errorMax = Math.max(stepErrors[stepIndex], Math.max(stepErrors[stepIndex - 1],stepErrors[stepIndex - 2]));
						errorMin = Math.min(stepErrors[stepIndex], Math.min(stepErrors[stepIndex - 1],stepErrors[stepIndex - 2]));
						errorRange = errorMax - errorMin;

						// pure sectioning
						// startingMultiplierBoundary = stepParameters[stepIndex - 2];
						// stepSizeToUse = Math.abs((stepParameters[stepIndex] - stepParameters[stepIndex - 2]) / (magicNumberOfSteps - 1));
						
						// try a quadratic approximation approach
						// first, we define a parabola going through the final three points; this is ugly, so you might want to
						// derive it yourself in case you're wondering. it's not terribly hard...
						// y = ax^2 + bx + c
						x_1 = stepParameters[stepIndex - 2];
						x_2 = stepParameters[stepIndex - 1];
						x_3 = stepParameters[stepIndex];
						y_1 = stepErrors[stepIndex - 2];
						y_2 = stepErrors[stepIndex - 1];
						y_3 = stepErrors[stepIndex];
						
						A = (x_2 - x_1)/(x_2*x_2 - x_1*x_1) ;
						B = (y_2 - y_1)/(x_2*x_2 - x_1*x_1);
						
						b = (y_3 - y_1 - B*(x_3*x_3 - x_1*x_1)) / ((x_3 - x_1) - A*(x_3*x_3 - x_1*x_1));
						a = -b * A + B ;
						c = y_1 - a*x_1*x_1 - b*x_1;
						
						
						// now, we can figure where the bottom of the parabola is... -b/2a
						// but, instead of going straight to the bottom, let's go a little bit up from
						// the bottom on either side and use those as new edges for our search
						// minGuess the height at the bottom of the parabola, i.e., y(-b/2a)
						minGuess = - b*b / (4*a) + c;
//						System.out.println("a=" + a + " b=" + b + " c=" + c + " ; minGuess=" + minGuess + " at " + (-b/(2*a)));
						
						// now, let us define "a little bit up" as a certain fraction of the distance
						// between the bottom and the lower of our two edges... now, we know that
						// points 1 and 3 are the edges, so we look for the lower of those two
						lowerEdgeHeight = Math.min(y_1,y_3);
						
						newEdgeTargetHeight = minGuess + magicFractionAboveMinGuess * (lowerEdgeHeight - minGuess);

						// now we determine what x's result in that height.
						// the ole quadratic formula...
						newXUpper = (-b - Math.sqrt(b*b - 4*a*(c - newEdgeTargetHeight))) / (2*a);
						newXLower = (-b + Math.sqrt(b*b - 4*a*(c - newEdgeTargetHeight))) / (2*a);

						startingMultiplierBoundary = (newXLower);
						stepSizeToUse = Math.abs((newXUpper - newXLower) / (magicNumberOfSteps - 1));
						
//						System.out.println("lEH=" + lowerEdgeHeight + " nETH=" + newEdgeTargetHeight + " nXL=" + newXLower + " nxU=" + newXUpper);
						
//						System.out.println("--> restarting: errorRange = " + errorRange + " ; new start = " + startingMultiplierBoundary + " ; new size = " + stepSizeToUse);
						if (errorRange < stoppingTolerance) {
							withinTolerance = true;
							finalMultiplier = -b / (2*a);
//							System.out.println("    == hit stopping tolerance, moving on after " + nSearches + " searches ==");
						}
						break;
					} // end of if going uphill
					if (stepIndex == magicNumberOfSteps - 1) {
						startingMultiplierBoundary = stepParameters[0];
						stepSizeToUse = stepSizeToUse * 2.0; // Beware the MAGIC NUMBER!!! doubling our way back up...
//						System.out.println("--> no min yet, restarting: new start = " + startingMultiplierBoundary + " ; new size = " + stepSizeToUse);
					}
				} // end of if stepIndex > 2
			} // end of for stepIndex
			if (breakDueToNaNs) {
				System.out.println("cont LS encountered a NaN on search " + nSearches + "; returning original parameters");
				break;
			}
			
			
			
			
		} // end of while tolerance not met
			if (breakDueToNaNs) {
				// something bad happened, use the old ones
				finalPair[0] = serializedWeights;
				finalPair[1] = biases;
			} else {
				// should be fine, use the new ones
				parameterStep = MatrixOperations.multiplyByConstant(finalMultiplier,normalizedDirection);
				finalPair = newWeightsBiases(serializedWeights,biases,parameterStep);
			}

		
		return finalPair;
	}

	public static Object[] makeSteps(double startingMultiplierBoundary, double stepSizeToUse,
			MultiFormatMatrix serializedWeights, MultiFormatMatrix biases, MultiFormatMatrix normalizedDirection
			)	throws Exception {
		
		int magicNumberOfSteps = 3;

		MultiFormatMatrix parameterStep = null;
		MultiFormatMatrix[] newPair = null;

		double[] stepParameters = new double[magicNumberOfSteps];
		MultiFormatMatrix[] stepWeights = new MultiFormatMatrix[magicNumberOfSteps];
		MultiFormatMatrix[] stepBiases  = new MultiFormatMatrix[magicNumberOfSteps];

		for (int stepIndex = 0; stepIndex < magicNumberOfSteps; stepIndex++) {
			
			// determine the multiplier for this step
			stepParameters[stepIndex] = startingMultiplierBoundary - stepIndex * stepSizeToUse; // negative one because we want to go downhill

			// figure out the additive step to take
			parameterStep = MatrixOperations.multiplyByConstant(stepParameters[stepIndex],normalizedDirection);

			// apply the additive step to the original parameters
			newPair = newWeightsBiases(serializedWeights,biases,parameterStep);
			stepWeights[stepIndex] = MatrixOperations.newCopy(newPair[0]); // doing a new copy just to make sure...
			stepBiases[ stepIndex] = MatrixOperations.newCopy(newPair[1]);
		}

		return new Object[] {new MultiFormatMatrix[][] {stepWeights , stepBiases} , stepParameters};
	}

	public static Object[] makeSteps(double startingMultiplierBoundary, double stepSizeToUse, int nSteps,
			MultiFormatMatrix serializedWeights, MultiFormatMatrix biases, MultiFormatMatrix normalizedDirection
			)	throws Exception {
		
		int magicNumberOfSteps = nSteps;

		MultiFormatMatrix parameterStep = null;
		MultiFormatMatrix[] newPair = null;

		double[] stepParameters = new double[magicNumberOfSteps];
		MultiFormatMatrix[] stepWeights = new MultiFormatMatrix[magicNumberOfSteps];
		MultiFormatMatrix[] stepBiases  = new MultiFormatMatrix[magicNumberOfSteps];

		for (int stepIndex = 0; stepIndex < magicNumberOfSteps; stepIndex++) {
			
			// determine the multiplier for this step
			stepParameters[stepIndex] = startingMultiplierBoundary - stepIndex * stepSizeToUse; // negative one because we want to go downhill

			// figure out the additive step to take
			parameterStep = MatrixOperations.multiplyByConstant(stepParameters[stepIndex],normalizedDirection);

			// apply the additive step to the original parameters
			newPair = newWeightsBiases(serializedWeights,biases,parameterStep);
			stepWeights[stepIndex] = MatrixOperations.newCopy(newPair[0]); // doing a new copy just to make sure...
			stepBiases[ stepIndex] = MatrixOperations.newCopy(newPair[1]);
		}

		return new Object[] {new MultiFormatMatrix[][] {stepWeights , stepBiases} , stepParameters};
	}

	public static MultiFormatMatrix[] categoricalLineSearchRedone(MultiFormatFloat explanatoryVariables,
			MultiFormatFloat targets,	int nHidden, int nOutputsMinusOne,
			MultiFormatMatrix serializedWeights, MultiFormatMatrix biases,
			MultiFormatMatrix stackedGradient, double euclideanStepSize,
			double stoppingTolerance, double fractionAboveMinGuess) throws Exception {

		// initializations
		
		double errorRange = Double.NaN;
		double errorMax = -1;
		double errorMin = -2;
		
		double a = -1, b = -2, c = -3, A =-4, B = -5;
		double x_1, x_2, x_3, y_1, y_2, y_3;
		double minGuess = Double.NaN;
		double lowerEdgeHeight = -7;
		double newEdgeTargetHeight = -8;
		double newXLower = -8;
		double newXUpper = -8;
		double finalMultiplier = Double.NaN;

		double[] stepParameters = null;
		double[] stepErrors     = null;

		Object[] stepWeightsBiasesPairsParameters = null;

		MultiFormatMatrix parameterStep = null; 
		MultiFormatMatrix[] finalPair = new MultiFormatMatrix[2];

		// section until we are within some tolerance
		boolean withinTolerance = false;
		boolean breakDueToNaNs = false;
		
		double startingMultiplierBoundary = 0.0; // this actually needs to be zero
		double stepSizeToUse = euclideanStepSize;

		// normalize the direction of the gradient...
		MultiFormatMatrix normalizedDirection = MatrixOperations.normalizeColumn(stackedGradient);

		int nSearches = 0;
		while (!withinTolerance) {
			nSearches++;
			
			// ask for all the multipliers and resulting parameters
			stepWeightsBiasesPairsParameters = NNHelperMethods.makeSteps(startingMultiplierBoundary, stepSizeToUse, serializedWeights, biases, normalizedDirection);
			
			// unpack the goodies
			stepParameters = (double[]) stepWeightsBiasesPairsParameters[1];
			stepErrors = new double[stepParameters.length];

			// do everything else...
			for (int stepIndex = 0; stepIndex < stepParameters.length; stepIndex++) {

				// determine the errors associated with each step
				stepErrors[stepIndex] = NNHelperMethods.findErrorSingleHidden(explanatoryVariables,
						targets,
						( (MultiFormatMatrix[][])stepWeightsBiasesPairsParameters[0] )[0][stepIndex],
						( (MultiFormatMatrix[][])stepWeightsBiasesPairsParameters[0] )[1][stepIndex],
						nHidden, nOutputsMinusOne
						)[0];

				
				if (Double.isNaN(stepErrors[stepIndex])) {
					breakDueToNaNs = true;
					break;
				}
//				System.out.println("[" + stepIndex + "] " + stepParameters[stepIndex] + " -> " + stepErrors[stepIndex]);

				// check if we are going uphill. but, make sure we have stuff to compare to...
				if (stepIndex > 1) {
					if (stepErrors[stepIndex] > stepErrors[stepIndex - 1]) {
						// reset step size & bounds; breakout; and restart

						// this will be before the minimum because either we went down and came back up
						// or, we are only on the third point, so we should start at the beginning again, and just look more finely
						errorMax = Math.max(stepErrors[stepIndex], Math.max(stepErrors[stepIndex - 1],stepErrors[stepIndex - 2]));
						errorMin = Math.min(stepErrors[stepIndex], Math.min(stepErrors[stepIndex - 1],stepErrors[stepIndex - 2]));
						errorRange = errorMax - errorMin;

						// pure sectioning
						// startingMultiplierBoundary = stepParameters[stepIndex - 2];
						// stepSizeToUse = Math.abs((stepParameters[stepIndex] - stepParameters[stepIndex - 2]) / (magicNumberOfSteps - 1));
						
						// try a quadratic approximation approach
						// first, we define a parabola going through the final three points; this is ugly, so you might want to
						// derive it yourself in case you're wondering. it's not terribly hard...
						// y = ax^2 + bx + c
						x_1 = stepParameters[stepIndex - 2];
						x_2 = stepParameters[stepIndex - 1];
						x_3 = stepParameters[stepIndex];
						y_1 = stepErrors[stepIndex - 2];
						y_2 = stepErrors[stepIndex - 1];
						y_3 = stepErrors[stepIndex];
						
						A = (x_2 - x_1)/(x_2*x_2 - x_1*x_1) ;
						B = (y_2 - y_1)/(x_2*x_2 - x_1*x_1);
						
						b = (y_3 - y_1 - B*(x_3*x_3 - x_1*x_1)) / ((x_3 - x_1) - A*(x_3*x_3 - x_1*x_1));
						a = -b * A + B ;
						c = y_1 - a*x_1*x_1 - b*x_1;
						
						
						// now, we can figure where the bottom of the parabola is... -b/2a
						// but, instead of going straight to the bottom, let's go a little bit up from
						// the bottom on either side and use those as new edges for our search
						// minGuess the height at the bottom of the parabola, i.e., y(-b/2a)
						minGuess = - b*b / (4*a) + c;
//						System.out.println("a=" + a + " b=" + b + " c=" + c + " ; minGuess=" + minGuess + " at " + (-b/(2*a)));
						
						// now, let us define "a little bit up" as a certain fraction of the distance
						// between the bottom and the lower of our two edges... now, we know that
						// points 1 and 3 are the edges, so we look for the lower of those two
						lowerEdgeHeight = Math.min(y_1,y_3);
						
						newEdgeTargetHeight = minGuess + fractionAboveMinGuess * (lowerEdgeHeight - minGuess);

						// now we determine what x's result in that height.
						// the ole quadratic formula...
						newXUpper = (-b - Math.sqrt(b*b - 4*a*(c - newEdgeTargetHeight))) / (2*a);
						newXLower = (-b + Math.sqrt(b*b - 4*a*(c - newEdgeTargetHeight))) / (2*a);

						startingMultiplierBoundary = (newXLower);
						stepSizeToUse = Math.abs((newXUpper - newXLower) / (stepParameters.length - 1));
						
//						System.out.println("lEH=" + lowerEdgeHeight + " nETH=" + newEdgeTargetHeight + " nXL=" + newXLower + " nxU=" + newXUpper);
						
//						System.out.println("--> restarting: errorRange = " + errorRange + " ; new start = " + startingMultiplierBoundary + " ; new size = " + stepSizeToUse);
						if (errorRange < stoppingTolerance) {
							withinTolerance = true;
							finalMultiplier = -b / (2*a);
//							System.out.println("    == hit stopping tolerance, moving on after " + nSearches + " searches ==");
						}
						break;
					} // end of if going uphill
					if (stepIndex == stepParameters.length - 1) {
						startingMultiplierBoundary = stepParameters[0];
						stepSizeToUse = stepSizeToUse * 2.0; // Beware the MAGIC NUMBER!!! doubling our way back up...
//						System.out.println("--> no min yet, restarting: new start = " + startingMultiplierBoundary + " ; new size = " + stepSizeToUse);
					}
				} // end of if stepIndex > 2
			} // end of for stepIndex

			
			if (breakDueToNaNs) {
				System.out.println("cont LS encountered a NaN on search " + nSearches + "; returning original parameters");
				break;
			}
			
			
		} // end of while tolerance not met

		
		if (breakDueToNaNs) {
			// something bad happened, use the old ones
			finalPair[0] = serializedWeights;
			finalPair[1] = biases;
		} else {
			// should be fine, use the new ones
			parameterStep = MatrixOperations.multiplyByConstant(finalMultiplier,normalizedDirection);
			finalPair = newWeightsBiases(serializedWeights,biases,parameterStep);
		}

		System.out.println("final multiplier = " + finalMultiplier);
		return finalPair;
	}

	
	public static MultiFormatMatrix categoricalGridSearch(MultiFormatFloat explanatoryVariables,
			MultiFormatFloat targets,	int nHidden, int nOutputsMinusOne,
			MultiFormatMatrix serializedWeights, MultiFormatMatrix biases,
			MultiFormatMatrix stackedGradient, double radius,
			int nSteps) throws Exception {

		// initializations
		double startingMultiplierBoundary = radius; // this actually needs to be zero
		double stepSizeToUse = (2 * radius) / (nSteps - 1);

		MultiFormatMatrix normalizedDirection = MatrixOperations.normalizeColumn(stackedGradient);
		
		Object[] stepWeightsBiasesPairsParameters = NNHelperMethods.makeSteps(
				startingMultiplierBoundary, stepSizeToUse, nSteps, serializedWeights, biases, normalizedDirection);

		double[] stepParameters = (double[])stepWeightsBiasesPairsParameters[1];
		
		MultiFormatMatrix resultsMatrix = new MultiFormatMatrix(1,new long[] {nSteps,3});
		
		for (int stepIndex = 0; stepIndex < nSteps; stepIndex++) {
//			System.out.println("sI = " + stepIndex + " sP.length = " + stepParameters.length);
			resultsMatrix.setValue(stepIndex,
					0,
					stepParameters[stepIndex]);
			resultsMatrix.setValue(stepIndex,1, stepParameters[stepIndex] * normalizedDirection.getValue(0,0) / stackedGradient.getValue(0,0));
			
			resultsMatrix.setValue(stepIndex,2, NNHelperMethods.findErrorSingleHidden(explanatoryVariables, targets,
					((MultiFormatMatrix[][])stepWeightsBiasesPairsParameters[0])[0][stepIndex],
					((MultiFormatMatrix[][])stepWeightsBiasesPairsParameters[0])[1][stepIndex],
					nHidden, nOutputsMinusOne)[0]
					                           );
		}

		return resultsMatrix;
	}

	public static double[] quadraticSearchHelper(double[] stepParameters,
			double[] stepErrorsTotal, int stepIndex, double fractionAboveMinGuess) {
	
//		for (int aStepIndex = 0; aStepIndex < stepParameters.length; aStepIndex++) {
//			System.out.println("q:\t" + aStepIndex + "\t->\t" + stepParameters[aStepIndex] + "\t" + stepErrorsTotal[aStepIndex]);
//		}

		// this will be before the minimum because either we went down and came back up
		// or, we are only on the third point, so we should start at the beginning again, and just look more finely
		double errorMax = Math.max(stepErrorsTotal[stepIndex], Math.max(stepErrorsTotal[stepIndex - 1],stepErrorsTotal[stepIndex - 2]));
		double errorMin = Math.min(stepErrorsTotal[stepIndex], Math.min(stepErrorsTotal[stepIndex - 1],stepErrorsTotal[stepIndex - 2]));
		double errorRange = errorMax - errorMin;

		// try a quadratic approximation approach
		// first, we define a parabola going through the final three points; this is ugly, so you might want to
		// derive it yourself in case you're wondering. it's not terribly hard...
		// y = ax^2 + bx + c
		double x_1 = stepParameters[stepIndex - 2];
		double x_2 = stepParameters[stepIndex - 1];
		double x_3 = stepParameters[stepIndex];
		double y_1 = stepErrorsTotal[stepIndex - 2];
		double y_2 = stepErrorsTotal[stepIndex - 1];
		double y_3 = stepErrorsTotal[stepIndex];

		double A = (x_2 - x_1)/(x_2*x_2 - x_1*x_1) ;
		double B = (y_2 - y_1)/(x_2*x_2 - x_1*x_1);

		double b = (y_3 - y_1 - B*(x_3*x_3 - x_1*x_1)) / ((x_3 - x_1) - A*(x_3*x_3 - x_1*x_1));
		double a = -b * A + B ;
		double c = y_1 - a*x_1*x_1 - b*x_1;


		// now, we can figure where the bottom of the parabola is... -b/2a
		// but, instead of going straight to the bottom, let's go a little bit up from
		// the bottom on either side and use those as new edges for our search
		// minGuess the height at the bottom of the parabola, i.e., y(-b/2a)
		double minGuess = - b*b / (4*a) + c;

		// now, let us define "a little bit up" as a certain fraction of the distance
		// between the bottom and the lower of our two edges... now, we know that
		// points 1 and 3 are the edges, so we look for the lower of those two
		double lowerEdgeHeight = Math.min(y_1,y_3);

		double newEdgeTargetHeight = minGuess + fractionAboveMinGuess * (lowerEdgeHeight - minGuess);

		// now we determine what x's result in that height.
		// the ole quadratic formula...
		double newXUpper = (-b - Math.sqrt(b*b - 4*a*(c - newEdgeTargetHeight))) / (2*a);
		double newXLower = (-b + Math.sqrt(b*b - 4*a*(c - newEdgeTargetHeight))) / (2*a);
		
		double startingMultiplierBoundary = (newXLower);
		double stepSizeToUse = Math.abs((newXUpper - newXLower) / (stepParameters.length - 1));
		
		double finalMultiplier = -b/(2 * a);
		
		return new double[] {startingMultiplierBoundary , stepSizeToUse, errorRange, finalMultiplier};

	}
}
