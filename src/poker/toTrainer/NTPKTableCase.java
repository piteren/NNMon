/**
 * 2017 (c) piteren
 */

package poker.toTrainer;

import java.util.LinkedList;

import poker.PKTable;
import trainer.NTaiSolver;
import trainer.toCase.NTCase;
import trainer.toCase.NTActor;

/**
 * trained poker_table_case
 */
public class NTPKTableCase extends PKTable implements NTCase {
        
    private final int playerPerTableAM;                                         //player per table amount
    
    public NTPKTableCase(int numPl){
        playerPerTableAM = numPl;
        setBilindsAndStack(2,5,500);
    }
        
    @Override
    public NTCase duplicate(){
        return new NTPKTableCase(playerPerTableAM);
    }
    
    @Override
    public int caseNumOfActors(){
        return playerPerTableAM;
    }

    @Override
    public int caseNumOfClasses(){
        return 4;
    }

    @Override
    public boolean actDecisionChangesState(){
        return true;
    }
    
    @Override
    public void takeSolvers(LinkedList<NTaiSolver> sol){
        removeAllPlayers();
        for(NTaiSolver solv: sol)
            addPlayer(new NTPKPlayer(solv));
    }
    
    @Override
    public LinkedList<NTActor> getMyActors(){
        return null;
    }

    @Override
    public int[] currentPossibleDecisions(){
        return null;
    }
    
    @Override
    public void runCase(int numRuns){
        handsToRunAM=numRuns;
        while(handsToRunAM>0) runHand(0);
    }

    @Override
    public void moveCaseToNextState(int decIX){}

    @Override
    public void sampleTestRun(){
        // code here!!
    }
}