/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;

import Tree.StateTree;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.values.Value;
import burlapcontroller.actions.CriteriaAction;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import prefuse.visual.VisualItem;
import BurlapVisualizer.MyController;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;


/**
 * This class contains all the info about the visualizer in another window.
 * @author jlewis
 */
public class DataDisplay implements ItemListener
{
    
    private StateTree tree;
    private List<String> allAttribs;
    
    private String HTMLHeader;
    private String HTMLTail;
    private String seperator;
    
    
    JFrame frame;
    JPanel p;
    JPanel list;
    JLabel srcTable;
    JLabel actionTable;
    JLabel resultTable;
    JLabel cost;
    JLabel impact;
    JLabel time;
    JLabel rewardTotal;
    JPanel display = null;
    JScrollPane scroll = null;
    JCheckBox ignoreDegCheckBox;
    boolean ignoreDegredation = false;
    
    MyController myController;
    List<VisualItem> items;
    FinalControlListener fcl;
    
    /**
     * When called this frame is disposed of.
     */
    public void close()
    {
        frame.dispose();
    }
    
    public boolean shouldIgnoreDegredation()
    {
        return ignoreDegredation;
    }
    
    /**
     * This set all initially needed data and opens a blank window.
     * @param t The state tree for the state space
     * @param allStateAttribs all defined attributes of a state.
     * @param controller The controller for this state space
     * @param takenActionItems the actions(in item form) of the actions taken
     * @param fcl The mouse control listener(used for when user clicks a button)
     */
    public DataDisplay(StateTree t, List<String> allStateAttribs, MyController controller, List<VisualItem> takenActionItems, FinalControlListener fcl)
    {
        ignoreDegCheckBox = new JCheckBox("Ignore degredation");
        ignoreDegCheckBox.addItemListener(this);
        
        this.fcl = fcl;
        items = takenActionItems;
        tree = t;
        allAttribs = allStateAttribs;
        myController = controller;
        cost = new JLabel();
        impact = new JLabel();
        time = new JLabel();
        rewardTotal = new JLabel();
        
        double[] rewards = controller.computeTotalReward(t.actionsTaken);
        double reward;
        for(int i = 0; i < rewards.length; i++)
        {
            rewards[i] = (double) Math.round(rewards[i] * 100000) / 100000;
        }
        reward = controller.getRewardWithCA(t.actionsTaken);
        reward = (double) Math.round(reward * 100000) / 100000;
        
        cost.setText("Cost: " + String.valueOf(rewards[0]));
        impact.setText("Impact: " + String.valueOf(rewards[1]));
        time.setText("Time: " + String.valueOf(rewards[2]));
        rewardTotal.setText("Reward: " + String.valueOf(reward));
        
        this.setUpCharts("no action", null, null);
    }
    
    

    
    /**
     * This function updates tables based on what has been clicked(or not clicked)
     * @param clickedAction String name of the clicked action("no action" if an action was not clicked)
     * @param srcState The source state that was clicked(or refered to by the action)
     * @param resultState The result state of the action on the src state
     */
    public void setUpCharts(String clickedAction, State srcState, State resultState)
    {
        HTMLHeader = "<html>";
        HTMLTail = "</html>";
        seperator = "<br>";

        
        String wholeMessage = "";
        wholeMessage += HTMLHeader;
        wholeMessage += "<head>";
        wholeMessage += "</head>";
        wholeMessage += "<body>";
        wholeMessage += setUpActionChart(clickedAction);
        wholeMessage += seperator + seperator + seperator;
        wholeMessage += "</body>";
        wholeMessage += HTMLTail;       
        actionTable = new JLabel(wholeMessage);
        
        List<String> differencesInStates = this.compareAttributes(srcState, resultState); 
        
        wholeMessage = "";
        wholeMessage += HTMLHeader;
        wholeMessage += "<head>";
        wholeMessage += "</head>";
        wholeMessage += "<body>";
        wholeMessage += setUpStateChart(srcState, differencesInStates);
        wholeMessage += seperator + seperator + seperator;
        wholeMessage += "</body>";
        wholeMessage += HTMLTail;         
        srcTable = new JLabel(wholeMessage);
        
        wholeMessage = "";
        wholeMessage += HTMLHeader;
        wholeMessage += "<head>";
        wholeMessage += "</head>";
        wholeMessage += "<body>";
        wholeMessage += setUpStateChart(resultState, differencesInStates);
        wholeMessage += seperator + seperator + seperator;
        wholeMessage += "</body>";
        wholeMessage += HTMLTail;       
        resultTable = new JLabel(wholeMessage);
        
        if(frame == null) frame = new JFrame("Visualizer Information");
        else frame.remove(p);
        
        p = new JPanel();
        
        list = new JPanel(); 
        list.add(srcTable);
        list.add(actionTable);
        list.add(resultTable);

        // create the middle panel components
//        List<ActionButton> actionButtons = getButtonList();
//        if(display == null)
//        {
//            display = new JPanel();
//            for(int i = 0; i < actionButtons.size(); i++)
//            {
//                display.add(actionButtons.get(i));
//            }
//                scroll = new JScrollPane(display, 
//                JScrollPane.VERTICAL_SCROLLBAR_NEVER, 
//                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//                scroll.getHorizontalScrollBar().setUnitIncrement(8);
//                scroll.setPreferredSize(new Dimension(500, 50));
//        }

        JPanel actionInfo = new JPanel();
        actionInfo.add(cost);
        actionInfo.add(impact);
        actionInfo.add(time);
        actionInfo.add(rewardTotal);
        actionInfo.add(ignoreDegCheckBox);
        

       p.add(list);
//       p.add(scroll);
       p.add(actionInfo);

       frame.add(p);
       frame.setSize(900, 700);
       frame.setVisible(true);
    }
    
    
    /**
     * sets up buttons that when activated call the itemClick method of FinalControlListener
     * @return List of buttons that map to itemClicked of FinalControlListener
     */
    private List<ActionButton> getButtonList()
    {
        List<ActionButton> buttonList = new ArrayList<>();
        for(int i = 0; i < tree.actionsTaken.size(); i++) 
        {
            CriteriaAction thisAction = tree.actionsTaken.get(i);
            String text = Integer.toString(i+1) +". " + thisAction.getName();
            buttonList.add(new ActionButton(text, i, thisAction.getName(), this.fcl, this.items.get(i)));
        }

        return buttonList;
    }
    
