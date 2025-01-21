package com.optum.itl;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobRunner {

    public void preRunJob(Request request, String action) {
        System.out.println(Timestamp.from(Instant.now()) +  " " + request.getRequestId() + " Entry to preRunJob - requestId = " + request.getRequestId());
        //Update Request to Started status and set current timestamp
        RequestManager requestManager = new RequestManager();
        requestManager.updateRequest(String.valueOf(request.getRequestId()), action);
        //Send an email to the user and let them know the job has started
        EmailSender emailSender = new EmailSender();
        emailSender.sendEmail(request, action);
        System.out.println(Timestamp.from(Instant.now()) +  " " + request.getRequestId() + " Exit from preRunJob - requestId = " + request.getRequestId());
    }

    public void runJob(Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);
        String requestType = SafeStr.codeqlClean(request.getRequestType());

        System.out.println(Timestamp.from(Instant.now()) +  " " + requestID + " Entry to runJob - requestId = " + requestID + " request userid = " + userID);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println(Timestamp.from(Instant.now()) +  " " + requestID + " Entry to new runJob thread process");
                String jobStatus = "";
                        //Update Request status to Started and set current timestamp and send user an email
                preRunJob(request, "Started");
                try {
                    if (request.getRequestType().equalsIgnoreCase("ddrBaseInstall")
                        || request.getRequestType().equalsIgnoreCase("ddrUpgradeInstall")
                        || request.getRequestType().equalsIgnoreCase("ddrKbInstall")) {
                        JobBuilder jobBuilder = new JobBuilder();
                        jobBuilder.buildDDRInstall(request);
                        ProcessBuilder ddrInstallProcessBuilder = new ProcessBuilder("C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe", "-Command", "d:\\Optum\\ITL\\runtime\\" + userID + "\\" + requestID + "\\DDRInstallRunner.ps1");
                        ddrInstallProcessBuilder.start();
                    } else if (request.getRequestType().equalsIgnoreCase("baseInstall")) {
                        JobBuilder jobBuilder = new JobBuilder();
                        jobBuilder.buildBaseInstall(request);
                        ProcessBuilder baseInstallProcessBuilder = new ProcessBuilder("C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe", "-Command", "d:\\Optum\\ITL\\runtime\\" + userID + "\\" + requestID + "\\BaseInstallRunner.ps1");
                        baseInstallProcessBuilder.start();
                    } else if (request.getRequestType().equalsIgnoreCase("cuInstall")) {
                        JobBuilder jobBuilder = new JobBuilder();
                        jobBuilder.buildCUInstall(request);
                        ProcessBuilder cuInstallProcessBuilder = new ProcessBuilder("C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe", "-Command", "d:\\Optum\\ITL\\runtime\\" + userID + "\\" + requestID + "\\CUInstallRunner.ps1");
                        cuInstallProcessBuilder.start();
                    } else if (request.getRequestType().equalsIgnoreCase("kbLoad")) {
                        JobBuilder jobBuilder = new JobBuilder();
                        jobBuilder.buildKBLoad(request);
                        ProcessBuilder kbLoadProcessBuilder = new ProcessBuilder("C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe", "-Command", "d:\\Optum\\ITL\\runtime\\" + userID + "\\" + requestID + "\\KBLoadRunner.ps1");
                        kbLoadProcessBuilder.start();
                    } else if (request.getRequestType().equalsIgnoreCase("lcdLoad")) {
                        JobBuilder jobBuilder = new JobBuilder();
                        jobBuilder.buildLCDLoad(request);
                        ProcessBuilder lcdLoadProcessBuilder = new ProcessBuilder("C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe", "-Command", "d:\\Optum\\ITL\\runtime\\" + userID + "\\" + requestID + "\\LCDLoadRunner.ps1");
                        lcdLoadProcessBuilder.start();
                    } else {
                        ProcessBuilder processBuilder = new ProcessBuilder("C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe", "-Command", "d:\\Optum\\ITL\\runtime\\" + userID + "\\" + requestID + "\\" + requestType + ".ps1");
                        processBuilder.start();
                        if (request.getRequestType().equalsIgnoreCase("provisionVM")) {
                            try {
                                System.out.println(Timestamp.from(Instant.now()) + " " + requestID + " going to sleep for 4 minutes");
                                Thread.sleep(240000);
                                System.out.println(Timestamp.from(Instant.now()) + " " + requestID + " waking up after 4 minutes");
                                JobBuilder jobBuilder = new JobBuilder();
                                jobBuilder.buildTestVMConnection(request);
                                ProcessBuilder testVMConnectionProcessBuilder = new ProcessBuilder("C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe", "-Command", "d:\\Optum\\ITL\\runtime\\" + userID + "\\" + requestID + "\\" + "vmTestConnectionRunner.ps1");
                                testVMConnectionProcessBuilder.start();
                            } catch (InterruptedException ie) {
                                System.out.println(Timestamp.from(Instant.now()) + "An error occurred while running the Update SQL Server VM Name Runner");
                                ie.printStackTrace();
                            }
                        }
                    }
                    System.out.println(Timestamp.from(Instant.now()) +  " " + requestID + " Process started");
                    if (requestType.equalsIgnoreCase("provisionVM")
                            || requestType.equalsIgnoreCase("ddrBaseInstall")
                            || requestType.equalsIgnoreCase("ddrUpgradeInstall")
                            || requestType.equalsIgnoreCase("ddrKbInstall")
                            || requestType.equalsIgnoreCase("baseInstall")
                            || requestType.equalsIgnoreCase("cuInstall")
                            || requestType.equalsIgnoreCase("kbLoad")
                            || requestType.equalsIgnoreCase("lcdLoad")) {
                        //Read the log file to find either of the following strings - Completed Successfully or Failed
                        JobMonitor jobMonitor = new JobMonitor();
                        jobStatus = jobMonitor.getStatus(request);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception ex) {
                    System.out.println(Timestamp.from(Instant.now()) +  " " + requestID + ex);
                }
                if (jobStatus.contains("Failed")) {
                    //If job status failed, update request to Failed status and set current timestamp and send user an email
                    postRunJob(request, jobStatus);
                } else {
                    //If job status "Completed Successfully", update request to Ended status and set current timestamp and send user an email
                    postRunJob(request, "Ended");
                }
                System.out.println(Timestamp.from(Instant.now()) +  " " + requestID + " Exit from new thread process - Request is complete email sent - " + requestID);
            }
        });
        executorService.shutdown();
        System.out.println(Timestamp.from(Instant.now()) +  " " + requestID + " Exit from runJob" + " - requestId = " + requestID + " request userid = " + userID);
    }

    public void postRunJob(Request request, String action) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        String vmName = SafeStr.codeqlClean(request.getVmName());
        int requestID = SafeStr.isRequestIdValid(request);
        String requestType = SafeStr.codeqlClean(request.getRequestType());

        System.out.println(Timestamp.from(Instant.now()) +  " " + requestID + " Entry to postRunJob - requestId = " + requestID);
        //Update Request to Ended status and set current timestamp
        RequestManager requestManager = new RequestManager();
        requestManager.updateRequest(String.valueOf(requestID), action);
        request.setStatus(action);
        //If request is for a new VM, add to UserProfile current VM total and add a UserVM
        if (requestType.equalsIgnoreCase("provisionVM")) {
            UserManager userManager = new UserManager();
            userManager.addUserVM(userID, vmName);
            userManager.updateUserProfile(userID, requestType);
            //If request is to remove a VM, reduce UserProfile current VM total and delete the UserVM
        } else if (requestType.equalsIgnoreCase("deleteVM")) {
            UserManager userManager = new UserManager();
            userManager.deleteUserVM(userID, vmName);
            userManager.updateUserProfile(userID, requestType);
        } else if (requestType.equalsIgnoreCase("baseInstall")) {
            // The zip file needs to be deleted after it's copied over to the users VM.
            // Because there is a delay in this process, it needs to be done in this postRunJob method.
            JobBuilder jobBuilder = new JobBuilder();
            jobBuilder.deleteBaseInstallZip(request);
        }
        //Send an email to the user and let them know the job has ended
        EmailSender emailSender = new EmailSender();
        emailSender.sendEmail(request, action);
        System.out.println(Timestamp.from(Instant.now()) +  " " + requestID + " Exit from postRunJob - requestId = " + requestID);
    }
}
