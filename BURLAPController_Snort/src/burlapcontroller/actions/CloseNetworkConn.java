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
import burlapcontroller.Reward;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author stefano
 */
public class CloseNetworkConn extends CriteriaAction implements FullActionModel {

    public CloseNetworkConn(String actionName, Domain domain) throws IOException {
        super(actionName, domain);
    }

    @Override
    protected State performActionHelper(State s, GroundedAction groundedAction) {
        double r = Math.random();

        ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
        if (r < 0.1) {
            agent.setValue(ATTPVSFTPD, 0);
            agent.setValue(ATTPSMBD, 0);
            agent.setValue(ATTPPHPCGI, 0);
            agent.setValue(ATTPIRCD, 0);
            agent.setValue(ATTPDISTCCD, 0);
            agent.setValue(ATTPRMI, 0);
        }

        reward.setReward(this.getName(), 2*reward.getReward(this.getName()));
 
        //return the state we just modified
        return s;
    }

    @Override
    public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
//        super.printDebugInfo(s.getFirstObjectOfClass(CLASSAGENT));

        ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
        State ns1 = s.copy();
        State ns2 = s.copy();
        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPVSFTPD, 0);
        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPSMBD, 0);
        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPPHPCGI, 0);
        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPIRCD, 0);
        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPDISTCCD, 0);
        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPRMI, 0);

        TransitionProbability t1 = new TransitionProbability(ns1, 0.1);
        TransitionProbability t2 = new TransitionProbability(ns2, 0.9);

        List<TransitionProbability> tps = new ArrayList<TransitionProbability>(2);
        tps.add(t1);
        tps.add(t2);

        return tps;
    }

}
