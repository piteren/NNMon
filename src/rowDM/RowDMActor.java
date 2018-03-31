/*
 * 2018 (c) piteren
 */

package rowDM;

import diffUtils.URand;
import java.util.LinkedList;

public class RowDMActor {

    protected RowDMCase             myCase;
    private LinkedList<Double>      myRewards = new LinkedList();               //actor rewards

    //constructor(case)
    public RowDMActor(RowDMCase myC){
        myCase = myC;
    }

    //makes decision
    public int makeDecision(LinkedList<Double> caseState){                      //this actor do not uses given case state
        return URand.i(myCase.numOfClasses);
    }

    //gets decision feedback from case
    public void takeFeedback(RowDMFeedback myFeedback){
        System.out.println(myFeedback.getReward());
        myRewards.add(myFeedback.getReward());                                  //takes only reward, do not takes correct decision for learning
    }
}
