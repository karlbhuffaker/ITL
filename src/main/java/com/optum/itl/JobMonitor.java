package com.optum.itl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Instant;

public class JobMonitor {

    public String getStatus(Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);
        String requestType = SafeStr.codeqlClean(request.getRequestType());

        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + "Entry JobMonitor.getStatus - requestType - " + requestType);
        String status = "";
        boolean foundMessage = false;
        boolean exceededWaitTime = false;
        int totalWaitTime = 0;
        File logFile = null;
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID  + " before while loop");
        System.out.println(Timestamp.from(Instant.now()) + " " + requestID + " going to sleep for 15 seconds");
        try {
            Thread.sleep(15000);
        } catch (InterruptedException ie) {
            System.out.println(Timestamp.from(Instant.now()) + "An error occurred while waiting - totalWaitTime = " + totalWaitTime);
            ie.printStackTrace();
        }
        System.out.println(Timestamp.from(Instant.now()) + " " + requestID + " waking up after 15 second sleep");
        //Read log file for Successful or Failed message or retry up to 1 hour of wait time
        while (!foundMessage || !exceededWaitTime) {
            try {
                if (requestType.equalsIgnoreCase("provisionVM")
                        || requestType.equalsIgnoreCase("ddrBaseInstall")
                        || requestType.equalsIgnoreCase("ddrUpgradeInstall")
                        || requestType.equalsIgnoreCase("ddrKbInstall")
                        || requestType.equalsIgnoreCase("baseInstall")
                        || requestType.equalsIgnoreCase("cuInstall")
                        || requestType.equalsIgnoreCase("kbLoad")
                        || requestType.equalsIgnoreCase("lcdLoad")) {
                    //Read log file
                    // Check if the path is safe.
                    String filename = requestType + ".log";
                    Path publicFolder = Paths.get("d:/Optum/ITL/runtime/" + userID + "/" + requestID + "/").normalize().toAbsolutePath();
                    Path filePath = publicFolder.resolve(filename).normalize().toAbsolutePath();
                    if (!SafeStr.getValidPathName(publicFolder, filePath)) {
                        throw new IllegalArgumentException("Invalid filename");
                    } else {
                        logFile = new File("d:/Optum/ITL/runtime/" + userID + "/" + requestID + "/" + requestType + ".log");
                        System.out.println(Timestamp.from(Instant.now()) + " " + requestID + " log file name = " + logFile);
                    }

                    if (SafeStr.codeqlClean(String.valueOf(logFile)) != null) {
                        BufferedReader logFileBufferedReader = new BufferedReader(new FileReader(logFile));

                        boolean firstTime = false;
                        String successMessage = "Completed Successfully";
                        String failedMessage = "Failed";
                        String logLine;
                        while ((logLine = logFileBufferedReader.readLine()) != null) {
                            if (!firstTime) {
                                System.out.println(Timestamp.from(Instant.now()) + " " + requestID + " logLine - " + logLine);
                                firstTime = true;
                            }
                            //if Completed Successfully or Failed message found exit loop, else sleep for 30 seconds, read file again and look for either message again
                            if (logLine.contains(successMessage)) {
                                System.out.println(Timestamp.from(Instant.now()) + " " + requestID + " found Completed Successfully message - exit");
                                foundMessage = true;
                                status = status.concat("Completed Successfully");
                                break;
                            } else if (logLine.contains(failedMessage)) {
                                System.out.println(Timestamp.from(Instant.now()) + " " + requestID + " found Failed message - exit");
                                foundMessage = true;
                                status = status.concat("Failed - " + logLine);
                                break;
                            }
                        }
                        logFileBufferedReader.close();
                        //wait 30 seconds and try to read the file again
                        if (foundMessage) {
                            break;
                        }
                        try {
                            System.out.println(Timestamp.from(Instant.now()) + " " + requestID + " going to sleep for 30 seconds");
                            Thread.sleep(30000);
                            System.out.println(Timestamp.from(Instant.now()) + " " + requestID + " waking up after 30 second sleep");
                            totalWaitTime = totalWaitTime + 30;
                            System.out.println(Timestamp.from(Instant.now()) + " " + requestID + " totalWaitTime = " + totalWaitTime);
                            //if total wait time exceeds 1 hour, exit with error
                            if (totalWaitTime >= 3600) {
                                exceededWaitTime = true;
                                status = status.concat("Failed - Exceeded wait time");
                                if (exceededWaitTime) {
                                    break;
                                }
                            }
                        } catch (InterruptedException ie) {
                            System.out.println(Timestamp.from(Instant.now()) + " " + requestID + "An error occurred while waiting - totalWaitTime = " + totalWaitTime);
                            ie.printStackTrace();
                            exceededWaitTime = true;
                        }
                    } else {
                        throw new IOException("File is not safe");
                    }
                }
            } catch(IOException e){
                System.out.println(Timestamp.from(Instant.now()) + " " + requestID + "An error occurred while reading log file - ");
                e.printStackTrace();
                status = status.concat("Failed - An error occurred while reading log file");
                exceededWaitTime = true;
                break;
            }
        }
        return status;
    }
}
