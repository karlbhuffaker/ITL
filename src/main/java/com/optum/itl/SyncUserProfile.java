package com.optum.itl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class SyncUserProfile {

    public static void main(String[] args) {
        String action = "";
        for (String s: args) {
            action = args[0];
        }
        SyncUserProfile syncUserProfile = new SyncUserProfile();
        if (action.equalsIgnoreCase("UserProfile")) {
            syncUserProfile.synchronizeUserProfile("devops");
            syncUserProfile.synchronizeUserProfile("Development");
            syncUserProfile.synchronizeUserProfile("QA_Support");
        } else if (action.equalsIgnoreCase("UserVM")) {
            syncUserProfile.addUserVMs();
        }
    }

    public void synchronizeUserProfile(String userGroup) {
        File userProfiles = new File("d:/Optum/ITL/runtime/files/" + userGroup + "UserProfiles.csv");
        try {
            CSVReader csvReader = new CSVReader(new FileReader(userProfiles));
            String[] nextLine;
            //Read each Active Directory userid record
            while ((nextLine = csvReader.readNext()) != null) {
                if (!nextLine[0].contains("saMAccountName")) {
                    //get userProfile for AD userid
                    UserManager userManager = new UserManager();
                    UserProfile userProfile = userManager.getUserProfile(nextLine[0]);
                    //If UserProfile is not null, see if there are any differences.
                    if (userProfile != null) {
                        //Compare active directory user name, user group and email address data with UserProfile data.
                        if (!nextLine[1].equalsIgnoreCase(userProfile.getUserName())
                                || (!userGroup.equalsIgnoreCase(userProfile.getUserGroup()))
                                || (!nextLine[2].equalsIgnoreCase(userProfile.getEmailAddress()))) {
                            //Update UserProfile
                            userManager.updateUserProfile(nextLine[0], null, nextLine[1], userGroup, nextLine[2]);
                        }
                    } else {
                        //We have a new userid
                        UserProfile newUserProfile = new UserProfile();
                        newUserProfile.setUserId(nextLine[0]);
                        newUserProfile.setUserName(nextLine[0]);
                        newUserProfile.setUserGroup(userGroup);
                        newUserProfile.setEmailAddress(nextLine[2]);
                        userManager.addUserProfile(newUserProfile);
                    }
                }
            }
        } catch (FileNotFoundException fnfException) {
            System.out.println("FNF Exception");
            fnfException.printStackTrace();
        } catch (CsvValidationException csvException) {
            System.out.println("CSV Exception");
            csvException.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
    }


    public void addUserVMs() {
        File userProfiles = new File("d:/Optum/ITL/runtime/files/UserVMs.csv");
        try {
            CSVReader csvReader = new CSVReader(new FileReader(userProfiles));
            String[] nextLine;
            //Read each userVM export record
            while ((nextLine = csvReader.readNext()) != null) {
                if (!nextLine[0].contains("userId")) {
                    //Add userVM
                    UserManager userManager = new UserManager();
                    userManager.addUserVM(nextLine[0], nextLine[1]);
                }
            }
        } catch (FileNotFoundException fnfException) {
            System.out.println("FNF Exception");
            fnfException.printStackTrace();
        } catch (CsvValidationException csvException) {
            System.out.println("CSV Exception");
            csvException.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
    }
}