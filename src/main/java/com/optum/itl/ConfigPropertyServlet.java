package com.optum.itl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
//import javax.swing.*;
import javax.swing.JOptionPane;

@WebServlet(name = "configProperty")
public class ConfigPropertyServlet extends HttpServlet {
    private static final String KB_PROPERTYNAME_REGEX = "^KB_\\d{4}_[A-Z0-9]+_\\d+\\.\\d+-\\d+\\.\\d+"; // Example: KB_2024_Q1A_5.0-6.0
    private static final Pattern kb_propertyName_pattern = Pattern.compile(KB_PROPERTYNAME_REGEX);

    private static final String KB_PROPERTYVALUE_REGEX = "^KB_\\d{4}_\\w{3}$"; // Example: KB_2025_Q1B
    private static final Pattern kb_propertyValue_pattern = Pattern.compile(KB_PROPERTYVALUE_REGEX);

    private static final String LCD_PROPERTYNAME_REGEX = "^\\d{8}";  // Example: 20240816
    private static final Pattern lcd_propertyName_pattern = Pattern.compile(LCD_PROPERTYNAME_REGEX);

    private static final String LCD_PROPERTYVALUE_REGEX = "^\\d{8}";  // Example: 20240816
    private static final Pattern lcd_propertyValue_pattern = Pattern.compile(LCD_PROPERTYVALUE_REGEX);

