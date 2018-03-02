/*
 * 2017 (c) piteren
 */
package trainer.toCase;

import trainer.NetTrainerAISolver;
import trainer.NTCaseFeedback;
import utilities.UArr;

/**
 * trained actor interface
 * object that implements this interface should extend client_actor and should have AIsolver
 * 1.knows case state and has all information to prepare solver input data
 * 2.interprets solver output data (it may know case_ACTUAL_POSSIBLE_decision_list, so it should use that info to "trim" solver out)
 * 3.prepares feedback for solver
 * should override client_actor_METHOD that makes decision for case with solver_supported_METHOD
 */
public interface NTActor {
    
    //returns actor AIsolver
    public NetTrainerAISolver getMySolver();

    //prepares solver input data array
    public double[] prepSolverIN();

    //interprets solver output array, returns decision index
    public int intpSolverOUT(double[] solverOUT);

    // ??
    /*
    default public int intpSolverOUT(double[] solverOUT){
        int aX = UArr.maxVix(solverOUT);
        return aX;
    }
    */

    //prepares complete feedback for solver (for taken decision index)
    public NTCaseFeedback prepFeedbackToSolver(int decIX);
}