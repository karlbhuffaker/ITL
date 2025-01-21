package com.optum.itl;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class UserHandler {

    public String validateRequest(HttpServletRequest request) {
        System.out.println("Entry to validateRequest");
        String validationMessage = "";
        if (request.getParameter("requestType").equalsIgnoreCase("addUserProfile")) {
            validationMessage = isAddUserProfileRequestValid(request);
        } else if (request.getParameter("requestType").equalsIgnoreCase("maintainUserProfile")) {
            validationMessage = isMaintainUserProfileRequestValid(request);
        } else if (request.getParameter("requestType").equalsIgnoreCase("deleteUserProfile")) {
            validationMessage = isDeleteUserProfileRequestValid(request);
        } else if (request.getParameter("requestType").equalsIgnoreCase("addUserVM")) {
            validationMessage = isAddUserVMRequestValid(request);
        } else if (request.getParameter("requestType").equalsIgnoreCase("deleteUserVM")) {
            validationMessage = isDeleteUserVMRequestValid(request);
        }
        System.out.println("Exit to validateRequest");
        return validationMessage;
    }

    public String isAddUserProfileRequestValid(HttpServletRequest request) {
        System.out.println("Entry to isAddUserProfileRequestValid");
        String validationMessage = "";
        UserManager userManager = new UserManager();
        UserProfile userProfile = userManager.getUserProfile(request.getParameter("userid"));
        //Verify userid does not exists in the User_Profile DB table
        if (userProfile != null) {
            validationMessage = validationMessage.concat(" Failed - userid already exists in the DB");
        }
        //Verify the userid's current VM total <= allowed VM total
        if (Integer.parseInt(request.getParameter("currentVMTotal")) > Integer.parseInt(request.getParameter("allowedVMTotal"))) {
            validationMessage = validationMessage.concat(" Failed - the user current vm total is greater then the allowed vm total");
        }
        System.out.println("Exit from isAddUserVMRequestValid - validationMessage" + validationMessage);
        return validationMessage;
    }

    public String isMaintainUserProfileRequestValid(HttpServletRequest request) {
        System.out.println("Entry to isMaintainUserProfileRequestValid");
        String validationMessage = "";
        UserManager userManager = new UserManager();
        UserProfile userProfile = userManager.getUserProfile(request.getParameter("userid"));
        //Verify userid exists in the User_Profile DB table
        if (userProfile == null) {
            validationMessage = validationMessage.concat(" Failed - userid does not exist in the DB");
        }
        //TODO verify any additional data as needed
        //Verify the userid's current VM total <= allowed VM total
        if (Integer.parseInt(request.getParameter("currentVMTotal")) > Integer.parseInt(request.getParameter("allowedVMTotal"))) {
            validationMessage = validationMessage.concat(" Failed - the user current vm total is greater then the allowed vm total");
        }
        System.out.println("Exit from isMaintainUserProfileRequestValid - validationMessage" + validationMessage);
        return validationMessage;
    }

    public String isDeleteUserProfileRequestValid(HttpServletRequest request) {
        System.out.println("Entry to isDeleteUserProfileRequestValid - request userid = " + request.getParameter("userid"));
        String validationMessage = "";
        UserManager userManager = new UserManager();
        UserProfile userProfile = userManager.getUserProfile(request.getParameter("userid"));
        //Verify userid exists in the User_Profile DB table
        if (userProfile == null) {
            validationMessage = validationMessage.concat(" Failed - userid does not exist in the DB");
        }
        //Verify there are no VMs for this userid in the UserVM DB table
        List <UserVm> userVMs = userManager.getUserVms(request.getParameter("userid"));
        if (userVMs != null && userVMs.size() > 0) {
            validationMessage = validationMessage.concat("Failed - VMs exist for this userid");
        }
        System.out.println("Exit from isMaintainUserProfileRequestValid - validationMessage" + validationMessage);
        return validationMessage;
    }

    public String isAddUserVMRequestValid(HttpServletRequest request) {
        System.out.println("Entry to isAddUserVMRequestValid");
        String validationMessage = "";
        UserManager userManager = new UserManager();
        UserProfile userProfile = userManager.getUserProfile(request.getParameter("userid"));
        //Verify userid exists in the User_Profile DB table
        if (userProfile == null) {
            validationMessage = validationMessage.concat(" Failed - userid does not exist");
        } else {
            //Verify the userid's current VM total < allowed VM total
            if (userProfile.getCurrentVmTotal() >= userProfile.getAllowedVmTotal()) {
                System.out.println("userProfile.getCurrentVmTotal()=" + userProfile.getCurrentVmTotal() + ", userProfile.getAllowedVmTotal() = " + userProfile.getAllowedVmTotal());
                validationMessage = validationMessage.concat(" Failed - user already has the maximum allowed VMs");
            }
        }
        //Verify the entered VM name does not already exist
        UserVm userVm = userManager.getUserVm(request.getParameter("vmName"));
        if (userVm != null) {
            validationMessage = validationMessage.concat(" Failed - another VM already exists with this VM name - " + userVm.getVmName());
        }
        System.out.println("Exit from isAddUserVMRequestValid - validationMessage" + validationMessage);
        return validationMessage;
    }

    public String isDeleteUserVMRequestValid(HttpServletRequest request) {
        System.out.println("Entry to isDeleteVMRequestValid - request userid = " + request.getParameter("userid") + " uservm = " +  request.getParameter("vmName"));
        String validationMessage = "";
        UserManager userManager = new UserManager();
        UserProfile userProfile = userManager.getUserProfile(request.getParameter("userid"));
        //Verify userid exists in the User_Profile DB table
        if (userProfile == null) {
            validationMessage = validationMessage.concat("Failed - userid does not exist");
        }
        //Verify VMs exist for userid
        validationMessage = validationMessage.concat(" " + verifyUserVMExists(request.getParameter("userid"), request.getParameter("vmName")));
        System.out.println("Exit from isDeleteVMRequestValid - validationMessage" + validationMessage);
        return validationMessage;
    }

    private String verifyUserVMExists(String userid, String vmName) {
        //Verify selected VM exists for userid
        String validationMessage = "";
        UserManager userManager = new UserManager();
        List <UserVm> userVMs = userManager.getUserVms(userid);
        if (userVMs == null) {
            validationMessage = validationMessage.concat("Failed - selected VM does not exist for userid");
        }
        boolean vmFound = false;
        for (UserVm userVM : userVMs) {
            System.out.println("userVM.getVmName()=" + userVM.getVmName());
            if (userVM.getVmName().equalsIgnoreCase(vmName)) {
                vmFound = true;
            }
        }
        if (!vmFound) {
            validationMessage = validationMessage.concat(" Failed - selected VM does not exist for userid");
        }
        System.out.println("Exit from verifyUserVMExists - validationMessage" + validationMessage);
        return validationMessage;
    }

    public String prepareFailedResponse(int requestID, String status) {
        String failedResponseMessage = "Your request has failed - Request #" + requestID + ". Please review your request or contact DevOps if needed." + status;
        System.out.println(failedResponseMessage);
        return failedResponseMessage;
    }
}
