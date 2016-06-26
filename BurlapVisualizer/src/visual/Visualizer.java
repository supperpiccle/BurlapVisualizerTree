/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;


import Tree.StateNode;
import Tree.StateTree;
import burlap.oomdp.core.states.State;
import burlapcontroller.Reward;
import burlapcontroller.actions.CriteriaAction;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.DataSizeAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualItem;

import prefuse.visual.expression.InGroupPredicate;
import prefuse.render.EdgeRenderer;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;
import BurlapVisualizer.MyController;
import Tree.Connection;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlapcontroller.Termination;
import java.awt.BasicStroke;
import java.awt.geom.Point2D;
import java.util.Hashtable;
import prefuse.action.filter.FisheyeTreeFilter;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.data.Tree;
import prefuse.visual.NodeItem;


/**
 * This class is our handle the the visualizer as a whole.  After the state space has been selected this class takes over execution.
 * @author Justin Lewis
 */
public class Visualizer {
    Tree graph; //this is the major data type of prefuse(contains the nodes and edges and other info)
    Visualization vis;
    private Display d;
    StateTree tree;
     List<StateNode> nodeList;
    private DataDisplay dataDisplay;
    FinalControlListener tooltipHandler;
    List<String> allAtrribs;
    Reward reward;
    JFrame frame;
    MyController thisController;
    FinalControlListener mouse;
    List<VisualItem> edgeItems; //this is a list of the edge items from init to target state
    List<Edge> edges;
    List<Node> nodes;
    StateValueContainer stateValueContainer;
    ActionValueContainer actionValueContainer;
    Chart chart;
    NodeAndEdgeDataMan nodeAndEdgeManager;
    List<ComputeState> computableStates;
    double degredation;
    int temp = 0;
    List<Connection> removedConnections;
    ComputeState lastComputeState;
    List<State> chosenStates;
    
    
    /**
     * This function will close the {@link visual.DataDisplay} and this class.
     */
    public void closeWindows()
    {
        if(frame != null && dataDisplay != null)
        {
            frame.dispose();
            dataDisplay.close(); 
            chart.close();
            
        }

    }
    
    /**
     * This thread sets up the actual visualization with the fed in data.
     * <p>
     * When this function ends all that is left running is control listeners and prefuse threads.
     * There is nothing more we must do.
     * @param tree tree data structure you want to visualize.
     * @param allAttribs List of string of attributes each state has
     * @param actions List of every possible action in the MDP
     * @param controller The underlying controller for this MDP
     * @throws IOException
     * @throws ParseException 
     */
    public Visualizer(StateTree tree, List<String> allAttribs, List<CriteriaAction> actions, MyController controller, double degredation) throws IOException, ParseException
    {

        reward = controller.getRewardFunction();
        actionValueContainer = new ActionValueContainer(reward);
        stateValueContainer = new StateValueContainer(actionValueContainer);
        removedConnections = new ArrayList();

        

        
this.computableStates = new ArrayList();
this.chosenStates = new ArrayList();
        
        this.tree = tree;
        this.nodeList = this.tree.getNodes();
        this.allAtrribs = allAttribs;
        thisController = controller;
        this.degredation = degredation;
       
        System.out.println("data next");
        setUpData(true);
        System.out.println("data done");
        setUpVisualization();
        setUpRenderers();
        setUpActions();
        setUpDisplay();
        chart = new Chart(stateValueContainer, actionValueContainer);
        mouse.setChart(chart);
//        nodeAndEdgeManager.waitForChanges();
}

    
    
