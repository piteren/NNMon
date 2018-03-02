/*
 * 2018 (c) piteren
 */

package rowDM.toTrainer;

import rowDM.RowDMActor;
import rowDM.RowMDFeedback;
import trainer.NTCaseFeedback;
import trainer.NetTrainerAISolver;
import trainer.toCase.NTActor;
import utilities.UArr;

/**
 * trained row data modeling actor
 */
public class NTRowDMActor  extends RowDMActor implements NTActor {

    protected final NetTrainerAISolver mySolver;

    NTRowDMActor(NTRowDMCase myC, NetTrainerAISolver mSolv){
        super(myC);
        mySolver = mSolv;
    }

    @Override
    public NetTrainerAISolver getMySolver(){
        return mySolver;
    }

    @Override
    public double[] prepSolverIN(){
        // code here : give double array from list from case
    }

    @Override
    public int intpSolverOUT(double[] solverOUT){
        int aX = UArr.maxVix(solverOUT);
        return aX;
    }

    @Override
    public NTCaseFeedback prepFeedbackToSolver(int decIX){
        // code here
        NTCaseFeedback ntFeedback = new NTCaseFeedback(corrCIX, rew, null);
        return ntFeedback;
    }

}
