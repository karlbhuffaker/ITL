package com.optum.itl;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

public class RequestHandler {

    public Request testing(Request request) {
        request.setRequestType("provisionVM");
        request.setUserId("userid");
        request.setUserGroup("devops");
        request.setSubmitTimestamp(RequestManager.getCurrentTimeStamp());
        request.setTemplate("CM54SP2-SQL16-Win16-Template");
        request.setVmName("vmname");
        request.setProductInstall("ClaimsManager_5.4_SP2-CU03_Base_Install.zip");
        request.setDb("SQL");
        request.setProductUpgrade("CM_5.4-SP2_CU03.zip");
        request.setKb("CM_KB_2020_Q3B_5.0-5.4.zip");
        request.setPeLcd("0_B_20200515_5.X.zip");
        //request.setFeLcd("0_A_20200515_5.X.zip");
        return request;
    }

    public Request receiveRequest(HttpServletRequest httpServletRequest) {
        Request request = new Request();
        try {
            boolean testing = false;
            if (testing) {
                request = testing(request);
            } else {
                request.setRequestType(SafeStr.codeqlClean(httpServletRequest.getParameter("requestType")));
                request.setUserId(SafeStr.codeqlClean(String.valueOf(httpServletRequest.getSession().getAttribute("userid"))));
                request.setUserGroup(SafeStr.codeqlClean(String.valueOf(httpServletRequest.getSession().getAttribute("usergroup"))));
                request.setSubmitTimestamp(RequestManager.getCurrentTimeStamp());
                request.setTemplate(SafeStr.codeqlClean(httpServletRequest.getParameter("template")));
                request.setVmName(SafeStr.codeqlClean(httpServletRequest.getParameter("vmName")));
                request.setProductInstall(SafeStr.codeqlClean(httpServletRequest.getParameter("baseInstall")));
                request.setDb(SafeStr.codeqlClean(httpServletRequest.getParameter("db")));
                request.setProductUpgrade(SafeStr.codeqlClean(httpServletRequest.getParameter("cuInstall")));
                request.setKb(SafeStr.codeqlClean(httpServletRequest.getParameter("kb")));
                request.setPeLcd(SafeStr.codeqlClean(httpServletRequest.getParameter("lcd")));
                request.setIlogSystemRules(SafeStr.codeqlClean(httpServletRequest.getParameter("ilog_system_rules")));
            }

            anyOtherActiveJobsForVMName(request);
            unavailableFunctionality(request);
            switch(request.getRequestType().toLowerCase()) {
                case "provisionvm": isProvisionVMRequestValid(request);
                    break;
                case "startvm","restartvm","restartvmguest","stopvm","stopvmguest":
                    isMaintainVMRequestValid(request);
                    break;
                case "deletevm":
                    isDeleteVMRequestValid(request);
                    break;
                case "ddrbaseinstall","ddrupgradeinstall","ddrkbinstall","baseinstall","cuinstall","kbload":
                    verifyUserVMExists(request);
                    break;
                case "lcdload":
                    isLCDLoadRequestValid(request);
                    break;
                default:
                    throw new InvalidParameterException("Failed - invalid request type - " + request.getRequestType());
            }
            request.setStatus("Submitted");
            //System.out.println("Exit to receiveRequest");
            return request;
        } catch (InvalidParameterException ex) {
            request.setStatus(ex.getMessage());
            return request;
        }
    }

    public void anyOtherActiveJobsForVMName(Request request) {
        System.out.println("Entry to anyOtherActiveJobsForVMName");
        RequestManager requestManager = new RequestManager();
        boolean otherActiveRequestsForVM = requestManager.getActiveRequests(request);
        //Verify userid exists in the User_Profile DB table

        if (otherActiveRequestsForVM) {
            throw new InvalidParameterException("Failed - there are other active requests for this VM");
        }
        System.out.println("Exit from anyOtherActiveJobsForVMName - validationMessage");
    }

    public void unavailableFunctionality(Request request) {
        System.out.println("Entry to unavailableFunctionality");
        RequestManager requestManager = new RequestManager();
        boolean unavailableFunctionality = requestManager.wasVMBuiltFrom531OrCentosTemplate(request);
        if (Arrays.asList("ddrbaseinstall","baseinstall","cuinstall","kbload","lcdLoad").contains(request.getRequestType().toLowerCase())
                && unavailableFunctionality) {
            throw new InvalidParameterException("Failed - user selected unavailable functionality for 5.3.1 or Linux");
        }
        System.out.println("Exit from anyOtherActiveJobsForVMName - validationMessage");
    }

