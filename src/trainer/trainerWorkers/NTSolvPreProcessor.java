/*
 * 2017 (c) piteren
 */
package trainer.trainerWorkers;

import deepLearn.DLlearnParams;
import java.util.LinkedList;
import java.util.List;

import trainer.NTaiSolver;

/**
 * performs solvers initialization and preprocessing tasks
 */
public class NTSolvPreProcessor {
    
    public LinkedList<NTaiSolver> makeSolvers(int sNum, String netPath, DLlearnParams tLearParams){
        LinkedList<NTaiSolver> tSolvers = new LinkedList();
        while(tSolvers.size() < sNum){
            NTaiSolver newSol = new NTaiSolver(netPath, tLearParams);
            tSolvers.add(newSol);
        }
        return tSolvers;
    }
    
    public void reinitSolverWeights( List<NTaiSolver> solvToReinit ){
        for(NTaiSolver sol: solvToReinit) sol.initWeights();
    }
}