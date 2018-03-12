/*
 * 2017 (c) piteren
 */
package neuralNetwork;

import static neuralNetwork.NNLearnParams.WInitDist.UNIFORM;
import genX.GXgenXinterface;
import utilities.UArr;
import utilities.URand;

/**
 * fully-connected layer
 */
public class NNLayerFC extends NNLay {
    
    private double[]        vINarr;                                             //temp array for vIN values
    
    //constructor    
    public NNLayerFC(NNLearnParams mDLp, int oW, NFtype lNFtp){
        super(mDLp,oW);
        myNFtype = lNFtp;
        lType = NNLayType.FC;
    }
                
    @Override
    protected void finalizeBuild(){
        super.finalizeBuild();
        vWeights = new double[vIN.getWidth()+1][vOUT.getWidth()];
        dWeights = new double[vIN.getWidth()+1][vOUT.getWidth()];
        lmpM = new double[vIN.getWidth()+1][vOUT.getWidth()];
        lmpV = new double[vIN.getWidth()+1][vOUT.getWidth()];
        restartLrnMethodParams();
    }

    @Override   //initializes random weights with Xavier formula
    protected void initWeights(){
        double scale = myDLParams.wIScale.getLinDoubleValue();
        for(int i=0; i<vWeights.length; i++)
            for(int j=0; j<vWeights[0].length; j++)
                vWeights[i][j] = weigthInVal(scale);
        if(myLHistograms.get(1).isActive()) myLHistograms.get(1).build( UArr.flat(vWeights) );   //weights histogram
    }

    //Xavier initialization weight value (Caffe) with scale
    private double weigthInVal(double scale){
        if(myDLParams.wIDist==UNIFORM) return ( URand.one() - 0.5 ) * 3.2 / Math.sqrt(vWeights.length) * scale; //uniform distribution base
        else return URand.gauss() / Math.sqrt(vWeights.length) * scale;                                         //gaussian distribution base
    }

    @Override
    public void restartLrnMethodParams(){
        lmpM = new double[vIN.getWidth()+1][vOUT.getWidth()];
        lmpV = new double[vIN.getWidth()+1][vOUT.getWidth()];
    }
   
    //************************************************************************** NNrunFBinterface
    @Override
    protected void calcVout(){
        vINarr = vIN.getD(0);                                                   //get vIN values
        double[] vOUTtempArray = new double[vOUT.getWidth()];                   //temporary array for vOUT values
        
        //calculate W*vIN;
        for(int i=0; i<vIN.getWidth(); i++)                                     //for every input value
            for(int o=0; o<vOUT.getWidth(); o++)                                //for every output node
                vOUTtempArray[o] += vINarr[i] * vWeights[i][o];                 //calculate & add output part
        
        //add bias (W*1)
        int last = vIN.getWidth();
        for(int o=0; o<vOUT.getWidth(); o++)                                    //for every output node
            vOUTtempArray[o] += vWeights[last][o];                              //calculate & add bias part
        
        //vNN histogram
        if(myLHistograms.get(3).isActive())
            myLHistograms.get(3).build(vOUTtempArray);
        
        //node norm
        for(int o=0; o<vOUT.getWidth(); o++)
            vOUTtempArray[o] = nodeNorm[o].processSample(vOUTtempArray[o]);
        
        //nnOff & nnScl histograms
        if(myLHistograms.get(4).isActive()){
            double[] nnOArr = new double[vOUT.getWidth()];
            for(int o=0; o<vOUT.getWidth(); o++)
                nnOArr[o] = nodeNorm[o].getNNoff();
            myLHistograms.get(4).build( nnOArr );
        }
        if(myLHistograms.get(5).isActive()){
            double[] nnSArr = new double[vOUT.getWidth()];
            for(int o=0; o<vOUT.getWidth(); o++)
                nnSArr[o] = nodeNorm[o].getNNscl();
            myLHistograms.get(5).build( nnSArr );
        }

        //vNOD histogram
        if(myLHistograms.get(6).isActive())
            myLHistograms.get(6).build(vOUTtempArray);
        
        //calculate nodeAF for all outputs and set vOUT
        for(int o=0; o<vOUTtempArray.length; o++)                               
            vOUTtempArray[o] = nodeFunc(vOUTtempArray[o], myNFtype);
        
        vOUT.setD(0, vOUTtempArray);                                            //copy vOUTtempArray to vOUT
    }

    @Override
    protected void calcDin(int h){
        double[] dNODEtemp = dOUT.getD(h);                                      //temporary array for dNODE values
        
        double[] vout = vOUT.getD(h);
        //global gradient at node
        for(int o=0; o<dOUT.getWidth(); o++)
            dNODEtemp[o] = dNODEtemp[o]*nodeDFunc(vout[o], myNFtype);
        
        //global gradient at node norm (update always, even if nnorm turned off)
        for(int o=0; o<dNODEtemp.length; o++)
            dNODEtemp[o] *= nodeNorm[o].getNNscl();

        //push global node gradient to dWeights and inputs
        double[] dINtempArray = new double[dIN.getWidth()];                     //temporary array for dIN values
        double globalWeightGradient;
        double[] vin = vIN.getD(h);                                             //temporary array for vIN values
        for(int i=0; i<dIN.getWidth(); i++)                                     //for every input grad
            for(int o=0; o<dOUT.getWidth(); o++){                               //for every node grad of this layer
                globalWeightGradient = dNODEtemp[o] * vin[i];                   //calculate global weight gradient
                dWeights[i][o] += globalWeightGradient;                         //cummulate (with previous runs value if not reset) global weight gradient
                dINtempArray[i] += globalWeightGradient;                        //calculate global input gradient
            }
        
        //push global node gradient to bias dWeights
        int last = dIN.getWidth();
        for(int o=0; o<dOUT.getWidth(); o++)
            dWeights[last][o] += dNODEtemp[o];
        
        dIN.setD(h,dINtempArray);
    }

