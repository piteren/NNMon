/**
 * 2017 (c) piteren
 */

package trainer;

import neuralNetwork.NNLearnParams;
import neuralNetwork.NNdp;
import neuralNetwork.NNetwork;
import dataUtilities.GData;
import javafx.scene.paint.Color;
import utilities.UArr;
import utilities.URand;
import trainer.toCase.NTCaseSpecificFeedback;
import utilities.threadRun.UTRobject;

import java.util.LinkedList;

/**
 * extends NNetwork with additional fields and methods to communicate with NetTrainer and NTCase
 * calculates network errors and gradients
 */
public class NTaiSolver extends NNetwork implements UTRobject{

    private final Color             myColor;                                    // solver color (used with any colored data)
    private final GData             gErrData,                                   // (glb) data of solver errors
                                    iErrData,                                   // (int) data of solver errors
                                    gRewData,                                   // (glb) data of solver rewards
                                    iRewData,                                   // (int) data of solver rewards
                                    iPosData;                                   // (int) data of solver position among other solvers, values added by posInspector

    private double[]                myLastOut;                                  // field that stores last out of net

    private LinkedList<double[]>    inGradients = new LinkedList<>();           // list of gradients arrays, accepts null (not known gradient)
                                                                                // backward indexed (like vOUT f.e.): 0 = current history level, index 1 = previous...

    private NTCaseSpecificFeedback  gCaseFeedback,                              // case specific feedback (global)
                                    iCaseFeedback;                              // case specific feedback (interval)

    //constructor(net_path, learning parameters)
    public NTaiSolver(String path, NNLearnParams myLP){
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

    // overridden to store Out for further calculations
    @Override
    public double[] runFWD(double[] inV){
        myLastOut = super.runFWD(inV);
        return myLastOut;
    }

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
        Double errVal = null;

        // got correct classification (supervised classification case)
        if(corrCIX!=null){
            //System.out.print("out  : ");
            //for(int i=0; i<myLastOut.length; i++) System.out.print(myLastOut[i] + " ");
            //System.out.println();

            errVal = decisionError(myLastOut, corrCIX);
            // store error
            gErrData.add(errVal);
            iErrData.add(errVal);
            inGradients.addFirst(NNdp.gradCE(myLastOut, corrCIX));
            // inGradients.addFirst(NNdp.gradSVM(myLastOut, corrCIX, myDLParams.offsetSVM));
        }
        // got rewards
        if(rewVal!=null){
            // store reward
            gRewData.add(rewVal);
            iRewData.add(rewVal);
            // and still no error calculated >> reinforcement case
            if(errVal==null) {
                inGradients.addFirst(NNdp.gradReinforcement(myLastOut, rewVal));
                errVal = 1.0;
            }
        }
        // we do not have even reward >> gradient is null
        if(errVal==null) inGradients.addFirst(null);
    }

    //********************************************************************************************* solver error methods

    //returns decision error of net, SVM version by now
    public double decisionError(double[] out, int cCix){
        return NNdp.lossCE(out, cCix);
        // return NNdp.lossSVM(out, cCix, myDLParams.offsetSVM);
    }

    protected void resetGGData(){
        gErrData.flush(5);
        gRewData.flush(5);
    }

    //flushes all interval data
    public void resetIGData(int newScale){
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

    // solver in thread mode runs backprop
    @Override
    public void runTR(int mIP){
        runBWD(inGradients);
        inGradients.clear();
    }
}