package org.R2Useful;

import java.io.*;

public class MultiFormatFloat implements Serializable {

  // Beware the MAGIC ASSUMPTIONS!!!

  private String infoFileSuffix = new String("_info");
  // final int BufferSize = 32000;
  // final int BufferSize = 3000;
  // int                   BufferSize           = 4096;
  // int                   BufferSize           = 1310720; // 10MB worth of elements
  // int                   BufferSize           = 8192; // MB worth of elements
  // 16384
  private int BufferSize = 16384; // MB worth of elements

  private String fileDefaultBase = "Float";

  private ByteArrayOutputStream byteArrayOutputStream;
  private ObjectOutputStream objectOutputStream;
  private ByteArrayInputStream byteArrayInputStream;
  private ObjectInputStream objectInputStream;

  private int dataFormat;

  /*
   * final int dataInNativeArray = 0; final int dataInVector = 1; final int
   * dataInVectorFile = 2; final int dataInObjectFile = 3;
   */

  // Beware the MAGIC NUMBERS!!! these are getting hardcoded to avoid memory
  // bottlenecks...
  public static final int dataInNativeArray = 0;
  public static final int dataInVector = 1;
  public static final int dataInVectorFile = 2;
  public static final int dataInObjectFile = 3;

  private final int magicBytesPerElement =
      4; // Beware the MAGIC NUMBER!!! here floats, so 4B/element

  private int numDimensions;
  private long[] dimensions;
  private long dataVectorLength = -1;
  private Object dataNativeArray = null;

  private String fileName;
  private File file;
  private RandomAccessFile randomAccessFile;

  private long serializedBufferSize = -1;
  private int lastBufferIndex = -1;
  private long lastVectorIndex = -1;
  private float[] readWriteFloatBuffer;
  private byte[] readWriteByteBuffer;
  private boolean bufferChangedFlag;

  private boolean garbageCollectMeFlag = true;
  private boolean permanentStyleFlag = false;

  public MultiFormatFloat() {}

  private void writeObject(java.io.ObjectOutputStream out) throws IOException, Exception {
    if (dataFormat == dataInVectorFile) {
      throw new Exception();
    }

    //    System.out.println("MFM: starting a writeObject serialization...");
    // send the data format...
    out.writeInt(dataFormat);
    // send the dimensions...
    out.writeObject(dimensions);
    out.writeInt(numDimensions);

    if (dataFormat == dataInNativeArray) {
      out.writeObject(dataNativeArray);
    } else if (dataFormat == dataInVector) {
      // it is in vector...
      out.writeObject(readWriteFloatBuffer);
      out.writeLong(dataVectorLength);
    } else if (dataFormat == dataInObjectFile) {
      out.writeLong(dataVectorLength);
      out.writeInt(BufferSize);
      int numBuffers = (int) ((dataVectorLength - 1) / BufferSize) + 1;
      for (int i = 0; i < numBuffers; i++) {
        readBlock(i, 0);
        out.writeObject(readWriteFloatBuffer);
      }
    }

    // and the garbage collection flag...
    out.writeBoolean(garbageCollectMeFlag);
    out.writeBoolean(permanentStyleFlag);
    //    System.out.println("MFM: ending a writeObject serialization...");
  }

  private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException, Exception {
    //    System.out.println("MFM: starting a readObject de-serialization...");

    // read the data format...
    dataFormat = in.readInt();
    // read the dimensions...
    dimensions = (long[]) in.readObject();
    numDimensions = in.readInt();

    if (dataFormat == dataInNativeArray) {
      dataNativeArray = in.readObject();
    } else if (dataFormat == dataInVector) {
      // it is in vector...
      readWriteFloatBuffer = (float[]) in.readObject();
      dataVectorLength = in.readLong();
    } else if (dataFormat == dataInObjectFile) {
      // read in the necessary parameters
      dataVectorLength = in.readLong();
      BufferSize = in.readInt();

      // set up the necessary variables
      fileName = "Matrix" + this.hashCode();
      file = new File(fileName);
      randomAccessFile = new RandomAccessFile(file, "rw");

      int numBuffers = (int) ((dataVectorLength - 1) / BufferSize) + 1;
      readWriteFloatBuffer = new float[BufferSize];

      // copy over the goodies
      for (int i = 0; i < numBuffers; i++) {
        readWriteFloatBuffer = (float[]) in.readObject();
        writeBlock(i);
      }

      lastBufferIndex = numBuffers - 1;
      bufferChangedFlag = false;
    }

    garbageCollectMeFlag = in.readBoolean();

    permanentStyleFlag = in.readBoolean();
    //    System.out.println("MFM: ending a readObject de-serialization...");
  }

