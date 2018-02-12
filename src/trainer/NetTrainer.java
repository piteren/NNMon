/*
 * 2017 (c) piteren
 */
package trainer;

import dataUtilities.GData;
import trainer.toCase.NTCase;
import utilities.ULogDoubleProperty;
import deepLearn.DLlearnParams;
import java.util.LinkedList;
import java.util.List;

import java.util.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import trainer.NTSolversManager.SolvOrder;
import utilities.threadRun.UTRobject;
import utilities.threadRun.UThreadRun;

/*
 * initializes traing_groups of solvers with training parameters, starts traning of groups in Envy, manages solvers with rewards
 */
public class NetTrainer extends Observable implements UTRobject{

    //************************************************************************** trainer base
    private List<NTCase>                myCases = new LinkedList();;                            //trainer cases list
    private final SimpleIntegerProperty caseRepeatNum = new SimpleIntegerProperty();            //number of internal case runs
    
    private final NTSolversManager      mySolvMan;                                              //solvers manager
    private final NTSolvPreProcessor    mySolvPrePro = new NTSolvPreProcessor();                //solvers preprocesor
    private final NTtopPosInspector     myTopPosInsp;                                           //top position inspector
    private final NTgenXDoctor          myGenXDr;                                               //genX doctor
    private final NTwinHSolvPromotor    myWinHSPromotor;                                        //historical winning solvers promotor

    //************************************************************************** loops & circle
    private final SimpleIntegerProperty loopAM = new SimpleIntegerProperty(),                   //amount of loops in one circle
                                        trLoopCounter = new SimpleIntegerProperty(0);           //counter of performed training loops (up to loopAM)
    private final SimpleBooleanProperty doLoops = new SimpleBooleanProperty();                  //flag for doing loops (thread breaker)
    private final SimpleIntegerProperty igdSolvScale = new SimpleIntegerProperty(1);            //interval solvers GData scale
    
    private int                         trainingCircleCounter = 0;                              //counter of performed training circles (one circle == loopAM loops)
    private long                        startCircleTimeMS,                                      //last circle time start in ms
                                        stopCircleTimeMS;                                       //last circle time stop in ms
    
    private int                         maxReportSolvAmount = 20;                               //max reported solvers amount
    
    private double                      tAvgWinRateOfCycle = 0;                                 //trainer average winrate of last cycle
        
    //************************************************************************** backpropagation params
    private DLlearnParams               trainerLearnParams;                                     //object with learning parameters managed by this trainer (and shared with solvers)
    
    public enum TrainerMessage{         CIRCLE_FINISHED,                                        //trainer messages for observers
                                        CIRCLE_HALTED,
                                        NEW_CIRCLE_STARTED;}
    
    //constructor(case, numCases, NNlearnParams)
    public NetTrainer(NTCase mCs, int casesNum, String pth, DLlearnParams tLP){
        for(int i=0; i<casesNum; i++) myCases.add(mCs.duplicate());
        trainerLearnParams = tLP;
        
        mySolvMan = new NTSolversManager( mySolvPrePro.makeSolvers( mCs.caseNumOfActors()*casesNum, pth, trainerLearnParams ) );
        
        myTopPosInsp = new NTtopPosInspector(true);
        myGenXDr = new NTgenXDoctor(true);
        myWinHSPromotor = new NTwinHSolvPromotor(false);       
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
    public SimpleIntegerProperty getIgdSolvScale(){
        return igdSolvScale;
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
    public SimpleBooleanProperty getDoNodeNorm(){
        return trainerLearnParams.doNodeNorm;
    }
    public ULogDoubleProperty getNNormUDecay(){
        return trainerLearnParams.nodeNormUDecay;
    }
    public SimpleDoubleProperty getNNormSDScale(){
        return trainerLearnParams.nodeNormSDScale;
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
    public void setWIDist(DLlearnParams.WInitDist wID){
        trainerLearnParams.wIDist = wID;
    }
    public void setLearnMeth(DLlearnParams.LearnMeth lM){
        trainerLearnParams.lMeth = lM;
        for(NetTrainerAISolver sol: getSolvers())
            sol.restartLrnMethodParams();
    }
    
    public List<NetTrainerAISolver> getSolvers(){
        return mySolvMan.getSolvers();
    }
    public List<NetTrainerAISolver> getSolvers(SolvOrder ord){
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
    
    public void resetGlobalSats(){
        for(NetTrainerAISolver sol: getSolvers())
            sol.resetGData();
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
        for(NetTrainerAISolver sol: mySolvMan.getSolvers()){
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
            for(NetTrainerAISolver sol: mySolvMan.getSolvers()){
                scNum += sol.getSclCount();
                scAm += sol.getSclAmnt();
                sol.resetScaleFacts();
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
    //puts all solvers to cases (random_swich)
    private void putSolversToCases(List<NetTrainerAISolver> allSolv){        
        int solCounter = 0;
        for(int i=0; i<myCases.size(); i++){
            //prepare case solvers list
            LinkedList<NetTrainerAISolver> caseSolvers = new LinkedList();
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
        for(NetTrainerAISolver solv:  mySolvMan.getSolvers()){
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
            
            for(NetTrainerAISolver solv: getSolvers()) 
                solv.resetIntervalERData( igdSolvScale.getValue() );
            
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
        myTopPosInsp.parentsCheck(trLoopCounter.getValue(), mySolvMan);
        
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

        List<NetTrainerAISolver> newSolvers = new LinkedList();
        if(myWinHSPromotor.isPromotorActive())
            newSolvers.addAll( myWinHSPromotor.promotedHWinningsolvers( getSolvers( SolvOrder.SORTED ) ) );

        myGenXDr.genXop( getSolvers( SolvOrder.SORTED ) );
        
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
}//Trainer