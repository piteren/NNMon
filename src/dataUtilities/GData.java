/*
 * 2017 (c) piteren
 */
package dataUtilities;

import javafx.scene.paint.Color;
import java.util.LinkedList;
import java.util.Observable;

/**
 * keeps step data, supports additive mode (cumulative), scaling, smoothing
 */
public class GData extends Observable {
    private int                 scale;                      // scale factor, number of data samples scaled to one OUT_sample
    private final boolean       addMode;                    // cumulative data flag
    private int                 maxWidth = 0;               // max size of data, if==0 then unlimited, must be even
    private double              buffer;                     // data buffer (used when scale > 1)
    private int                 bufferSize;                 // number of samples taken to the buffer
    
    private int                 numASncNotf = 0;            // counter of samples added since last notification
    private long                lastUpdMs = 0;              // last update time
    private static long         notfDelayMs = 50;           // min delay between observables notification
    
    private final Color         color;                      // GD color
    private LinkedList<Double>  data = new LinkedList();    // stored data samples
    
    //constructor
    public GData(   int scl,                                                    //initial scale
                    boolean scM,                                                //additive scaling marker
                    int maxWd,                                                  //max width
                    Color c)                                                    //color     
    {
        scale = scl;
        addMode = scM;
        color = c;
        maxWidth = maxWd + maxWd%2;                                        
    }

    //adds new sample to data
    public void add(double val){
        
        //check maxWidth condition
        if(data.size() == maxWidth && maxWidth!=0)
            scaleDown();

        buffer+=val;
        bufferSize++;
        if(bufferSize==scale){
            if(addMode) buffer += getLastV();
            else        buffer = buffer/scale; 
            data.add(buffer);
            numASncNotf++;
            buffer = 0;
            bufferSize = 0;

            if(System.currentTimeMillis()-lastUpdMs > notfDelayMs){
                setChanged();
                notifyObservers( getData(numASncNotf) );
                lastUpdMs = System.currentTimeMillis();
                numASncNotf = 0;
            }
        }
    }

    //scales data down (scale*2)
    public void scaleDown(){
        LinkedList<Double> newData = new LinkedList();
        
        double val;
        while(!data.isEmpty()){
            val = data.removeFirst();
            val += data.removeFirst();
            newData.add(val/2);
        }
        data = newData;
        scale = scale*2;
        
        setChanged();
        notifyObservers( getData(0) );
        numASncNotf = data.size();
    }

    //clears stored data
    public void flush(){
        data = new LinkedList();
        buffer = 0;
        bufferSize = 0;
        
        setChanged();
        notifyObservers( getData(0) );
        numASncNotf = 0;
    }

    //clears stored data and sets new scale
    public void flush(int newScale){
        scale = newScale;
        flush();
    }

    //returns smoothed GData with width using linear method
    public GData smooth(int sW){
        GData nGD = new GData(scale,addMode,maxWidth,color);
        int sWidth = sW;
        if(data.size()<sWidth) sWidth = data.size();

        double[][] weightsNvalues = new double[2][2*sWidth-1];
        double sumWeight = 1;
        double weight;
        //define inintial weights and values
        for(int i=0; i<sWidth; i++){
            //weights
            weight=1-i*((double)1/sWidth);
            weightsNvalues[0][sWidth-1-i]=weight;
            weightsNvalues[0][sWidth-1+i]=weight;
            if(i>0)sumWeight+=2*weight;
            //values
            weightsNvalues[1][sWidth-1-i]=data.get(0)*weightsNvalues[0][sWidth-1-i];
            weightsNvalues[1][sWidth-1+i]=data.get(i);
        }
        //normalize weights
        for(int j=0; j<weightsNvalues[0].length; j++) weightsNvalues[0][j]=weightsNvalues[0][j]/sumWeight;
        //traverse through data
        double newValue;
        for(int i=0; i<data.size(); i++){
            //calculate new value
            newValue = 0;
            for(int j=0; j<weightsNvalues[1].length; j++) newValue += weightsNvalues[1][j]*weightsNvalues[0][j];
            nGD.data.add(newValue);

            //offset values by one
            for(int j=0; j<weightsNvalues[1].length-1; j++) weightsNvalues[1][j]=weightsNvalues[1][j+1];
            if((i+sWidth)>(data.size()-1)) weightsNvalues[1][weightsNvalues[1].length-1]=data.get(data.size()-1);
            else weightsNvalues[1][weightsNvalues[1].length-1]=data.get(i+sWidth);
        }
        return nGD;
    }

    public Color getColor(){
        return color;
    }

    //returns size of GData (number of samples)
    public int getSize(){
        return data.size();
    }

    //returns data list
    public LinkedList<Double> getData(){
        return data;
    }

    //returns data list of n samples from the end
    public LinkedList<Double> getData(int numFromEnd){
        LinkedList<Double> endData = new LinkedList();
        int startIX = data.size() - numFromEnd;
        for(int i=0; i<numFromEnd; i++)
            endData.add(data.get( startIX+i ));
        return endData;
    }

    //returns last value in data or 0 if empty
    public double getLastV(){
        if(data.isEmpty()) return 0;
        return data.getLast();
    }

    //returns sum of all data samples
    public double calcSumV(){
        double val=0;
        for(int i=0; i<data.size(); i++) val+=data.get(i);
        return val;
    }

    //returns sum of last n data samples
    public double calcSumV(int n){
        double val=0;
        int from = data.size()-1-n;
        if(from<0) from=0;
        for(int i=from; i<data.size(); i++) val+=data.get(i);
        return val;
    }

    //returns avg of all data samples
    public double calcAvgV(){
        double val=0;
        for(int i=0; i<data.size(); i++) val+=data.get(i);
        return val/data.size();
    }
}//GData