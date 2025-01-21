package com.optum.itl;

import java.sql.*;
import java.util.ArrayList;

public class RequestManager {

//    public static void main(String[] args) {
//        String requestId = "";
//        String action = "";
//        for (String s: args) {
//            requestId = args[0];
//            action = args[2];
//        }
//        System.out.println(requestId);
//        System.out.println(action);
//        RequestManager requestManager = new RequestManager();
//        requestManager.updateRequest(requestId, action);
//    }

    public int addRequest(Request request) {
        System.out.println("Entry to RequestManager.addRequest");
        int requestID = 0;

        try {
            String driverName = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverName); // here is the ClassNotFoundException
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/itl", ConfigLoader.getProperty("User"), ConfigLoader.getProperty("DBCreds"));
            String insertSQL = "INSERT INTO `itl`.`request` (`request_type`, `user_id`, `user_group`, `status`, `submit_timestamp`, `template`, `vm_name`,  `product_install`,  `db`, `product_upgrade`, `kb`, `pe_lcd`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement stmt = conn.prepareStatement(insertSQL, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, request.getRequestType());
            stmt.setString(2, request.getUserId());
            stmt.setString(3, request.getUserGroup());
            stmt.setString(4, request.getStatus());
            stmt.setTimestamp(5, request.getSubmitTimestamp());
            stmt.setString(6, request.getTemplate());
            stmt.setString(7, request.getVmName());
            stmt.setString(8, request.getProductInstall());
            stmt.setString(9, request.getDb());
            stmt.setString(10, request.getProductUpgrade());
            stmt.setString(11, request.getKb());
            stmt.setString(12, request.getPeLcd());
            //stmt.setString(12, request.getFeLcd());
            //stmt.setString(13, request.getIlogSystemRules());
            System.out.println(stmt.toString());
            if (stmt.executeUpdate() > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if ( generatedKeys.next() ) {
                    requestID = generatedKeys.getInt(1);
                }
            }
            conn.close();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
        System.out.println("Exit from addRequest - Record inserted with id = " + requestID);
        return requestID;
    }

    public void updateRequest(String requestId, String action) {
        //System.out.println("Entry to updateRequest");
        try {
            String driverName = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverName); // here is the ClassNotFoundException
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/itl", ConfigLoader.getProperty("User"), ConfigLoader.getProperty("DBCreds"));
            String updateSQL = "";
            if (action.equalsIgnoreCase("Started")) {
                updateSQL = "UPDATE `itl`.`request` SET `status` = ?, `start_timestamp` = ? WHERE (`request_id` = ?);";
            }
            else {
                updateSQL = "UPDATE `itl`.`request` SET `status` = ?, `end_timestamp` = ? WHERE (`request_id` = ?);";
            }
            PreparedStatement stmt = conn.prepareStatement(updateSQL);
            stmt.setString(1, action);
            stmt.setTimestamp(2, getCurrentTimeStamp());
            stmt.setString(3, requestId);
            System.out.println(stmt.toString());
            stmt.executeUpdate();
            conn.close();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
        //System.out.println("Exit to updateRequest");
    }

