/*
 * 2017 (c) piteren
 */
package trainer.trainerWorkers;

import dataUtilities.GData;

import java.util.List;
import javafx.scene.paint.Color;
import trainer.NetTrainerAISolver;

/**
 * controls interval solvers position among list, processes and prepares position data
 */
public class NTtopPosInspector {
    
    private boolean             processorActive;                                //mark for activity of processor
    
    private int                 everyNLoopTP = 10,                              //number of loops to perform top_position check
                                numSolvCC = 4,                                  //number of solvers to consider in pos calculations
                                fromRange = 50;                                 //range of calculations
    private GData               topPosSolvData,                                 //top position data for training solvers
                                prevtopPosSolvData;                             //previous circle top position data
    
    //constructor
    public NTtopPosInspector(boolean pAct){
        processorActive = pAct;
        topPosSolvData = new GData(1, false, 0, Color.rgb(50,200,50));
        prevtopPosSolvData = null;
    }
    
    public boolean isProcessorActive(){
        return processorActive;
    }
    public void setProcessorActive(boolean pAct){
        processorActive = pAct;
    }
    public GData getTopPosSData(){
        return topPosSolvData;
    }

    //checks solvers positions and updates pos data
    public void parentsCheck(int loopNum, NTSolversManager solvMan){
        if(processorActive && loopNum%everyNLoopTP==0){
            List<NetTrainerAISolver> sol = solvMan.getSolvers(NTSolversManager.SolvOrder.SORTED);
            int drawNum = numSolvCC;
            if(drawNum > sol.size()) drawNum = sol.size();

            for(int i=0; i<sol.size(); i++)
                sol.get(i).getIPosData().add(i);                                //update solver PosData
            //calculate and add trainerTopParentsRank
            
            int from = fromRange;
            if(from > sol.get(0).getIPosData().getSize()) from = sol.get(0).getIPosData().getSize();
            double val=0;
            for(int j=0; j<drawNum; j++)
                val += sol.get(j).getIPosData().calcSumV(from);
            val=val/from-(numSolvCC-1)*numSolvCC/2;
            topPosSolvData.add(val);

            // here draws data

            //GData smoothedPD = topPosSolvData.smooth(10);
            // smoothedPD.setColor(Color.rgb(50,100,50));
            // here draws smoothed data
            // if(prevtopPosSolvData!=null) myTrainerUI.ParentsPanel.addGdata(prevtopPosSolvData);
        }
    }
    
    
    //writes current PD to previous, clears PosData 4 all solvers and current 4 trainer
    public void resetPosData(List<NetTrainerAISolver> sol){
        if(processorActive){
            for(NetTrainerAISolver pl: sol) pl.getIPosData().flush();
            //prevtopPosSolvData = topPosSolvData.smooth(10);
            topPosSolvData.flush();
        }
    } 
}