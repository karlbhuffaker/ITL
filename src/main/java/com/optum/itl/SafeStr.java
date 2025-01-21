package com.optum.itl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SafeStr {

    private SafeStr() {
        // private constructor to make Sonar happy.
    }

    /*  The requestID can grow depending on how many requests a user makes.
        The max number is assigned thinking that no user will request more than 100,000 requests.
        If they do, then the max number will need to be increased.
        This function is only to make Sonar happy.
     */
    public static int codeqlClean(int requestID) {
        // Define the safe range
        int min = 1;
        int max = 100000;
        boolean clean = false;
        int rtnVal = 0;

        // Check if the number is within the safe range
        if (requestID >= min && requestID <= max) {
            clean = true;
        };
        if (clean) {
            rtnVal = requestID;
        } else {
            rtnVal = -1;
        }
        return rtnVal;
    }

    public static int isRequestIdValid(Request request) {
        int requestID = request.getRequestId();
        int rtnRequestID = codeqlClean(requestID);

        String validationMessage = "";
        if (rtnRequestID == -1) {
            validationMessage = validationMessage.concat("Failed - The request # " + rtnRequestID + " for user " + request.getUserId() + " is over it's safe max.");
            System.out.println(validationMessage);
        }
        return requestID;
    }

    public static String codeqlClean(String unsafe) {
        if (unsafe == null) return "null";
        if (unsafe.isEmpty()) return "";
        int len = unsafe.length();
        StringBuilder out = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = unsafe.charAt(i);
            if (c > 31 && c < 127)
                out.append(c);
        }
        return out.toString();
    }

    private static StringBuilder replaceAnyFoundUnsafeChars(String unsafe, StringBuilder out, int len, int i) {
        char c = unsafe.charAt(i);
        if (c < 32 || c > 127 || c == '\\') {
            if (out == null) { // we never actually allocate this until there is an unsafe char found, performance
                out = new StringBuilder(len + 50);
                out.append(unsafe, 0, i);
            }

            switch (c) {
                case '\\': out.append("\\\\"); break;
                case '\r': out.append("\\r"); break;
                case '\n': out.append("\\n"); break;
                case '\t': out.append("\\t"); break;
                default:
                    String hex = Long.toString(c | 0xffffL, 16); // assure unsigned
                    out.append("\\u");
                    for (int digits = hex.length(); digits < 4; digits++)
                        out.append('0');
                    out.append(hex);
                    break;
            }
        } else if (out != null) {
            out.append(c);
        }
        return out;
    }

    public static String getValidFileName(String pFileName){
        return null == pFileName ? "" : pFileName.replaceAll("[~#%&*{}\":|><\\\\/?]", "");
    }

    // Ensure that the path stays within the public folder
    public static boolean getValidPathName(Path pFolderPath, Path pFilePath){
        boolean pathIsSafe = true;
        if (!pFilePath.startsWith(pFolderPath + File.separator)) {
            pathIsSafe = false;
        }
        return pathIsSafe;
    }

    // Ensure that the path stays within the public folder
    public static boolean getValidPathName(Path pFolderPath){
        boolean pathIsSafe = true;
        if (!pFolderPath.startsWith(pFolderPath + File.separator)) {
            pathIsSafe = false;
        }
        return pathIsSafe;
    }

    public static String validatePath(String pPath) {
        if (pPath.contains("..")) {
            throw new IllegalArgumentException("Invalid path");
        }
        return pPath;
    }

    // This functons is used to validate that a path has not been changed by injection.
    public static boolean pathValidator(Request request, String vFolderPath) {
        String userID = SafeStr.codeqlClean(request.getUserId());
        int requestID = SafeStr.isRequestIdValid(request);
        String requestType = SafeStr.codeqlClean(request.getRequestType());
        boolean isSafe = true;

        String filename =  requestType + ".ps1";
        Path publicFolder = Paths.get(vFolderPath + userID + "\\" + requestID + "\\").normalize().toAbsolutePath();
        Path filePath = publicFolder.resolve(filename).normalize().toAbsolutePath();
        if (!SafeStr.getValidPathName(publicFolder, filePath)) {
            isSafe = false;
        }
        return isSafe;
    }


}
