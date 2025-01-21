package com.optum.itl;

import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JobBuilder {

    public static void main(String[] args) {
        Request request = new Request();
        JobBuilder jobBuilder = new JobBuilder();
        jobBuilder.buildJob(request);
    }

    public Request testing(Request request) {
        request.setRequestType("baseInstall");
        request.setUserId("jschwab2");
        request.setUserGroup("devops");
        request.setSubmitTimestamp(RequestManager.getCurrentTimeStamp());
        request.setTemplate("CM54SP2-SQL16-Win16-Template");
        request.setVmName("jasvm24");
        request.setProductInstall("CES_5.4_SP2-CU02.2_Base_Install.zip");
        request.setDb("SQL");
        request.setProductUpgrade("CM_5.4-SP2_CU03.zip");
        request.setKb("CM_KB_2020_Q3B_5.0-5.4.zip");
        request.setPeLcd("0_B_20200515_5.X.zip");
        request.setFeLcd("0_A_20200515_5.X.zip");
        return request;
    }

    public void buildJob(Request request) {
        int requestID = SafeStr.isRequestIdValid(request);
        String requestType = SafeStr.codeqlClean(request.getRequestType());

        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Entry to buildJob");
        boolean testing = false;
        if (testing) {
            request = testing(request);
        }
        //build the job file
        buildJobFolder(request);
        //Build job command files and install file folders
        if (requestType.equalsIgnoreCase("provisionVM")
                || requestType.equalsIgnoreCase("startVM")
                || requestType.equalsIgnoreCase("restartVM")
                || requestType.equalsIgnoreCase("restartVMGuest")
                || requestType.equalsIgnoreCase("stopVM")
                || requestType.equalsIgnoreCase("stopVMGuest")
                || requestType.equalsIgnoreCase("deleteVM")) {
            buildProvisionMaintainVMFile(request);
        }
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Exit from buildJob");
    }

    private void buildJobFolder(Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);

        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Entry to buildJobFolder");
        //Create new directory folder for all the files needed to fulfil the request
        try {
            // Check if the path is safe.
            Path publicFolder = Paths.get("d:/Optum/ITL/runtime/" + userID + "/" + requestID).normalize().toAbsolutePath();
            if (!SafeStr.getValidPathName(publicFolder)) {
                throw new IllegalArgumentException("Invalid folder name");
            } else {
                Path target = Paths.get("d:/Optum/ITL/runtime/" + userID + "/" + requestID);
                Files.createDirectories(target);
            }

//            Path target = Paths.get("d:/Optum/ITL/runtime/" + request.getUserId() + "/" + requestID);
//            Files.createDirectories(target);
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error occurred creating the batch job folder.");
            e.printStackTrace();
        }
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Exit from buildJobFolder");
    }

    private void buildProvisionMaintainVMFile(Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);
        String requestType = SafeStr.codeqlClean(request.getRequestType());
        String vmName = SafeStr.codeqlClean(request.getVmName());
        String userGroup = SafeStr.codeqlClean(request.getUserGroup());

        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Entry to buildProvisionMaintainVMFile - request type - "  + requestType);

        // Check if the path is safe.
        String repoProvisionMaintainVMFile = null;
        String provisionMaintainVMFile = null;

        if (!SafeStr.pathValidator(request, "D:/app/Tomcat9/webapps/ITL/WEB-INF/classes/powershell/vm_services/")) {
            throw new IllegalArgumentException("Invalid filename");
        } else {
            repoProvisionMaintainVMFile = "D:/app/Tomcat9/webapps/ITL/WEB-INF/classes/powershell/vm_services/" + requestType + ".ps1";
        }
        if (!SafeStr.pathValidator(request, "D:/Optum/ITL/runtime/")) {
            throw new IllegalArgumentException("Invalid filename");
        } else {
            provisionMaintainVMFile = "D:/Optum/ITL/runtime/" + userID + "/" + requestID + "/" + requestType + ".ps1";
        }


        //Copy provision maintain VM powershell file from repository folder to request folder
        try {
            //copy the Powershell file to the new folder
            if (SafeStr.codeqlClean(repoProvisionMaintainVMFile) != null && SafeStr.codeqlClean(provisionMaintainVMFile) != null) {
                Files.copy(new File(repoProvisionMaintainVMFile).toPath(), new File(provisionMaintainVMFile).toPath(), StandardCopyOption.REPLACE_EXISTING);

                //Update the Powershell file based on request info
                BufferedReader bufferedReader = new BufferedReader(new FileReader(provisionMaintainVMFile));
                String line;
                ArrayList<String> lines = new ArrayList<String>();
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("${vmName}")) {
                        line = line.replace("${vmName}", "'" + vmName + "'");
                    }
                    if (line.contains("${vCenterName}")) {
                        line = line.replace("${vCenterName}", "'icp-L33-vcenter.otl.lab'");
                    }
                    if (line.contains("${vServiceUser}")) {
                        line = line.replace("${vServiceUser}", "'" + ConfigLoader.getProperty("ServiceUser") + "'");
                    }
                    if (line.contains("${vServicePassword}")) {
                        line = line.replace("${vServicePassword}", "'" + ConfigLoader.getProperty("ICPCreds") + "'");
                    }
                    if (line.contains("${userName}")) {
                        line = line.replace("${userName}", "'" + ConfigLoader.getProperty("ICPUser") + "'");
                    }
                    if (line.contains("${pw}")) {
                        line = line.replace("${pw}", "'" + ConfigLoader.getProperty("ICPCreds") + "'");
                    }
                    if (line.contains("${resourcePool}")) {
                        if (userGroup.equalsIgnoreCase("devops")) {
                            line = line.replace("${resourcePool}", "DEVOPS");
                        } else if (userGroup.equalsIgnoreCase("Development")) {
                            line = line.replace("${resourcePool}", "DEV_1");
                        } else if (userGroup.equalsIgnoreCase("QA_Support")) {
                            line = line.replace("${resourcePool}", "QA_SUPP_1");
                        }
                    }
                    if (line.contains("${OU}")) {
                        if (userGroup.equalsIgnoreCase("devops")) {
                            line = line.replace("${OU}", "devops");
                        } else if (userGroup.equalsIgnoreCase("Development")) {
                            line = line.replace("${OU}", "development");
                        } else if (userGroup.equalsIgnoreCase("QA_Support")) {
                            line = line.replace("${OU}", "QA_SUPPORT");
                        }
                    }
                    if (line.contains("${DC}")) {
                        if (userGroup.equalsIgnoreCase("devops")) {
                            line = line.replace("${DC}", "DEVOPS");
                        } else if (userGroup.equalsIgnoreCase("Development")) {
                            line = line.replace("${DC}", "development");
                        } else if (userGroup.equalsIgnoreCase("QA_Support")) {
                            line = line.replace("${DC}", "qa");
                        }
                    }
                    if (line.contains("${server}")) {
                        if (userGroup.equalsIgnoreCase("devops")) {
                            line = line.replace("${server}", "'" + "icplabs-dc-03.devops.icp.lab" + "'");
                        } else if (userGroup.equalsIgnoreCase("Development")) {
                            line = line.replace("${server}", "'" + "icplabs-dc-05.development.icp.lab" + "'");
                        } else if (userGroup.equalsIgnoreCase("QA_Support")) {
                            line = line.replace("${server}", "'" + "icplabs-dc-07.qa.icp.lab" + "'");
                        }
                    }
                    if (line.contains("${dhcpServer}")) {
                        if (userGroup.equalsIgnoreCase("devops")) {
                            line = line.replace("${dhcpServer}", "'" + "icplabs-dc-01.icp.lab" + "'");
                        } else if (userGroup.equalsIgnoreCase("Development")) {
                            line = line.replace("${dhcpServer}", "'" + "icplabs-dc-05.development.icp.lab" + "'");
                        } else if (userGroup.equalsIgnoreCase("QA_Support")) {
                            line = line.replace("${dhcpServer}", "'" + "icplabs-dc-07.qa.icp.lab" + "'");
                        }
                    }
                    if (line.contains("${scopeId}")) {
                        if (userGroup.equalsIgnoreCase("devops")) {
                            line = line.replace("${scopeId}", "devops.icp.lab");
                        } else if (userGroup.equalsIgnoreCase("Development")) {
                            line = line.replace("${scopeId}", "development.icp.lab");
                        } else if (userGroup.equalsIgnoreCase("QA_Support")) {
                            line = line.replace("${scopeId}", "qa.icp.lab");
                        }
                    }
                    if (line.contains("${osSpec}")) {
                        if (userGroup.equalsIgnoreCase("devops")) {
                            if (request.getTemplate().contains("RHEL9")) {
                                line = line.replace("${osSpec}", "RHEL9");
                            } else if (request.getTemplate().contains("Rocky")) {
                                line = line.replace("${osSpec}", "Rocky");
                            } else {
                                line = line.replace("${osSpec}", "DEVOPS");
                            }
                        } else if (userGroup.equalsIgnoreCase("Development")) {
                            if (request.getTemplate().contains("RHEL9")) {
                                line = line.replace("${osSpec}", "RHEL9");
                            } else if (request.getTemplate().contains("Rocky")) {
                                line = line.replace("${osSpec}", "Rocky");
                            } else {
                                line = line.replace("${osSpec}", "DEVELOPMENT");
                            }
                        } else if (userGroup.equalsIgnoreCase("QA_Support")) {
                            if (request.getTemplate().contains("RHEL9")) {
                                line = line.replace("${osSpec}", "RHEL9");
                            } else if (request.getTemplate().contains("Rocky")) {
                                line = line.replace("${osSpec}", "Rocky");
                            } else {
                                line = line.replace("${osSpec}", "QA_SUPPORT");
                            }
                        }
                    }
                    if (line.contains("${dataStore}")) {
                        if (userGroup.equalsIgnoreCase("devops")) {
                            line = line.replace("${dataStore}", "DEVOPS_DATA");
                        } else if (userGroup.equalsIgnoreCase("Development")) {
                            line = line.replace("${dataStore}", "DEVELOPMENT_DS");
                        } else if (userGroup.equalsIgnoreCase("QA_Support")) {
                            line = line.replace("${dataStore}", "QA_SUPPORT_DS");
                        }
                    }
                    if (line.contains("${networkName}")) {
                        if (userGroup.equalsIgnoreCase("devops")) {
                            line = line.replace("${networkName}", "icp-vm-737");
                        } else if (userGroup.equalsIgnoreCase("Development")) {
                            line = line.replace("${networkName}", "icp-vm-739");
                        } else if (userGroup.equalsIgnoreCase("QA_Support")) {
                            line = line.replace("${networkName}", "icp-vm-741");
                        }
                    }
                    if (line.contains("${vmHost}")) {
                        if (userGroup.equalsIgnoreCase("devops")) {
                            line = line.replace("${vmHost}", "vmicp22.otl.lab");
                        } else if (userGroup.equalsIgnoreCase("Development")) {
                            line = line.replace("${vmHost}", "vmicp20.otl.lab");
                        } else if (userGroup.equalsIgnoreCase("QA_Support")) {
                            line = line.replace("${vmHost}", "vmicp26.otl.lab");
                        }
                    }
                    if (line.contains("${vmLocation}")) {
                        line = line.replace("${vmLocation}", "ICPLAB");
                    }
                    if (line.contains("${template}")) {
                        line = line.replace("${template}", request.getTemplate());
                    }
                    if (request.getTemplate() != null) {
                        String vmLoggingFolderPath = null;
                        if (!SafeStr.pathValidator(request, "D:/Optum/ITL/runtime/")) {
                            throw new IllegalArgumentException("Invalid filename");
                        } else {
                            vmLoggingFolderPath = "'D:\\Optum\\ITL\\runtime\\" + userID + "\\" + requestID + "\\'";
                        }

                        if (request.getTemplate().contains("SQL")) {
                            if (line.contains("#D:")) {
                                line = line.replace("#D:", "D:");
                                line = line.replace("${vmName2}", "'" + vmName + "'");
                                line = line.replace("${vmService}", "'MSSQLSERVER'");
                                line = line.replace("${vmLoggingFolderPath}", vmLoggingFolderPath);
                                line = line.replace("${vmLogName}", "'" + requestType + ".log'");
                            }
                        }
                    }
                    lines.add(line);
                }
                bufferedReader.close();

                if (SafeStr.codeqlClean(provisionMaintainVMFile) != null) {
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(provisionMaintainVMFile));
                    for (String s : lines) {
                        bufferedWriter.write(s);
                        bufferedWriter.write("\n");
                        bufferedWriter.flush();
                    }
                    bufferedWriter.close();
                } else {
                    throw new IOException("File is not safe");
                }
            } else {
                throw new IOException("File is not safe");
            }
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error occurred while copying/updating Powershell file  - provisionMaintainVMFile = " + provisionMaintainVMFile);
            e.printStackTrace();
        }
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Exit from buildProvisionMaintainVMFile");
    }

    public static void zip(String directoryToBeZippedStr, String zipFileStr) {
        System.out.println(Timestamp.from(Instant.now()) + " - Entry to zip");
        File directoryToBeZipped = new File(directoryToBeZippedStr);
        File zipFile = new File(zipFileStr);
        ZipFile zip = new ZipFile(zipFile);

        // Adding the list of files and directories to be zipped to a list
        ArrayList<File> fileList = new ArrayList<>();
        Arrays.stream(directoryToBeZipped.listFiles()).forEach((File file) -> {fileList.add(file);});

        ZipParameters parameters = new ZipParameters();
        //parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        //parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FASTEST);
        parameters.setEncryptFiles(false);
        fileList.stream().forEach((File f) -> {
            try
            {
                if(f.isDirectory()) {
                    zip.addFolder(f, parameters);
                } else {
                    zip.addFile(f, parameters);
                }
            }
            catch(ZipException zipException) {
                System.out.println(Timestamp.from(Instant.now()) + " - " + "ZIP Exception while creating encrypted zips.");
            }
        });
        System.out.println(Timestamp.from(Instant.now()) + " - Exit from zip");
    }

    public static void unzip(final String zipFilePath, final String unzipLocation) {
        System.out.println(Timestamp.from(Instant.now()) + " - Entry to unzip");

        try  {
            if (SafeStr.codeqlClean(zipFilePath) != null) {
                try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
                    ZipEntry entry = zipInputStream.getNextEntry();

                    while (entry != null) {
                        Path filePath = Paths.get(unzipLocation, SafeStr.validatePath(entry.getName())).normalize();
                        if (!filePath.startsWith(Paths.get(unzipLocation).normalize())) {
                            throw new Exception("Bad zip entry");
                        }

                        if (!entry.isDirectory()) {
                            unzipFiles(zipInputStream, filePath);
                        } else {
                            Files.createDirectories(filePath);
                        }

                        zipInputStream.closeEntry();
                        entry = zipInputStream.getNextEntry();
                    }
                } catch (IOException e) {
                    System.out.println(Timestamp.from(Instant.now()) + " - An error occurred while unzipping product zip file in unzip");
                    e.printStackTrace();
                } catch (Exception e) {
                    System.out.println(Timestamp.from(Instant.now()) + " Bad zip entry - An error occurred while unzipping product zip file in unzip");
                    e.printStackTrace();
                }
            } else {
                throw new IOException("File is not safe");
            }
        } catch (IOException ioe) {
            System.out.println(Timestamp.from(Instant.now()) + " - File is not safe");
            ioe.printStackTrace();
        }
        System.out.println(Timestamp.from(Instant.now()) + " Exit from unzip");
    }

    public static void unzipFiles(final ZipInputStream zipInputStream, final Path unzipFilePath) {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(unzipFilePath.toAbsolutePath().toString()))) {
            byte[] bytesIn = new byte[1024];
            int read = 0;
            while ((read = zipInputStream.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - An error occurred while unzipping product zip file in unzipFiles");
            e.printStackTrace();
        }
    }

    public void buildDDRInstall(Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);
        String requestType = SafeStr.codeqlClean(request.getRequestType());

        try {
//            String repoDDRInstallZipFileStr = "d:/Optum/ITL/repository/product_installation/files/BaseInstall/DDR-Installer_6_PROD.zip";
//            String ddrInstallZipFileStr = "d:/Optum/ITL/runtime/" + request.getUserId() + "/" + request.getRequestId() + "/DDR-Installer_6_PROD.zip";

            String repoDDRInstallZipFileStr = "d:/Optum/ITL/repository/product_installation/files/BaseInstall/DDR-Installer.zip";
            String ddrInstallZipFileStr = "d:/Optum/ITL/runtime/" + userID + "/" + requestID + "/DDR-Installer.zip";
            String requestFolder = "d:/Optum/ITL/runtime/" + userID + "/" + requestID;
            String requestFolder2 = "d:\\\\Optum\\\\ITL\\\\runtime\\\\" + userID + "\\\\" + requestID;
            String ddrInstallBatFileStr = "d:/Optum/ITL/runtime/" + userID + "/" + requestID + "/install.bat";
            String ddrInstallPropertiesFileStr = "d:/Optum/ITL/runtime/" + userID + "/" + requestID + "/install.properties";
            String repoDDRInstallRunnerPSFileStr = "D:/app/Tomcat9/webapps/ITL/WEB-INF/classes/powershell/product_services/DDRInstallRunner.ps1";
            String ddrInstallRunnerPSFileStr = "d:/Optum/ITL/runtime/" + userID + "/" + requestID + "/DDRInstallRunner.ps1";
            //Copy ddr install product zip file from repository folder to request folder
            System.out.println(Timestamp.from(Instant.now()) + " " + requestID + " ddrInstallZipFileStr = " + ddrInstallZipFileStr);
            Files.copy(new File(repoDDRInstallZipFileStr).toPath(), new File(ddrInstallZipFileStr).toPath(), StandardCopyOption.REPLACE_EXISTING);
            //Unzip ddr install product zip file
            unzip(ddrInstallZipFileStr, requestFolder);
            //Delete ddr install product zip file
            FileUtils.forceDelete(new File(ddrInstallZipFileStr));
            //Update ddr install install.bat file
            updateDDRInstallBat(request, new File(ddrInstallBatFileStr));
            //Update ddr install install.properties file
            updateDDRInstallProperties(request, new File(ddrInstallPropertiesFileStr), requestFolder2);
            //Zip ddr install product folder
            zip(requestFolder, ddrInstallZipFileStr);
            //Delete ddr install product folder
            FileUtils.forceDelete(new File(requestFolder + "/jre-linux"));
            FileUtils.forceDelete(new File(requestFolder + "/jre-windows"));
            FileUtils.forceDelete(new File(requestFolder + "/libs"));
            FileUtils.forceDelete(new File(requestFolder + "/ddr.zip"));
            FileUtils.forceDelete(new File(requestFolder + "/debug_install.bat"));
            FileUtils.forceDelete(new File(requestFolder + "/debug_install.sh"));
            FileUtils.forceDelete(new File(requestFolder + "/install.bat"));
            FileUtils.forceDelete(new File(requestFolder + "/install.sh"));
            FileUtils.forceDelete(new File(requestFolder + "/install.properties"));
            //Copy ddr install runner powershell file from repository folder to request folder
            Files.copy(new File(repoDDRInstallRunnerPSFileStr).toPath(), new File(ddrInstallRunnerPSFileStr).toPath(), StandardCopyOption.REPLACE_EXISTING);
            //Update ddr install runner.ps1
            updateDDRInstallRunnerPSFile(new File(ddrInstallRunnerPSFileStr), request);
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error in buildDDRInstall");
            e.printStackTrace();
        } catch (Exception ex) {
            System.out.println(Timestamp.from(Instant.now()) + " " + requestID + ex);
        }
    }

    private void updateDDRInstallBat(Request request, File ddrInstallPropertiesFile){
        int requestID = SafeStr.isRequestIdValid(request);

        //Update the ddr install.bat file based on user's request
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Entry to updateDDRInstallBat");
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(ddrInstallPropertiesFile));
            String line;
            ArrayList<String> lines = new ArrayList<String>();
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("ddr.loader.Installer")) {
                    line = line.replace("ddr.loader.Installer", "ddr.loader.Installer noui");
                }
                lines.add(line);
            }
            bufferedReader.close();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(ddrInstallPropertiesFile));
            for (String s : lines) {
                bufferedWriter.write(s);
                bufferedWriter.write("\r\n");
                bufferedWriter.flush();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error occurred while updating install.bats file - " + ddrInstallPropertiesFile.toPath());
            e.printStackTrace();
        }
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Exit from updateDDRInstallBat");
    }

    private void updateDDRInstallProperties(Request request, File ddrInstallPropertiesFileStr, String requestFolder2){
        int requestID = SafeStr.isRequestIdValid(request);
        String requestType = SafeStr.codeqlClean(request.getRequestType());

        //Update the ddr installer install.properties file based on user's request
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Entry to updateDDRInstallProperties");
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(ddrInstallPropertiesFileStr));
            String line;
            ArrayList<String> lines = new ArrayList<String>();
            while ((line = bufferedReader.readLine()) != null) {
                if (requestType.equalsIgnoreCase("ddrBaseInstall") ||
                        requestType.equalsIgnoreCase("ddrUpgradeInstall")  ||
                        requestType.equalsIgnoreCase("ddrKbInstall"))  {

                    if (requestType.equalsIgnoreCase("ddrBaseInstall")) {
                        if (line.contains("#dbinstalltype=INSTALL")) {
                            line = line.replace("#dbinstalltype=INSTALL", "dbinstalltype=INSTALL");
                        }
                        if (line.contains("#installshortcuts=true")) {
                            line = line.replace("#installshortcuts=true", "installshortcuts=true");
                        }
                    }

                    if (requestType.equalsIgnoreCase("ddrKbInstall")) {
                        if (line.contains("#dbinstalltype=KBLOAD")) {
                            line = line.replace("#dbinstalltype=KBLOAD", "dbinstalltype=KBLOAD");
                        }
                    }

                    if (requestType.equalsIgnoreCase("ddrUpgradeInstall")) {
                        if (line.contains("#dbinstalltype=UPGRADE")) {
                            line = line.replace("#dbinstalltype=UPGRADE", "dbinstalltype=UPGRADE");
                        }
                    }

                    if (request.getDb().contains("SQL")) {
                        if (line.contains("#dburl=jdbc:sqlserver://localhost:1433")) {
                            line = line.replace("#dburl=jdbc:sqlserver://localhost:1433", "dburl=jdbc:sqlserver://localhost:1433");
                        }
                        if (line.contains("#dbadminuser=sa")) {
                            line = line.replace("#dbadminuser=sa", "dbadminuser=sa");
                        }
                    } else if (request.getDb().contains("ORA")) {
                        if (line.contains("#dburl=jdbc:oracle:thin:@localhost:1521")) {
                            line = line.replace("#dburl=jdbc:oracle:thin:@localhost:1521", "dburl=jdbc:oracle:thin:@localhost:1521");
                        }
                        if (line.contains("#dbadminuser=SYSTEM")) {
                            line = line.replace("#dbadminuser=SYSTEM", "dbadminuser=SYSTEM");
                        }
                    } else if (request.getDb().contains("POST")) {
                        if (line.contains("#dburl=jdbc:postgresql://ddr_postgres:5432")) {
                            line = line.replace("#dburl=jdbc:postgresql://ddr_postgres:5432", "dburl=jdbc:postgresql://ddr_postgres:5432");
                        }
                        if (line.contains("#dbadminuser=postgres")) {
                            line = line.replace("#dbadminuser=postgres", "dbadminuser=postgres");
                        }
                    }

                    if (line.contains("#datafilename=ICP_DATA")) {
                        line = line.replace("#datafilename=ICP_DATA", "datafilename=ICP_DATA");
                    }

                    if (line.contains("#datafilepath=C:\\\\Optum\\\\Data")) {
                        line = line.replace("#datafilepath=C:\\\\Optum\\\\Data", "datafilepath=D:\\\\icpdata\\\\");
                    }
                    if (line.contains("#datafilesize=1")) {
                        line = line.replace("#datafilesize=1", "datafilesize=1");
                    }
                    if (line.contains("#indexfilename=ICP_INDEX")) {
                        line = line.replace("#indexfilename=ICP_INDEX", "indexfilename=ICP_INDEX");
                    }
                    if (line.contains("#indexfilepath=C:\\\\Optum\\\\Data")) {
                        line = line.replace("#indexfilepath=C:\\\\Optum\\\\Data", "indexfilepath=D:\\\\icpdata\\\\");
                    }
                    if (line.contains("#indexfilesize=1")) {
                        line = line.replace("#indexfilesize=1", "indexfilesize=1");
                    }
                    if (line.contains("#logfilename=ICP_LOG")) {
                        line = line.replace("#logfilename=ICP_LOG", "logfilename=ICP_LOG");
                    }
                    if (line.contains("#logfilepath=C:\\\\Optum\\\\Data")) {
                        line = line.replace("#logfilepath=C:\\\\Optum\\\\Data", "logfilepath=D:\\\\icpdata\\\\");
                    }
                }

                if (line.contains("#dbname=icp")) {
                    line = line.replace("#dbname=icp", "dbname=icp");
                }
                if (line.contains("#dbadmincreds=")) {
                    line = line.replace("#dbadmincreds=", "dbadmincreds=Optum123");
                }
                if (line.contains("#dbapplicationuser=icp_p")) {
                    line = line.replace("#dbapplicationuser=icp_p", "dbapplicationuser=icp_p");
                }
                if (line.contains("#dbapplicationcreds=")) {
                    line = line.replace("#dbapplicationcreds=", "dbapplicationcreds=Optum123");
                }
                if (line.contains("#kbfile=.\\\\KB_Extracts\\\\CES_KB_2023_Q4A_5.0-6.0.zip")) {
                    line = line.replace("#kbfile=.\\\\KB_Extracts\\\\CES_KB_2023_Q4A_5.0-6.0.zip", "kbfile=" + requestFolder2 + "\\\\" + request.getKb());
                }
                if (line.contains("#installdirectory=C:\\\\Optum\\\\DDR")) {
                    line = line.replace("#installdirectory=C:\\\\Optum\\\\DDR", "installdirectory=C:\\\\Optum\\\\DDR");
                }
                if (line.contains("#modules=analyzer,broker,connector,datamanager,ui")) {
                    line = line.replace("#modules=analyzer,broker,connector,datamanager,ui", "modules=allinone");
                }
                if (line.contains("#dbsid=ICP")) {
                    line = line.replace("#dbsid=ICP", "dbsid=ICP");
                }
                lines.add(line);
            }
            bufferedReader.close();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(ddrInstallPropertiesFileStr));
            for (String s : lines) {
                bufferedWriter.write(s);
                bufferedWriter.write("\n");
                bufferedWriter.flush();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error occurred while updating install.properties file - " + ddrInstallPropertiesFileStr.toPath());
            e.printStackTrace();
        }
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Exit from updateDDRInstallProperties");
    }

    private void updateDDRInstallRunnerPSFile(File ddrInstallRunnerPSFileStr, Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);
        String requestType = SafeStr.codeqlClean(request.getRequestType());
        String userGroup = SafeStr.codeqlClean(request.getUserGroup());

        //Update the DDR Install Runner Powershell file based on user's request
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Entry to updateDDRnstallRunnerPSFile - ddrInstallPSFile = " + ddrInstallRunnerPSFileStr);
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(ddrInstallRunnerPSFileStr));
            String line;
            ArrayList<String> lines = new ArrayList<String>();
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("${vmName}")) {
                    line = line.replace("${vmName}", "'" + request.getVmName() + "'");
                }
                if (line.contains("${userGroup}")) {
                    if (userGroup.contains("QA")) {
                        line = line.replace("${userGroup}", "'qa'");
                    } else {
                        line = line.replace("${userGroup}", "'" + userGroup + "'");
                    }
                }
                if (requestType.equalsIgnoreCase("ddrBaseInstall")) {
                    if (line.contains("${vmService}")) {
                        if (request.getDb().contains("SQL")) {
                            line = line.replace("${vmService}", "'MSSQLSERVER'");
                        } else if (request.getDb().contains("ORA")) {
                            line = line.replace("${vmService}", "'OracleOra'");
                        } else if (request.getDb().contains("POST")) {
                            line = line.replace("${vmService}", "'postgresql'");
                        }
                    }
                } else if (requestType.equalsIgnoreCase("ddrUpgradeInstall")) {
                    if (line.contains("${vmService}")) {
                        line = line.replace("${vmService}", "'ICP_Service'");
                    }
                }
                else if (requestType.equalsIgnoreCase("ddrKbInstall")) {
                    if (line.contains("${vmService}")) {
                        //line = line.replace("${vmService}", "'DDR_Analyzer'");
                        line = line.replace("${vmService}", "'DDR_AllInOne'");
                    }
                }
                if (line.contains("${adminUserName}")) {
                    line = line.replace("${adminUserName}", "'" + ConfigLoader.getProperty("AdminUser") + "'");
                }
                if (line.contains("${adminPassword}")) {
                    line = line.replace("${adminPassword}", "'" + ConfigLoader.getProperty("ICPCreds") + "'");
                }
                if (line.contains("${RequestType}")) {
                    line = line.replace("${RequestType}", "'" + requestType + "'");
                }
                if (line.contains("${RepoDDRInstallFilePath}")) {
                    line = line.replace("${RepoDDRInstallFilePath}", "'d:\\Optum\\ITL\\repository\\product_installation\\files\\'");
                }
                if (line.contains("${DDRInstallFilePath}")) {
                    line = line.replace("${DDRInstallFilePath}", "'d:\\Optum\\ITL\\runtime\\" +  userID + "\\" + requestID + "\\'");
                }
                if (line.contains("${DDRInstallPropertiesFilePath}")) {
                    String ddrInstallPropertiesFilePathStr = "d:/Optum/ITL/runtime/" + userID + "/" + requestID;
                    line = line.replace("${DDRInstallPropertiesFilePath}", "'" + ddrInstallPropertiesFilePathStr + "'");
                }
                if (line.contains("${DDRInstallFileName}")) {
                    line = line.replace("${DDRInstallFileName}", "'" + "DDR-Installer.zip" + "'");
                    //line = line.replace("${DDRInstallFileName}", "'" + "DDR-Installer_6_PROD.zip" + "'");
                }
                if (line.contains("${RepoKBFilePath}")) {
                    line = line.replace("${RepoKBFilePath}", "'d:\\Optum\\ITL\\repository\\product_installation\\files\\KB\\'");
                }
                if (line.contains("${KBFileName}")) {
                    line = line.replace("${KBFileName}", "'" + request.getKb() + "'");
                }
                if (line.contains("${DDRInstallBatchJob}")) {
                    line = line.replace("${DDRInstallBatchJob}", "'install.bat'");
                }
                if (line.contains("${vmLoggingFolderPath}")) {
                    line = line.replace("${vmLoggingFolderPath}", "'d:\\Optum\\ITL\\runtime\\" +  userID + "\\" + requestID + "\\'");
                }
                if (line.contains("${vmLogName}")) {
                    line = line.replace("${vmLogName}", "'" + requestType + ".log'");
                }
                lines.add(line);
            }
            bufferedReader.close();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(ddrInstallRunnerPSFileStr));
            for (String s : lines) {
                bufferedWriter.write(s);
                bufferedWriter.write("\n");
                bufferedWriter.flush();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error occurred while updating DDR Install Runner Powershell file - " + ddrInstallRunnerPSFileStr.toPath());
            e.printStackTrace();
        }
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Exit from updateDDRInstallRunnerPSFile");
    }

    public void deleteBaseInstallZip(Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);
        try {
            String baseInstallZipFileStr = "d:/Optum/ITL/runtime/" + userID + "/" + requestID + "/" + request.getProductInstall();

            //Delete base install product folder
            if (SafeStr.codeqlClean(baseInstallZipFileStr) != null) {
                FileUtils.forceDelete(new File(baseInstallZipFileStr));
            } else {
                throw new IOException("File is not safe");
            }
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error in deleteBaseInstallZip");
            e.printStackTrace();
        } catch (Exception ex) {
            System.out.println(Timestamp.from(Instant.now()) + " " + requestID + ex);
        }
    }
    public void buildBaseInstall(Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);

        try {
            String repoBaseInstallZipFileStr = "d:/Optum/ITL/repository/product_installation/files/BaseInstall/" + request.getProductInstall();
            String baseInstallZipFileStr = "d:/Optum/ITL/runtime/" + userID + "/" + requestID + "/" + request.getProductInstall();
            String requestFolder = "d:/Optum/ITL/runtime/" + userID + "/" + requestID;
            String zipFileStr = request.getProductInstall();
            String zipMinusZip = zipFileStr.replaceAll(".zip", "/");
            String zipToDisk = zipFileStr.replaceAll("Base_Install.zip", "Disk1");
            String zipToDisk2 = zipToDisk.replaceAll("4_S", "4\\+S");
            String baseInstallPropertiesFileStr = "d:/Optum/ITL/runtime/" + userID + "/" + requestID + "/" + zipMinusZip + zipToDisk2 + "/install.properties";
            String repoBaseInstallRunnerPSFileStr = "D:/app/Tomcat9/webapps/ITL/WEB-INF/classes/powershell/product_services/BaseInstallRunner.ps1";
            String baseInstallRunnerPSFileStr = "d:/Optum/ITL/runtime/" + userID + "/" + requestID + "/BaseInstallRunner.ps1";
            //Copy base install product zip file from repository folder to request folder
            if (SafeStr.codeqlClean(repoBaseInstallZipFileStr) != null && SafeStr.codeqlClean(baseInstallZipFileStr) != null) {
                try {
                    Files.copy(new File(repoBaseInstallZipFileStr).toPath(), new File(baseInstallZipFileStr).toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    // Print the line number where exception occurred
                    e.printStackTrace();
                }
            } else {
                throw new IOException("File is not safe");
            }
            //Unzip base install product zip file
            unzip(baseInstallZipFileStr, requestFolder);
            //Delete base install product zip file
            if (SafeStr.codeqlClean(baseInstallZipFileStr) != null) {
                FileUtils.forceDelete(new File(baseInstallZipFileStr));
            } else {
                throw new IOException("File is not safe");
            }
            //Update base install install.properties file
            updateBaseInstallProperties(request, new File(baseInstallPropertiesFileStr));
            //Zip base install product folder
            zip(requestFolder, baseInstallZipFileStr);

            //Copy baseInstall runner powershell file from repository folder to request folder
            Files.copy(new File(repoBaseInstallRunnerPSFileStr).toPath(), new File(baseInstallRunnerPSFileStr).toPath(), StandardCopyOption.REPLACE_EXISTING);
            //Update BaseInstallRunner.ps1
            updateBaseInstallRunnerPSFile(new File(baseInstallRunnerPSFileStr), request);

        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error in buildBaseInstall");
            e.printStackTrace();
        } catch (Exception ex) {
            System.out.println(Timestamp.from(Instant.now()) + " " + requestID + ex);
        }
    }

    private void updateBaseInstallProperties(Request request, File baseInstallPropertiesFile){
        int requestID = SafeStr.isRequestIdValid(request);

        //Update the product install.properties file based on user's request
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Entry to updateInstallProperties");
        try {
            String line;
            ArrayList<String> lines = null;
            if (SafeStr.codeqlClean(String.valueOf(baseInstallPropertiesFile)) != null) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(baseInstallPropertiesFile));
                lines = new ArrayList<String>();
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("#INSTALLER_UI=silent")) {
                        line = line.replace("#INSTALLER_UI=silent", "INSTALLER_UI=silent");
                    }
                    if (line.contains("#USER_INSTALL_DIR=C:\\\\Optum\\\\ICP")) {
                        line = line.replace("#USER_INSTALL_DIR=C:\\\\Optum\\\\ICP", "USER_INSTALL_DIR=C:\\\\Optum\\\\ICP");
                    }
                    if (line.contains("#DESKTOP_SHORTCUTS=true")) {
                        line = line.replace("#DESKTOP_SHORTCUTS=true", "DESKTOP_SHORTCUTS=true");
                    }
                    if (line.contains("#DB_SELECTION=")) {
                        if (request.getDb().equalsIgnoreCase("SQL")) {
                            line = line.replace("#DB_SELECTION=", "DB_SELECTION=sqlserver");
                        } else if (request.getDb().equalsIgnoreCase("ORA")) {
                            line = line.replace("#DB_SELECTION=", "DB_SELECTION=oracle");
                        }
                    }
                    if (line.contains("#DB_CON_HOST=")) {
                        line = line.replace("#DB_CON_HOST=", "DB_CON_HOST=localhost");
                    }
                    if (line.contains("#DB_CON_SID=")) {
                        line = line.replace("#DB_CON_SID=", "DB_CON_SID=ICP");
                    }
                    if (line.contains("#DB_CON_PORT=")) {
                        if (request.getDb().equalsIgnoreCase("SQL")) {
                            line = line.replace("#DB_CON_PORT=", "DB_CON_PORT=1433");
                        } else if (request.getDb().equalsIgnoreCase("ORA")) {
                            line = line.replace("#DB_CON_PORT=", "DB_CON_PORT=1521");
                        }
                    }
                    if (line.contains("#USE_INSTANCE_NAME=true/false")) {
                        line = line.replace("#USE_INSTANCE_NAME=true/false", "USE_INSTANCE_NAME=true");
                    }
                    if (line.contains("#DB_CON_INSTANCE=")) {
                        if (request.getDb().equalsIgnoreCase("SQL")) {
                            line = line.replace("#DB_CON_INSTANCE=", "DB_CON_INSTANCE=1433");
                        }
                    }
                    if (line.contains("#DB_TS_PATH=")) {
                        line = line.replace("#DB_TS_PATH=", "DB_TS_PATH=D:\\\\ICPdata");
                    }
                    if (line.contains("#DB_TS_SIZE=")) {
                        line = line.replace("#DB_TS_SIZE=", "DB_TS_SIZE=5240");
                    }
                    if (line.contains("#DB_ADMIN_USER=")) {
                        if (request.getDb().equalsIgnoreCase("SQL")) {
                            line = line.replace("#DB_ADMIN_USER=", "DB_ADMIN_USER=sa");
                        } else if (request.getDb().equalsIgnoreCase("ORA")) {
                            line = line.replace("#DB_ADMIN_USER=", "DB_ADMIN_USER=SYSTEM");
                        }
                    }
                    if (line.contains("#DB_SYS_PWD=")) {
                        line = line.replace("#DB_SYS_PWD=", "DB_SYS_PWD=Optum123");
                    }
                    if (line.contains("#SILENT_SYSDBA_USER=")) {
                        line = line.replace("#SILENT_SYSDBA_USER=", "SILENT_SYSDBA_USER=SYS");
                    }
                    if (line.contains("#SILENT_SYSDBA_PWD=")) {
                        line = line.replace("#SILENT_SYSDBA_PWD=", "SILENT_SYSDBA_PWD=Optum123");
                    }
                    if (line.contains("#DB_CON_PWD=")) {
                        line = line.replace("#DB_CON_PWD=", "DB_CON_PWD=Optum123");
                    }
                    lines.add(line);
                }
                bufferedReader.close();
            } else {
                throw new IOException("File is not safe");
            }

            if (SafeStr.codeqlClean(String.valueOf(baseInstallPropertiesFile)) != null) {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(baseInstallPropertiesFile));
                for (String s : lines) {
                    bufferedWriter.write(s);
                    bufferedWriter.write("\n");
                    bufferedWriter.flush();
                }
                bufferedWriter.close();
            } else {
                throw new IOException("File is not safe");
            }
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error occurred while updating install.properties file - " + baseInstallPropertiesFile.toPath());
            e.printStackTrace();
        }
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Exit from updateInstallProperties");
    }

    private void updateBaseInstallRunnerPSFile(File baseInstallRunnerPSFile, Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);
        String requestType = SafeStr.codeqlClean(request.getRequestType());
        String userGroup = SafeStr.codeqlClean(request.getUserGroup());

        //Update the BaseInstall Runner Powershell file based on user's request
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Entry to updateBaseInstallRunnerPSFile - baseInstallPSFile = " + baseInstallRunnerPSFile);
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(baseInstallRunnerPSFile));
            String line;
            ArrayList<String> lines = new ArrayList<String>();
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("${vmName}")) {
                    line = line.replace("${vmName}", "'" + request.getVmName() + "'");
                }
                if (line.contains("${userGroup}")) {
                    if (userGroup.contains("QA")) {
                        line = line.replace("${userGroup}", "'qa'");
                    } else {
                        line = line.replace("${userGroup}", "'" + userGroup + "'");
                    }
                }
                if (line.contains("${vmService}")) {
                    if (request.getDb().contains("SQL")) {
                        line = line.replace("${vmService}", "'MSSQLSERVER'");
                    } else if (request.getDb().contains("ORA")) {
                        line = line.replace("${vmService}", "'OracleOra'");
                    }
                }
                if (line.contains("${adminUserName}")) {
                    line = line.replace("${adminUserName}", "'" + ConfigLoader.getProperty("AdminUser") + "'");
                }
                if (line.contains("${adminPassword}")) {
                    line = line.replace("${adminPassword}", "'" + ConfigLoader.getProperty("ICPCreds") + "'");
                }
                if (line.contains("${RepoBaseInstallFilePath}")) {
                    line = line.replace("${RepoBaseInstallFilePath}", "'d:\\Optum\\ITL\\repository\\product_installation\\files\\'");
                }
                if (line.contains("${BaseInstallFilePath}")) {
                    line = line.replace("${BaseInstallFilePath}", "'d:\\Optum\\ITL\\runtime\\" +  userID + "\\" + requestID + "\\'");
                }
                if (line.contains("${BaseInstallPropertiesFilePath}")) {
                    String zipFileStr = request.getProductInstall();
                    String zipMinusZip = zipFileStr.replaceAll(".zip", "/");
                    String zipToDisk = zipFileStr.replaceAll("Base_Install.zip", "Disk1");
                    String zipToDisk2 = zipToDisk.replaceAll("4_S", "4\\+S");
                    String baseInstallPropertiesFilePathStr = "d:/Optum/ITL/runtime/" + userID + "/" + requestID + "/" + zipMinusZip + zipToDisk2;
                    line = line.replace("${BaseInstallPropertiesFilePath}", "'" + baseInstallPropertiesFilePathStr + "'");
                }
                if (line.contains("${BaseInstallFileName}")) {
                    line = line.replace("${BaseInstallFileName}", "'" + request.getProductInstall() + "'");
                }
                if (line.contains("${BaseInstallBatchJob}")) {
                    line = line.replace("${BaseInstallBatchJob}", "'setupicp.bat -f'");
                }
                if (line.contains("${vmLoggingFolderPath}")) {
                    line = line.replace("${vmLoggingFolderPath}", "'d:\\Optum\\ITL\\runtime\\" +  userID + "\\" + requestID + "\\'");
                }
                if (line.contains("${vmLogName}")) {
                    line = line.replace("${vmLogName}", "'" + requestType + ".log'");
                }
                lines.add(line);
            }
            bufferedReader.close();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(baseInstallRunnerPSFile));
            for (String s : lines) {
                bufferedWriter.write(s);
                bufferedWriter.write("\n");
                bufferedWriter.flush();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error occurred while updating BaseInstall Runner Powershell file - " + baseInstallRunnerPSFile.toPath());
            e.printStackTrace();
        }
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Exit from updateBaseInstallRunnerPSFile");
    }

    public void buildCUInstall(Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);

        try {
            String repoCUInstallZipFileStr = "d:/Optum/ITL/repository/product_installation/files/CUInstall/" + request.getProductUpgrade();
            String cuInstallZipFileStr = "d:/Optum/ITL/runtime/" + userID + "/" + requestID + "/" + request.getProductUpgrade();
            String requestFolder = "d:/Optum/ITL/runtime/" + userID + "/" + requestID;
            String zipFileStr = request.getProductUpgrade();
            String zipMinusZip = "";
            System.out.println(Timestamp.from(Instant.now()) + " " + requestID + " cuInstallZipFileStr = " + cuInstallZipFileStr);

            if (zipFileStr.length() > 1000) {
                throw new IllegalArgumentException(zipFileStr + " Input is too long");
            } else {
                if (zipFileStr.contains("_Installer_")) {
                    zipMinusZip = zipFileStr.replaceAll("(\\_I.*p)", "");
                    System.out.println(Timestamp.from(Instant.now()) + " " + requestID + " - with _Installer_ - zipMinusZip = " + zipMinusZip);
                } else {
                    zipMinusZip = zipFileStr.replaceAll(".zip", "");
                    System.out.println(Timestamp.from(Instant.now()) + " " + requestID + " - without _Installer_ - zipMinusZip = " + zipMinusZip);
                }
            }
            String cuInstallPropertiesFileStr = "d:/Optum/ITL/runtime/" + userID + "/" + requestID + "/" + zipMinusZip + "/install.properties";
            System.out.println(Timestamp.from(Instant.now()) + " " + requestID + " cuInstallPropertiesFileStr = " + cuInstallPropertiesFileStr);
            String repoCUInstallRunnerPSFileStr = "D:/app/Tomcat9/webapps/ITL/WEB-INF/classes/powershell/product_services/CUInstallRunner.ps1";
            String cuInstallRunnerPSFileStr = "d:/Optum/ITL/runtime/" + userID + "/" + requestID + "/CUInstallRunner.ps1";
            //Copy cu install product zip file from repository folder to request folder
            if (SafeStr.codeqlClean(repoCUInstallZipFileStr) != null && SafeStr.codeqlClean(cuInstallZipFileStr) != null) {
                Files.copy(new File(repoCUInstallZipFileStr).toPath(), new File(cuInstallZipFileStr).toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                throw new IOException("File is not safe");
            }
            //Unzip cu install product zip file
            unzip(cuInstallZipFileStr, requestFolder);
            //Delete cu install product zip file
            if (SafeStr.codeqlClean(cuInstallZipFileStr) != null) {
                FileUtils.forceDelete(new File(cuInstallZipFileStr));
            } else {
                throw new IOException("File is not safe");
            }
            //Update cu install install.properties file
            updateCUInstallProperties(request, new File(cuInstallPropertiesFileStr));
            //Zip cu install product folder
            zip(requestFolder, cuInstallZipFileStr);
//            //Delete cu install product folder
            if (SafeStr.codeqlClean(requestFolder + "/" + zipMinusZip) != null) {
                FileUtils.forceDelete(new File(requestFolder + "/" + zipMinusZip));
            } else {
                throw new IOException("File is not safe");
            }
            //Copy cuInstall Runner powershell file from repository folder to request folder
            Files.copy(new File(repoCUInstallRunnerPSFileStr).toPath(), new File(cuInstallRunnerPSFileStr).toPath(), StandardCopyOption.REPLACE_EXISTING);
            //Update CUInstallRunner.ps1
            updateCUInstallRunnerPSFile(new File(cuInstallRunnerPSFileStr), request, zipMinusZip);
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error in buildCUInstall");
            e.printStackTrace();
        } catch (Exception ex) {
            System.out.println(Timestamp.from(Instant.now()) + " " + requestID + ex);
        }
    }

    private void updateCUInstallProperties(Request request, File cuInstallPropertiesFile){
        int requestID = 0;

        try {
            requestID = SafeStr.isRequestIdValid(request);
            //Update the product install.properties file based on user's request
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Entry to updateCUInstallProperties");

            if (SafeStr.codeqlClean(String.valueOf(cuInstallPropertiesFile)) != null) {
                String content = Files.readString(cuInstallPropertiesFile.toPath(), StandardCharsets.UTF_8);

                BufferedReader bufferedReader = new BufferedReader(new FileReader(cuInstallPropertiesFile));
                String line;
                ArrayList<String> lines = new ArrayList<String>();
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("#INSTALLER_UI=silent")) {
                        line = line.replace("#INSTALLER_UI=silent", "INSTALLER_UI=silent");
                    }
                    if (line.contains("#SILENT_INSTALL_DIR=C:\\\\Optum\\\\ICP_CumulativeUpdates")) {
                        line = line.replace("#SILENT_INSTALL_DIR=C:\\\\Optum\\\\ICP_CumulativeUpdates", "SILENT_INSTALL_DIR=C:\\\\Optum\\\\ICP_CumulativeUpdates");
                    }
                    if (line.contains("#SILENT_ICP_INSTALL_DIR=C:\\\\Optum\\\\ICP")) {
                        line = line.replace("#SILENT_ICP_INSTALL_DIR=C:\\\\Optum\\\\ICP", "SILENT_ICP_INSTALL_DIR=C:\\\\Optum\\\\ICP");
                    }
                    if (line.contains("#SILENT_REINSTALL_CU=true")) {
                        line = line.replace("#SILENT_REINSTALL_CU=true", "SILENT_REINSTALL_CU=false");
                    }
                    if (line.contains("#SILENT_UPDATE_DB=true")) {
                        line = line.replace("#SILENT_UPDATE_DB=true", "SILENT_UPDATE_DB=true");
                    }
                    if (line.contains("#SILENT_DELETE_SOURCE_FILES=false")) {
                        line = line.replace("#SILENT_DELETE_SOURCE_FILES=false", "SILENT_DELETE_SOURCE_FILES=false");
                    }
                    lines.add(line);
                }
                bufferedReader.close();

                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(cuInstallPropertiesFile));
                for (String s : lines) {
                    bufferedWriter.write(s);
                    bufferedWriter.write("\n");
                    bufferedWriter.flush();
                }
                bufferedWriter.close();
            } else {
                throw new IOException("File is not safe");
            }
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error occurred while updating install.properties file - " + cuInstallPropertiesFile.toPath());
            e.printStackTrace();
        }
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Exit from updateInstallProperties");
    }

    private void updateCUInstallRunnerPSFile(File cuInstallRunnerPSFile, Request request, String zipMinusZip) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);
        String requestType = SafeStr.codeqlClean(request.getRequestType());
        String userGroup = SafeStr.codeqlClean(request.getUserGroup());

        //Update the cu install runner Powershell file based on user's request
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Entry to updateCUInstallRunnerPSFile - cuInstallRunnerPSFile = " + cuInstallRunnerPSFile);
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(cuInstallRunnerPSFile));
            String line;
            ArrayList<String> lines = new ArrayList<String>();
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("${vmName}")) {
                    line = line.replace("${vmName}", "'" + request.getVmName() + "'");
                }
                if (line.contains("${userGroup}")) {
                    if (userGroup.contains("QA")) {
                        line = line.replace("${userGroup}", "'qa'");
                    } else {
                        line = line.replace("${userGroup}", "'" + userGroup + "'");
                    }
                }
                if (line.contains("${vmService}")) {
                    line = line.replace("${vmService}", "'ICP_Service'");
                }
                if (line.contains("${adminUserName}")) {
                    line = line.replace("${adminUserName}", "'" + ConfigLoader.getProperty("AdminUser") + "'");
                }
                if (line.contains("${adminPassword}")) {
                    line = line.replace("${adminPassword}", "'" + ConfigLoader.getProperty("ICPCreds") + "'");
                }
                if (line.contains("${RepoCUInstallFilePath}")) {
                    line = line.replace("${RepoCUInstallFilePath}", "'d:\\Optum\\ITL\\repository\\product_installation\\files\\'");
                }
                if (line.contains("${CUInstallFilePath}")) {
                    line = line.replace("${CUInstallFilePath}", "'d:\\Optum\\ITL\\runtime\\" +  userID + "\\" + requestID + "\\'");
                }
                if (line.contains("${CUInstallFileName}")) {
                    line = line.replace("${CUInstallFileName}", "'" + request.getProductUpgrade() + "'");
                }
                if (line.contains("${CUInstallBatchJob}")) {
                    line = line.replace("${CUInstallBatchJob}", "'installcu.bat -f'");
                }
                if (line.contains("${CUInstallPropertiesFilePath}")) {
                    line = line.replace("${CUInstallPropertiesFilePath}", "'" + "d:/Optum/ITL/runtime/" + userID + "/" + requestID + "/" + zipMinusZip + "'");
                }
                if (line.contains("${vmLoggingFolderPath}")) {
                    line = line.replace("${vmLoggingFolderPath}", "'d:\\Optum\\ITL\\runtime\\" +  userID + "\\" + requestID + "\\'");
                }
                if (line.contains("${vmLogName}")) {
                    line = line.replace("${vmLogName}", "'" + requestType + ".log'");
                }
                lines.add(line);
            }
            bufferedReader.close();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(cuInstallRunnerPSFile));
            for (String s : lines) {
                bufferedWriter.write(s);
                bufferedWriter.write("\n");
                bufferedWriter.flush();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error occurred while updating CUInstall Runner Powershell file - " + cuInstallRunnerPSFile.toPath());
            e.printStackTrace();
        }
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Exit from updateCUInstallPSFile");
    }

    public void buildKBLoad(Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);

        String repoKBLoadRunnerPSFile = "D:/app/Tomcat9/webapps/ITL/WEB-INF/classes/powershell/product_services/KBLoadRunner.ps1";
        String kbLoadRunnerPSFile = "d:/Optum/ITL/runtime/" + userID + "/" + requestID + "/KBLoadRunner.ps1";
        //Copy KB Load runner powershell file from repository folder to request folder
        try {
            Files.copy(new File(repoKBLoadRunnerPSFile).toPath(), new File(kbLoadRunnerPSFile).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error occurred while updating the KB Load Runner Powershell file - " + kbLoadRunnerPSFile);
            e.printStackTrace();
        }
        //Update KBLoadRunner.ps1
        updateKBLoadRunnerPSFile(new File(kbLoadRunnerPSFile), request);
    }

    private void updateKBLoadRunnerPSFile(File kbLoadRunnerPSFile, Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);
        String requestType = SafeStr.codeqlClean(request.getRequestType());
        String userGroup = SafeStr.codeqlClean(request.getUserGroup());

        //Update the kb load runner Powershell file based on user's request
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Entry to updateKBLoadRunnerPSFile - kbLoadRunnerPSFile = " + kbLoadRunnerPSFile.toPath());
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(kbLoadRunnerPSFile));
            String line;
            ArrayList<String> lines = new ArrayList<String>();
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("${vmName}")) {
                    line = line.replace("${vmName}", "'" + request.getVmName() + "'");
                }
                if (line.contains("${userGroup}")) {
                    if (userGroup.contains("QA")) {
                        line = line.replace("${userGroup}", "'qa'");
                    } else {
                        line = line.replace("${userGroup}", "'" + userGroup + "'");
                    }
                }
                if (line.contains("${vmService}")) {
                    line = line.replace("${vmService}", "'ICP_Service'");
                }
                if (line.contains("${adminUserName}")) {
                    line = line.replace("${adminUserName}", "'" + ConfigLoader.getProperty("AdminUser") + "'");
                }
                if (line.contains("${adminPassword}")) {
                    line = line.replace("${adminPassword}","'" +  ConfigLoader.getProperty("ICPCreds") +"'");
                }
                if (line.contains("${RepoKBFilePath}")) {
                    line = line.replace("${RepoKBFilePath}", "'d:\\Optum\\ITL\\repository\\product_installation\\files\\KB\\'");
                }
                if (line.contains("${KBFilePath}")) {
                    line = line.replace("${KBFilePath}", "'c:\\Optum\\ICP\\KB_Extracts\\'");
                }
                if (line.contains("${KBFileName}")) {
                    line = line.replace("${KBFileName}", "'" + request.getKb() + "'");
                }
                if (line.contains("${KBLoaderBatchJob}")) {
                    line = line.replace("${KBLoaderBatchJob}", "'C:\\Optum\\ICP\\bin\\startSmartLoadKB.bat -f'");
                }
                if (line.contains("${vmLoggingFolderPath}")) {
                    line = line.replace("${vmLoggingFolderPath}", "'d:\\Optum\\ITL\\runtime\\" +  userID + "\\" + requestID + "\\'");
                }
                if (line.contains("${vmLogName}")) {
                    line = line.replace("${vmLogName}", "'" + requestType + ".log'");
                }
                lines.add(line);
            }
            bufferedReader.close();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(kbLoadRunnerPSFile));
            for (String s : lines) {
                bufferedWriter.write(s);
                bufferedWriter.write("\n");
                bufferedWriter.flush();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error occurred while updating KBLoad Runner Powershell file - " + kbLoadRunnerPSFile.toPath());
            e.printStackTrace();
        }
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Exit from updateKBLoadRunnerPSFile");
    }

    public void buildLCDLoad(Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);

        String repoLCDLoadRunnerPSFile = "D:/app/Tomcat9/webapps/ITL/WEB-INF/classes/powershell/product_services/LCDLoadRunner.ps1";
        String lcdLoadRunnerPSFile = "d:/Optum/ITL/runtime/" + userID + "/" + requestID + "/LCDLoadRunner.ps1";
        //Copy LCD Load Runner powershell file from repository folder to request folder
        try {
            Files.copy(new File(repoLCDLoadRunnerPSFile).toPath(), new File(lcdLoadRunnerPSFile).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error occurred while updating LCDLoad Runner Powershell file - " + lcdLoadRunnerPSFile);
            e.printStackTrace();
        }
        //Update LCDLoadRunner.ps1
        updateLCDLoadRunnerPSFile(new File(lcdLoadRunnerPSFile), request);
    }

    private void updateLCDLoadRunnerPSFile(File lcdLoadRunnerPSFile, Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);
        String requestType = SafeStr.codeqlClean(request.getRequestType());
        String userGroup = SafeStr.codeqlClean(request.getUserGroup());

        //Update the lcd load runner Powershell file based on user's request
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Entry to updateLCDLoadRunnerPSFile - lcdLoadRunnerPSFile = " + lcdLoadRunnerPSFile.toPath());
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(lcdLoadRunnerPSFile));
            String line;
            ArrayList<String> lines = new ArrayList<String>();
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("${vmName}")) {
                    line = line.replace("${vmName}", "'" + request.getVmName() + "'");
                }
                if (line.contains("${userGroup}")) {
                    if (userGroup.contains("QA")) {
                        line = line.replace("${userGroup}", "'qa'");
                    } else {
                        line = line.replace("${userGroup}", "'" + userGroup + "'");
                    }
                }
                if (line.contains("${vmService}")) {
                    line = line.replace("${vmService}", "'ICP_Service'");
                }
                if (line.contains("${adminUserName}")) {
                    line = line.replace("${adminUserName}", "'" + ConfigLoader.getProperty("AdminUser") + "'");
                }
                if (line.contains("${adminPassword}")) {
                    line = line.replace("${adminPassword}", "'" + ConfigLoader.getProperty("ICPCreds") + "'");
                }
                if (line.contains("${RepoLCDFilePath}")) {
                    line = line.replace("${RepoLCDFilePath}", "'d:\\Optum\\ITL\\repository\\product_installation\\files\\LCD\\'");
                }
                if (line.contains("${LCDFilePath}")) {
                    line = line.replace("${LCDFilePath}", "'c:\\Optum\\ICP\\LCD_Extracts\\'");
                }
                if (line.contains("${LCDFileName}")) {
                    line = line.replace("${LCDFileName}", "'" + request.getPeLcd() + "'");
                }
                if (line.contains("${LCDLoaderBatchJob}")) {
                    line = line.replace("${LCDLoaderBatchJob}", "'C:\\Optum\\ICP\\bin\\LCDBulkLoader.bat'");
                }
                if (line.contains("${vmLoggingFolderPath}")) {
                    line = line.replace("${vmLoggingFolderPath}", "'d:\\Optum\\ITL\\runtime\\" +  userID + "\\" + requestID + "\\'");
                }
                if (line.contains("${vmLogName}")) {
                    line = line.replace("${vmLogName}", "'" + requestType + ".log'");
                }
                lines.add(line);
            }
            bufferedReader.close();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(lcdLoadRunnerPSFile));
            for (String s : lines) {
                bufferedWriter.write(s);
                bufferedWriter.write("\n");
                bufferedWriter.flush();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error occurred while updating LCDLoad Runner Powershell file - " + lcdLoadRunnerPSFile.toPath());
            e.printStackTrace();
        }
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Exit from updateLCDLoadRunnerPSFile");
    }

    public void buildSQLServerVMNameUpdate(Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);

        String repoSQLServerVMNameUpdatePSFile = "D:/app/Tomcat9/webapps/ITL/WEB-INF/classes/powershell/vm_services/SQLServerVMNameUpdateRunner.ps1";
        String SQLServerVMNameUpdateRunnerPSFile = "d:/Optum/ITL/runtime/" + userID + "/" + requestID + "/SQLServerVMNameUpdateRunner.ps1";
        //Copy SQL Server VM Name Update Runner powershell file from repository folder to request folder
        try {
            Files.copy(new File(repoSQLServerVMNameUpdatePSFile).toPath(), new File(SQLServerVMNameUpdateRunnerPSFile).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error occurred while copying the SQL Server VM Name Update Runner Powershell file - " + SQLServerVMNameUpdateRunnerPSFile);
            e.printStackTrace();
        }
        //Update SQLServerVMNameUpdateRunner.ps1
        updateSQLServerVMNameUpdateRunnerPSFile(new File(SQLServerVMNameUpdateRunnerPSFile), request);
    }

    private void updateSQLServerVMNameUpdateRunnerPSFile(File SQLServerVMNameUpdateRunnerPSFile, Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);
        String requestType = SafeStr.codeqlClean(request.getRequestType());
        String userGroup = SafeStr.codeqlClean(request.getUserGroup());

        //Update the update SQL Server VM Name runner Powershell file based on user's request
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Entry to updateSQLServerVMNameUpdateRunnerPSFile - SQLServerVMNameUpdateRunnerPSFile = " + SQLServerVMNameUpdateRunnerPSFile.toPath());
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(SQLServerVMNameUpdateRunnerPSFile));
            String line;
            ArrayList<String> lines = new ArrayList<String>();
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("${vmName}")) {
                    line = line.replace("${vmName}", "'" + request.getVmName() + "'");
                }
                if (line.contains("${userGroup}")) {
                    line = line.replace("${userGroup}", "'" + userGroup + "'");
                }
                if (line.contains("${vmService}")) {
                    line = line.replace("${vmService}", "'MSSQLSERVER'");
                }
                if (line.contains("${vmLoggingFolderPath}")) {
                    line = line.replace("${vmLoggingFolderPath}", "'d:\\Optum\\ITL\\runtime\\" +  userID + "\\" + requestID + "\\'");
                }
                if (line.contains("${vmLogName}")) {
                    line = line.replace("${vmLogName}", "'" + requestType + ".log'");
                }
                lines.add(line);
            }
            bufferedReader.close();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(SQLServerVMNameUpdateRunnerPSFile));
            for (String s : lines) {
                bufferedWriter.write(s);
                bufferedWriter.write("\n");
                bufferedWriter.flush();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error occurred while updating LCDLoad Runner Powershell file - " + SQLServerVMNameUpdateRunnerPSFile.toPath());
            e.printStackTrace();
        }
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Exit from updateSQLServerVMNameUpdateRunnerPSFile");
    }

    public void buildTestVMConnection(Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);

        String repoVMTestConnectionRunnerPSFile = "D:/app/Tomcat9/webapps/ITL/WEB-INF/classes/powershell/vm_services/vmTestConnectionRunner.ps1";
        String vmTestConnectionRunnerPSFile = "d:/Optum/ITL/runtime/" + userID + "/" + requestID + "/vmTestConnectionRunner.ps1";
        //Copy SVM Test Connection Runner powershell file from repository folder to request folder
        try {
            Files.copy(new File(repoVMTestConnectionRunnerPSFile).toPath(), new File(vmTestConnectionRunnerPSFile).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error occurred while copying the VM Test Connection Runner Powershell file - " + vmTestConnectionRunnerPSFile);
            e.printStackTrace();
        }
        //Update vmTestConnectionRunner.ps1
        updateVMTestConnectionRunnerPSFile(new File(vmTestConnectionRunnerPSFile), request);
    }

    private void updateVMTestConnectionRunnerPSFile(File vmTestConnectionRunnerPSFile, Request request) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);
        String requestType = SafeStr.codeqlClean(request.getRequestType());

        //Update the VM Test Conenction Runner Powershell file based on user's request
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Entry to updateVMTestConnectionRunnerRunnerPSFile - SQLServerVMNameUpdateRunnerPSFile = " + vmTestConnectionRunnerPSFile.toPath());
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(vmTestConnectionRunnerPSFile));
            String line;
            ArrayList<String> lines = new ArrayList<String>();
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("${vmName}")) {
                    line = line.replace("${vmName}", "'" + request.getVmName() + "'");
                }
                if (line.contains("${adminUserName}")) {
                    line = line.replace("${adminUserName}", "'" + ConfigLoader.getProperty("AdminUser") + "'");
                }
                if (line.contains("${adminPassword}")) {
                    line = line.replace("${adminPassword}", "'" + ConfigLoader.getProperty("ICPCreds") + "'");
                }
                if (line.contains("${vmLoggingFolderPath}")) {
                    line = line.replace("${vmLoggingFolderPath}", "'d:\\Optum\\ITL\\runtime\\" +  userID + "\\" + requestID + "\\'");
                }
                if (line.contains("${vmLogName}")) {
                    line = line.replace("${vmLogName}", "'" + requestType + ".log'");
                }
                lines.add(line);
            }
            bufferedReader.close();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(vmTestConnectionRunnerPSFile));
            for (String s : lines) {
                bufferedWriter.write(s);
                bufferedWriter.write("\n");
                bufferedWriter.flush();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " An error occurred while updating VM Test Connection Runner Powershell file - " + vmTestConnectionRunnerPSFile.toPath());
            e.printStackTrace();
        }
        System.out.println(Timestamp.from(Instant.now()) + " - " + requestID + " Exit from updateVMTestConnectionRunnerRunnerPSFile");
    }
}
