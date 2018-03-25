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
    
    NNLayType                           lType;                              // layer type
    NFtype                              myNFtype;                           // layer node function type

<<<<<<< HEAD
    final NNLearnParams                 myDLParams;                         // layer learning parameters
    
    DSdataSocket                        vIN,                                // object input FWD data
                                        vOUT,                               // object output FWD data (MDS SER)
                                        dIN,                                // object input BWD data (MDS SER)
                                        dOUT;                               // object output BWD data (MDS PAR)

    double[][]                          vWeights,                           // weights array
                                        dWeights,                           // weights gradient array
                                        lmpM,lmpV;                          // weights update memory parameters
=======
    protected final NNLearnParams       myDLParams;                         // layer learning parameters
    
    protected DSdataSocket              vIN,                                // object input FWD data
                                        vOUT,                               // object output FWD data
                                        dIN,                                // object input BWD data
                                        dOUT;                               // object output BWD data

    double[][]                          vWeights,                           // weights array
                                        dWeights;                           // weights gradient array
    double[][]                          lmpM,lmpV;                          // weights update memory parameters
>>>>>>> c93a836729b2d88c6b6ab3ec1b564746c052410c

    NNLayerNormalizer                   layNorm;                            // layerNormalizer object
    
    List<Histogram>                     myLHistograms = new LinkedList<>(); // list of layer histograms
                                
    protected int                       maxMemRecurrence = 0;               // stores (written during build) maximal level of memory recurrence for this object (max vOUT history level taken from this object during runFWD, for RRN typically ==1)
    private LinkedList<NNLay>           prevNetObjects = new LinkedList<>(),// list of objects networked PREV to this object
                                        nextNetObjects = new LinkedList<>();// list of objects networked NEXT to this object
<<<<<<< HEAD
    private LinkedList<Integer>         prevTOffConn = new LinkedList<>(),  // time offset of prev (incoming) connection
                                        nextTOffConn = new LinkedList<>();  // time offset of next (outgoing) connection

=======
    private int[]                       prevTimeOffConnection,              // time offset of prev (incoming) connection
                                        nextTimeOffConnection;              // time offset of next (outgoing) connection
>>>>>>> c93a836729b2d88c6b6ab3ec1b564746c052410c
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
        layNorm = new NNLayerNormalizer(mDLp, oW);
        initHistograms();                                                           //histograms initialization
    }
    
    // inits vIN,dIN,dOUT using networking information
    protected void finalizeBuild() {
<<<<<<< HEAD

        DSdataSocket pvOUT, tempDS;
        for(int i=0; i<prevNetObjects.size(); i++){                                         // 4 every i prev_object
            int cO = prevTOffConn.get(i);                                                   // connection time offset
            pvOUT = prevNetObjects.get(i).vOUT;                                             // prev_object vOUT
            tempDS = new DSdataSocket(pvOUT.getWidth());                                    // tempDS object common 4 this.dIN and prev_object.dOUT
            
            //vIN
            if(vIN==null) vIN = new DSmultiDataSocket(MDStype.SER, cO);
            ((DSmultiDataSocket)vIN).addDS(pvOUT,cO);
            
            //dIN
            if(dIN==null) dIN = new DSmultiDataSocket(MDStype.SER, cO);
            ((DSmultiDataSocket)dIN).addDS(tempDS,cO);
            
            //dOUT
            if(prevNetObjects.get(i).dOUT==null) prevNetObjects.get(i).dOUT = new DSmultiDataSocket(MDStype.PAR, cO);
            ((DSmultiDataSocket)prevNetObjects.get(i).dOUT).addDS(tempDS,cO);
        }

        for(int i = 0; i< nextTOffConn.size(); i++)
            if(nextTOffConn.get(i) > 0)
                for(int j = nextTOffConn.get(i); j>0; j--)
                    vOUT.setD(j-1, new double[vOUT.getWidth()]);                        //initialize vOUT @prev_history_state_to_offset_value with 0;
    }

    // inits weights
    abstract void initWeights();
=======
        for(int i=0; i<prevNetObjects.size(); i++){                                         //4 every i prev_object
            int cO = prevTimeOffConnection[i];                                              //connection time offset
            DSdataSocket    PvOUTds = prevNetObjects.get(i).vOUT,                           //prev_object vOUT
                            tempDS = new DSdataSocket(PvOUTds.getWidth());                  //tempDS object common 4 this.dIN and prev_object.dOUT
            
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
                        vOUT.setD(j-1, new double[vOUT.getWidth()]);                     //initialize vOUT @prev_history_state_to_offset_value with 0;
    }

    // inits weights
    protected abstract void initWeights();
>>>>>>> c93a836729b2d88c6b6ab3ec1b564746c052410c

    @Override
    public void connectWithNext(NNRunAndLearn nextRalObj, int tOff){
        NNLay nextNNLay = (NNLay)nextRalObj;

        nextNetObjects.add(nextNNLay);
<<<<<<< HEAD
        nextTOffConn.add(tOff);

        if(tOff > maxMemRecurrence) maxMemRecurrence = tOff;

        nextNNLay.prevNetObjects.add(this);
        nextNNLay.prevTOffConn.add(tOff);
=======
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
>>>>>>> c93a836729b2d88c6b6ab3ec1b564746c052410c
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
<<<<<<< HEAD
                if(nextTOffConn.get(i)==0)
=======
                if(nextTimeOffConnection[i]==0)
>>>>>>> c93a836729b2d88c6b6ab3ec1b564746c052410c
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
<<<<<<< HEAD
                if(prevTOffConn.get(i)==0)
=======
                if(prevTimeOffConnection[i]==0)
>>>>>>> c93a836729b2d88c6b6ab3ec1b564746c052410c
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

    // divides weight gradients by factor
    public void scaledW(double factor){
        if(dWeights!=null){
            for(int i=0; i<dWeights.length; i++)
                for(int j=0; j<dWeights[0].length; j++)
                    dWeights[i][j] = dWeights[i][j] / factor;
        }
    }

    // calculates node function (based on aF type) for given input
<<<<<<< HEAD
    static double nodeFunc(double inV, NFtype nft){
=======
    protected static double nodeFunc(double inV, NFtype nft){
>>>>>>> c93a836729b2d88c6b6ab3ec1b564746c052410c
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
<<<<<<< HEAD
    static double nodeDFunc(double oV, NFtype nft){
        double locGrad = 1;                                                     //default val for some cases
        switch(nft){
            case LIN:                                               break;
            case SIGM:              locGrad = oV*(1-oV);            break;
=======
    protected static double nodeDFunc(double oV, NFtype nft){
        double locGrad = 1;                                                     //default val for some cases
        switch(nft){
            case LIN:                                               break;
            case SIGM:              locGrad = (1-oV)*oV;            break;
>>>>>>> c93a836729b2d88c6b6ab3ec1b564746c052410c
            case TANH:              locGrad = 1-oV*oV;              break;
            case RELU:  if(oV==0)   locGrad = 0;                    break;
            case LRELU: if(oV<0)    locGrad = 0.01;                 break;
        }        
        return locGrad;        
    }

    // forces layNorm off
    protected void forceNNOff(){ layNorm.forceOff(); }
    
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