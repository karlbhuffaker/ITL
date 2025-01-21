package com.optum.itl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "login")
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserManager userManager = new UserManager();
        //verify userid exists and is active in USER_PROFILE DB table
        System.out.println("LoginServlet - doPost - userid = " + request.getParameter("userid"));
        UserProfile userProfile = userManager.getUserProfile(request.getParameter("userid"), request.getParameter("password"));
        if (userProfile == null) {
            //return to login page
            request.setAttribute("errorMessage", "Invalid userid/password or userid is not active. Try again.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        } else {
            if (userProfile.getUserId().equalsIgnoreCase(request.getParameter("userid"))
                    && userProfile.getStatus().equalsIgnoreCase("active")) {
                //Store userid in HTTPSession
                request.getSession().setAttribute("userid", userProfile.getUserId());
                request.getSession().setAttribute("username", userProfile.getUserName());
                request.getSession().setAttribute("usergroup", userProfile.getUserGroup());
                //forward to Home page
                request.getServletContext().getRequestDispatcher("/dashboard.jsp").forward(request, response);
            } else {
                //return to login page
                request.setAttribute("errorMessage", "Invalid userid/password or userid is not active. Try again.");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
        }
    }
}