  public Object clone() {
    MultiFormatFloat tempClone = null;
    try {
      tempClone = new MultiFormatFloat(this.dataFormat, this.dimensions);

      long[] currentIndex = new long[this.numDimensions];
      boolean carryOn = true;
      float tempFloat = -3.0F;
      int nDims = this.numDimensions;
      long[] theDims = this.dimensions;

      while (carryOn) {

        tempFloat = this.getValue(currentIndex);
        tempClone.setValue(currentIndex, tempFloat);

        // determine the last dimension that can be bumped up...
        // all the "-1"s are because we are dealing in indices...
        for (int dimIndex = nDims - 1; dimIndex >= 0; dimIndex--) {
          if (currentIndex[dimIndex] < theDims[dimIndex] - 1) {
            currentIndex[dimIndex]++;
            for (int prevDimIndex = nDims - 1; prevDimIndex > dimIndex; prevDimIndex--) {
              currentIndex[prevDimIndex] = 0;
            }
            carryOn = true;
            break;
          }
          carryOn = false;
        } // end for
      } // end while

    } catch (Exception e) {
      System.out.println("problems creating MFF clone...");
      e.printStackTrace();
    }
    return tempClone;
  }

  public void setGarbageCollectionMode(boolean newValue) {
    garbageCollectMeFlag = newValue;
  }

  public void writeBlock(int index) throws Exception {

    byteArrayOutputStream = new ByteArrayOutputStream();
    objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

    objectOutputStream.writeObject(readWriteFloatBuffer);
    readWriteByteBuffer = byteArrayOutputStream.toByteArray();

    serializedBufferSize = readWriteByteBuffer.length;

    // System.out.println("writeBlock bufferIndex = " + bufferIndex + "
    // serializedBufferSize = " + serializedBufferSize);

    randomAccessFile.seek(index * serializedBufferSize);
    randomAccessFile.write(readWriteByteBuffer);
  }

  public void readBlock(int bufferIndex, long vectorIndex) throws Exception {

    // System.out.println("In readBlock bufferIndex = " + bufferIndex + "
    // serializedBufferSize = " + serializedBufferSize);

    randomAccessFile.seek(bufferIndex * serializedBufferSize);
    //    randomAccessFile.seek((long)bufferIndex * serializedBufferSize);

    randomAccessFile.readFully(readWriteByteBuffer);

    byteArrayInputStream = new ByteArrayInputStream(readWriteByteBuffer);
    objectInputStream = new ObjectInputStream(byteArrayInputStream);

    readWriteFloatBuffer = (float[]) objectInputStream.readObject();

    lastVectorIndex = vectorIndex;
    lastBufferIndex = bufferIndex;
    bufferChangedFlag = false;
  }

  //  public void initialize(int dataFormat, int[] IntDimensions) throws Exception {
  //
  //    long[] LongDimensions = new long[IntDimensions.length];
  //    for (int i = 0; i < IntDimensions.length; i++) {
  //      LongDimensions[i] = IntDimensions[i];
  //    }
  //    initialize(dataFormat, LongDimensions);
  //  }

