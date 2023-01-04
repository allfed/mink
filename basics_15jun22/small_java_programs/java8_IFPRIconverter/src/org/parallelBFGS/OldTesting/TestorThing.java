package org.parallelBFGS.OldTesting;
//import java.io.BufferedReader;
//import java.io.File;
import java.io.File;
//import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
//import java.io.FileWriter;
//import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
//import java.util.concurrent.*;

public class TestorThing {



	public static void main(String commandLineOptions[]) throws Exception {
		
		int nThreadsToUse = 8;
		
		DeadDropDoubles droppingPlace = new DeadDropDoubles(nThreadsToUse);
		
		double[][] listArray = {
				{1,2,3,4,5},
				{11,22,33,44,55},
				{111,222,333,444,555},
				{1111,2222,3333,4444,5555},
				{-1,-2,-3,-4,-5},
				{-11,-22,-33,-44,-55},
				{-111,-222,-333,-444,-555},
				{-1111,-2222,-3333,-4444,-5555}
				};
		
		ProducerDoubles[] bundleOfProducers = new ProducerDoubles[nThreadsToUse];
		
		for (int threadIndex = 0; threadIndex < nThreadsToUse; threadIndex++) {
			System.out.println("...trying to start producer thread #" + threadIndex);
			bundleOfProducers[threadIndex] = new ProducerDoubles(threadIndex,droppingPlace);
			bundleOfProducers[threadIndex].setDoubleList(listArray[threadIndex]);
	    new Thread(bundleOfProducers[threadIndex]).start();
		}
		System.out.println("___done starting producer threads, sleeping then starting the consumers___");
    Thread.sleep(500);
    (new Thread(new ConsumerDoubles(-1,droppingPlace))).start();
		System.out.println("   consumer -1 started");
//    (new Thread(new ConsumerIntegers(-2,droppingPlace))).start();
//		System.out.println("   consumer -2 started");

	} // main

}
