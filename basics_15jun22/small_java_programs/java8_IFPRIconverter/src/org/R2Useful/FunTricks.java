package org.R2Useful;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class FunTricks {

  // totally swiped from: http://www.journaldev.com/861/4-ways-to-copy-file-in-java
  public static void copyFileUsingStream(File source, File dest) throws IOException {
    InputStream is = null;
    OutputStream os = null;
    try {
      is = new FileInputStream(source);
      os = new FileOutputStream(dest);
      byte[] buffer = new byte[1024];
      int length;
      while ((length = is.read(buffer)) > 0) {
        os.write(buffer, 0, length);
      }
    } finally {
      is.close();
      os.close();
    }
  }

  public static void appendLineToTextFile(String lineToAppend, String inputFile, boolean appendFlag)
      throws FileNotFoundException, IOException {
    File fileToWrite = new File(inputFile);
    FileOutputStream fileOutputStream = null;
    if (appendFlag) {
      fileOutputStream = new FileOutputStream(fileToWrite, true); // the true means append
    } else {
      fileOutputStream = new FileOutputStream(fileToWrite);
    }

    PrintWriter outputWriter = new PrintWriter(fileOutputStream);

    outputWriter.println(lineToAppend);
    outputWriter.flush();
    outputWriter.close();
    fileOutputStream.close();
  }

  public static int nLinesInTextFile(String inputFile) throws FileNotFoundException, IOException {
    // figure out the total number of pixels in input file...
    int nLinesTotal = -1;

    RandomAccessFile randFile = new RandomAccessFile(inputFile, "r");

    long lastRec = randFile.length();

    randFile.close();

    FileReader inRead = new FileReader(inputFile);
    LineNumberReader lineRead = new LineNumberReader(inRead);

    lineRead.skip(lastRec);
    nLinesTotal = lineRead.getLineNumber();

    lineRead.close();
    inRead.close();

    return nLinesTotal;
  }

  public static void writeInfoFile(
      String basename, long nRows, long nCols, int formatIndex, String delimiter)
      throws FileNotFoundException {

    File infoFileObject = new File(basename + ".info.txt");
    PrintWriter infoOutSteam = new PrintWriter(infoFileObject);

    infoOutSteam.print(nRows + "\t = Number of Rows\n");
    infoOutSteam.print(nCols + "\t = Number of Columns\n");
    infoOutSteam.print((nRows * nCols) + "\t = Total Number of Elements\n");
    infoOutSteam.print(
        formatIndex
            + "\t = The MultiFormatMatrix format the matrix was stored in\n"); // Beware the MAGIC
    // NUMBER!!!
    infoOutSteam.print(delimiter + "\t = The string used to delimit elements in the Rows\n");

    infoOutSteam.flush();
    infoOutSteam.close();
  }

  public static void writeInfoFile(String basename, long nRows, long nCols, String delimiter)
      throws FileNotFoundException {
    int formatIndex = 3; // brute force it to "in memory"
    writeInfoFile(basename, nRows, nCols, formatIndex, delimiter);
  }

  public static void writeStringToFile(String contents, String filename)
      throws FileNotFoundException {

    PrintWriter outStream = new PrintWriter(filename);

    outStream.print(contents);

    outStream.flush();
    outStream.close();
  }

  public static void writeStringArrayToFile(String[] contents, String filename)
      throws FileNotFoundException {

    PrintWriter outSteam = new PrintWriter(filename);

    for (int lineIndex = 0; lineIndex < contents.length; lineIndex++) {
      outSteam.print(contents[lineIndex] + "\n");
    }

    outSteam.flush();
    outSteam.close();
  }

  public static void writeStringToFile(String contents, File filename)
      throws FileNotFoundException {

    PrintWriter outSteam = new PrintWriter(filename);

    outSteam.print(contents);

    outSteam.flush();
    outSteam.close();
  }

  public static void writeStringArrayToFile(String[] contents, File filename)
      throws FileNotFoundException {

    PrintWriter outSteam = new PrintWriter(filename);

    for (int lineIndex = 0; lineIndex < contents.length; lineIndex++) {
      outSteam.print(contents[lineIndex] + "\n");
    }

    outSteam.flush();
    outSteam.close();
  }

  public static String readTextFileToString(String basename)
      throws FileNotFoundException, IOException {

    BufferedReader fileReader = new BufferedReader(new FileReader(basename));

    String fileContents = "";

    String fileLine = fileReader.readLine();
    while (fileLine != null) {
      fileContents += fileLine + "\n";

      fileLine = fileReader.readLine();
    }
    fileReader.close();

    return fileContents;
  }

  public static String[] readTextFileToArray(String filename)
      throws FileNotFoundException, IOException {

    int nLines = FunTricks.nLinesInTextFile(filename);

    BufferedReader fileReader = new BufferedReader(new FileReader(filename));

    String[] fileContents = new String[nLines];

    int lineIndex = 0;
    String fileLine = fileReader.readLine();
    while (fileLine != null) {
      fileContents[lineIndex] = fileLine;
      lineIndex++;

      fileLine = fileReader.readLine();
      // System.out.println(fileLine);
    }
    fileReader.close();

    return fileContents;
  }

  public static String[] readSomeLinesOfTextFileToArray(String filename, int nLines)
      throws FileNotFoundException, IOException {

    BufferedReader fileReader = new BufferedReader(new FileReader(filename));

    String[] fileContents = new String[nLines];

    for (int lineIndex = 0; lineIndex < nLines; lineIndex++) {
      fileContents[lineIndex] = fileReader.readLine();
      // check for null-ness; if so, break. there are a couple reasons for this
      // a) if we've reached the end of the file, there is no need to waste time trying
      //    to read stuff that isn't there
      // b) if the file is still in the process of being written, we might
      //    hit a null (end of file at that time) and then by the time we read the next
      //    line, something more has been written
      // so, it is best just to bail as soon as any null is struck
      if (fileContents[lineIndex] == null) {
        break;
      }
    }

    fileReader.close();

    return fileContents;
  }

  public static String[] readSomeLinesOfTextFileToArray(String filename, int nLines, int nJunkLines)
      throws FileNotFoundException, IOException {

    BufferedReader fileReader = new BufferedReader(new FileReader(filename));

    String[] fileContents = new String[nLines];

    for (int lineIndex = 0; lineIndex < nJunkLines; lineIndex++) {
      fileReader.readLine();
    }

    for (int lineIndex = 0; lineIndex < nLines; lineIndex++) {
      fileContents[lineIndex] = fileReader.readLine();
      // check for null-ness; if so, break. there are a couple reasons for this
      // a) if we've reached the end of the file, there is no need to waste time trying
      //    to read stuff that isn't there
      // b) if the file is still in the process of being written, we might
      //    hit a null (end of file at that time) and then by the time we read the next
      //    line, something more has been written
      // so, it is best just to bail as soon as any null is struck
      if (fileContents[lineIndex] == null) {
        break;
      }
    }

    fileReader.close();

    return fileContents;
  }

  public static void splitMatrixTextFileByLines(
      String inputFilename, String outputFilename, int nChunks) throws Exception {
    Object[] rowsColsFormatDelimiter = MatrixOperations.readInfoFile(inputFilename);
    long nRows = (Long) rowsColsFormatDelimiter[0];
    long nCols = (Long) rowsColsFormatDelimiter[1];
    int formatIndex = (Integer) rowsColsFormatDelimiter[2];
    String delimiterString = (String) rowsColsFormatDelimiter[3];

    long[] lastRowInBlock = new long[nChunks];

    int nChunksToUse = -1;

    //      System.out.println("nRows = " + nRows);

    if (nChunks > nRows) {
      // System.out.println("splitMatrixTextFileByLines: nChunks {" + nChunks + "} > {" + nRows + "}
      // nRows; resetting nChunks to nRows");
      nChunksToUse = (int) nRows;
    } else {
      nChunksToUse = nChunks;
    }

    // set up the reader and writer...
    BufferedReader fileReader = new BufferedReader(new FileReader(inputFilename + ".txt"));
    PrintWriter outStream = null;

    for (int chunkIndex = 0; chunkIndex < nChunksToUse; chunkIndex++) {
      lastRowInBlock[chunkIndex] = nRows * (chunkIndex + 1) / nChunksToUse - 1;
      //          System.out.println("chunk #" + chunkIndex + " ends at row " +
      // lastRowInBlock[chunkIndex]);
    }

    // set up the nested loops to do the re-writing...
    long nRowsToRead = -1;
    for (int chunkIndex = 0; chunkIndex < nChunksToUse; chunkIndex++) {
      // the read/write loop
      if (chunkIndex == 0) {
        nRowsToRead = lastRowInBlock[chunkIndex] + 1;
      } else {
        nRowsToRead = lastRowInBlock[chunkIndex] - lastRowInBlock[chunkIndex - 1];
      }

      outStream = new PrintWriter(outputFilename + "_" + chunkIndex + ".txt");
      for (long rwIndex = 0; rwIndex < nRowsToRead; rwIndex++) {
        outStream.println(fileReader.readLine());
      }
      outStream.flush();
      outStream.close();
      FunTricks.writeInfoFile(
          outputFilename + "_" + chunkIndex, nRows, nCols, formatIndex, delimiterString);
    }
  }

  public static void splitMatrixTextFileByLinesInfixBeforeUnderscore(
      String inputFilename, String outputPrefix, int nChunks) throws Exception {
    Object[] rowsColsFormatDelimiter = MatrixOperations.readInfoFile(inputFilename);
    long nRows = (Long) rowsColsFormatDelimiter[0];
    long nCols = (Long) rowsColsFormatDelimiter[1];
    int formatIndex = (Integer) rowsColsFormatDelimiter[2];
    String delimiterString = (String) rowsColsFormatDelimiter[3];

    long[] lastRowInBlock = new long[nChunks];

    int nChunksToUse = -1;

    //      System.out.println("nRows = " + nRows);

    if (nChunks > nRows) {
      System.out.println(
          "splitMatrixTextFileByLines: nChunks {"
              + nChunks
              + "} > {"
              + nRows
              + "} nRows; resetting nChunks to nRows");
      nChunksToUse = (int) nRows;
    } else {
      nChunksToUse = nChunks;
    }

    // set up the reader and writer...
    BufferedReader fileReader = new BufferedReader(new FileReader(inputFilename + ".txt"));
    PrintWriter outStream = null;

    for (int chunkIndex = 0; chunkIndex < nChunksToUse; chunkIndex++) {
      lastRowInBlock[chunkIndex] = nRows * (chunkIndex + 1) / nChunksToUse - 1;
      //          System.out.println("chunk #" + chunkIndex + " ends at row " +
      // lastRowInBlock[chunkIndex]);
    }

    // Beware the MAGIC NUMBER!!! assuming unix file separators....
    String outputFirst = null;
    int lastUnderscoreIndex = inputFilename.lastIndexOf("_");
    if (lastUnderscoreIndex < 0) {
      // reset it to the end of the string....
      lastUnderscoreIndex = inputFilename.length() - 1;
    }
    if (inputFilename.lastIndexOf("/") < 0) {
      //          outputFirst = inputFilename.substring(0,inputFilename.lastIndexOf("_"));
      outputFirst = inputFilename.substring(0, lastUnderscoreIndex);
    } else {
      //          System.out.println(inputFilename.lastIndexOf("/") + " " + lastUnderscoreIndex);

      //          outputFirst =
      // inputFilename.substring(inputFilename.lastIndexOf("/"),inputFilename.lastIndexOf("_"));
      outputFirst = inputFilename.substring(inputFilename.lastIndexOf("/"), lastUnderscoreIndex);
    }

    String outputLast = inputFilename.substring(inputFilename.lastIndexOf("_") + 1);
    String outputName = null;
    // set up the nested loops to do the re-writing...
    long nRowsToRead = -1;
    for (int chunkIndex = 0; chunkIndex < nChunksToUse; chunkIndex++) {
      // the read/write loop
      if (chunkIndex == 0) {
        nRowsToRead = lastRowInBlock[chunkIndex] + 1;
      } else {
        nRowsToRead = lastRowInBlock[chunkIndex] - lastRowInBlock[chunkIndex - 1];
      }
      //          System.out.println("[" + inputFilename + "]");
      //          System.out.println("[" + outputPrefix + "][" + outputFirst + "][" + outputLast +
      // "]");
      outputName = outputPrefix + outputFirst + "_" + chunkIndex + "_" + outputLast;
      outStream = new PrintWriter(outputName + ".txt");
      for (long rwIndex = 0; rwIndex < nRowsToRead; rwIndex++) {
        outStream.print(fileReader.readLine() + "\n");
      }
      outStream.flush();
      outStream.close();
      FunTricks.writeInfoFile(outputName, nRowsToRead, nCols, formatIndex, delimiterString);
    }
  }

  public static void reassembleMatrixTextFileByLinesInfixBeforeUnderscore(
      String inputFilenameNoInfix, String outputPrefix, int nChunks) throws Exception {
    // figure out the name of the info file for the first chunk we wish to consider...
    String chunkFirst = inputFilenameNoInfix.substring(0, inputFilenameNoInfix.lastIndexOf("_"));

    //      chunkFirst = inputFilenameNoInfix.substring(0,inputFilenameNoInfix.lastIndexOf("_"));

    String chunkLast = inputFilenameNoInfix.substring(inputFilenameNoInfix.lastIndexOf("_") + 1);

    String chunkName = chunkFirst + "_" + 0 + "_" + chunkLast;

    // System.out.println("input no infix = [" + inputFilenameNoInfix + "]");

    Object[] rowsColsFormatDelimiter = MatrixOperations.readInfoFile(chunkName);

    long nCols = (Long) rowsColsFormatDelimiter[1];
    int formatIndex = (Integer) rowsColsFormatDelimiter[2];
    String delimiterString = (String) rowsColsFormatDelimiter[3];

    // set up the reader and writer...
    BufferedReader fileReader = null;

    String chunkBaseFirst = null;
    if (inputFilenameNoInfix.lastIndexOf("/") < 0) {
      chunkBaseFirst = inputFilenameNoInfix.substring(0, inputFilenameNoInfix.lastIndexOf("_"));
    } else {
      chunkBaseFirst =
          inputFilenameNoInfix.substring(
              inputFilenameNoInfix.lastIndexOf("/") + 1, inputFilenameNoInfix.lastIndexOf("_"));
    }

    String outputName = outputPrefix + chunkBaseFirst + "_" + chunkLast;

    // System.out.println("[" + outputPrefix + "][" + chunkBaseFirst + "][" + chunkLast + "]");
    // System.out.println("output prefix = " + outputName);

    // imperfect check if all the chunks are there...
    for (int chunkIndex = 0; chunkIndex < nChunks; chunkIndex++) {
      chunkName = chunkFirst + "_" + chunkIndex + "_" + chunkLast;

      if (!(new File(chunkName + ".info.txt").exists())) {
        System.out.println(chunkName + ".info.txt has failed to exist. bailing...");
        throw new Exception();
      }
    }

    PrintWriter outStream = new PrintWriter(outputName + ".txt");

    long nRows = 0; // we need to accumulate this
    long nRowsThis = -1;
    for (int chunkIndex = 0; chunkIndex < nChunks; chunkIndex++) {
      chunkName = chunkFirst + "_" + chunkIndex + "_" + chunkLast;

      rowsColsFormatDelimiter = MatrixOperations.readInfoFile(chunkName);
      nRowsThis = (Long) rowsColsFormatDelimiter[0];
      nRows += nRowsThis;

      fileReader = new BufferedReader(new FileReader(chunkName + ".txt"));
      for (long rowIndex = 0; rowIndex < nRowsThis; rowIndex++) {
        outStream.println(fileReader.readLine());
      }
      fileReader.close();
    }

    outStream.flush();
    outStream.close();
    FunTricks.writeInfoFile(outputName, nRows, nCols, formatIndex, delimiterString);
  }

  public static String[] readLineToArrayIgnoreRepeated(String line, String delimiter) {
    String[] splitLine = line.split(delimiter);

    //      System.out.println("original = [" + line + "]");
    //      for (int splitIndex = 0; splitIndex < splitLine.length; splitIndex++) {
    //          System.out.println("[" + splitIndex + "] = [" + splitLine[splitIndex] + "]");
    //      }

    // now count up the number of non-empties
    int nValid = 0;
    for (int splitIndex = 0; splitIndex < splitLine.length; splitIndex++) {
      if (!splitLine[splitIndex].isEmpty()) {
        //              System.out.println("FULL: [" + splitIndex + "] = [" + splitLine[splitIndex]
        // + "]");
        nValid++;
      }
    }

    String[] onlyValid = new String[nValid];
    int storageIndex = 0;
    for (int splitIndex = 0; splitIndex < splitLine.length; splitIndex++) {
      if (!splitLine[splitIndex].isEmpty()) {
        onlyValid[storageIndex] = splitLine[splitIndex];
        storageIndex++;
      }
    }

    //      for (int splitIndex = 0; splitIndex < nValid; splitIndex++) {
    //          System.out.println("[" + splitIndex + "] = [" + onlyValid[splitIndex] + "]");
    //      }

    return onlyValid;
  }

  public static String[] readLineToArrayOnArbitrarySpaceOrTab(String inLine) {

    // run through the string to figure out how many non-white stretches there are
    int nWords = 0;
    int thisCharIndex = 0;

    boolean inAWordBefore = false;
    boolean inAWordNow = false;

    //      for (int thisCharIndex = 0; thisCharIndex < inLine.length(); thisCharIndex++) {
    while (thisCharIndex < inLine.length()) {
      if (inLine.charAt(thisCharIndex) == ' ' || inLine.charAt(thisCharIndex) == '\t') {
        inAWordNow = false;
      } else {
        inAWordNow = true;
      }

      //          if (        inAWordNow && inAWordBefore) {
      //              // do nothing; word continues
      //          } else if ( inAWordNow && !inAWordBefore) {
      //              nWords++;
      //          } else if (!inAWordNow &&  inAWordBefore) {
      //              // we just exited a word
      //          } else if (!inAWordNow && !inAWordBefore) {
      //              // do nothing; whitespace continues
      //          }

      // the only case that matters is inAWordNow && !inAWordBefore
      if (inAWordNow && !inAWordBefore) {
        nWords++;
      }

      // reset the trailing tracker
      thisCharIndex++;
      inAWordBefore = inAWordNow;
    }

    int thisWord = -1; // actually needs to be negative one so that the first word we hit will be #0
    int beginWordIndex = -1;
    int endWordIndex = -2;
    String[] lineAsArray = new String[nWords];

    // run through again and extract them to the array
    // reset word booleans and index
    inAWordBefore = false;
    inAWordNow = false;
    thisCharIndex = 0;
    while (thisCharIndex < inLine.length()) {
      if (inLine.charAt(thisCharIndex) == ' ' || inLine.charAt(thisCharIndex) == '\t') {
        inAWordNow = false;
      } else {
        inAWordNow = true;
      }

      if (inAWordNow && !inAWordBefore) {
        // count which word we're on
        thisWord++;

        // mark beginning of word
        beginWordIndex = thisCharIndex;
      } else if (!inAWordNow && inAWordBefore) {
        // we just exited a word
        endWordIndex = thisCharIndex;

        // write it down
        //              System.out.println("word #" + thisWord + "; b = " + beginWordIndex + "; e =
        // " + endWordIndex + "; contents [" + inLine.substring(beginWordIndex,endWordIndex) + "]");
        lineAsArray[thisWord] = inLine.substring(beginWordIndex, endWordIndex);
      } else if (inAWordNow && thisCharIndex == inLine.length() - 1) {
        // we are at the end of the string, are in a word, and haven't hit whitespace
        // so, we want to keep this word

        // write it down
        //              System.out.println("word #" + thisWord + "; b = " + beginWordIndex + "; no e
        // ; contents [" + inLine.substring(beginWordIndex) + "]");
        lineAsArray[thisWord] = inLine.substring(beginWordIndex);
      }

      // reset the trailing tracker
      thisCharIndex++;
      inAWordBefore = inAWordNow;
    }

    return lineAsArray;
  }

  public static String onlySomeDecimalPlacesKeepTrailingZeros(double value, int nDecimals) {
    NumberFormat formatForNumber = NumberFormat.getInstance();

    String patternFormat = "#0.";
    for (int index = 0; index < nDecimals; index++) {
      patternFormat += "0";
    }

    //      System.out.println("onlySomeDecimalPlaces: patter format = [" + patternFormat + "]");

    ((DecimalFormat) formatForNumber).applyPattern(patternFormat);

    // Beware the MAGIC NUMBER!!!
    // somehow, we have to handle the "bad" double values....
    if (Double.isInfinite(value) || Double.isNaN(value)) {
      return "-0.01";
    }

    return formatForNumber.format(value);
  }

  public static String onlySomeDecimalPlaces(double value, int nDecimals) {
    NumberFormat formatForNumber = NumberFormat.getInstance();

    String patternFormat = "#0.";
    for (int index = 0; index < nDecimals; index++) {
      patternFormat += "0";
    }

    ((DecimalFormat) formatForNumber).applyPattern(patternFormat);

    // Beware the MAGIC NUMBER!!!
    // somehow, we have to handle the "bad" double values....
    if (Double.isInfinite(value) || Double.isNaN(value)) {
      return "-0.01";
    }

    String withPossibleExtraZeros = formatForNumber.format(value);
    String cleanedString = withPossibleExtraZeros;
    String digitToConsider = null;
    boolean foundNonZero = false;
    // now work backward to get rid of any trailing zeros
    if (nDecimals > 0) {
      for (int charIndex = withPossibleExtraZeros.length() - 1;
          charIndex > withPossibleExtraZeros.length() - nDecimals - 1;
          charIndex--) {
        digitToConsider = withPossibleExtraZeros.substring(charIndex, charIndex + 1);
        //          System.out.println("sv=[" + withPossibleExtraZeros + "], index to consider=[" +
        // charIndex + "], digit to consider=[" + digitToConsider + "]");
        if (!digitToConsider.equalsIgnoreCase("0")) {
          //              System.out.println("inside conditional, found non-zero");
          cleanedString = withPossibleExtraZeros.substring(0, charIndex + 1);
          foundNonZero = true;
          break;
        }
      }
      if (!foundNonZero) {
        // we should only get here if the decimal part consists entirely of zeros
        //              System.out.println("after for loop, apparently unbroken");
        cleanedString = withPossibleExtraZeros.substring(0, withPossibleExtraZeros.indexOf("."));
      }
    }

    return cleanedString;
  }

  public static String padStringWithLeadingSpaces(String original, int totalLength) {

    String paddedString = "";

    if (original.length() >= totalLength) {
      paddedString = original;
    } else {
      int nSpacesNeeded = totalLength - original.length();
      for (int padIndex = 0; padIndex < nSpacesNeeded; padIndex++) {
        paddedString += " ";
      }
      paddedString = paddedString + original;
    }

    return paddedString;
  }

  public static String padStringWithLeadingZeros(String original, int totalLength) {

    String paddedString = "";

    if (original.length() >= totalLength) {
      paddedString = original;
    } else {
      int nSpacesNeeded = totalLength - original.length();
      for (int padIndex = 0; padIndex < nSpacesNeeded; padIndex++) {
        paddedString += "0";
      }
      paddedString = paddedString + original;
    }

    return paddedString;
  }

  public static String fitInFiveCharacters(double value) {

    String numberPart = null;
    String finalString = null;
    // now check how big the number is...
    if (value <= -100) {
      // it either won't fit (in which case we won't bother failing gracefully
      // or it will only fit the integer part
      //          finalString = FunTricks.onlySomeDecimalPlaces(value, 0);
      numberPart = Long.toString(Math.round(value));
      finalString = FunTricks.padStringWithLeadingSpaces(numberPart, 5);
    } else if (value <= -10) {
      // we can fit one decimal place
      numberPart = FunTricks.onlySomeDecimalPlaces(value, 1);
      finalString = FunTricks.padStringWithLeadingSpaces(numberPart, 5);
    } else if (value < 0) {
      // we can fit two decimal places
      numberPart = FunTricks.onlySomeDecimalPlaces(value, 2);
      finalString = FunTricks.padStringWithLeadingSpaces(numberPart, 5);
    } else if (value >= 1000) {
      // it either won't fit (in which case we won't bother failing gracefully
      // or it will only fit the integer part
      //          finalString = FunTricks.onlySomeDecimalPlaces(value, 0);
      numberPart = Long.toString(Math.round(value));
      finalString = FunTricks.padStringWithLeadingSpaces(numberPart, 5);
    } else if (value >= 100) {
      // we can fit one decimal place
      numberPart = FunTricks.onlySomeDecimalPlaces(value, 1);
      finalString = FunTricks.padStringWithLeadingSpaces(numberPart, 5);
    } else if (value >= 10) {
      // we can fit two decimal places
      numberPart = FunTricks.onlySomeDecimalPlaces(value, 2);
      finalString = FunTricks.padStringWithLeadingSpaces(numberPart, 5);
    } else {
      // we can fit three decimal places
      numberPart = FunTricks.onlySomeDecimalPlaces(value, 3);
      finalString = FunTricks.padStringWithLeadingSpaces(numberPart, 5);
    }

    return finalString;
  }

  public static String fitInNCharacters(double value, int n) throws Exception {
    if (n < 4) {
      System.out.println("you asked for value = " + value + " to go in " + n + " characters.");
      System.out.println(
          "it doesn't make sense for n < 4. e.g., we want to be able to handle -0.1");
      throw new Exception();
    }

    String correctLengthString = null;
    // let's try a totally different brute force approach.
    String valueAsString = Double.toString(value);

    int originalLength = valueAsString.length();

    // maybe we're ok to begin with...
    if (originalLength == n) {
      // we're done!
      correctLengthString = valueAsString;
      return correctLengthString;
    }

    // or if it is super short, we just add initial spaces and we're good
    if (originalLength < n) {
      correctLengthString = FunTricks.padStringWithLeadingSpaces(valueAsString, n);
      return correctLengthString;
    }

    // the real work goes here: too much information, so it needs to get truncated somehow...
    // what happens if we just cut it off? well, we might end up with rounding errors.

    // let's try counting up:
    int nDecimalPlacesToUse = 0;
    //      String previousResult = FunTricks.onlySomeDecimalPlaces(value, nDecimalPlacesToUse);
    String previousResult =
        FunTricks.onlySomeDecimalPlacesKeepTrailingZeros(value, nDecimalPlacesToUse);
    // trim off the decimal point
    previousResult = previousResult.substring(0, previousResult.lastIndexOf("."));
    String partialResult =
        FunTricks.onlySomeDecimalPlacesKeepTrailingZeros(value, nDecimalPlacesToUse);
    while (partialResult.length() <= n) {
      previousResult = partialResult;

      partialResult =
          FunTricks.onlySomeDecimalPlacesKeepTrailingZeros(value, ++nDecimalPlacesToUse);
      //          System.out.println("v=" + value + "; nDecimalPlacesToUse=" + nDecimalPlacesToUse +
      // "; previous=[" + previousResult + "]; partialResult=[" + partialResult + "]");
    }

    // hopefully, when we get here, the "previous result" will be the right length...
    // but we might need to make up for extra space at the front...
    correctLengthString = FunTricks.padStringWithLeadingSpaces(previousResult, n);

    return correctLengthString;
  }

  public static String fitInNCharactersOLD(double value, int n) throws Exception {

    if (n < 4) {
      System.out.println("you asked for value = " + value + " to go in " + n + " characters.");
      System.out.println(
          "it doesn't make sense for n < 4. e.g., we want to be able to handle -0.1");
      throw new Exception();
    }
    boolean haveString = false;
    String numberPart = null;
    String finalString = null;
    // now check how big the number is...
    if (value <= -Math.pow(10.0, n - 3)) {
      // it either won't fit (in which case we won't bother failing gracefully
      // or it will only fit the integer part
      numberPart = Long.toString(Math.round(value));
      finalString = FunTricks.padStringWithLeadingSpaces(numberPart, n);
      haveString = true;
    } else {

      // the negative numbers
      for (int tenPower = (n - 4); tenPower > 0; tenPower--) {
        if (value <= -Math.pow(10.0, tenPower)) {
          // we can fit a few decimal places
          finalString = FunTricks.onlySomeDecimalPlaces(value, n - tenPower - 3);
          haveString = true;
          break;
        }
      }
    }

    if (!haveString && value >= Math.pow(10.0, n - 2)) {
      // it either won't fit (in which case we won't bother failing gracefully
      // or it will only fit the integer part
      numberPart = Long.toString(Math.round(value));
      finalString = FunTricks.padStringWithLeadingSpaces(numberPart, n);
      haveString = true;
    } else {
      // the positive numbers
      for (int tenPower = (n - 3); tenPower > 0; tenPower--) {
        //              System.out.println("10pow = " + tenPower + "; n = " + n + "; can fit test =
        // " + (n - tenPower - 2));
        if (value >= Math.pow(10.0, tenPower)) {
          // we can fit a few decimal places
          finalString = FunTricks.onlySomeDecimalPlaces(value, n - tenPower - 2);
          //                  finalString = FunTricks.padStringWithLeadingSpaces(numberPart, n);
          haveString = true;
          break;
        }
      }
    }

    // the single digit cases...
    if (!haveString) {
      if (value == -0.0) {
        //              System.out.println("negative zero");
        finalString = FunTricks.onlySomeDecimalPlaces(0.0, n - 2);
      } else if (value == 0.0) {
        //              System.out.println("positive zero");
        finalString = FunTricks.onlySomeDecimalPlaces(0.0, n - 2);
      } else if (value < 0) {
        finalString = FunTricks.onlySomeDecimalPlaces(value, n - 3);
        //              finalString = FunTricks.padStringWithLeadingSpaces(numberPart, n);
      } else {
        finalString = FunTricks.onlySomeDecimalPlaces(value, n - 2);
        //              finalString = FunTricks.padStringWithLeadingSpaces(numberPart, n);
      }
    }

    // ok, there is a funny case when you have something like -9.999999906077472 and 5 characters,
    // it will
    // end up rounding to -10.00; rather than figure out the "right way", i'm just going to manually
    // correct it
    // at the end here.
    //      if (finalString.length() != n) {
    //          int nDecimalPlacesInFinalString = finalString.length() - finalString.indexOf(".") -
    // 1;
    //          double backToDouble = Double.parseDouble(finalString);
    //          // the idea is that this is usually a problem with negative numbers (positive
    // numbers just
    //          // run out of space in our tiny mantissa), so we add a tiny bit to bring it closer
    // to zero
    //          // and hence
    ////            double newValue = backToDouble + Math.pow(10.0, -nDecimalPlacesInFinalString);
    //          // let's just try to cut back the number of decimal places a little
    //          if (nDecimalPlacesInFinalString)
    //          String newFinalString = FunTricks.onlySomeDecimalPlaces(value,
    // nDecimalPlacesInFinalString - 1);
    //
    //          System.out.println("      fitInNCharacters funny case [" + value + "] in " + n +
    //                  " --> [" + finalString + "] nDPIFS=" + nDecimalPlacesInFinalString + ";
    // newValue=" + newValue);
    //
    //      }
    return finalString;
  }

  public static String[] parseRepeatedDelimiterToArray(String lineContents, String delimiter) {

    // this lets you have multiple delimiters between fields to be ignored. the motivation is to
    // be able to take fixed spaced outputs and parse them like they are delimited because they
    // might
    // have multiple spaces between the values...

    // the irregularity of the output format is beginning to stink badly enough that i am going to
    // brute force
    // a parser...
    String[] rawSplitLine = lineContents.split(delimiter);

    // now count up the number of non-empties
    int nValid = 0;
    for (int splitIndex = 0; splitIndex < rawSplitLine.length; splitIndex++) {
      if (!rawSplitLine[splitIndex].isEmpty()) {
        nValid++;
      }
    }
    String[] onlyValid = new String[nValid];
    int storageIndex = 0;
    for (int splitIndex = 0; splitIndex < rawSplitLine.length; splitIndex++) {
      if (!rawSplitLine[splitIndex].isEmpty()) {
        onlyValid[storageIndex] = rawSplitLine[splitIndex];
        storageIndex++;
      }
    }

    return onlyValid;
  }

  public static String statusCheck(int currentCaseIndex, int nTotalCases, int nChecks)
      throws Exception {

    String statusString = null;

    if (currentCaseIndex % (nTotalCases / nChecks + 1) == 0) {
      statusString =
          currentCaseIndex
              + "/"
              + nTotalCases
              + " = "
              + FunTricks.fitInNCharacters(100 * (currentCaseIndex + 1.0) / nTotalCases, 5)
              + "%";
    }

    return statusString;
  }

  public static String statusCheck(
      int currentCaseIndex, int nTotalCases, int nChecks, long startTimeMillis) throws Exception {

    String statusString = null;

    if (currentCaseIndex % (nTotalCases / nChecks + 1) == 0) {
      double elapsedTimeSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000.0;
      double remainingTimeSeconds =
          elapsedTimeSeconds / (currentCaseIndex + 1) * (nTotalCases - currentCaseIndex - 1);
      double remainingTimeMinutes = remainingTimeSeconds / 60.0;

      statusString =
          currentCaseIndex
              + "/"
              + nTotalCases
              + " = "
              + FunTricks.fitInNCharacters(100 * (currentCaseIndex + 1.0) / nTotalCases, 5)
              + "% at "
              + FunTricks.onlySomeDecimalPlaces(elapsedTimeSeconds, 1)
              + "; to go = "
              + FunTricks.onlySomeDecimalPlaces(remainingTimeSeconds, 1)
              + "s/"
              + FunTricks.onlySomeDecimalPlaces(remainingTimeMinutes, 1)
              + "m";
    }

    return statusString;
  }

  public static void dumpDoubleArray(double[] arrayToDump, String label) {
    for (int dumpIndex = 0; dumpIndex < arrayToDump.length; dumpIndex++) {
      System.out.println(label + " [" + dumpIndex + "]=" + arrayToDump[dumpIndex]);
    }
  }

  public static void dumpCollectionOfDoubleArrays(double[][] arraysToDump, String[] labels) {
    for (int labelIndex = 0; labelIndex < labels.length; labelIndex++) {
      System.out.print(labels[labelIndex] + "\t");
    }
    System.out.println();
    for (int dumpIndex = 0; dumpIndex < arraysToDump[0].length; dumpIndex++) {
      System.out.print("[" + dumpIndex + "] =\t");
      for (int labelIndex = 0; labelIndex < labels.length; labelIndex++) {
        System.out.print(arraysToDump[labelIndex][dumpIndex] + "\t");
      }
      System.out.println();
    }
  }
}