  public void initialize(int dataFormat, long[] dimensions) throws Exception {

    this.dataFormat = dataFormat;
    this.dimensions = dimensions;
    this.numDimensions = dimensions.length;

    // calcluate length of data vector if necessary
    dataVectorLength = dimensions[0];
    for (int dimIndex = 1; dimIndex < this.numDimensions; dimIndex++) {
      dataVectorLength *= dimensions[dimIndex];
    }

    // allocate space
    switch (dataFormat) {

        // case dataInNativeArray:
      case 0:
        // Beware the MAGIC NUMBER!!! 0 = dataInNativeArray
        switch (numDimensions) {
          case 1:
            dataNativeArray = new float[(int) dimensions[0]];
            break;
          case 2:
            dataNativeArray = new float[(int) dimensions[0]][(int) dimensions[1]];
            break;
          case 3:
            dataNativeArray =
                new float[(int) dimensions[0]][(int) dimensions[1]][(int) dimensions[2]];
            break;
          case 4:
            dataNativeArray =
                new float[(int) dimensions[0]][(int) dimensions[1]][(int) dimensions[2]]
                    [(int) dimensions[3]];
            break;
          default:
            System.out.println("Error!  Invalid number of dimensions (" + numDimensions + ").");
            break;
        }
        break;

        // case dataInVector:
      case 1:
        // Beware the MAGIC NUMBER!!! 1=dataInVector
        if (dataVectorLength > Integer.MAX_VALUE) {
          System.out.println("dataVectorLength [" + dataVectorLength + "] > Integer.MAX_VALUE.");
        } else {
          readWriteFloatBuffer = new float[(int) dataVectorLength];
        }
        break;

        // case dataInVectorFile:
      case 2:
        // Beware the MAGIC NUMBER!!! 2=dataInVectorFile
        fileName = this.fileDefaultBase + this.hashCode();
        try {
          file = new File(fileName);
          randomAccessFile = new RandomAccessFile(file, "rw");
          for (long i = 0; i < dataVectorLength; i++) {
            randomAccessFile.writeFloat(0.0F);
          }
        } catch (Exception e) {
          System.out.println("Error!  File (" + fileName + ") could not be initialized.");
        }

        try {
          randomAccessFile.seek(0L);
          lastVectorIndex = 0;
        } catch (Exception e) {
          System.out.println("Error!  randomAccessFile.seek(0L) failed");
        }

        break;

        // case dataInObjectFile:
      case 3:
        // Beware the MAGIC NUMBER!!! 3=dataInObjectFile

        //      fileName = "Matrix" + this.hashCode();
        fileName = this.fileDefaultBase + this.hashCode();

        readWriteFloatBuffer = new float[BufferSize];

        file = new File(fileName);
        randomAccessFile = new RandomAccessFile(file, "rw");

        // this should be unnecessary as it should be initialized to all zeros anyway...
        //      for (int i = 0; i < readWriteFloatBuffer.length; i++) {
        //        readWriteFloatBuffer[i] = 0.0F;
        //      }

        int numBuffers = (int) ((dataVectorLength - 1) / BufferSize) + 1;
        for (int i = 0; i < numBuffers; i++) {
          writeBlock(i);
        }

        try {
          randomAccessFile.seek(0L);
          lastVectorIndex = 0;
        } catch (Exception e) {
          System.out.println("Error!  randomAccessFile.seek(0L) failed");
        }

        lastBufferIndex = 0;
        lastVectorIndex = 0;
        bufferChangedFlag = false;
        break;
      default:
        System.out.println("Error!  Unknown dataFormat.");
        break;
    }
  }

  public void initializePermanent(int dataFormat, long[] dimensions, String path) throws Exception {

    this.permanentStyleFlag = true;

    // Beware the MAGIC ASSUMPTION!!! we are going to define the info/header file's suffix that will
    // be tacked onto
    // the name of the matrix...

    this.dataFormat = dataFormat;
    this.dimensions = dimensions;
    this.numDimensions = dimensions.length;

    File infoFileToWrite;
    FileWriter outInfoStream;
    PrintWriter outoutout;

    // calcluate length of data vector if necessary
    if (dataFormat == dataInVectorFile || dataFormat == dataInObjectFile) {

      dataVectorLength = dimensions[0];
      for (int dimIndex = 1; dimIndex < numDimensions; dimIndex++) {
        dataVectorLength *= dimensions[dimIndex];
      }
    } else {
      System.err.println(
          "dataFormat is "
              + dataFormat
              + "which is an \"in memory\" format. This constructor "
              + "is for on-disk representations...");
      throw new Exception();
    }

    garbageCollectMeFlag = false;

    // allocate space
    switch (dataFormat) {
        // cases 0 and 1 are gone due to being in-memory representations. the idiot-proofing should
        // take care of this.
        // case dataInNativeArray:
        // case dataInVectorFile:
      case 2:
        // Beware the MAGIC NUMBER!!! 2=dataInVectorFile
        fileName = path;
        // we want a new matrix and hence a new file...

        // create the file and fill with zeros...
        file = new File(fileName);
        randomAccessFile = new RandomAccessFile(file, "rw");
        for (long i = 0; i < dataVectorLength; i++) {
          randomAccessFile.writeFloat(0.0F);
        }

        // reset to the beginning of the file...
        randomAccessFile.seek(0L);
        lastVectorIndex = 0;

        // create the header/info file...
        infoFileToWrite = new File(path + infoFileSuffix);
        outInfoStream = new FileWriter(infoFileToWrite);
        outoutout = new PrintWriter(outInfoStream);

        outoutout.println(dataFormat);
        outoutout.println(numDimensions);
        for (int dimIndex = 0; dimIndex < numDimensions; dimIndex++) {
          outoutout.println(" " + dimensions[dimIndex]);
        }
        outoutout.println(dataVectorLength);
        outoutout.println(garbageCollectMeFlag);
        outoutout.println(serializedBufferSize);

        // clean up...
        outoutout.flush();
        outoutout.close();
        outInfoStream.close();

        break;

        // case dataInObjectFile:
      case 3:
        // Beware the MAGIC NUMBER!!! 3=dataInObjectFile

        fileName = path;
        readWriteFloatBuffer = new float[BufferSize];
        int numBuffers = (int) ((dataVectorLength - 1) / BufferSize) + 1;

        file = new File(fileName);
        randomAccessFile = new RandomAccessFile(file, "rw");

        // create all the blocks of zeros
        for (int i = 0; i < numBuffers; i++) {
          writeBlock(i);
        }

        // reset ourselves to the top...
        randomAccessFile.seek(0L);
        lastVectorIndex = 0;

        lastBufferIndex = 0;
        lastVectorIndex = 0;
        bufferChangedFlag = false;

        // write out the info file....
        // create the header/info file...
        infoFileToWrite = new File(path + infoFileSuffix);
        outInfoStream = new FileWriter(infoFileToWrite);
        outoutout = new PrintWriter(outInfoStream);

        outoutout.println(dataFormat);
        outoutout.println(numDimensions);
        for (int dimIndex = 0; dimIndex < numDimensions; dimIndex++) {
          outoutout.println(" " + dimensions[dimIndex]);
        }
        outoutout.println(dataVectorLength);
        outoutout.println(BufferSize);
        outoutout.println(garbageCollectMeFlag);
        outoutout.println(serializedBufferSize);

        // clean up...
        outoutout.flush();
        outoutout.close();
        outInfoStream.close();

        break;

      default:
        System.out.println("Error!  Unknown dataFormat.");
        break;
    }
  }

