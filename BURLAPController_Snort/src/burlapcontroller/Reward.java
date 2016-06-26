/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burlapcontroller;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlapcontroller.actions.CriteriaAction;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author stefano
 */
public class Reward implements RewardFunction {

    private double maxCost = 0;
    private double minCost = 0;
    private double maxRespTime = 0;
    private double minRespTime = 0;

    private double wr = 1;
    private double wc = 0;
    private double wi = 0;

    public double getWr() {
        return wr;
    }

    public void setWr(double wr) {
        this.wr = wr;
    }

    public double getWc() {
        return wc;
    }

    public void setWc(double wc) {
        this.wc = wc;
    }

    public double getWi() {
        return wi;
    }

    public void setWi(double wi) {
        this.wi = wi;
    }
    
    

    private Hashtable<String, Double> reward;

    public Reward(List<Action> actions, double wr, double wc, double wi) throws FileNotFoundException, IOException {
        this.wr=wr;
        this.wc=wc;
        this.wi=wi;
        for (Action a : actions) {
            if (a instanceof CriteriaAction) {
                CriteriaAction c = (CriteriaAction) a;
                if (c.getCost() > maxCost) {
                    maxCost = c.getCost();
                }
                if (c.getCost() < minCost) {
                    minCost = c.getCost();
                }
                if (c.getResponseTime() > maxRespTime) {
                    maxRespTime = c.getResponseTime();
                }
                if (c.getResponseTime() < minRespTime) {
                    minRespTime = c.getResponseTime();
                }
            }
        }

        reward = new Hashtable<String, Double>();
        for (Action a : actions) {
            if (a instanceof CriteriaAction) {
                if (a.getName().equals("increaseLogVerb")) {
                    System.out.println();
                }
                CriteriaAction c = (CriteriaAction) a;
                reward.put(c.getName(), rewardHelper(c));
            }

        }

//        FileInputStream fis = new FileInputStream("config/reward.properties");
//        Properties p = new Properties();
//        p.load(fis);
//        wr = Double.valueOf(p.getProperty("wr"));
//        wc = Double.valueOf(p.getProperty("wc"));
//        wi = Double.valueOf(p.getProperty("wi"));
//        fis.close();
    }

    private double rewardHelper(CriteriaAction a) {
//        double ret = -wr * ((a.getResponseTime() - minRespTime) / (maxRespTime - minRespTime))
//                - wc * ((a.getCost() - minCost) / (maxCost - minCost)) - wi * a.getImpact();
        double ret = -wr*(a.getResponseTime()/maxRespTime) - wc* (a.getCost()/maxCost) - wi*(a.getImpact());
        

        return ret;
    }

    @Override
    public double reward(State s, GroundedAction a, State sprime) {
//        System.out.println("Reward: "+ reward.get(a.actionName()));
//        double currentReward = reward.get(a.actionName());
//        currentReward *=2;
//        System.out.println("Reward for " + a.actionName() + " = " + currentReward);
//        reward.put(a.actionName(), currentReward);
        return reward.get(a.actionName());
    }

    public void setReward(String actionName, double d) {
        reward.put(actionName, d);
    }

    public double getReward(String actionName) {
        return reward.get(actionName);
    }

}
