package poker.toTrainer;

import poker.PKPlayerStats;
import trainer.toCase.NTCaseSpecificFeedback;

public class NTPKGameCaseSpecFeedback extends PKPlayerStats implements NTCaseSpecificFeedback{
        
    @Override
    public void merge(NTCaseSpecificFeedback feedbackToAdd){
        super.merge((PKPlayerStats)feedbackToAdd);
    }
    @Override
    public String toString(){
        return super.toString();
    }
}