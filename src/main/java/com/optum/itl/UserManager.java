package com.optum.itl;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;

public class UserManager {

    public static void main(String[] args) {
        String userId = "";
        String vmName = "";
        String action = "";
        for (String s: args) {
            userId = args[0];
            vmName = args[1];
            action = args[2];
        }
        System.out.println(userId);
        System.out.println(vmName);
        System.out.println(action);
        UserManager userManager = new UserManager();
        if (action.equalsIgnoreCase("AddVM")) {
            userManager.addUserVM(userId, vmName);
        } else if (action.equalsIgnoreCase("deleteVM")) {
            userManager.deleteUserVM(userId, vmName);
        }
        userManager.updateUserProfile(userId, action);

        //        UserProfile userProfile = userManager.getUserProfile(userId);
        //        UserCredentials userCredentials = new UserCredentials();
        //        DirContext dirContext = userCredentials.connnectToLDAP();
        //        //NOTE: replace theUserName below with the Active Directory/LDAP user whose attribites you want printed.
        //        userCredentials.getUserInfo("userid", dirContext);
    }

    public UserProfile getUserProfile(String userid, String password) {
        DirContext dirContext = isUserIdValid(userid, password);
        UserProfile userProfile = null;
        if (dirContext != null) {
            userProfile = getUserProfile(userid);
        }
        return userProfile;
    }

    public UserProfile getUserProfile(String userid) {
        UserProfile userProfile = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            String driverName = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverName); // here is the ClassNotFoundException
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/itl", ConfigLoader.getProperty("User"), ConfigLoader.getProperty("DBCreds"));
            String query = null;

            if (userid != null) {
                String useridplus = userid;
                query = "SELECT * FROM `itl`.`user_profile` WHERE user_id = ?;";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, useridplus);
                rs = pstmt.executeQuery();

