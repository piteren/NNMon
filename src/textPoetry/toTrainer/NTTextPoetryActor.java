/*
 * 2017 (c) piteren
 */
package textPoetry.toTrainer;

import textPoetry.TextPoetryActor;
import textPoetry.TextPoetryFeedback;
import trainer.NTaiSolver;
import trainer.NTCaseFeedback;
import trainer.toCase.NTActor;

/**
 * trained text poetry actor
 */
public class NTTextPoetryActor extends TextPoetryActor implements NTActor {
    
    protected final NTaiSolver mySolver;
    
    NTTextPoetryActor(NTTextPoetryCase myC, NTaiSolver mSolv){
        super(myC);
        mySolver = mSolv;
    }
    
    @Override
    public NTaiSolver getMySolver(){
        return mySolver;
    }

    @Override
    public double[] prepSolverIN(){
        Character givenC = myCase.prepCurrentState();
        double[] binArr = new double[((NTTextPoetryCase)myCase).numOfChars];
        binArr[(int)givenC-((NTTextPoetryCase)myCase).startingCharNum] = 1.0;
        return binArr;
    }

    @Override 
    public NTCaseFeedback prepFeedbackToSolver(int decIX){
        Character actCh = (char)( ((NTTextPoetryCase)myCase).startingCharNum + decIX );
        TextPoetryFeedback caseFeedback = myCase.prepFeedback(actCh);
        Integer corrCIX = (int)caseFeedback.getCorrectDecision()-((NTTextPoetryCase)myCase).startingCharNum;
        Double rew = caseFeedback.getReward();
        NTCaseFeedback ntFeedback = new NTCaseFeedback(corrCIX, rew, null);
        return ntFeedback;
    }
}