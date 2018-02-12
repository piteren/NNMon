/*
 * 2017 (c) piteren
 */
package trainer;

import deepLearn.DLlearnParams;
import java.util.LinkedList;
import java.util.List;
import utilities.ULogDoubleProperty;

/**
 * performs solvers initialization and preprocessing tasks
 */
public class NTSolvPreProcessor {
    
    protected LinkedList<NetTrainerAISolver> makeSolvers(int sNum, String netPath, DLlearnParams tLearParams){
        LinkedList<NetTrainerAISolver> tSolvers = new LinkedList();
        while(tSolvers.size() < sNum){
            NetTrainerAISolver newSol = new NetTrainerAISolver(netPath, tLearParams);
            tSolvers.add(newSol);
        }
        return tSolvers;
    }
    
    public void reinitSolverWeights( List<NetTrainerAISolver> solvToReinit ){
        for(NetTrainerAISolver sol: solvToReinit) sol.initWeights();
    }
}