  public void initializePermanentWithValue(
      int dataFormat, long[] dimensions, String path, float initialValue) throws Exception {

    this.permanentStyleFlag = true;

    // Beware the MAGIC ASSUMPTION!!! we are going to define the info/header file's suffix that will
    // be tacked onto
    // the name of the matrix...

    this.dataFormat = dataFormat;
    this.dimensions = dimensions;
    this.numDimensions = dimensions.length;

    File infoFileToWrite;
    FileWriter outInfoStream;
    PrintWriter outoutout;

    // calcluate length of data vector if necessary
    if (dataFormat == dataInVectorFile || dataFormat == dataInObjectFile) {

      dataVectorLength = dimensions[0];
      for (int dimIndex = 1; dimIndex < numDimensions; dimIndex++) {
        dataVectorLength *= dimensions[dimIndex];
      }
    } else {
      System.err.println(
          "dataFormat is "
              + dataFormat
              + "which is an \"in memory\" format. This constructor "
              + "is for on-disk representations...");
      throw new Exception();
    }

    garbageCollectMeFlag = false;

    // allocate space
    switch (dataFormat) {
        // cases 0 and 1 are gone due to being in-memory representations. the idiot-proofing should
        // take care of this.
        // case dataInNativeArray:
        // case dataInVectorFile:
      case 2:
        // Beware the MAGIC NUMBER!!! 2=dataInVectorFile
        fileName = path;
        // we want a new matrix and hence a new file...

        // create the file and fill with zeros...
        file = new File(fileName);
        randomAccessFile = new RandomAccessFile(file, "rw");
        for (long i = 0; i < dataVectorLength; i++) {
          randomAccessFile.writeFloat(initialValue);
        }

        // reset to the beginning of the file...
        randomAccessFile.seek(0L);
        lastVectorIndex = 0;

        // create the header/info file...
        infoFileToWrite = new File(path + infoFileSuffix);
        outInfoStream = new FileWriter(infoFileToWrite);
        outoutout = new PrintWriter(outInfoStream);

        outoutout.println(dataFormat);
        outoutout.println(numDimensions);
        for (int dimIndex = 0; dimIndex < numDimensions; dimIndex++) {
          outoutout.println(" " + dimensions[dimIndex]);
        }
        outoutout.println(dataVectorLength);
        outoutout.println(garbageCollectMeFlag);
        outoutout.println(serializedBufferSize);

        // clean up...
        outoutout.flush();
        outoutout.close();
        outInfoStream.close();

        break;

        // case dataInObjectFile:
      case 3:
        // Beware the MAGIC NUMBER!!! 3=dataInObjectFile

        fileName = path;
        readWriteFloatBuffer = new float[BufferSize];
        for (int elementIndex = 0; elementIndex < BufferSize; elementIndex++) {
          readWriteFloatBuffer[elementIndex] = initialValue;
        }

        int numBuffers = (int) ((dataVectorLength - 1) / BufferSize) + 1;

        file = new File(fileName);
        randomAccessFile = new RandomAccessFile(file, "rw");

        // create all the blocks of zeros
        for (int i = 0; i < numBuffers; i++) {
          writeBlock(i);
        }

        // reset ourselves to the top...
        randomAccessFile.seek(0L);
        lastVectorIndex = 0;

        lastBufferIndex = 0;
        lastVectorIndex = 0;
        bufferChangedFlag = false;

        // write out the info file....
        // create the header/info file...
        infoFileToWrite = new File(path + infoFileSuffix);
        outInfoStream = new FileWriter(infoFileToWrite);
        outoutout = new PrintWriter(outInfoStream);

        outoutout.println(dataFormat);
        outoutout.println(numDimensions);
        for (int dimIndex = 0; dimIndex < numDimensions; dimIndex++) {
          outoutout.println(" " + dimensions[dimIndex]);
        }
        outoutout.println(dataVectorLength);
        outoutout.println(BufferSize);
        outoutout.println(garbageCollectMeFlag);
        outoutout.println(serializedBufferSize);

        // clean up...
        outoutout.flush();
        outoutout.close();
        outInfoStream.close();

        break;

      default:
        System.out.println("Error!  Unknown dataFormat.");
        break;
    }
  }

