package org.R2Useful;

import java.io.*;

public class MultiFormatMatrix implements Serializable {

  // Beware the MAGIC ASSUMPTIONS!!!
  
  private static final String infoFileSuffix = new String("_info");
  // final int BufferSize = 32000;
  // final int BufferSize = 3000;
  // int                   BufferSize           = 4096;
  // int                   BufferSize           = 1310720; // 10MB worth of elements
  // int                   BufferSize           = 8192; // MB worth of elements
  // 16384
  private int                   BufferSize           = 16384; // MB worth of elements

  private String                fileDefaultBase = "Matrix";
  
  private ByteArrayOutputStream byteArrayOutputStream;
  private ObjectOutputStream    objectOutputStream;
  private ByteArrayInputStream  byteArrayInputStream;
  private ObjectInputStream     objectInputStream;
  
  private int                   dataFormat;
  
  /*
   * final int dataInNativeArray = 0; final int dataInVector = 1; final int
   * dataInVectorFile = 2; final int dataInObjectFile = 3;
   */
  
  // Beware the MAGIC NUMBERS!!! these are getting hardcoded to avoid memory
  // bottlenecks...
  public static final int             dataInNativeArray    = 0;
  public static final int             dataInVector         = 1;
  public static final int             dataInVectorFile     = 2;
  public static final int             dataInObjectFile     = 3;
  
  private int                   numDimensions;
  private long[]                dimensions;
  private long                  dataVectorLength     = -1;
  private Object                dataNativeArray      = null;
  
  private String                fileName;
  private File                  file;
  private RandomAccessFile      randomAccessFile;
  
  private int                   serializedBufferSize = -1;
  private int                   lastBufferIndex      = -1;
  private long                  lastVectorIndex      = -1;
  private double[]              ReadWriteDoubleBuffer;
  private byte[]                ReadWriteByteBuffer;
  private boolean               BufferChanged;
  
  private boolean               GarbageCollectMe     = true;
  private boolean               PermanentStyleFlag   = false;
  
  public MultiFormatMatrix() {    
  }

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
      out.writeObject(ReadWriteDoubleBuffer);
      out.writeLong(dataVectorLength);
    } else if (dataFormat == dataInObjectFile) {
      out.writeLong(dataVectorLength);
      out.writeInt(BufferSize);
      int numBuffers = (int)((dataVectorLength - 1) / BufferSize) + 1;
      for (int i = 0; i < numBuffers; i++) {
        readBlock(i, 0);
        out.writeObject(ReadWriteDoubleBuffer);
      }
    }
    
    // and the garbage collection flag...
    out.writeBoolean(GarbageCollectMe);
    out.writeBoolean(PermanentStyleFlag);
