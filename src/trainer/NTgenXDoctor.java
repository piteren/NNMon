/*
 * 2017 (c) piteren
 */
package trainer;

import deepLearn.NNetwork;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import utilities.URand;

/**
 * injects genX
 */
public class NTgenXDoctor {
    
    private SimpleBooleanProperty processorActive = new SimpleBooleanProperty();//mark for activity of processor
    private int genXChildsRange = 50,                                           //% of sorted solvers to be genX injected
                genXParRange = 25;                                              //% of sorted solvers to be considered as a parents
    
    //constructor
    NTgenXDoctor(boolean pAct){
        processorActive.set(pAct);
    }
    
    public void setGenXChildsRange(int newGXCrang){
        genXChildsRange = newGXCrang;
        genXParRange = genXChildsRange/2;
    }
    
    public SimpleBooleanProperty getProcessorActive(){
        return processorActive;
    }
    
    //generates genX_solvers for given list of solvers (should be sorted from top)
    public void genXop(List<NetTrainerAISolver> sourceSolv){
        if(processorActive.getValue()){
            System.out.println("...genX injection");
            int numChilds = sourceSolv.size()*genXChildsRange/100; 
            int numParents = sourceSolv.size()*genXParRange/100; 
            
            int limiter;
            for(int i=0; i<numChilds; i++){
                ArrayList<NNetwork> NNa;
                limiter = URand.i(numParents);
                if (limiter<2) limiter=2;
                int a = URand.i(limiter);
                int b = URand.i(limiter);
                while(a==b) b = URand.i(limiter);   

                sourceSolv.get( sourceSolv.size()-1-i ).genX(sourceSolv.get(a), sourceSolv.get(b));
            }
        }
    }    
}