  public void initialize(int dataFormat, long[] dimensions, float initalValue) throws Exception {

    this.dataFormat = dataFormat;
    this.dimensions = dimensions;
    this.numDimensions = dimensions.length;

    // calcluate length of data vector if necessary
    dataVectorLength = dimensions[0];
    for (int dimIndex = 1; dimIndex < this.numDimensions; dimIndex++) {
      dataVectorLength *= dimensions[dimIndex];
    }

    // allocate space
    switch (dataFormat) {

        // case dataInNativeArray:
      case 0:
        // Beware the MAGIC NUMBER!!! 0 = dataInNativeArray
        System.out.println(
            "Initialization with a value is only implemented for on disk representations (2 or 3),"
                + " not 0. Face it, I'm lazy.");
        throw new Exception();

        // case dataInVector:
      case 1:
        // Beware the MAGIC NUMBER!!! 1=dataInVector
        System.out.println(
            "Initialization with a value is only implemented for on disk representations (2 or 3),"
                + " not 1. Face it, I'm lazy");
        throw new Exception();

        // case dataInVectorFile:
      case 2:
        // Beware the MAGIC NUMBER!!! 2=dataInVectorFile
        fileName = this.fileDefaultBase + this.hashCode();
        try {
          file = new File(fileName);
          randomAccessFile = new RandomAccessFile(file, "rw");
          for (long i = 0; i < dataVectorLength; i++) {
            randomAccessFile.writeFloat(initalValue);
          }
        } catch (Exception e) {
          System.out.println("Error!  File (" + fileName + ") could not be initialized.");
        }

        try {
          randomAccessFile.seek(0L);
          lastVectorIndex = 0;
        } catch (Exception e) {
          System.out.println("Error!  randomAccessFile.seek(0L) failed");
        }

        break;

        // case dataInObjectFile:
      case 3:
        // Beware the MAGIC NUMBER!!! 3=dataInObjectFile

        //      fileName = "Matrix" + this.hashCode();
        fileName = this.fileDefaultBase + this.hashCode();

        readWriteFloatBuffer = new float[BufferSize];

        file = new File(fileName);
        randomAccessFile = new RandomAccessFile(file, "rw");

        // this would be unnecessary for the normal MFF which starts out as all zeros...
        for (int i = 0; i < readWriteFloatBuffer.length; i++) {
          readWriteFloatBuffer[i] = initalValue;
        }

        int numBuffers = (int) ((dataVectorLength - 1) / BufferSize) + 1;
        for (int i = 0; i < numBuffers; i++) {
          writeBlock(i);
        }

        try {
          randomAccessFile.seek(0L);
          lastVectorIndex = 0;
        } catch (Exception e) {
          System.out.println("Error!  randomAccessFile.seek(0L) failed");
        }

        lastBufferIndex = 0;
        lastVectorIndex = 0;
        bufferChangedFlag = false;
        break;
      default:
        System.out.println("Error!  Unknown dataFormat.");
        break;
    }
  }