//    System.out.println("MFM: ending a writeObject serialization...");
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException, Exception {
//    System.out.println("MFM: starting a readObject de-serialization...");

    // read the data format...
    dataFormat = in.readInt();
    // read the dimensions...
    dimensions = (long[])in.readObject();
    numDimensions = in.readInt();

    if (dataFormat == dataInNativeArray) {
      dataNativeArray = in.readObject();
    } else if (dataFormat == dataInVector){
      // it is in vector...
      ReadWriteDoubleBuffer = (double[])in.readObject();
      dataVectorLength = in.readLong();
    } else if (dataFormat == dataInObjectFile) {
      // read in the necessary parameters
      dataVectorLength      =           in.readLong();
      BufferSize            =           in.readInt();

      // set up the necessary variables
      fileName = "Matrix" + this.hashCode();
      file = new File(fileName);
      randomAccessFile = new RandomAccessFile(file, "rw");

      int numBuffers = (int)((dataVectorLength - 1) / BufferSize) + 1;
      ReadWriteDoubleBuffer = new double[BufferSize];

      // copy over the goodies
      for (int i = 0; i < numBuffers; i++) {
        ReadWriteDoubleBuffer = (double[])in.readObject();
        writeBlock(i);
      }
      
      lastBufferIndex = numBuffers - 1;
      BufferChanged = false;
    }
    
    GarbageCollectMe = in.readBoolean();

    PermanentStyleFlag = in.readBoolean();
//    System.out.println("MFM: ending a readObject de-serialization...");
  }

  
  
  public void setGarbageCollectionMode(boolean newValue) {
    GarbageCollectMe = newValue;
  }
  
  public void writeBlock(int index) throws Exception {
    
    byteArrayOutputStream = new ByteArrayOutputStream();
    objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
    
    
    
    objectOutputStream.writeObject(ReadWriteDoubleBuffer);
    ReadWriteByteBuffer = byteArrayOutputStream.toByteArray();
    
    serializedBufferSize = ReadWriteByteBuffer.length;
    
    // System.out.println("writeBlock bufferIndex = " + bufferIndex + "
    // serializedBufferSize = " + serializedBufferSize);
    
    randomAccessFile.seek((long)index * serializedBufferSize);
    randomAccessFile.write(ReadWriteByteBuffer);
    
  }
  
    public void readBlock(int bufferIndex, long vectorIndex) throws Exception {
    
    // System.out.println("In readBlock bufferIndex = " + bufferIndex + "
    // serializedBufferSize = " + serializedBufferSize);
    
    randomAccessFile.seek((long)bufferIndex * serializedBufferSize);
    
    randomAccessFile.readFully(ReadWriteByteBuffer);
    
    byteArrayInputStream = new ByteArrayInputStream(ReadWriteByteBuffer);
    objectInputStream = new ObjectInputStream(byteArrayInputStream);
    
    ReadWriteDoubleBuffer = (double[])objectInputStream.readObject();
    
    lastVectorIndex = vectorIndex;
    lastBufferIndex = bufferIndex;
    BufferChanged = false;
  }
  
  public void initialize(int dataFormat, int[] IntDimensions) throws Exception {
    
    long[] LongDimensions = new long[IntDimensions.length];
    for (int i = 0; i < IntDimensions.length; i++) {
      LongDimensions[i] = IntDimensions[i];
    }
    initialize(dataFormat, LongDimensions);
  }
  
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
        dataNativeArray = new double[(int)dimensions[0]];
        break;
      case 2:
        dataNativeArray = new double[(int)dimensions[0]][(int)dimensions[1]];
        break;
      case 3:
        dataNativeArray = new double[(int)dimensions[0]][(int)dimensions[1]][(int)dimensions[2]];
        break;
      case 4:
        dataNativeArray = new double[(int)dimensions[0]][(int)dimensions[1]][(int)dimensions[2]][(int)dimensions[3]];
        break;
      default:
        System.out.println("Error!  Invalid number of dimensions ("
            + numDimensions + ").");
      break;
      }
      break;
      
      // case dataInVector:
    case 1:
      // Beware the MAGIC NUMBER!!! 1=dataInVector
      if (dataVectorLength > Integer.MAX_VALUE) {
        System.out.println("dataVectorLength > Integer.MAX_VALUE.");
      } else {
        ReadWriteDoubleBuffer = new double[(int)dataVectorLength];
      }
      break;
      
      // case dataInVectorFile:
    case 2:
      // Beware the MAGIC NUMBER!!! 2=dataInVectorFile
