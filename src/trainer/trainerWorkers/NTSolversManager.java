/*
 * 2017 (c) piteren
 */
package trainer.trainerWorkers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import trainer.NetTrainerAISolver;
import utilities.URand;

/**
 * manages solvers list
 * sorts, randomizes, adds etc.
 */
public class NTSolversManager {

    private LinkedList<NetTrainerAISolver>  tSolvers = new LinkedList();        //manager solvers
    private SolvOrder                       solverLOState;                      //state (sort) of solvers list

    public enum SolvOrder{
        MODIFIED,
        SORTED,
        RANDOMIZED;
    }
    
    //constuctor
    public NTSolversManager(List<NetTrainerAISolver> solv){
        tSolvers.addAll(solv);
    }
    
    //return unmodifaiable list of solvers (read only) in order not specified (does not matter)
    public List<NetTrainerAISolver> getSolvers(){
        return Collections.unmodifiableList(tSolvers);
    }

    //return unmodifaiable list of solvers (read only) in order given by argument
    public List<NetTrainerAISolver> getSolvers(SolvOrder ord){
        switch( ord ){
            case MODIFIED:
                break;
            case SORTED:
                sortSolvers();
                break;
            case RANDOMIZED:
                randomizeSolvers();
                break;
        }
        return getSolvers();
    }
    
    //replaces worst performing solvers
    public void replaceWorstSolvers(List<NetTrainerAISolver> repSolv){
        sortSolvers();
        for(NetTrainerAISolver rs: repSolv) 
            if(!tSolvers.contains(rs))
                tSolvers.removeLast();
        for(NetTrainerAISolver rs: repSolv) 
            if(!tSolvers.contains(rs))
                tSolvers.add(rs);
        solverLOState = SolvOrder.MODIFIED;
    }
    
    //returns number of kept solvers
    public int solvNum(){
        return tSolvers.size();
    }
    
    //sort solvers by interval_reward_total (best first)
    private void sortSolvers(){
        if(solverLOState != SolvOrder.SORTED){
            LinkedList<NetTrainerAISolver> unsortedSolvers = new LinkedList();
            unsortedSolvers.addAll(tSolvers);
            tSolvers.clear();

            int index;
            double currMaxReward;
            while(unsortedSolvers.size()>0){
                index=0;
                currMaxReward=unsortedSolvers.get(0).getIRewData().getLastV();
                for(int i=0; i<unsortedSolvers.size(); i++){
                    if( currMaxReward < unsortedSolvers.get(i).getIRewData().getLastV() ){
                        index=i;
                        currMaxReward=unsortedSolvers.get(i).getIRewData().getLastV();
                    }
                }
                tSolvers.add(unsortedSolvers.remove(index));
            }    
            solverLOState = SolvOrder.SORTED;
        }
    }
    
    //randomizes solvers order in list
    private void randomizeSolvers(){
        LinkedList<NetTrainerAISolver> unrandomizedSolvers = new LinkedList();
        unrandomizedSolvers.addAll(tSolvers);
        tSolvers.clear();

        int ix;
        while(unrandomizedSolvers.size()>0){
            ix=URand.i(unrandomizedSolvers.size());
            tSolvers.add(unrandomizedSolvers.remove(ix));
        }
        solverLOState = SolvOrder.RANDOMIZED;
    }
}