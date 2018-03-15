/**
 * 2017 (c) piteren
 */

package neuralNetwork;

import genX.GXgenXinterface;
import dataUtilities.DSmultiDataSocket;
import dataUtilities.DSdataSocket;
import neuralNetwork.NNLayerPW.PWlayType;

import java.lang.reflect.Array;
import java.util.List;
import java.util.LinkedList;
import utilities.UArr;

import utilities.UFileOperator;

/**
 * network class, has subnetwork,
 * creates subnetwork (builds from file)
 * may act as a standalone net (FWD and BWD may be run with given input)
 * or may be element of other subnetwork (has parent) - acts like a NNLay so it still is connectable, learnable
 */
public class NNetwork extends NNLay {

    private final LinkedList<NNLay>     ALLsubLayers = new LinkedList<>();  // all subnetwork layers of this net
    private SupportingIOlay             FIOlay,                             // first IO supporting net layer
                                        LIOlay;                             // last  IO supporting net layer
    
    private int                         runFWDcounter = 0;                  // counter of FWD runs since last backprop, incremented only when backprop is enabled
    
    private long                        sclCounter = 0;
    private double                      sclAmount = 0;

    //IN nad OUT supporting layer, used to easily exchange data with subnet
    private class SupportingIOlay extends NNLay {
        
        SupportingIOlay(NNLearnParams mDLp, int oW){
            super(mDLp, oW);                        
        }

        @Override
        protected void initWeights(){}

        @Override
        public void restartLrnMethodParams(){}

        @Override
        protected void calcVout(){
            double[] vOUTtempArray = vIN.getD(0);
            
            //vOUT norm (network input normalization)
            for(int o=0; o<vOUT.getWidth(); o++)
                vOUTtempArray[o] = nodeNorm[o].processSample(vOUTtempArray[o]);
            
            vOUT.setD(0, vOUTtempArray); 
        }

        @Override
        protected void calcDin(int h){
            double[] dNODEtemp = dOUT.getD(h);
            for(int o=0; o<dNODEtemp.length; o++)
                dNODEtemp[o] *= nodeNorm[o].getNNscl();

            dIN.setD(h, dNODEtemp);
        }

        @Override
        public void updateLearnableParams(){}

        @Override 
        public void genX(GXgenXinterface parA, GXgenXinterface parB){}

        @Override
        public int nParam(){ return 0; }

        @Override
        public int nNodes(){ return 0; }
    }
    
    //constructor(path of nn file)
    public NNetwork(NNLearnParams mDLp, String path){
        super(mDLp,0);
        lType = NNLayType.NET;
        vOUT = null;                                                            //delete vOUT, we don't know the width now, we will create it later (...in finalize)
        buildDescriptionBasedNet((new UFileOperator(path)).readFile());
    }
    
    //********************************************************************************************* builds and finalizes

    //builds network based on (String) decription
    private void buildDescriptionBasedNet(List<String> desc){
        int lineIX=0;
        String[] line;
        do{
            line = desc.get(lineIX).split(" ");
             // ?? line = desc.get(lineIX).split("\\s+");
            switch(line[0]){
                case "<laye>":
                    switch(line[2]){
                        case "IN": ALLsubLayers.add(new SupportingIOlay(myDLParams, Integer.parseInt(line[3]))); break;
                        case "FC":
                            NFtype lNFtype = null;
                            switch(line[4]){
                                case "LIN":     lNFtype = NFtype.LIN;   break;
                                case "SIGM":    lNFtype = NFtype.SIGM;  break;
                                case "TANH":    lNFtype = NFtype.TANH;  break;
                                case "RELU":    lNFtype = NFtype.RELU;  break;
                                case "LRELU":   lNFtype = NFtype.LRELU; break;
                            }
                            ALLsubLayers.add(new NNLayerFC(myDLParams, Integer.parseInt(line[3]), lNFtype));                   
                            break;
                        case "PW":
                            PWlayType lPWlayType = null;
                            switch(line[4]){
                                case "ADD":     lPWlayType = PWlayType.ADD; break;
                                case "MUL":     lPWlayType = PWlayType.MUL; break;
                                case "NOP":     lPWlayType = PWlayType.NOP; break;
                                case "REV":     lPWlayType = PWlayType.REV; break;
                                case "TNH":     lPWlayType = PWlayType.TNH; break;
                            }
                            ALLsubLayers.add(new NNLayerPW(myDLParams, Integer.parseInt(line[3]), lPWlayType));
                            break;
                        case "NN": ALLsubLayers.add(new NNetwork(myDLParams, "path here")); break;
                    }
                    break;
                case "<topo>":
                    int                 fLIX = Integer.parseInt( line[1].substring( 0, line[1].length()-1) ),
                                        tLIX;                                   //from & to layer IX in ALLlayers     
                    NNLay layF = ALLsubLayers.get(fLIX),
                                        layT;                                   //from & to layer pointer
                    for(int i=2; i<line.length; i++){
                        //resolve to layer
                        tLIX = Integer.parseInt(line[i]);
                        layT=ALLsubLayers.get(tLIX);

                        int offTime = 1;    // BY NOW WE ASSUME that writing to memory may be done ONLY by layers with index less or equal (to this layer) and with time_offset==1
                        if(fLIX<tLIX) offTime = 0;
                        layF.connectWithNext(layT, offTime);
                    }
                    break;
            }
            lineIX++;
        }while(!line[0].equals("<eond>"));

        finalizeBuild();
        initWeights();
    }

