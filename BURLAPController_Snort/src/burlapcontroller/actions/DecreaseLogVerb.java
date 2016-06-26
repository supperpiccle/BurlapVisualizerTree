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
import burlap.oomdp.singleagent.common.SimpleAction;
import static burlapcontroller.Controller.ATTACTIVE;
import static burlapcontroller.Controller.ATTLOGVERB;
import static burlapcontroller.Controller.ATTPSCAN;
import static burlapcontroller.Controller.ATTQUARANTINED;
import static burlapcontroller.Controller.CLASSAGENT;
import static burlapcontroller.Controller.T1;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author stefano
 */
public class DecreaseLogVerb extends CriteriaAction implements FullActionModel {

    public DecreaseLogVerb(String actionName, Domain domain) throws IOException {
        super(actionName, domain);
    }

    @Override
    protected State performActionHelper(State s, GroundedAction groundedAction) {
//        System.out.println(this.getName() + " reward = " + reward.getReward(this.getName()));
        ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
        int currentVerb = agent.getIntValForAttribute(ATTLOGVERB);
        currentVerb--;

        //Setting the attribute as true for the next state
        agent.setValue(ATTLOGVERB, currentVerb);

        //return the state we just modified
        return s;
    }

    @Override
    public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
//        super.printDebugInfo(s.getFirstObjectOfClass(CLASSAGENT));

        State ns = s.copy();
        int currentVerb = ns.getFirstObjectOfClass(CLASSAGENT).getIntValForAttribute(ATTLOGVERB);
        currentVerb--;
        ns.getFirstObjectOfClass(CLASSAGENT).setValue(ATTLOGVERB, currentVerb);
        TransitionProbability t = new TransitionProbability(ns, 1);
        List<TransitionProbability> tps = new ArrayList<TransitionProbability>(1);
        tps.add(t);
        return tps;
    }

}
