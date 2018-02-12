/*
 * 2017 (c) piteren
 */
package trainer.toCase;

import java.util.LinkedList;
import trainer.NetTrainerAISolver;
import trainer.NTCaseFeedback;
import utilities.threadRun.UTRobject;

/**
 * interface of case that communicates with trainer (trainer runs case) and NTactor
 * object that implements this interface should extend client case
 */
public interface NTCase extends UTRobject{
    
    //duplicates case
    public NTCase duplicate();
    
    //************************************************************************** some informational methods
    //returns case specific possible number of actors: 0-any N-specific
    public int caseNumOfActors();
    //returns number of classes in case (=width of NNet output)
    public int caseNumOfClasses();
    //returns information wether actor decision changes state
    public boolean actDecisionChangesState();
    
    //takes list of solvers from trainer (creates new list of trained_actors, for everyone gives one solver and puts those actors to case)
    public void takeSolvers(LinkedList<NetTrainerAISolver> solvers);
    
    //returns actors list
    public LinkedList<NTActor> getMyActors();    
    //returns <0;1> array marking current_possible_decisions among all classes
    public int[] currentPossibleDecisions();

    //runs case x times, only rewarded runs count (single case run may need many not_rewarded_actor_decisions)
    public default void runCase(int numRuns){
        // below is generic algorithm - may be overriden in more complex environment
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
    public void moveCaseToNextState(int decIX);
    
    //method that performs sample test of case on actor
    public void sampleTestRun();
    
    @Override
    public default void runTR(int mIP){
        runCase(mIP);
    }
}