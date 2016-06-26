/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burlapcontroller.actions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import static burlapcontroller.Controller.*;
import static burlapcontroller.Controller.CLASSAGENT;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author stefano
 */
public class SystemReboot extends CriteriaAction implements FullActionModel {

    public SystemReboot(String actionName, Domain domain) throws IOException {
        super(actionName, domain);
    }

    @Override
    protected State performActionHelper(State s, GroundedAction groundedAction) {
        ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
        double pScan = agent.getRealValForAttribute(ATTPSCAN);
        double pVsftpd = agent.getRealValForAttribute(ATTPVSFTPD);
        double pSmbd = agent.getRealValForAttribute(ATTPSMBD);
        double pPhpcgi = agent.getRealValForAttribute(ATTPPHPCGI);
        double pIrcd = agent.getRealValForAttribute(ATTPIRCD);
        double pDistccd = agent.getRealValForAttribute(ATTPDISTCCD);
        double pRmi = agent.getRealValForAttribute(ATTPRMI);

//        double prob = Math.random();
//        if (prob < 0.3) {
//            pScan = 0;
//        }
//        prob = Math.random();
//        if (prob < 0.3) {
//            pVsftpd = 0;
//        }
//        prob = Math.random();
//        if (prob < 0.3) {
//            pSmbd = 0;
//        }
//        prob = Math.random();
//        if (prob < 0.3) {
//            pPhpcgi = 0;
//        }
//        prob = Math.random();
//        if (prob < 0.3) {
//            pIrcd = 0;
//        }
//        prob = Math.random();
//        if (prob < 0.3) {
//            pDistccd = 0;
//        }
//        prob = Math.random();
//        if (prob < 0.3) {
//            pRmi = 0;
//        }
        double prob = Math.random();
        if (prob < 0.3) {
            pScan = 0;
            pVsftpd = 0;
            pSmbd = 0;
            pPhpcgi = 0;
            pIrcd = 0;
            pDistccd = 0;
            pRmi = 0;

        }

        //Setting the attributes for the next state
        agent.setValue(ATTREBOOTED, true);
        agent.setValue(ATTPSCAN, pScan);
        agent.setValue(ATTPVSFTPD, pVsftpd);
        agent.setValue(ATTPSMBD, pSmbd);
        agent.setValue(ATTPPHPCGI, pPhpcgi);
        agent.setValue(ATTPIRCD, pIrcd);
        agent.setValue(ATTPDISTCCD, pDistccd);
        agent.setValue(ATTPRMI, pRmi);

        //return the state we just modified
        return s;
    }

    @Override
    public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
//        super.printDebugInfo(s.getFirstObjectOfClass(CLASSAGENT));

//        ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
//        double pScan = agent.getRealValForAttribute(ATTPSCAN);
//        double pVsftpd = agent.getRealValForAttribute(ATTPVSFTPD);
//        double pSmbd = agent.getRealValForAttribute(ATTPSMBD);
//        double pPhpcgi = agent.getRealValForAttribute(ATTPPHPCGI);
//        double pIrcd = agent.getRealValForAttribute(ATTPIRCD);
//        double pDistccd = agent.getRealValForAttribute(ATTPDISTCCD);
//        double pRmi = agent.getRealValForAttribute(ATTPRMI);
//
//        double prob = Math.random();
//        if (prob < 0.3) {
//            pScan = 0;
//        }
//        prob = Math.random();
//        if (prob < 0.3) {
//            pVsftpd = 0;
//        }
//        prob = Math.random();
//        if (prob < 0.3) {
//            pSmbd = 0;
//        }
//        prob = Math.random();
//        if (prob < 0.3) {
//            pPhpcgi = 0;
//        }
//        prob = Math.random();
//        if (prob < 0.3) {
//            pIrcd = 0;
//        }
//        prob = Math.random();
//        if (prob < 0.3) {
//            pDistccd = 0;
//        }
//        prob = Math.random();
//        if (prob < 0.3) {
//            pRmi = 0;
//        }
//
//        State ns1 = s.copy();
//
//        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTREBOOTED, true);
//        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPSCAN, pScan);
//        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPVSFTPD, pVsftpd);
//        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPSMBD, pSmbd);
//        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPPHPCGI, pPhpcgi);
//        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPIRCD, pIrcd);
//        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPDISTCCD, pDistccd);
//        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPRMI, pRmi);
//
//        TransitionProbability t1 = new TransitionProbability(ns1, 1);
//
//        List<TransitionProbability> tps = new ArrayList<TransitionProbability>(1);
//        tps.add(t1);
        
        List<TransitionProbability> transitions = new ArrayList<>();

        State nextState1 = s.copy();
        State nextState2 = s.copy();
        ObjectInstance irsAgent = nextState1.getFirstObjectOfClass(CLASSAGENT);

        
        irsAgent.setValue(ATTREBOOTED, true);
        irsAgent.setValue(ATTPSCAN, 0);
        irsAgent.setValue(ATTPVSFTPD, 0);
        irsAgent.setValue(ATTPSMBD, 0);
        irsAgent.setValue(ATTPPHPCGI, 0);
        irsAgent.setValue(ATTPIRCD, 0);
        irsAgent.setValue(ATTPDISTCCD, 0);
        irsAgent.setValue(ATTPRMI, 0);
        
        ObjectInstance irsAgent2 = nextState2.getFirstObjectOfClass(CLASSAGENT);
        irsAgent2.setValue(ATTREBOOTED, true);
        
                
        TransitionProbability t1 = new TransitionProbability(nextState1, 0.3);
        TransitionProbability t2 = new TransitionProbability(nextState2, 0.7);
        transitions.add(t1);
        transitions.add(t2);
        
        return transitions;

    }

}
