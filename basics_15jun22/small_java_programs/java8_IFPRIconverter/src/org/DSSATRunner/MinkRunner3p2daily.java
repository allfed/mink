package org.DSSATRunner;

import org.R2Useful.*;

public class MinkRunner3p2daily {

  public static void main(String commandLineOptions[]) throws Exception {

    TimerUtility bigTimer = new TimerUtility();

    ////////////////////////////////////////////
    // handle the command line arguments...
    ////////////////////////////////////////////

    // System.out.print("command line arguments: \n");
    // for (int i = 0; i < commandLineOptions.length; i++) {
    //  System.out.print(i + " " + commandLineOptions[i]);
    // }

    // System.out.println();
    // System.out.println("running Mink3p2daily.java (from MinkRunner3p2daily.java)");
    // System.out.println();

    String initFileName = commandLineOptions[0];

    bigTimer.tic();

    Mink3p2daily runnerObject = new Mink3p2daily(initFileName);

    runnerObject.doSimulationsOnExistingWeather();

    // System.out.println();
    // System.out.println("done running Mink3p2daily");
    // System.out.println();

    // System.out.println(bigTimer.tocMillis() + " ms for RUNNER...");

    // System.out.println(bigTimer.sinceStartMessage());
  } // main
}
