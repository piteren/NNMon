/*
 * 2017 (c) piteren
 */
package neuralNetwork;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import utilities.ULogDoubleProperty;
import utilities.ULogDoubleProperty.TSform;

/**
 * back prop learning parameters
 */
public class NNLearnParams {
    
    public WInitDist                wIDist = WInitDist.UNIFORM;                                     //weights init distribution
    public ULogDoubleProperty       wIScale = new ULogDoubleProperty(TSform.DBL,0);             //weights init scale
    
    public SimpleBooleanProperty    doBackprop = new SimpleBooleanProperty(true);           //marker for learning activity (classic backprop or reinforcement)
    public ULogDoubleProperty       learningRate = new ULogDoubleProperty(TSform.DBL,-6);   //learning rate        
    public ULogDoubleProperty       batchSize = new ULogDoubleProperty(TSform.INT,0);       //batch size (number of samples in one batch)
    
    public WUpdAlgorithm            myWUpAlg = WUpdAlgorithm.MMNTM;                                //default learning method
    public SimpleDoubleProperty     mmx = new SimpleDoubleProperty(0.1);                    //momentum factor
    public double                   adamBeta1 = 0.9,                                        //adam parameters
                                    adamBeta2 = 0.999,
                                    adamEps = 0.00000001;
    
    public SimpleBooleanProperty    doL2reg = new SimpleBooleanProperty(true);              //marker for L2reg activity
    public ULogDoubleProperty       L2regSize = new ULogDoubleProperty(TSform.DBL,-10);     //size of L2reg parameter (0 -> there is no L2 reg)
    
    public SimpleBooleanProperty    doGradL = new SimpleBooleanProperty(true);              //marker for gradient Limit
    public ULogDoubleProperty       gradLSize = new ULogDoubleProperty(TSform.DBL,1);       //size of gradient limit bound
    
    public ULogDoubleProperty       tanhRanger = new ULogDoubleProperty(TSform.DBL,0),      //reinforcement reward range multiplier
                                    tanhScaler = new ULogDoubleProperty(TSform.DBL,0);      //reinforcement reward value multiplier
    
    public double                   offsetSVM = 0.1;                                        //SVM offset parameter
    
    public SimpleBooleanProperty    doNodeNorm = new SimpleBooleanProperty(true);           //marker for node normalization activity
    public SimpleDoubleProperty     nodeNormABScale = new SimpleDoubleProperty(0.2);        //node normalization abs scale (average of abs)
    public ULogDoubleProperty       nodeNormUPDecay = new ULogDoubleProperty(TSform.DBL,-6); //node normalization update decay param

    // weight initialization distribution
    public enum WInitDist{
        UNIFORM,
        GAUSSIAN
    }

    // weight update algorithm
    public enum WUpdAlgorithm {
        MMNTM,
        ADAM
    }
    
    public NNLearnParams(){}
}