//      fileName = "Matrix" + this.hashCode();
      fileName = this.fileDefaultBase + this.hashCode();

      try {
        file = new File(fileName);
        randomAccessFile = new RandomAccessFile(file, "rw");
        for (long i = 0; i < dataVectorLength; i++) {
          randomAccessFile.writeDouble(0.0);
        }
      } catch (Exception e) {
        System.out.println("Error!  File (" + fileName
            + ") could not be initialized.");
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

      ReadWriteDoubleBuffer = new double[BufferSize];
      
      file = new File(fileName);
      randomAccessFile = new RandomAccessFile(file, "rw");
      
      for (int i = 0; i < ReadWriteDoubleBuffer.length; i++) {
        ReadWriteDoubleBuffer[i] = 0.0;
      }
      
      int numBuffers = (int)((dataVectorLength - 1) / BufferSize) + 1;
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
      BufferChanged = false;
      break;
    default:
      System.out.println("Error!  Unknown dataFormat.");
    break;
    }
  }

  public void initializePermanent(int dataFormat, long[] dimensions, String path) throws Exception {

    this.PermanentStyleFlag = true;
    
    // Beware the MAGIC ASSUMPTION!!! we are going to define the info/header file's suffix that will be tacked onto
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
      System.err.println("dataFormat is " + dataFormat + "which is an \"in memory\" format. This constructor " +
          "is for on-disk representations...");
      throw new Exception();
    }
    
    GarbageCollectMe = false;
    
    // allocate space
    switch (dataFormat) {
    // cases 0 and 1 are gone due to being in-memory representations. the idiot-proofing should take care of this.
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
        randomAccessFile.writeDouble(0.0);
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
      outoutout.println(GarbageCollectMe);
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
      ReadWriteDoubleBuffer = new double[BufferSize];
      int numBuffers = (int)((dataVectorLength - 1) / BufferSize) + 1;
      
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
      BufferChanged = false;

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
      outoutout.println(GarbageCollectMe);
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

  public void loadPermanent(String path) throws Exception {

    this.PermanentStyleFlag = true;
    
    String lineContents = null;
    
    File infoFileToRead = new File(path + infoFileSuffix);;
    FileReader inInfoStream = new FileReader(infoFileToRead);
    BufferedReader ininin = new BufferedReader(inInfoStream);
    
    // read the header/info file
    System.out.println("Starting to load file [" + path + "] as an MFM. Hashcode = " + this.hashCode());
    lineContents = ininin.readLine();
    dataFormat = Integer.parseInt(lineContents);
    System.out.println("   dataFormat = " + dataFormat);
    lineContents = ininin.readLine();
    numDimensions = Integer.parseInt(lineContents);
    System.out.println("   numDimensions = " + numDimensions);
    // cycle through all the dimensions to pull out the goodies
    System.out.print(  "   dimensions = ");
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
      GarbageCollectMe = true;
    } else {
      GarbageCollectMe = false;
    }
    System.out.println("   GarbageCollectMe = " + GarbageCollectMe);
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
      System.err.println("dataFormat is " + dataFormat + "which is an \"in memory\" format. This constructor " +
          "is for on-disk representations...");
      throw new Exception();
    }
    
    // set up the files...
    switch (dataFormat) {
    // cases 0 and 1 are gone due to being in-memory representations. the idiot-proofing should take care of this.
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
      randomAccessFile.seek(0L);
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
      randomAccessFile.seek(0L);
      
      // create the byte-buffer...

      // trying to pretend like we have done an initialization "writeBlock"
      ReadWriteDoubleBuffer = new double[BufferSize];
      ReadWriteByteBuffer = new byte[serializedBufferSize];
      
      // try to use values which will never be used in practice so that
      // the conditionals will then make us load a block and go...
      BufferChanged = false;
      this.lastBufferIndex = -15;
      this.lastVectorIndex = -16;
      readBlock(0,0L);

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
  
  public MultiFormatMatrix(int FormatIndex, long[] dimensions) throws Exception {
    initialize(FormatIndex, dimensions);
  }

  public MultiFormatMatrix(int FormatIndex, long[] dimensions, String path) throws Exception {
    initializePermanent(FormatIndex, dimensions, path);
  }

  public MultiFormatMatrix(String path) throws Exception {
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

  
  public double getValue(long i) throws Exception {
//    long[] coordinates = { i };
//    System.out.println("i = " + i);
//    System.out.println("coordinates1D = " + coordinates1D);
    long[] coordinates1D = {i};

//    coordinates1D[0] = i;
//    return getValue(coordinates);
    return getValue(coordinates1D);
    }
  
  public double getValue(long i, long j) throws Exception {
    long[] coordinates = { i, j };
//    coordinates2D[0] = i;
//    coordinates2D[1] = j;
    return getValue(coordinates);
//    return getValue(coordinates2D);
  }
  
  public double getValue(long i, long j, long k) throws Exception {
    long[] coordinates = { i, j, k };
    return getValue(coordinates);
//    coordinates3D[0] = i;
//    coordinates3D[1] = j;
//    coordinates3D[2] = k;
//    return getValue(coordinates3D);
  }
  public double getValue(long i, long j, long k, long l) throws Exception {
    long[] coordinates = { i, j, k, l };
    return getValue(coordinates);
  }

  
  public double getValue(long[] coordinates) throws Exception {
    
    long vectorIndex = -1;
    
    if (dataFormat == dataInVector || dataFormat == dataInVectorFile
        || dataFormat == dataInObjectFile) {
      
      switch (numDimensions) {
      case 1:
        vectorIndex =    coordinates[0];
        break;
      case 2:
        vectorIndex =   (coordinates[0] * dimensions[1]) + coordinates[1];
        break;
      case 3:
        vectorIndex =  ((coordinates[0] * dimensions[1]) + coordinates[1]) * dimensions[2] + coordinates[2];
        break;
      case 4:
        vectorIndex = (((coordinates[0] * dimensions[1]) + coordinates[1]) * dimensions[2] + coordinates[2]) * dimensions[3] + coordinates[3];
        break;
      default:
        System.out.println("Error!  Invalid number of dimensions ("
            + numDimensions + ").");
      break;
      }
      
    }
    
    switch (dataFormat) {
    
    // case dataInNativeArray:
    case 0:
      // Beware the MAGIC NUMBER!!! 0 = dataInNativeArray
      switch (numDimensions) {
      case 1:
        return       ((double[])dataNativeArray)[(int)coordinates[0]];
      case 2:
        return     ((double[][])dataNativeArray)[(int)coordinates[0]][(int)coordinates[1]];
      case 3:
        return   ((double[][][])dataNativeArray)[(int)coordinates[0]][(int)coordinates[1]][(int)coordinates[2]];
      case 4:
        return ((double[][][][])dataNativeArray)[(int)coordinates[0]][(int)coordinates[1]][(int)coordinates[2]][(int)coordinates[3]];
      default:
        System.out.println("Error!  Invalid number of dimensions ("
            + numDimensions + ").");
      return Double.NaN;
      }
      
      // case dataInVector:
    case 1:
      // Beware the MAGIC NUMBER!!! 1=dataInVector
      return ReadWriteDoubleBuffer[(int)vectorIndex];
      
      // case dataInVectorFile:
    case 2:
      // Beware the MAGIC NUMBER!!! 2=dataInVectorFile
      
    {
      synchronized (this) {
        
        if (vectorIndex != lastVectorIndex + 1) {
          try {
            randomAccessFile.seek(vectorIndex * 8);
          } catch (Exception e) {
            System.out
            .println("Error!  randomAccessFile.seek( (long) index * 8) failed");
          }
        }
        
        double value = Double.NaN;
        try {
          value = randomAccessFile.readDouble();
        } catch (Exception e) {
          System.out.println("Error!  randomAccessFile.readDouble() failed");
        }
        
        lastVectorIndex = vectorIndex;
        return value;
      }
      
    }
    
    // case dataInObjectFile:
    case 3:
      // Beware the MAGIC NUMBER!!! 3=dataInObjectFile
      
      synchronized (this) {
        
        int bufferIndex = (int)(vectorIndex / BufferSize);
        
        if (bufferIndex != lastBufferIndex) {
          
          if (BufferChanged) {
            writeBlock(lastBufferIndex);
          }
          
          readBlock(bufferIndex, vectorIndex);
        }
        
        return ReadWriteDoubleBuffer[(int)(vectorIndex % BufferSize)];
        
      }
      
    default:
      System.out.println("Error!  Unkown data format (" + dataFormat + ").");
    return Double.NaN;
    }
  }  
  
  public void setValue(long i, double value) throws Exception {
    long[] coordinates1D = {i};
    setValue(coordinates1D, value);
  }
  
  
  public void setValue(long i, long j, double value) throws Exception {
    long[] coordinates2D = {i,j};
//    coordinates2D[1] = j;
    setValue(coordinates2D, value);
  }
  
  
  public void setValue(long i, long j, long k, double value) throws Exception {
    long[] coordinates3D = {i,j,k};
//    coordinates3D[1] = j;
//    coordinates3D[2] = k;
    setValue(coordinates3D, value);
  }
  
  public void setValue(long i, long j, long k, long l, double value) throws Exception {
    long[] coordinates3D = {i,j,k,l};
    setValue(coordinates3D, value);
  }
  
  public void setValue(long[] coordinates, double value) throws Exception {
    
    long vectorIndex = 0;
    
    if (dataFormat == dataInVector || dataFormat == dataInVectorFile
        || dataFormat == dataInObjectFile) {
      
      switch (numDimensions) {
      case 1:
        vectorIndex = coordinates[0];
        break;
      case 2:
        vectorIndex = (coordinates[0] * dimensions[1]) + coordinates[1];
        break;
      case 3:
        vectorIndex = ((coordinates[0] * dimensions[1]) + coordinates[1])
        * dimensions[2] + coordinates[2];
        break;
      case 4:
        vectorIndex = (((coordinates[0] * dimensions[1]) + coordinates[1])
        * dimensions[2] + coordinates[2]) * dimensions[3] + coordinates[3];
        break;
      default:
        System.out.println("Error!  Invalid number of dimensions ("
            + numDimensions + ").");
      break;
      }
      
    }
    
    switch (dataFormat) {
    // case dataInNativeArray:
    case 0:
      // Beware the MAGIC NUMBER!!! 0 = dataInNativeArray
      switch (numDimensions) {
      case 1:
        ((double[])dataNativeArray)[(int)coordinates[0]] = value;
        break;
      case 2:
        ((double[][])dataNativeArray)[(int)coordinates[0]][(int)coordinates[1]] = value;
        break;
      case 3:
        ((double[][][])dataNativeArray)[(int)coordinates[0]][(int)coordinates[1]][(int)coordinates[2]] = value;
        break;
      case 4:
        ((double[][][][])dataNativeArray)[(int)coordinates[0]][(int)coordinates[1]][(int)coordinates[2]][(int)coordinates[3]] = value;
        break;
      default:
        System.out.println("Error!  Invalid number of dimensions ("
            + numDimensions + ").");
      break;
      }
      return;
      
      // case dataInVector:
    case 1:
      // Beware the MAGIC NUMBER!!! 1=dataInVector
      ReadWriteDoubleBuffer[(int)vectorIndex] = value;
      return;
      
      // case dataInVectorFile:
    case 2:
      // Beware the MAGIC NUMBER!!! 2=dataInVectorFile
      
      try {
        randomAccessFile.seek(vectorIndex * 8);
        randomAccessFile.writeDouble(value);
        return;
      } catch (Exception e) {
        System.out
        .println("Error!  writing index (" + vectorIndex + ") failed");
      }
      return;
      
      // case dataInObjectFile:
    case 3:
      // Beware the MAGIC NUMBER!!! 3=dataInObjectFile
      
      synchronized (this) {
        
        int bufferIndex = (int)(vectorIndex / BufferSize);
        
        if (bufferIndex != lastBufferIndex) {
          
          if (BufferChanged) {
            writeBlock(lastBufferIndex);
          }
          
          readBlock(bufferIndex, vectorIndex);
        }
        
        if (ReadWriteDoubleBuffer[(int)(vectorIndex % BufferSize)] != value) {
          ReadWriteDoubleBuffer[(int)(vectorIndex % BufferSize)] = value;
          BufferChanged = true;
        }
        
        return;
        
      }
      
    default:
      System.out.println("Error!  Unkown data format (" + dataFormat + ").");
    return;
    }
  }
  
  protected void finalize() throws Exception {
    // System.out.println("Finalizing Matrix" + this.hashCode());
    if (PermanentStyleFlag & lastBufferIndex > -1) {
      writeBlock(lastBufferIndex);  
    }
    
    if (GarbageCollectMe) {
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
  public void finishRecordingMatrix() throws Exception {
    this.finalize();
  }
  public Object clone() {
    MultiFormatMatrix tempClone = null;
    try {
      tempClone = new MultiFormatMatrix(this.dataFormat,this.dimensions);

      long[]  currentIndex  = new long[this.numDimensions];
      boolean carryOn       = true;
      double  tempDouble     = -3.0;
      int     nDims         = this.numDimensions;
      long[]  theDims       = this.dimensions;

      while(carryOn) {
        
        tempDouble = this.getValue(currentIndex);
        tempClone.setValue(currentIndex,tempDouble);
        
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
      System.out.println("problems creating MFM clone...");
      e.printStackTrace();
    }
    return tempClone;
  }
}