  public void loadPermanent(String path) throws Exception {

    this.permanentStyleFlag = true;

    String lineContents = null;

    File infoFileToRead = new File(path + infoFileSuffix);
    ;
    FileReader inInfoStream = new FileReader(infoFileToRead);
    BufferedReader ininin = new BufferedReader(inInfoStream);

    // read the header/info file
    System.out.println(
        "Starting to load file [" + path + "] as an MFM. Hashcode = " + this.hashCode());
    lineContents = ininin.readLine();
    dataFormat = Integer.parseInt(lineContents);
    System.out.println("   dataFormat = " + dataFormat);
    lineContents = ininin.readLine();
    numDimensions = Integer.parseInt(lineContents);
    System.out.println("   numDimensions = " + numDimensions);
    // cycle through all the dimensions to pull out the goodies
    System.out.print("   dimensions = ");
    dimensions = new long[numDimensions];
    for (int dimIndex = 0; dimIndex < numDimensions; dimIndex++) {
      lineContents = ininin.readLine();
      // the idea is that there is a single space before the dimension lengths.
      dimensions[dimIndex] = Long.parseLong(lineContents.substring(1));
      System.out.print(dimensions[dimIndex] + " by ");
    }
    System.out.print("\n");
    lineContents = ininin.readLine();
    dataVectorLength = Long.parseLong(lineContents);
    System.out.println("   dataVectorLength = " + dataVectorLength);
    if (dataFormat == dataInObjectFile) {
      lineContents = ininin.readLine();
      BufferSize = Integer.parseInt(lineContents);
      System.out.println("   BufferSize = " + BufferSize);
    }
    lineContents = ininin.readLine();
    if (lineContents.equalsIgnoreCase("true")) {
      garbageCollectMeFlag = true;
    } else {
      garbageCollectMeFlag = false;
    }
    System.out.println("   GarbageCollectMe = " + garbageCollectMeFlag);
    lineContents = ininin.readLine();
    serializedBufferSize = Integer.parseInt(lineContents);
    System.out.println("   serializedBufferSize = " + serializedBufferSize);

    ininin.close();
    inInfoStream.close();

    // calcluate length of data vector if necessary
    if (dataFormat == dataInVectorFile || dataFormat == dataInObjectFile) {

      dataVectorLength = dimensions[0];
      for (int dimIndex = 1; dimIndex < numDimensions; dimIndex++) {
        dataVectorLength *= dimensions[dimIndex];
      }
    } else {
      System.err.println(
          "dataFormat is "
              + dataFormat
              + "which is an \"in memory\" format. This constructor "
              + "is for on-disk representations...");
      throw new Exception();
    }

    // set up the files...
    switch (dataFormat) {
        // cases 0 and 1 are gone due to being in-memory representations. the idiot-proofing should
        // take care of this.
        // case dataInNativeArray:
        // case dataInVectorFile:
      case 2:
        // Beware the MAGIC NUMBER!!! 2=dataInVectorFile
        fileName = path;
        // we want a new matrix and hence a new file...

        // create the file and fill with zeros...
        file = new File(fileName);
        randomAccessFile = new RandomAccessFile(file, "rw");
        // place ourselves at the beginning of the file.
        randomAccessFile.seek(0);
        lastVectorIndex = 0;

        break;

        // case dataInObjectFile:
      case 3:
        // Beware the MAGIC NUMBER!!! 3=dataInObjectFile

        fileName = path;
        //      int numBuffers = (int)((dataVectorLength - 1) / BufferSize) + 1;

        file = new File(fileName);
        randomAccessFile = new RandomAccessFile(file, "rw");

        // reset ourselves to the top...
        randomAccessFile.seek(0);

        // create the byte-buffer...

        // trying to pretend like we have done an initialization "writeBlock"
        readWriteFloatBuffer = new float[BufferSize];
        readWriteByteBuffer = new byte[(int) serializedBufferSize];

        // try to use values which will never be used in practice so that
        // the conditionals will then make us load a block and go...
        bufferChangedFlag = false;
        this.lastBufferIndex = -15;
        this.lastVectorIndex = -16;
        readBlock(0, 0);

        break;

      default:
        System.out.println("Error!  Unknown dataFormat.");
        break;
    }
  }

  public void DumpByteArray() {
    System.out.println("stand-in for dump");
    for (int byteIndex = 0; byteIndex < serializedBufferSize; byteIndex++) {
      //      System.out.println("[" + byteIndex + "] = " + ReadWriteByteBuffer[byteIndex]);
    }
  }

  public MultiFormatFloat(int FormatIndex, long[] dimensions) throws Exception {
    initialize(FormatIndex, dimensions);
  }

  public MultiFormatFloat(int FormatIndex, long[] dimensions, float initialValue) throws Exception {
    initialize(FormatIndex, dimensions, initialValue);
  }

  public MultiFormatFloat(int FormatIndex, long[] dimensions, String path) throws Exception {
    initializePermanent(FormatIndex, dimensions, path);
  }

  public MultiFormatFloat(int FormatIndex, long[] dimensions, String path, float initialValue)
      throws Exception {
    initializePermanentWithValue(FormatIndex, dimensions, path, initialValue);
  }

  public MultiFormatFloat(String path) throws Exception {
    loadPermanent(path);
  }

  public int getDataFormat() {
    return this.dataFormat;
  }

  public int getNumDimensions() {
    return numDimensions;
  }

  public long[] getDimensions() {
    return dimensions;
  }

  public long getTotalNumberOfElements() {
    return dataVectorLength;
  }

  public float getValue(long i) throws Exception {
    long[] coordinates1D = {i};
    return getValue(coordinates1D);
  }

