/**
 * 2017 (c) piteren
 */

package neuralNetwork;

import genX.GXgenXinterface;
import dataUtilities.DSmultiDataSocket;
import dataUtilities.DSdataSocket;
import dataUtilities.DSmultiDataSocket.MDStype;
import dataUtilities.HistoFace;
import dataUtilities.Histogram;
import java.util.LinkedList;
import java.util.List;

/**
 * abstract class for NN layer
 * layer has nodes and learnable parameters
 */
public abstract class NNLay implements NNRunAndLearn, GXgenXinterface, HistoFace {
    
    NNLayType lType;                                                      // layer type
    NFtype                            myNFtype;                           // layer activation function type

    protected final NNLearnParams     myDLParams;                         // layer learning parameters
    
    protected DSdataSocket            vIN,                                // object input FWD data
                                      vOUT,                               // object output FWD data
                                      dIN,                                // object input BWD data
                                      dOUT;                               // object output BWD data

    double[][]                        vWeights,                           // weights array
                                      dWeights;                           // weights gradient array
    double[][]                        lmpM,lmpV;                          // weights update memory parameters

    NNodeNormalizer[]                 nodeNorm;                           // node normalization objects
    
    List<Histogram>                   myLHistograms = new LinkedList<>(); // list of layer histograms
                                
    protected int                     maxMemRecurrence = 0;               // stores (written during build) maximal level of memory recurrence for this object (max vOUT history level taken from this object during runFWD, for RRN typically ==1)
    private LinkedList<NNLay>         prevNetObjects = new LinkedList<>(),// list of objects networked PREV to this object
                                      nextNetObjects = new LinkedList<>();// list of objects networked NEXT to this object
    private int[]                     prevTimeOffConnection,              // time offset of prev (incoming) connection
                                      nextTimeOffConnection;              // time offset of next (outgoing) connection
    // layer type
    public enum NNLayType {
        FC,
        PW,
        NET
    }

    // node activation function type
    public enum NFtype {
        LIN,
        SIGM,
        TANH,
        RELU,
        LRELU,
    }
                    
    // constructor(parent, width)
    NNLay(NNLearnParams mDLp, int oW) {
        myDLParams = mDLp;
        vOUT = new DSdataSocket(oW);                                                //vOUT initialization
        initNNorm(oW);                                                              //node normalization objects init
        initHistograms();                                                           //histograms initialization
    }
    
    // inits vIN,dIN,dOUT using networking information
    protected void finalizeBuild() {
        DSdataSocket tempDS;
        for(int i=0; i<prevNetObjects.size(); i++){                                         //4 every i prev_object
            int cO = prevTimeOffConnection[i];                                              //connection time offset
            DSdataSocket PvOUTds = prevNetObjects.get(i).vOUT;                              //prev_object vOUT
            tempDS = new DSdataSocket(PvOUTds.getWidth());                                  //tempDS object common 4 this.dIN and prev_object.dOUT
            
            //vIN
            if(vIN==null) vIN = new DSmultiDataSocket(PvOUTds,MDStype.SER,cO);              //create vIN with prev_object vOUT
            else ((DSmultiDataSocket)vIN).addDS(PvOUTds,cO);                                //add prev_object vOUT to vIN 
            
            //dIN
            if(dIN==null) dIN = new DSmultiDataSocket(tempDS,MDStype.SER,cO);               //create dIN with tempDS
            else ((DSmultiDataSocket)dIN).addDS(tempDS,cO);                                 //add tempDS to dIN 
            
            //dOUT
            if(prevNetObjects.get(i).dOUT==null)
                prevNetObjects.get(i).dOUT = new DSmultiDataSocket(tempDS,MDStype.PAR,cO);  //initialize dOUT of prev (MDS,1)
            else ((DSmultiDataSocket)prevNetObjects.get(i).dOUT).addDS(tempDS,cO);          //or add to it
        }
        if(nextTimeOffConnection!=null)
            for(int i=0; i<nextTimeOffConnection.length; i++)
                if(nextTimeOffConnection[i]>0)    
                    for(int j=nextTimeOffConnection[i]; j>0; j--)
                        vOUT.setD(j-1, new double[vOUT.getWidth()]);                        //initialize vOUT @prev_history_state_to_offset_value with 0;     
    }

    // inits weights
    protected abstract void initWeights();

    @Override
    public void connectWithNext(NNRunAndLearn nextRalObj, int tOff){
        NNLay nextNNLay = (NNLay)nextRalObj;

        nextNetObjects.add(nextNNLay);
        int[] ntOA;
        if(nextTimeOffConnection!=null){
            ntOA = new int[nextTimeOffConnection.length+1];
            for(int i=0; i<nextTimeOffConnection.length; i++)
                ntOA[i] = nextTimeOffConnection[i];
            ntOA[nextTimeOffConnection.length] = tOff;
        }
        else{
            ntOA = new int[1];
            ntOA[0] = tOff;
        }
        nextTimeOffConnection = ntOA;
        if(tOff > maxMemRecurrence) maxMemRecurrence = tOff;

        nextNNLay.prevNetObjects.add(this);
        int[] ptOA;
        if(nextNNLay.prevTimeOffConnection!=null){
            ptOA = new int[nextNNLay.prevTimeOffConnection.length+1];
            for(int i=0; i<nextNNLay.prevTimeOffConnection.length; i++)
                ptOA[i] = nextNNLay.prevTimeOffConnection[i];
            ptOA[nextNNLay.prevTimeOffConnection.length] = tOff;
        }
        else{
            ptOA = new int[1];
            ptOA[0] = tOff;
        }
        nextNNLay.prevTimeOffConnection = ptOA;
    }