                while (rs.next()) {
                    userProfile = new UserProfile();
                    userProfile.setUserId(rs.getString("user_id"));
                    userProfile.setUserGroup(rs.getString("user_group"));
                    userProfile.setUserName(rs.getString("user_name"));
                    userProfile.setStatus(rs.getString("status"));
                    userProfile.setEmailAddress(rs.getString("email_address"));
                    userProfile.setAllowedVmTotal(rs.getInt("allowed_vm_total"));
                    userProfile.setCurrentVmTotal(rs.getInt("current_vm_total"));
                }
                conn.close();
            }
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
        return userProfile;
    }

    public ArrayList getUserProfiles(String userid) {
        ArrayList userProfileArray = new ArrayList();
        PreparedStatement stmt = null;
        String insertSQL = null;
        ResultSet rs = null;

        try {
            String driverName = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverName); // here is the ClassNotFoundException
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/itl", ConfigLoader.getProperty("User"), ConfigLoader.getProperty("DBCreds"));

            String query = null;
            if (userid != null) {
                String selectedUseridplus = userid;
                insertSQL = "select * from `itl`.`user_profile` where user_id = ? order by user_id asc;";
                stmt = conn.prepareStatement(insertSQL);
                stmt.setString(1, selectedUseridplus);
                rs = stmt.executeQuery();

            } else {
                query = "SELECT * FROM `itl`.`user_profile` order by user_id asc;";
                stmt = conn.prepareStatement(query);
                rs = stmt.executeQuery();
            }

            while(rs.next()) {
                UserProfile userProfile = new UserProfile();
                userProfile.setUserId(rs.getString("user_id"));
                userProfile.setUserGroup(rs.getString("user_group"));
                userProfile.setUserName(rs.getString("user_name"));
                userProfile.setStatus(rs.getString("status"));
                userProfile.setEmailAddress(rs.getString("email_address"));
                userProfile.setAllowedVmTotal(rs.getInt("allowed_vm_total"));
                userProfile.setCurrentVmTotal(rs.getInt("current_vm_total"));
                userProfileArray.add(userProfile);
            }
            conn.close();

        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
        return userProfileArray;
    }

    public void addUserProfile(UserProfile userProfile) {
        System.out.println("Entry to addUserProfile");
        try {
            String driverName = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverName); // here is the ClassNotFoundException
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/itl", ConfigLoader.getProperty("User"), ConfigLoader.getProperty("DBCreds"));
            String insertSQL = "INSERT INTO `itl`.`user_profile` (`user_id`, `user_name`, `user_group`, `email_address`, `status`, `create_timestamp`, `allowed_vm_total`, `current_vm_total`) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement stmt = conn.prepareStatement(insertSQL, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, userProfile.getUserId());
            stmt.setString(2, userProfile.getUserName());
            if (userProfile.getUserGroup().equalsIgnoreCase("devops")) {
                stmt.setString(3, "devops");
            } else if (userProfile.getUserGroup().equalsIgnoreCase("development")) {
                stmt.setString(3, "development");
            } else if (userProfile.getUserGroup().equalsIgnoreCase("QA_Support")) {
                stmt.setString(3, "QA_Support");
            } else {
                stmt.setString(3, "other");
            }
            stmt.setString(4, userProfile.getEmailAddress());
            stmt.setString(5, userProfile.getStatus());
            stmt.setTimestamp(6, getCurrentTimeStamp());
            stmt.setInt(7, userProfile.getAllowedVmTotal());
            stmt.setInt(8, userProfile.getCurrentVmTotal());
            System.out.println(stmt.toString());
            stmt.executeUpdate();
            conn.close();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
        System.out.println("Exit from addUserProfile");
    }

    public void updateUserProfile(String userId, String requestType) {
        updateUserProfile(userId, requestType, null, null, null);
    }

    public void updateUserProfile(String userId, String requestType, String userName, String userGroup, String emailAddress) {
        System.out.println("Entry to updateUserProfile");
        try {
            String driverName = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverName); // here is the ClassNotFoundException
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/itl", ConfigLoader.getProperty("User"), ConfigLoader.getProperty("DBCreds"));
            UserProfile userProfile = getUserProfile(userId);
            String updateSQL = "UPDATE `itl`.`user_profile` SET `current_vm_total` = ? WHERE (`user_id` = ?);";
            PreparedStatement stmt = conn.prepareStatement(updateSQL);
            if (requestType == null) {
                //Update user name, user group and email address
                String updateUserInfoSQL = "UPDATE `itl`.`user_profile` SET `user_name` = ?, `user_group` = ?, `email_address` = ? WHERE (`user_id` = ?);";
                PreparedStatement userInfoStmt = conn.prepareStatement(updateUserInfoSQL);
                userInfoStmt.setString(1, userName);
                userInfoStmt.setString(2, userGroup);
                userInfoStmt.setString(3, emailAddress);
                userInfoStmt.setString(4, userId);
                System.out.println(userInfoStmt.toString());
                userInfoStmt.executeUpdate();
            } else {
                if (requestType.equalsIgnoreCase("provisionVM")) {
                    //Add 1 to current VM total
                    stmt.setInt(1, userProfile.getCurrentVmTotal() + 1);
                } else if (requestType.equalsIgnoreCase("deleteVM")) {
                    //Subtract 1 from current VM total
                    stmt.setInt(1, userProfile.getCurrentVmTotal() - 1);
                }
                stmt.setString(2, userId);
                System.out.println(stmt.toString());
                stmt.executeUpdate();
            }
            conn.close();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
        System.out.println("Exit to updateUserProfile");
    }

    public void updateUserProfile(UserProfile userProfile) {
        System.out.println("Entry to updateUserProfile - request userid = " + userProfile.getUserId());
        try {
            String driverName = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverName); // here is the ClassNotFoundException
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/itl", ConfigLoader.getProperty("User"), ConfigLoader.getProperty("DBCreds"));
            UserProfile tbuUserProfile = getUserProfile(userProfile.getUserId());
            String updateSQL = "UPDATE `itl`.`user_profile` SET `user_name` = ?, `user_group` = ?, `email_address` = ?, `status` = ?, `allowed_vm_total` = ?, `current_vm_total` = ?, `update_timestamp` = ?, `update_user_id` = ? WHERE (`user_id` = ?);";
            PreparedStatement stmt = conn.prepareStatement(updateSQL);
            stmt.setString(1, userProfile.getUserName());
            stmt.setString(2, userProfile.getUserGroup());
            stmt.setString(3, userProfile.getEmailAddress());
            stmt.setString(4, userProfile.getStatus());
            stmt.setInt(5, userProfile.getAllowedVmTotal());
            stmt.setInt(6, userProfile.getCurrentVmTotal());
            stmt.setTimestamp(7, userProfile.getUpdateTimestamp());
            stmt.setString(8, userProfile.getUpdateUserId());
            stmt.setString(9, userProfile.getUserId());
            System.out.println(stmt.toString());
            stmt.executeUpdate();
            conn.close();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
        System.out.println("Exit to updateUserProfile");
    }

    public void deleteUserProfile(String userId) {
        System.out.println("Entry to UserManager.deleteUserProfile");
        try {
            String driverName = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverName); // here is the ClassNotFoundException
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/itl", ConfigLoader.getProperty("User"), ConfigLoader.getProperty("DBCreds"));
            String deleteSQL = "DELETE FROM `itl`.`user_profile` WHERE (`user_id` = ?);";
            PreparedStatement stmt = conn.prepareStatement(deleteSQL, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, userId);
            System.out.println(stmt.toString());
            stmt.executeUpdate();
            conn.close();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
        System.out.println("Exit from deleteUserProfile - UserVM deleted - userid = " + userId);
    }

    public void addUserVM(String userId, String vmName) {
        System.out.println("Entry to UserManager.addUserVM");
        try {
            String driverName = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverName); // here is the ClassNotFoundException
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/itl", ConfigLoader.getProperty("User"), ConfigLoader.getProperty("DBCreds"));
            String insertSQL = "INSERT INTO `itl`.`user_vm` (`user_id`, `vm_name`) VALUES (?, ?);";
            PreparedStatement stmt = conn.prepareStatement(insertSQL, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, userId);
            stmt.setString(2, vmName);
            System.out.println(stmt.toString());
            stmt.executeUpdate();
            conn.close();
        } catch (Exception e) {
            System.out.println("Exception - addUserVM failed");
            e.printStackTrace();
        }
        System.out.println("Exit from addUserVM - userVM added - with userId = " + userId + "vmName = " + vmName);
    }

    public void deleteUserVM(String userId, String vmName) {
        System.out.println("Entry to UserManager.deleteUserVM");
        try {
            String driverName = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverName); // here is the ClassNotFoundException
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/itl", ConfigLoader.getProperty("User"), ConfigLoader.getProperty("DBCreds"));
            String deleteSQL = "DELETE FROM `itl`.`user_vm` WHERE (`user_id` = ? AND `vm_name` = ?);";
            PreparedStatement stmt = conn.prepareStatement(deleteSQL, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, userId);
            stmt.setString(2, vmName);
            System.out.println(stmt.toString());
            stmt.executeUpdate();
            conn.close();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
        System.out.println("Exit from deleteUserVM - UserVM deleted - userid = " + userId + "VMName = " + vmName);
    }

    public ArrayList<UserVm> getUserVMs() {
        ArrayList<UserVm> userVMs = new ArrayList();
        try {
            String driverName = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverName); // here is the ClassNotFoundException
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/itl", ConfigLoader.getProperty("User"), ConfigLoader.getProperty("DBCreds"));
            String query = "SELECT * FROM `itl`.`user_vm` order by user_id asc, vm_name asc;";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                UserVm userVm = new UserVm();
                userVm.setUserId(rs.getString("user_id"));
                userVm.setVmName(rs.getString("vm_name"));
                userVMs.add(userVm);
            }

            conn.close();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
        return userVMs;
    }

    public UserVm getUserVm(String vmName) {
        UserVm userVm = null;

        try {
            String driverName = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverName); // here is the ClassNotFoundException
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/itl", ConfigLoader.getProperty("User"), ConfigLoader.getProperty("DBCreds"));
            PreparedStatement pstmt = null;
            String query = null;

            if (vmName != null) {
                String vmnameplus = vmName;
                query = "SELECT * FROM `itl`.`user_vm` WHERE vm_name = ?;";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, vmnameplus);
                ResultSet rs = pstmt.executeQuery();
                while(rs.next()) {
                    userVm = new UserVm();
                    userVm.setUserId(rs.getString("user_id"));
                    userVm.setVmName(rs.getString("vm_name"));
                }
            }

            conn.close();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
        return userVm;
    }

    public ArrayList<UserVm> getUserVms(String userid) {
        ArrayList<UserVm> userVms = new ArrayList();
        try {
            String driverName = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverName); // here is the ClassNotFoundException
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/itl", ConfigLoader.getProperty("User"), ConfigLoader.getProperty("DBCreds"));
            PreparedStatement pstmt = null;
            String useridplus = userid;
            String query = null;
            if (userid != null) {
                query = "SELECT * FROM `itl`.`user_vm` WHERE user_id = ? order by vm_name asc;";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, useridplus);
                ResultSet rs = pstmt.executeQuery();

                while(rs.next()) {
                    UserVm userVm = new UserVm();
                    userVm.setUserId(rs.getString("user_id"));
                    userVm.setVmName(rs.getString("vm_name"));
                    userVms.add(userVm);
                }
            } else {
                throw new Exception("Invalid userid");
            }
            conn.close();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
        return userVms;
    }

    private DirContext isUserIdValid(String userid, String password) {
        DirContext ctx = null;
        try{
            Hashtable env = new Hashtable();
            //Get ldap properties
            ConfigManager configManager = new ConfigManager();
            ArrayList<ConfigProperty> configPropertyGroup = configManager.getConfigPropertyGroup("ldap");
            for (ConfigProperty configProperty : configPropertyGroup) {
                if (configProperty.getPropertyName().contains("INITIAL_CONTEXT_FACTORY")) {
                    env.put(Context.INITIAL_CONTEXT_FACTORY, configProperty.getPropertyValue());
                } else if (configProperty.getPropertyName().contains("PROVIDER_URL")) {
                    env.put(Context.PROVIDER_URL, configProperty.getPropertyValue());
                } else if (configProperty.getPropertyName().contains("SECURITY_AUTHENTICATION")) {
                    env.put(Context.SECURITY_AUTHENTICATION, configProperty.getPropertyValue());
                } else if (configProperty.getPropertyName().contains("SECURITY_PRINCIPAL")) {
                    env.put(Context.SECURITY_PRINCIPAL, userid + configProperty.getPropertyValue());
                }
            }
            env.put(Context.SECURITY_CREDENTIALS, password);
            System.out.println("Attempting to Connect...");
            ctx = new InitialDirContext(env);
            System.out.println("LDAP Connection Successful.");
        }catch(NamingException nex){
            System.out.println("LDAP Connection failed.");
            nex.printStackTrace();
        }
        return ctx;
    }

