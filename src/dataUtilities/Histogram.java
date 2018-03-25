/*
 * 2017 (c) piteren
 */
package dataUtilities;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import utilities.ULogDoubleProperty;
import utilities.ULogDoubleProperty.TSform;
/**
 * histogram like object (basic), turns set of data into some statistical info (mean, sDev, min, max)
 */
public class Histogram extends Observable {

    private boolean                         isActive = false;                               //activity flag
    private long                            lastUpdMs = 0;                                  //last update time
    private static ULogDoubleProperty       notfDelayMs = new ULogDoubleProperty(TSform.INT,2);    //min dalay between observables notification
    
    private final LinkedList<Double>        tempHData = new LinkedList();                   //temp copied data (source)
    
    //fields of source data statistical information
    private double                          dMean,                                          //mean of source data
                                            dMin,                                           //min value of source data
                                            dMax,                                           //max value of source data
                                            dSDev;                                          //std deviation of source data

    //constructor
    public Histogram(){
    }
    
    //histogram copy
    public Histogram copy(){
        Histogram newH = new Histogram();
        newH.dMean = dMean;
        newH.dMin = dMin;
        newH.dMax = dMax;
        newH.dSDev = dSDev;
        return newH;
    }
    
    public static ULogDoubleProperty getNotfDelayMs(){
        return notfDelayMs;
    }
    
    public boolean isActive(){
        return isActive;
    }
    
    //starts histogram activity for given observer
    public void start(Observer obs){
        isActive = true;
        addObserver(obs);
    }
    //stops histogram activity for given observer
    public void stop(Observer obs){
        isActive = false;
        deleteObserver(obs);
    }
    
    //builds histogram from given data
    public void build(double[] histogramData){
        if(isActive){
            //add new values
            for(int i=1; i<histogramData.length; i++)
                tempHData.add(histogramData[i]);
            
            //calculate and update
            if(System.currentTimeMillis()-lastUpdMs > notfDelayMs.getLinIntegerValue()){
                lastUpdMs = System.currentTimeMillis();

                //calc mean, min, max
                dMean = 0;
                dMin = tempHData.get(0);
                dMax = dMin;        
                for(Double d: tempHData){
                    dMean+=d;
                    if(dMin>d) dMin = d;
                    if(dMax<d) dMax = d;
                }
                dMean = dMean/tempHData.size();

                //calc std deviation
                dSDev = 0;
                for(Double d: tempHData) dSDev += Math.pow( d-dMean, 2);
                dSDev = Math.sqrt(dSDev/tempHData.size());

                tempHData.clear();
                
                setChanged();
                notifyObservers();
            }
        }
    }
    
    public double getdMin(){
        return dMin; //sortedHData.getFirst();
    }
    public double getdMax(){
        return dMax; //sortedHData.getLast();
    }
    public double getdMean(){
        return dMean;
    }
    public double getdSDev(){
        return dSDev;
    }
}