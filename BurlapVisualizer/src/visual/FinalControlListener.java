package visual;


import Tree.Connection;
import Tree.StateNode;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlapcontroller.actions.CriteriaAction;
import java.awt.BasicStroke;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableEdgeItem;
import prefuse.visual.tuple.TableNodeItem;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * This class is our handle to the user's mouse.  Based on what the user clicks there are specific things desired which are handled here.
 * @author Justin Lewis
 */
public class FinalControlListener extends ControlAdapter implements Control {
    

    public String ItemClicked;
    CriteriaAction clickedAction = null;
    
    DataDisplay dataDisplay;
//    NodeAndEdgeDataMan nodeAndEdgeDataMan;
    
    List<NodeItem> highlightedNodes;
    TableEdgeItem prevEdge;
    
    int prevnodeInfoNum;
    NodeItem prevNode;
    Chart chart;
    StateValueContainer chosenSVC;
    ActionValueContainer chosenAVC;
    JPopupMenu menu;
    NodeItem currentState;
    
    Visualizer thisVis;

    /**
     * All constructor does is initialize variables
     */
    public FinalControlListener(RewardFunction rf)
    {
        highlightedNodes = new ArrayList<>();
        chosenAVC = new ActionValueContainer(rf);
        chosenSVC = new StateValueContainer(chosenAVC);
        menu = new JPopupMenu();
    }
    
    /**
     * This function sets the {@link visual.DataDisplay} needed for the control listener.
     * <p>
     * The reason for having a dataDisplay in the control listener is so that it can be called
     * with update info whenever the user clicks a node or edge.
     * @param dataDisplay this is the handle to the window with all the info about the visualizer
     */
    public void setDataDisplay(DataDisplay dataDisplay)
    {
        this.dataDisplay = dataDisplay;
    }
    
    public void setChart(Chart chart)
    {
        this.chart = chart;
        chart.chosenAVC = chosenAVC;
        chart.chosenSVC = chosenSVC;
    }
    
    public void setVisualizer(Visualizer vis)
    {
        this.thisVis = vis;
    }
    
    /**
     * This function catches mouse clicks that are not on a node or edge.
     * <p>
     * This function basically just deselects whatever was previously selected by the user.
     * @param e The MouseEvent that created the event 
     */
    @Override
    public void mouseClicked(MouseEvent e)
    {
        itemClicked(null, e);
    }
    
