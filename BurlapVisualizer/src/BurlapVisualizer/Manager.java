/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BurlapVisualizer;

import Tree.StateNode;
import Tree.StateTree;
import burlap.oomdp.core.states.State;
import burlapcontroller.actions.CriteriaAction;
import java.util.List;
import visual.Visualizer;

/**
 * This class manages how {@link BurlapVisualizer.MyController} and
 * {@link visual.Visualizer} interact with each other.
 *
 * @author Justin Lewis
 */
public class Manager {

    List<StateNode> nodes; //list of all nodes in state space
    List<State> takenStates;//list of all the states taken
    List<CriteriaAction> takenActions;//list of all the actions taken
    Visualizer MDPvisual;//an instance of the visualizer

    /**
     * This function is only called by {@link BurlapVisualizer.TestProject} when the
     * user specifies a different initial state.
     * <p>
     * All this function does is call {@link visual.Visualizer#closeWindows() }
     * to close all windows with the old initial state.
     */
    public void close() {
        if(MDPvisual != null) MDPvisual.closeWindows();
    }

    /**
     * When this function is called, everything about the initial state is known
     * and it is time to set everything up for the visualization.
     * <p>
     * This function is responsible for getting all the connections between
     * states by calling the appropriate functions.  After it gets these connections
     * it sends that data to the visualizer and gives it complete control
     *
     * @param scan
     * @param vsftpd
     * @param smbd
     * @param phpcgi
     * @param ircd
     * @param distccd
     * @param rmi
     * @param cost 
     * @param impact
     * @param time
     * @throws Exception
     */
    public void run(double scan, double vsftpd, double smbd, double phpcgi, double ircd, double distccd, double rmi, double cost, double impact, double time, double gamma, double degredation) throws Exception 
    {

        MyController temp = new MyController(0, 0, 0, 0, 0, 0, 0, "", 1, 0, 0, 0.9);
        temp.getEntireStateSpaceAndConnections();
        List<String> allAttribs = temp.getAllStateAttributes();//grab attribute names to pass into MDPvisual constructor
        
        MyController c = new MyController(scan, vsftpd, smbd, phpcgi, ircd, distccd, rmi, "", cost, impact, time, gamma);//this sets up and solves the MDP solution


        nodes = c.getEntireStateSpaceAndConnections();//nodes is a list that contains trees with a height of at most 2.
                                                      //for more info about what c.getEntireStateSpaceAndConnections() does look
                                                      //at the JavaDoc
                                                      

        takenActions = c.valueIterator();//the actions taken from intial state to target state
        takenStates = c.getStateSequenceToTarget();//the states taken from intial state to target state

        List<CriteriaAction> allPosActions = c.getDefinedActions();//this is a list of every action that the MDP has defined
        
        
        //now we have all our data we need for the visualizer.
        //To make things easier I created the StateTree class which can wrap all
        //this data into one nice data structure
        StateTree tree = new StateTree(c.getInitialState(), nodes, allPosActions);
        tree.setTakenActions(takenActions);
        tree.setStatesTaken(takenStates);
        tree.buildTree();//this sets the connections between connections that c.getIntireStatSpaceAndConnections() did not do.
//        System.out.println("MDP next");

        MDPvisual = new Visualizer(tree, allAttribs, allPosActions, c, degredation);//the visualizer takes over from here
    }
}