    /**
     * This is the last function in the program that gets called that sets up the windows for viewing.
     */
    public void setUpDisplay()
    {
        mouse = new FinalControlListener(thisController.getRewardFunction()); //create listener
        dataDisplay = new DataDisplay(tree, this.allAtrribs, thisController,edgeItems, mouse); //set up the dataDisplay(mouse is needed for buttons)
        dataDisplay.setUpCharts("no action", null, null);//set charts up with nothing selected
        mouse.setDataDisplay(dataDisplay);//and let mouse have control of dataDisplay(To set up tables when user clicks)
        mouse.setVisualizer(this);
        mouse.currentState = (NodeItem) vis.getVisualItem("graph.nodes", nodes.get(0));
        
        
        
        
//        nodeAndEdgeManager.mouse = mouse;
//        nodeAndEdgeManager.dataDisplay = dataDisplay;
                        
        d = new Display(vis);
        d.setSize(900, 900);
        d.addControlListener(new DragControl());
        d.addControlListener(new PanControl());
        d.addControlListener(new ZoomControl());
        d.addControlListener(new WheelZoomControl());
        d.addControlListener(mouse); //controls when you click node or edge
        
        
        frame = new JFrame("Burlap Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(d);
        frame.pack();
        frame.setVisible(true);
        
        //these layouts were defined in setUpActions()
        vis.run("color");
        vis.run("layout");
        vis.run("repaint");
//        vis.run("postLayout");
    }
    
    public void updateDisplay()
    {
        d.setVisualization(vis);
        
        vis.run("color");
        vis.run("layout");
        vis.run("repaint");
        
        d.repaint();
    }
    
    /**
     * This function sets up the visualization with the given graph.
     * <p>
     * The visualizer in prefuse handles things like color rather than the raw data like the graph.
     */
    public void setUpVisualization()
    {       
        vis = new Visualization();

        Table n = graph.getNodeTable();
        Table e = graph.getEdgeTable();
        Graph g_new = new Graph(n, e, true); //this was required to give edges directions
//        graph = g_new;
        

        vis.add("graph", g_new);
        setItems();//now that vis has been created we can deal with items.

    }
    public void rebootVis()
    {
        
        vis.run("color");
        vis.run("layout");
        vis.run("repaint");
//        vis.removeGroup("graph");
//        
//        Table n = graph.getNodeTable();
//        Table e = graph.getEdgeTable();
//        Graph g_new = new Graph(n, e, true); //this was required to give edges directions
//        
//        
//        vis.add("graph", g_new);
    }
    
//    public void updateVisualization()
//    {
//        Table n = graph.getNodeTable();
//        Table e = graph.getEdgeTable();
//        Graph g_new = new Graph(n, e, true); //this was required to give edges directions
////        graph = g_new;
////        vis.removeAction("graph");
//        vis.removeGroup("graph");
//        vis.add("graph", g_new);
////        setItems();//now that vis has been created we can deal with items.
//    }
    
    
    /**
     * This handles how the nodes and edges are drawn in prefuse.
     * <p>
     * We gave some edges labels and all nodes a label.  We also made the nodes oval shapped which was handled in this function.
     */
    public void setUpRenderers()
    {
        FinalRenderer r = new FinalRenderer();
        EdgeRenderer er = new EdgeRenderer(Constants.EDGE_TYPE_LINE, prefuse.Constants.EDGE_ARROW_FORWARD);
//        er.setDefaultLineWidth(10);
       
        DefaultRendererFactory drf = new DefaultRendererFactory();
        drf.setDefaultRenderer(r);
        drf.setDefaultEdgeRenderer(er);

        
        // We now have to have a renderer for our decorators.
        LabelRenderer test = new LabelRenderer("ActionName");
//        test.setVerticalAlignment(-10);
//        test.setHorizontalPadding(15);
//        test.setVerticalPadding(10);
        drf.add(new InGroupPredicate("ActionName"), test);
        drf.add(new InGroupPredicate("stateRewards"), new LabelRenderer("StateReward"));
        

    
        // -- Decorators responsible for both types of labels(edges and nodes)
        //edge labels
        final Schema DECORATOR_SCHEMA = PrefuseLib.getVisualItemSchema();
        DECORATOR_SCHEMA.setDefault(VisualItem.INTERACTIVE, false); 
        DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR, 
                               ColorLib.rgb(0, 0, 0)); 
//        DECORATOR_SCHEMA.setDefault(VisualItem.STARTY, 25);
//        DECORATOR_SCHEMA.setDefault(VisualItem.HIGHLIGHT, true);
//        DECORATOR_SCHEMA.setDefault(VisualItem.X, 120);
        DECORATOR_SCHEMA.setDefault(VisualItem.FONT, 
                               FontLib.getFont("Goblin One",12));
        
        //node labels
        final Schema DECSchema = PrefuseLib.getVisualItemSchema();
        DECSchema.setDefault(VisualItem.INTERACTIVE, false);
        DECSchema.setDefault(VisualItem.TEXTCOLOR, ColorLib.rgb(0, 200, 0));
        DECSchema.setDefault(VisualItem.FONT, FontLib.getFont("Tahoma", 12));
        
        //add decorators to vis
        vis.addDecorators("ActionName", "graph.edges", DECORATOR_SCHEMA);
        vis.addDecorators("stateRewards", "graph.nodes", DECSchema);
      
                // This Factory will use the ShapeRenderer for all nodes.
        vis.setRendererFactory(drf);
        
    }
    
    /**
     * These actions define how color and size work in our visualization.
     */
    public void setUpActions()
    {
        int[] s_pallette = {ColorLib.rgb(255,105,180), ColorLib.rgb(0,0,255), ColorLib.rgb(255, 0, 0), ColorLib.rgb(102,51,153)};
        //colors are as follows for s_pallette(node colors)
        //pink(is any node that does not have anything to do with the path)
        //blue(final state)
        //red(initial state)
        //purple(is a state in the path from initial state to target state)
        int[] e_pallette = {ColorLib.rgb(255,0,0), ColorLib.rgb(0,255,0)};
        //black(an action that was not taken)
        //blue(an action that was taken)
        
        
        //this action handles how nodes are colored
        DataColorAction fill = new DataColorAction("graph.nodes", "nodeInfo", Constants.ORDINAL, VisualItem.FILLCOLOR, s_pallette);   
        fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255,255, 0));
     
        
        //this action handles how edges are colored
        DataColorAction edges = new DataColorAction("graph.edges", "inPath",  Constants.NOMINAL, VisualItem.STROKECOLOR, e_pallette);
        edges.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 255, 0));
        DataColorAction arrowColor = new DataColorAction("graph.edges", "inPath",Constants.NOMINAL, VisualItem.FILLCOLOR, e_pallette);
        arrowColor.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 255, 0));
        //this action handles how thick edges are
        DataSizeAction edgeThick = new DataSizeAction("graph.edges", "weight");
        
        
        //list of actions that define color...let it run indefinatly
        ActionList color = new ActionList(Activity.INFINITY);
        color.add(fill);
        color.add(edges);
        color.add(arrowColor);
        color.add(edgeThick);
