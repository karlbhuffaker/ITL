package com.optum.itl;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Testing {

    public static void main(String[] args) {
        String hostname;

        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();

            String hostname2 = System.getenv("COMPUTERNAME");
            //String hostname2 = env.get("COMPUTERNAME");
            System.out.println("hostname = " + hostname + " hostname2 = " + hostname2 + " ip = " + addr);
        } catch (UnknownHostException ex) {
            System.out.println("Hostname can not be resolved");


        }
    }
}
