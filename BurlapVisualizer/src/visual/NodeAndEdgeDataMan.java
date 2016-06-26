/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;

import BurlapVisualizer.MyController;
import Tree.StateNode;
import Tree.StateTree;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlapcontroller.Termination;
import burlapcontroller.actions.CriteriaAction;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tree;

/**
 *
 * @author jlewis
 */
public class NodeAndEdgeDataMan 
{
    Stack<NodeAndEdgeData> nodeAndEdgeDataStack;
    Tree graph;
    StateTree tree;
    MyController thisController;
    int depthOfStates = 0;
    FinalControlListener mouse;
    DataDisplay dataDisplay;
    
    public NodeAndEdgeDataMan(Tree graph, MyController controller, StateTree tree)
    {
        this.graph = graph;
        thisController = controller;
        nodeAndEdgeDataStack = new Stack();
        this.tree = tree;
    }
    public void pushView(List<List<State>> visibleStates, List<List<CriteriaAction>> visibleActions,
                    List<State> optimalStateSeq, List<CriteriaAction> optimalActionSeq)
    {
//        this.graph = graph;
        graph.clear();
        NodeAndEdgeData newView = new NodeAndEdgeData();
        newView.optimalActionSeq = optimalActionSeq;
        newView.optimalStateSeq = optimalStateSeq;
        newView.visibleActions = visibleActions;
        newView.visibleStates = visibleStates;
        newView.indexOfLastState = depthOfStates;
        newView.edges = new ArrayList();
        nodeAndEdgeDataStack.push(newView);
        depthOfStates++;
        for(int i = 0; i < optimalActionSeq.size(); i++)
        {
            System.out.println(optimalActionSeq.get(i).getName());
        }
        System.out.println();
        this.setNodesAndEdges();
        setDefaultEdgeData();
        this.handleOptimalPath();
    }
    
