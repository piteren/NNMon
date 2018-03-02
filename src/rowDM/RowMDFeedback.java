/*
 * 2018 (c) piteren
 */

package rowDM;

public class RowMDFeedback {

    private final Double        reward;
    private final int           corrC;

    //constructor(reward, correct class)
    RowMDFeedback(double rew, int cC){
        reward = rew;
        corrC = cC;
    }

    public final Double getReward(){
        return reward;
    }

    public final int getCorrectDecision(){
        return corrC;
    }
}