//        color.add(new RepaintAction());
        
        
        //we define two different layouts here
        //the first layout runs first for around 5 seconds.
        //the reason is because it is a force directed graph.
        //After the graph has finished doing all it's moving
        //it would be nice for it to stop, which is why the second layout
        //exists.  In this way
        //things like decorators can still move around when needed and
        //the nodes stay still
        ActionList layout = new ActionList(Activity.INFINITY);
//        ActionList postLayout = new ActionList(Activity.INFINITY);
        
        
        //setup the first fdl with high gravity
//        ForceDirectedLayout fdl = new ForceDirectedLayout("graph", false);
//        ForceSimulator fs = new ForceSimulator();
//        NBodyForce bodyForces = new NBodyForce(-1000, -1, 1);
//        DragForce dragForce = new DragForce((float) 0.009);
//        fs.addForce(bodyForces);
//        fs.addForce(dragForce);
//        fs.addForce(new SpringForce((float) 0.0006, 10));
//        fdl.setForceSimulator(fs);
        NodeLinkTreeLayout treeLayout = new NodeLinkTreeLayout("graph", Constants.ORIENT_LEFT_RIGHT, 150, 100, 120);
        treeLayout.setLayoutAnchor(new Point2D.Double(25,300));
            
        
        //layout #1 just contains the fdl(and repaint so changes are seen)
//        layout.add(fdl);
        layout.add(treeLayout);
        layout.add(new FinalDecoratorLayout("stateRewards"));
        layout.add(new FinalDecoratorLayout("ActionName"));
        layout.add(new RepaintAction());
        
        //the second layout contains only the decorators
//        postLayout.add(new RepaintAction());
//        postLayout.add(new FinalDecoratorLayout("EdgeReward"));
//        postLayout.add(new FinalDecoratorLayout("stateRewards"));
        
        ActionList filter = new ActionList();
        filter.add(new FisheyeTreeFilter("graph", 2));
        filter.add(treeLayout);
        

        
        
        vis.putAction("color", color);
        vis.putAction("layout", layout);