//    public UserProfile isUserActive(String userid, String password){
    //Verify user exists and is active in USER_PROFILE DB

//        DirContext dirContext = connnectToLDAP(userid, password);
//        if (dirContext != null) {
//            getUserProfileInfo(userid, dirContext);
//        }
//        return getUserProfileInfo(userid, dirContext);
//    }

//    private UserProfile getUserProfileInfo(String username, DirContext ctx) {
//        //Get UserProfile from DB
//        //Build UserProfile object
//
//        try {
//            SearchControls constraints = new SearchControls();
//            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
//            //NOTE: The attributes in array below are needed for the UI:
//            //distinguishedName: CN=User Name
//            //mail: user.name@optum.com
//            //memberOf: CN=ICPLAB_DEVOPS_GLOBAL,OU=icp.lab,DC=otl,DC=lab, CN=ICPLAB_DEVOPS
//            //if accountExpires: 9223372036854775807 then account is active
//            String[] attrIDs = { "distinguishedName", "mail", "accountExpires", "memberof"};
//            constraints.setReturningAttributes(attrIDs);
//            //NOTE: replace OU=forest,DC=domain,DC=com below with your domain info. It is essentially the Base Node for Search.
//            NamingEnumeration creds = ctx.search("OU=icp.lab,DC=otl,DC=lab", "sAMAccountName=" + username, constraints);
//            if (creds.hasMore()) {
//                Attributes attrs = ((SearchResult) creds.next()).getAttributes();
//                System.out.println(attrs.get("distinguishedName"));
//                System.out.println(attrs.get("mail"));
//                System.out.println(attrs.get("accountExpires"));
//                System.out.println(attrs.get("memberOf"));
//                UserProfile userProfile = new UserProfile();
//                userProfile
//                if (attrs.get("accountExpires").equals("9223372036854775807")) {
//                    UserProfile userProfile = new UserProfile();
//                }
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return isUserValid;
//    }

    public static Timestamp getCurrentTimeStamp() {
        java.util.Date today = new java.util.Date();
        return new Timestamp(today.getTime());
    }

}