    /**
     * Sets up the table for state(in one table update this function will get called twice sometimes)
     * @param clickedState The state to make a table out of
     * @param different a string of the state attribute name that is different from the other state that was clicked
     * @return 
     */
    private String setUpStateChart(State clickedState, List<String> different)
    {
        String wholeMessage = "";

        wholeMessage += "<table border =\"1\">";
        //wholeMessage += "<table border =\"1\" style =\"width:100%\">";
        wholeMessage += "<th>Attribute Name</th>";
        wholeMessage += "<th>Attribute Value</th>";
        
        for(int i = 0; i< allAttribs.size(); i++)
        {
            wholeMessage += "<tr>";
            String stateAttrib = allAttribs.get(i);
            
            if(clickedState != null)
            {
                boolean highlight = false; //flag to determine if state attribute should be highlighted
                if(different != null)
                {
                    for(int u = 0; u < different.size(); u++)
                    {
                        if(allAttribs.get(i).equals(different.get(u))) //the different string name and the current attribute exists so between s1 and s2 this attr is different
                        {
                            highlight = true;//so highlight it
                        }
                    }
                }
                
                if(highlight)
                {
                    wholeMessage += "<td><FONT style=\"BACKGROUND-COLOR: yellow\">" + stateAttrib + "</FONT></td>";
                    wholeMessage += "<td><FONT style=\"BACKGROUND-COLOR: yellow\">" + clickedState.getObject("agent0").getValues().get(i) + "</td>";
                }
                else
                {
                    wholeMessage += "<td>" + stateAttrib + "</td>";
                    wholeMessage += "<td>" + clickedState.getObject("agent0").getValues().get(i) + "</td>";
                }

            }
            else
            {
                wholeMessage += "<td>" + stateAttrib + "</td>";
                wholeMessage += "<td></td>";
            }
            wholeMessage += "</tr>";
        }
        wholeMessage += "</table>";

        return wholeMessage;
    }
    
    
    /**
     * This function sets the html for the action table
     * @param clickedAction the action name
     * @return 
     */
    private String setUpActionChart(String clickedAction)
    {

        String wholeMessage = "";

        wholeMessage += "<table border=\"1\" style=\"width:100%\">";
        wholeMessage += "<th>Action Name</th>";
        wholeMessage += "<th>Cost</th>";
        wholeMessage += "<th>Imact</th>";
        wholeMessage += "<th>Response Time</th>";
        for(int i = 0; i < tree.allPossibleActions.size(); i++)
        {
            CriteriaAction act = tree.allPossibleActions.get(i);
            wholeMessage += "<tr>";
            if(clickedAction.equals(act.getName()))
            {
               wholeMessage += "<td><FONT style=\"BACKGROUND-COLOR: yellow\">" + act.getName() + "</FONT></td>"; 
               wholeMessage += "<td><FONT style=\"BACKGROUND-COLOR: yellow\">" + act.getCost() + "</FONT></td>";
               wholeMessage += "<td><FONT style=\"BACKGROUND-COLOR: yellow\">" + act.getImpact() + "</FONT></td>";
               wholeMessage += "<td><FONT style=\"BACKGROUND-COLOR: yellow\">" + act.getResponseTime() + "</FONT></td>";
            }
            else
            {
                wholeMessage += "<td>" + act.getName() + "</td>";
                wholeMessage += "<td>" + act.getCost() + "</td>";
                wholeMessage += "<td>" + act.getImpact() + "</td>";
                wholeMessage += "<td>" + act.getResponseTime() + "</td>";
            }

            wholeMessage += "</tr>";
            
            
        }
        
        wholeMessage += "</table>";

        return wholeMessage;
    }
    
    
    //returns a list of string with the names of attributes that are different between each state
    //it checks every every state value and appends the name of the attribute to differingValues which
    //is return so we know the names of the different values between s1 and s2
    private List<String> compareAttributes(State s1, State s2)
    {
        if(s2 == null) return null; //s1 will always be srcState but targetState may be null
        List<String> attrNames = myController.getAllStateAttributes();
        List<String> differingValues = new ArrayList<>();
        List<Value> attrValue1 = myController.getAllStateValuesFor(s1);
        List<Value> attrValue2 = myController.getAllStateValuesFor(s2);
        
        for(int i = 0; i < attrNames.size(); i++)
        {
            if(!attrValue1.get(i).equals(attrValue2.get(i))) //DOES NOT equal(different)
            {
                differingValues.add(attrNames.get(i));
            }
        }
        return differingValues;
    }

    @Override
    public void itemStateChanged(ItemEvent e) 
    {
        ignoreDegredation = true;
        ignoreDegCheckBox.setEnabled(false);
    }

}
