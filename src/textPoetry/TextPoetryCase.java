package textPoetry;

import java.util.LinkedList;

/**
 * TEXT POETERY CASE - the learnable problem
 * holds text to learn
 * prepares current state - Character (for actor) and possible decision list
 * gets actor decision (index of decision in prepared list),
 * returns feedback to actor and moves to next state then (do not need to move to prep feedback)
 */
public class TextPoetryCase {
    
    protected final String text;                                                //text to solve
    private int strIX = 0;                                                      //current index at text, defines current state    
    protected TextPoetryActor myActor;                                          //actor of this case
    private final LinkedList<Character> possDecisions;                          //all possible decisions for this case
    
    //constructor
    public TextPoetryCase(String inText){
        text = inText;
        possDecisions = new LinkedList();
        for(int i=0; i<text.length(); i++){
            Character c = text.charAt(i);
            if(!possDecisions.contains(c)) possDecisions.add(c);
        }
        myActor = new TextPoetryActor(this);
    }    
 
    //returns text
    protected String getText(){
        return text;
    }
    //prepares and returns current state
    public Character prepCurrentState(){
        return text.charAt(strIX);
    }
    //prepares current decision list
    public LinkedList<Character> getPossDecisions(){
        return possDecisions;
    }
    
    //prepares feedback with reward and correct decision for given actor decision
    public TextPoetryFeedback prepFeedback(Character actorChoosenState){       
        int cIX = strIX+1;
        if(cIX==text.length()) cIX = 0;
        Character corrC = text.charAt(cIX);
        
        Double reward = 1.0;
        if( actorChoosenState!=corrC  ) reward = -1.0;     
        
        return new TextPoetryFeedback(reward, corrC);
    }    
    //moves case to next state
    public void moveToNextState(){
        strIX++;
        if(strIX==text.length()) strIX=0;
    }
}