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
    
<<<<<<< HEAD
    int                                 width;                                  // width of data (width of array)
=======
    protected int                       width;                                  // width of data (width of array)
>>>>>>> c93a836729b2d88c6b6ab3ec1b564746c052410c
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
<<<<<<< HEAD
=======
    
    //************************************************************************** methods reading and writing socket data
>>>>>>> c93a836729b2d88c6b6ab3ec1b564746c052410c

    //resets DS from given history level to the end (flushes data)
    public void resetFrom(int ix){
        while(dataArrList.size() > ix) dataArrList.removeLast();
    }

<<<<<<< HEAD
    //if not ready >> makes data ready at h history
=======
    //if not ready >> makes data ready at h history & inits with 0
>>>>>>> c93a836729b2d88c6b6ab3ec1b564746c052410c
    void makeDataReady(int h){
        while(dataArrList.size() < h+1) dataArrList.add(null);
    }
    
    //sets array (puts this array without copy) to data at history h
    public void setD(int h, double[] aVal){
        makeDataReady(h);
        dataArrList.set(h, aVal);
    }

    //returns data array from history h
<<<<<<< HEAD
    public double[] getD(int h){ return dataArrList.get(h); }
=======
    public double[] getD(int h){
        //return Arrays.copyOf(dataArrList.get(h), dataArrList.get(h).length);
        return dataArrList.get(h);
    }
>>>>>>> c93a836729b2d88c6b6ab3ec1b564746c052410c
}