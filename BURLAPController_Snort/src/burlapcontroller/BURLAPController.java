/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burlapcontroller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

/**
 *
 * @author stefano
 */
public class BURLAPController {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {

//        SnortAlertParser parser = new SnortAlertParser(args[0]);
//
//        double pScan = parser.getpScan();
//        double pVsftpd = parser.getpVsftpd();
//        double pSmbd = parser.getpSmbd();
//        double pPhpCgi = parser.getpPhpcgi();
//        double pIrcd = parser.getpIrcd();
//        double pDistccd = parser.getpDistccd();
//        double pRmi = parser.getpRmi();
//        String ip = parser.getSourceIP();
//        
//        System.out.println("pScan = " + pScan);
//        System.out.println("pVsftpd = " + pVsftpd);
//        System.out.println("pSmbd = " + pSmbd);
//        System.out.println("pPhpCgi = " + pPhpCgi);
//        System.out.println("pIrcd = " + pIrcd);
//        System.out.println("pDistccd = " + pDistccd);
//        System.out.println("pRmi = " + pRmi);
//        System.out.println("IP = " + ip);
//
//        Controller c = new Controller(pScan, pVsftpd, pSmbd, pPhpCgi, pIrcd, pDistccd, pRmi, ip);
        Controller c = new Controller(1, 0, 0, 0, 0, 1, 0, "", 0, 0, 1);
        String outputPath = "output/"; //directory to record results

//	run example
//        Date d1 = new Date();
        c.valueIteration(outputPath, 0.9);
        System.out.println(c.getEpisodeAnalysis().getActionSequenceString());
//        c.uct(outputPath);
        //c.testSolutions();
//        c.QLearningExample(outputPath);

//        c.testSolutionsParallel();
//        Date d2 = new Date();
//        System.out.println("Execution Time: " + (d2.getTime() - d1.getTime()));
//        c.DFSExample(outputPath);
//        c.BFSExample(outputPath);
//        BufferedReader br = new BufferedReader(new FileReader(outputPath + "vi.episode"));
//        String line;
//        while ((line = br.readLine()) != null) {
//            System.out.println(line);
//        }
//        br.close();
    }
    
}