    @Override
    public void updateLearnableParams(){
        //dWeights histogram
        if(myLHistograms.get(2).isActive())
            myLHistograms.get(2).build( UArr.flat(dWeights) );
        
        double lR = myDLParams.learningRate.getLinDoubleValue();
        
        double mmx = myDLParams.mmx.getValue();
        
        double adamBeta1 = myDLParams.adamBeta1;
        double adamBeta2 = myDLParams.adamBeta2;
        double adamEps = myDLParams.adamEps;
        
        double L2regS = 0;
        if(myDLParams.doL2reg.getValue()) L2regS = myDLParams.L2regSize.getLinDoubleValue();

        switch(myDLParams.myWUpAlg){
            case MMNTM:
                for(int i=0; i<dWeights.length; i++)
                    for(int o=0; o<dWeights[0].length; o++){
                        lmpM[i][o] = mmx * lmpM[i][o] -lR * dWeights[i][o];
                        vWeights[i][o] += lmpM[i][o] -L2regS * vWeights[i][o];
                    }
                break;
            case ADAM:
                for(int i=0; i<dWeights.length; i++)
                    for(int o=0; o<dWeights[0].length; o++){
                        lmpM[i][o] = adamBeta1*lmpM[i][o] + (1-adamBeta1)*dWeights[i][o];
                        lmpV[i][o] = adamBeta2*lmpV[i][o] + (1-adamBeta2)*Math.pow(dWeights[i][o],2);
                        vWeights[i][o] += -lR * lmpM[i][o] / (Math.sqrt(lmpV[i][o]) + adamEps) -L2regS * vWeights[i][o];
                    }
                break;
        }

        dWeights = new double[vIN.getWidth()+1][vOUT.getWidth()];                                   //reset dWeighs
        if(myLHistograms.get(1).isActive()) myLHistograms.get(1).build( UArr.flat(vWeights) );      //weights histogram
    }
    
    //************************************************************************** genX interface
    @Override
    public void genX(GXgenXinterface parA, GXgenXinterface parB){
        NNLayerFC   layA = (NNLayerFC)parA,
                    layB = (NNLayerFC)parB;
        int pMX = 0;                        //weight MIX (from both parents) probability (%) for one child
        int mxA = 0;                            //weight MIX probability (%) for exact weight
        int pMU = 0;                        //weight MUTATION probability (%) for one child 
        int pMI = 0;                            //weight INCREASE (mutation) probability (%) for exact weight
        int mIA = 0;                            //MAX weight (%) INCREASE
        int pMD = 0;                            //weight DECREASE (mutation) probability (%) for exact weight
        int mDA = 0;                            //MAX weight (%) DECREASE
        int pMR = 0;                            //weight RANDOM SET (mutation) probability (%) for exact weight
                
        //if(nnPA.genAge>nnPB.genAge) genXnet.genAge = nnPA.genAge + nnPB.genAge/nnPA.genAge;
        //else genXnet.genAge = nnPB.genAge + nnPA.genAge/nnPB.genAge;
                
        //choose source weights by random
        for(int k=0; k<vWeights.length; k++)
            for(int l=0; l<vWeights[0].length; l++){
                if(URand.one()>0.5) vWeights[k][l] = layA.vWeights[k][l];
                else                vWeights[k][l] = layB.vWeights[k][l];
            }
        //weights mixing
        if(URand.i(100)<pMX)
            for(int k=0; k<vWeights.length; k++)
                for(int l=0; l<vWeights[0].length; l++)
                    if(URand.i(100)<mxA) vWeights[k][l] = (layA.vWeights[k][l]+layB.vWeights[k][l])/2;
            
        //weights mutation
        double scale = myDLParams.wIScale.getLinDoubleValue();
        if(URand.i(100)<pMU)
            for(int k=0; k<vWeights.length; k++)
                for(int l=0; l<vWeights[0].length; l++){   
                    if(URand.i(100)<pMI) vWeights[k][l] = vWeights[k][l] * (URand.i(mIA)+1)/100;    //increase
                    if(URand.i(100)<pMD) vWeights[k][l] = vWeights[k][l] * (URand.i(mDA)+1)/100;    //decrease
                    if(URand.i(100)<pMR) vWeights[k][l] = weigthInVal(scale);                       //random set for existing weight
                }
        
        restartLrnMethodParams();

    }
    
    //************************************************************************** methods returning some information about layer
    @Override
    public int nParam(){ return vWeights.length*vWeights[0].length; }

    @Override
    public int nNodes(){ return vOUT.getWidth(); }
}