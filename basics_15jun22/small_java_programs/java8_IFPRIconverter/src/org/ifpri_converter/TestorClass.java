package org.ifpri_converter;
import java.io.BufferedReader;
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

public class TestorClass {




	public static void main(String commandLineOptions[]) throws Exception {
		
		String[] testList = new String[] {
			"zero",
			"one",
			"two",
			"two",
			"three",
			"zero"
		};
		
		int[] intCodes = new int[testList.length];

		StringToUniqueCode encoderTest = new StringToUniqueCode(1);
		
		for (int listIndex = 0; listIndex < testList.length; listIndex++) {
			intCodes[listIndex] = encoderTest.getCode(testList[listIndex]);
			System.out.println(listIndex + " -> [" + testList[listIndex] + "] -> " + intCodes[listIndex]);
		}
		
		String[] dictionaryDone = encoderTest.getDictionaryList();
		for (int dictIndex = 0; dictIndex < dictionaryDone.length; dictIndex++) {
			System.out.println(dictIndex + " -> [" + dictionaryDone[dictIndex] + "]");
		}
		
		
	} // main

}

