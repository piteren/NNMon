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

import java.util.LinkedList;

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
        LinkedList<Double> cState = myCase.prepCurrentState();
        double[] in = new double[cState.size()];
        for(int i=0; i<in.length; i++)
            in[i] = cState.get(i);
        return in;
    }

    @Override
    public NTCaseFeedback prepFeedbackToSolver(int decIX){
        RowMDFeedback caseFeedback = myCase.prepFeedback(decIX);
        Integer corrCIX = caseFeedback.getCorrectDecision();
        Double rew = caseFeedback.getReward();
        NTCaseFeedback ntFeedback = new NTCaseFeedback(corrCIX, rew, null);
        return ntFeedback;
    }

}
