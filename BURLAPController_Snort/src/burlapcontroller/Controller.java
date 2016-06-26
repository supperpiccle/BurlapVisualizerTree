/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burlapcontroller;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.behavior.singleagent.planning.deterministic.uninformed.dfs.DFS;
import burlap.behavior.singleagent.planning.stochastic.montecarlo.uct.UCT;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.auxiliary.stateconditiontest.TFGoalCondition;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import burlapcontroller.actions.ActivateFirewall;
import burlapcontroller.actions.Backup;
import burlapcontroller.actions.BlockSrcIP;
import burlapcontroller.actions.CriteriaAction;
import burlapcontroller.actions.DecreaseLogVerb;
import burlapcontroller.actions.DisableHoneypot;
import burlapcontroller.actions.FlowRateLimit;
import burlapcontroller.actions.FlowRateUnlimit;
import burlapcontroller.actions.GenerateAlert;
import burlapcontroller.actions.IncreaseLogVerb;
import burlapcontroller.actions.ManualResolution;
import burlapcontroller.actions.QuarantineSystem;
import burlapcontroller.actions.RedirectToHoneypot;
import burlapcontroller.actions.SoftwareUpdate;
import burlapcontroller.actions.SystemReboot;
import burlapcontroller.actions.SystemShutdown;
import burlapcontroller.actions.UnblockSrcIP;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 *
 * @author stefano
 */
public class Controller {

    public static final String ATTPSCAN = "pScan";
    public static final String ATTPVSFTPD = "pVsftpd";
    public static final String ATTPSMBD = "pSmbd";
    public static final String ATTPPHPCGI = "pPhpCgi";
    public static final String ATTPIRCD = "pIrcd";
    public static final String ATTPDISTCCD = "pDistccd";
    public static final String ATTPRMI = "pRmi";

    public static final String ATTATTACKERIP = "attackerIP";

    public static final String ATTFIREWALL = "firewall";
    public static final String ATTBLOCKEDIPS = "blocked_ips";
    public static final String ATTFLOWLIMITIPS = "flowlimit_ips";
    public static final String ATTHONEYPOTIPS = "honeypot_ips";
    public static final String ATTLOGVERB = "logVerb";
    public static final String ATTACTIVE = "active";
    public static final String ATTQUARANTINED = "quarantined";
    public static final String ATTREBOOTED = "rebooted";
    public static final String ATTALERTED = "alerted";
    public static final String ATTBACKUP = "backup";
    public static final String ATTSOFTWARE = "softwareUpToDate";

    public static final String CLASSAGENT = "agent";
    public static final String CLASSLOCATION = "location";

    //Definition of thresholds t1 and t2
    public static final double T1 = 0.2;
    public static final double T2 = 0.4;

    private SADomain domain = null;
    private Reward rf;
    private TerminalFunction tf;
    private StateConditionTest goalCondition;
    private State initialState;
    private HashableStateFactory hashingFactory;
    private Environment env;

    private List<State> stateSpace = null;
    private EpisodeAnalysis episodeAnalysis = null;
    private List<CriteriaAction> definedActions = null;
    private Hashtable<String, CriteriaAction> actionMap = null;

    private Planner planner = null;
    private Policy policy = null;

    public EpisodeAnalysis getEpisodeAnalysis() {
        return episodeAnalysis;
    }

    public List<CriteriaAction> getDefinedActions() {
        return definedActions;
    }

    public List<State> getStateSpace() {
        return stateSpace;
    }

    public Hashtable<String, CriteriaAction> getActionMap() {
        return actionMap;
    }

    public State getInitialState() {
        return initialState;
    }

    public Policy getPolicy() {
        return policy;
    }
    
    public Reward getRewardFunction() {
        return (Reward) rf;
    }
    
    public double getV(State s) {
        double ret = -1;
        if (planner instanceof ValueIteration) {
            ValueIteration vi = (ValueIteration)planner;
            ret = vi.value(s);
        }
        return ret;
    }
    