    public void setNodesAndEdges()
    {
        
        NodeAndEdgeData newData = nodeAndEdgeDataStack.lastElement();
        State startingPoint = newData.optimalStateSeq.get(newData.indexOfLastState);
        
//        System.out.println("testing");
//        for(int i = 0; i < 10; i++)
//        {
//            System.out.println("visibleActions " + visibleActions.get(0).get(i).getName() + " and tree " + tree.actionsTaken.get(i).getName());
//        }
        List<State> alreadySetNodes = new ArrayList();
        this.graph.addRoot();
        Node n = graph.getRoot();
        
        //optimal path
        Node tempInit;
        for(int i = 0; i < newData.optimalStateSeq.size(); i++)
        {
            if(i == newData.indexOfLastState) tempInit = n;
//            Node n = graph.addNode();
            n.set("type", "node");
            n.set("state", newData.optimalStateSeq.get(i).getCompleteStateDescription()); 
            n.set("stateClass", newData.optimalStateSeq.get(i));       
            double stateValueFunction = thisController.getV(newData.optimalStateSeq.get(i));
            double finalStateValueFunction = (double) Math.round(stateValueFunction * 100000) / 100000; //the math.round goes to 5 places(up to the decimal point)
            n.set("StateReward", finalStateValueFunction);
//            nodes.add(n);
//            alreadySetNodes.add(tree.statesTaken.get(i));


            
                Node temp = n;
                
                if(i < newData.optimalStateSeq.size() - 1)
                {
                    n = graph.addChild(n);
                    Edge edge = graph.getEdge(temp, n);
                    edge.set("srcState", newData.optimalStateSeq.get(i));//set the edges source state
                    edge.set("resultState", newData.optimalStateSeq.get(i + 1));//and target state
                    edge.set("action", newData.optimalActionSeq.get(i).getName());
                    edge.set("CriteriaAction", newData.optimalActionSeq.get(i));
                    edge.set("ActionName", newData.optimalActionSeq.get(i).getName());
                }
                if(i == 0) n.set("nodeInfo", 2); //red node
                else if(i > 0 && i < newData.optimalStateSeq.size() - 1) n.set("nodeInfo", 3); //purple node
                else n.set("nodeInfo", 1); //blue node 
        }
        //paths other than the otimal one
        if(newData.indexOfLastState == 0) n = graph.getRoot();
        else
        {
//            System.out.println("HEEYYY");
        }
        State prevState = null;
        Node prevNode = null;
        for(int i = 0; i < newData.visibleStates.size(); i++)
        {
            int numOfNodes = newData.visibleStates.get(i).size();
            for(int j = 0; j < numOfNodes;j++)
            {
                boolean flag = false;
                for(int k = 0; k < graph.getNodeCount(); k++)
                {
                    if(graph.getNode(k).get("stateClass").equals(newData.visibleStates.get(i).get(j)))
                    {
                        flag = true; //copy was found
                    }
                    if(flag)//handle the copy
                    {
//                        System.out.println("hanging here bro");
//                        System.out.println("prev state is" + prevNode.get("stateClass").toString());
                        Edge e = graph.getEdge(n, graph.getNode(k));
                        if(e == null)
                        {
                            e = graph.addEdge(n, graph.getNode(k));
                            e.set("srcState", prevState);//set the edges source state
                            e.set("resultState", newData.visibleStates.get(i).get(j));//and target state
                            e.set("action", newData.visibleActions.get(i).get(j).getName());
                            e.set("CriteriaAction", newData.visibleActions.get(i).get(j));
                            e.set("ActionName", newData.visibleActions.get(i).get(j).getName());
                        }
                        
                        flag = false;
                        
                        j++;
                        if(j >= newData.visibleStates.get(i).size())
                        {
                            break;
                        }

                        prevState = newData.visibleStates.get(i).get(j - 1);
                        n = graph.getNode(k);
                        k = 0;
//                    n = graph.addChild(n);
                    }

                }

                if(j >= newData.visibleStates.get(i).size())
                        break;

//                alreadySetNodes.add(visibleStates.get(i).get(j));
                Node temp = n;
                n = graph.addChild(n);

                if(j == 0)
                {
//                    n = graph.addChild(n);
                    Edge edge = graph.getEdge(temp, n);
                    edge.set("srcState", startingPoint);//set the edges source state
                    edge.set("resultState", newData.visibleStates.get(i).get(j));//and target state
                    edge.set("action", newData.visibleActions.get(i).get(j).getName());
                    edge.set("CriteriaAction", newData.visibleActions.get(i).get(j));
                    edge.set("ActionName", newData.visibleActions.get(i).get(j).getName());
//                    System.out.println("I SHOULD SEE THIS ONLY ONCE");
                }
                
                else if(j < newData.visibleStates.get(i).size())// - 1)
                {
//                    n = graph.addChild(n);
                    Edge edge = graph.getEdge(temp, n);
                    edge.set("srcState", prevState);//set the edges source state
                    edge.set("resultState", newData.visibleStates.get(i).get(j));//and target state
                    edge.set("action", newData.visibleActions.get(i).get(j).getName());
                    edge.set("CriteriaAction", newData.visibleActions.get(i).get(j));
                    edge.set("ActionName", newData.visibleActions.get(i).get(j).getName());
                }
                n.set("type", "node");
                n.set("state", newData.visibleStates.get(i).get(j).getCompleteStateDescription()); 
                n.set("stateClass", newData.visibleStates.get(i).get(j));  
                prevState = newData.visibleStates.get(i).get(j);
                prevNode = n;
                double stateValueFunction = thisController.getV(newData.visibleStates.get(i).get(j));
                double finalStateValueFunction = (double) Math.round(stateValueFunction * 100000) / 100000; //the math.round goes to 5 places(up to the decimal point)
                n.set("StateReward", finalStateValueFunction);
                
                TerminalFunction tf = new Termination();
//                System.out.println("we are on  j = " + j + "and totoal is " + newData.visibleStates.get(i).size());
                if(finalStateValueFunction == 0.0)
                {
//                    System.out.println("this state is final but " + tf.isTerminal(newData.visibleStates.get(i).get(j)));
                }
                if(tf.isTerminal(newData.visibleStates.get(i).get(j)))
                {
                    n.set("nodeInfo", 1); //blue node
                }
                else if(newData.visibleStates.get(i).get(j).equals(tree.initialState))
                {
                    n.set("nodeInfo", 2); //red node
                }
                else
                {
                    n.set("nodeInfo", 0); //pink node
                }
                
//                nodes.add(n);
            }
        }


        
        for(int i = 0; i < graph.getNodeCount(); i++)
        {
//            System.out.println(graph.getNode(i).get("nodeInfo"));
        }
        
    }
    
    
    public void handleOptimalPath()
    {
        NodeAndEdgeData newData = nodeAndEdgeDataStack.lastElement();
        graph.getNode(0).set("nodeInfo", 2);
        for(int i = 0; i < tree.stateNodesTaken.size() - 1; i++)
        {
            
            //the following lines are to end up with one state in the path and the next
//            StateNode src = tree.stateNodesTaken.get(i);
            State target = newData.optimalStateSeq.get(i+1);
            int indexSrc = i;
            int indexTarget = i+1;
            Node nodeSrc = graph.getNode(indexSrc);
            Node nodeTarget = graph.getNode(indexTarget);
            
//            stateValueContainer.addStateValue(thisController.getV(src.s));
//            actionValueContainer.addAction(src.s, target.s, src.checkIfResultState(target.s));
            TerminalFunction tf = new Termination();
            
            if(!tf.isTerminal(target)) nodeTarget.set("nodeInfo", 3); //purple nodes
            else nodeTarget.set("nodeInfo", 1);
            if(!nodeSrc.equals(nodeTarget)) //if the result state is the same as the src then nothing needs to happen
            {
                Edge e = graph.getEdge(nodeSrc, nodeTarget); //get the edge between the nodes
            
                if(e != null) //and change it accordingly
                {
                e.set("inPath", 1);  
                e.set("weight", 25.0);
                newData.edges.add(e); //edges is a list of the edges from initial to target
                }
            }

            
            //the below loops add labels to edges coming off of states in the state path
//            for(int u = 0; u < tree.stateNodesTaken.get(i).connections.size(); u++)
//            {
//                List<List<StateNode>> resultingStates = tree.stateNodesTaken.get(i).getAllResultingStateNodes(); //this is the srcNode 
//                for(int j = 0; j < resultingStates.size(); j++) //for every possible action
//                {
//                    for(int q = 0; q < resultingStates.get(j).size(); q++) //for every possible resulting state from the previous action
//                    {
//                        StateNode targetState = resultingStates.get(j).get(q);
//                        int index = nodeList.indexOf(targetState);
//                   
//                        Node theTargetNode = graph.getNode(index); //get the target node
//                        Edge e = graph.getEdge(nodeSrc, theTargetNode);
//                        String act = (String) e.get("action"); //the action name to go from nodeSrc to theTargetNode
//
//                        double rewardValue = reward.getReward(act); //find the reward
//                        double finalValue = (double) Math.round(rewardValue * 10000) / 10000; //round to 5 places
//                        e.set("reward", finalValue);//and set that in e
//                    }
//                }
//            }
                        
        }//end of loop
//        stateValueContainer.addStateValue(0); //final state has 0 value
    }
    public List<Edge> getEdgesFromInitialToFinal()
    {
        return nodeAndEdgeDataStack.lastElement().edges;
    }
   