  public float getValue(long i, long j) throws Exception {
    long[] coordinates = {i, j};
    return getValue(coordinates);
  }

  public float getValue(long i, long j, long k) throws Exception {
    long[] coordinates = {i, j, k};
    return getValue(coordinates);
  }

  public float getValue(long i, long j, long k, long l) throws Exception {
    long[] coordinates = {i, j, k, l};
    return getValue(coordinates);
  }

  public float getValue(long[] coordinates) throws Exception {

    long vectorIndex = -1;

    if (dataFormat == dataInVector
        || dataFormat == dataInVectorFile
        || dataFormat == dataInObjectFile) {

      switch (numDimensions) {
        case 1:
          vectorIndex = coordinates[0];
          break;
        case 2:
          vectorIndex = (coordinates[0] * dimensions[1]) + coordinates[1];
          break;
        case 3:
          vectorIndex =
              ((coordinates[0] * dimensions[1]) + coordinates[1]) * dimensions[2] + coordinates[2];
          break;
        case 4:
          vectorIndex =
              (((coordinates[0] * dimensions[1]) + coordinates[1]) * dimensions[2] + coordinates[2])
                      * dimensions[3]
                  + coordinates[3];
          break;
        default:
          vectorIndex = coordinates[0];
          for (int dimIndex = 1; dimIndex < numDimensions; dimIndex++) {
            vectorIndex = vectorIndex * dimensions[dimIndex] + coordinates[dimIndex];
          }
          //        System.out.println("Error!  Invalid number of dimensions ("
          //            + numDimensions + ").");
          break;
      }
    }

    switch (dataFormat) {

        // case dataInNativeArray:
      case 0:
        // Beware the MAGIC NUMBER!!! 0 = dataInNativeArray
        switch (numDimensions) {
          case 1:
            return ((float[]) dataNativeArray)[(int) coordinates[0]];
          case 2:
            return ((float[][]) dataNativeArray)[(int) coordinates[0]][(int) coordinates[1]];
          case 3:
            return ((float[][][]) dataNativeArray)
                [(int) coordinates[0]][(int) coordinates[1]][(int) coordinates[2]];
          case 4:
            return ((float[][][][]) dataNativeArray)
                [(int) coordinates[0]][(int) coordinates[1]][(int) coordinates[2]][
                (int) coordinates[3]];
          default:
            System.out.println(
                "Error!  Invalid number of dimensions ("
                    + numDimensions
                    + ") for native arrays (not yet supported this high).");
            return Float.NaN;
        }

        // case dataInVector:
      case 1:
        // Beware the MAGIC NUMBER!!! 1=dataInVector
        return readWriteFloatBuffer[(int) vectorIndex];

        // case dataInVectorFile:
      case 2:
        // Beware the MAGIC NUMBER!!! 2=dataInVectorFile

        {
          synchronized (this) {
            if (vectorIndex != lastVectorIndex + 1) {
              try {
                randomAccessFile.seek(vectorIndex * magicBytesPerElement);
              } catch (Exception e) {
                System.out.println(
                    "Error!  randomAccessFile.seek( (long) index * magicBytesPerElement) failed");
              }
            }

            float value = Float.NaN;
            try {
              value = randomAccessFile.readFloat();
            } catch (Exception e) {
              System.out.println("Error!  randomAccessFile.readFloat() failed");
            }

            lastVectorIndex = vectorIndex;
            return value;
          }
        }

        // case dataInObjectFile:
      case 3:
        // Beware the MAGIC NUMBER!!! 3=dataInObjectFile

        synchronized (this) {
          int bufferIndex = (int) (vectorIndex / BufferSize);

          if (bufferIndex != lastBufferIndex) {

            if (bufferChangedFlag) {
              writeBlock(lastBufferIndex);
            }

            readBlock(bufferIndex, vectorIndex);
          }

          return readWriteFloatBuffer[(int) (vectorIndex % BufferSize)];
        }

      default:
        System.out.println("Error!  Unkown data format (" + dataFormat + ").");
        return Float.NaN;
    }
  }

  public void setValue(long i, float value) throws Exception {
    long[] coordinates1D = {i};
    setValue(coordinates1D, value);
  }

  public void setValue(long i, long j, float value) throws Exception {
    long[] coordinates2D = {i, j};
    setValue(coordinates2D, value);
  }

  public void setValue(long i, long j, long k, float value) throws Exception {
    long[] coordinates3D = {i, j, k};
    setValue(coordinates3D, value);
  }

  public void setValue(long i, long j, long k, long l, float value) throws Exception {
    long[] coordinates4D = {i, j, k, l};
    setValue(coordinates4D, value);
  }

