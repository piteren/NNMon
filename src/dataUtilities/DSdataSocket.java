/**
 * 2017 (c) piteren
 */

package dataUtilities;

import java.util.LinkedList;

/**
 * data socket
 * keeps list of double arrays
 * list index keeps previous (historical) samples of data
 */
public class DSdataSocket {
    
    int                                 width;                                  // width of data (width of array)
    private final LinkedList<double[]>  dataArrList = new LinkedList<>();       // raw data list, 0-actual, 1-history(+1), 2-history(+2)...
    
    // constructor (width)
    public DSdataSocket(int wd){
        width = wd;
    }
    
    // returns data readiness for read at given history step
    public boolean isDataReadReady(int h){
        if(dataArrList.size() > h) return dataArrList.get(h)!=null;
        return false;
    }

    // moves data one Time_Step_Forwad
    public void moveDataTSF(){
        dataArrList.addFirst(null);
    }
    
    public int getWidth(){
        return width;
    }

    //resets DS from given history level to the end (flushes data)
    public void resetFrom(int ix){
        while(dataArrList.size() > ix) dataArrList.removeLast();
    }

    //if not ready >> makes data ready at h history
    void makeDataReady(int h){
        while(dataArrList.size() < h+1) dataArrList.add(null);
    }
    
    //sets array (puts this array without copy) to data at history h
    public void setD(int h, double[] aVal){
        makeDataReady(h);
        dataArrList.set(h, aVal);
    }

    //returns data array from history h
    public double[] getD(int h){ return dataArrList.get(h); }
}