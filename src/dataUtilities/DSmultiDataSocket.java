/*
 * 2017 (c) piteren
 */
package dataUtilities;

//   !!!! check and make MDS ready for getting MDS as a source 4 dsSource  !!!!!
//******************************************************************************

import java.util.LinkedList;

/**
 * data socket build of list of data sockets
 * supports two types of MDS:
 *  SER - serial
 *  PAR - parallel
 */
public class DSmultiDataSocket extends DSDataSocket {
    
    private final MDStype                   myMDStype;                          // MDS type
    private final LinkedList<DSDataSocket>  dsSource = new LinkedList<>();      // list of sockets that makes this MDS
    private final LinkedList<Integer>       timeOff = new LinkedList<>();       // time offset of corresponding ds
    
    public enum MDStype{
        SER,                                                                    // serial
        PAR                                                                     // parallel
    }
    
    //constructor (socket, type, time_offset)
    public DSmultiDataSocket(MDStype tp, int tmO){
        super(0);
        myMDStype = tp;
        timeOff.add(tmO);
    }
    
    //adds DS to MDS
    public void addDS(DSDataSocket ds, int tmO){
        dsSource.add(ds);
        if(myMDStype==MDStype.SER) width += ds.width;
        else width = ds.width;
        timeOff.add(tmO);
    }
    
    @Override
    public boolean isDataReadReady(int h){
        switch(myMDStype){
            case SER:
                for(int i=0; i<dsSource.size(); i++)
                    if(!dsSource.get(i).isDataReadReady(h+timeOff.get(i)))
                        return false;
                break;
            case PAR:
                for(int i=0; i<dsSource.size(); i++)
                    if(h-timeOff.get(i)>=0)
                        if(!dsSource.get(i).isDataReadReady(h-timeOff.get(i)))
                            return false;
                break;
        }
        return true;
    }

    @Override
    public void moveDataTSF(){
        for(DSDataSocket ds: dsSource) ds.moveDataTSF();
    }

    @Override
    public void resetFrom(int ix){
        for(DSDataSocket ds: dsSource) ds.resetFrom(ix);
    }
    
    @Override
    public void setD(int h, double[] aVal){
        if(myMDStype==MDStype.SER){
            int counter = 0;
            double[] partArr;
            DSDataSocket dsi;
            
            //for every DS from MDS
            for(int i=0; i<dsSource.size(); i++){
                dsi = dsSource.get(i);
                dsi.makeDataReady(h);
                
                partArr = new double[dsi.width];
                System.arraycopy(aVal, counter, partArr, 0, dsi.width);
                dsi.setD(h, partArr);
                counter += dsi.width;
            }
        }
    }

    @Override
    public double[] getD(int h){
        DSDataSocket dsi;
        double[] outArr = new double[width];
        
        switch(myMDStype){
            case SER:
                int counter = 0;
                // for every DS from MDS
                for(int i=0; i<dsSource.size(); i++){
                    dsi = dsSource.get(i);
                    System.arraycopy(dsi.getD(h + timeOff.get(i)), 0, outArr, counter, dsi.width);
                    counter += dsi.width;
                }
                break;
            case PAR:
                double[] tempArr;
                int tOff;
                // for every DS from MDS
                for(int i=0; i<dsSource.size(); i++){
                    dsi = dsSource.get(i);
                    tOff =  h - timeOff.get(i);
                    if(tOff >= 0){
                        tempArr = dsi.getD(tOff);
                        for(int j=0; j<outArr.length; j++)
                            outArr[j] += tempArr[j];
                    }
                }  
                break;
        }               
        return outArr;
    }
}