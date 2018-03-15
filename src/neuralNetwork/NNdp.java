/**
 * 2018 (c) piteren
 */

package neuralNetwork;

import utilities.UArr;

/**
 * NN data processing class
 */
public class NNdp {

    //creates loss array 4 SVM loss
    public static double[] arrLossSVM(double[] out, int cCix, double offS){

        double[] loss = new double[out.length];
        double corrSC = out[cCix];
        for(int i=0; i<out.length; i++)
            if(i!=cCix)
                if(out[i] - corrSC + offS > 0)
                    loss[i] = out[i] - corrSC + offS;
        return loss;
    }

    //creates grad array 4 SVM loss
    public static double[] gradSVM(double[] out, int cCix, double offS){

        double[] loss = arrLossSVM(out, cCix, offS);
        double[] gradL = new double[out.length];
        double rew = 1;
        // double rew = valTanhScld(1);
        for(int i=0; i<loss.length; i++){
            if(loss[i]>0){
                gradL[i] = rew;
                gradL[cCix] += -rew;                                         //set grad 4 proper score
            }
        }
        return gradL;
    }

    //calculates SVM loss error value
    public static double lossSVM(double[] out, int cCix, double offS){
        double totNetLoss = 0;
        double[] loss = arrLossSVM(out, cCix, offS);
        for(int i=0; i<loss.length; i++)
            totNetLoss += loss[i];
        return totNetLoss;
    }

    //
    public static double[] arrProbCE(double[] out){
        double[] prCE = new double[out.length];
        double sum = 0;
        for(int i=0; i<out.length; i++){
            prCE[i] = Math.exp(out[i]);
            sum += prCE[i];
        }
        for(int i=0; i<out.length; i++)
            prCE[i] = prCE[i] / sum;
        return prCE;
    }

    //
    public static double[] gradCE(double[] out, int cCix){
        double[] gradL = arrProbCE(out);
        gradL[cCix] -= 1;
        return gradL;
    }

    //
    public static double lossCE(double[] out, int cCix){
        double sum = 0;
        //System.out.print("out  : ");
        //for(int i=0; i<out.length; i++) System.out.print(out[i] + " ");
        //System.out.println();

        //System.out.print("prob : ");
        for(int i=0; i<out.length; i++) {
            //System.out.print(Math.exp(out[i]) + " ");
            sum += Math.exp(out[i]);
        }
        //System.out.print(Math.exp(out[cCix])/sum + " ");
        //System.out.println(-Math.log(Math.exp(out[cCix])/sum));
        return -Math.log(Math.exp(out[cCix])/sum);
    }

    //creates grad array 4 reinforcement case
    public static double[] gradReinforcement(double[] out, double reward){
        int width = out.length;
        double[] rFG = new double[width];
        double rew = reward;
        // double rew = valTanhScld(reward);
        int maxIX = UArr.maxVix(out);
        for(int i=0; i<width; i++){
            if(i==maxIX) rFG[i] = -rew;
            else         rFG[i] = rew/(width-1);
        }
        return rFG;
    }

    /*
    //calculates tanh scaled value with range and scale parameters, used to scale initial gradients
    private double valTanhScld(double val){
        return Math.tanh(val * myDLParams.tanhRanger.getLinDoubleValue() ) * 1.3130352 * myDLParams.tanhScaler.getLinDoubleValue();
    }
    */
}
