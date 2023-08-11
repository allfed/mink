package org.R2Useful;

// import ncsa.d2k.core.modules.*;
import java.io.*;
import java.net.*;
import java.util.Date;

public class SimpleSender {

  private int portNumber = -2;
  private String ComputerName = "";
  private long SleepTime = -1;
  private long BadConnectionRetries = 5;
  private boolean weHaveAGoodConnection = false;

  private ObjectOutputStream senderOut = null;

  private long nObjectsSent = 0;
  private boolean verboseStatus = false;
  private String label = null;

  public SimpleSender(
      String ComputerName, int portNumber, long SleepTime, long BadConnectionRetries) {
    this.portNumber = portNumber;
    this.ComputerName = ComputerName;
    this.SleepTime = SleepTime;
    this.BadConnectionRetries = BadConnectionRetries;

    this.nObjectsSent = 0;
    this.verboseStatus = false;
    this.label = null;
  }

  public SimpleSender(
      String ComputerName,
      int portNumber,
      long SleepTime,
      long BadConnectionRetries,
      String label) {
    this.portNumber = portNumber;
    this.ComputerName = ComputerName;
    this.SleepTime = SleepTime;
    this.BadConnectionRetries = BadConnectionRetries;

    this.nObjectsSent = 0;
    if (label == null) {
      this.verboseStatus = false;
    } else {
      this.verboseStatus = true;
      this.label = new String(label);
    }
  }

  public void establishConnection() throws Exception {

    if (weHaveAGoodConnection) {
      System.out.println(
          "  --[" + this.toString() + "] already has a good connection; doing nothing--");
      return;
    }

    Socket outgoingSocket = null;

    for (long badRetry = 0; badRetry < BadConnectionRetries; badRetry++) {
      try {
        outgoingSocket = new Socket(ComputerName, portNumber);
        break;
      } catch (IOException e) {
        System.out.println(
            "  --["
                + this.toString()
                + "] failed to connect to ["
                + ComputerName
                + "] on port ["
                + portNumber
                + "] current retry = "
                + badRetry
                + "--");
        outgoingSocket = null; // just to make sure we null it out
      }
      Thread.sleep(SleepTime);
    }

    weHaveAGoodConnection = (outgoingSocket != null);

    if (weHaveAGoodConnection) {
      senderOut = new ObjectOutputStream(outgoingSocket.getOutputStream());
    } else {
      senderOut = null;
    }
  }

  public void sendObject(Object X) throws Exception {

    // check if we have a connection yet
    if (!weHaveAGoodConnection) {
      System.out.println(" no connection yet, attempting to establish... " + this.toString());
      this.establishConnection();
    }

    // check again
    if (!weHaveAGoodConnection) {
      System.out.println(" no good connection for " + this.toString() + "; bailing...");
      throw new Exception();
    }

    senderOut.writeObject(X);
    this.nObjectsSent++;

    if (verboseStatus) {
      System.out.println(
          "["
              + this
              + "/"
              + label
              + "] sent ["
              + X
              + "] as #"
              + nObjectsSent
              + " at "
              + new Date());
      new Thread().dumpStack();
    }

    senderOut.flush();
    senderOut.reset();
  }

  public void closeConnection() throws Exception {
    if (weHaveAGoodConnection) {
      senderOut.flush();
      senderOut.close();
    }
  }
}
