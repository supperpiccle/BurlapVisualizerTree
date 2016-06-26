/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burlapcontroller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 *
 * @author stefano
 */
public class SnortAlertParser {

    BufferedReader br = null;
    private double pScan, pVsftpd, pSmbd, pPhpcgi, pIrcd, pDistccd, pRmi;
    private String sourceIP;

    private final static String PORTSCAN = "122:1";
    private final static String VSFTPD = "1:80000000";
    private final static String SMBD = "1:80000001";
    private final static String IRCD = "1:80000002";
    private final static String DISTCCD = "1:80000003";
    private final static String RMI = "1:80000004";
    private final static String PHPCGI = "1:22063";

    public SnortAlertParser(String alertPath) throws FileNotFoundException, IOException {
        File f = new File(alertPath);
        br = new BufferedReader(new FileReader(f));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains(PORTSCAN)) {
                pScan = 1;
                sourceIP = getIp();
            }
            else if (line.contains(VSFTPD)) {
                pVsftpd = 1;
                sourceIP = getIp();
            }
            else if (line.contains(SMBD)) {
                pSmbd = 1;
                sourceIP = getIp();
            }
            else if (line.contains(IRCD)) {
                pIrcd = 1;
                sourceIP = getIp();
            }
            else if (line.contains(DISTCCD)) {
                pDistccd = 1;
                sourceIP = getIp();
            }
            else if (line.contains(RMI)) {
                pRmi = 1;
                sourceIP = getIp();
            }
            else if (line.contains(PHPCGI)) {
                pPhpcgi = 1;
                sourceIP = getIp();
            }
        }
        br.close();
        FileOutputStream fos = new FileOutputStream(f);
        fos.getChannel().truncate(0);
        fos.close();
    }

    private String getIp() throws IOException {
        br.readLine();
        String line = br.readLine();
        String[] words = line.split(" ");
        String ip = words[1].split(":")[0];
        return ip;
    }

    public double getpScan() {
        return pScan;
    }

    public double getpVsftpd() {
        return pVsftpd;
    }

    public double getpSmbd() {
        return pSmbd;
    }

    public double getpPhpcgi() {
        return pPhpcgi;
    }

    public double getpIrcd() {
        return pIrcd;
    }

    public double getpDistccd() {
        return pDistccd;
    }

    public double getpRmi() {
        return pRmi;
    }

    public String getSourceIP() {
        return sourceIP;
    }

}
