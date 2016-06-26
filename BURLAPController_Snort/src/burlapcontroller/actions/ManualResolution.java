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
public class ManualResolution extends CriteriaAction implements FullActionModel {

    
    public ManualResolution(String actionName, Domain domain) throws IOException {
        super(actionName, domain);
    }

    @Override
    protected State performActionHelper(State s, GroundedAction groundedAction) {
        
        ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
//        System.out.println(s.getCompleteStateDescription());
        //Setting the attributes for the next state
        agent.setValue(ATTPSCAN, 0);
        agent.setValue(ATTPVSFTPD, 0);
        agent.setValue(ATTPSMBD, 0);
        agent.setValue(ATTPPHPCGI, 0);
        agent.setValue(ATTPIRCD, 0);
        agent.setValue(ATTPDISTCCD, 0);
        agent.setValue(ATTPRMI, 0);
        agent.setValue(ATTQUARANTINED, false);
        agent.setValue(ATTACTIVE, true);
        //return the state we just modified
        return s;
    }

    @Override
    public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
//        super.printDebugInfo(s.getFirstObjectOfClass(CLASSAGENT));

        State ns1 = s.copy();

        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPSCAN, 0);
        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPVSFTPD, 0);
        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPSMBD, 0);
        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPPHPCGI, 0);
        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPIRCD, 0);
        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPDISTCCD, 0);
        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTPRMI, 0);
        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTQUARANTINED, false);
        ns1.getFirstObjectOfClass(CLASSAGENT).setValue(ATTACTIVE, true);

        TransitionProbability t1 = new TransitionProbability(ns1, 1);

        List<TransitionProbability> tps = new ArrayList<TransitionProbability>(1);
        tps.add(t1);

        return tps;
    }

}