  public void setValue(long[] coordinates, float value) throws Exception {

    long vectorIndex = 0;

    if (dataFormat == dataInVector
        || dataFormat == dataInVectorFile
        || dataFormat == dataInObjectFile) {

      switch (numDimensions) {
        case 1:
          vectorIndex = coordinates[0];
          break;
        case 2:
          vectorIndex = (coordinates[0] * dimensions[1]) + coordinates[1];
          break;
        case 3:
          vectorIndex =
              ((coordinates[0] * dimensions[1]) + coordinates[1]) * dimensions[2] + coordinates[2];
          break;
        case 4:
          vectorIndex =
              (((coordinates[0] * dimensions[1]) + coordinates[1]) * dimensions[2] + coordinates[2])
                      * dimensions[3]
                  + coordinates[3];
          break;
        default:
          vectorIndex = coordinates[0];
          for (int dimIndex = 1; dimIndex < numDimensions; dimIndex++) {
            vectorIndex = vectorIndex * dimensions[dimIndex] + coordinates[dimIndex];
          }
          //        System.out.println("Error!  Invalid number of dimensions ("
          //            + numDimensions + ").");
          break;
      }
    }

    switch (dataFormat) {
        // case dataInNativeArray:
      case 0:
        // Beware the MAGIC NUMBER!!! 0 = dataInNativeArray
        switch (numDimensions) {
          case 1:
            ((float[]) dataNativeArray)[(int) coordinates[0]] = value;
            break;
          case 2:
            ((float[][]) dataNativeArray)[(int) coordinates[0]][(int) coordinates[1]] = value;
            break;
          case 3:
            ((float[][][]) dataNativeArray)
                    [(int) coordinates[0]][(int) coordinates[1]][(int) coordinates[2]] =
                value;
            break;
          case 4:
            ((float[][][][]) dataNativeArray)
                    [(int) coordinates[0]][(int) coordinates[1]][(int) coordinates[2]][
                    (int) coordinates[3]] =
                value;
            break;
          default:
            System.out.println(
                "Error!  Invalid number of dimensions ("
                    + numDimensions
                    + ") not yet supported this high for native arrays.");
            break;
        }
        return;

        // case dataInVector:
      case 1:
        // Beware the MAGIC NUMBER!!! 1=dataInVector
        readWriteFloatBuffer[(int) vectorIndex] = value;
        return;

        // case dataInVectorFile:
      case 2:
        // Beware the MAGIC NUMBER!!! 2=dataInVectorFile

        try {
          randomAccessFile.seek(vectorIndex * magicBytesPerElement);
          randomAccessFile.writeFloat(value);
          return;
        } catch (Exception e) {
          System.out.println("Error!  writing index (" + vectorIndex + ") failed");
        }
        return;

        // case dataInObjectFile:
      case 3:
        // Beware the MAGIC NUMBER!!! 3=dataInObjectFile

        synchronized (this) {
          int bufferIndex = (int) (vectorIndex / BufferSize);

          if (bufferIndex != lastBufferIndex) {

            if (bufferChangedFlag) {
              writeBlock(lastBufferIndex);
            }

            readBlock(bufferIndex, vectorIndex);
          }

          if (readWriteFloatBuffer[(int) (vectorIndex % BufferSize)] != value) {
            readWriteFloatBuffer[(int) (vectorIndex % BufferSize)] = value;
            bufferChangedFlag = true;
          }

          return;
        }

      default:
        System.out.println("Error!  Unkown data format (" + dataFormat + ").");
        return;
    }
  }

  public void finishRecordingMatrix() throws Exception {
    this.finalize();
  }

  protected void finalize() throws Exception {
    // System.out.println("Finalizing Matrix" + this.hashCode());
    if (permanentStyleFlag & lastBufferIndex > -1) {
      writeBlock(lastBufferIndex);
    }

    if (garbageCollectMeFlag) {
      switch (dataFormat) {

          // case dataInNativeArray:
        case 0:
          // Beware the MAGIC NUMBER!!! 0 = dataInNativeArray

          break;

          // case dataInVector:
        case 1:
          // Beware the MAGIC NUMBER!!! 1=dataInVector
          break;

          // case dataInVectorFile:
        case 2:
          // Beware the MAGIC NUMBER!!! 2=dataInVectorFile
          randomAccessFile.close();
          file.delete();
          break;

          // case dataInObjectFile:
        case 3:
          // Beware the MAGIC NUMBER!!! 3=dataInObjectFile

          File file = new File(fileName);
          randomAccessFile.close();
          file.delete();
          break;

        default:
          System.out.println("Error!  Unkown data format (" + dataFormat + ").");
          return;
      }
    }
  }
}
