/*
 * 2018 (c) piteren
 */

package rowDM;

public class RowDMFeedback {

    private final Double    reward;     // reward value
    private final Integer   corrC;      // correct classification index

    //constructor(reward, cClassif)
    RowDMFeedback(Double rew, Integer cCl){
        reward = rew;
        corrC = cCl;
    }

    public final Double getReward(){ return reward; }

    public final Integer getCorrectDecision(){ return corrC; }
}