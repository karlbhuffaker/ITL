package com.optum.itl;

import com.sun.mail.smtp.SMTPTransport;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

public class EmailSender {

    private static final String USERNAME = "";
    private static final String PASSWORD = "";
    String SMTP_SERVER = "";
    String EMAIL_FROM = "";
    String EMAIL_CC = "";
    private static final String EMAIL_SUBJECT = "ITL Request #";
    private static final String EMAIL_TEXT = "Your ITL request job has ";

    public void sendEmail(Request request, String startOrEnd) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);
        String requestType = SafeStr.codeqlClean(request.getRequestType());
        String userGroup = SafeStr.codeqlClean(request.getUserGroup());
        String vmName = SafeStr.codeqlClean(request.getVmName());
        String status = SafeStr.codeqlClean(request.getStatus());


        Properties properties = System.getProperties();
        //Get email properties
        ConfigManager configManager = new ConfigManager();
        ArrayList<ConfigProperty> configPropertyGroup = configManager.getConfigPropertyGroup("email");
        for (ConfigProperty configProperty : configPropertyGroup) {
            if (configProperty.getPropertyName().equalsIgnoreCase("mail.smtp.host")) {
                properties.put("mail.smtp.host", configProperty.getPropertyValue());
                SMTP_SERVER = configProperty.getPropertyValue();
            } else if (configProperty.getPropertyName().equalsIgnoreCase("mail.smtp.auth")) {
                properties.put("mail.smtp.auth", configProperty.getPropertyValue());
            } else if (configProperty.getPropertyName().equalsIgnoreCase("mail.smtp.port")) {
                properties.put("mail.smtp.port", configProperty.getPropertyValue());
            } else if (configProperty.getPropertyName().equalsIgnoreCase("devops_email")) {
                EMAIL_FROM = configProperty.getPropertyValue();
                EMAIL_CC = configProperty.getPropertyValue();
            }
        }
        Session session = Session.getInstance(properties, null);
        Message message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(EMAIL_FROM));
            //Get UserProfile email address
            UserManager userManager = new UserManager();
            UserProfile userProfile = userManager.getUserProfile(userID);
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userProfile.getEmailAddress(), false));
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(EMAIL_CC, false));
            String templateOrProduct = "";
            if (request.getTemplate() != null && !request.getTemplate().isEmpty()) {
                templateOrProduct = request.getTemplate();
            } else if (request.getProductInstall() != null && !request.getProductInstall().isEmpty()) {
                templateOrProduct = request.getProductInstall();
            } else if (request.getProductUpgrade() != null && !request.getProductUpgrade().isEmpty()) {
                templateOrProduct = request.getProductUpgrade();
            } else if (request.getKb() != null && !request.getKb().isEmpty()) {
                templateOrProduct = request.getKb();
            } else if (request.getPeLcd() != null && !request.getPeLcd().isEmpty()) {
                templateOrProduct = request.getPeLcd();
            }
            if (startOrEnd.equalsIgnoreCase("Started")) {
                message.setSubject(EMAIL_SUBJECT + requestID + " - Request Type - " + requestType + " - User Group - " + userGroup +  ", VM Name - " + vmName + ", Job status - Started");
                message.setText(EMAIL_TEXT + "started - ITL Request #" + requestID + ", Request Type - " + requestType + " - User Group - " + userGroup + ", VM Name - " + vmName + ", Requested Template/Product - " + templateOrProduct + ", Job status - Started");
            }
            else {
                message.setSubject(EMAIL_SUBJECT + requestID + " - Request Type - " + requestType + " - User Group - " + userGroup + ", VM Name - " + vmName + ", Job status - " + status);
                message.setText(EMAIL_TEXT + "ended - ITL Request #" + requestID + ", Request Type - " + requestType + " - User Group - " + userGroup + ", VM Name - " + vmName + ", Requested Template/Product - " + templateOrProduct + ", Job status - " + status + System.lineSeparator() + "Please verify the request was completed as expected.");
            }
            message.setSentDate(new Date());
            SMTPTransport smtpTransport = (SMTPTransport) session.getTransport("smtp");
            smtpTransport.connect(SMTP_SERVER, USERNAME, PASSWORD);
            smtpTransport.sendMessage(message, message.getAllRecipients());
            System.out.println("Response: " + smtpTransport.getLastServerResponse());
            smtpTransport.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
