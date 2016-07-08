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
import BurlapVisualizer.MyController;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlapcontroller.Termination;
import java.awt.BasicStroke;
import java.awt.geom.Point2D;
import java.util.Hashtable;
import java.util.Iterator;
import javax.swing.JPanel;
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
    Display d;
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
    List<ComputeState> computableStates;
    List<ComputeState> oldComputeStates;
    double degredation;
    ComputeState lastComputeState; //this acts as our computeState that is our currentState
    List<State> chosenStates;
    List<CriteriaAction> chosenActions;
    ActionList color;
    ActionList layout;
    ActionList repaint;
    List<List<State>> visibleStates = new ArrayList();
    List<List<Boolean>> undoStates = new ArrayList(); /////unimplemented at the moment
    List<List<CriteriaAction>> visibleActions = new ArrayList();
    List<State> temporaryHiddenStates = new ArrayList();
    List<StateNode> temporaryHiddentStateNodes = new ArrayList();
    
    JPanel panel;
    
    int[] Originals_pallette;
    int[] s_pallette;
    int[] Originale_pallette;
    int[] e_pallette;
    
    DataColorAction fill;
    DataColorAction dcaEdges;
    DataColorAction arrowColor;
    
    
    
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
        
        Originals_pallette = new int[4];
        Originals_pallette[0] = ColorLib.rgb(0,0,255);
        Originals_pallette[1] = ColorLib.rgb(255, 0, 0);
        Originals_pallette[2] = ColorLib.rgb(102,51,153);
        Originals_pallette[3] = ColorLib.rgb(255,105,180);
        
        Originale_pallette = new int[2];
        Originale_pallette[0] = ColorLib.rgb(0,255,0);
        Originale_pallette[1] = ColorLib.rgb(255,0,0);
        

        
        this.computableStates = new ArrayList();
        this.oldComputeStates = new ArrayList();
        this.chosenStates = new ArrayList();
        this.chosenActions = new ArrayList();
        
        this.tree = tree;
        this.nodeList = this.tree.getNodes();
        this.allAtrribs = allAttribs;
        thisController = controller;
        this.degredation = degredation;
        panel = new JPanel();
       
//        System.out.println("data next");
        setUpData(true);
//        System.out.println("data done");
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
        
//        graph.addChild(10);
        
        
//        nodeAndEdgeManager.mouse = mouse;
//        nodeAndEdgeManager.dataDisplay = dataDisplay;
                        
        d = new Display(vis);
        d.setSize(900, 900);
        d.addControlListener(new DragControl());
        d.addControlListener(new PanControl());
        d.addControlListener(new ZoomControl());
        d.addControlListener(new WheelZoomControl());
        d.addControlListener(mouse); //controls when you click node or edge
        
//        panel.add(d);
        
        frame = new JFrame("Burlap Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(d);
        frame.pack();
        frame.setVisible(true);
        
        //these layouts were defined in setUpActions()
        vis.run("color");
        vis.run("layout");
//        vis.run("repaint");
//        vis.run("postLayout");
    }
    
    public void updateDisplay()
    {
        d.setVisualization(vis);
        
        vis.run("color");
        vis.run("layout");
//        vis.run("repaint");
        
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
    
    public void updateVisualization()
    {
        vis.putAction("color", color);
        setItems();
        vis.run("color");
        vis.run("layout");
        
        
    }
    
    
    /**
     * This handles how the nodes and edges are drawn in prefuse.
     * <p>
     * We gave some edges labels and all nodes a label.  We also made the nodes oval shapped which was handled in this function.
     */
    public void setUpRenderers()
    {
        FinalRenderer r = new FinalRenderer();
        EdgeRenderer er = new EdgeRenderer(Constants.EDGE_TYPE_LINE, prefuse.Constants.EDGE_ARROW_FORWARD);
//        er.setDefaultLineWidth(5);
//        er.setManageBounds(false);
       
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
    
    
    
    public void setEdgePallette(boolean init)
    {
        if(init)
        {
            e_pallette = Originale_pallette.clone();
            return;
        }
        boolean subOptimalPath = false;
        boolean justAPath = false;
        for(int i = 0; i < graph.getEdgeCount(); i++)
        {
            if(graph.getEdge(i).get("inPath").equals(2))
            {
                subOptimalPath = true;
            }
            if(graph.getEdge(i).get("inPath").equals(1))
            {
                justAPath = true;
            }
        }
        if(subOptimalPath && justAPath)
        {
            System.out.println("suboptmal");
            e_pallette = new int[3];
            e_pallette[0] = Originale_pallette[0];
            e_pallette[1] = Originale_pallette[1];
            e_pallette[2] = ColorLib.rgb(0, 191, 255);
        }
        else if(subOptimalPath)
        {
            e_pallette = new int[2];
            e_pallette[0] = Originale_pallette[0];
            e_pallette[1] = ColorLib.rgb(0, 191, 255);;
        }
        else if(justAPath)
        {
            System.out.println("we hit clone");
            e_pallette = Originale_pallette.clone();
        }
        else
        {
             e_pallette = new int[1];
            e_pallette[0] = Originale_pallette[0];
        }
        
        color.remove(dcaEdges);
        color.remove(arrowColor);
        dcaEdges = new DataColorAction("graph.edges", "inPath",  Constants.NOMINAL, VisualItem.STROKECOLOR, e_pallette);
        dcaEdges.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 255, 0));
        arrowColor = new DataColorAction("graph.edges", "inPath",Constants.NOMINAL, VisualItem.FILLCOLOR, e_pallette);
        arrowColor.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 255, 0));
        color.add(dcaEdges);
        color.add(arrowColor);
        
    }
    
    /**
     * These actions define how color and size work in our visualization.
     */
    public void setUpActions()
    {
        setStatePallette(true);
//        s_pallette = {ColorLib.rgb(0,0,255), ColorLib.rgb(255, 0, 0), ColorLib.rgb(102,51,153), ColorLib.rgb(255,105,180)};
        //colors are as follows for s_pallette(node colors)
        //blue(final state)
        //red(initial state)
        //purple(is a state in the path from initial state to target state)
        //pink(is any node that does not have anything to do with the path)
        setEdgePallette(true);
        //black(an action that was not taken)
        //blue(an action that was taken)
        
        
        //this action handles how nodes are colored
        fill = new DataColorAction("graph.nodes", "nodeInfo", Constants.ORDINAL, VisualItem.FILLCOLOR, s_pallette);   
        fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255,255, 0));
        
        //this action handles how edges are colored
        dcaEdges = new DataColorAction("graph.edges", "inPath",  Constants.NOMINAL, VisualItem.STROKECOLOR, e_pallette);
        dcaEdges.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 255, 0));
        arrowColor = new DataColorAction("graph.edges", "inPath",Constants.NOMINAL, VisualItem.FILLCOLOR, e_pallette);
        arrowColor.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 255, 0));
        //this action handles how thick edges are
        DataSizeAction edgeThick = new DataSizeAction("graph.edges", "weight");
        edgeThick.setMinimumSize(20);
        
        
        //list of actions that define color...let it run indefinatly
        color = new ActionList(Activity.INFINITY);
        color.add(fill);
        color.add(dcaEdges);
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
        layout = new ActionList(Activity.INFINITY);
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
        
        repaint = new ActionList();
        repaint.add(new RepaintAction());
        

        
        
        vis.putAction("color", color);
        vis.putAction("layout", layout);