    public Controller(double scan, double vsftpd, double smbd, double phpcgi,
            double ircd, double distccd, double rmi, String ip, double wr, double wc, double wi)
            throws FileNotFoundException, IOException {
        domain = new SADomain();

        //State space definition
        Attribute pScan = new Attribute(domain, ATTPSCAN, Attribute.AttributeType.REAL);
        pScan.setLims(0, 1);
        Attribute pVsftpd = new Attribute(domain, ATTPVSFTPD, Attribute.AttributeType.REAL);
        pVsftpd.setLims(0, 1);
        Attribute pSmbd = new Attribute(domain, ATTPSMBD, Attribute.AttributeType.REAL);
        pSmbd.setLims(0, 1);
        Attribute pPhpCgi = new Attribute(domain, ATTPPHPCGI, Attribute.AttributeType.REAL);
        pPhpCgi.setLims(0, 1);
        Attribute pIrcd = new Attribute(domain, ATTPIRCD, Attribute.AttributeType.REAL);
        pIrcd.setLims(0, 1);
        Attribute pDistccd = new Attribute(domain, ATTPDISTCCD, Attribute.AttributeType.REAL);
        pDistccd.setLims(0, 1);
        Attribute pRmi = new Attribute(domain, ATTPRMI, Attribute.AttributeType.REAL);
        pRmi.setLims(0, 1);

        Attribute attackerIp = new Attribute(domain, ATTATTACKERIP, Attribute.AttributeType.STRING);

        Attribute firewall = new Attribute(domain, ATTFIREWALL, Attribute.AttributeType.BOOLEAN);
        Attribute blockedIps = new Attribute(domain, ATTBLOCKEDIPS, Attribute.AttributeType.STRING);
        Attribute flowlimitIps = new Attribute(domain, ATTFLOWLIMITIPS, Attribute.AttributeType.STRING);
        Attribute honeypotIps = new Attribute(domain, ATTHONEYPOTIPS, Attribute.AttributeType.STRING);
        Attribute logVerb = new Attribute(domain, ATTLOGVERB, Attribute.AttributeType.INT);
        logVerb.setLims(0, 5);
        Attribute active = new Attribute(domain, ATTACTIVE, Attribute.AttributeType.BOOLEAN);
        Attribute quarantined = new Attribute(domain, ATTQUARANTINED, Attribute.AttributeType.BOOLEAN);
        Attribute rebooted = new Attribute(domain, ATTREBOOTED, Attribute.AttributeType.BOOLEAN);
        Attribute alerted = new Attribute(domain, ATTALERTED, Attribute.AttributeType.BOOLEAN);
        Attribute backup = new Attribute(domain, ATTBACKUP, Attribute.AttributeType.BOOLEAN);
        Attribute softwareUpToDate = new Attribute(domain, ATTSOFTWARE, Attribute.AttributeType.BOOLEAN);

        ObjectClass agentClass = new ObjectClass(domain, CLASSAGENT);
        agentClass.addAttribute(pScan);
        agentClass.addAttribute(pVsftpd);
        agentClass.addAttribute(pSmbd);
        agentClass.addAttribute(pPhpCgi);
        agentClass.addAttribute(pIrcd);
        agentClass.addAttribute(pDistccd);
        agentClass.addAttribute(pRmi);

        agentClass.addAttribute(attackerIp);

        agentClass.addAttribute(firewall);
        agentClass.addAttribute(blockedIps);
        agentClass.addAttribute(flowlimitIps);
        agentClass.addAttribute(honeypotIps);
        agentClass.addAttribute(logVerb);
        agentClass.addAttribute(active);
        agentClass.addAttribute(quarantined);
        agentClass.addAttribute(rebooted);
        agentClass.addAttribute(alerted);
        agentClass.addAttribute(backup);
        agentClass.addAttribute(softwareUpToDate);

        // TODO
        ///Actions definition
        CriteriaAction activateFirewall = new ActivateFirewall("activateFirewall", domain);
        CriteriaAction blockSrcIp = new BlockSrcIP("blockSrcIP", domain);
        DecreaseLogVerb dec = new DecreaseLogVerb("decreaseLogVerb", domain);
        CriteriaAction disableHoneyPot = new DisableHoneypot("disableHoneypot", domain);
        CriteriaAction flowRateLimit = new FlowRateLimit("flowRateLimit", domain);
        CriteriaAction flowRateUnlimit = new FlowRateUnlimit("flowRateUnlimit", domain);
        CriteriaAction generateAlert = new GenerateAlert("generateAlert", domain);
        IncreaseLogVerb inc = new IncreaseLogVerb("increaseLogVerb", domain);
        CriteriaAction manualResolution = new ManualResolution("manualResolution", domain);
        CriteriaAction quarantineSystem = new QuarantineSystem("quarantineSystem", domain);
        CriteriaAction redirectToHoneypot = new RedirectToHoneypot("redirectToHoneypot", domain);
        CriteriaAction systemReboot = new SystemReboot("systemReboot", domain);
        CriteriaAction systemShutdown = new SystemShutdown("systemShutdown", domain);
        CriteriaAction unblockSrcIP = new UnblockSrcIP("unblockSrcip", domain);
//        CriteriaAction closeNetworkConn = new CloseNetworkConn("closeNetworkConn", domain);
        CriteriaAction backupAction = new Backup("backup", domain);
        CriteriaAction softwareUpdate = new SoftwareUpdate("softwareUpdate", domain);

        definedActions = new ArrayList<>();
        definedActions.add(activateFirewall);
        definedActions.add(blockSrcIp);
        definedActions.add(disableHoneyPot);
        definedActions.add(flowRateLimit);
        definedActions.add(flowRateUnlimit);
        definedActions.add(generateAlert);
        definedActions.add(inc);
        definedActions.add(dec);
        definedActions.add(manualResolution);
        definedActions.add(quarantineSystem);
        definedActions.add(redirectToHoneypot);
        definedActions.add(systemReboot);
        definedActions.add(systemShutdown);
        definedActions.add(unblockSrcIP);
        definedActions.add(backupAction);
        definedActions.add(softwareUpdate);

        actionMap = new Hashtable<>();
        actionMap.put(activateFirewall.getName(), activateFirewall);
        actionMap.put(blockSrcIp.getName(), blockSrcIp);
        actionMap.put(disableHoneyPot.getName(), disableHoneyPot);
        actionMap.put(flowRateLimit.getName(), flowRateLimit);
        actionMap.put(flowRateUnlimit.getName(), flowRateUnlimit);
        actionMap.put(generateAlert.getName(), generateAlert);
        actionMap.put(inc.getName(), inc);
        actionMap.put(dec.getName(), dec);
        actionMap.put(manualResolution.getName(), manualResolution);
        actionMap.put(quarantineSystem.getName(), quarantineSystem);
        actionMap.put(redirectToHoneypot.getName(), redirectToHoneypot);
        actionMap.put(systemReboot.getName(), systemReboot);
        actionMap.put(systemShutdown.getName(), systemShutdown);
        actionMap.put(unblockSrcIP.getName(), unblockSrcIP);
        actionMap.put(backupAction.getName(), backupAction);
        actionMap.put(softwareUpdate.getName(), softwareUpdate);

        ///////////////////////////////////////////////////////////////////////////
        //Setting the initial state
        initialState = new MutableState();
        ObjectInstance agent = new MutableObjectInstance(domain.getObjectClass(CLASSAGENT), "agent0");
        agent.setValue(ATTPSCAN, scan);
        agent.setValue(ATTPVSFTPD, vsftpd);
        agent.setValue(ATTPSMBD, smbd);
        agent.setValue(ATTPPHPCGI, phpcgi);
        agent.setValue(ATTPIRCD, ircd);
        agent.setValue(ATTPDISTCCD, distccd);
        agent.setValue(ATTPRMI, rmi);

        agent.setValue(ATTATTACKERIP, ip);

        agent.setValue(ATTFIREWALL, false);
        agent.setValue(ATTBLOCKEDIPS, "");
        agent.setValue(ATTFLOWLIMITIPS, "");
        agent.setValue(ATTALERTED, false);
        agent.setValue(ATTHONEYPOTIPS, "");
        agent.setValue(ATTLOGVERB, 0);
        agent.setValue(ATTACTIVE, true);
        agent.setValue(ATTQUARANTINED, false);
        agent.setValue(ATTREBOOTED, false);
        agent.setValue(ATTBACKUP, false);
        agent.setValue(ATTSOFTWARE, false);

        initialState.addObject(agent);

        ///////////////////////////////////////////////////////////////////////////
        //Defining Reward Function and Goal Conditions
        ///////////////////////////////////////////////////////////////////////////
//        rf = new UniformCostRF();
        rf = new Reward(domain.getActions(), wr, wc, wi);
        tf = new Termination();
        goalCondition = new TFGoalCondition(tf);

//        closeNetworkConn.setReward((Reward) rf);
        inc.setReward((Reward) rf);
        dec.setReward((Reward) rf);

        //set up the state hashing system for tabular algorithms
        hashingFactory = new SimpleHashableStateFactory(false);

//        Set<HashableState> reachableHashedStates = StateReachability.getReachableHashedStates(initialState, domain, hashingFactory);
//        System.out.println(reachableHashedStates.size());
//        List<State> reachableStates = StateReachability.getReachableStates(initialState, domain, hashingFactory);
        //set up the environment for learning algorithms
//        env = new SimulatedEnvironment(domain, rf, tf, initialState);
//        TerminalExplorer exp = new TerminalExplorer(domain, env);
//        exp.explore();
        ///////////////////////////////////////////////////////////////////////////
    }

