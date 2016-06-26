/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burlapcontroller;

import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import static burlapcontroller.Controller.*;

/**
 *
 * @author stefano
 */
public class Termination implements TerminalFunction {

    private int counter = 0;

    public Termination() {
    }

    @Override
    public boolean isTerminal(State s) {
        counter++;
        //get location of agent in next state
        ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
        double pScan = agent.getRealValForAttribute(ATTPSCAN);
        double pVsftpd = agent.getRealValForAttribute(ATTPVSFTPD);
        double pSmbd = agent.getRealValForAttribute(ATTPSMBD);
        double pPhpCgi = agent.getRealValForAttribute(ATTPPHPCGI);
        double pIrcd = agent.getRealValForAttribute(ATTPIRCD);
        double pDistccd = agent.getRealValForAttribute(ATTPDISTCCD);
        double pRmi = agent.getRealValForAttribute(ATTPRMI);

        String blockedIps = agent.getStringValForAttribute(ATTBLOCKEDIPS);
        boolean firewall = agent.getBooleanValForAttribute(ATTFIREWALL);
        String flowLimitedIps = agent.getStringValForAttribute(ATTFLOWLIMITIPS);
        String honeypottedIps = agent.getStringValForAttribute(ATTHONEYPOTIPS);
        int logVerb = agent.getIntValForAttribute(ATTLOGVERB);
        boolean quarantined = agent.getBooleanValForAttribute(ATTQUARANTINED);
        boolean active = agent.getBooleanValForAttribute(ATTACTIVE);
        boolean softwareUpToDate = agent.getBooleanValForAttribute(ATTSOFTWARE);
        boolean backup = agent.getBooleanValForAttribute(ATTBACKUP);
        boolean alerted = agent.getBooleanValForAttribute(ATTALERTED);
        boolean rebooted = agent.getBooleanValForAttribute(ATTREBOOTED);
        if (counter % 500000 == 0) {
            System.out.println(counter);
        }

//        boolean ret = (pScan < T1 && pVsftpd < T1 && pSmbd < T1 && pPhpCgi < T1 && pIrcd < T1
//                && pDistccd < T1 && pRmi < T1 && !blockedIps && active && !flowLimitedIps 
//                && !honeypottedIps && logVerb == 0 && !quarantined) || (pScan < T2 && pVsftpd < T1 && pSmbd < T1 && pPhpCgi < T1 && pIrcd < T1
//                && pDistccd < T1 && pRmi < T1 && !blockedIps && active && flowLimitedIps 
//                && !honeypottedIps && !quarantined);
        //These terminal states represent some kind of anomaly that we cannot identify as an attack
        //Therefore we increase the defenses by activating firewall and logging and
        //flowlimiting for the source IP address causing the anomaly.
        boolean controlledAnomalyStates = (pScan < T2 && pVsftpd < T2 && pSmbd < T2
                && pPhpCgi < T2 && pIrcd < T2 && pDistccd < T2 && pRmi < T2)
                && (pScan >= T1 || pVsftpd >= T1 || pSmbd >= T1
                || pPhpCgi >= T1 || pIrcd >= T1 || pDistccd >= T1 || pRmi >= T1)
                && (firewall && blockedIps.isEmpty() && !flowLimitedIps.isEmpty()
                && honeypottedIps.isEmpty() && logVerb > 0  && active && !quarantined);

        //These terminal states represent a totally clean or cleaned system.
        boolean allClean = (pScan < T1 && pVsftpd < T1 && pSmbd < T1
                && pPhpCgi < T1 && pIrcd < T1 && pDistccd < T1 && pRmi < T1)
                && (blockedIps.isEmpty() && flowLimitedIps.isEmpty() 
                && honeypottedIps.isEmpty() && logVerb == 0 && active && !quarantined);
        boolean ret = allClean || controlledAnomalyStates;

        return ret;
    }
}
