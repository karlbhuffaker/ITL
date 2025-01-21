package com.optum.itl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "home")
public class DashboardServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

//        request.setAttribute("userid", request.getParameter("userid"));
//        //request.setAttribute("password", request.getParameter("password"));
//        UserManager userManager = new UserManager();
//        //verify userid is active in USER_PROFILE DB table
//        UserProfile userProfile = userManager.getUserProfile(request.getParameter("userid"));
////        UserProfile userProfile = new UserProfile();
////        userProfile.setUserId("userid");
////        userProfile.setStatus("active");
////        userProfile.setEmailAddress("ICPLAB_DEVOPS_ADMIN@ds.uhc.com");
//
//        System.out.println(request.getParameter("userid"));
//
//        if (userProfile != null
//                && userProfile.getUserId().equalsIgnoreCase(request.getParameter("userid"))
//                && userProfile.getStatus().equalsIgnoreCase("active")) {
//            //forward to Home page and return userid and email address
//            request.setAttribute("emailAddress", userProfile.getEmailAddress());
//            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
//        } else
//            //return to login page
//            request.setAttribute("errorMessage", "Invalid userid/password or userid is not active. Try again.");
//            request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            //request.setAttribute("emailAddress", userProfile.getEmailAddress())
//       System.out.println("userid=" + request.getParameter("userid"));
//       request.setAttribute("userid", request.getParameter("userid"));
//       request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
        //        PrintWriter out = response.getWriter();
//        out.print("userid = " + request.getParameter("userid") + " Password = " +  request.getParameter("password"));
   }
}
