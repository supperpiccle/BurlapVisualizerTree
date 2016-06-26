/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BurlapVisualizer;

import burlap.oomdp.singleagent.RewardFunction;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;


//This is the class with the main function.  All it does is simply
// open a JFrame with text input boxes that allow you to enter
// your initial state.  After you push the ok button it calls the
// controller with the attributes you described.
/**
 * This is the main class where execution starts.
 * <p>
 * This is the class that handles what state space gets visualized based on user input.
 * 
 * @author jlewis
 */
public class TestProject {

    private static JFrame frame;


    private static JPanel statePanel;
    private static JPanel rewardPanel;
    private static JPanel mainPanel;
    private static JPanel rewardDegredationPanel;
    private static List<JLabel> probabilityLabels;
    private static List<JLabel> rewardLabels;
    private static List<JLabel> RewardDegredationLabels;
    private static List<JTextField> textEnteredProbs;
    private static List<JTextField> textEnteredReward;
    private static List<JTextField> textEnteredRewardDegredation;
    private static JButton button;
    private static Manager m = null;

    /**
     * main method
     *
     * @param args[] the command line arguements
     */
    public static void main(String args[]) throws IOException 
    {
        MyController temp = new MyController(0, 0, 0, 0, 0, 0, 0, "", 1, 0, 0, 0.9); //we make a temp controller to get the names of attributes

        List<String> attrNames = temp.getAllStateAttributes();//get the name of every defined attribute
        setUpUI(attrNames, temp.getRewardFunction());
    }

    /**
     * Sets up the User Interface.
     * <p>
     * This method simply creates a JFrame and then adds as many input text
     * fields and labels to describe those text fields as neccesarry to describe
     * the initial state. Also adds an OK button at the bottom. From there it
     * calls Manager to set up the state space. When it detects that the OK
     * button has been pressed again it closes everything it has and starts
     * fresh.
     *
     * @param allAttribsForState this is a list of all the names of attributes
     * for states
     */
    public static void setUpUI(List<String> allAttribsForState, RewardFunction rf) {

        button = new JButton("OK");
        button.addActionListener(new ButtonListener());
        button.setPreferredSize(new Dimension(40, 40));
        JPanel buttonPanel = new JPanel(new GridLayout(3, 3));
        buttonPanel.add(new JLabel(""));
        buttonPanel.add(new JLabel(""));
        buttonPanel.add(new JLabel(""));
        buttonPanel.add(new JLabel(""));
        buttonPanel.add(button);
        buttonPanel.add(new JLabel(""));
        buttonPanel.add(new JLabel(""));
        buttonPanel.add(new JLabel(""));
        buttonPanel.add(new JLabel(""));


        probabilityLabels = new ArrayList<>();
        rewardLabels = new ArrayList<>();
        RewardDegredationLabels = new ArrayList<>();
        textEnteredProbs = new ArrayList<>();
        textEnteredReward = new ArrayList<>();
        textEnteredRewardDegredation = new ArrayList<>();
        

        
        statePanel = new JPanel(); 
        rewardPanel = new JPanel();
        mainPanel = new JPanel();
        rewardDegredationPanel = new JPanel();
        
        statePanel.setLayout(new GridLayout(0, 2)); //7 attributes + 1 button = 8 rows AND 2 columns 1 for label(text) and the second for input(The zero allows for as many rows as neccesarry)
        rewardPanel.setLayout(new GridLayout(0,2));
//        rewardDegredationPanel.setLayout(mgr);
        
        statePanel.add(new JLabel("State attributes"));
        statePanel.add(new JLabel(" "));
        
        rewardPanel.add(new JLabel("Reward parameters"));
        rewardPanel.add(new JLabel(" "));
        
        frame = new JFrame("Input for Burlap");

        for (int i = 0; i < allAttribsForState.size(); i++) //for every attribute make the corresponding label and text feild
        {
            if (allAttribsForState.get(i).equals("attackerIP")) //after attackerIP the attributes are things that don't make sense to change(like firewall and such)
            {
                break;//done adding stuff break out of loop
            }
            probabilityLabels.add(new JLabel(allAttribsForState.get(i)));

            textEnteredProbs.add(new JTextField());

            statePanel.add(probabilityLabels.get(i));
            statePanel.add(textEnteredProbs.get(i));
        }
        
        rewardLabels.add(new JLabel("Cost"));
        rewardLabels.add(new JLabel("Impact"));
        rewardLabels.add(new JLabel("Response Time"));
        rewardLabels.add(new JLabel("Gamma"));
        rewardLabels.add(new JLabel("Degredation"));
        for(int i = 0; i < 5; i++)
        {
            rewardPanel.add(rewardLabels.get(i));
            textEnteredReward.add(new JTextField());
            rewardPanel.add(textEnteredReward.get(i));
        }
        mainPanel.add(statePanel);
        mainPanel.add(rewardPanel);
        mainPanel.add(buttonPanel);
        mainPanel.setLayout(new GridLayout(0, 3));
        frame.add(mainPanel);//add main panel
        frame.pack();//make frame as small as possible and still keep integrity

        frame.setVisible(true);

    }

