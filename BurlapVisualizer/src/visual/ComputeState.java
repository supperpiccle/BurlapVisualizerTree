/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;

import Tree.StateNode;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.core.states.State;
import burlapcontroller.actions.CriteriaAction;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jlewis
 */
public class ComputeState 
{
    StateNode thisState;
    List<StateNode> prevStates;
    List<CriteriaAction> prevActions;
    EpisodeAnalysis ea;
    boolean undoStates;
    CriteriaAction undoAction;
    boolean validEa = true;
    
    public ComputeState()
    {
        prevActions = new ArrayList();
        prevStates = new ArrayList<>();
        ea = new EpisodeAnalysis();
//        undoStates = new ArrayList();
    }
    
    public List<State> convertToStateList()
    {
        List<State> s = new ArrayList();
        for(int i = 0; i < prevStates.size(); i++)
        {
            s.add(prevStates.get(i).s);
        }
        s.add(thisState.s);
        if(ea == null) return s;
        for(int i = 0; i < ea.stateSequence.size(); i++)
        {
            s.add(ea.stateSequence.get(i));
        }
        return s;
    }
}