//        vis.putAction("repaint", repaint);
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
        
        //set up graph and set all columns
        if(init)
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
        else
        {
            vis.cancel("color");
            vis.cancel("layout");
            vis.cancel("repaint");
            vis.removeAction("color");
            graph.clear();
            
        }
        
        //clear the graph every time we call this function
//        graph.clearSpanningTree();
//        graph.
       

        
        if(init)
        {
            generateComputeStates(tree.initialNode, null, true);
        }
        
            
        

        
        visibleStates = new ArrayList();
        undoStates = new ArrayList(); /////unimplemented at the moment
        visibleActions = new ArrayList();
        
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
        


        //below lines could be combined into generateVisibleStateAndVisibleActions
        double optimalReward = thisController.getRewardWithCA(tree.actionsTaken);
        double largestAllowedReward = optimalReward * (1 +(degredation/100));
        for(int i = 0; i < computableStates.size(); i++)
        {
            if(computableStates.get(i).equals(lastComputeState))
            {
                System.out.println("adding last compute");
            }
            List<State> onePath = new ArrayList();
            List<CriteriaAction> oneActionPath = new ArrayList();
            List<Boolean> canStateUndo = new ArrayList(); //unimplemented at the moment
            for(int j = 0; j < computableStates.get(i).prevStates.size(); j++)
            {
                onePath.add(computableStates.get(i).prevStates.get(j).s);
                canStateUndo.add(Boolean.FALSE);
            }
            onePath.add(computableStates.get(i).thisState.s);
            
            if(computableStates.get(i).validEa)
            {
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
                    onePath.add(computableStates.get(i).ea.stateSequence.get(j));
                }
            }

            for(int j = 0; j < computableStates.get(i).prevActions.size(); j++)
            {
                oneActionPath.add(computableStates.get(i).prevActions.get(j));
            }
            
            if(computableStates.get(i).validEa)
            {
                for(int j = 0; j < computableStates.get(i).ea.actionSequence.size();j++)
                {
                    String actName = computableStates.get(i).ea.actionSequence.get(j).actionName();
                    CriteriaAction act = definedActions.get(actName);
                    oneActionPath.add(act);
                }
            }

            
            
            //will soon implement the compute states to not include anything above degredataion 
            //which will remove the need for this if
            if(degredation >= 0)
            {
                //below are print statements that allow the programmer to see which paths were included
                double thisReward = thisController.getRewardWithCA(oneActionPath);
                boolean containRemovedConnections = false;
                
                if(thisReward >= largestAllowedReward && !containRemovedConnections)
                {
                    visibleStates.add(onePath);
                    visibleActions.add(oneActionPath);
                    undoStates.add(canStateUndo);
                }
            }
            else
            {
                visibleStates.add(onePath);
                visibleActions.add(oneActionPath);
                undoStates.add(canStateUndo);
            }

            

        }

       
