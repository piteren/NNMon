/*
 * 2017 (c) piteren
 */
package deepLearn;

import genX.GXgenXinterface;
import dataUtilities.DSmultiDataSocket;
import dataUtilities.DSdataSocket;
import deepLearn.NNLayerPW.PWlayType;
import java.util.List;
import java.util.LinkedList;
import utilities.UArr;

import utilities.UFileOperator;

/*
 * network class, parent object of subnetwork, creates (builds) subnetwork (from file)
 * may act as a standalone net (FWD and BWD may be run with given input)
 * or may be element of other subnetwork (has parent) - acts like a DLlearningNetworkedObject so it still is connectable, learnable
 */
public class NNetwork extends DLNetworkedObject{
    
    private String                      NNname;                                 //network name
    private String                      NNdesc;                                 //network description
    
    private final LinkedList<DLNetworkedObject>   
                                        ALLsubLayers = new LinkedList();        //all internal layers of this net
    private SupportingIOlay             FIOlay,                                 //first IO supporting net layer
                                        LIOlay;                                 //last  IO supporting net layer
    
    protected LinkedList<Integer>       corrCix = new LinkedList();             //correct classification index (if known) for backpropagation lerning
    protected LinkedList<Double>        rewardVal = new LinkedList();           //reward values for reinforcement learning (when correct classification index is unknown), this list accepts null (reward not known)
    
    private int                         runFWDcounter = 0;                      //counter of FWD runs since last backprop, incremented only when backprop is enabled
    
    private long                        sclCounter = 0;
    private double                      sclAmount = 0;
    
    //IN nad OUT supporting layer, used to easily exchange data with subnet
    private class SupportingIOlay extends DLNetworkedObject{
        
