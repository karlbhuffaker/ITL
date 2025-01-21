package com.optum.itl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "user")
public class UserServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Receive and validate request
        System.out.println("Entry to UserServlet.doPost - request type = " + request.getParameter("requestType") + " request userid - " + request.getParameter("userid") + "session userid = " + request.getSession().getAttribute("userid"));
        String validationMessage = "";
        if (request.getParameter("requestType").equalsIgnoreCase("addUserProfile")) {
            //validate userProfile data
            UserHandler userHandler = new UserHandler();
            validationMessage = userHandler.validateRequest(request);
            if (validationMessage.contains("Failed")) {
                System.out.println("Exit to UserServlet.doPost after validation error - validation message - " + validationMessage);
                ///Send a response message to the user letting them know the request failed
                request.setAttribute("responseMessage", validationMessage);
                request.getRequestDispatcher("/addUserProfile.jsp").forward(request, response);
            } else {
                //add userProfile to the DB
                UserManager userManager = new UserManager();
                UserProfile userProfile = new UserProfile();
                userProfile.setUserId(request.getParameter("userid"));
                userProfile.setUserName(request.getParameter("userName"));
                userProfile.setUserGroup(request.getParameter("userGroup"));
                userProfile.setStatus(request.getParameter("status"));
                userProfile.setEmailAddress(request.getParameter("emailAddress"));
                userProfile.setAllowedVmTotal(Integer.parseInt(request.getParameter("allowedVMTotal")));
                userProfile.setCurrentVmTotal(Integer.parseInt(request.getParameter("currentVMTotal")));
                userProfile.setCreateTimestamp(userManager.getCurrentTimeStamp());
                userManager.addUserProfile(userProfile);
                response.sendRedirect(request.getContextPath() + "/user?requestType=viewUserProfiles");
            }
        } else if (request.getParameter("requestType").equalsIgnoreCase("maintainUserProfile")) {
            //validate userProfile data
            UserHandler userHandler = new UserHandler();
            validationMessage = userHandler.validateRequest(request);
            if (validationMessage.contains("Failed")) {
                System.out.println("Exit to UserServlet.doPost after validation error - validation message - " + validationMessage);
                ///Send a response message to the user letting them know the request failed
                request.setAttribute("responseMessage", validationMessage);
                request.getRequestDispatcher("/maintainUserProfile.jsp").forward(request, response);
            } else {
                //update userProfile in the DB
                UserManager userManager = new UserManager();
                UserProfile userProfile = new UserProfile();
                userProfile.setUserId(request.getParameter("userid"));
                userProfile.setUserName(request.getParameter("userName"));
                userProfile.setUserGroup(request.getParameter("userGroup"));
                userProfile.setStatus(request.getParameter("status"));
                userProfile.setEmailAddress(request.getParameter("emailAddress"));
                userProfile.setAllowedVmTotal(Integer. parseInt(request.getParameter("allowedVMTotal")));
                userProfile.setCurrentVmTotal(Integer. parseInt(request.getParameter("currentVMTotal")));
                userProfile.setUpdateTimestamp(userManager.getCurrentTimeStamp());
                userProfile.setUpdateUserId((String)request.getSession().getAttribute("userid"));
                userManager.updateUserProfile(userProfile);
                response.sendRedirect(request.getContextPath() + "/user?requestType=viewUserProfiles");
            }
        } else if (request.getParameter("requestType").equalsIgnoreCase("deleteUserProfile")) {
            //validate userProfile can be deleted
            UserHandler userHandler = new UserHandler();
            validationMessage = userHandler.validateRequest(request);
            if (validationMessage.contains("Failed")) {
                System.out.println("Exit to UserServlet.doPost after validation error - validationMessage = " + validationMessage);
                ///Send a response message to the user letting them know the request failed
                request.setAttribute("responseMessage", validationMessage);
                request.getRequestDispatcher("/deleteUserProfile.jsp").forward(request, response);
            } else {
                //delete userProfile from the DB
                UserManager userManager = new UserManager();
                userManager.deleteUserProfile(request.getParameter("userid"));
                response.sendRedirect(request.getContextPath() + "/user?requestType=viewUserProfiles");
            }
        } else if (request.getParameter("requestType").equalsIgnoreCase("addUserVM")) {
            //validate userVM data
            UserHandler userHandler = new UserHandler();
            validationMessage = userHandler.validateRequest(request);
            if (validationMessage.contains("Failed")) {
                System.out.println("Exit to UserServlet.doPost after validation error - validation message - " + validationMessage);
                ///Send a response message to the user letting them know the request failed
                request.setAttribute("responseMessage", validationMessage);
                request.getRequestDispatcher("/addUserVM.jsp").forward(request, response);
            } else {
                //add userVM to the DB and increment the currentVMTotal for the userid
                UserManager userManager = new UserManager();
                userManager.addUserVM(request.getParameter("userid"), request.getParameter("vmName"));
                userManager.updateUserProfile((String)request.getParameter("userid"), "provisionVM");
                response.sendRedirect(request.getContextPath() + "/user?requestType=viewUserVMs");
            }
        } else if (request.getParameter("requestType").equalsIgnoreCase("deleteUserVM")) {
            //validate userVM data
            UserHandler userHandler = new UserHandler();
            validationMessage = userHandler.validateRequest(request);
            if (validationMessage.contains("Failed")) {
                System.out.println("Exit to UserServlet.doPost after validation error - validation message - " + validationMessage);
                ///Send a response message to the user letting them know the request failed
                request.setAttribute("responseMessage", validationMessage);
                request.getRequestDispatcher("/deleteUserVM.jsp").forward(request, response);
            } else {
                //delete userVM from the DB and decrement the currentVMTotal for the userid
                UserManager userManager = new UserManager();
                userManager.deleteUserVM(request.getParameter("userid"), request.getParameter("vmName"));
                userManager.updateUserProfile((String)request.getParameter("userid"), "deleteVM");
                response.sendRedirect(request.getContextPath() + "/user?requestType=viewUserVMs");
            }
        } else {
            validationMessage = validationMessage.concat(" Failed - invalid request type - " + request.getParameter("requestType"));
            if (validationMessage.contains("Failed")) {
                System.out.println("Exit to UserServlet.doPost after validation error - validation message - " + validationMessage);
                ///Send a response message to the user letting them know the request failed
                request.setAttribute("responseMessage", validationMessage);
                request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Request for user profile pages and/or data
        System.out.println("Entry to UserServlet.doGet - requestType = " + request.getParameter("requestType") + " userid = " + request.getParameter("userid") + " selectedUserid - " + request.getParameter("selectedUserid") + ", selectedAll - " + request.getParameter("selectedAll"));
        String requestType = request.getParameter("requestType");
        UserManager userManager = new UserManager();
        if (requestType != null && requestType.equalsIgnoreCase("viewUserProfile")) {
            UserProfile userProfile = userManager.getUserProfile(request.getParameter("userid"));
            ArrayList userVMs = userManager.getUserVms(request.getParameter("userid"));
            request.setAttribute("userProfile", userProfile);
            request.setAttribute("userVMs", userVMs);
            System.out.println("Before forward to viewUserProfile");
            request.getRequestDispatcher("/viewUserProfile.jsp").forward(request, response);
        } else if (requestType != null && requestType.equalsIgnoreCase("viewUserProfiles")) {
            ArrayList userProfiles = null;
            if (request.getParameter("selectedAll") != null) {
                userProfiles = userManager.getUserProfiles(null);
            } else if (request.getParameter("selectedUserid") != null) {
                userProfiles = userManager.getUserProfiles(request.getParameter("selectedUserid"));
            } else {
                userProfiles = userManager.getUserProfiles(null);
            }
            request.setAttribute("userProfiles", userProfiles);
            request.getRequestDispatcher("/viewUserProfiles.jsp").forward(request, response);
        } else if (requestType != null && requestType.equalsIgnoreCase("addUserProfile")) {
            request.getRequestDispatcher("/addUserProfile.jsp").forward(request, response);
        } else if (requestType != null && requestType.equalsIgnoreCase("maintainUserProfile")) {
            UserProfile userProfile = userManager.getUserProfile(request.getParameter("userid"));
            request.setAttribute("userProfile", userProfile);
            System.out.println("Before forward to maintainUserProfile");
            request.getRequestDispatcher("/maintainUserProfile.jsp").forward(request, response);
        } else if (requestType != null && requestType.equalsIgnoreCase("deleteUserProfile")) {
            UserProfile userProfile = userManager.getUserProfile(request.getParameter("userid"));
            request.setAttribute("userProfile", userProfile);
            System.out.println("Before forward to deleteUserProfile");
            request.getRequestDispatcher("/deleteUserProfile.jsp").forward(request, response);
        } else if (requestType != null && requestType.equalsIgnoreCase("viewUserVMs")) {
            ArrayList userVMs = null;
            if (request.getParameter("selectedAll") != null) {
                userVMs = userManager.getUserVMs();
            } else if (request.getParameter("selectedUserid") != null) {
                userVMs = userManager.getUserVms(request.getParameter("selectedUserid"));
            } else {
                userVMs = userManager.getUserVMs();
            }
            request.setAttribute("userVMs", userVMs);
            request.getRequestDispatcher("/viewUserVMs.jsp").forward(request, response);
        } else if (requestType != null && requestType.equalsIgnoreCase("addUserVM")) {
            request.getRequestDispatcher("/addUserVM.jsp").forward(request, response);
        } else if (requestType != null && requestType.equalsIgnoreCase("deleteUserVM")) {
            UserVm userVm = userManager.getUserVm(request.getParameter("vmName"));
            request.setAttribute("userVm", userVm);
            System.out.println("Before forward to deleteUserVM");
            request.getRequestDispatcher("/deleteUserVM.jsp").forward(request, response);
        }
    }
}
