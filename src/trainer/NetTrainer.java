/**
 * 2017 (c) piteren
 */

package trainer;

import trainer.toCase.NTCase;
import neuralNetwork.NNLearnParams;
import dataUtilities.GData;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.paint.Color;

import utilities.URand;
import utilities.threadRun.UTRobject;
import utilities.threadRun.UThreadRun;
import utilities.ULogDoubleProperty;

/**
 * manages training of solvers @cases
 */
public class NetTrainer extends Observable implements UTRobject{

    //********************************************************************************************** trainer main fields

    private List<NTCase>                myCases = new LinkedList();;                            //trainer cases list
    private final SimpleIntegerProperty caseRepeatNum = new SimpleIntegerProperty();            //number of internal case runs
    
    private final NTSolversManager      mySolvMan;                                              //solvers manager
    private final NTSolvPreProcessor    mySolvPrePro = new NTSolvPreProcessor();                //solvers preprocessor
    private final NTtopPosInspector     myTopPosInsp = new NTtopPosInspector(false);       //top position inspector
    private final NTgenXDoctor          myGenXDr = new NTgenXDoctor(true);                 //genX doctor
    private final NTwinHSolvPromotor    myWinHSPromotor = new NTwinHSolvPromotor(false);   //historical winning solvers promotor

    //*************************************************************************************************** loops & circle

    private final SimpleIntegerProperty loopAM = new SimpleIntegerProperty(),                   //amount of loops in one circle
                                        trLoopCounter = new SimpleIntegerProperty(0); //counter of performed training loops (up to loopAM)
    private final SimpleBooleanProperty doLoops = new SimpleBooleanProperty();                  //flag for doing loops (thread breaker)

    private final SimpleIntegerProperty iGDSolvScale = new SimpleIntegerProperty(1);  //interval solvers GData scale
    
    private int                         trainingCircleCounter = 0;                              //counter of performed training circles (one circle == loopAM loops)
    private long                        startCircleTimeMS,                                      //last circle time start in ms
                                        stopCircleTimeMS;                                       //last circle time stop in ms
    
    private int                         maxReportSolvAmount = 20;                               //max reported solvers amount
    
    private double                      tAvgWinRateOfCycle = 0;                                 //trainer average winrate of last cycle
        
    //************************************************************************************************** backprop params

    private final NNLearnParams trainerLearnParams;                                             //object with learning parameters managed by this trainer (and shared with solvers)

    // trainer messages for observers
    public enum TrainerMessage {
        CIRCLE_FINISHED,
        CIRCLE_HALTED,
        NEW_CIRCLE_STARTED
    }

    // solvers order according to their performance
    public enum SolvOrder{
        MODIFIED,
        SORTED,
        RANDOMIZED
    }

    //****************************************************************************************** trainer workers classes

    // injects genX
    private class NTgenXDoctor {

        private SimpleBooleanProperty processorActive = new SimpleBooleanProperty();//mark for activity of processor
        private int genXChildsRange = 50,                                           //% of sorted solvers to be genX injected
                genXParRange = 25;                                              //% of sorted solvers to be considered as a parents

