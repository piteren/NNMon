/**
 * 2018 (c) piteren
 */

package clapri;

import java.util.LinkedList;

/**
 * Classification Problem Case interface
 */
public interface CPCase {

    // returns list of case actors
    // in case of one actor list has one element
    LinkedList<CPActor> getMyActors();

    // returns current case state
    CPState getCurrentState();

    // sets new state for case with given classification_index
    // in case when future state do not depends form classification classIX is omitted
    void setNewState(int classIX);

    // prepares feedback for given decision
    CPFeedback prepFeedback(int calssIX);

    // default run of case
    // may be overridden in more complex cases
    default void runOnce(){
        CPState cStat = getCurrentState();
        int aDec = 0;
        for (CPActor act: getMyActors()) {
            aDec = act.makeDecision(cStat);
            CPFeedback cFdb = prepFeedback(aDec);
            act.takeFeedback(cFdb);
        }
        setNewState(aDec);
    }
}