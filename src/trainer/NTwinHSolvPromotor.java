/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trainer;

import java.util.LinkedList;
import java.util.List;
import utilities.URand;

/**
 * responsible for promotion of historical winning solvers
 */
public class NTwinHSolvPromotor {
    
    private boolean processorActive;                                            //marker for winning solvers promotion option
    private LinkedList<NetTrainerAISolver> wSolvers;                            //list of winnig solvers
    
    private int minSizeOfWSolversToStart=10;                                    //minimal size of winning solver list to start promotion procedure
    private int maxNumWSolvers=100;                                             //maximal number of previous looops winning solvers to keep
    private int numWPlayersToAdd = 2;                                           //number of previous loops winning solvers to add
    
    //constructor
    NTwinHSolvPromotor(boolean pAct){
        processorActive = pAct;
        wSolvers = new LinkedList();
    }
    
    public boolean isPromotorActive(){
        return processorActive;
    }
    
    public void setProcessorActive(boolean pAct){
        processorActive = pAct;
    }
    
    //promotes some historical winning solvers
    public List<NetTrainerAISolver> promotedHWinningsolvers(List<NetTrainerAISolver> sourceSolv){
        //manage winning solvers list, keep their max size add current best solver
        if(wSolvers.size()==maxNumWSolvers) wSolvers.remove(URand.i(maxNumWSolvers));
        wSolvers.add(sourceSolv.get(0));

        //create list with some historical winning solvers
        List<NetTrainerAISolver> proSolvers = new LinkedList();
        if(wSolvers.size() > minSizeOfWSolversToStart){
            int i = numWPlayersToAdd;
            while(i>0){
                NetTrainerAISolver oldPl = wSolvers.get(URand.i(wSolvers.size()-10 ) );
                if(!sourceSolv.contains(oldPl))
                    proSolvers.add(oldPl);
                i--;
            }
        }
        return proSolvers;
    }
}