    // restarts (sets to initial values) parameteres of learn method
    public abstract void restartLrnMethodParams();
    
    //************************************************************************** methods of running FWD & BWD

    // calculates vOUT_fwd
    abstract protected void calcVout();

    // calculates dIN_bwd
    abstract protected void calcDin(int h);

    @Override
    public void runFWD(){
        if(vIN.isDataReadReady(0)){    
            if(myLHistograms.get(0).isActive()) myLHistograms.get(0).build(vIN.getD(0));        //vIN histogram
            
            calcVout();
            
            if(myLHistograms.get(7).isActive()) myLHistograms.get(7).build(vOUT.getD(0));       //vOUT histogram
            
            //runs recurrently on next net objects
            for(int i=0; i<nextNetObjects.size(); i++)
                if(nextTimeOffConnection[i]==0)
                    nextNetObjects.get(i).runFWD();                                             
        }
    }

    @Override
    public void runBWD(int h){
        if(dOUT.isDataReadReady(h)){
            if(myLHistograms.get(8).isActive()) myLHistograms.get(8).build(dOUT.getD(h));           //dOUT histogram

            calcDin(h);

            //runs recurrently on prev net objects
            for(int i=0; i<prevNetObjects.size(); i++)
                if(prevTimeOffConnection[i]==0)
                    prevNetObjects.get(i).runBWD(h);                                            
        }
    }

    // returns max abs value of weight gradient
    public double maxdW(){
        double maxD = 0;
        if(dWeights!=null){
            for(int i=0; i<dWeights.length; i++)
                for(int j=0; j<dWeights[0].length; j++)
                    if(Math.abs(dWeights[i][j]) > maxD) maxD = Math.abs(dWeights[i][j]);
        }
        return maxD;
    }

    // divides weight gradients with scl factor
    public void scaledW(double scl){
        if(dWeights!=null){
            for(int i=0; i<dWeights.length; i++)
                for(int j=0; j<dWeights[0].length; j++)
                    dWeights[i][j] = dWeights[i][j] / scl;
        }
    }

    // calculates node function (based on aF type) for given input
    protected static double nodeFunc(double inV, NFtype nft){
        double out = inV;                                                       //default val for some cases
        switch(nft){
            case LIN:                                               break;
            case SIGM:              out = 1/(1 + Math.exp(-inV));   break;
            case TANH:              out = Math.tanh(inV);           break;
            case RELU:  if(inV<0)   out = 0;                        break;
            case LRELU: if(inV<0)   out = 0.01*inV;                 break;
        }
        return out;
    }

    // calculates node function derivative (based on aF type) for given output of function
    protected static double nodeDFunc(double oV, NFtype nft){
        double locGrad = 1;                                                     //default val for some cases
        switch(nft){
            case LIN:                                               break;
            case SIGM:              locGrad = (1-oV)*oV;            break;
            case TANH:              locGrad = (1-oV*oV);            break;
            case RELU:  if(oV==0)   locGrad = 0;                    break;
            case LRELU: if(oV<0)    locGrad = 0.01;                 break;
        }        
        return locGrad;        
    }

    // initializes node normalization objects
    protected void initNNorm(int oW){
        nodeNorm = new NNodeNormalizer[oW];
        for(int i= 0; i<nodeNorm.length; i++)
            nodeNorm[i] = new NNodeNormalizer(myDLParams);
    }

    // forces NNorm off
    protected void forceNNOff(){
        for(int i= 0; i<nodeNorm.length; i++)
            nodeNorm[i].forceOff();
    }
    
    //********************************************************************* methods returning some information about Lay

    public NNLayType getLayType(){
        return lType;
    }

    // returns object number of nodes (nodes are are common features for most object implementations, so such method makes sense)
    abstract public int nNodes();

    // returns object number of learnable parameters (usually learnable object has parameters to learn, this method returns number of them)
    abstract public int nParam();

    //********************************************************************************* HistoFace methods implementation

    @Override
    public final void initHistograms() {
        /*
        0-vIN
        1-vW
        2-dW
        3-nNN
        4-nnOff
        5-nnScl
        6-vNOD
        7-vOUT
        8-dOUT
        */
        for(int i=0; i<9; i++)
            myLHistograms.add( new Histogram() );
    }

    @Override
    public List<Histogram> getHistograms() {
        return myLHistograms;
    }

    @Override
    public void killHistograms() {
        for(Histogram h: myLHistograms) h.deleteObservers();
        myLHistograms.clear();
        myLHistograms = null;
    }
}