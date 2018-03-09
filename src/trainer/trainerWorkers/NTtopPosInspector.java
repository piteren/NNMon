/*
 * 2017 (c) piteren
 */
package trainer.trainerWorkers;

import dataUtilities.GData;

import java.util.List;
import javafx.scene.paint.Color;
import trainer.NTaiSolver;

/**
 * controls (reads, adds) interval solvers position data
 * prepares top_position_slov_data
 */
public class NTtopPosInspector {
    
    private boolean             processorActive;        // mark of processor activity

    // *************************************************** data processing algorithm prams
    private int                 everyNLoopTP = 10,      // number of loops to perform top_position check
                                numSolvCC = 4,          // number of top solvers to consider in pos calculations
                                fromDataRange = 50;     // calculations data range (number of considered backward data samples)

    private GData               currTopPosSolvData;     // curr circle top position data
    
    //constructor
    public NTtopPosInspector(boolean pAct){
        processorActive = pAct;
        currTopPosSolvData = new GData(1, false, 0, Color.rgb(50,200,50));
    }
    
    public boolean isProcessorActive(){
        return processorActive;
    }

    public void setProcessorActive(boolean pAct){
        processorActive = pAct;
    }

    public GData getTopPosSData(){
        return currTopPosSolvData;
    }

    //checks solvers positions and updates pos data
    public void parentsCheck(int loopNum, List<NTaiSolver> sol){
        if(processorActive && loopNum%everyNLoopTP==0){
            int drawNum = numSolvCC;
            if(drawNum > sol.size()) drawNum = sol.size();

            // update solver iPosData
            for(int i=0; i<sol.size(); i++)
                sol.get(i).getIPosData().add(i);

            // calculate & add trainerTopParentsRank
            int from = fromDataRange;
            if(from > sol.get(0).getIPosData().getSize()) from = sol.get(0).getIPosData().getSize();
            double val=0;
            for(int j=0; j<drawNum; j++)
                val += sol.get(j).getIPosData().calcSumV(from);
            val=val/from-(numSolvCC-1)*numSolvCC/2;
            currTopPosSolvData.add(val);

            // smooth data here?
        }
    }

    public void resetPosData(List<NTaiSolver> sol){
        if(currTopPosSolvData!=null)
            currTopPosSolvData.flush();
    } 
}