    @Override
    public void initWeights(){
        for(NNLay lay: ALLsubLayers) lay.initWeights();
    }

    @Override
    protected void finalizeBuild(){
        //finalize first IO layer
        FIOlay = (SupportingIOlay)ALLsubLayers.removeFirst();
        
        //add last IO layer
        LIOlay = new SupportingIOlay(myDLParams,ALLsubLayers.getLast().vOUT.getWidth());
        ALLsubLayers.getLast().connectWithNext(LIOlay,0);

        // turn off NNorm @ last layers
        ALLsubLayers.getLast().forceNNOff();
        LIOlay.forceNNOff();
        
        //finalize all supporting and sub layers
        FIOlay.finalizeBuild();
        for(NNLay lay: ALLsubLayers) lay.finalizeBuild();
        LIOlay.finalizeBuild();
        
        //search for max mem recurrence of net
        for(NNLay lay: ALLsubLayers)
            if(lay.maxMemRecurrence > this.maxMemRecurrence)
                this.maxMemRecurrence = lay.maxMemRecurrence;

        vOUT = LIOlay.vOUT;                                                                     //vOUT reference connection
        
        super.finalizeBuild();
        
        //case when this net has no other connected before (is first and gets vIN from envy)
        if(vIN==null){       
            vIN = new DSdataSocket(FIOlay.vOUT.getWidth());
            FIOlay.dIN = new DSdataSocket(FIOlay.vOUT.getWidth());                              //dIN may left null or: dIN = firstNetLayer.dIN
        }
        else FIOlay.dIN = dIN;                                                                  //reference connection
        FIOlay.vIN = vIN;                                                                       //vIN reference connection
        
        //case when this net has no other connected after (is last and gets dOUT from envy)
        if(dOUT==null)       
            dOUT = new DSdataSocket(LIOlay.vOUT.getWidth());
        else LIOlay.dOUT = dOUT;
        
        //lastNetLayer dOUT   !! it wont work PROBABLY with dOUT==MDS
        if(LIOlay.dOUT==null)
            LIOlay.dOUT = new DSmultiDataSocket(dOUT,DSmultiDataSocket.MDStype.PAR,0);  //initialize with net_dOUT
        else ((DSmultiDataSocket)LIOlay.dOUT).addDS(dOUT,0);                            //or add net_dOUT
    }

    @Override
    public void restartLrnMethodParams(){
        for(NNLay lay: getALLsubLayers())
            lay.restartLrnMethodParams();
    }
    
    public LinkedList<NNLay> getALLsubLayers(){
        return ALLsubLayers;
    }
    
    //************************************************************************************** main running methods of net

    @Override
    protected void calcVout() {
        FIOlay.vOUT.moveDataTSF();
        for(NNLay lay: ALLsubLayers) lay.vOUT.moveDataTSF();
        LIOlay.vOUT.moveDataTSF();

        FIOlay.runFWD();                                                     //run FWD subnet
    }

    @Override
    protected void calcDin(int h) { LIOlay.runBWD(h); }                       //run BWD subnet

