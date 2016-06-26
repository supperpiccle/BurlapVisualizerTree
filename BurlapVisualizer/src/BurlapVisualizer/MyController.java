/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BurlapVisualizer;

import Tree.StateNode;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.Policy.ActionProb;
import burlap.behavior.singleagent.planning.Planner;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.values.Value;
import burlap.oomdp.singleagent.GroundedAction;
import burlapcontroller.Controller;
import burlapcontroller.actions.CriteriaAction;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * This class acts as an extension to {@link burlapcontroller.Controller} to provide methods to more easily
 * grab the data needed for the visualizer.
 * @author Justin Lewis
 */
public class MyController extends Controller {
    Planner planner;
    List<State> statespace;
    Policy p;   
    List<StateNode> nodes;
    double gamma;
    
    
    /**
     * This is the constructor for MyController which inherits {@link burlapcontroller.Controller} to add functionality needed to easily
     * grab the info needed for the visualizer(such as connections between state.
     * @param scan
     * @param vsftpd
     * @param smbd
     * @param phpcgi
     * @param ircd
     * @param distccd
     * @param rmi
     * @param ip
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public MyController(double scan, double vsftpd, double smbd, double phpcgi, double ircd, double distccd, double rmi, String ip, double cost, double impact, double time, double gamma) throws FileNotFoundException, IOException {
        super(scan, vsftpd, smbd, phpcgi, ircd, distccd, rmi, ip, time, cost, impact);
        
        super.valueIteration("", gamma);
        getReward(super.getEpisodeAnalysis().actionSequence);
        nodes = new ArrayList();
        this.gamma = gamma;
    }
    
    /**
     * This overrides the BURLAPS valueIterator() and it allows us to get the actions sequence of actions taken from the initial state to the target state.
     * <p>
     * The actual running of the controller happens at {@link #MyController(double, double, double, double, double, double, double, java.lang.String) }
     * and this method simply grabs the actions taken and returns that as a list.
     * @return A list of CriteriaAction which 
     * @throws IOException 
     */
    public List<CriteriaAction> valueIterator() throws IOException 
    {
        int count = this.getEpisodeAnalysis().maxTimeStep();//number of actions taken from initial state to target state
        List<CriteriaAction> actions = new ArrayList();//the list to add to and return
        
        Hashtable<String, CriteriaAction> map = getActionMap();//the map from string to CriteriaAction
        
        for(int i = 0; i < count; i++) //for each action...
        {
            String name = this.getEpisodeAnalysis().actionSequence.get(i).actionName();//get name
            CriteriaAction action = map.get(name);// now map that name to the hashtable        
            actions.add(action);// and finally add it to the list
        }
        return actions;//return the action list
    }
    
    /**
     * This basically actions as a getter function to grab the state Sequence.
     * @return List of states taken to get to the target state 
     */
    public List<State> getStateSequenceToTarget()
    {
        return this.getEpisodeAnalysis().stateSequence;
    }
    
    public double getGamma()
    {
        return this.gamma;
    }
    
    /**
     * This functions gets the entire state space and applies every action that can be applied to any given state.
     * <p>
     * An important note about what this function does not do: It only provides links from one state to another state.
     * What this means is the each {@link Tree.StateNode} only contains the root state and then children states from applying 1 action.
     * To get the full connections between states(applying more than 1 action to a state) {@link Tree.StateTree#buildTree() } must be called AFTER
     * this method has been called.
     * @return List of {@link Tree.StateNode StateNode} that contains all the resulting states given any arbitrary state.
     * @throws IOException 
     */
    public List<StateNode> getEntireStateSpaceAndConnections() throws IOException
    {
        List<State> states = this.getStateSpace(); //get all possible states

        Hashtable<String, CriteriaAction> map = getActionMap(); //in this all we do is specify action name and we will get it's correspoinding CriteriaAction
        for(int counter = 0; counter < states.size(); counter++) //iteratte through all states
        {
            State s = states.get(counter);  //current state we are working with
            StateNode current = new StateNode();//eventually put the state into here
            current.setNodeState(s);           //set the node root to state(now to fill in the actions
            
            List<ActionProb> potAction; //this is a list of all the actions that we can take(in addition to the probabilities that that action will be taken
                                        //but that is irrelevant to the purpose of the visualizer
            try//see below comment about why this try catch block exists
            {
                //the line below fails if it returns empty list....weird seems like a bug which is also the purpose for the try catch
                potAction = this.getPolicy().getActionDistributionForState(s); //potAction(potentialAction) hold all possible actions

                for(int i = 0; i < potAction.size(); i++) //go through each action and apply it
                {
                    String actionName = potAction.get(i).ga.actionName();
                                     
                    GroundedAction groundAction;
                    groundAction = map.get(actionName).getAssociatedGroundedAction(); //the ground action is what we will need for a future function call
                                                                                      //and grounded action is part of CriteriaAction so just grab that
                            
                    List<State> result = getResultingStates(s, groundAction);//when you apply action groundAction what state(s) are possible outcomes?
                    current.addStatesConnection(result, map.get(actionName));//add that list of resulting states into
                }
            }
            catch(IndexOutOfBoundsException e)
            {
                //this is only here because when no actions
                //can be performed meaning it is a final state
                //the getActionDisttributionForState(s) is the offending function.
                //I swear that function has got a bug.
            }

            nodes.add(current); //add node to list of nodes
            
        }
        return nodes; //finally return our list of nodes with possible states
    }

    
    /**
     * This method just takes a state and applied to passed in action and records the possible outcomes from that action.
     * @param s The state that is having an action applied to it.
     * @param act This action that is being applied to @param s
     * @return A list of the possible result states
     */
    private List<State> getResultingStates(State s, GroundedAction act)
    {
       List<State> nextStates = new ArrayList<>();
       List<TransitionProbability> transitionProbList = act.getTransitions(s); //all possible transistions
      
       for (TransitionProbability t: transitionProbList) 
       {
           nextStates.add(t.s); //add the state
       }
       return nextStates;
    }
    
