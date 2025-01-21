package com.optum.itl;

import java.sql.*;
import java.util.ArrayList;
import java.util.*;

import com.mysql.cj.Query;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ConfigManager {

    public static void main(String[] args) {

        String userId = args[0];
        String vmName = args[1];
        String action = args[2];

        System.out.println(userId);
        System.out.println(vmName);
        System.out.println(action);
    }

    public ArrayList<ConfigProperty> getConfigPropertyGroup(String groupName) {
        ArrayList<ConfigProperty> configPropertyGroup = new ArrayList<>();

        try {
            String driverName = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverName); // here is the ClassNotFoundException
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/itl", ConfigLoader.getProperty("User"), ConfigLoader.getProperty("DBCreds"));
            String query = "";
            if (groupName != null) {
                String selectedGroupNameplus = groupName;
                query = "select * from `itl`.`config_property` where property_group = ? order by property_order asc;";
                try {
                    PreparedStatement pstmt = conn.prepareStatement(query);
                        pstmt.setString(1, selectedGroupNameplus);
                        ResultSet rs = pstmt.executeQuery();

                    while(rs.next()) {
                        ConfigProperty configProperty = new ConfigProperty();
                        configProperty.setPropertyId(rs.getLong("property_id"));
                        configProperty.setPropertyGroup(rs.getString("property_group"));
                        configProperty.setPropertyName(rs.getString("property_name"));
                        configProperty.setPropertyValue(rs.getString("property_value"));
                        configProperty.setPropertyOrder(rs.getLong("property_order"));
                        configPropertyGroup.add(configProperty);
                    }
                    conn.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
        return configPropertyGroup;
    }

    public static void displayData(ResultSet rs) throws SQLException {
        System.out.println("property_id:" + rs.getLong(1));
        System.out.println("property_group:" + rs.getString(2));
        System.out.println("property_name:" + rs.getString(3));
        System.out.println("property_value:" + rs.getString(4));
        System.out.println("property_order:" + rs.getLong(5));
        System.out.println("");
    }

    /**
     * This function is designed to update the config_property table with the versions of:
     *  KB's, LCD's, CU's, Base Code, vSphere Templates.
     *  We will keep three version of each group name.
     *  There will be a total of eight records for each group.  4 rows for CES and 4 rows for CM.
     *  The Property_name for record 1 and 5 will be "<option value="" disabled>".  These records should never be deleted or moved.
     *  The top property name will be the oldest version.  The last property name will be the oldest.
     *  When updating CES records (1-4) with a new version, property name 1 will be removed.
     *    The second name will be moved up to one's spot and new version will take 2's spot.
     *    The property_name values must look like this: <option value="<property_name>">
     *    The property_value values represent a short name for the property_name values.
     *       Simular to CES_KB_2024_Q1A where the full name is simular to CES_KB_2024_Q1A_5.0-6.00.zip
     *    The property_order values will have to be updated to reflect the correct 1-4 order.
     *    Repeat the same for the CM records (5-8).
     *
     * @param configProperty
     * @param version
     */
    public void updateConfigPropertyGroups(ConfigProperty configProperty, String version ) {
//        System.out.println("Property: " + product);
        System.out.println("Version: " + version);
//        String newProduct = product;
        String newVersion = version;
        String newPropertyGroup = configProperty.getPropertyGroup();
        String newPropertyName = configProperty.getPropertyName();
        String newPropertyValue = configProperty.getPropertyValue();

        ArrayList<ConfigProperty> configPropertyGroupOrig = new ArrayList<>();
        ArrayList<ConfigProperty> configPropertyGroupUpdated = new ArrayList<>();
        ConfigProperty configPropertyNewLine = new ConfigProperty();

        try {
            // Retrieve the db rows as they are currently recorded.
            configPropertyGroupOrig = getConfigPropertyGroup(configProperty.getPropertyGroup());

            int rowcntr = 0;
            long orderNbr = 1;

            // Do nothing with the first row
            configPropertyNewLine = new ConfigProperty();
            configPropertyNewLine.setPropertyId(configPropertyGroupOrig.get(rowcntr).getPropertyId()); // Need to get a legit PropertyID
            configPropertyNewLine.setPropertyGroup(configPropertyGroupOrig.get(rowcntr).getPropertyGroup());
            configPropertyNewLine.setPropertyName(configPropertyGroupOrig.get(rowcntr).getPropertyName());
            configPropertyNewLine.setPropertyValue(configPropertyGroupOrig.get(rowcntr).getPropertyValue());
            configPropertyNewLine.setPropertyOrder(configPropertyGroupOrig.get(rowcntr).getPropertyOrder());
            configPropertyGroupUpdated.add(configPropertyNewLine);

            // Row2 will be removed by copying data from row3 into row2.
            rowcntr = 2;
            orderNbr = 2;
            configPropertyNewLine = new ConfigProperty();  // Reset configPropertyNewLine
            configPropertyNewLine.setPropertyId(configPropertyGroupOrig.get(rowcntr).getPropertyId()); // Need to get a legit PropertyID
            configPropertyNewLine.setPropertyGroup(configPropertyGroupOrig.get(rowcntr).getPropertyGroup());
            configPropertyNewLine.setPropertyName(configPropertyGroupOrig.get(rowcntr).getPropertyName());
            configPropertyNewLine.setPropertyValue(configPropertyGroupOrig.get(rowcntr).getPropertyValue());
            configPropertyNewLine.setPropertyOrder(configPropertyGroupOrig.get(rowcntr).getPropertyOrder());
            configPropertyGroupUpdated.add(configPropertyNewLine);
            // Update the third row with property order = 2
            configPropertyGroupUpdated.get(configPropertyGroupUpdated.size()-1).setPropertyOrder(orderNbr);

            // Update the fourth row with property order = 3
            rowcntr++;
            orderNbr = 3;
            configPropertyNewLine = new ConfigProperty();
            configPropertyNewLine.setPropertyId(configPropertyGroupOrig.get(rowcntr).getPropertyId());
            configPropertyNewLine.setPropertyGroup(configPropertyGroupOrig.get(rowcntr).getPropertyGroup());
            configPropertyNewLine.setPropertyName(configPropertyGroupOrig.get(rowcntr).getPropertyName());
            configPropertyNewLine.setPropertyValue(configPropertyGroupOrig.get(rowcntr).getPropertyValue());
            configPropertyNewLine.setPropertyOrder(configPropertyGroupOrig.get(rowcntr).getPropertyOrder());
            configPropertyGroupUpdated.add(configPropertyNewLine);
            configPropertyGroupUpdated.get(configPropertyGroupUpdated.size()-1).setPropertyOrder(orderNbr);

            // Add a new row
            rowcntr++;
            orderNbr = 4;
            configPropertyNewLine = new ConfigProperty();
            configPropertyNewLine.setPropertyId(configPropertyGroupOrig.get(1).getPropertyId());
            configPropertyNewLine.setPropertyGroup(newPropertyGroup);
            if (newPropertyGroup.contains("kb") || newPropertyGroup.contains("ddrkb")) {
                configPropertyNewLine.setPropertyName(newPropertyName.substring(0,15) + "CES_" + newPropertyName.substring(15,newPropertyName.length()-2)+".zip" + newPropertyName.substring(newPropertyName.length()-2,newPropertyName.length()));
                configPropertyNewLine.setPropertyValue("CES_" + newPropertyName.substring(15,newPropertyName.length()-10));
            } else if (newPropertyGroup.contains("lcd")) {
                configPropertyNewLine.setPropertyName("\""+ newPropertyName.substring(0,14) + "\"0_A_" + newPropertyName.substring(15,newPropertyName.length()-2)+".zip" + newPropertyName.substring(newPropertyName.length()-2,newPropertyName.length()));
                configPropertyNewLine.setPropertyValue("0_A_"+newPropertyName.substring(15,newPropertyName.length()-2)+".zip");
            }
            configPropertyNewLine.setPropertyOrder(configPropertyGroupOrig.get(1).getPropertyOrder());
            configPropertyGroupUpdated.add(configPropertyNewLine);
            configPropertyGroupUpdated.get(configPropertyGroupUpdated.size()-1).setPropertyOrder(orderNbr);

            // Do nothing with the fifth row
            rowcntr = 4;
            orderNbr = 5;
            configPropertyNewLine = new ConfigProperty();
            configPropertyNewLine.setPropertyId(configPropertyGroupOrig.get(rowcntr).getPropertyId());
            configPropertyNewLine.setPropertyGroup(configPropertyGroupOrig.get(rowcntr).getPropertyGroup());
            configPropertyNewLine.setPropertyName(configPropertyGroupOrig.get(rowcntr).getPropertyName());
            configPropertyNewLine.setPropertyValue(configPropertyGroupOrig.get(rowcntr).getPropertyValue());
            configPropertyNewLine.setPropertyOrder(configPropertyGroupOrig.get(rowcntr).getPropertyOrder());
            configPropertyGroupUpdated.add(configPropertyNewLine);
            configPropertyGroupUpdated.get(configPropertyGroupUpdated.size()-1).setPropertyOrder(orderNbr);

            // Delete the sixth row
            rowcntr = 6;
            orderNbr = 6;
            configPropertyNewLine = new ConfigProperty();
            configPropertyNewLine.setPropertyId(configPropertyGroupOrig.get(rowcntr).getPropertyId());
            configPropertyNewLine.setPropertyGroup(configPropertyGroupOrig.get(rowcntr).getPropertyGroup());
            configPropertyNewLine.setPropertyName(configPropertyGroupOrig.get(rowcntr).getPropertyName());
            configPropertyNewLine.setPropertyValue(configPropertyGroupOrig.get(rowcntr).getPropertyValue());
            configPropertyNewLine.setPropertyOrder(configPropertyGroupOrig.get(rowcntr).getPropertyOrder());
            configPropertyGroupUpdated.add(configPropertyNewLine);
            // Update the third row with property order = 2
            configPropertyGroupUpdated.get(configPropertyGroupUpdated.size()-1).setPropertyOrder(orderNbr);

            // Update the eighth row with property order = 7
            rowcntr++;
            orderNbr = 7;
            configPropertyNewLine = new ConfigProperty();
            configPropertyNewLine.setPropertyId(configPropertyGroupOrig.get(rowcntr).getPropertyId());
            configPropertyNewLine.setPropertyGroup(configPropertyGroupOrig.get(rowcntr).getPropertyGroup());
            configPropertyNewLine.setPropertyName(configPropertyGroupOrig.get(rowcntr).getPropertyName());
            configPropertyNewLine.setPropertyValue(configPropertyGroupOrig.get(rowcntr).getPropertyValue());
            configPropertyNewLine.setPropertyOrder(configPropertyGroupOrig.get(rowcntr).getPropertyOrder());
            configPropertyGroupUpdated.add(configPropertyNewLine);
            configPropertyGroupUpdated.get(configPropertyGroupUpdated.size()-1).setPropertyOrder(orderNbr);

            // Add a new row
            rowcntr = 7;
            orderNbr = 8;
            configPropertyNewLine = new ConfigProperty();
            configPropertyNewLine.setPropertyId(configPropertyGroupOrig.get(5).getPropertyId());
            configPropertyNewLine.setPropertyGroup(newPropertyGroup);
            if (newPropertyGroup.contains("kb") || newPropertyGroup.contains("ddrkb")) {
                configPropertyNewLine.setPropertyName(newPropertyName.substring(0,15) + "CM_" + newPropertyName.substring(15,newPropertyName.length()-2)+".zip" + newPropertyName.substring(newPropertyName.length()-2,newPropertyName.length()));
                configPropertyNewLine.setPropertyValue("CM_" + newPropertyName.substring(15,newPropertyName.length()-10));

            } else if (newPropertyGroup.contains("lcd")) {
                configPropertyNewLine.setPropertyName(newPropertyName.substring(0,14) + "\"0_B_" + newPropertyName.substring(15,newPropertyName.length()-2)+".zip" + newPropertyName.substring(newPropertyName.length()-2,newPropertyName.length()));
                configPropertyNewLine.setPropertyValue("0_B_"+newPropertyName.substring(15,newPropertyName.length()-2)+".zip");
            }
            configPropertyNewLine.setPropertyOrder(configPropertyGroupOrig.get(5).getPropertyOrder());
            configPropertyGroupUpdated.add(configPropertyNewLine);
            configPropertyGroupUpdated.get(configPropertyGroupUpdated.size()-1).setPropertyOrder(orderNbr);

            updateConfigPropertyTable(configPropertyGroupUpdated, newPropertyGroup);

        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
    }

    /**
     * Update the ConfigProperty table with the updated values
     * @param configPropertyGroupUpdated
     */
    public void updateConfigPropertyTable(ArrayList<ConfigProperty> configPropertyGroupUpdated, String newPropertyGroup ) {
        try {
            // Connect to the database
            String driverName = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverName); // here is the ClassNotFoundException
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/itl", ConfigLoader.getProperty("User"), ConfigLoader.getProperty("DBCreds"));

            if (newPropertyGroup != null) {
                String selectedGroupNameplus = newPropertyGroup;
                String deleteQuery = "Delete From itl.config_property Where property_group = ?;";
                try {
                    PreparedStatement pstmt = conn.prepareStatement(deleteQuery);
                    pstmt.setString(1, selectedGroupNameplus);
                    pstmt.executeUpdate();

                    String insertQuery = "INSERT INTO itl.config_property (property_group, property_name, property_value, property_order) VALUES (?,?,?,?)";
                    pstmt = conn.prepareStatement(insertQuery);

                    for (int i=0; i<=7; i++) {

                        pstmt.setString(1, configPropertyGroupUpdated.get(i).getPropertyGroup());
                        pstmt.setString(2, configPropertyGroupUpdated.get(i).getPropertyName());
                        pstmt.setString(3, configPropertyGroupUpdated.get(i).getPropertyValue());
                        pstmt.setInt(4, (int) configPropertyGroupUpdated.get(i).getPropertyOrder());
                        pstmt.executeUpdate(); // Execute the insert for each property
                    }
                    conn.close();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
    }
}
