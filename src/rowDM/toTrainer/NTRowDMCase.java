/*
 * 2018 (c) piteren
 */
package rowDM.toTrainer;

import rowDM.RowDMCase;
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
        // code here
        // read file, put data to objects
    }

    @Override
    public NTCase duplicate(){
        // !! how duplicating should work ??
        return new NTRowDMCase(path);
    }

    @Override
    public boolean actDecisionChangesState(){
        return false;
    }

    @Override
    public LinkedList<NTRowDMActor> getMyActors(){
        // code here
    }

    @Override
    public int[] currentPossibleDecisions(){
        int[] cPD = new int[999];

        // code here
        // int[] cPD = new int[width_of_classification];
        Arrays.fill(cPD,1);
        return cPD;
    }

    @Override
    public void moveCaseToNextState(int decIX){
        moveToNextRow();
    }

}