    /**
     * This function returns a list of all the attribute names any state has.
     * <p>
     * This function can be used in conjunction to {@link #getAllStateValuesFor(burlap.oomdp.core.states.State)}
     * to get the value of the corresponding name.  Since they both return arrays you must use parralel lists to make sure the name
     * and value pair go together.  For example index 1 refers to the same attribute in the lists returned by both functions.
     * @return List of all the names of the attributes any state has in the state space
     */
    public List<String> getAllStateAttributes()
    {
        List<String> attribNames = new ArrayList<>();
        State s = this.getInitialState();
        List<Attribute> attribList = new ArrayList<>();
        attribList = s.getObject("agent0").getObjectClass().attributeList;
        for(int i = 0; i< attribList.size(); i++)
        {
            attribNames.add(attribList.get(i).name);
        }
        return attribNames;
    }

    
    /**
     * This function just calls {@link #getAllStateValuesFor(burlap.oomdp.core.states.State)} and passes the inital state as the parameter.
     * @return List that contains all of the values for the initial state. 
     */
   public List<Value> getAllStateValuesForInitialState()
   {
       return getAllStateValuesFor(this.getInitialState());
   }
   
   /**
    * This function returns a list of the state attributes values.
    * <p>
    * The values by themselves are pretty much unless unless they are also mapped
    * to the list of strings at @see getAllStateAttributes.  Parallel lists allows for the mapping.
    * @param s The state whose attribute values will be returned
    * @return A list of values whose indexes can be mapped to the string grabbed from {@link #getAllStateAttributes() getAllStateAttributes}.
    */
   public List<Value> getAllStateValuesFor(State s)
   {
       List<String> names = this.getAllStateAttributes();//get the names so we can grab all the values
       List<Value> value = new ArrayList<>();//list to return
       for(int i = 0; i < names.size(); i++)
       {
           value.add(s.getObject("agent0").getValueForAttribute(names.get(i))); //agent 0 is the object that contains all our attributes.
                                                                                //in a multiagent system this would have to change.
       }
       return value;
   }

   
   
        /**
         * This function computes the total cost, impact, and time the policy took and returns it.
         * <p>
         * The array represents all three values in the following format:
         * [0] = cost
         * [1] = impact
         * [2] = time
         * @param actions list of actions from initial state to target state
         * @return the array of values in the specified format
         */
        public double[] computeTotalReward(List<CriteriaAction> actions)
        {
            double[] Reward = new double[3];
            Reward[0] = 0.0;
            Reward[1] = 0.0;
            Reward[2] = 0.0;
            for(int i = 0; i < actions.size(); i++)
            {
                //grap the cost, impact, and time to do each action and add to the previous sum.
                Reward[0] += actions.get(i).getCost();
                Reward[1] += actions.get(i).getImpact();
                Reward[2] += actions.get(i).getResponseTime();
            }
            return Reward;
        }
        
        public double getReward(List<GroundedAction> acts) 
        {
            double sum = 0;
            for (GroundedAction a:acts) {
                sum+=this.getRewardFunction().getReward(a.actionName());
            }
            return sum;
        }
        public double getRewardWithCA(List<CriteriaAction> acts)
        {
            List<GroundedAction> gActs = new ArrayList<>();
            for(CriteriaAction ca: acts)
            {
                gActs.add(ca.getAssociatedGroundedAction());
            }
            return getReward(gActs);
        }
        public double getRewardWithCA(CriteriaAction act)
        {
            List<CriteriaAction> listWithOneObject = new ArrayList();
            listWithOneObject.add(act);
            return getRewardWithCA(listWithOneObject);
        }
}
