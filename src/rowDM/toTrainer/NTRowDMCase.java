/*
 * 2018 (c) piteren
 */

package rowDM.toTrainer;

import rowDM.RowDMCase;
import trainer.NTaiSolver;
import trainer.toCase.NTActor;
import trainer.toCase.NTCase;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * trained row data modeling case
 */

public class NTRowDMCase extends RowDMCase implements NTCase {

    private final String path;                                                  //file with data

    //constructor(row_data_filepath)
    public NTRowDMCase(String pth){
        super(pth);
        path = pth;
    }

    @Override
    public NTCase duplicate(){ return new NTRowDMCase(path); }

    @Override
    public int caseNumOfActors(){ return 1; }

    @Override
    public int caseNumOfClasses(){ return numOfClasses; }

    @Override
    public boolean actDecisionChangesState(){ return false; }

    @Override
    public void takeSolvers(LinkedList<NTaiSolver> solvers){
        myActor = new NTRowDMActor(this, solvers.get(0));
    }

    @Override
    public LinkedList<NTActor> getMyActors(){
        LinkedList<NTActor> myAct = new LinkedList();
        myAct.add( (NTActor)myActor );
        return myAct;
    }

    @Override
    public int[] currentPossibleDecisions(){
        int[] cPD = new int[numOfClasses];
        Arrays.fill(cPD,1);
        return cPD;
    }

    @Override
    public void moveCaseToNextState(int decIX){
        moveToNextRow();
    }

    @Override
    public void sampleTestRun(){
        // code here!!
    }
}