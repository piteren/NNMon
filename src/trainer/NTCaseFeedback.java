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
    
    private final Integer                   correctClassificationIX;    // correct classification index
    private final Double                    reward;                     // reward value SHOULD BE 0 MEAN 1 MAX
    private final NTCaseSpecificFeedback    caseFeedback;               // feedback in case "language"
                                                                        // ...to be implemented on case side ...or "null" if case has no specific feedback)
    //constructor(cClassif, reward, case_feedback)
    public NTCaseFeedback(Integer corrC, Double rew, NTCaseSpecificFeedback cSF)
    {
        correctClassificationIX = corrC;
        reward = rew;
        caseFeedback = cSF;
    }
    
    //returns correct classification array
    public final Integer getCorrClassfIX(){
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