    public void isProvisionVMRequestValid(Request request) {
        String vmName = SafeStr.codeqlClean(request.getVmName());

        System.out.println("Entry to isProvisionVMRequestValid");
        UserManager userManager = new UserManager();
        UserProfile userProfile = userManager.getUserProfile(request.getUserId());
        //Verify userid exists in the User_Profile DB table
        if (userProfile == null) {
            throw new InvalidParameterException("Failed - userid does not exist");
        }
        //Set user group in request
        request.setUserGroup(userProfile.getUserGroup());
        //Verify the user entered a VM name
        if (request.getVmName() == null) {
            throw new InvalidParameterException("Failed - user did not enter a VM name");
        }
        //Verify the user selected a template
        if (request.getTemplate() == null) {
            throw new InvalidParameterException("Failed - user did not select a template");
        }
        //Verify the VM Name only contains alphaNumreic
        if (!StringUtils.isAlphanumeric(request.getVmName())) {
            throw new InvalidParameterException("Failed - VM Name must be alphanumeric");
        }
        //Verify the VM Name is not > 15 characters
        if (request.getVmName().length() > 15) {
            throw new InvalidParameterException("Failed - VM Name length is > 15 characters");
        }
        //Verify the entered VM name does not already exist
        UserVm userVm = userManager.getUserVm(vmName);
        if (userVm != null) {
            throw new InvalidParameterException("Failed - another VM already exists with this VM name - " + userVm.getVmName());
        }
        //Verify the userid's current VM total < allowed VM total
        if (userProfile.getCurrentVmTotal() >= userProfile.getAllowedVmTotal()) {
            System.out.println("userProfile.getCurrentVmTotal()=" +  userProfile.getCurrentVmTotal() + ", userProfile.getAllowedVmTotal() = " + userProfile.getAllowedVmTotal());
            throw new InvalidParameterException("Failed - user already has the maximum allowed VMs");
        }
        System.out.println("Exit from isProvisionVMRequestValid - validationMessage");
    }

    public void isMaintainVMRequestValid(Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        String vmName = SafeStr.codeqlClean(request.getVmName());

        System.out.println("Entry to isMaintainVMRequestValid");
        UserManager userManager = new UserManager();
        UserProfile userProfile = userManager.getUserProfile(userID);
        //Verify userid exists in the User_Profile DB table
        if (userProfile == null) {
            throw new InvalidParameterException("Failed - userid does not exist");
        }
        //Set user group in request
        request.setUserGroup(userProfile.getUserGroup());
        //Verify the user selected a VM name
        if (vmName == null) {
            throw new InvalidParameterException("Failed - user did not enter a VM name");
        }
        verifyUserVMExists(request);
    }

    public String isDeleteVMRequestValid(Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());

        System.out.println("Entry to isDeleteVMRequestValid");
        String validationMessage = "";
        UserManager userManager = new UserManager();
        UserProfile userProfile = userManager.getUserProfile(userID);
        //Verify userid exists in the User_Profile DB table
        if (userProfile == null) {
            throw new InvalidParameterException("Failed - userid does not exist");
        }
        //Set user group in request
        request.setUserGroup(userProfile.getUserGroup());
        //Verify VMs exist for userid
        verifyUserVMExists(request);
        return validationMessage;
    }

    public void isLCDLoadRequestValid(Request request) {
        //Verify user selected at least one LCD file
        //Verify VMs exist for userid
        if ((request.getPeLcd() == null || request.getPeLcd().isEmpty())) {
        //if ((request.getPeLcd() == null || request.getPeLcd().isEmpty()) && (request.getFeLcd() == null || request.getFeLcd().isEmpty())) {
            throw new InvalidParameterException("Failed - user did not choose a LCD file to load");
        }
        verifyUserVMExists(request);
    }

    private void verifyUserVMExists(Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        String vmName = SafeStr.codeqlClean(request.getVmName());

        //Verify selected VM exists for userid
        UserManager userManager = new UserManager();
        List <UserVm> userVMs = userManager.getUserVms(userID);
        if (userVMs == null) {
            throw new InvalidParameterException("Failed - selected VM does not exist for userid");
        }
        boolean vmFound = false;
        for (UserVm userVM : userVMs) {
            //System.out.println("userVM.getVmName()=" + userVM.getVmName());
            if (userVM.getVmName().equalsIgnoreCase(vmName)) {
                vmFound = true;
            }
        }
        if (!vmFound) {
            throw new InvalidParameterException("Failed - selected VM does not exist for userid");
        }
        //System.out.println("Exit from verifyUserVMExists - validationMessage" + validationMessage);
    }

    public String prepareFailedResponse(int requestID, String status) {
        String failedResponseMessage = "Your request has failed - Request #" + requestID + ". Please review your request or contact DevOps if needed." + status;
        System.out.println(failedResponseMessage);
        return failedResponseMessage;
    }
}