    //runs net forward with given input data array (nnetwork is top layer = self runnable), returns vOUT
    public double[] runFWD(double[] inV){
        vIN.moveDataTSF();
        vIN.setD(0, inV);
        
        runFWD();
        
        if(myDLParams.doBackprop.getValue()) runFWDcounter++;
        else runFWDcounter = 0;
        
        double[] outArr = vOUT.getD(0);
        
        // resets recorded data (all but maxMemRecurrence old) in case we do not backprop (free memory)
        if(!myDLParams.doBackprop.getValue()){
            FIOlay.vOUT.resetFrom(maxMemRecurrence);
            for(NNLay lay: ALLsubLayers)
                lay.vOUT.resetFrom(maxMemRecurrence);
            LIOlay.vOUT.resetFrom(maxMemRecurrence);
        } 
        return outArr;
    }

    // runs net backward with given gradients list
    // sets gradients and preforms parameters update
    // !! resets data sockets after all - !! put into method, call after all networks...
    public void runBWD(LinkedList<double[]> grList){

        dOUTset(grList);
        
        for(int i=0; i<runFWDcounter; i++) runBWD(i);
        runFWDcounter = 0;
        
        updateLearnableParams();
        
        //reset data sockets
        FIOlay.dIN.resetFrom(0);
        FIOlay.vOUT.resetFrom(maxMemRecurrence);
        for(NNLay lay: ALLsubLayers){
            lay.dIN.resetFrom(0);
            lay.vOUT.resetFrom(maxMemRecurrence);
        }
        LIOlay.dIN.resetFrom(0);
        LIOlay.vOUT.resetFrom(maxMemRecurrence);
        dOUT.resetFrom(0);
    }

    // sets dOUT of net with given gradients list
    private void dOUTset(LinkedList<double[]> grList){
        // prepare complete list (replace nulls with divided gradients)
        int widthOfGrad = grList.get(0).length;                                             // zero indexed grList always has gradient array
        double[] pomArr;
        int ix = 0;
        while(ix < grList.size()){
            if(grList.get(ix)==null){
                int num = 1;                                                                    // number of nulls
                int nullIX = ix + 1;
                while(nullIX < grList.size() && grList.get(nullIX)==null){
                    num++;
                    nullIX++;
                }
                // set new values for last gradient
                pomArr = grList.get(ix-1);
                for(int j=0; j<widthOfGrad; j++)
                    pomArr[j] = pomArr[j]/(num + 1);
                // copy divided gradient to other gradients
                for(int j=0; j<num; j++)
                    grList.set(ix - 1 + j, pomArr);
            }
            ix++;
        }

        for(int i=0; i<grList.size(); i++)
            dOUT.setD(i, grList.get(i));
    }

    @Override
    public void updateLearnableParams(){
        if(myDLParams.doGradL.getValue()){
            double maxD = 0;
            double tempV;
            for(NNLay lay: ALLsubLayers){
                tempV = lay.maxdW();
                if(tempV > maxD) maxD = tempV;
            }
            if(maxD > myDLParams.gradLSize.getLinDoubleValue()){
                double scale = maxD/myDLParams.gradLSize.getLinDoubleValue();
                sclCounter++;
                sclAmount += scale;
                for(NNLay lay: ALLsubLayers)
                    lay.scaledW(scale);
            }
        }
            
        for(NNLay lay: ALLsubLayers)
            lay.updateLearnableParams();
    }
    
    //reports scale occurence
    public void reportScaleOcc(){
        System.out.print("scaled " + sclCounter + " times");
        if(sclCounter>0) System.out.print(" with avg scale " + sclAmount/sclCounter);
        System.out.println();
    }

    public long getSclCount() { return sclCounter; }

    public double getSclAmnt() { return sclAmount; }

    //resets scale counters
    public void resetScaleCnts() {
        sclCounter = 0;
        sclAmount = 0;
    }

    //************************************************************************** methods of GXgenXinterface

    @Override
    public void genX(GXgenXinterface parA, GXgenXinterface parB){
        NNetwork    netA = (NNetwork)parA,
                    netB = (NNetwork)parB;
        for(int i=0; i<ALLsubLayers.size(); i++)
            ALLsubLayers.get(i).genX( netA.ALLsubLayers.get(i), netB.ALLsubLayers.get(i));
    }
    
    //************************************************************************** methods returning some information about net

    //returns net number of layers (only end layers, grouping not counted)
    public int nLays(){ return this.ALLsubLayers.size(); }

    @Override
    public int nParam(){
        int num=0;
        for(NNLay lay: ALLsubLayers) num += lay.nParam();
        return num;
    }
    @Override

    public int nNodes(){
        int num=0;
        for(NNLay lay: ALLsubLayers) num += lay.nNodes();
        return num;
    }
}//NNetwork