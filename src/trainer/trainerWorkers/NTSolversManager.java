/*
 * 2017 (c) piteren
 */
package trainer.trainerWorkers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import trainer.NTaiSolver;
import utilities.URand;

/**
 * manages solvers list
 * sorts, randomizes, adds etc.
 */
public class NTSolversManager {

    private LinkedList<NTaiSolver>  tSolvers = new LinkedList();        //manager solvers
    private SolvOrder                       solverLOState;                      //state (sort) of solvers list

    public enum SolvOrder{
        MODIFIED,
        SORTED,
        RANDOMIZED;
    }
    
    //constuctor
    public NTSolversManager(List<NTaiSolver> solv){
        tSolvers.addAll(solv);
    }
    
    //return unmodifaiable list of solvers (read only) in order not specified (does not matter)
    public List<NTaiSolver> getSolvers(){
        return Collections.unmodifiableList(tSolvers);
    }

    //return unmodifaiable list of solvers (read only) in order given by argument
    public List<NTaiSolver> getSolvers(SolvOrder ord){
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
    public void replaceWorstSolvers(List<NTaiSolver> repSolv){
        sortSolvers();
        for(NTaiSolver rs: repSolv)
            if(!tSolvers.contains(rs))
                tSolvers.removeLast();
        for(NTaiSolver rs: repSolv)
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
            LinkedList<NTaiSolver> unsortedSolvers = new LinkedList();
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
        LinkedList<NTaiSolver> unrandomizedSolvers = new LinkedList();
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