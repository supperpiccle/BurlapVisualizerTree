package visual;


import Tree.Connection;
import Tree.StateNode;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlapcontroller.actions.CriteriaAction;
import java.awt.BasicStroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.PopupMenuListener;
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
        if(dataDisplay.shouldIgnoreDegredation()) thisVis.degredation = -1;
        
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
//        
//        else if(item.get("type").equals("node") && SwingUtilities.isRightMouseButton(e) && e.isShiftDown())
//        {
//            expandSelectedNode(item);
//        }
        
        else if(item.get("type").equals("node") && SwingUtilities.isRightMouseButton(e))
        {

            
            JPopupMenu popup = new JPopupMenu();
//            popup.setLocation(e.getXOnScreen(), e.getYOnScreen());
            JMenuItem gotoStateMenuItem = new JMenuItem("Go to state");
            JMenuItem expandMenuItem = new JMenuItem("expand");

            ActionListener gotoState = new ActionListener() 
            {
                @Override
                public void actionPerformed(ActionEvent e) 
                {
                    goToState(item);
                }
            };
            
            ActionListener expand = new ActionListener() 
            {
                @Override
                public void actionPerformed(ActionEvent e) 
                {
                    expandSelectedNode(item);
                }
            };
            
            gotoStateMenuItem.addActionListener(gotoState);
            expandMenuItem.addActionListener(expand);
            popup.add(gotoStateMenuItem);
            popup.add(expandMenuItem);
//            MenuControlListener mcl = new MenuControlListener();
//            popup.addMouseListener(mcl);
            
//            thisVis.d.addMouseListener(mcl);
            thisVis.panel.add(popup);
            popup.show(e.getComponent(), e.getX(), e.getY());
//            return;
            
            
            
        }
        
        else if(item.get("type").equals("edge") && SwingUtilities.isRightMouseButton(e))
        {
            JPopupMenu popup = new JPopupMenu();
//            popup.setLocation(e.getXOnScreen(), e.getYOnScreen());
            JMenuItem executeMenuItem = new JMenuItem("Execute action");
            JMenuItem hideMenuItem = new JMenuItem("Hide Branch");

            ActionListener execute = new ActionListener() 
            {
                @Override
                public void actionPerformed(ActionEvent e) 
                {
                    ChangeCurrentState(item, true);
                }
            };
            
            ActionListener hideBranch = new ActionListener() 
            {
                @Override
                public void actionPerformed(ActionEvent e) 
                {
                    try 
                    {
                        handleHideEdge(item);
                    } 
                    catch (ParseException ex) 
                    {
                        Logger.getLogger(FinalControlListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            
            executeMenuItem.addActionListener(execute);
            hideMenuItem.addActionListener(hideBranch);
            popup.add(executeMenuItem);
            popup.add(hideMenuItem);
            MenuControlListener mcl = new MenuControlListener();
            popup.addMouseListener(mcl);
            
//            thisVis.d.addMouseListener(mcl);
            thisVis.panel.add(popup);
            popup.show(e.getComponent(), e.getX(), e.getY());
//            return;
        }

    }
        
    //end public functions
    //----------------------------------------------------------------------
    //begin private functions
    
    private void expandSelectedNode(Tuple item)
    {
        if(item.get("stateClass").equals(thisVis.tree.initialState)) return; //the inital state is already as expanded as its gonna get
        StateNode clickedState = null;
        for(int i = 0; i < thisVis.tree.nodes.size(); i++)
        {
            if(item.get("stateClass").equals(thisVis.tree.nodes.get(i).s))
            {
                clickedState = thisVis.tree.nodes.get(i);
            }
        }
        State currentStateClass = (State) currentState.get("stateClass");
        for(int i = 0; i < thisVis.graph.getNodeCount(); i++)
        {
            if(thisVis.graph.getNode(i).get("stateClass").equals(item.get("stateClass")))
            {
                CriteriaAction action = (CriteriaAction) thisVis.graph.getNode(i).getParentEdge().get("CriteriaAction");
                State prevStateToClicked = (State) thisVis.graph.getNode(i).getParent().get("stateClass");
//                  thisVis.generateComputeStates(clickedState, action, true);
                thisVis.addComputeState(clickedState, action, prevStateToClicked);
//                    thisVis.generateComputeStates(clickedState, action, false);
                    
                try {
                    thisVis.setUpData(false);
                    thisVis.updateVisualization();
                    thisVis.rehighlightPath(false, false);
                    prevNode = null;
                    prevEdge = null;
                    thisVis.rehighlightPath(true, false);
                    this.resetCurrentState();
                }
                catch (ParseException ex) {
                    Logger.getLogger(FinalControlListener.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
        }
    return;
    }
    
    private void ChangeCurrentState(Tuple item, boolean updateGraph)
    {
            clickedAction = (CriteriaAction) item.get("CriteriaAction");

//            item.setStroke(new BasicStroke(10));
//            item.setEndFillColor(ColorLib.rgb(100, 100, 100));
            double weight = item.getFloat("weight");
            TableEdgeItem edge = (TableEdgeItem) item;


          
//            if(weight == 200.0)
//            {
//                int inPath = (int) item.get("inPath");
////                int inPath = item.getInt("inPath");
//                if(inPath == 1)
//                {
//                    item.set("weight", 20);
//                }
//                else
//                {
//                    item.set("weight", 1.0);
//                }
//            }
            if(!(weight == 200.0) && edge.getSourceNode().getBoolean("CurrentState"))
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
                currentState = edge.getTargetItem();
                double actionReward = (double) edge.get("reward");
                double stateReward = (double) edge.getTargetNode().get("StateReward");
                
                
                chosenAVC.addAction((State) edge.getSourceNode().get("stateClass"), (State) edge.getTargetNode().get("stateClass"), clickedAction);
                double reward = (double) edge.getTargetNode().get("StateReward");
                chosenSVC.addStateValue(reward);
                chart.update();
                try 
                {
                    if(thisVis.chosenStates.isEmpty())
                    {
                        thisVis.chosenStates.add((State) edge.getSourceNode().get("stateClass"));
                        thisVis.chosenStates.add((State) edge.getTargetNode().get("stateClass"));
                    }
                    else
                    {
                        thisVis.chosenStates.add((State) edge.getTargetNode().get("stateClass"));
                    }
                    thisVis.chosenActions.add(clickedAction);
                    
                    
                    thisVis.generateComputeStates(resultState, clickedAction, true);
                    if(updateGraph)
                    {
                        thisVis.setUpData(false);              
                        thisVis.updateVisualization(); 
                    }
                    thisVis.rehighlightPath(true, false);
                    prevNode = null;
                    prevEdge = null;
                    
                    
//                    currentState = (NodeItem) edge.getTargetNode();
                    
                    for(int i = 0; i < thisVis.graph.getNodeCount(); i++)
                    {
                        if(thisVis.graph.getNode(i).getBoolean("CurrentState"))
                        {
                            currentState = (NodeItem) thisVis.vis.getVisualItem("graph.nodes", thisVis.graph.getNode(i));
                        }
                    }
                    
//                chart.addStateValueAndAction(stateReward, actionReward, clickedAction.getName());
                
//                Edge edge = (Edge) item.get("Edge");
                } 
                catch (ParseException ex) {
                    Logger.getLogger(FinalControlListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            
    }
    
    private void goToState(VisualItem item)
    {
        ContainerOfActionAndStateSeqence container = getPathFrom((State) currentState.get("stateClass"), (State) item.get("stateClass"), item);
        if(container != null)
        {
            for(int i = 0; i < container.actions.size(); i++) 
            {
                TupleSet edges = thisVis.vis.getGroup("graph.edges");
                Iterator<Tuple> iter = edges.tuples();
                while(iter.hasNext())
                {
                    Tuple thisEdge = iter.next();
                    if(thisEdge.get("CriteriaAction").equals(container.actions.get(i)) && thisEdge.get("srcState").equals(currentState.get("stateClass")))
                    {   
                        if(!thisEdge.get("resultState").equals(container.states.get(i+1)))
                        {
                            continue;
                        }
                        if(i + 1 == container.actions.size())
                        {
                            System.out.println("final time");
                            ChangeCurrentState(thisEdge, true);
                        }
                        else
                        {
                            ChangeCurrentState(thisEdge, false);
                        }
//                        break;
                    }
                }
            }
        }
    }
    
    private void hideNodes(Node n, Node parent)
    {

            
        
//                    System.out.println("no connection between " + thisVis.temporaryHiddentStateNodes.get(0).s + "\n" + thisVis.temporaryHiddentStateNodes.get(0).connections.get(0).action.getName());
        if(n.getInDegree() == 1)
        {
            List<Node> childNodes = new ArrayList();
            for(int i = 0; i < n.getChildCount(); i++)
            {
                childNodes.add(n.getChild(i));
            }
//            thisVis.graph.removeNode(n);
            thisVis.temporaryHiddenStates.add((State) n.get("stateClass"));


            for(int i = 0; i < childNodes.size(); i++)
            {
                hideNodes(childNodes.get(i), childNodes.get(i).getParent());
            }
        }
    }
    
    private void handleHideEdge(VisualItem item) throws ParseException
    {
        prevNode = null;
        prevEdge = null;
        dataDisplay.setUpCharts("no action", null, null);
        Iterator edges = thisVis.graph.edges();
        Edge e = null;
        State src = (State) item.get("srcState");
        State result = (State) item.get("resultState");
        while(edges.hasNext())
        {
            e = (Edge) edges.next();
            if(e.get("srcState").equals(src) && e.get("resultState").equals(result))
            {
                break;
            }
        }
        Node removeNode = e.getTargetNode();

        Connection toBeRemoved = new Connection();
//      ComputeState hey = new ComputeState();
        StateNode newHiddenStateNode = new StateNode();
        newHiddenStateNode.s = (State) e.getSourceNode().get("stateClass");
        toBeRemoved.action = (CriteriaAction) e.get("CriteriaAction");
        toBeRemoved.states.add((State) e.getTargetNode().get("stateClass"));
        newHiddenStateNode.connections.add(toBeRemoved);
        thisVis.temporaryHiddentStateNodes.add(newHiddenStateNode);
        
        hideNodes(removeNode, e.getSourceNode());
            try 
            {
              thisVis.setUpData(false);  
              thisVis.updateVisualization();
              thisVis.rehighlightPath(true, false);
              this.resetCurrentState();
            } 
            catch (ParseException ex) 
            {
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
    
    private boolean getPathFrom(State end, Node n, List<CriteriaAction> actionList, CriteriaAction act, List<State> stateList)
    {
        if(actionList == null) actionList = new ArrayList();
        if(act != null) 
        {
            actionList.add(act);
            stateList.add((State) n.get("stateClass"));
        }
        if(n.get("stateClass").equals(end))  
        {
            return true;
        }
        if(n.getChildCount() == 0) return false;
        Iterator edges = n.childEdges();
       
        List<CriteriaAction> path = null;
        
        
        
        Iterator iter = n.childEdges();
        int numOfEdges = 0;
        while(iter.hasNext())
        {
            numOfEdges++;
            Edge e = (Edge) iter.next();
            CriteriaAction nextAction = (CriteriaAction) e.get("CriteriaAction");
            if(getPathFrom(end, e.getTargetNode(), actionList, act, stateList))
            {
                if(end.equals(e.getTargetNode().get("stateClass")))
                {
                    stateList.add(end);
                }
                actionList.add(nextAction);
                stateList.add((State) n.get("stateClass"));
                return true;
            }
        }
        
        return false;
    }
    
    private ContainerOfActionAndStateSeqence getPathFrom(State starting, State end, VisualItem item)
    {
        Node n = null;
        for(int i = 0; i < thisVis.graph.getNodeCount(); i++)
        {
            if(thisVis.graph.getNode(i).get("stateClass").equals(starting))
            {
                n = thisVis.graph.getNode(i);
            }
        }
        List<CriteriaAction> reversePath = new ArrayList<>();
        List<CriteriaAction> path = new ArrayList();
        List<State> statePath = new ArrayList();
        List<State> reverseStatePath = new ArrayList();
        getPathFrom(end, n, reversePath, null, reverseStatePath);
        
        
        for(int i = reverseStatePath.size() - 1; i >= 0; i--)
        {
            statePath.add(reverseStatePath.get(i));
//            System.out.println(reverseStatePath.get(i));
        }
        
        for(int i = reversePath.size()-1; i >= 0; i--)
        {
//            System.out.println("adding action " + reversePath.get(i));
            path.add(reversePath.get(i));
        }
        
        ContainerOfActionAndStateSeqence container = new ContainerOfActionAndStateSeqence();
        container.states = statePath;
        container.actions = path;
        return container;
//        for(int i = 0; i < n.getChildCount(); i++)
//        {
//            
//        }
//        
//        int startIndex = 0;
//        int endIndex = 0;
//        int visibleNum = 0;
//        System.out.println("Staring" + starting.getCompleteStateDescription());
//        System.out.println("Ending" + end.getCompleteStateDescription());
//        StateNode startingNode = null;
//        
//        List<CriteriaAction> actions = new ArrayList();
//        for(int i = 0; i < thisVis.nodeList.size(); i++)
//        {
//            if(thisVis.nodeList.get(i).s.equals(starting)) 
//            {
//                startingNode = thisVis.nodeList.get(i);
//                break;
//            }
//        }
////        thisVis.generateComputeStates(startingNode); NEED THIS TO WORK LATER
//        
//        for(int i = 0; i < thisVis.visibleStates.size(); i++)
//        {
////            for(int j = 0; j < thisVis.visibleStates.get(i).size(); j++) System.out.println(thisVis.visibleStates.get(i).get(j).getCompleteStateDescription());
////            System.out.println(i + " " + thisVis.computableStates.get(i).thisState.s.getCompleteStateDescription());
//            if(thisVis.visibleStates.get(i).contains(starting) && thisVis.visibleStates.get(i).contains(end)) 
//            {
////                System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
//                        
//                startIndex = thisVis.visibleStates.get(i).indexOf(starting);
//                endIndex = thisVis.visibleStates.get(i).indexOf(end);
//                visibleNum = i;
//                break;
////                boolean flag = false;
////                for(int j = 0; j < thisVis.computableStates.get(i).prevStates.size(); j++)
////                {
////                    if(thisVis.computableStates.get(i).prevStates.get(j).s.equals(starting)) flag = true;
////                    if(flag)
////                    {
////                        actions.add(thisVis.computableStates.get(i).prevActions.get(j));
////                    }
////                }
//////                System.out.println("sizeof this crap is " + actions.size());
////                return actions;
//            }
//            
//            
//        }
//        
//        if(thisVis.tree.statesTaken.contains(starting) && thisVis.tree.statesTaken.contains(end))
//        {
//            startIndex = thisVis.tree.statesTaken.indexOf(starting);
//            endIndex = thisVis.tree.statesTaken.indexOf(end);
//            visibleNum = -1;
//        }
//        
//        if(startIndex == 0 && endIndex == 0)
//        {
//            return null;
//        }
//        else if(visibleNum != -1)
//        {
//            for(int i = startIndex; i < endIndex; i++)
//            {
//                actions.add(thisVis.visibleActions.get(visibleNum).get(i));
//            }
//        }
//
//        else
//        {
//            for(int i = startIndex; i < endIndex; i++)
//            {
//                actions.add(thisVis.tree.actionsTaken.get(i));
//            }
//        }
//        return actions;
        
    }
    
    private void resetCurrentState()
    {
        for(int i = 0; i < thisVis.graph.getNodeCount(); i++)
        {
            if(thisVis.graph.getNode(i).getBoolean("CurrentState"))
            {
                currentState = (NodeItem) thisVis.vis.getVisualItem("graph.nodes", thisVis.graph.getNode(i));
            }
        }
    }
    
    
    class ContainerOfActionAndStateSeqence
    {
        List<CriteriaAction> actions;
        List<State> states;
        public ContainerOfActionAndStateSeqence()
        {
            actions = new ArrayList();
            states = new ArrayList();
        }
    }
}
