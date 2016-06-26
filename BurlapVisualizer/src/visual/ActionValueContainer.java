/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlapcontroller.actions.CriteriaAction;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jlewis
 */
public class ActionValueContainer {
    private List<Double> rewardForAction;
    private List<Double> CumulativeValue;
    private List<String> actionName;
    private RewardFunction rf;
    
    public ActionValueContainer(RewardFunction rf)
    {
        this.rf = rf;
        rewardForAction = new ArrayList<>();
        CumulativeValue = new ArrayList<>();
        actionName = new ArrayList<>();
    }
    
    public void addAction(State s, State result, CriteriaAction act)
    {
        double value = rf.reward(s, act.getAssociatedGroundedAction(), result);
        rewardForAction.add(value);
        actionName.add(act.getName());
    }
    public List<Double> getRewards()
    {
        return rewardForAction;
    }
    public List<Double> getCumulativeReward()
    {
        double sum = 0;
        for(int i = 0; i < rewardForAction.size(); i++)
        {
            sum += rewardForAction.get(i);
            try
            {
                CumulativeValue.set(i, sum); 
            }
            catch(Exception e)
            {
                CumulativeValue.add(sum);
            }
        }
        return CumulativeValue;
    }
    public String getActionName(int i)
    {
        return actionName.get(i);
    }
}
