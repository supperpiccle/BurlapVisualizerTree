/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;

import BurlapVisualizer.MyController;
import burlap.oomdp.core.states.State;
import burlapcontroller.actions.CriteriaAction;
import java.util.List;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 *
 * @author jlewis
 */
public class NodeAndEdgeData 
{
    List<CriteriaAction> initialActions;
    List<State> initialStates;
    List<List<State>> visibleStates;
    List<List<CriteriaAction>> visibleActions;
    List<State> optimalStateSeq;
    List<CriteriaAction> optimalActionSeq;
    List<Edge> edges;
    int indexOfLastState;
}
