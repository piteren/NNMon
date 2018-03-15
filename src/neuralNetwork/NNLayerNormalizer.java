/**
 * 2018 (c) piteren
 */

package neuralNetwork;

/**
 * layer normalizer class
 * normalizes array values (each separately) with offset and scale
 * assumes: target offset = 0
 *          target abscale = learn_params abscale
 */
public class NNLayerNormalizer {

    private boolean             forcedOff = false;
    private NNLearnParams       myDLParams;
    private double[]            actABS,                     // actual abscale
                                nnOff,                      // nn_offset param (distance of samples average from target offset /0 )
                                nnScl;                      // nn_scale param (abs scale of samples [nnOff subtracted])

    // constructor(NNLearnParams)
    NNLayerNormalizer(NNLearnParams myDLpms, int width){
        myDLParams = myDLpms;
        actABS = new double[width];
        nnOff = new double[width];
        nnScl = new double[width];
        for(int i=0; i<width; i++){
            actABS[i] = myDLParams.nodeNormABScale.getValue();
            nnOff[i] = 0;
            nnScl[i] = 1;
        }
    }

    // takes new samples array and updates nnOff & nnScl
    // returns array offsetted and scaled
    protected double[] processSample(double[] samples){

        //update parameters
        if(myDLParams.doNodeNorm.getValue() && !forcedOff){
            double  decay = myDLParams.nodeNormUPDecay.getLinDoubleValue(),             // speed of updates
                    targetABS = myDLParams.nodeNormABScale.getValue();                  // target abs scale

            for(int i=0; i<samples.length; i++){
                nnOff[i]  += decay * (samples[i] - nnOff[i]);                           // update offset
                actABS[i] += decay * (Math.abs(samples[i] - nnOff[i]) - actABS[i]);     // update actual abs scale

                if(actABS[i] * nnScl[i] > targetABS || nnScl[i]>100)                    // decrease scaling
                    nnScl[i] -= decay * nnScl[i];
                else
                    nnScl[i] += decay * nnScl[i];
            }


        }
        double[] out = new double[samples.length];
        for(int i=0; i<samples.length; i++)
            out[i] = (samples[i] - nnOff[i]) * nnScl[i];
        return out;
    }

    protected double[] getNNoff(){ return nnOff; }

    protected double[] getNNscl(){ return nnScl; }

    protected double[] scale(double[] samples){
        double[] scaled = new double[samples.length];
        for(int i=0; i<samples.length; i++)
            scaled[i] = samples[i] * nnScl[i];
        return scaled;
    }

    protected void forceOff(){ forcedOff = true; }
}