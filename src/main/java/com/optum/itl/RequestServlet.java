package com.optum.itl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;

@WebServlet(name = "request")
public class RequestServlet extends HttpServlet {
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        //Receive and validate new request from a user
        //System.out.println("RequestServlet.doPost Entry = " + httpServletRequest.getParameter("userid"));
        System.out.println(Timestamp.from(Instant.now()) + " - Entry to RequestServlet.doPost - request type - " + httpServletRequest.getParameter("requestType"));
        RequestHandler requestHandler = new RequestHandler();
        Request request = requestHandler.receiveRequest(httpServletRequest);
        //Add new request to the database, even if the request failed validation
        RequestManager requestManager = new RequestManager();
        request.setRequestId(requestManager.addRequest(request));
        if (request.getStatus().contains("Failed")) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + request.getRequestId() + "Exit to RequestServlet.doPost after validation error");
            //Get the list of VMs the user owns
            UserManager userManager = new UserManager();
            ArrayList userVMs = userManager.getUserVms((String)httpServletRequest.getSession().getAttribute("userid"));
            httpServletRequest.setAttribute("userVMs", userVMs);
            //Get the group of options from Config_Property table
            ConfigManager configManager = new ConfigManager();
            ArrayList configPropertyGroup = null;
            if (request.getRequestType() != null && request.getRequestType().equalsIgnoreCase("provisionVM")) {
                configPropertyGroup = configManager.getConfigPropertyGroup("template");
            } else if (request.getRequestType() != null && request.getRequestType().equalsIgnoreCase("ddrBaseInstall")) {
                configPropertyGroup = configManager.getConfigPropertyGroup("ddrkb");
            } else if (request.getRequestType() != null && request.getRequestType().equalsIgnoreCase("ddrUpgradeInstall")) {
                configPropertyGroup = configManager.getConfigPropertyGroup("ddrkb");
            } else if (request.getRequestType() != null && request.getRequestType().equalsIgnoreCase("ddrKbInstall")) {
                configPropertyGroup = configManager.getConfigPropertyGroup("ddrkb");
            } else if (request.getRequestType() != null && request.getRequestType().equalsIgnoreCase("baseInstall")) {
                configPropertyGroup = configManager.getConfigPropertyGroup("base");
            } else if (request.getRequestType() != null && request.getRequestType().equalsIgnoreCase("cuInstall")) {
                configPropertyGroup = configManager.getConfigPropertyGroup("cu");
            } else if (request.getRequestType() != null && request.getRequestType().equalsIgnoreCase("kbLoad")) {
                configPropertyGroup = configManager.getConfigPropertyGroup("kb");
            } else if (request.getRequestType() != null && request.getRequestType().equalsIgnoreCase("lcdLoad")) {
                configPropertyGroup = configManager.getConfigPropertyGroup("lcd");
            }
            if (configPropertyGroup != null && !configPropertyGroup.isEmpty()){
                httpServletRequest.setAttribute("configPropertyGroup", configPropertyGroup);
            }
            ///Send a response message to the user letting them know the request failed
            httpServletRequest.setAttribute("responseMessage", requestHandler.prepareFailedResponse(request.getRequestId(), request.getStatus()));
            if (request.getRequestType().equalsIgnoreCase("provisionVM")) {
                httpServletRequest.getRequestDispatcher("/provisionVMRequest.jsp").forward(httpServletRequest, httpServletResponse);
            } else if (request.getRequestType().equalsIgnoreCase("maintainVM")) {
                httpServletRequest.getRequestDispatcher("/maintainVMRequest.jsp").forward(httpServletRequest, httpServletResponse);
            } else if (request.getRequestType().equalsIgnoreCase("deleteVM")) {
                httpServletRequest.getRequestDispatcher("/deleteVMRequest.jsp").forward(httpServletRequest, httpServletResponse);
            } else if (request.getRequestType().equalsIgnoreCase("ddrBaseInstall")) {
                httpServletRequest.getRequestDispatcher("/ddrBaseInstallRequest.jsp").forward(httpServletRequest, httpServletResponse);
            } else if (request.getRequestType().equalsIgnoreCase("ddrUpgradeInstall")) {
                httpServletRequest.getRequestDispatcher("/ddrUpgradeInstallRequest.jsp").forward(httpServletRequest, httpServletResponse);
            } else if (request.getRequestType().equalsIgnoreCase("ddrKbInstall")) {
                httpServletRequest.getRequestDispatcher("/ddrKbInstallRequest.jsp").forward(httpServletRequest, httpServletResponse);
            } else if (request.getRequestType().equalsIgnoreCase("baseInstall")) {
                httpServletRequest.getRequestDispatcher("/icpBaseInstallRequest.jsp").forward(httpServletRequest, httpServletResponse);
            } else if (request.getRequestType().equalsIgnoreCase("cuInstall")) {
                httpServletRequest.getRequestDispatcher("/icpCuInstallRequest.jsp").forward(httpServletRequest, httpServletResponse);
            } else if (request.getRequestType().equalsIgnoreCase("kbLoad")) {
                httpServletRequest.getRequestDispatcher("/kbLoadRequest.jsp").forward(httpServletRequest, httpServletResponse);
            } else if (request.getRequestType().equalsIgnoreCase("lcdLoad")) {
                httpServletRequest.getRequestDispatcher("/lcdLoadRequest.jsp").forward(httpServletRequest, httpServletResponse);
            }
        } else {
            //Build a job to fulfill request
            JobBuilder jobBuilder = new JobBuilder();
            jobBuilder.buildJob(request);
            //Submit a job to run in it's own thread to fulfill request
            boolean testing = false;
            if (!testing) {
                JobRunner jobRunner = new JobRunner();
                //Run the job
                jobRunner.runJob(request);
            }
            //Redirect the user to the View Request page
            System.out.println(Timestamp.from(Instant.now()) + " - " + request.getRequestId() + "Exit from RequestServlet.doPost after running job");
            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/request?requestType=viewRequests");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Receive request for a single user's requests, or for all user requests
        System.out.println(Timestamp.from(Instant.now()) + " - Entry to RequestServlet.doGet - RequestType - " + request.getParameter("requestType") + " selectedUserid - " + request.getParameter("selectedUserid") + ", selectedAll - " + request.getParameter("selectedAll"));
        String requestType = request.getParameter("requestType");
        //Get the list of VMs the user owns
        UserManager userManager = new UserManager();
        ArrayList userVMs = userManager.getUserVms((String)request.getSession().getAttribute("userid"));
        request.setAttribute("userVMs", userVMs);
        //Get the group of options from Config_Property table
        ConfigManager configManager = new ConfigManager();
        ArrayList configPropertyGroup = null;
        if (requestType != null && requestType.equalsIgnoreCase("provisionVM")) {
            configPropertyGroup = configManager.getConfigPropertyGroup("template");
        } else if (requestType != null && requestType.equalsIgnoreCase("ddrBaseInstall")) {
            configPropertyGroup = configManager.getConfigPropertyGroup("ddrkb");
        } else if (requestType != null && requestType.equalsIgnoreCase("ddrUpgradeInstall")) {
            configPropertyGroup = configManager.getConfigPropertyGroup("ddrkb");
        } else if (requestType != null && requestType.equalsIgnoreCase("ddrKbInstall")) {
            configPropertyGroup = configManager.getConfigPropertyGroup("ddrkb");
        } else if (requestType != null && requestType.equalsIgnoreCase("baseInstall")) {
            configPropertyGroup = configManager.getConfigPropertyGroup("base");
        } else if (requestType != null && requestType.equalsIgnoreCase("cuInstall")) {
            configPropertyGroup = configManager.getConfigPropertyGroup("cu");
        } else if (requestType != null && requestType.equalsIgnoreCase("kbLoad")) {
            configPropertyGroup = configManager.getConfigPropertyGroup("kb");
        } else if (requestType != null && requestType.equalsIgnoreCase("lcdLoad")) {
            configPropertyGroup = configManager.getConfigPropertyGroup("lcd");
        }
        if (configPropertyGroup != null && !configPropertyGroup.isEmpty()){
            request.setAttribute("configPropertyGroup", configPropertyGroup);
        }
        //forward the user to the requested screen
        if (requestType != null && requestType.equalsIgnoreCase("provisionVM")) {
            request.getRequestDispatcher("/provisionVMRequest.jsp").forward(request, response);
        } else if (requestType != null && requestType.equalsIgnoreCase("maintainVM")) {
            request.getRequestDispatcher("/maintainVMRequest.jsp").forward(request, response);
        } else if (requestType != null && requestType.equalsIgnoreCase("deleteVM")) {
            request.getRequestDispatcher("/deleteVMRequest.jsp").forward(request, response);
        } else if (requestType != null && requestType.equalsIgnoreCase("ddrBaseInstall")) {
            request.getRequestDispatcher("/ddrBaseInstallRequest.jsp").forward(request, response);
        } else if (requestType != null && requestType.equalsIgnoreCase("ddrUpgradeInstall")) {
            request.getRequestDispatcher("/ddrUpgradeInstallRequest.jsp").forward(request, response);
        } else if (requestType != null && requestType.equalsIgnoreCase("ddrKbInstall")) {
            request.getRequestDispatcher("/ddrKbInstallRequest.jsp").forward(request, response);
        } else if (requestType != null && requestType.equalsIgnoreCase("baseInstall")) {
            request.getRequestDispatcher("/icpBaseInstallRequest.jsp").forward(request, response);
        } else if (requestType != null && requestType.equalsIgnoreCase("cuInstall")) {
            request.getRequestDispatcher("/icpCuInstallRequest.jsp").forward(request, response);
        } else if (requestType != null && requestType.equalsIgnoreCase("kbLoad")) {
            request.getRequestDispatcher("/kbLoadRequest.jsp").forward(request, response);
        } else if (requestType != null && requestType.equalsIgnoreCase("lcdLoad")) {
            request.getRequestDispatcher("/lcdLoadRequest.jsp").forward(request, response);
        } else if (requestType != null && requestType.equalsIgnoreCase("viewRequests")) {
            RequestManager requestManager = new RequestManager();
            ArrayList requests = requestManager.getRequests((String) request.getSession().getAttribute("userid"), request.getParameter("selectedUserid"), request.getParameter("selectedAll"));
            request.setAttribute("requests", requests);
            request.getRequestDispatcher("/viewRequests.jsp").forward(request, response);
        }
    }
}
