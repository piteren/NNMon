/**
 * 2017 (c) piteren
 */

package poker;

import java.util.LinkedList;

import diffUtils.URand;

/**
 * poker player, keeps hand cards and stack
 * communicates with table about decisions   
 */
public class PKPlayer { 
    
    protected PKTable               myTable;                    // player table
    private PKCard                  cA;                         // card B of player
    private PKCard                  cB;                         // card B of player
    protected int                   stack;                      // player actual stack
    protected int                   vPut;                       // cash put yet to pot (on active river)
    private LinkedList<PKDecision>  myPossibleDecisions;        // player possible decisions
    
    protected PKPlayerStats         myIntervalStats;            // player statistics
    
    public PKPlayer(){
        myIntervalStats = new PKPlayerStats();
    }
    
    protected void setMyPDecisions(LinkedList<PKDecision> de){
        myPossibleDecisions = de;
    }
    protected LinkedList<PKDecision> myPossibleDecisions(){
        return myPossibleDecisions;
    }
    
    public void setMyCards(PKCard ca, PKCard cb){
        cA = ca;
        cB = cb;
    }
    public PKCard cA(){
        return cA;
    }
    public PKCard cB(){
        return cB;
    }
    
    public PKDecision makeDecision(){                                           //returns decision made by player
        return myPossibleDecisions.get(URand.i(myPossibleDecisions.size()));
    }
    
    protected void addHandStats(PKPlayerStats handStats){ myIntervalStats.merge(handStats); }
}//PKPlayer