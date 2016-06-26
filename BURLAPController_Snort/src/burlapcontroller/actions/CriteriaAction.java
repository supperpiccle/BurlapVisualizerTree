/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burlapcontroller.actions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.SimpleAction;
import static burlapcontroller.Controller.*;
import burlapcontroller.Reward;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import javax.el.ELProcessor;

/**
 *
 * @author stefano
 */
public abstract class CriteriaAction extends SimpleAction {

    protected double cost;
    protected double responseTime;
    protected double impact;
    private static final String ATTCOST = "Cost";
    private static final String ATTRESPTIME = "ResponseTime";
    private static final String ATTIMPACT = "Impact";

    protected double sPscan;
    protected double sPvsftpd;
    protected double sPsmbd;
    protected double sPphpcgid;
    protected double sPircd;
    protected double sPdistccd;
    protected double sPrmi;

    protected String sAttackerIp;

    protected boolean sFirewall;
    protected String sBlockedIps;
    protected String sFlolimitIps;
    protected String sHoneypotIps;
    protected int sReconfCounter;
    protected int sLogVerb;
    protected boolean sActive;
    protected boolean sQuarantined;
    protected boolean sRebooted;
    protected boolean sAlerted;
    protected boolean sBackup;
    protected boolean sUpdated;

    private String precondition;
    private static final String ATTPRECOND = "Precondition";

    protected Reward reward = null;

    protected void getStateAttributes(State s) {
        ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);

        sPscan = agent.getRealValForAttribute(ATTPSCAN);
        sPvsftpd = agent.getRealValForAttribute(ATTPVSFTPD);
        sPsmbd = agent.getRealValForAttribute(ATTPSMBD);
        sPphpcgid = agent.getRealValForAttribute(ATTPPHPCGI);
        sPircd = agent.getRealValForAttribute(ATTPIRCD);
        sPdistccd = agent.getRealValForAttribute(ATTPDISTCCD);
        sPrmi = agent.getRealValForAttribute(ATTPRMI);

        sAttackerIp = agent.getStringValForAttribute(ATTATTACKERIP);

        sFirewall = agent.getBooleanValForAttribute(ATTFIREWALL);
        sBlockedIps = agent.getStringValForAttribute(ATTBLOCKEDIPS);
        sFlolimitIps = agent.getStringValForAttribute(ATTFLOWLIMITIPS);
        sHoneypotIps = agent.getStringValForAttribute(ATTHONEYPOTIPS);
        sLogVerb = agent.getIntValForAttribute(ATTLOGVERB);
        sActive = agent.getBooleanValForAttribute(ATTACTIVE);
        sQuarantined = agent.getBooleanValForAttribute(ATTQUARANTINED);
        sRebooted = agent.getBooleanValForAttribute(ATTREBOOTED);
        sAlerted = agent.getBooleanValForAttribute(ATTALERTED);
        sBackup = agent.getBooleanValForAttribute(ATTBACKUP);
        sUpdated = agent.getBooleanValForAttribute(ATTSOFTWARE);
    }

    protected void printDebugInfo(ObjectInstance agent) {
        System.out.println("Executing action: " + super.getName());
        System.out.println(agent.getObjectDescription());
    }

    @Override
    public boolean applicableInState(State s, GroundedAction groundedAction) {
        getStateAttributes(s);
        ELProcessor elprocessor = new ELProcessor();
        elprocessor.setValue("pScan", sPscan);
        elprocessor.setValue("pVsftpd", sPvsftpd);
        elprocessor.setValue("pSmbd", sPsmbd);
        elprocessor.setValue("pPhpcgid", sPphpcgid);
        elprocessor.setValue("pIrcd", sPircd);
        elprocessor.setValue("pDistccd", sPdistccd);
        elprocessor.setValue("pRmi", sPrmi);

        elprocessor.setValue("firewall", sFirewall);
        elprocessor.setValue("blockedIps", sBlockedIps);
        elprocessor.setValue("flowlimitIps", sFlolimitIps);
        elprocessor.setValue("honeypotIps", sHoneypotIps);
        elprocessor.setValue("reconfCounter", sReconfCounter);
        elprocessor.setValue("logVerb", sLogVerb);
        elprocessor.setValue("active", sActive);
        elprocessor.setValue("quarantined", sQuarantined);
        elprocessor.setValue("rebooted", sRebooted);
        elprocessor.setValue("alerted", sAlerted);
        elprocessor.setValue("backup", sBackup);
        elprocessor.setValue("softwareUpToDate", sUpdated);

        elprocessor.setValue("T1", T1);
        elprocessor.setValue("T2", T2);

        Object eval = elprocessor.eval(precondition);
        boolean ret = Boolean.valueOf(eval.toString());
        return ret;
    }

    public CriteriaAction(String name, Domain domain) throws FileNotFoundException, IOException {
        super(name, domain);
        FileInputStream fis = new FileInputStream("config/" + name + ".properties");
        Properties p = new Properties();
        p.load(fis);
        cost = Double.valueOf(p.getProperty(ATTCOST));
        responseTime = Double.valueOf(p.getProperty(ATTRESPTIME));
        impact = Double.valueOf(p.getProperty(ATTIMPACT));
        precondition = p.getProperty(ATTPRECOND);
        fis.close();
//        System.out.println("Action: " + name);
//        System.out.println("Response Time: " + responseTime);
//        System.out.println("Cost: " + cost);
//        System.out.println("Impact: " + impact);
//        System.out.println("Precondition: " + precondition);
    }

    public double getCost() {
        return cost;
    }

    public double getResponseTime() {
        return responseTime;
    }

    public double getImpact() {
        return impact;
    }

    public void setReward(Reward r) {
        this.reward = r;
    }

}