    public void BFSExample(String outputPath) {

        DeterministicPlanner planner = new BFS(domain, goalCondition, hashingFactory);
        Policy p = planner.planFromState(initialState);
//        rf = new UniformCostRF();
        p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "bfs");
    }

    public void valueIteration(String outputPath, double gamma) {

        planner = new ValueIteration(domain, rf, tf, gamma, hashingFactory, 0.001, 100);
        ValueIteration vi = (ValueIteration) planner;
        policy = planner.planFromState(initialState);
        stateSpace = vi.getAllStates();
        EpisodeAnalysis ea = policy.evaluateBehavior(initialState, rf, tf);
        this.episodeAnalysis = ea;
        ea.writeToFile(outputPath + "vi");
    }
    
    public EpisodeAnalysis getOptimalPathFrom(State startingState)
    {
        EpisodeAnalysis ea = policy.evaluateBehavior(startingState, rf, tf);
        return ea;
    }

    public void DFSExample(String outputPath) {

        DeterministicPlanner planner = new DFS(domain, goalCondition, hashingFactory);
        Policy p = planner.planFromState(initialState);
        p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "dfs");

    }

    public void uct(String outputPath) {
        UCT uct = new UCT(domain, rf, tf, 0.9, hashingFactory, 10, 10, 3);
        Policy p = uct.planFromState(initialState);
        EpisodeAnalysis ea = p.evaluateBehavior(initialState, rf, tf);
        ea.writeToFile(outputPath + "uct");
    }
    
    public void setRewardWeights(double wr, double wc, double wi) {
        rf.setWr(wr);
        rf.setWc(wc);
        rf.setWi(wi);
    }
    

