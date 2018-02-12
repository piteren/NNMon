/*
 * 2017 (c) piteren
 */
package deepLearn;

/**
 * node normalizer object
 */
public class NNnodeNormalizer {
    
    private DLlearnParams       myDLParams;
    private double              actSDS,                                         //actual sd
                                nnOff,                                          //nn_offset param
                                nnScl;                                          //nn_scale param
    
    //constructor(DLlearnParams)
    NNnodeNormalizer(DLlearnParams myDLpms){
        myDLParams = myDLpms;
        actSDS = myDLParams.nodeNormSDScale.getValue();
        
        nnOff = 0;
        nnScl = 1;
    }
 
    //tekes new sample of data ad updates nnOff & nnScl
    protected double processSample(double smpl){
            
        //update parameters
        if(myDLParams.doNodeNorm.getValue()){
            double  decay = myDLParams.nodeNormUDecay.getLinDoubleValue(),
                    targetSDS = myDLParams.nodeNormSDScale.getValue(),
                    upd;

            upd = smpl * decay;
            nnOff = (1-decay) * nnOff + upd;

            upd = Math.abs(smpl - nnOff) * decay;
            actSDS = (1-decay) * actSDS + upd;
            
            if(actSDS * nnScl > targetSDS || nnScl>100) nnScl = (1-decay) * nnScl;
            else                                        nnScl = (1+decay) * nnScl; 
            
        }
        
        return (smpl - nnOff) * nnScl;
    }
    
    protected double getNNoff(){
        return nnOff;
    }
    protected double getNNscl(){
        return nnScl;
    }
}