        //constructor
        public NTgenXDoctor(boolean pAct){
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
        public void genXop(List<NTaiSolver> sourceSolv){
            if(processorActive.getValue()){
                System.out.println("...genX injection");
                int numChilds = sourceSolv.size()*genXChildsRange/100;
                int numParents = sourceSolv.size()*genXParRange/100;

                int limiter;
                for(int i=0; i<numChilds; i++){
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

    //manages solvers list, works like solvers HR (sorts, randomizes, adds etc.)
    private class NTSolversManager {

        private LinkedList<NTaiSolver>  tSolvers = new LinkedList();        //manager solvers
        private SolvOrder                       solverLOState;                      //state (sort) of solvers list



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

    //performs solvers initialization and preprocessing tasks
    private class NTSolvPreProcessor {

        // creates list of solvers with given number, path to net_conf_file and learnig_parameteres
        public LinkedList<NTaiSolver> makeSolvers(int sNum, String netPath, NNLearnParams tLearParams){
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

    //controls (reads, adds) interval solvers position data, prepares top_position_slov_data
    private class NTtopPosInspector {

        private boolean             processorActive;        // mark of processor activity

        // *************************************************** data processing algorithm prams
        private int                 everyNLoopTP = 10,      // number of loops to perform top_position check
                                    numSolvCC = 4,          // number of top solvers to consider in pos calculations
                                    fromDataRange = 50;     // calculations data range (number of considered backward data samples)

        private GData               currTopPosSolvData;     // curr circle top position data

        //constructor
        public NTtopPosInspector(boolean pAct){
            processorActive = pAct;
            currTopPosSolvData = new GData(1, false, 0, Color.rgb(50,200,50));
        }

        public boolean isProcessorActive(){
            return processorActive;
        }

        public void setProcessorActive(boolean pAct){
            processorActive = pAct;
        }

        public GData getTopPosSData(){
            return currTopPosSolvData;
        }

        //checks solvers positions and updates pos data
        public void parentsCheck(int loopNum, List<NTaiSolver> sol){
            if(processorActive && loopNum%everyNLoopTP==0){
                int drawNum = numSolvCC;
                if(drawNum > sol.size()) drawNum = sol.size();

                // update solver iPosData
                for(int i=0; i<sol.size(); i++)
                    sol.get(i).getIPosData().add(i);

                // calculate & add trainerTopParentsRank
                int from = fromDataRange;
                if(from > sol.get(0).getIPosData().getSize()) from = sol.get(0).getIPosData().getSize();
                double val=0;
                for(int j=0; j<drawNum; j++)
                    val += sol.get(j).getIPosData().calcSumV(from);
                val=val/from-(numSolvCC-1)*numSolvCC/2;
                currTopPosSolvData.add(val);

                // smooth data here?
            }
        }

        public void resetPosData(List<NTaiSolver> sol){
            if(currTopPosSolvData!=null)
                currTopPosSolvData.flush();
        }
    }

    // responsible for promotion of historical winning solvers
    private class NTwinHSolvPromotor {

        private boolean processorActive;                                            //marker for winning solvers promotion option
        private LinkedList<NTaiSolver> wSolvers;                            //list of winnig solvers

        private int minSizeOfWSolversToStart=10;                                    //minimal size of winning solver list to start promotion procedure
        private int maxNumWSolvers=100;                                             //maximal number of previous looops winning solvers to keep
        private int numWPlayersToAdd = 2;                                           //number of previous loops winning solvers to add

        //constructor
        public NTwinHSolvPromotor(boolean pAct){
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
        public List<NTaiSolver> promotedHWinningsolvers(List<NTaiSolver> sourceSolv){
            //manage winning solvers list, keep their max size add current best solver
            if(wSolvers.size()==maxNumWSolvers) wSolvers.remove(URand.i(maxNumWSolvers));
            wSolvers.add(sourceSolv.get(0));

            //create list with some historical winning solvers
            List<NTaiSolver> proSolvers = new LinkedList();
            if(wSolvers.size() > minSizeOfWSolversToStart){
                int i = numWPlayersToAdd;
                while(i>0){
                    NTaiSolver oldPl = wSolvers.get(URand.i(wSolvers.size()-10 ) );
                    if(!sourceSolv.contains(oldPl))
                        proSolvers.add(oldPl);
                    i--;
                }
            }
            return proSolvers;
        }
    }


    // constructor(case, numCases, nnlearnParams)
    public NetTrainer(NTCase mCs, int casesNum, String pth, NNLearnParams tLP){
        for(int i=0; i<casesNum; i++) myCases.add(mCs.duplicate());
        trainerLearnParams = tLP;
        // create solvers and put to the manager
        List<NTaiSolver> trSolvers = mySolvPrePro.makeSolvers( mCs.caseNumOfActors()*casesNum, pth, trainerLearnParams );
        mySolvMan = new NTSolversManager( trSolvers );
    }
    
    public SimpleIntegerProperty getCaseRepeat(){
        return caseRepeatNum;
    }
    public SimpleBooleanProperty getDoLoops(){
        return doLoops;
    }
    public SimpleIntegerProperty getLoopAM(){
        return loopAM;
    } 
    public SimpleIntegerProperty getTrainingLoopCounter(){
        return trLoopCounter;
    }
    public SimpleIntegerProperty getIGDSolvScale(){
        return iGDSolvScale;
    }
    
    public SimpleBooleanProperty getDoBackpropLearning(){
        return trainerLearnParams.doBackprop;
    }
    public ULogDoubleProperty getLearningRate(){
        return trainerLearnParams.learningRate;
    }
    public SimpleDoubleProperty getMmx(){
        return trainerLearnParams.mmx;
    }
    public SimpleBooleanProperty getDoL2reg(){
        return trainerLearnParams.doL2reg;
    }
    public ULogDoubleProperty getL2regSize(){
        return trainerLearnParams.L2regSize;
    }
    public SimpleBooleanProperty getDoGradL(){
        return trainerLearnParams.doGradL;
    }
    public ULogDoubleProperty getGradLSize(){
        return trainerLearnParams.gradLSize;
    }
    public SimpleBooleanProperty getDoNodeNorm(){ return trainerLearnParams.doNodeNorm; }
    public ULogDoubleProperty getNNormUDecay(){
        return trainerLearnParams.nodeNormUPDecay;
    }
    public SimpleDoubleProperty getNNormSDScale(){
        return trainerLearnParams.nodeNormABScale;
    }
    public ULogDoubleProperty getTanhRanger(){
        return trainerLearnParams.tanhRanger;
    }
    public ULogDoubleProperty getTanhScaler(){
        return trainerLearnParams.tanhScaler;
    }
    public ULogDoubleProperty getBatchSize(){
        return trainerLearnParams.batchSize;
    }
    public void setWIDist(NNLearnParams.WInitDist wID){
        trainerLearnParams.wIDist = wID;
    }
    public void setLearnMeth(NNLearnParams.WUpdAlgorithm lM){
        trainerLearnParams.myWUpAlg = lM;
        for(NTaiSolver sol: getSolvers())
            sol.restartLrnMethodParams();
    }
    
    public List<NTaiSolver> getSolvers(){
        return mySolvMan.getSolvers();
    }
    public List<NTaiSolver> getSolvers(SolvOrder ord){
        return mySolvMan.getSolvers(ord);
    }
    
    public ULogDoubleProperty getWIScale(){
        return trainerLearnParams.wIScale;
    }
    public void reinitAllSolvWeights(){
        mySolvPrePro.reinitSolverWeights(getSolvers());
    }
    
    public GData getTopPosSData(){
        return myTopPosInsp.getTopPosSData();
    }
    
    public SimpleBooleanProperty getGenXProcAct(){
        return myGenXDr.getProcessorActive();
    } 
    
    public void setGenXChildsRange(int newGXCrang){
        myGenXDr.setGenXChildsRange(newGXCrang);
    }

    // resets all solvers global GData
    public void resetAllSolvGlobalGData(){
        for(NTaiSolver sol: getSolvers())
            sol.resetGGData();
    }
    
    //reports best solvers
    private void reportCycleStatsAndBestPl(int num){
        double deltaTime = stopCircleTimeMS-startCircleTimeMS;
        deltaTime=deltaTime/1000;
        System.out.println("Training cycle: "+trainingCircleCounter+", cycle time: "+deltaTime+" sec. ");
      //System.out.println("Average vinrate (bb/100) of top "+num+"/"+tSolvers.size()+" players: "+(int)(tAvgWinRateOfCycle*100/bb));
        
        double avgNN=0;
        double avgNC=0;
        double avgNL=0;
        for(NTaiSolver sol: mySolvMan.getSolvers()){
            avgNN+=sol.nNodes();
            avgNC+=sol.nParam();
            avgNL+=sol.nLays();
        }
        avgNN=avgNN/mySolvMan.solvNum();
        avgNC=avgNC/mySolvMan.solvNum();
        avgNL=avgNL/mySolvMan.solvNum();
        System.out.println("avgNHodes: "  +String.format("%1.1f",avgNN)+
                          " avgNParams: " +String.format("%1.1f",avgNC)+
                          " avgNLays: "   +String.format("%1.1f",avgNL));
        
        if(trainerLearnParams.doGradL.getValue()){
            long scNum = 0;
            double scAm = 0;
            for(NTaiSolver sol: mySolvMan.getSolvers()){
                scNum += sol.getSclCount();
                scAm += sol.getSclAmnt();
                sol.resetScaleCnts();
            }
            System.out.print("scaled gradients "+scNum+" times");
            if(scNum>0) System.out.print(" with avg scale "+String.format("%1.1f",scAm/scNum));
            System.out.println();
        }

        int prepNum = num;
        if(prepNum > mySolvMan.solvNum()) prepNum = mySolvMan.solvNum();
        for(int i=0; i<prepNum; i++){
            System.out.println(mySolvMan.getSolvers(SolvOrder.SORTED).get(i).myIntervalPerformance());
        }
    }

    //calculates and updates avgWinRateOfCycle
    private double cycleAvgWinR(){
        double totIR = 0;
        for(int i=0; i<mySolvMan.solvNum(); i++)
            totIR += mySolvMan.getSolvers().get(i).getIRewData().getLastV() / loopAM.getValue();
        return totIR/mySolvMan.solvNum();
    }

    //************************************************************************** training circle main logic methods

    //puts solvers to cases
    private void putSolversToCases(List<NTaiSolver> allSolv){
        int solCounter = 0;
        for(int i=0; i<myCases.size(); i++){
            //prepare case solvers list
            LinkedList<NTaiSolver> caseSolvers = new LinkedList();
            for(int j=0; j<myCases.get(i).caseNumOfActors(); j++)
                caseSolvers.add( allSolv.get(solCounter++) );
            myCases.get(i).takeSolvers(caseSolvers);
        }
    }
    
    //runs cases num times
    private void runThCases(int numRep) throws InterruptedException{
        LinkedList<UThreadRun> threadRunners = new LinkedList();
        //start case threads
        for(int i=0; i<myCases.size(); i++){            
            UThreadRun caseRunner = new UThreadRun(myCases.get(i), numRep);
            threadRunners.add(caseRunner);
        }
        //join case threads
        for(int i=0; i<threadRunners.size(); i++)
            threadRunners.get(i).join();
    }

    //runs all solvers backprop
    private void runThSolversBackprop() throws InterruptedException{
        LinkedList<UThreadRun> threadRunners = new LinkedList();
        //start backprop threads
        for(NTaiSolver solv:  getSolvers()){
            UThreadRun backpropRunner = new UThreadRun(solv);
            threadRunners.add(backpropRunner);
        }
        //join backprop threads
        for(int i=0; i<threadRunners.size(); i++)
            threadRunners.get(i).join();
    }
    
    //one loop algorithm
    private void doOneLoop() throws InterruptedException{
        //first loop in circle >> some preparation
        if(trLoopCounter.getValue()==0){
            startCircleTimeMS=System.currentTimeMillis();
            setChanged();
            notifyObservers(TrainerMessage.NEW_CIRCLE_STARTED);
            
            for(NTaiSolver solv: getSolvers())
                solv.resetIGData( iGDSolvScale.getValue() );
            
            myTopPosInsp.resetPosData(getSolvers());
        }
        
        trLoopCounter.setValue(trLoopCounter.getValue()+1 );
        
        putSolversToCases(getSolvers(SolvOrder.RANDOMIZED));
        runThCases(caseRepeatNum.getValue());
        
        //time for backpropagation
        if(trainerLearnParams.doBackprop.getValue())
            if(trLoopCounter.getValue()%getBatchSize().getLinIntegerValue()==0)
                runThSolversBackprop();                                             
       
        //parents check
        myTopPosInsp.parentsCheck(trLoopCounter.getValue(), getSolvers(SolvOrder.SORTED));
        
        //time for post circle revision >> last loop in circle
        if(trLoopCounter.getValue()%loopAM.getValue()==0){
            trLoopCounter.setValue(0);
            trainingCircleCounter++;
            
            stopCircleTimeMS = System.currentTimeMillis();
            doPostCircleProcess();
            
            setChanged();
            notifyObservers(TrainerMessage.CIRCLE_FINISHED);
        }
    }

    //post circle solvers revision (genX, etc...), reports...
    private void doPostCircleProcess(){
        
        //tAvgWinRateOfCycle=cycleAvgWinR(numT);
        reportCycleStatsAndBestPl(maxReportSolvAmount);

        List<NTaiSolver> newSolvers = new LinkedList();
        if(myWinHSPromotor.isPromotorActive())
            newSolvers.addAll( myWinHSPromotor.promotedHWinningsolvers( getSolvers(SolvOrder.SORTED)) );

        myGenXDr.genXop(getSolvers(SolvOrder.SORTED));
        
        mySolvMan.replaceWorstSolvers(newSolvers);
    }
    
    @Override
    public void runTR(int mTP){
        switch(mTP){
            case 0:                                                             //loops controlled by doLoops flag
                try {
                    while(doLoops.getValue())
                        doOneLoop();
                    setChanged();
                    notifyObservers(TrainerMessage.CIRCLE_HALTED);
                } catch (InterruptedException ex) {}
                break;
            case 1:                                                             //just single run
                try {
                    doOneLoop();
                } catch (InterruptedException ex) {}
                break;
            case 2:                                                             //loops with resetting weights
                try {
                    while(doLoops.getValue()){
                        doOneLoop();
                        reinitAllSolvWeights();
                    }
                    setChanged();
                    notifyObservers(TrainerMessage.CIRCLE_HALTED);
                } catch (InterruptedException ex) {}
                break;
            case 3:                                                             //sample test
                boolean learnInState = getDoBackpropLearning().getValue();
                getDoBackpropLearning().setValue(false);
                for(int i=0; i<myCases.size(); i++)
                    myCases.get(i).sampleTestRun();
                getDoBackpropLearning().setValue(learnInState);
                break;
        }
    }       
}//NetTrainer