    /**
     * This function handles the highlighting and selecting of nodes.
     * <p>
     * This function is always called when the mouse is clicked whether the user clicked on an item
     * (Which would cause this function to be called by prefuse) or by {@link #mouseClicked(java.awt.event.MouseEvent) }
     * (Which is called when the user clicks but not on an item).
     * @param item The item that was clicked(for the purpose of this program either edge or node.)
     * @param e The mouse event that caused the event(we don't care right click left click does the same thing)
     */
    @Override
    public void itemClicked(VisualItem item, MouseEvent e)
    {
        deSelectPrevItem();//always deSelect whatever was already selected
        
        if(item == null) //if item is null just update dataDisplay and get out of here
        {
            dataDisplay.setUpCharts("no action", null, null);
            return;
        }
        
        if(handleDuplicate(item)) return; //if the same item was handled just return(we just want to deselect if which was called by deSelectPrevItem                  
        
        
        if (item.get("type").equals("node") && SwingUtilities.isLeftMouseButton(e))//type node
        {  
            dataDisplay.setUpCharts("no action", (State) item.get("stateClass"), null); //update the table with this state
            TableNodeItem node = (TableNodeItem) item;//get the NodeItem
            handleNode(node);//and pass it to handleNode which will take care of the highlighting
        }

        
        else if(item.get("type").equals("edge") && SwingUtilities.isLeftMouseButton(e))//type edge
        {
            ItemClicked = item.getString("action"); //for dataDisplay a string of action name is needed
            dataDisplay.setUpCharts(ItemClicked, (State)item.get("srcState"), (State) item.get("resultState"));// update the table for the selected edge
            
//            System.out.println("We need to do some crap with" + item.get("resultState"));
            
            TableEdgeItem edge = (TableEdgeItem) item; //get the edgeItem
            handleEdge(edge);//handleEdge takes care of highlighting the edge
        }  
        
        else if(item.get("type").equals("node") && SwingUtilities.isRightMouseButton(e))
        {
//            System.out.println(currentState);
            List<CriteriaAction> actions = getPathFrom((State) currentState.get("stateClass"), (State) item.get("stateClass"));
//            System.out.println("According to actions");
            if(actions != null)
            {
                for(int i = 0; i < actions.size(); i++) 
                {
                    TupleSet edges = thisVis.vis.getGroup("graph.edges");
                    Iterator<Tuple> iter = edges.tuples();
                    while(iter.hasNext())
                    {
                        Tuple thisEdge = iter.next();
//                    System.out.println("thisEdge " + thisEdge);
//                    System.out.println("actions[" + i + "] " + actions.get(i));
                        if(thisEdge.get("CriteriaAction").equals(actions.get(i)) && thisEdge.get("srcState").equals(currentState.get("stateClass")))
                        {   
                            thisEdge.set("weight", 200);
                            System.out.println("executing " + thisEdge.get("ActionName"));

                        
                            State first = (State) thisEdge.get("srcState");
                            State second = (State) thisEdge.get("resultState");
//                        Edge edge = null;
                            for(int j = 0; j < thisVis.graph.getNodeCount(); j++)
                            {
//                            System.out.println("hery");
                                if(thisVis.graph.getNode(j).get("stateClass").equals(thisEdge.get("resultState"))) 
                                {
//                                    System.out.println("THIS IS A REQUIRED STATEMENT");
                                    currentState.setStroke(new BasicStroke(0));
                                    currentState.set("CurrentState", false);
                                    currentState = (NodeItem) thisVis.vis.getVisualItem("graph.nodes", thisVis.nodes.get(j));
                                    currentState.set("CurrentState", true);
                                    currentState.setStroke(new BasicStroke(10));
                                
                                
                                }
                            }
                        
//                        currentState = thisVis.vis.getVisualItem("graph.nodes", )
                            break;
//                        thisVis.vis.getVisualItem("graph.edges", thisEdge).set("weight", 200);
//                        currentState = thisVis.vis.getVisualItem("graph.nodes", )
                        }
                    }
//                    System.out.println(actions.get(i).getName());
                }
            }

        }
        
        else if(item.get("type").equals("edge") && SwingUtilities.isRightMouseButton(e) && e.isShiftDown())
        {
            try {
                handleHideEdge(item);
            } catch (ParseException ex) {
                Logger.getLogger(FinalControlListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        else if(item.get("type").equals("edge") && SwingUtilities.isRightMouseButton(e))
        {
            clickedAction = (CriteriaAction) item.get("CriteriaAction");

//            item.setStroke(new BasicStroke(10));
//            item.setEndFillColor(ColorLib.rgb(100, 100, 100));
            double weight = item.getFloat("weight");
            TableEdgeItem edge = (TableEdgeItem) item;


          
            if(weight == 200.0)
            {
                int inPath = (int) item.get("inPath");
//                int inPath = item.getInt("inPath");
                if(inPath == 1)
                {
                    item.set("weight", 20);
                }
                else
                {
                    item.set("weight", 1.0);
                }
            }
            else if(!(weight == 200.0) && edge.getSourceNode().getBoolean("CurrentState"))
            {
//                JPopupMenu menu = new JPopupMenu();
//                JMenuItem next = new JMenuItem("Execute action");
//                JMenuItem take = new JMenuItem("Take me to state");
//                next.addMouseListener(new MenuControlListener());
//                menu.add(next);
//                menu.setLocation(e.getXOnScreen(), e.getYOnScreen());
//                menu.setVisible(true);
                
                StateNode resultState = null;
                for(int i = 0; i < thisVis.tree.nodes.size(); i++)
                {
                    if(thisVis.tree.nodes.get(i).s.equals(edge.getTargetNode().get("stateClass")))
                    {
                        resultState = thisVis.tree.nodes.get(i);
                    }
                }
                
                System.out.println("executing " + clickedAction.getName());
                item.set("weight", 200.0);
                edge.getSourceItem().setStroke(new BasicStroke(0));
                edge.getSourceNode().set("CurrentState", false);
                edge.getTargetItem().setStroke(new BasicStroke(10));
                edge.getTargetNode().set("CurrentState", true);
                currentState = edge.getTargetItem();
                double actionReward = (double) edge.get("reward");
                double stateReward = (double) edge.getTargetNode().get("StateReward");
                
                
                chosenAVC.addAction((State) edge.getSourceNode().get("stateClass"), (State) edge.getTargetNode().get("stateClass"), clickedAction);
                double reward = (double) edge.getTargetNode().get("StateReward");
                chosenSVC.addStateValue(reward);

                chart.update();
                try {
                    if(thisVis.chosenStates.size() == 0)
                    {
                        thisVis.chosenStates.add((State) edge.getSourceNode().get("stateClass"));
                        thisVis.chosenStates.add((State) edge.getTargetNode().get("stateClass"));
                        
                    }
                    else
                    {
                        thisVis.chosenStates.add((State) edge.getTargetNode().get("stateClass"));
                    }
//                    thisVis.chosenEdges.add(edge);
                    thisVis.generateComputeStates(resultState, clickedAction);
                    thisVis.setUpData(false);
//                    thisVis.updateDisplay();
//                    thisVis.rebootVis();
                    
                    thisVis.setUpVisualization();
                    thisVis.setUpRenderers();
                    thisVis.setUpActions();
                    thisVis.updateDisplay();
                    thisVis.rehighlightPath();
//                chart.addStateValueAndAction(stateReward, actionReward, clickedAction.getName());
                
//                Edge edge = (Edge) item.get("Edge");
                } catch (ParseException ex) {
                    Logger.getLogger(FinalControlListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            
        }

    }
        
    //end public functions
    //----------------------------------------------------------------------
    //begin private functions
    
    
    private void handleHideEdge(VisualItem item) throws ParseException
    {
        prevNode = null;
                prevEdge = null;
                dataDisplay.setUpCharts("no action", null, null);
            System.out.println("we are hiding baby");
            State srcState = (State) item.get("srcState");
            StateNode srcStateNode;
            State resultState = (State) item.get("resultState");
            System.out.println("Src state is " + srcState.getCompleteStateDescription());
            CriteriaAction act = (CriteriaAction) item.get("CriteriaAction");
            System.out.println("action to hide is " + act.getName());

            
            
            
            
            
            for(int i = 0; i < thisVis.tree.nodes.size(); i++)
            {
                if(thisVis.tree.nodes.get(i).s.equals(srcState))
                {
                    srcStateNode = thisVis.tree.nodes.get(i);
                }
            }
            
            for(int i = 0; i < thisVis.tree.nodes.size(); i++)
            {
                if(thisVis.tree.nodes.get(i).s.equals(srcState))
                {
                    for(int j = 0; j < thisVis.tree.nodes.get(i).connections.size(); j++)
                    {
                        if(thisVis.tree.nodes.get(i).connections.get(j).action.equals(act))
                        {
                            for(int k = 0; k < thisVis.tree.nodes.get(i).connections.get(j).states.size(); k++)
                            {
                                if(thisVis.tree.nodes.get(i).connections.get(j).states.get(k).equals(resultState))
                                {
                                    System.out.println("removeing!");
                                    thisVis.removedConnections.add(thisVis.tree.nodes.get(i).connections.remove(j));
                                    if(j != 0) j--;
                                    break;
//                                    thisVis.tree.nodes.get(i).connections.get(j).states.remove(k);
//                                    thisVis.tree.nodes.get(i).connections.get(j).nodes.remove(k);
//                                    if(k != 0) k--;
//                                    if(thisVis.tree.nodes.get(i).connections.get(j).states.isEmpty())
//                                    {
//                                        Connection remove = thisVis.tree.nodes.get(i).connections.remove(j);
//                                        j--;
//                                        break;
//                                    }
                                    

                                }
                            }
                        }
                    }
                }
            }
            try {
            thisVis.setUpData(false);
            thisVis.setUpVisualization();
            thisVis.setUpRenderers();
            thisVis.setUpActions();
            thisVis.updateDisplay();
            
            for(int i = 0; i < thisVis.nodes.size(); i++)
            {
                if(thisVis.nodes.get(i).get("CurrentState").equals(true))
                {
                    System.out.println("ITS STILL HERE");
                    currentState = (NodeItem) thisVis.vis.getVisualItem("graph.nodes", thisVis.nodes.get(i));
                    System.out.println(currentState.getStroke().getLineWidth());
                    System.out.println("currentState = " + currentState.getClass());
                    currentState.setSize(10);
                    currentState.setStroke(new BasicStroke(10));
                    
                    thisVis.vis.getVisualItem("graph.nodes", thisVis.graph.getNode(0)).setStroke(new BasicStroke(10));
//                    thisVis.vis.getVisualItem("graph.nodes", thisVis.nodes.get(i)).setStroke(new BasicStroke(10));
                }
            }
//            thisVis.vis.getVis
//            thisVis.updateVisualization();
//            thisVis.setUpVisualization();
//            thisVis.setUpRenderers();
//            thisVis.setUpActions();
//            thisVis.updateDisplay();
//            thisVis.setUpVisualization();
            } catch (ParseException ex) {
                Logger.getLogger(FinalControlListener.class.getName()).log(Level.SEVERE, null, ex);
                }
    }
    
    private void deSelectPrevItem()
    {
        while(!highlightedNodes.isEmpty())//highlighting will change so get rid of here
        {                                //these nodes did not have their stroke size changed so there is no need to setStroke here
            highlightedNodes.get(0).setHighlighted(false);//set false
            highlightedNodes.remove(0);//pop off list
        }
            
            
        if(prevEdge != null)
        {
            prevEdge.setHighlighted(false);   //if prevEdge exists deHighlight it      
        }
        if(prevNode != null) //and some for node
        {                    //this node DID have it's Stroke changed so we need to set it back to zero
            prevNode.setHighlighted(false); 
            if(!prevNode.getBoolean("CurrentState"))
            {
                prevNode.setStroke(new BasicStroke((float) 0.0));//this makes a stroke of size 1(Despite the zero).
            }
            
        }
    }
    /**
     * This function checks for duplicate item and if it does handles getting rid of the previous pointers to items clicked.
     * @param item The visualItem that was clicked
     * @return true or false if it had to handle a case of a duplicate
     */
    private boolean handleDuplicate(VisualItem item)
    {
        NodeItem tempNode = null;
        EdgeItem tempEdge = null;
        
        if(item.get("type").equals("node")) tempNode = (TableNodeItem) item;        
        if(item.get("type").equals("edge")) tempEdge = (TableEdgeItem) item;


        if(tempNode != null)//if the item clicked is a node
        {
            if(tempNode.equals(prevNode))//and is the same as the prev Clicked node
            {
                //get rid of everything
                prevNode = null;
                prevEdge = null;
                dataDisplay.setUpCharts("no action", null, null);
                return true;
            }
        }
            
        else if(tempEdge != null)//same process for edge
        {
            if(tempEdge.equals(prevEdge))
            {
                prevNode = null;
                prevEdge = null;
                dataDisplay.setUpCharts("no action", null, null);
                return true;
            }
        }
        return false;//this statement is reached if both if and else if statement was false(meaning no duplicate)
    }
    
    /**
     * This function handles the highlighting of the node and it's children
     * @param node the clicked node
     */
    private void handleNode(TableNodeItem node)
    {
        if(node.get("CurrentState").equals(false))
        {
            node.setStroke(new BasicStroke(5));//This is the actual node clicked.  To make it different give it a thick stroke
        }


        node.setHighlighted(true);//and highlight it
        prevNode = node;//and then when this function is called again this will be the prev node

        Iterator<Edge> edgeIterator = node.edges();//every edge coming off this node
        while(edgeIterator.hasNext())
        {
            TableEdgeItem edge = (TableEdgeItem) edgeIterator.next();
            if(!edge.getTargetNode().equals((Node) node)) //we are only interested in the edges that point AWAY from the node(result states)
            {
                edge.getTargetItem().setHighlighted(true);//highlight
                highlightedNodes.add(edge.getTargetItem());//and add to the list so the next item clicked can dehighlight this
            }
        }
        prevEdge = null;//if there was an edge selected last set it to null
    }
    
    /**
     * very similar to operation to handleEdge but this handles highlighting the edges
     * @param edge the edge to highlight
     */
    private void handleEdge(TableEdgeItem edge)
    {
        //this code is very self explanitory.
        edge.setHighlighted(true);
        edge.getSourceItem().setHighlighted(true);
        edge.getTargetItem().setHighlighted(true);
        prevEdge = edge;
           
        highlightedNodes.add(edge.getSourceItem());
        highlightedNodes.add(edge.getTargetItem());
            
        prevNode = null;//if node was selected last go ahead and set that to null
    }
    
    private List<CriteriaAction> getPathFrom(State starting, State end)
    {
//        System.out.println("Staring" + starting.getCompleteStateDescription());
//        System.out.println("Ending" + end.getCompleteStateDescription());
        StateNode startingNode = null;
        for(int i = 0; i < thisVis.nodeList.size(); i++)
        {
            if(thisVis.nodeList.get(i).s.equals(starting)) 
            {
                startingNode = thisVis.nodeList.get(i);
                break;
            }
        }
//        thisVis.generateComputeStates(startingNode); NEED THIS TO WORK LATER
        List<CriteriaAction> actions = new ArrayList();
        for(int i = 0; i < thisVis.computableStates.size(); i++)
        {
            if(thisVis.computableStates.get(i).thisState.s.equals(end)) 
            {
                boolean flag = false;
                for(int j = 0; j < thisVis.computableStates.get(i).prevStates.size(); j++)
                {
                    if(thisVis.computableStates.get(i).prevStates.get(j).s.equals(starting)) flag = true;
                    if(flag)
                    {
                        actions.add(thisVis.computableStates.get(i).prevActions.get(j));
                    }
                }
//                System.out.println("sizeof this crap is " + actions.size());
                return actions;
            }
        }
        return null;
    }

}
