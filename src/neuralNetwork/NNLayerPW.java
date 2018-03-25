/*
 * 2017 (c) piteren
 */
package neuralNetwork;

import genX.GXgenXinterface;

/**
 * pointwise operating layer (and similar...)
 */
public class NNLayerPW extends NNLay {
    
    private final PWlayType pwLayType;                                          //type of layer operation
    
    public enum PWlayType{
        ADD,                    //add
        MUL,                    //multiplication
        NOP,                    //no operation (linear)
        REV,                    //0-1 reverse
        TNH,                    //tanh (scaling)
    }
    
    public NNLayerPW(NNLearnParams mDLp, int oW, PWlayType tp){
        super(mDLp, oW);
        pwLayType = tp;
        lType = NNLayType.PW;
    }
    
    @Override
<<<<<<< HEAD
    protected void initWeights(){}

=======
    protected void initWeights(){}    
>>>>>>> c93a836729b2d88c6b6ab3ec1b564746c052410c
    @Override
    public void restartLrnMethodParams(){}
    
    //************************************************************************** methods of NNrunFBinterface
    @Override
    protected void calcVout(){
        double[] vOUTtempArray = new double[vOUT.getWidth()];                   //temporary array for vOUT values        
        int ixI = 0;                                                            //Input index
        int ixO = 0;                                                            //Output index
        double[] vin = vIN.getD(0);
        
        switch(pwLayType){
            case ADD:
                while(ixI < vIN.getWidth()){
                    vOUTtempArray[ixO] += vin[ixI];
                    ixI++;
                    ixO++;
                    if(ixO==vOUT.getWidth()) ixO = 0;
                }
                break;
            case MUL:
                for(int k=0; k<vOUTtempArray.length; k++) vOUTtempArray[k] = 1; //initialize temp array with (1)
                while(ixI < vIN.getWidth()){
                    vOUTtempArray[ixO] *= vin[ixI];
                    ixI++;
                    ixO++;
                    if(ixO==vOUT.getWidth()) ixO = 0;
                }
                break;
            case NOP: for(int i=0; i<vIN.getWidth(); i++) vOUTtempArray[i] = vin[i];                        break;
            case REV: for(int i=0; i<vIN.getWidth(); i++) vOUTtempArray[i] = 1 - vin[i];                    break;
            case TNH: for(int i=0; i<vIN.getWidth(); i++) vOUTtempArray[i] = nodeFunc(vin[i], NFtype.TANH); break;
        }
        vOUT.setD(0, vOUTtempArray);                                            //update vOUT
    }
    @Override
    protected void calcDin(int h){       
        double[] dINtempArray = new double[dIN.getWidth()];                     //temporary array for dIN values
        int ixI = 0;                                                            //input index
        int ixO = 0;                                                            //output index
        int off;
        double[] dout = dOUT.getD(h);
        double[] vin = vIN.getD(h);

        switch(pwLayType){
            case ADD:
                while(ixI < dIN.getWidth()){
                    dINtempArray[ixI] = dout[ixO];
                    ixI++;
                    ixO++;
                    if(ixO==dOUT.getWidth()) ixO=0;
                }        
                break;
            case MUL:
                while(ixI < dIN.getWidth()){
                    off = ixI - dOUT.getWidth();
                    if(ixI < dOUT.getWidth()) off = dOUT.getWidth() + ixI;
                    dINtempArray[ixI] = dout[ixO] * vin[off];
                    ixI++;
                    ixO++;
                    if(ixO==dOUT.getWidth()) ixO=0;
                }
                break;
            case NOP: for(int i=0; i<dIN.getWidth(); i++) dINtempArray[i] = dout[i];                                    break;
            case REV: for(int i=0; i<dIN.getWidth(); i++) dINtempArray[i] = -dout[i];                                   break;
            case TNH: double[] vout = vOUT.getD(h);
                      for(int i=0; i<dIN.getWidth(); i++) dINtempArray[i] = dout[i] * nodeDFunc(vout[i], NFtype.TANH);  break;
        }
        dIN.setD(h, dINtempArray);
    }
    @Override
    public void updateLearnableParams(){}
    
    //************************************************************************** methods of GXgenXinterface
    @Override
    public void genX(GXgenXinterface parA, GXgenXinterface parB){}
    
    @Override
    public int nNodes(){
        return 0;
    }
    @Override
    public int nParam(){
        return 0;
    }
}