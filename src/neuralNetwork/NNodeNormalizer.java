/**
 * 2017 (c) piteren
 */
package neuralNetwork;

/**
 * node normalizer class
 * normalizes value with offset and scale
 * assumes: target offset = 0
 *          target abscale = learn_params abscale
 */
public class NNodeNormalizer {

    private boolean             forcedOff = false;
    private NNLearnParams       myDLParams;
    private double              actABS,                                             //actual abscale
                                nnOff = 0,                                          //nn_offset param
                                nnScl = 1;                                          //nn_scale param
    
    //constructor(NNLearnParams)
    NNodeNormalizer(NNLearnParams myDLpms){
        myDLParams = myDLpms;
        actABS = myDLParams.nodeNormABScale.getValue();
    }
 
    // takes new sample of data and updates nnOff & nnScl
    // returns value offsetted and scaled
    protected double processSample(double sample){
            
        //update parameters
        if(myDLParams.doNodeNorm.getValue() && !forcedOff){
            double  decay = myDLParams.nodeNormUPDecay.getLinDoubleValue(),
                    targetABS = myDLParams.nodeNormABScale.getValue();

            nnOff  += decay * (sample - nnOff);
            actABS += decay * (Math.abs(sample - nnOff) - actABS);

            if(actABS * nnScl > targetABS || nnScl>100) nnScl -= decay * nnScl;
            else                                        nnScl += decay * nnScl;
            
        }
        
        return (sample - nnOff) * nnScl;
    }
    
    protected double getNNoff(){
        return nnOff;
    }

    protected double getNNscl(){
        return nnScl;
    }

    protected void forceOff(){ forcedOff = true; }
}