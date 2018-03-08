/*
 * 2017 (c) piteren
 */
package trainer.toCase;

import java.util.LinkedList;
import trainer.NTaiSolver;
import trainer.NTCaseFeedback;
import utilities.threadRun.UTRobject;

/**
 * interface of case that communicates with trainer (trainer runs case) and NTactor
 * object that implements this interface should extend client case
 */
public interface NTCase extends UTRobject{
    
    // duplicates case
    NTCase duplicate();
    
    // ************************************************************************************** some informational methods

    // returns case specific possible number of actors: 0-any N-specific
    int caseNumOfActors();

    // returns number of classes in case (=width of NNet output)
    int caseNumOfClasses();

    // returns information whether actor decision changes future case state
    boolean actDecisionChangesState();


    // takes list of solvers from trainer
    // >> creates new list of NTactors, for each gives one AIsolver and puts those NTactors to case
    void takeSolvers(LinkedList<NTaiSolver> solvers);
    
    // returns NTactors list
    LinkedList<NTActor> getMyActors();

    // returns <0;1> array marking current_possible_decisions among all classes
    int[] currentPossibleDecisions();

    // runs case x times, only rewarded runs count (single case run may need many not_rewarded_actor_decisions)
    // ...below is generic algorithm - may be overridden in more complex case
    default void runCase(int numRuns){
        for(int i=0; i<numRuns; i++){
            int actDec = 0;
            for(NTActor act: getMyActors()){
                double[] solverIN = act.prepSolverIN();
                double[] solverOUT = act.getMySolver().runFWD(solverIN);
                actDec = act.intpSolverOUT(solverOUT);
                
                NTCaseFeedback caseFeedback = act.prepFeedbackToSolver(actDec);
                act.getMySolver().takeCaseFeedback(caseFeedback);
            }
            moveCaseToNextState(actDec);                                        //if case has more than one actor its next state cannot depend on actor decision
        }
    }
    
    // moves case to next state with given actor decision
    void moveCaseToNextState(int decIX);
    
    //method that performs sample test of case on actor (test of performance without learning)
    void sampleTestRun();

    @Override
    default void runTR(int mIP){
        runCase(mIP);
    }
}