    private void setDefaultEdgeData()
    {
        for(int i = 0; i < graph.getEdgeCount(); i++)
        {
            Edge e = graph.getEdge(i);
            e.set("type", "edge");
            e.set("weight", 1.0);
            e.set("inPath", 0);
        }
    }
    public void update()
    {
            
            
        if(mouse.clickedAction.equals(nodeAndEdgeDataStack.lastElement().optimalActionSeq.get(0)))
        {
            NodeAndEdgeData newData = nodeAndEdgeDataStack.lastElement();
//            EpisodeAnalysis tempEA = thisController.getOptimalPathFrom(tree.statesTaken.get(1));
            
            
//            List<State> optimalStateSeq = new ArrayList();
//            List<CriteriaAction> optimalActionSeq = new ArrayList();
//            for(int i = 0; i <= newData.indexOfLastState; i++)
//            {
//                optimalStateSeq.add(newData.optimalStateSeq.get(i));
//            }
//            for(int i = 1; i < newData.optimalStateSeq.size(); i++)//for(int i = 0; i < tempEA.stateSequence.size(); i++)
//            {
//                optimalStateSeq.add(newData.optimalStateSeq.get(i));
//            }
//            
//            for(int i = 0; i < newData.indexOfLastState; i++)
//            {
//                optimalActionSeq.add(newData.optimalActionSeq.get(i));
//            }
//            for(int i = 1; i < newData.optimalActionSeq.size(); i++)
//            {
////                GroundedAction ga = tempEA.actionSequence.get(i);
////                CriteriaAction ca = thisController.getActionMap().get(ga.actionName());
//                optimalActionSeq.add(newData.optimalActionSeq.get(i));
//            }
//            for(int i = 0; i < optimalActionSeq.size(); i++)
//            {
////                System.out.println(optimalActionSeq.get(i).getName());
//            }
            
            List<List<State>> visibleStates = new ArrayList();
            List<List<CriteriaAction>> visibleActions = new ArrayList();
            int numOfPaths = tree.initialNode.connections.size(); //this will have to be changed later
            Hashtable<String, CriteriaAction> definedActions = thisController.getActionMap();
            for(int i = 0; i < numOfPaths; i++)
            {
                List<State> onePath = new ArrayList();
                List<CriteriaAction> oneActionPath = new ArrayList();
                if(tree.initialNode.connections.get(i).states.get(0).equals(tree.statesTaken.get(1)))
                {
                     continue;//will have to be changed
                }
                EpisodeAnalysis ea = thisController.getOptimalPathFrom(tree.initialNode.connections.get(i).states.get(0));
                for(int j = 0; j < ea.numTimeSteps(); j++)
                {
//                    System.out.println("add state:" + ea.stateSequence.get(j).getCompleteStateDescription());
                
                    onePath.add(ea.stateSequence.get(j));
                }
                oneActionPath.add(tree.initialNode.connections.get(i).action);
                for(int j = 0; j < ea.actionSequence.size(); j++)
                {
                    String actName = ea.actionSequence.get(j).action.getName();
                    CriteriaAction act = definedActions.get(actName);
                    System.out.println("adding action:" + act.getName() + "     " + actName);
                
                    oneActionPath.add(act);
                }
                System.out.println();
                visibleStates.add(onePath);
                visibleActions.add(oneActionPath);
//            break;
            }
            this.pushView(visibleStates, visibleActions, newData.optimalStateSeq, newData.optimalActionSeq);
        }
            
        
    }
}