    public static void infoBox(String infoMessage, String titleBar)
    {
//        final JDialog dialog = new JDialog();
//        dialog.setAlwaysOnTop(true);
//        JOptionPane.showMessageDialog(dialog, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);

        MessageBox.infoBox(infoMessage, titleBar);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Receive and validate request
        System.out.println("Entry to UserServlet.doPost - request type = " + request.getParameter("requestType") + " request userid - " + request.getParameter("userid") + "session userid = " + request.getSession().getAttribute("userid"));
        String validationMessage = "";
        if (request.getParameter("requestType").equalsIgnoreCase("updateConfigProperty")) {
            //validate userProfile data
            UserHandler userHandler = new UserHandler();
            validationMessage = userHandler.validateRequest(request);
            if (validationMessage.contains("Failed")) {
                System.out.println("Exit to UserServlet.doPost after validation error - validation message - " + validationMessage);
                ///Send a response message to the user letting them know the request failed
                request.setAttribute("responseMessage", validationMessage);
                request.getRequestDispatcher("/updateConfigProperty.jsp").forward(request, response);
            } else {
                //Update the Config_Property DB with updated KB, LCD, CU, Base, and Template values.
                ConfigProperty configProperty = new ConfigProperty();
                ConfigManager configManager = new ConfigManager();

                String product = request.getParameter("product");
                String version = request.getParameter("version");
                String propertyGroup = request.getParameter("propertyGroup");
                String propertyName = "<option value=\"" + request.getParameter("propertyName") + "\">";
                String propertyValue = request.getParameter("propertyValue");

                configProperty.setPropertyName(request.getParameter("productVersion"));
                configProperty.setPropertyGroup(request.getParameter("propertyGroup"));
                configProperty.setPropertyName("<option value=\"" + request.getParameter("propertyName") + "\">");
                configProperty.setPropertyValue(request.getParameter("propertyValue"));

                // Regex code
                if (propertyGroup.contains("kb") || propertyGroup.contains("ddrkb")) {
                    // Check the Property Name
                    String propertyname = request.getParameter("propertyName");
                    Matcher name_matcher = kb_propertyName_pattern.matcher(propertyname);
                    boolean propertyName_isValid = name_matcher.matches();

                    // Check the Property Value
                    String propertyvalue = request.getParameter("propertyValue");
                    Matcher value_matcher = kb_propertyValue_pattern.matcher(propertyvalue);
                    boolean propertyValue_isValid = value_matcher.matches();

                    if (propertyName_isValid && propertyValue_isValid) {
//                        JOptionPane.showMessageDialog(dialog, "The variable Property Name is correct!", "Error", JOptionPane.ERROR_MESSAGE);
                        configManager.updateConfigPropertyGroups(configProperty, version);
                        request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
                    } else {
                        if (!propertyName_isValid) {
//                            infoBox("The Property Name is incorrect!  It must be in a format simular to KB_2024_Q1A_5.0-6.00", "KB Property Name");
//                            JOptionPane.showMessageDialog(null, "The Property Name is incorrect!  It must be in a format simular to KB_2024_Q1A_5.0-6.00", "Error", JOptionPane.INFORMATION_MESSAGE);
                        }
                        if (!propertyValue_isValid) {
//                            JOptionPane.showMessageDialog(null, "The Property Value is incorrect!  It must be in a format simular to KB_2025_Q1B", "Error", JOptionPane.INFORMATION_MESSAGE);
//                            infoBox("The Property Value is incorrect!  It must be in a format simular to KB_2025_Q1B", "KB Property Value");
                        }
                        request.setAttribute("responseMessage", validationMessage);
                        request.getRequestDispatcher("/updateConfigProperty.jsp").forward(request, response);
                    }
                } else if (propertyGroup.contains("lcd")) {
                    // Check the Property Name
                    String propertyname = request.getParameter("propertyName");
                    Matcher name_matcher = lcd_propertyName_pattern.matcher(propertyname);
                    boolean propertyName_isValid = name_matcher.matches();

                    // Check the Property Value
                    String propertyvalue = request.getParameter("propertyValue");
                    Matcher value_matcher = lcd_propertyValue_pattern.matcher(propertyvalue);
                    boolean propertyValue_isValid = value_matcher.matches();

                    if (propertyName_isValid && propertyValue_isValid) {
//                        JOptionPane.showMessageDialog(dialog, "The Property Name is correct!", "Error", JOptionPane.ERROR_MESSAGE);
                        configManager.updateConfigPropertyGroups(configProperty, version);
                        request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
                    } else {
                        if (!propertyName_isValid) {
//                            JOptionPane.showMessageDialog(dialog, "The Property Name is incorrect!  It must be in a format simular to 20240328", "Error", JOptionPane.ERROR_MESSAGE);
//                            infoBox("The Property Name is incorrect!  It must be in a format simular to 20240328", "LCD Property Value");
                        }
                        if (!propertyValue_isValid) {
                            validationMessage.contains("");
//                            JOptionPane.showMessageDialog(dialog, "The Property Name is incorrect!  It must be in a format simular to 20240328", "Error", JOptionPane.ERROR_MESSAGE);
//                            infoBox("The Property Value is incorrect!  It must be in a format simular to 20240328", "LCD Property Value");
                        }
                        request.setAttribute("responseMessage", validationMessage);
                        request.getRequestDispatcher("/updateConfigProperty.jsp").forward(request, response);
                    }
                }
                // End Regex code
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Request for user profile pages and/or data
        System.out.println("Entry to UserServlet.doGet - requestType = " + request.getParameter("requestType") + " userid = " + request.getParameter("userid") + " selectedUserid - " + request.getParameter("selectedUserid") + ", selectedAll - " + request.getParameter("selectedAll"));
        String requestType = request.getParameter("requestType");
//        UserManager userManager = new UserManager();
        if (requestType != null && requestType.equalsIgnoreCase("updateConfigProperty")) {
            ConfigProperty configProperty = new ConfigProperty();
            String productVersion = request.getParameter("productVersion");
            configProperty.setPropertyGroup(request.getParameter("propertyGroup"));
            configProperty.setPropertyGroup(request.getParameter("propertyName"));
            configProperty.setPropertyGroup(request.getParameter("propertyValue"));

            request.setAttribute("configProperty", configProperty);

            request.getRequestDispatcher("/updateConfigProperty.jsp").forward(request, response);
        }
    }
}