    // This function is the original, used in prod, function.
    public ArrayList getRequests(String userid, String selectedUserid, String selectedAll) {
        System.out.println("Entry RequestManager.getRequests - userid = " + userid + " - selectedUserid = " + selectedUserid + " - selectedAll = " + selectedAll);
        ArrayList requestArray = new ArrayList();
        try {
            String driverName = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverName); // here is the ClassNotFoundException
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/itl", ConfigLoader.getProperty("User"), ConfigLoader.getProperty("DBCreds"));
            Statement statement = conn.createStatement();
            PreparedStatement pstmt = null;
            String psUseridplus = null;

                    String query;
            if (selectedAll != null) {
                query = "select * from `itl`.`request` order by submit_timestamp desc limit 100;";
            } else if (selectedUserid != null) {
                psUseridplus = selectedUserid;
                query = "select * from `itl`.`request` where user_id = ? order by submit_timestamp desc limit 100;";
                pstmt = conn.prepareStatement(query);

            } else {
                psUseridplus = userid;
                query = "select * from `itl`.`request` where user_id = ? order by submit_timestamp desc limit 100;";
                pstmt = conn.prepareStatement(query);
            }
            pstmt.setString(1, psUseridplus);
            ResultSet resultSet = pstmt.executeQuery();
            while(resultSet.next()) {
                Request request = new Request();
                request.setRequestId(resultSet.getInt("request_id"));
                request.setRequestType(resultSet.getString("request_type"));
                request.setUserId(resultSet.getString("user_id"));
                request.setStatus(resultSet.getString("status"));
                request.setSubmitTimestamp(resultSet.getTimestamp("submit_timestamp"));
                request.setStartTimestamp(resultSet.getTimestamp("start_timestamp"));
                request.setEndTimestamp(resultSet.getTimestamp("end_timestamp"));
                request.setTemplate(resultSet.getString("template"));
                request.setVmName(resultSet.getString("vm_name"));
                request.setUserGroup(resultSet.getString("user_group"));
                //This if condition is for view only on the viewRequest page
                if (request.getRequestType().equalsIgnoreCase("ddrBaseInstall")) {
                    request.setProductInstall(resultSet.getString("kb"));
                } else if (request.getRequestType().equalsIgnoreCase("ddrUpgradeInstall")) {
                    request.setProductInstall(resultSet.getString("kb"));
                } else if (request.getRequestType().equalsIgnoreCase("ddrKbInstall")) {
                    request.setProductInstall(resultSet.getString("kb"));
                } else if (request.getRequestType().equalsIgnoreCase("baseInstall")) {
                    request.setProductInstall(resultSet.getString("product_install"));
                } else if (request.getRequestType().equalsIgnoreCase("cuInstall")) {
                    request.setProductInstall(resultSet.getString("product_upgrade"));
                } else if (request.getRequestType().equalsIgnoreCase("kbLoad")) {
                    request.setProductInstall(resultSet.getString("kb"));
                } else if (request.getRequestType().equalsIgnoreCase("lcdLoad")) {
                    request.setProductInstall(resultSet.getString("pe_lcd"));
                }
                request.setDb(resultSet.getString("db"));
                request.setProductUpgrade(resultSet.getString("product_upgrade"));
                request.setKb(resultSet.getString("kb"));
                //request.setFeLcd(resultSet.getString("fe_lcd"));
                request.setPeLcd(resultSet.getString("pe_lcd"));
                request.setIlogSystemRules(resultSet.getString("ilog_system_rules"));
                requestArray.add(request);
            }
            conn.close();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
        return requestArray;
    }

    public boolean getActiveRequests(Request request) {
        System.out.println("Entry RequestManager.getActiveRequests");
        boolean otherActiveRequestsForVM = false;
        String vmName = SafeStr.codeqlClean(request.getVmName());

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String driverName = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverName); // here is the ClassNotFoundException
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/itl", ConfigLoader.getProperty("User"), ConfigLoader.getProperty("DBCreds"));

            Statement statement = conn.createStatement();
            String vmnameplus = vmName;
            String Started = "Started";
            String query = "select * from `itl`.`request` where vm_name = ? and status = ?;";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, vmnameplus);
            pstmt.setString(2, Started);

            rs = pstmt.executeQuery();
            System.out.println(statement.toString());
            if(rs.next()) {
                System.out.println("There are other active requests in progress for this VMName - " + vmName);
                otherActiveRequestsForVM = true;
            }
            conn.close();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
        return otherActiveRequestsForVM;
    }

    public boolean wasVMBuiltFrom531OrCentosTemplate(Request request) {
        System.out.println("Entry RequestManager.wasVMBuiltFrom531OrCentosTemplate");
        boolean wasVMBuiltFrom531OrCentosTemplate = false;
        String vmName = SafeStr.codeqlClean(request.getVmName());

        try {
            String driverName = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverName); // here is the ClassNotFoundException
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/itl", ConfigLoader.getProperty("User"), ConfigLoader.getProperty("DBCreds"));

            String vmnameplus = vmName;
            String query = "select * from `itl`.`request` where vm_name = ? and  (template like '%531%' or template like '%CentOS%');";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, vmnameplus);
            ResultSet rs = pstmt.executeQuery();

            if(rs.next()) {
                System.out.println("This functionality is not available yet for 5.3.1 VMs or Linux VMs");
                wasVMBuiltFrom531OrCentosTemplate = true;
            }

            conn.close();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
        return wasVMBuiltFrom531OrCentosTemplate;
    }

    public static Timestamp getCurrentTimeStamp() {
        java.util.Date today = new java.util.Date();
        return new Timestamp(today.getTime());
    }
}