//    public void testSolutions() {
//
//        Hashtable<List<GroundedAction>, Integer> table = new Hashtable<>();
//        Hashtable<List<GroundedAction>, Double> rewardTable = new Hashtable<>();
//        Hashtable<List<GroundedAction>, Double> responseTimeTable = new Hashtable<>();
//        Hashtable<List<GroundedAction>, Double> impactTable = new Hashtable<>();
//        Hashtable<List<GroundedAction>, Double> costTable = new Hashtable<>();
//
//        int iterations = 1;
//
//        for (int i = 0; i < iterations; i++) {
//            System.out.println("i=" + i);
////            ValueIteration planner = new ValueIteration(domain, rf, tf, 0.9, hashingFactory, 0.001, 100);
////            ValueIteration planner = new ParallelVI(domain, rf, tf, 0.9, hashingFactory, 0.001, 100);
//
////            UCT planner = new UCT(domain, rf, tf, 0.9, hashingFactory, 30, 30, 3);
//            Policy p = planner.planFromState(initialState);
//
//            EpisodeAnalysis ea = p.evaluateBehavior(initialState, rf, tf);
//            if (table.containsKey(ea.actionSequence)) {
//                int counter = table.get(ea.actionSequence);
//                counter++;
//                table.put(ea.actionSequence, counter);
//            } else {
//                //Counter initialization
//                table.put(ea.actionSequence, 1);
//
//                //Setting reward for action sequence
//                List<Double> rewardSequence = ea.rewardSequence;
//                double reward = 0;
//                for (Double r : rewardSequence) {
//                    reward += r;
//                }
//                rewardTable.put(ea.actionSequence, reward);
//
//                //Setting Response time and impact for action sequence
//                double responseTime = 0;
//                double impact = 0;
//                double cost = 0;
//                List<GroundedAction> l = ea.actionSequence;
//                for (GroundedAction ga : l) {
//                    CriteriaAction a = (CriteriaAction) ga.action;
//                    responseTime += a.getResponseTime();
//                    impact += a.getImpact();
//                    cost += a.getCost();
//                }
//                responseTimeTable.put(l, responseTime);
//                impactTable.put(l, impact);
//                costTable.put(l, cost);
//            }
//            if (i % 1000 == 0) {
//                System.out.println("i=" + i);
//            }
//        }
//        System.out.println("Size = " + table.size());
//        Enumeration<List<GroundedAction>> e = table.keys();
//        double totalReward = 0;
//        double totalRespTime = 0;
//        double totalCost = 0;
//        double totalImpact = 0;
//        int maxExecutions = 0;
//        String mostExecutedAction = "";
//        while (e.hasMoreElements()) {
//            List<GroundedAction> l = e.nextElement();
//            for (GroundedAction ga : l) {
//                System.out.print(ga.actionName() + ", ");
//            }
//            System.out.print(";");
//            System.out.print(rewardTable.get(l) + "; ");
//            totalReward += rewardTable.get(l) * table.get(l);
//            System.out.print(responseTimeTable.get(l) + ";");
//            totalRespTime += responseTimeTable.get(l) * table.get(l);
//            System.out.print(costTable.get(l) + ";");
//            totalCost += costTable.get(l) * table.get(l);
//            System.out.print(impactTable.get(l) + ";");
//            totalImpact += impactTable.get(l) * table.get(l);
//            System.out.print(table.get(l));
//            if (table.get(l) > maxExecutions) {
//                maxExecutions = table.get(l);
//                mostExecutedAction = "";
//                for (GroundedAction ga : l) {
//                    mostExecutedAction += ga.actionName() + ", ";
//                }
//            }
//            System.out.println();
//        }
//        double avgReward = totalReward / iterations;
//        double avgRespTime = totalRespTime / iterations;
//        double avgCost = totalCost / iterations;
//        double avgImpact = totalImpact / iterations;
//        System.out.println("Average Response Time: " + avgRespTime);
//        System.out.println("Average Cost: " + avgCost);
//        System.out.println("Average Impact: " + avgImpact);
//        System.out.println("Average Reward: " + avgReward);
//        System.out.println("Most executed policy, with " + maxExecutions + " occurrences:");
//        System.out.println(mostExecutedAction);
//
//        //Computing Confidence intervals
//        e = table.keys();
//        double respTimeBar = 0;
//        double costBar = 0;
//        double impactBar = 0;
//        double rewardBar = 0;
//        while (e.hasMoreElements()) {
//            List<GroundedAction> l = e.nextElement();
//            respTimeBar += table.get(l) * (responseTimeTable.get(l) - avgRespTime) * (responseTimeTable.get(l) - avgRespTime);
//            costBar += table.get(l) * (costTable.get(l) - avgCost) * (costTable.get(l) - avgCost);
//            impactBar += table.get(l) * (impactTable.get(l) - avgImpact) * (impactTable.get(l) - avgImpact);
//            rewardBar += table.get(l) * (rewardTable.get(l) - avgReward) * (rewardTable.get(l) - avgReward);
//        }
//        double respTimeVariance = respTimeBar / (iterations - 1);
//        double costVariance = costBar / (iterations - 1);
//        double impactVariance = impactBar / (iterations - 1);
//        double rewardVariance = rewardBar / (iterations - 1);
//
//        double respTimeStddev = Math.sqrt(respTimeVariance);
//        double costStddev = Math.sqrt(costVariance);
//        double impactStddev = Math.sqrt(impactVariance);
//        double rewardStddev = Math.sqrt(rewardVariance);
//
//        System.out.println(respTimeStddev);
//        System.out.println(costStddev);
//        System.out.println(impactStddev);
//        System.out.println(rewardStddev);
//
//        double respTimeLo = avgRespTime - 1.96 * respTimeStddev;
//        double respTimeHi = avgRespTime + 1.96 * respTimeStddev;
//        double costLo = avgCost - 1.96 * costStddev;
//        double costHi = avgCost + 1.96 * costStddev;
//        double impactLo = avgImpact - 1.96 * impactStddev;
//        double impactHi = avgImpact + 1.96 * impactStddev;
//        double rewardLo = avgReward - 1.96 * rewardStddev;
//        double rewardHi = avgReward + 1.96 * rewardStddev;
//
//        if (respTimeLo < 0) {
//            respTimeLo = 0;
//        }
//        if (costLo < 0) {
//            costLo = 0;
//        }
//        if (impactLo < 0) {
//            impactLo = 0;
//        }
//        if (rewardHi > 0) {
//            rewardHi = 0;
//        }
//
//        System.out.println("Resolution Time 95% Confidence Interval: [" + respTimeLo + ", " + respTimeHi + "]");
//        System.out.println("Cost 95% Confidence Interval: [" + costLo + ", " + costHi + "]");
//        System.out.println("Impact 95% Confidence Interval: [" + impactLo + ", " + impactHi + "]");
//        System.out.println("Reward 95% Confidence Interval: [" + rewardLo + ", " + rewardHi + "]");
//
//    }
   
}
