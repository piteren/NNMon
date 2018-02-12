/*
 * 2017 (c) piteren
 */
package trainer;

import trainer.toCase.NTCaseSpecificFeedback;

/**
 * feedback data from case
 * specifies format of feedback (fields type, that are written in constructor)
 */
public class NTCaseFeedback {
    
    private final Integer                   correctClassificationIX;            //correct classification index
    private final Double                    reward;                             //reward value SHOULD BE 0 MEAN 1 MAX
    private final NTCaseSpecificFeedback    caseFeedback;                       //feedback in case "language" (class to be implemented on case side ...or "null" object)
    
    //constructor
    public NTCaseFeedback(  Integer corrC,                      //correct cl index
                            Double rew,                         //reward
                            NTCaseSpecificFeedback cSF)         //case specific feedback
    {
        correctClassificationIX = corrC;
        reward = rew;
        caseFeedback = cSF;
    }
    
    //returns correct classification array
    public final Integer getCorrectClassificationIX(){
        return correctClassificationIX;
    }
    //returnd reward
    public final Double getReward(){
        return reward;
    }
    //returnd caseFeedback
    public final NTCaseSpecificFeedback getCaseFeedback(){
        return caseFeedback;
    }
}