        SupportingIOlay(DLlearnParams mDLp, int oW){
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
            dIN.setD(h, dOUT.getD(h)); 
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
    public NNetwork(DLlearnParams mDLp, String path){
        super(mDLp,0);
        lType = DLlayType.NET;
        vOUT = null;                                                            //delete vOUT, we don't know the width now, we will create it later (...in finalize)
        buildDescriptionBasedNet((new UFileOperator(path)).readFile());
    }
    
    //************************************************************************** builds and finalizes
    //builds network based on (String) decription
    private void buildDescriptionBasedNet(List<String> desc){
        int lineIX=0;
        String[] line;
        do{
            line = desc.get(lineIX).split(" ");
            switch(line[0]){
                case "<name>": NNname = line[1];                        break;
                case "<desc>": NNdesc = desc.get(lineIX).substring(7);  break;
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
                    DLNetworkedObject   layF = ALLsubLayers.get(fLIX),
                                        layT;                                   //from & to layer pointer
                    for(int i=2; i<line.length; i++){
                        //resolve to layer
                        tLIX = Integer.parseInt(line[i]);
                        layT=ALLsubLayers.get(tLIX);

                        int offTime = 1;    // BY NOW WE ASSUME that writing to memory may be done ONLY by sublayers with index greater or equal (next or this) and with time_offset==1
                        if(fLIX<tLIX) offTime = 0;
                        layF.addNextOC(layT,offTime);
                        layT.addPrevOC(layF,offTime); 
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
        for(DLNetworkedObject lay: ALLsubLayers) lay.initWeights();
    }
    @Override
    protected void finalizeBuild(){
        //finalize first IO layer
        FIOlay = (SupportingIOlay)ALLsubLayers.removeFirst();
        
        //add last IO layer
        LIOlay = new SupportingIOlay(myDLParams,ALLsubLayers.getLast().vOUT.getWidth());
        ALLsubLayers.getLast().addNextOC(LIOlay,0);
        LIOlay.addPrevOC(ALLsubLayers.getLast(),0); 
        
        //finalize all supporting and sub layers
        FIOlay.finalizeBuild();
        for(DLNetworkedObject lay: ALLsubLayers) lay.finalizeBuild();
        LIOlay.finalizeBuild();
        
        //serach for max mem recurrency of net
        for(DLNetworkedObject lay: ALLsubLayers){
            if(lay.maxMemRecurrency > this.maxMemRecurrency)
                this.maxMemRecurrency = lay.maxMemRecurrency;
            
        }
        vOUT = LIOlay.vOUT;                                                             //vOUT reference connection
        
        super.finalizeBuild();
        
        //case when this net has no other connected before (is first and gets vIN from envy)
        if(vIN==null){       
            vIN = new DSdataSocket(FIOlay.vOUT.getWidth());
            FIOlay.dIN = new DSdataSocket(FIOlay.vOUT.getWidth());                      //dIN may left null or: dIN = firstNetLayer.dIN
        }
        else FIOlay.dIN = dIN;                                                          //reference connection
        FIOlay.vIN = vIN;                                                               //vIN reference connection
        
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
        for(DLNetworkedObject lay: getALLsubLayers())
            lay.restartLrnMethodParams();
    }
    
    public LinkedList<DLNetworkedObject> getALLsubLayers(){
        return ALLsubLayers;
    }
    
    //************************************************************************** main running methods of net
    @Override
    protected void calcVout(){
        FIOlay.vOUT.moveDataTSF();
        for(DLNetworkedObject lay: ALLsubLayers) lay.vOUT.moveDataTSF();
        LIOlay.vOUT.moveDataTSF();
        FIOlay.runFWD();                                                     //run FWD subnet
    }
    @Override
    protected void calcDin(int h){
        LIOlay.runBWD(h);                                                     //run BWD subnet
    }
    //runs net forward with given input data array (nnetwork is top layer = self runnable), returns vOUT
    public double[] runFWD(double[] inV){
        vIN.moveDataTSF();
        vIN.setD(0,inV);                                                        
        
        runFWD();
        
        if(myDLParams.doBackprop.getValue()) runFWDcounter++;
        else runFWDcounter = 0;
        
        double[] outArr = vOUT.getD(0);
        
        //resets recorded data (all but maxMemRecurency old) in case we do not backprop (free memory)
        if(!myDLParams.doBackprop.getValue()){
            FIOlay.vOUT.resetFrom(maxMemRecurrency);
            for(DLNetworkedObject lay: ALLsubLayers)
                lay.vOUT.resetFrom(maxMemRecurrency);
            LIOlay.vOUT.resetFrom(maxMemRecurrency);
        } 
        return outArr;
    }
    //runs net backward, sets gradients and preforms parameters update with given learning rate (nnetwork is top layer = self runnable)
    public void runBWD(){        
        dOUTset();
        
        // !! here we could check wether for sure we have reward_data for runFWDcounter backprop loops
        
        for(int i=0; i<runFWDcounter; i++) runBWD(i);
        runFWDcounter = 0;
        
        updateLearnableParams();
        
        //reset data sockets
        FIOlay.dIN.resetFrom(0);
        FIOlay.vOUT.resetFrom(maxMemRecurrency);
        for(DLNetworkedObject lay: ALLsubLayers){
            lay.dIN.resetFrom(0);
            lay.vOUT.resetFrom(maxMemRecurrency);
        }
        LIOlay.dIN.resetFrom(0);
        LIOlay.vOUT.resetFrom(maxMemRecurrency);
        dOUT.resetFrom(0);
    }
    //sets dOUT of net for given by runFWDcounter history range
    private void dOUTset(){
        //set dOUT with correct classification information
        if(!corrCix.isEmpty()){
            //set gradients with SVM loss
            for(int i=0; i<runFWDcounter; i++)
                dOUT.setD(i, gradSVM(vOUT.getD(i), corrCix.get(i), runFWDcounter) );
            corrCix.clear();                                                        
        }
        //set dOUT with reinforcement rewards
        else{
            double val = 0;
            for(int i=rewardVal.size()-1; i>-1; i--){
                if(rewardVal.get(i)!=null) val = rewardVal.get(i);
                else rewardVal.set(i,val);
            }
            for(int i=0; i<runFWDcounter; i++)
                dOUT.setD(i, gradReinforcement(vOUT.getD(i), rewardVal.get(i), runFWDcounter) );
            rewardVal.clear();
        }
    }
    //calculates tanh scaled value with range and scale parameters
    private double valTanhScld(double val){
        return Math.tanh(val * myDLParams.tanhRanger.getLinDoubleValue() ) * 1.3130352 * myDLParams.tanhScaler.getLinDoubleValue();
    }
    @Override
    public void updateLearnableParams(){
        if(myDLParams.doGradL.getValue()){
            double maxD = 0;
            double tempV;
            for(DLNetworkedObject lay: ALLsubLayers){
                tempV = lay.maxdW();
                if(tempV > maxD) maxD = tempV;
            }
            if(maxD > myDLParams.gradLSize.getLinDoubleValue()){
                double scale = maxD/myDLParams.gradLSize.getLinDoubleValue();
                sclCounter++;
                sclAmount += scale;
                for(DLNetworkedObject lay: ALLsubLayers)
                    lay.scaledW(scale);
            }
        }
            
        for(DLNetworkedObject lay: ALLsubLayers)
            lay.updateLearnableParams();
    }
    
    //reports scale occurence
    public void reportScaleOcc(){
        System.out.print("scaled "+sclCounter+" times");
        if(sclCounter>0) System.out.print(" with avg scale "+sclAmount/sclCounter);
        System.out.println();
    }
    public long getSclCount(){
        return sclCounter;
    }
    public double getSclAmnt(){
        return sclAmount;
    }
    //resets scale counters
    public void resetScaleFacts(){
        sclCounter = 0;
        sclAmount = 0;
    }
    
    //************************************************************************** net error methods
    //returns decision error of net
    public double decisionError(double[] out, int cCix){
        return totValLossSVM(out, cCix);
    }
    //returns decision error of net, overloaded
    public double decisionError(double[] out, double[] corrC){
        return decisionError(out, UArr.maxVix(corrC));
    }
    //************************************************************************** SVM
    //creates loss array 4 SVM loss
    private double[] arrLossSVM(double[] out, int cCix){
        double[] loss = new double[out.length];
        double corrSC = out[cCix];
        for(int i=0; i<out.length; i++)
            if(i!=cCix)
                if(out[i]-corrSC+myDLParams.offsetSVM > 0)
                    loss[i] = out[i] - corrSC + myDLParams.offsetSVM;
        return loss;
    }
    //creates grad array 4 SVM loss
    private double[] gradSVM(double[] out, int cCix, int numFRuns){
        double[] loss = arrLossSVM(out, cCix);
        double[] gradL = new double[out.length];
        double rew = valTanhScld(1)/numFRuns;
        for(int i=0; i<loss.length; i++){
            if(loss[i]>0){
                gradL[i] = rew;
                gradL[cCix] += -rew;                                         //set grad 4 proper score
            }
        }
        return gradL;
    }
    //calculates SVM loss error value
    private double totValLossSVM(double[] out, int cCix){        
        double totNetLoss = 0;
        double[] loss = arrLossSVM(out, cCix);
        for(int i=0; i<loss.length; i++)
            totNetLoss += loss[i];
        return totNetLoss;
    }

    //************************************************************************** reinforcement gradient
    //creates grad array 4 reinforcement case
    private double[] gradReinforcement(double[] out, double reward, int numFRuns){
        int width = out.length;
        double[] rFG = new double[width];
        double rew = valTanhScld(reward)/numFRuns;
        int maxIX = UArr.maxVix(out);
        for(int i=0; i<width; i++){
            if(i==maxIX) rFG[i] = -rew;
            else         rFG[i] = rew/(width-1);
        }
        return rFG;
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
    public int nLays(){
        return this.ALLsubLayers.size();
    }
    @Override
    public int nParam(){
        int num=0;
        for(DLNetworkedObject lay: ALLsubLayers) num += lay.nParam();
        return num;
    }
    @Override
    public int nNodes(){
        int num=0;
        for(DLNetworkedObject lay: ALLsubLayers) num += lay.nNodes();
        return num;
    }
}//NNetwork