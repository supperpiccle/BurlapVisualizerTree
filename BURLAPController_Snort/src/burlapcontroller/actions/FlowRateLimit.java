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
public class FlowRateLimit extends CriteriaAction implements FullActionModel {

    public FlowRateLimit(String actionName, Domain domain) throws IOException {
        super(actionName, domain);
    }

    @Override
    protected State performActionHelper(State s, GroundedAction groundedAction) {
        ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
        double pScan = agent.getRealValForAttribute(ATTPSCAN);
        double prob = Math.random();
        if (prob < 0.4) {
            pScan = 0;
        }

        //Setting the attributes for the next state
        agent.setValue(ATTFLOWLIMITIPS, sFlolimitIps + sAttackerIp + ";");
        agent.setValue(ATTPSCAN, pScan);

        //return the state we just modified
        return s;
    }

    @Override
    public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
//        super.printDebugInfo(s.getFirstObjectOfClass(CLASSAGENT));

        State ns1 = s.copy();
        State ns2 = s.copy();

        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTFLOWLIMITIPS, sFlolimitIps + sAttackerIp + ";");
        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPSCAN, 0);

        ns2.getFirstObjectOfClass(CLASSAGENT).setValue(ATTFLOWLIMITIPS, sFlolimitIps + sAttackerIp + ";");

        TransitionProbability t1 = new TransitionProbability(ns1, 0.5);
        TransitionProbability t2 = new TransitionProbability(ns2, 0.5);

        List<TransitionProbability> tps = new ArrayList<TransitionProbability>(2);
        tps.add(t1);
        tps.add(t2);

        return tps;
    }

}
