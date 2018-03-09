package trainer;

import deepLearn.NNetwork;
import deepLearn.DLlearnParams;
import dataUtilities.GData;
import javafx.scene.paint.Color;
import utilities.URand;
import trainer.toCase.NTCaseSpecificFeedback;
import utilities.threadRun.UTRobject;

/*
 * extends NNetwork with additional fields and methods 4 communication with trainer and Envy
 */
public class NTaiSolver extends NNetwork implements UTRobject{

    private final Color             myColor;                                    // solver color (used with any colored data)
    private final GData             gErrData,                                   // (glb) data of solver errors
                                    iErrData,                                   // (int) data of solver errors
                                    gRewData,                                   // (glb) data of solver rewards
                                    iRewData,                                   // (int) data of solver rewards
                                    iPosData;                                   // (int) data of solver position among other solvers, values added by posInspector

    private NTCaseSpecificFeedback  gCaseFeedback,                              // case specific feedback (global)
                                    iCaseFeedback;                              // case specific feedback (interval)

    //constructor(net_path, learnig parameters)
    public NTaiSolver(String path, DLlearnParams myLP){
        super(myLP, path);
        myColor = Color.rgb(URand.i(200),URand.i(200),URand.i(200));

        iPosData = new GData(1,false,   0, myColor);

        gErrData = new GData(5,false,1000, myColor);
        iErrData = new GData(1,false,1000, myColor);
        gRewData = new GData(5,true, 1000, myColor);
        iRewData = new GData(1,true, 1000, myColor);
    }

    public GData getIPosData(){ return iPosData; }
    public GData getGErrData(){ return gErrData; }
    public GData getIErrData(){ return iErrData; }
    public GData getGRewData(){ return gRewData; }
    public GData getIRewData(){ return iRewData; }
    public Color getMyColor(){ return myColor; }

    //takes feedback from case and puts data into proper objects
    public void takeCaseFeedback(NTCaseFeedback myFeedback){
        // merge interval feedback
        if(iCaseFeedback==null) iCaseFeedback = myFeedback.getCaseFeedback();
        else iCaseFeedback.merge(myFeedback.getCaseFeedback());

        // merge global feedback
        // ********** !! gCaseFeedback is turned off by now to limit memory consumption
        // if(gCaseFeedback==null) gCaseFeedback = myFeedback.getCaseFeedback();
        // else gCaseFeedback.merge(myFeedback.getCaseFeedback());

        Integer corrCIX = myFeedback.getCorrClassfIX();
        Double rewVal = myFeedback.getReward();
        //add index of correct classification to list of net and calculated error value to error list
        if(corrCIX!=null){
            corrCix.addFirst(corrCIX);
            double errVal = decisionError(vOUT.getD(0),corrCIX);
            gErrData.add(errVal);
            iErrData.add(errVal);
        }

        //add rewVal to list of net (even if == Null it is needed)
        rewardVal.addFirst(rewVal);

        //add reward to solver rewards list
        if(rewVal!=null){
            gRewData.add(rewVal);
            iRewData.add(rewVal);
        }
    }

    protected void resetGData(){
        gErrData.flush(5);
        gRewData.flush(5);
    }

    //flushes all interval data
    public void resetIData(int newScale){
        if(iErrData!=null) iErrData.flush(newScale);    // error
        if(iRewData!=null) iRewData.flush(newScale);    // reward
        if(iPosData!=null) iPosData.flush();            // pos
        if(iCaseFeedback!=null) iCaseFeedback.flush();  // case feedback
    }

    //returns string with description of interval performance
    protected String myIntervalPerformance(){
        String perf;
        if(iCaseFeedback!=null) perf = ".envySfeedback: "+iCaseFeedback.toString();
        else perf = "rewarded: "+String.valueOf(iRewData.getLastV())+"  avg circle error: "+String.valueOf(iErrData.calcAvgV());
        return perf;
    }

    @Override
    public void runTR(int mIP){
        runBWD();
    }
}