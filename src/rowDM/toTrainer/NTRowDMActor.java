/*
 * 2018 (c) piteren
 */

package rowDM.toTrainer;

import rowDM.RowDMActor;
import rowDM.RowDMFeedback;
import trainer.NTCaseFeedback;
import trainer.NTaiSolver;
import trainer.toCase.NTActor;

import java.util.LinkedList;

/**
 * trained row data modeling actor
 */

public class NTRowDMActor extends RowDMActor implements NTActor {

    protected final NTaiSolver mySolver;

    NTRowDMActor(NTRowDMCase myC, NTaiSolver mSolv){
        super(myC);
        mySolver = mSolv;
    }

    @Override
    public NTaiSolver getMySolver(){
        return mySolver;
    }

    @Override
    public double[] prepSolverIN(){
        LinkedList<Double> cState = myCase.prepCurrentState();
        double[] in = new double[cState.size()];
        for(int i=0; i<in.length; i++)
            in[i] = cState.get(i);
        return in;
    }

    @Override
    public NTCaseFeedback prepFeedbackToSolver(int decIX){
        RowDMFeedback caseFeedback = myCase.prepFeedback(decIX);
        Integer corrCIX = caseFeedback.getCorrectDecision();
        Double rew = caseFeedback.getReward();
        NTCaseFeedback ntFeedback = new NTCaseFeedback(corrCIX, rew, null);
        return ntFeedback;
    }

}
