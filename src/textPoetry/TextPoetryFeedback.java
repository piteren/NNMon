package textPoetry;

/**
 * text poetry feedback
 * prepared by case, taken by actor
 */
public class TextPoetryFeedback {
    
    private final Double reward;
    private final Character corrC;
    
    //constructor(reward, correct decision)
    TextPoetryFeedback(double rew, char cD){
        reward = rew;
        corrC = cD;
    }
    
    public final Double getReward(){
        return reward;
    }
    
    public final Character getCorrectDecision(){
        return corrC;
    }
}