    /**
     * This listens to the OK button and when pushed returns double value of each text field
     * 
     */
    private static class ButtonListener implements ActionListener {

        
        /**
         * 
         * @param e it is the text of the button which is OK.  If pressed return data. 
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("OK")) {
                
                if (m != null) { //if there is a manager running close it.
                    m.close();
                }

                
                List<Double> values = getValues();
                try 
                {
                    System.out.println("cost: "+ values.get(7)+ "impact: "+ values.get(8)+ "time: "+ values.get(9) + "gamma: " + values.get(10) + "Degredation: " + values.get(11));
                    m = new Manager(); //then create new instance
                    m.run(values.get(0), values.get(1),
                            values.get(2), values.get(3),
                            values.get(4), values.get(5),
                            values.get(6), values.get(7),
                            values.get(8), values.get(9), 
                            values.get(10), values.get(11)); //the value list is in correct order to do this
                    
                    //m.run will run until this method is called again(by user pushing OK button) causing this m.run to close and another open
                }
                catch (Exception ex)//this execption will only be caught if values.get(n) returns null(should never happen) 
                {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "The provided input is invalid.");
                }                
            }
        }

        /**
         * Returns a list of doubles to specify the initial state and the reward function.
         * <p>
         * index [0] - [6] are the initial state
         * index [7] - [9] are the reward function in the following format
         * [7] = cost
         * [8] = impact
         * [9] = time
         * [10] = gamma
         * [11] = allowedDegredation
         * @return List of <Doubles> that define the values the user specified for each attribute and reward function. 
         */
        public List<Double> getValues() {
            List<Double> value = new ArrayList<>();
            double sum = 0;

            for (int i = 0; i < textEnteredProbs.size(); i++) 
            {
                if (!textEnteredProbs.get(i).getText().equals("")) //if there was something there add it to the list
                {
                    value.add(Double.parseDouble(textEnteredProbs.get(i).getText()));
                } 
                else //else just add a zero
                {
                    value.add(0.0);
                }
            }
            if("".equals(textEnteredReward.get(0).getText()) && "".equals(textEnteredReward.get(1).getText()) && "".equals(textEnteredReward.get(2).getText())) //empty
            {
                value.add(1.0); //cost = 1
                value.add(0.0);// impact = 0
                value.add(0.0);// time = 0
                sum = 1;
            }
            else
            {
                for(int i = 0; i < 3; i++) //go through each reward value
                {
                    double rewardValue;
                    if(textEnteredReward.get(i).getText().equals("")) rewardValue = 0;
                    else rewardValue = Double.parseDouble(textEnteredReward.get(i).getText());
                    value.add(rewardValue);
                    sum += rewardValue;
                }
            }

            if(sum < 1 || sum > 1) return null;
            
            double gamma = 0;
            if(textEnteredReward.get(3).getText().equals(""))
            {
                gamma = 0;
            }
            else
            {
                gamma = Double.parseDouble(textEnteredReward.get(3).getText());
                if(gamma < 0 || gamma >=1) return null;
            }
            value.add(gamma);
            
            if("".equals(textEnteredReward.get(4).getText()))
            {
                value.add(-1.0);
            }
            else
            {
                value.add(Double.parseDouble(textEnteredReward.get(4).getText()));
            }

            
            
            return value;
        }
    }
}
