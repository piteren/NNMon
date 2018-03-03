/*
 * 2017 (c) piteren
 */

package textPoetry;

import java.util.LinkedList;
import utilities.URand;

/**
 * text poetry actor - decider
 * reads state and possible decision list from given case
 * makes decision and takes feedback
 */
public class TextPoetryActor {
    
    protected TextPoetryCase        myCase;
    private LinkedList<Double>      myRewards = new LinkedList();               //actor rewards
    
    //constructor
    public TextPoetryActor(TextPoetryCase myC){
        myCase = myC;
    }
  
    //makes decision
    public int makeDecision(){
        char givenC = myCase.prepCurrentState();                                //this actor do not uses this information
        LinkedList<Character> possD = myCase.getPossDecisions();
        return URand.i(possD.size());
    }

    //gets decision feedback from case
    public void takeFeedback(TextPoetryFeedback myFeedback){
        myRewards.add(myFeedback.getReward());                                  //takes only reward, do not takes correct decision for learning
    }
}