//        visibleStates.add(tree.statesTaken);
//        visibleActions.add(tree.actionsTaken);
        
        
        setUpNodes(init);
        setPrefuseNodeConnections();
        setDefaultEdgeData();
        handleSubPathToTarget();
        handlePathToTarget();
        if(!init)
        {
            setStatePallette(false);
            setEdgePallette(false);
        }
       
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
//            System.out.println("reward is " + thisController.getRewardWithCA((CriteriaAction) item.get("CriteriaAction")));
            edgeItems.add(item);//this is used by dataDisplay later when we make buttons(When user clicks the button we want it to be 
                                //as if the user actually clicked that edge).
        }
        //give every node black stroke
        Iterator allNodes = graph.nodes();
        while(allNodes.hasNext())
        {
            Node tempNode = (Node) allNodes.next();
            VisualItem item = vis.getVisualItem("graph.nodes", tempNode);
            item.setStrokeColor(ColorLib.rgb(0, 0, 0));
            item.setStroke(new BasicStroke(0));
            if(item.getBoolean("CurrentState")) vis.getVisualItem("graph.nodes", tempNode).setStroke(new BasicStroke(10));
        }
        
        
//        for(int i = 0; i < graph.getEdgeCount(); i++)
//        {
//            vis.getVisualItem("graph.edges", graph.getEdge(i)).setSize(5);
//            vis.getVisualItem("graph.nodes", graph.getNode(i)).setSize(500);
//        }
        
    }
    /**
     * create nodes for each state
     */
    private void setUpNodes(boolean init)
    {
        State startingPoint = tree.initialState;
        
        if(init)    graph.addRoot().set("CurrentState", true); //this adds the root and sets a needed column
        else      graph.addRoot();
        Node n = graph.getRoot();
        
        
        
        //optimal path loop
        for(int i = 0; i < tree.statesTaken.size(); i++)
        {
            n.set("type", "node");
            n.set("state", tree.statesTaken.get(i).getCompleteStateDescription()); 
            n.set("stateClass", tree.statesTaken.get(i));       
            double stateValueFunction = thisController.getV(tree.statesTaken.get(i));
            double finalStateValueFunction = (double) Math.round(stateValueFunction * 100000) / 100000; //the math.round goes to 5 places(up to the decimal point)
            n.set("StateReward", finalStateValueFunction);
            nodes.add(n);
            Node temp = n;
                
            //indexing required this type of statement since I wanted the last node to NOT have an edge
            //hence it being a "final" state
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
        //end optimal path loop
        
        
        //paths other than the otimal one
        //visibleStates.get(i) represents a list of states that represent one path
        //visibleAction.get(i) represents a list of actions that represent one path
        //both lists are parrell meaning the to go from state 1 to 2 action 1 was applied and so forth
        for(int i = 0; i < visibleStates.size(); i++)
        {
            n = graph.getRoot();
            State prevState = null;
            int numOfNodes = visibleStates.get(i).size();
//            System.out.println("visi size " + visibleStates.get(i).size());
            for(int j = 0; j < numOfNodes;j++)
            { 
                //this prevents "hidden" paths from being produced
                //the break stops the production of the path and moves to the next path
                if(temporaryHiddenStates.contains(visibleStates.get(i).get(j)))
                {
                    break;
                }
                
                
                boolean flag = false; //flag is whether a state in visible states already has a node dedicated
                                      //to representing that state.  This helps to make edges point to that
                                      //node rather than creating another node for the same state
                Iterator nodeIterator = graph.nodes();
                while(nodeIterator.hasNext())
                {
                    Node test = (Node) nodeIterator.next();
                    if(test.get("stateClass").equals(visibleStates.get(i).get(j))) //if its found set flag to true
                    {
                        flag = true; //copy was found
//                        System.out.println(test.get("stateClass"));
                    }
                    if(flag)//handle the copy
                    {
                        Edge e = graph.getEdge(n, test); //if e is not null it means this case has been handled before
                                                         //although this case seems rare it happens sometimes
                        if(e == null && prevState != null)
                        {
                            boolean add = true; //this flag is used to determine whether the state can be added based
                                                //on if that edge has been hidden by the user
                            
                            //the below loop just checks to make sure we are not adding an edge that the user has hidden
                            for(int a = 0; a < temporaryHiddentStateNodes.size(); a++)
                            {
                                if(temporaryHiddentStateNodes.get(a).s.equals(prevState) 
                                        && temporaryHiddentStateNodes.get(a).connections.get(0).action.equals(visibleActions.get(i).get(j-1)))
                                {
                                    add = false;
                                }
                            }
                            if(add)
                            {
                                e = graph.addEdge(n, test);
                                e.set("srcState", prevState);//set the edges source state
                                e.set("resultState", visibleStates.get(i).get(j));//and target state
                                e.set("action", visibleActions.get(i).get(j-1).getName());
                                e.set("CriteriaAction", visibleActions.get(i).get(j-1));
                                e.set("ActionName", visibleActions.get(i).get(j-1).getName());
                                e.set("reward", thisController.getRewardWithCA(visibleActions.get(i).get(j-1)));
                            }
                        }
                        flag = false;
                        j++;  //since we handled a state go ahead and increment j
                              //I would have liked to have used continue but since
                              //we are looping over the graph and not visibleStates
                              //continue would not have the desired effect
                              //the following if statement handles cases where j goes over 
                              //visibleState.size()
                        if(j >= visibleStates.get(i).size())//if we go over break
                        {
                            break;
                        }

                        prevState = visibleStates.get(i).get(j - 1);
                        n = test;
                        nodeIterator = graph.nodes(); //start the iteration again to check the WHOLE graph for the next state
                    }
                }
                //another checker in case j gets incremented to much
                if(j >= visibleStates.get(i).size())
                        break;
                
                //if this state was hidden stop here and just go to the next j
                if(temporaryHiddenStates.contains(visibleStates.get(i).get(j)))
                {
                    //idk whether break or continue is best will come back later
                    break;
//                    continue;
                }
                
                


                    boolean add = true; //this checks for indiv edges(such as that between a non-optimal node
                                        //to an optimal one where if hide branch was used it would have no node
                                        //to hide but just a single edge.  This is the fix for that
                    for(int a = 0; a < temporaryHiddentStateNodes.size(); a++)
                    {
                        if(temporaryHiddentStateNodes.get(a).s.equals(prevState) 
                            && temporaryHiddentStateNodes.get(a).connections.get(0).action.equals(visibleActions.get(i).get(j-1)))
                        {
                            add = false;
                        }
                                
                    }
                
                    
                Node temp = n; //temp keeps up with n's last node value
                if(j == 0 || add) //if this is the first or is supposed to be added then do add
                {
//                    System.out.println("we adding");
                    n = graph.addChild(n);
                }
                else
                {
                    break;
                }
                

//                if(j == 0)
//                {
//                    Edge edge = graph.getEdge(temp, n);
//                    edge.set("srcState", startingPoint);//set the edges source state
//                    edge.set("resultState", visibleStates.get(i).get(j));//and target state
//                    edge.set("action", visibleActions.get(i).get(j).getName());
//                    edge.set("CriteriaAction", visibleActions.get(i).get(j));
//                    edge.set("ActionName", visibleActions.get(i).get(j).getName());
//                    edge.set("reward", thisController.getRewardWithCA(visibleActions.get(i).get(j)));
//                    System.out.println("action in j = 0: " + visibleActions.get(i).get(j).getName());
//                }
                //was else if
                if(j < visibleStates.get(i).size())
                {
                    Edge edge = graph.getEdge(temp, n);
                    edge.set("srcState", prevState);//set the edges source state
                    edge.set("resultState", visibleStates.get(i).get(j));//and target state
                    edge.set("action", visibleActions.get(i).get(j-1).getName());
                    edge.set("CriteriaAction", visibleActions.get(i).get(j-1));
                    edge.set("ActionName", visibleActions.get(i).get(j-1).getName());
                    edge.set("reward", thisController.getRewardWithCA(visibleActions.get(i).get(j-1)));
                }
                
                n.set("type", "node");
                n.set("state", visibleStates.get(i).get(j).getCompleteStateDescription()); 
                n.set("stateClass", visibleStates.get(i).get(j));  
                prevState = visibleStates.get(i).get(j);
                double stateValueFunction = thisController.getV(visibleStates.get(i).get(j));
                double finalStateValueFunction = (double) Math.round(stateValueFunction * 100000) / 100000; //the math.round goes to 5 places(up to the decimal point)
                n.set("StateReward", finalStateValueFunction);
                
                TerminalFunction tf = new Termination();

                if(tf.isTerminal(visibleStates.get(i).get(j)))
                {
                    n.set("nodeInfo", 0); //blue node
                }
                else if(visibleStates.get(i).get(j).equals(tree.initialState))
                {
                    n.set("nodeInfo", 1); //red node
                }
                else
                {
                    n.set("nodeInfo", 3); //pink node
                }
            }
        } 
    }
    /**
     * exactly what the function names says. It creates the connections between nodes in the graph.
     */
    private void setPrefuseNodeConnections()
    {
        int counter = 0;
        
        for(int i = 0; i < tree.actionsTaken.size(); i++)
        {
            Node n1 = graph.getNode(counter);
            Node n2 = graph.getNode(counter + 1);
          
            State currentState = tree.statesTaken.get(i);
            State nextState = tree.statesTaken.get(i + 1);
            
                TerminalFunction tf = new Termination();
                if(tf.isTerminal(nextState))
                {
                    n2.set("nodeInfo", 0); //blue node
                }
                if(currentState.equals(tree.initialState))
                {
                    n1.set("nodeInfo", 1); //red node
                }
                else
                {
                    n1.set("nodeInfo", 3); //pink node
                }
            counter++;
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
            e.set("inPath", 1);
        }
    }
    
    
    
    private void handleSubPathToTarget()
    {
        ComputeState cs = null;
        for(int i = 0; i < computableStates.size(); i++)
        {
            if(computableStates.get(i).validEa)
            {
                cs = computableStates.get(i);
                break;
            }
        }
        if(cs == null) return;
        else if(tree.statesTaken.contains(cs.thisState.s)) return;
        List<State> subOptimalPath = cs.convertToStateList();
        
        System.out.println("sizeof subPath " + subOptimalPath.size());
        
        int stateCounter = 0;
        int stop = subOptimalPath.size() - 1; //took off 1 for indexing and another 1 to stop edge at state before the final state
        for(int i = 0; i < graph.getEdgeCount(); i++)
        {
            Edge e = graph.getEdge(i);
//            System.out.println("in path is set to " + e.get("inPath"));
            if(e.getSourceNode().get("stateClass").equals(subOptimalPath.get(stateCounter))&&
                    e.getTargetNode().get("stateClass").equals(subOptimalPath.get(stateCounter + 1)))
            {
//                if(e.get("inPath").equals(0)) 
//                {
//                    System.out.println("what the crap " + e.get("inPath"));
//                    continue;
//                }
//                System.out.println("setting to 2");
                e.set("inPath", 2);
                i = 0;
                stateCounter++;
                if(stateCounter > stop) break;
            }
        }
    }
    
    /**
     * This function sets data needed for the nodes and edges from the initial state to the target state
     */
    private void handlePathToTarget()
    {
        for(int i = 0; i < tree.stateNodesTaken.size() - 1; i++)
        {
            //the following lines work because the indexes inside the graph
            //are the same as the indexes in the tree
            
            //The reason this is is because I added the optimal path first
            StateNode src = tree.stateNodesTaken.get(i);
            StateNode target = tree.stateNodesTaken.get(i+1);
            int indexSrc = i;
            int indexTarget = i+1;
            Node nodeSrc = graph.getNode(indexSrc);
            Node nodeTarget = graph.getNode(indexTarget);
            
            stateValueContainer.addStateValue(thisController.getV(src.s));
            actionValueContainer.addAction(src.s, target.s, src.checkIfResultState(target.s));
            
            if(!target.isTerminal()) nodeTarget.set("nodeInfo", 2); //purple nodes
            if(!nodeSrc.equals(nodeTarget)) //if the result state is the same as the src then nothing needs to happen
            {
                Edge e = graph.getEdge(nodeSrc, nodeTarget); //get the edge between the nodes
            
                if(e != null) //and change it accordingly
                {
                e.set("inPath", 0);  
                e.set("weight", 1.0);
                edges.add(e); //edges is a list of the edges from initial to target
                }
            }
                        
        }//end of loop
        stateValueContainer.addStateValue(0); //final state has 0 value
    }
    
    //below function was an early attempt at looking at state tree
    private void generateComputeStates(ComputeState prevState)
    {
        for(int i = 0; i < prevState.thisState.connections.size(); i++)
        {
            for(int j = 0; j < prevState.thisState.connections.get(i).nodes.size(); j++)
            {
                StateNode nextNode = findInTree(prevState.thisState.connections.get(i).nodes.get(0).s);
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
                generateComputeStates(nextState);
            }
        }
        
    }
    
    public void addComputeState(StateNode initState, CriteriaAction prevAction, State prevState)
    {
        ComputeState lastComputeState = null; //this covering up of lastComputeState is on purpose
                                              //note that in this function the global lastComputeState
                                              //is not used but i liked the name and couldn't come
                                              //up with a better name;
        int visibleStateNum = 0; //index in visibleState that the prevState and initState.s are found
        int endIndex = 0; //index of initState in the sublist at [visibleStateNum]
        for(int i = 0; i < computableStates.size(); i++)
        {
            if(visibleStates.get(i).contains(initState.s) && visibleStates.get(i).contains(prevState))
            {
                visibleStateNum = i;
                endIndex = visibleStates.get(i).indexOf(initState.s);
            }
        }
        
        for(int i = 0; i < initState.connections.size(); i++)
        {
            for(int j = 0; j < initState.connections.get(i).states.size(); j++)
            {
                boolean found = false;
                ComputeState newCompute = new ComputeState();
                ComputeState oldCompute = findInAlreadyComputedStates(initState.connections.get(i).nodes.get(j));
                if(oldCompute != null)
                {
//                    newCompute = findInAlreadyComputedStates(initState.connections.get(i).nodes.get(j));
//                    System.out.println("got new Compute from old states");
                    found = true;

                    
                }
//                else
                {
                    //the below lines are filling prevStates and prevActions
                    //----------------------------------------------------------------------
                    for(int k = 0; k < endIndex; k++)
                    {
                        newCompute.prevActions.add(visibleActions.get(visibleStateNum).get(k));
                    }
                    for(int k = 0; k < endIndex; k++)
                    {
                        StateNode node = null;
                        for(int l = 0; l < tree.nodes.size(); l++)
                        {
                            if(tree.nodes.get(l).s.equals(visibleStates.get(visibleStateNum).get(k)))
                            {
                                node = tree.nodes.get(l);
                            }
                        }
                        newCompute.prevStates.add(node);
                    }
                
                    newCompute.prevActions.add(initState.connections.get(i).action);
                    newCompute.prevStates.add(initState);
                    //------------------------------------------
                    //end filling prevStates and prevActions
                
                
                
                    newCompute.thisState = initState.connections.get(i).nodes.get(j);
                
                    if(found == false)
                    {
                        newCompute.ea = thisController.getOptimalPathFrom(initState.connections.get(i).states.get(j));
                        newCompute.ea.stateSequence.remove(0); //the first stateSequence is the current state which is already included so get rid of it
                        for(int a = 0; a < newCompute.prevActions.size(); a++)
                        {
                            System.out.print(newCompute.prevActions.get(a).getName() + "  ");
                        }
                        for(int a = 0; a < newCompute.ea.actionSequence.size(); a++)
                        {
                            System.out.print(newCompute.ea.actionSequence.get(a).actionName() + "  ");
                        }
                    
                    }
                    else
                    {
//                        System.out.println("we mergeing");
                        mergeOldComputetoNewCompute(newCompute, oldCompute);
//                        newCompute.ea = new EpisodeAnalysis();
                    }
                    
                }
                
                
                newCompute.validEa = false;


                //get reward of proposed path
                //I had to create a new list proposedPath so that I could group all
                //the actions up without messing up the integrity of each seperate list
                //in the compute state.  If only java provided a easy to use deep copy method.....sigh.....
                List<CriteriaAction> proposedPath = new ArrayList();
                for(int k = 0; k < newCompute.prevActions.size(); k++)
                {
                    proposedPath.add(newCompute.prevActions.get(k));
                }
//                EpisodeAnalysis tempAnalysis = thisController.getOptimalPathFrom(newCompute.thisState.s);
                for(int k = 0; k < newCompute.ea.actionSequence.size(); k++)
                {
                    CriteriaAction plannedAct = thisController.getActionMap().get(newCompute.ea.actionSequence.get(k).actionName());
                    proposedPath.add(plannedAct);
                }
                
                double totalReward = thisController.getRewardWithCA(proposedPath);
                
                double optimalReward = thisController.getRewardWithCA(tree.actionsTaken);
                double largestAllowedReward = optimalReward * (1 +(degredation/100));
                
//                System.out.println("this reward " + totalReward + "  and largeset " + largestAllowedReward);
                
                if(totalReward > largestAllowedReward || this.degredation < 0)
                {
//                    System.out.println("added that state");
                    if(!computableStates.contains(newCompute) || found && !checkForExistInTree(newCompute.thisState))
                    {
//                        System.out.println("we still added");
                        computableStates.add(newCompute);
                    }

                    
//                    System.out.println("did we add");
                }
                if(!oldComputeStates.contains(newCompute))
                {
//                    System.out.println("we added to oldStates");
                    oldComputeStates.add(newCompute);
                }
            }
        }
        System.out.println();
        System.out.println();
        System.out.println();
    }
    
    private void mergeOldComputetoNewCompute(ComputeState newCompute, ComputeState oldCompute)
    {
        if(newCompute.thisState.equals(oldCompute.thisState)) 
        {
            newCompute.ea = oldCompute.ea;
            newCompute.thisState = oldCompute.thisState;
//            newCompute = oldCompute;
            System.out.println("using the same ea");
        } //nothing to do
        
        else if(oldCompute.ea.stateSequence.contains(newCompute.thisState.s))
        {
            int stateStartIndex = oldCompute.ea.stateSequence.indexOf(newCompute.thisState.s) + 1;
            int actionStartIndex = stateStartIndex;
            for(int i = stateStartIndex; i < oldCompute.ea.stateSequence.size(); i++)
            {
                newCompute.ea.stateSequence.add(oldCompute.ea.stateSequence.get(i));
            }
            for(int i = actionStartIndex; i < oldCompute.ea.actionSequence.size(); i++)
            {
                newCompute.ea.actionSequence.add(oldCompute.ea.actionSequence.get(i));
            }
        }
//        System.out.println("action list for this state");
//        for(int i = 0; i < newCompute.prevActions.size(); i++)
//        {
//            System.out.print(newCompute.prevActions.get(i).getName() + "  ");
//        }
//        for(int i = 0; i < newCompute.ea.actionSequence.size(); i++)
//        {
//            System.out.print(newCompute.ea.actionSequence.get(i).actionName() + "  ");
//        }
        
    }
    
    
    
    //When degredation is set you need to work on how they are viewable.
    //the problem is when you are expanding nodes....which is in addComputeState.
    //man you are tired.
    public void generateComputeStates(StateNode initState, CriteriaAction prevAction, boolean changeCurrentState) 
    {
        
        for(int i = 0; i < oldComputeStates.size(); i++)
        {
            oldComputeStates.get(i).validEa = false;
        }
        if(changeCurrentState)
        {
            computableStates = new ArrayList<>();
        }
        
//        System.out.println(initState.s);
        
        //if null we set lastComputeState to initialState and ea to the very first ea
        if(lastComputeState == null) 
        {
            lastComputeState = new ComputeState();
            lastComputeState.thisState = initState;
            lastComputeState.ea = thisController.getEpisodeAnalysis();
            List<State> stateSeq = new ArrayList();
            List<GroundedAction> actionSeq = new ArrayList();
            for(int i = 1; i < lastComputeState.ea.stateSequence.size(); i++)
            {
                stateSeq.add(lastComputeState.ea.stateSequence.get(i));
            }
            for(int i = 0; i < lastComputeState.ea.actionSequence.size(); i++)
            {
                actionSeq.add(lastComputeState.ea.actionSequence.get(i));
            }
            lastComputeState.ea.stateSequence = stateSeq;
            lastComputeState.ea.actionSequence = actionSeq;
        }
        else if(changeCurrentState)
        {
//            ComputeState potComputeState = findInAlreadyComputedStates(initState);
//            System.out.println(potComputeState);
            
            lastComputeState.prevStates.add(lastComputeState.thisState);
            lastComputeState.prevActions.add(prevAction);
            lastComputeState.thisState = initState;
            
//            if(potComputeState != null) 
//            {
//                mergeOldComputetoNewCompute(lastComputeState, potComputeState);
//                System.out.println("we merged actually");
//            }
            
            if(!lastComputeState.ea.stateSequence.isEmpty() && lastComputeState.thisState.s.equals(lastComputeState.ea.stateSequence.get(0)))
            {
                lastComputeState.ea.stateSequence.remove(0);
                lastComputeState.ea.actionSequence.remove(0);
                System.out.println("staying on course");
            }
            
            else if(tree.stateNodesTaken.contains(initState))
            {
                System.out.println("we are in a tree node dont compute");
                lastComputeState.ea = new EpisodeAnalysis(); //the optimal path is always shown so no need for
                                                             //a real EpisodeAnaylsis
            }
            
            else
            {
                System.out.println("O MY GOD WE ARE RECOMPUTING");
//                if(findInAlreadyComputedStates(initState) != null) System.out.println(findInAlreadyComputedStates(initState).thisState.s);
                lastComputeState.ea = thisController.getOptimalPathFrom(initState.s);//this line must change
                lastComputeState.ea.stateSequence.remove(0);
//                lastComputeState.validEa = true;
//                lastComputeState.validEa = true;
//                computableStates.add(lastComputeState);
            }
        }
//        if(changeCurrentState)
//        {
//            computableStates.add(lastComputeState);
//            oldComputeStates.add(lastComputeState);
////            lastComputeState.validEa = true;
//        }
        
        computableStates.add(lastComputeState);
        oldComputeStates.add(lastComputeState);
        lastComputeState.validEa = true;
        
        
        
//        GroundedAction nextOptimalAction = null;
//        State nextOptimalState = null;
//        nextOptimalAction = lastComputeState.ea.actionSequence.get(0);
//        nextOptimalState = lastComputeState.ea.stateSequence.get(0);
//        
//        EpisodeAnalysis optimalEAForCurrentState = thisController.getOptimalPathFrom(initState.s);
//        if(optimalEAForCurrentState.stateSequence.size() > 2)
//        {
//            optimalEAForCurrentState.stateSequence.remove(0);
//            optimalEAForCurrentState.stateSequence.remove(0);
//            
//            optimalEAForCurrentState.actionSequence.remove(0);
//        }
//        nextOptimalAction = optimalEAForCurrentState.actionSequence.get(0);
//        nextOptimalState = optimalEAForCurrentState.stateSequence
        
        
        
        for(int i = 0; i < initState.connections.size(); i++)
        {
            for(int j = 0; j < initState.connections.get(i).states.size(); j++)
            {
                ComputeState newCompute = new ComputeState();
//                ComputeState oldCompute = findInAlreadyComputedStates(initState.connections.get(i).nodes.get(j));
//                if(oldCompute != null) oldCompute.validEa = true;
                
                for(int k = 0; k < lastComputeState.prevActions.size(); k++)
                {
                    newCompute.prevActions.add(lastComputeState.prevActions.get(k));
                }
                for(int k = 0; k < lastComputeState.prevStates.size(); k++)
                {
                    newCompute.prevStates.add(lastComputeState.prevStates.get(k));
                }
                
                
                CriteriaAction additionalAct = initState.connections.get(i).action;
                newCompute.prevActions.add(additionalAct);
                newCompute.prevStates.add(initState);
                newCompute.thisState = initState.connections.get(i).nodes.get(j);

                
                newCompute.ea = thisController.getOptimalPathFrom(newCompute.thisState.s);
                newCompute.ea.stateSequence.remove(0);
                newCompute.validEa = false;
                

                
//                if(oldCompute != null)
//                {
//                    mergeOldComputetoNewCompute(newCompute, oldCompute);
//                }
                
                
                double optimalReward = thisController.getRewardWithCA(tree.actionsTaken);
                double largestAllowedReward = optimalReward * (1 +(degredation/100));
                
                List<CriteriaAction> proposedPath = new ArrayList();
                for(int k = 0; k < newCompute.prevActions.size(); k++)
                {
                    proposedPath.add(newCompute.prevActions.get(k));
                }
                for(int k = 0; k < newCompute.ea.actionSequence.size(); k++)
                {
                    CriteriaAction plannedAct = thisController.getActionMap().get(newCompute.ea.actionSequence.get(k).actionName());
                    proposedPath.add(plannedAct);
                }
                
                
                double proposedReward = thisController.getRewardWithCA(proposedPath);
                
//                System.out.println("this reward  " + proposedReward + "   largest " + largestAllowedReward);
                
                if(proposedReward > largestAllowedReward)
                {
                    computableStates.add(newCompute);
//                    if(oldCompute == null) 
//                    {
//                        computableStates.add(newCompute);
////                        System.out.println("hey newCompute" + newCompute.thisState.s);
//                    }
//                    else
//                    {
//                        computableStates.add(oldCompute);
//                        System.out.println("we pulled out of olde" + oldCompute.thisState.s);
//                    }
                    
                }
                else if(this.degredation < 0)
                {
//                    if(oldCompute == null) 
                    {
//                        System.out.println("we added newer");
                        computableStates.add(newCompute);
                    }
//                    else 
//                    {
//                        System.out.println("we added older");
//                        computableStates.add(oldCompute);
//                    }
                }
            }
        }
        
//        System.out.println("checking lastComputeState" + lastComputeState.validEa + " sizeof ea " + lastComputeState.ea.stateSequence.size());
        
//        for(int i = 0; i < computableStates.size(); i++)
//        {
////            System.out.println(computableStates.get(i).thisState.s);
//        }
    }
    
    private ComputeState findInAlreadyComputedStates(StateNode s)
    {
        boolean inThisState = false;
        boolean inPrevStates = false;
        boolean inEA = false;
        int index = 0;
        int subIndex = 0;
        
        for(int i = 0; i < oldComputeStates.size(); i++)
        {
            ComputeState testState = oldComputeStates.get(i);
            if(testState.thisState.equals(s))
            {
                inThisState = true;
                index = i;
                break;
            }
            
            else if(testState.prevStates.contains(s))
            {
                inPrevStates = true;
                index = i;
                subIndex = testState.prevStates.indexOf(s);
                break;
            }
            else if(testState.ea.stateSequence.contains(s.s))
            {
                System.out.println("we found it in ea");
                inEA = true;
                index = i;
                subIndex = testState.ea.stateSequence.indexOf(s.s);
                break;
            }
        }
        
        ComputeState newCompute = new ComputeState();
        
        List<StateNode> newPrevStates = new ArrayList<StateNode>();
        List<CriteriaAction> newPrevActions = new ArrayList<>();
        List<State> newEaState = new ArrayList<>();
        List<GroundedAction> newEaAction = new ArrayList<>();
        
        if(inThisState) return oldComputeStates.get(index);
//        else if(inPrevStates)
//        {
//            System.out.println("do we really ever hit this?");
//             return oldComputeStates.get(index);
//        }
        else if(inEA)
        {
            for(int i = 0; i < chosenStates.size() - 1; i++)
            {
                for(int j = 0; j < tree.nodes.size(); j++)
                {
                    if(chosenStates.get(i).equals(tree.nodes.get(j).s))
                    {
                        newCompute.prevStates.add(tree.nodes.get(j));
                    }
                }
                
            }
            for(int i = 0; i < tree.nodes.size(); i++)
            {
                if(chosenStates.size() > 0)
                {
                    if(chosenStates.get(chosenStates.size() - 1).equals(tree.nodes.get(i).s))
                    {
                        newCompute.thisState = tree.nodes.get(i);
                    }
                }

            }
            for(int i = 0; i < chosenActions.size(); i++)
            {
                newCompute.prevActions.add(chosenActions.get(i));
            }
            
            newCompute.ea = new EpisodeAnalysis();
            for(int i = subIndex; i < oldComputeStates.get(index).ea.stateSequence.size(); i++)
            {
                newCompute.ea.stateSequence.add(oldComputeStates.get(index).ea.stateSequence.get(i));
            }
            
            for(int i = subIndex; i < oldComputeStates.get(index).ea.actionSequence.size(); i++)
            {
                newCompute.ea.actionSequence.add((GroundedAction) oldComputeStates.get(index).ea.actionSequence.get(i));
            }
            
            
            double optimalReward = thisController.getRewardWithCA(tree.actionsTaken);
            double largestAllowedReward = optimalReward * (1 +(degredation/100));
            double thisReward = getRewardForComputeState(newCompute);
            
            if(thisReward > largestAllowedReward) 
            {
                return newCompute;
            }
            else
            {
                return null;
            }
        }
        
        
        return null;
    }
    
    
    private double getRewardForComputeState(ComputeState compute)
    {
        List<CriteriaAction> actions = new ArrayList();
        for(int i = 0; i < compute.prevActions.size(); i++)
        {
            actions.add(compute.prevActions.get(i));
        }
        for(int i = 0; i < compute.ea.actionSequence.size(); i++)
        {
            CriteriaAction act = thisController.getActionMap().get(compute.ea.actionSequence.get(i).actionName());
            actions.add(act);
        }
        return thisController.getRewardWithCA(actions);
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

    void rehighlightPath(boolean changeCurrentState, boolean removeHiddenBan) 
    {
        if(removeHiddenBan)
        {
            temporaryHiddenStates = new ArrayList();
            temporaryHiddentStateNodes = new ArrayList<StateNode>();
        }
        for(int i = 0; i < graph.getNodeCount(); i++)
        {
            if(!graph.getNode(i).get("stateClass").equals(lastComputeState.thisState.s) && graph.getNode(i).getBoolean("CurrentState") && changeCurrentState)
            {
                graph.getNode(i).set("CurrentState", false);
                vis.getVisualItem("graph.nodes", graph.getNode(i)).setStroke(new BasicStroke(0));
//                vis.getVisualItem("graph.nodes", graph.getNode(0)).setStroke(new BasicStroke(0));
            }
            if(graph.getNode(i).get("stateClass").equals(lastComputeState.thisState.s))
            {
//                System.out.println("found it!!!");
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
//                            System.out.println("HIGHLIGHT " + graph.getEdge(graph.getNode(j), graph.getNode(k)).getString("ActionName"));
                            graph.getEdge(graph.getNode(j), graph.getNode(k)).set("weight", 200);
                        }
                    }
                }
            }
        }
    }
    
    private void setStatePallette(boolean init)
    {
        boolean pinkNode = false;
        for(int i = 0; i < graph.getNodeCount(); i++)
        {
            if(graph.getNode(i).get("nodeInfo").equals(3))
            {
                pinkNode = true;
                break;
            }
        }
        if(pinkNode) 
        {
            System.out.println("WE FOUND A PINk");
            s_pallette = Originals_pallette.clone();
        }
        else
        {
            s_pallette = new int[3];
            s_pallette[0] = Originals_pallette[0];
            s_pallette[1] = Originals_pallette[1];
            s_pallette[2] = Originals_pallette[2];
        }
        if(!init)
        {
            color.remove(fill);
            fill = new DataColorAction("graph.nodes", "nodeInfo", Constants.ORDINAL, VisualItem.FILLCOLOR, s_pallette);   
            fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255,255, 0));
            color.add(fill);
        }

    }
}