//        vis.putAction("postLayout", postLayout);
    }
    
    /**
     * This function defines what data prefuse will be working with such as
     * how nodes connect to other nodes.
     * @throws ParseException 
     */
    public void setUpData(boolean init) throws ParseException
    {
        nodes = new ArrayList<>();
        edgeItems = new ArrayList<>();
        edges = new ArrayList<>();
        
        State currentState;
        if(!init)
        {
            for(int i = 0; i < graph.getNodeCount(); i++)
            {
                
            }
        }
        //set up graph and set all columns
//        if(init)
        {
            graph = new Tree();
            graph.addColumn("state", String.class);
            graph.addColumn("reward", Double.class);
            graph.addColumn("nodeInfo", Integer.class);
            graph.addColumn("action", String.class);
            graph.addColumn("inPath", Integer.class);
            graph.addColumn("weight", float.class);
            graph.addColumn("type", String.class);
            graph.addColumn("stateClass", State.class);
            graph.addColumn("srcState", State.class);
            graph.addColumn("resultState", State.class);
            graph.addColumn("CriteriaAction", CriteriaAction.class);
            graph.addColumn("StateReward", Double.class);
            graph.addColumn("ActionName", String.class);
            graph.addColumn("CurrentState", boolean.class);
        }
        graph.clear();
//        graph.clearSpanningTree();
//        if(!init) return;
        
        StateNode computingState;
        if(lastComputeState == null) computingState = tree.initialNode;
        else computingState = lastComputeState.thisState;
        if(init)
        {
            System.out.println("befer comput states");
            generateComputeStates(computingState, null);
            System.out.println("after compute states");
        }
            
        

        
        List<List<State>> visibleStates = new ArrayList();
        List<List<Boolean>> undoStates = new ArrayList(); /////unimplemented at the moment
        List<List<CriteriaAction>> visibleActions = new ArrayList();
        
        int numOfPaths = tree.initialNode.connections.size(); //this will have to be changed later
        Hashtable<String, CriteriaAction> definedActions = thisController.getActionMap();
//        for(int i = 0; i < computableStates.size();i++)
//        {
//            List<State> StatePath = new ArrayList();
//            List<CriteriaAction> ActionPath = new ArrayList();
//
//            for(int j = 0; j < computableStates.get(i).prevStates.size(); j++)
//            {
////                System.out.println(computableStates.get(i).prevStates.get(j).s.getCompleteStateDescription());
//            }
////            System.out.println(computableStates.get(i).thisState.s.getCompleteStateDescription());
//            for(int j = 0; j < computableStates.get(i).prevActions.size(); j++)
//            {
////                System.out.println(computableStates.get(i).prevActions.get(j).getName());
//            }
////            System.out.println("----------------------");
//            for(int j = 0; j < computableStates.get(i).ea.numTimeSteps() - 1; j++)
//            {
////                System.out.println(computableStates.get(i).ea.actionSequence.get(j));
//            }
//            System.out.println();
//        }
//        System.out.println("We have a whooping total of " + computableStates.size());
        
        /*
        Now that all the computeStates have been set with their previous states and actions and with episode anaylisis run on each state,
        all that i need to do is fill the visibleactions and visible states accordingly.  should be a piece of cake
        */
        double optimalReward = thisController.getRewardWithCA(tree.actionsTaken);
        double largestAllowedReward = optimalReward * (1 +(degredation/100));
        for(int i = 0; i < computableStates.size(); i++)
        {
            List<State> onePath = new ArrayList();
            List<CriteriaAction> oneActionPath = new ArrayList();
            List<Boolean> canStateUndo = new ArrayList(); //unimplemented at the moment
            for(int j = 0; j < computableStates.get(i).prevStates.size(); j++)
            {
//                System.out.println("Adding from prevStates" + computableStates.get(i).prevStates.get(j).s.getCompleteStateDescription());
                onePath.add(computableStates.get(i).prevStates.get(j).s);
                canStateUndo.add(Boolean.FALSE);
            }
            onePath.add(computableStates.get(i).thisState.s);
            for(int j = 0; j < computableStates.get(i).ea.stateSequence.size(); j++)
            {
                if(j == 0 && computableStates.get(i).undoStates) 
                {
                    canStateUndo.add(Boolean.TRUE);
                }
                else
                {
                    canStateUndo.add(Boolean.FALSE);
                }
//                System.out.println("adding from state seq" + computableStates.get(i).ea.stateSequence.get(j).getCompleteStateDescription());
                onePath.add(computableStates.get(i).ea.stateSequence.get(j));
            }
//            oneActionPath.add(tree.initialNode.connections.get(i).action);
            for(int j = 0; j < computableStates.get(i).prevActions.size(); j++)
            {
//                String actName = ea.actionSequence.get(j).action.getName();
//                CriteriaAction act = definedActions.get(actName);
//                System.out.println("adding action:" + act.getName() + "     " + actName);
                
                oneActionPath.add(computableStates.get(i).prevActions.get(j));
            }
            for(int j = 0; j < computableStates.get(i).ea.actionSequence.size();j++)
            {
                String actName = computableStates.get(i).ea.actionSequence.get(j).actionName();
                CriteriaAction act = definedActions.get(actName);
                oneActionPath.add(act);
            }
            System.out.println();
            
            if(degredation >= 0)
            {
                
                //below are print statements that allow the programmer to see which paths were included
                double thisReward = thisController.getRewardWithCA(oneActionPath);
                boolean containRemovedConnections = false;
                for(int a = 0; a < onePath.size(); a++)
                {
                    for(int b = 0; b < removedConnections.size(); b++)
                    {
                        for(int c = 0; c < removedConnections.get(b).states.size(); c++)
                        {
                            if(onePath.get(a).equals(removedConnections.get(b).states.get(c)) && oneActionPath.get(a-1).equals(removedConnections.get(b).action))
                            {
                                containRemovedConnections = true;
                            }
                        }
                    }
                }
                
                if(thisReward >= largestAllowedReward && !containRemovedConnections)
                {
//                    System.out.println("did add");
//                    System.out.println("reward of " + thisReward + " compared to " + optimalReward);
                    visibleStates.add(onePath);
                    visibleActions.add(oneActionPath);
                    undoStates.add(canStateUndo);
                }
                else
                {
//                    System.out.println("DID NOT ADD");
//                    System.out.println("reward of " + thisReward + " compared to " + optimalReward);
                }
            }
            else
            {
                visibleStates.add(onePath);
                visibleActions.add(oneActionPath);
                undoStates.add(canStateUndo);
            }

            
//            System.out.println("sum of actions" + (computableStates.get(0).prevActions.size() + computableStates.get(0).ea.actionSequence.size()));
//            System.out.println("sum of states" + (computableStates.get(0).prevStates.size() + computableStates.get(0).ea.stateSequence.size()));

//            break;
        }
//        nodeAndEdgeManager.pushView(visibleStates, visibleActions, tree.statesTaken, tree.actionsTaken); //will have to be changed later
//        edges = nodeAndEdgeManager.getEdgesFromInitialToFinal();
//        setItems();
       
        
        
        
        setUpNodes(visibleStates, visibleActions, undoStates);
        setPrefuseNodeConnections(visibleStates, visibleActions);
        setDefaultEdgeData();
        handlePathToTarget();
//        Edge e = graph.addEdge(graph.getNode(1), graph.getNode(1));
//        e.set("weight", 100.0);
//                    e.set("type", "edge");
//            e.set("weight", 1.0);
//            e.set("inPath", 0);

        
//        EpisodeAnalysis ea = this.thisController.getOptimalPathFrom(nodeList.get(80).s, 0);
    }
    
    //end public functions.
    //------------------------------------------------------------------------------------------------------
    //begin of private functions
    
    /**
     * sets the items for edgeItems and gives every node a black stroke
     */
    private void setItems()
    {
        //edges is only the edges from initial state to target state
        for(int i = 0; i < edges.size(); i++)
        {
            VisualItem item = vis.getVisualItem("graph.edges", edges.get(i));
            CriteriaAction thisAction = (CriteriaAction) item.get("CriteriaAction");
            item.set("reward", thisController.getRewardWithCA(thisAction));
            System.out.println("reward is " + thisController.getRewardWithCA((CriteriaAction) item.get("CriteriaAction")));
            edgeItems.add(item);//this is used by dataDisplay later when we make buttons(When user clicks the button we want it to be 
                                //as if the user actually clicked that edge).
        }
        //give every node black stroke
        for(int i = 0; i < nodes.size(); i++)
        {
            VisualItem item = vis.getVisualItem("graph.nodes", nodes.get(i));
            item.setStrokeColor(ColorLib.rgb(0, 0, 0));
            item.setStroke(new BasicStroke(0));
        }
        vis.getVisualItem("graph.nodes", nodes.get(0)).setStroke(new BasicStroke(10));
        
        for(int i = 0; i < graph.getEdgeCount(); i++)
        {
            vis.getVisualItem("graph.edges", graph.getEdge(i)).setSize(5);
        }
        
    }
    /**
     * create nodes for each state
     */
    private void setUpNodes(List<List<State>> visibleStates, List<List<CriteriaAction>> visibleActions, List<List<Boolean>> undoStates)
    {
        State startingPoint = tree.initialState;
        
//        System.out.println("testing");
//        for(int i = 0; i < 10; i++)
//        {
//            System.out.println("visibleActions " + visibleActions.get(0).get(i).getName() + " and tree " + tree.actionsTaken.get(i).getName());
//        }
        List<State> alreadySetNodes = new ArrayList();
        graph.addRoot().set("CurrentState", true); //this adds the root and sets a needed column
        Node n = graph.getRoot();
        
        //optimal path
        for(int i = 0; i < tree.statesTaken.size(); i++)
        {
//            Node n = graph.addNode();
            n.set("type", "node");
            n.set("state", tree.statesTaken.get(i).getCompleteStateDescription()); 
            n.set("stateClass", tree.statesTaken.get(i));       
            double stateValueFunction = thisController.getV(tree.statesTaken.get(i));
            double finalStateValueFunction = (double) Math.round(stateValueFunction * 100000) / 100000; //the math.round goes to 5 places(up to the decimal point)
            n.set("StateReward", finalStateValueFunction);
            nodes.add(n);
            alreadySetNodes.add(tree.statesTaken.get(i));


            
                Node temp = n;
                
                if(i < tree.statesTaken.size() - 1)
                {
                    n = graph.addChild(n);
                    Edge edge = graph.getEdge(temp, n);
                    edge.set("srcState", tree.statesTaken.get(i));//set the edges source state
                    edge.set("resultState", tree.statesTaken.get(i + 1));//and target state
                    edge.set("action", tree.actionsTaken.get(i).getName());
                    edge.set("CriteriaAction", tree.actionsTaken.get(i));
                    edge.set("ActionName", tree.actionsTaken.get(i).getName());
                    edge.set("reward", thisController.getRewardWithCA(tree.actionsTaken.get(i)));
                }

            

        }
        //paths other than the otimal one
//        n = graph.getRoot();
//        State prevState = null;
//        Node prevNode = null;
        for(int i = 0; i < visibleStates.size(); i++)
        {
            n = graph.getRoot();
            State prevState = null;
            Node prevNode = null;
            int numOfNodes = visibleStates.get(i).size();
            for(int j = 0; j < numOfNodes;j++)
            {
                boolean flag = false;
                for(int k = 0; k < graph.getNodeCount(); k++)
                {
//                    System.out.println("j is " + j);
//                            System.out.println("total is" + visibleActions.get(i).size());
                    if(graph.getNode(k).get("stateClass").equals(visibleStates.get(i).get(j)))
                    {
                        flag = true; //copy was found
                    }
                    if(flag)//handle the copy
                    {
//                        System.out.println("hanging here bro");
//                        System.out.println("prev state is" + prevNode.get("stateClass").toString());
                        Edge e = graph.getEdge(n, graph.getNode(k));
//                        System.out.println("right before e == null i ==" + i);
                        if(e == null && prevState != null)
                        {
//                            System.out.println("j is " + (j - 1));
//                            System.out.println("adding " + visibleActions.get(i).get(j-1));
//                            System.out.println("total is" + visibleActions.get(i).size());
                            e = graph.addEdge(n, graph.getNode(k));
                            e.set("srcState", prevState);//set the edges source state
                            e.set("resultState", visibleStates.get(i).get(j));//and target state
                            e.set("action", visibleActions.get(i).get(j-1).getName());
                            e.set("CriteriaAction", visibleActions.get(i).get(j-1));
                            e.set("ActionName", visibleActions.get(i).get(j-1).getName());
                            e.set("reward", thisController.getRewardWithCA(visibleActions.get(i).get(j-1)));
                        }
                        
                        flag = false;
                        
                        
                        
                        j++;
//                        System.out.println("incremented j");
                        if(j >= visibleStates.get(i).size())
                        {
                            break;
                        }

                        prevState = visibleStates.get(i).get(j - 1);
                        n = graph.getNode(k);
                        k = 0;
//                    n = graph.addChild(n);
                    }

                }

                if(j >= visibleStates.get(i).size())
                        break;

//                alreadySetNodes.add(visibleStates.get(i).get(j));
                Node temp = n;
                n = graph.addChild(n);

                if(j == 0)
                {
//                    n = graph.addChild(n);
                    Edge edge = graph.getEdge(temp, n);
                    edge.set("srcState", startingPoint);//set the edges source state
                    edge.set("resultState", visibleStates.get(i).get(j));//and target state
                    edge.set("action", visibleActions.get(i).get(j).getName());
                    edge.set("CriteriaAction", visibleActions.get(i).get(j));
                    edge.set("ActionName", visibleActions.get(i).get(j).getName());
                    edge.set("reward", thisController.getRewardWithCA(visibleActions.get(i).get(j)));
//                    System.out.println("I SHOULD SEE THIS ONLY ONCE");
//                    if(undoStates.get(i).get(j).booleanValue())
                    {

                    }
                }

                else if(j < visibleStates.get(i).size())// - 1)
                {
//                    n = graph.addChild(n);
                    Edge edge = graph.getEdge(temp, n);
                    edge.set("srcState", prevState);//set the edges source state
                    edge.set("resultState", visibleStates.get(i).get(j));//and target state
                    edge.set("action", visibleActions.get(i).get(j-1).getName());
                    edge.set("CriteriaAction", visibleActions.get(i).get(j-1));
                    edge.set("ActionName", visibleActions.get(i).get(j-1).getName());
                    edge.set("reward", thisController.getRewardWithCA(visibleActions.get(i).get(j-1)));
                }
                
                //----------------------------------------------------------------------------------------------
//                                        System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
//                        int reverseEdge = graph.addEdge(1, 0);
//                        graph.getEdge(reverseEdge).set("weight", 200);
                        //-------------------------------------------------------------------------------------------
                        //the above lines prove that i can add edges that go against the laws of trees
                n.set("type", "node");
                n.set("state", visibleStates.get(i).get(j).getCompleteStateDescription()); 
                n.set("stateClass", visibleStates.get(i).get(j));  
                prevState = visibleStates.get(i).get(j);
                prevNode = n;
                double stateValueFunction = thisController.getV(visibleStates.get(i).get(j));
                double finalStateValueFunction = (double) Math.round(stateValueFunction * 100000) / 100000; //the math.round goes to 5 places(up to the decimal point)
                n.set("StateReward", finalStateValueFunction);
                
                TerminalFunction tf = new Termination();
//                System.out.println("we are on  j = " + j + "and totoal is " + visibleStates.get(i).size());
                if(finalStateValueFunction == 0.0)
                {
//                    System.out.println("this state is final but " + tf.isTerminal(visibleStates.get(i).get(j)));
                }
                if(tf.isTerminal(visibleStates.get(i).get(j)))
                {
                    n.set("nodeInfo", 1); //blue node
                }
                else if(visibleStates.get(i).get(j).equals(tree.initialState))
                {
                    n.set("nodeInfo", 2); //red node
                }
                else
                {
                    n.set("nodeInfo", 0); //pink node
                }
                
                nodes.add(n);
            }
        }


        
        
        
    }
    /**
     * exactly what the function names says. It creates the connections between nodes in the graph.
     */
    private void setPrefuseNodeConnections(List<List<State>> visibleStates, List<List<CriteriaAction>> visibleActions)
    {
        int counter = 0;
        
        for(int i = 0; i < tree.actionsTaken.size(); i++)
        {
            Node n1 = graph.getNode(counter);
            Node n2 = graph.getNode(counter + 1);
//            Edge edge = graph.addEdge(n1, n2);
//            edge.set("srcState", tree.statesTaken.get(counter));//set the edges source state
//            edge.set("resultState", tree.statesTaken.get(counter + 1));//and target state
//            edge.set("action", tree.actionsTaken.get(counter).getName());
//            edge.set("CriteriaAction", tree.actionsTaken.get(counter));
//            
            State currentState = tree.statesTaken.get(i);
            State nextState = tree.statesTaken.get(i + 1);
            
                TerminalFunction tf = new Termination();
                if(tf.isTerminal(nextState))
                {
                    n2.set("nodeInfo", 1); //blue node
                }
                if(currentState.equals(tree.initialState))
                {
                    n1.set("nodeInfo", 2); //red node
                }
                else
                {
                    n1.set("nodeInfo", 0); //pink node
                }
            counter++;
        }
        
        counter++;
        for(int i = 0; i < visibleStates.size(); i++)
        {
            Node n1 = graph.getNode(0);
            int numOfNodes = visibleStates.get(i).size();
//            System.out.println("numOfNode:"+ numOfNodes);

            for(int j = 0; j < numOfNodes; j++)
            {

                State currentState = visibleStates.get(i).get(j);//starting from this node get all the resulting state
            
//                n1 = n1.getFirstChild();

                

                
                        
                    
                    
                   
//                String description = currentState.getCompleteStateDescription();
//                n1.set("state", description); 
                
                //lastly specify what type of node the currentStateNode is.
                TerminalFunction tf = new Termination();
//                if(tf.isTerminal(currentState))
//                {
//                    n1.set("nodeInfo", 1); //blue node
//                }
//                else if(currentState.equals(tree.initialState))
//                {
//                    n1.set("nodeInfo", 2); //red node
//                }
//                else
//                {
//                    n1.set("nodeInfo", 0); //pink node
//                }
                counter++;
            }
            counter++;
        }
        

        for(int i = 0; i < graph.getNodeCount(); i++)
        {
//            System.out.println(graph.getNode(i).get("nodeInfo"));
        }
    }
    
    /**
     * This function just sets the default data for every edge.
     * <p>
     * For edges that are actions to the target state the weight and inPath parameters will change.
     */
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
    
    /**
     * This function sets data needed for the nodes and edges from the initial state to the target state
     */
    private void handlePathToTarget()
    {
        for(int i = 0; i < tree.stateNodesTaken.size() - 1; i++)
        {
            //the following lines are to end up with one state in the path and the next
            StateNode src = tree.stateNodesTaken.get(i);
            StateNode target = tree.stateNodesTaken.get(i+1);
            int indexSrc = i;
            int indexTarget = i+1;
            Node nodeSrc = graph.getNode(indexSrc);
            Node nodeTarget = graph.getNode(indexTarget);
            
            stateValueContainer.addStateValue(thisController.getV(src.s));
            actionValueContainer.addAction(src.s, target.s, src.checkIfResultState(target.s));
            
            if(!target.isTerminal()) nodeTarget.set("nodeInfo", 3); //purple nodes
            if(!nodeSrc.equals(nodeTarget)) //if the result state is the same as the src then nothing needs to happen
            {
                Edge e = graph.getEdge(nodeSrc, nodeTarget); //get the edge between the nodes
            
                if(e != null) //and change it accordingly
                {
                e.set("inPath", 1);  
                e.set("weight", 25.0);
//                int inPath = (int) e.get("inPath");
                edges.add(e); //edges is a list of the edges from initial to target
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
        stateValueContainer.addStateValue(0); //final state has 0 value
    }
    
    //change the below function to not do all the work before it exits.  Right now it takes a 
    private void generateComputeStates(ComputeState prevState)
    {
//        temp++;
//        System.out.println(temp);
        for(int i = 0; i < prevState.thisState.connections.size(); i++)
        {
            for(int j = 0; j < prevState.thisState.connections.get(i).nodes.size(); j++)
            {
                StateNode nextNode = findInTree(prevState.thisState.connections.get(i).nodes.get(0).s);
//                if(!checkForExistInTree(nextNode))
//                {
//                    
//                }
                    ComputeState nextState = new ComputeState();
        
                    for(int k = 0; k < prevState.prevStates.size(); k++)
                    {
                        nextState.prevStates.add(prevState.prevStates.get(k));
                    }
                    for(int k = 0; k < prevState.prevActions.size(); k++)
                    {
                        nextState.prevActions.add(prevState.prevActions.get(k));
                    }
                    boolean shouldAdd = true;
                    nextState.thisState = nextNode;
                    for(int k = 0; k < computableStates.size(); k++)
                    {
                        if(computableStates.get(k).thisState.equals(nextState.thisState))
                        {
                            shouldAdd = false;
                        }
                    }
                    nextState.prevStates.add(prevState.thisState);

                    nextState.prevActions.add(prevState.thisState.connections.get(i).action);
                    if(shouldAdd)
                    {
                        this.computableStates.add(nextState);
                    }
                    nextState.ea = thisController.getOptimalPathFrom(nextState.thisState.s);
//                    System.out.println("Size of computable States is: " + computableStates.size());
                    generateComputeStates(nextState);
//                    the below line are part of the Undo feature but I must ask Dr. Stefano what to do about the actions that can
                    //be performed on each state according to the policy(look in MyController at get resulting states.
//                    for(int k = 0; k < nextState.thisState.connections.size(); k++)
//                    {
////                        System.out.println("should print alot");
//                        for(int l = 0; l < nextState.thisState.connections.get(k).states.size(); l++)
//                        {
//                            System.out.println(nextState.thisState.connections.get(k).action.getName());
//                            if(nextState.thisState.equals(nextState.thisState.connections.get(k).states.get(l)))
//                            {
//                                nextState.undoStates = true;
//                                nextState.undoAction = nextState.thisState.connections.get(k).action;
//                                System.out.println("I CAN UNDO");
//                            }
//                        }
//                    }
                    
                    
//                }
            }
        }
        
    }
    public void generateComputeStates(StateNode initState, CriteriaAction prevAction) {
//        ComputeState initialComputeState = new ComputeState();
//        initialComputeState.thisState = initState;
        
        
        if(lastComputeState == null) 
        {
            lastComputeState = new ComputeState();
            lastComputeState.thisState = initState;
        }
        else
        {
            lastComputeState.prevStates.add(lastComputeState.thisState);
            lastComputeState.prevActions.add(prevAction);
            lastComputeState.thisState = initState;
        }
        
        for(int i = 0; i < lastComputeState.prevStates.size(); i++)
        {
            System.out.println(lastComputeState.prevStates.get(i).s.getCompleteStateDescription());
        }
        
        
//        initialComputeState.thisState = tree.initialNode;
        System.out.println("right before generate");
//        generateComputeStates(initialComputeState);
        System.out.println("right after generate");
        
        for(int i = 0; i < initState.connections.size(); i++)
        {
            for(int j = 0; j < initState.connections.get(i).states.size(); j++)
            {
                ComputeState newCompute = new ComputeState();
                
                for(int k = 0; k < lastComputeState.prevActions.size(); k++)
                {
                    newCompute.prevActions.add(lastComputeState.prevActions.get(k));
                }
                for(int k = 0; k < lastComputeState.prevStates.size(); k++)
                {
                    newCompute.prevStates.add(lastComputeState.prevStates.get(k));
                }
                
                newCompute.prevActions.add(initState.connections.get(i).action);
                newCompute.prevStates.add(initState);
                newCompute.thisState = initState.connections.get(i).nodes.get(j);
                newCompute.ea = new EpisodeAnalysis();//thisController.getOptimalPathFrom(newCompute.thisState.s);
                
//                if(!tree.stateNodesTaken.contains(newCompute.thisState))
                {
                    computableStates.add(newCompute);
                }
                
            }
        }
        
//        for(int i = 0; i < computableStates.size(); i++)
//        {
////            System.out.println(computableStates.get(i).thisState.s);
//        }
    }
    private boolean checkForExistInTree(StateNode n)
    {
        for(int i = 0; i < tree.stateNodesTaken.size(); i++)
        {
            if(tree.stateNodesTaken.get(i).equals(n)) 
            {
//                System.out.println("TRUE");
                return true;
            }
        }
//        System.out.println("FALSE");
        return false;
    }
    private StateNode findInTree(State s)
    {
        for(int i = 0; i < tree.nodes.size(); i++)
        {
            if(tree.nodes.get(i).s.equals(s)) return tree.nodes.get(i);
        }
        return null;
    }

    void rehighlightPath() 
    {
        for(int i = 0; i < graph.getNodeCount(); i++)
        {
            if(!graph.getNode(i).get("stateClass").equals(lastComputeState.thisState.s) && graph.getNode(i).getBoolean("CurrentState"))
            {
                graph.getNode(i).set("CurrentState", false);
                vis.getVisualItem("graph.nodes", graph.getNode(i)).setStroke(new BasicStroke(0));
//                vis.getVisualItem("graph.nodes", graph.getNode(0)).setStroke(new BasicStroke(0));
            }
            if(graph.getNode(i).get("stateClass").equals(lastComputeState.thisState.s))
            {
                System.out.println("found it!!!");
                graph.getNode(i).set("CurrentState", true);
                vis.getVisualItem("graph.nodes", graph.getNode(i)).setStroke(new BasicStroke(10));
            }
        }
        for(int i = 0; i < this.chosenStates.size() - 1; i++)
        {
            for(int j = 0; j < graph.getNodeCount(); j++)
            {
                if(graph.getNode(j).get("stateClass").equals(this.chosenStates.get(i)))
                {
                    for(int k = 0; k < graph.getNodeCount(); k++)
                    {
                        if(graph.getNode(k).get("stateClass").equals(this.chosenStates.get(i + 1)))
                        {
                            graph.getEdge(graph.getNode(j), graph.getNode(k)).set("weight", 200);
                        }
                    }
                }
            }
        }
        
//        for(int i = 0; i < graph.getEdgeCount(); i++)
//        {
//            for(int j = 0; j < chosenEdges.size(); j++)
//            {
//                if(graph.getEdge(i).equals(chosenEdges.get(j)))
//                {
//                    graph.getEdge(i).set("weight", 200);
//                    System.out.println("eDGES DO EXISTS");
//                }
//            }
//        }
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}