/*
 * 2017 (c) piteren
 */
package trainerVC;

import dataUtilities.GData;
import dataUtilities.Histogram;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import neuralNetwork.NNLearnParams;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Slider;

import neuralNetwork.NNLearnParams.WUpdAlgorithm;
import neuralNetwork.NNLearnParams.WInitDist;

import java.util.LinkedList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;

import poker.toTrainer.NTPKTableCase;
import textPoetry.toTrainer.NTTextPoetryCase;
import trainer.NetTrainer.SolvOrder;
import trainer.NetTrainer;

import utilities.ULogDoubleProperty;
import utilities.threadRun.UThreadRun;

import rowDM.toTrainer.NTRowDMCase;

public class TVMainViewController implements Initializable, Observer {
    
    private NetTrainer              myTrainer;                                  //trainer managed by this controller
    private UThreadRun              myTrainerThread;
    
    private TVChartGDataController  gRChartController,
                                    iRChartController,
                                    gEChartController,
                                    iEChartController,
                                    gXChartController;
    
    private TVCandleController      myCandleController;
    
    @FXML
    private TabPane                 preRUNtabPane;
                                   
    @FXML
    private ToggleButton            resetGlobButton,
                                    runButton,
                                    tryButton;
    @FXML
    private Button                  runOnceButton,
                                    resetButton;
    
    @FXML
    private ProgressBar             loopsPgBar;
    private TVProgressBarManager    loopsPBman;

    @FXML
    private Label                   lAMLabel,
                                    eRLabel,
                                    lrLabel,
                                    batchSLabel,
                                    mxLabel,
                                    L2regLabel,
                                    gradLLabel,
                                    nnDecayLabel,
                                    nnScaleLabel,
                                    tanhRLabel,    
                                    tanhVLabel,
                                    gXrLabel,
                                    weightSLabel,
                                    updCLabel,
                                    rangeCLabel;
                                    
    @FXML
    private Slider                  maxSCSlider,
                                    lAMSlider,
                                    eRSlider,
                                    lrSlider,
                                    batchSSlider,
                                    mxSlider,
                                    L2regSlider,
                                    gradLSlider,
                                    nnDecaySlider,
                                    nnScaleSlider,
                                    tanhRSlider,
                                    tanhVSlider,
                                    gXrSlider,
                                    weightSSlider,
                                    updCSlider,
                                    rangeCSlider;

    @FXML
    private LineChart               globErrChart,
                                    intErrChart,
                                    globPerChart,
                                    intPerChart,
                                    genXChart;
    @FXML
    private NumberAxis              iPCxAxis,
                                    iECxAxis;
    @FXML
    private CheckBox                udRCB,
                                    doSTestCB,
                                    doLearnCB,
                                    L2regCB,
                                    gradLCB,
                                    nnormCB,
                                    genXCB;  
    @FXML
    private ChoiceBox               leftChartCB,
                                    rightChartCB,
                                    wInDistCB,
                                    wUpdMethCB,
                                    cLLayCB,        cS0LCB, cS1LCB, cS2LCB, cS3LCB, cS4LCB, cS5LCB, cS6LCB, cS7LCB, cS8LCB, cS9LCB,
                                    cLWhatCB,       cS0WCB, cS1WCB, cS2WCB, cS3WCB, cS4WCB, cS5WCB, cS6WCB, cS7WCB, cS8WCB, cS9WCB, 
                                    candPresetCB;
    @FXML
    private Canvas                  myCandleCanvas;
    
    //inits(binds) logSlider with label and logDoubleProperty, property field (myTsForm) decides about label display format
    private void initLogSliderLabelProperty(    Slider sl,                      //slider
                                                Label lb,                       //slider label
                                                ULogDoubleProperty prM,         //logDoubleProperty
                                                Number iVal){                   //initial value (log)
        sl.valueProperty().bindBidirectional(prM);
        switch(prM.getTsForm()){
            case INT:
                lb.textProperty().bind(Bindings.createStringBinding(prM::linIntegerValueToString, prM) );
                break;
            case DBL:
                lb.textProperty().bind(Bindings.createStringBinding(prM::linDoubleValueToString, prM) );
                break;
        }
        prM.setValue(iVal);
    }
         
    //registers new PerformanceCharts observables
    private void registerPChartsObs(){
        LinkedList<GData> obsD = new LinkedList();
        int numSolv = myTrainer.getSolvers().size();
        if(numSolv>maxSCSlider.valueProperty().intValue()) numSolv = maxSCSlider.valueProperty().intValue();
        
        //calc num from backend list
        int numDSolv = 0;
        if(udRCB.selectedProperty().getValue()) numDSolv = numSolv/2;
        
        for(int i=0; i<numSolv-numDSolv; i++) obsD.add( myTrainer.getSolvers(SolvOrder.SORTED).get(i).getGRewData() );
        for(int i=0; i<numDSolv; i++) obsD.add( myTrainer.getSolvers(SolvOrder.SORTED).get( myTrainer.getSolvers().size()-1-i ).getGRewData() );
        gRChartController.setObservables(obsD);
        obsD.clear();
        for(int i=0; i<numSolv-numDSolv; i++) obsD.add( myTrainer.getSolvers(SolvOrder.SORTED).get(i).getIRewData() );   
        for(int i=0; i<numDSolv; i++) obsD.add( myTrainer.getSolvers(SolvOrder.SORTED).get( myTrainer.getSolvers().size()-1-i ).getIRewData() );
        iRChartController.setObservables(obsD);
        obsD.clear();
        for(int i=0; i<numSolv-numDSolv; i++) obsD.add( myTrainer.getSolvers(SolvOrder.SORTED).get(i).getGErrData() );
        for(int i=0; i<numDSolv; i++) obsD.add( myTrainer.getSolvers(SolvOrder.SORTED).get( myTrainer.getSolvers().size()-1-i ).getGErrData() );
        gEChartController.setObservables(obsD);
        obsD.clear();
        for(int i=0; i<numSolv-numDSolv; i++) obsD.add( myTrainer.getSolvers(SolvOrder.SORTED).get(i).getIErrData() );   
        for(int i=0; i<numDSolv; i++) obsD.add( myTrainer.getSolvers(SolvOrder.SORTED).get( myTrainer.getSolvers().size()-1-i ).getIErrData() );
        iEChartController.setObservables(obsD);
    }

    // updates line charts layout (pos and visibility) according to line_charts_choice_boxes_selection
    private void updatePChartsLay(){
        LinkedList<LineChart>   chL = new LinkedList(),
                chR = new LinkedList();
        chL.add(globErrChart);
        chL.add(intErrChart);
        chL.add(globPerChart);
        chL.add(intPerChart);
        chR.add(globErrChart);
        chR.add(intErrChart);
        chR.add(globPerChart);
        chR.add(intPerChart);
        for(LineChart lc: chL)
            lc.setVisible(false);
        for(LineChart lc: chR)
            lc.setVisible(false);
        if(leftChartCB.getSelectionModel().selectedIndexProperty().getValue() > 0){
            chL.get(leftChartCB.getSelectionModel().selectedIndexProperty().getValue()-1).setLayoutX(0);
            chL.get(leftChartCB.getSelectionModel().selectedIndexProperty().getValue()-1).setVisible(true);
        }
        if(rightChartCB.getSelectionModel().selectedIndexProperty().getValue() > 0) {
            chR.get(rightChartCB.getSelectionModel().selectedIndexProperty().getValue() - 1).setLayoutX(590);
            chR.get(rightChartCB.getSelectionModel().selectedIndexProperty().getValue() - 1).setVisible(true);
        }
    }
    
    private void startTrainerThread(int mTP){
        if(myTrainerThread==null || myTrainerThread.isTerminated()){
            myTrainer.getDoLoops().setValue(true);
            myTrainerThread = new UThreadRun(myTrainer, mTP);                          
        }
    }

    private void stopTrainerThread() throws InterruptedException{
        if(myTrainerThread!=null){
            myTrainer.getDoLoops().setValue(false);
            myTrainerThread.join();
            System.out.println("joined");
        }
    }
    
    private void initCandleController(){
        //build list of choiceboxes
        LinkedList<ChoiceBox> cSLcbList = new LinkedList();
        cSLcbList.add(cS0LCB);
        cSLcbList.add(cS1LCB);
        cSLcbList.add(cS2LCB);
        cSLcbList.add(cS3LCB);
        cSLcbList.add(cS4LCB);
        cSLcbList.add(cS5LCB);
        cSLcbList.add(cS6LCB);
        cSLcbList.add(cS7LCB);
        cSLcbList.add(cS8LCB);
        cSLcbList.add(cS9LCB);
        LinkedList<ChoiceBox> cSWcbList = new LinkedList();
        cSWcbList.add(cS0WCB);
        cSWcbList.add(cS1WCB);
        cSWcbList.add(cS2WCB);
        cSWcbList.add(cS3WCB);
        cSWcbList.add(cS4WCB);
        cSWcbList.add(cS5WCB);
        cSWcbList.add(cS6WCB);
        cSWcbList.add(cS7WCB);
        cSWcbList.add(cS8WCB);
        cSWcbList.add(cS9WCB);
        
        myCandleController = new TVCandleController(myTrainer, myCandleCanvas, candPresetCB, cLLayCB, cLWhatCB, cSLcbList, cSWcbList);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {    
        //!! choose one trainer
        //myTrainer = new NetTrainer(new NTTextPoetryCase("TXTs/textC.txt"), 1, "NETs/netTXffwd.txt", new NNLearnParams());
        //myTrainer = new NetTrainer(new NTTextPoetryCase("TXTs/textA.txt"), 1, "NETs/netTXrnn.txt", new NNLearnParams());
        //myTrainer = new NetTrainer(new NTTextPoetryCase("TXTs/textA.txt"), 1, "NETs/netTXlstmOLD.txt", new NNLearnParams());
        //myTrainer = new NetTrainer(new NTTextPoetryCase("TXTs/textB.txt"), 1, "NETs/netTXlstmN1L.txt", new NNLearnParams());
        //myTrainer = new NetTrainer(new NTTextPoetryCase("TXTs/textB.txt"), 1, "NETs/netTXlstmE_1L.txt", new NNLearnParams());
        //myTrainer = new NetTrainer(new NTPKTableCase(3), 1, "NETs/netPKffwd.txt", new NNLearnParams());
        //myTrainer = new NetTrainer(new NTPKTableCase(3), 1, "NETs/netPKlstm.txt", new NNLearnParams());
        //myTrainer = new NetTrainer(new NTRowDMCase("RDMdata/rowData.txt"), 1, "NETs/netRDMffwd.txt", new NNLearnParams());
<<<<<<< HEAD
        //myTrainer = new NetTrainer(new NTRowDMCase("RDMdata/normTData.txt"), 1, "NETs/netRDMnormTestffwd.txt", new NNLearnParams());
        //myTrainer = new NetTrainer(new NTRowDMCase("/home/p.niewinski/teraspace/R&D_projects/spacesModel/_workingFiles/FR_001sample/cvProperties.txt"), 1, "NETs/netRDMspacesFFWD.txt", new NNLearnParams());
        myTrainer = new NetTrainer(new NTRowDMCase("../_Gdata/spaces/cvProperties.txt"), 1, "NETs/netRDMspacesR.txt", new NNLearnParams());
=======
        myTrainer = new NetTrainer(new NTRowDMCase("RDMdata/normTData.txt"), 1, "NETs/netRDMnormTestffwd.txt", new NNLearnParams());
        //myTrainer = new NetTrainer(new NTRowDMCase("/home/p.niewinski/teraspace/R&D_projects/spacesModel/_workingFiles/FR_001sample/cvProperties.txt"), 1, "NETs/netRDMspacesFFWD.txt", new NNLearnParams());
>>>>>>> c93a836729b2d88c6b6ab3ec1b564746c052410c
        
        myTrainer.addObserver(this);
        
        //********************************************************************** preRUN tab pane
        
        preRUNtabPane.getSelectionModel().select(1);
        
        wInDistCB.getItems().add("UNIFORM");
        wInDistCB.getItems().add("GAUSSIAN");
        wInDistCB.getSelectionModel().selectedIndexProperty().addListener( new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue ov, Number pVal, Number nVal){
                //update chioceBoxes disability
                switch((int)nVal){
                    case 0: myTrainer.setWIDist(WInitDist.UNIFORM);             //UNIFORM
                            break;
                    case 1: myTrainer.setWIDist(WInitDist.GAUSSIAN);            //GAUSS
                            break;
                }
            }
        });
        wInDistCB.setValue("UNIFORM");
        
        resetButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                myTrainer.reinitAllSolvWeights();
            }
        });
        initLogSliderLabelProperty(weightSSlider, weightSLabel, myTrainer.getWIScale(), 0);
        tryButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                if(tryButton.isSelected()){
                    myTrainer.getDoBackpropLearning().setValue(false);
                    startTrainerThread(2);
                    tryButton.setText("stop");
                    resetButton.setDisable(true);
                    preRUNtabPane.getTabs().get(1).setDisable(true);
                }
                else{
                    try {
                        stopTrainerThread();
                    } catch (InterruptedException ex) {}
                    tryButton.setText("try");
                    resetButton.setDisable(false);
                    preRUNtabPane.getTabs().get(1).setDisable(false);
                }
            }
        });
        
        //********************************************************************** RUN tab pane
        
        runButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                if(runButton.isSelected()){
                    startTrainerThread(0);
                    runButton.setText("STOP");
                    runOnceButton.setDisable(true);
                    preRUNtabPane.getTabs().get(0).setDisable(true);
                }
                else{
                    try {
                        stopTrainerThread();
                    } catch (InterruptedException ex) {}
                    runButton.setText("RUN");
                    runOnceButton.setDisable(false);
                    preRUNtabPane.getTabs().get(0).setDisable(false);
                }  
            }
        });
        runOnceButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                if(myTrainerThread==null || myTrainerThread.isTerminated())
                    myTrainerThread = new UThreadRun(myTrainer,1); 
            }
        });

        loopsPBman = new TVProgressBarManager(loopsPgBar, myTrainer.getTrainingLoopCounter());
        
        //future loopAM & caseRepeat slider
        lAMSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                lAMLabel.textProperty().setValue( String.valueOf( (int)Math.pow(10, new_val.doubleValue() ) ) );
            }
        });
        lAMSlider.valueProperty().setValue(3);
        eRSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                eRLabel.textProperty().setValue( String.valueOf( (int)Math.pow(10, new_val.doubleValue() ) ) );
            }
        });
        eRSlider.valueProperty().setValue(0);
        
        //learn CB & sliders
        doLearnCB.selectedProperty().bindBidirectional(myTrainer.getDoBackpropLearning());      
        initLogSliderLabelProperty(lrSlider,        lrLabel,        myTrainer.getLearningRate(), -7);        
        initLogSliderLabelProperty(batchSSlider,    batchSLabel,    myTrainer.getBatchSize(),     0);
        
        //update method CB & mmx slider
<<<<<<< HEAD
        wUpdMethCB.getItems().add("CLASS");
=======
>>>>>>> c93a836729b2d88c6b6ab3ec1b564746c052410c
        wUpdMethCB.getItems().add("MMNTM");
        wUpdMethCB.getItems().add("ADAM");
        wUpdMethCB.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue ov, Number pVal, Number nVal){
                switch((int)nVal){
<<<<<<< HEAD
                    case 0: myTrainer.setLearnMeth(WUpdAlgorithm.CLASS);            //CLASSIC
                            break;
                    case 1: myTrainer.setLearnMeth(WUpdAlgorithm.MMNTM);            //MOMENTUM
                            break;
                    case 2: myTrainer.setLearnMeth(WUpdAlgorithm.ADAM);             //ADAM
                        break;
                }
            }
        });
        wUpdMethCB.setValue("CLASS");
=======
                    case 0: myTrainer.setLearnMeth(WUpdAlgorithm.MMNTM);            //MOMENTUM
                            break;
                    case 1: myTrainer.setLearnMeth(WUpdAlgorithm.ADAM);             //ADAM
                            break;
                }
            }
        });
        wUpdMethCB.setValue("MMNTM");
>>>>>>> c93a836729b2d88c6b6ab3ec1b564746c052410c
        mxSlider.valueProperty().bindBidirectional( myTrainer.getMmx() );
        mxLabel.textProperty().bind(mxSlider.valueProperty().asString("%1.2f"));
        
        //L2reg
        L2regCB.selectedProperty().bindBidirectional( myTrainer.getDoL2reg() );
        initLogSliderLabelProperty(L2regSlider,     L2regLabel,     myTrainer.getL2regSize(),   -18); 
        
        //gradient limiter
        gradLCB.selectedProperty().bindBidirectional( myTrainer.getDoGradL() );
        initLogSliderLabelProperty(gradLSlider,     gradLLabel,     myTrainer.getGradLSize(),     1);
        
        //node norm
        nnormCB.selectedProperty().bindBidirectional( myTrainer.getDoNodeNorm() );
        initLogSliderLabelProperty(nnDecaySlider,   nnDecayLabel,   myTrainer.getNNormUDecay(),  -4);
        nnScaleSlider.valueProperty().bindBidirectional( myTrainer.getNNormSDScale() );
        nnScaleLabel.textProperty().bind(nnScaleSlider.valueProperty().asString("%1.2f"));
        
        //reward scaling
        initLogSliderLabelProperty(tanhRSlider,     tanhRLabel,     myTrainer.getTanhRanger(),    0);        
        initLogSliderLabelProperty(tanhVSlider,     tanhVLabel,     myTrainer.getTanhScaler(),    0);
        
        //genX CB & slider
        genXCB.selectedProperty().bindBidirectional( myTrainer.getGenXProcAct() );
        genXCB.selectedProperty().setValue(false);
        gXrSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                myTrainer.setGenXChildsRange(new_val.intValue());
                gXrLabel.textProperty().setValue( String.valueOf( new_val.intValue() ) );
            }
        });
        gXrSlider.valueProperty().setValue(15);
        
        // init graph controllers for performance data
        gRChartController = new TVChartGDataController(globPerChart,false);
        iRChartController = new TVChartGDataController(intPerChart, true);
        gEChartController = new TVChartGDataController(globErrChart,false);
        iEChartController = new TVChartGDataController(intErrChart, false);
        iPCxAxis.setAutoRanging(false);
        iPCxAxis.setLowerBound(0);
        iECxAxis.setAutoRanging(false);
        iECxAxis.setLowerBound(0);
        //init graph controller for parents data and set observable
        gXChartController = new TVChartGDataController(genXChart, true);
        gXChartController.setObservables( myTrainer.getTopPosSData() );

        // set graph switch options and listeners
        leftChartCB.getItems().add("OFF");
        leftChartCB.getItems().add("GLOBAL LOSS");
        leftChartCB.getItems().add("INTERVAL LOSS");
        leftChartCB.getItems().add("GLOBAL REWARD");
        leftChartCB.getItems().add("INTERVAL REWARD");
        rightChartCB.getItems().add("OFF");
        rightChartCB.getItems().add("GLOBAL LOSS");
        rightChartCB.getItems().add("INTERVAL LOSS");
        rightChartCB.getItems().add("GLOBAL REWARD");
        rightChartCB.getItems().add("INTERVAL REWARD");
        leftChartCB.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue ov, Number pVal, Number nVal){
                if(rightChartCB.getSelectionModel().selectedIndexProperty().getValue()==(int)nVal)
                    rightChartCB.setValue("OFF");
                updatePChartsLay();
            }
        });
        rightChartCB.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue ov, Number pVal, Number nVal){
                if(leftChartCB.getSelectionModel().selectedIndexProperty().getValue()==(int)nVal)
                    leftChartCB.setValue("OFF");
                updatePChartsLay();
            }
        });
        leftChartCB.setValue("GLOBAL LOSS");
        rightChartCB.setValue("INTERVAL REWARD");
        
        initCandleController();
        initLogSliderLabelProperty(updCSlider,      updCLabel,      Histogram.getNotfDelayMs(),           2);
        initLogSliderLabelProperty(rangeCSlider,    rangeCLabel,    myCandleController.getRangeCandle(),  0);
    }
    
    @Override
    public void update(Observable o, Object arg) {
        System.out.println(arg);
        
        switch( (NetTrainer.TrainerMessage)arg ){
            case NEW_CIRCLE_STARTED:
                // prepare loopAM & caseRep values for trainer new circle
                int newLoopAM = (int)Math.pow(10, lAMSlider.valueProperty().getValue() );
                int newCaseRe = (int)Math.pow(10, eRSlider.valueProperty().getValue() );
                myTrainer.getLoopAM().setValue( newLoopAM );
                myTrainer.getCaseRepeat().setValue( newCaseRe );
                // calc new scale of iGD for solvers and axis bound for iCharts
                int scl = (newLoopAM*newCaseRe-1)/1000 +1;
                int iChartBound = newLoopAM*newCaseRe/scl + 1;
                myTrainer.getIGDSolvScale().setValue(scl);
                iPCxAxis.setUpperBound(iChartBound);
                iECxAxis.setUpperBound(iChartBound);
                // new bound for performance bar
                loopsPBman.setMyBound( myTrainer.getLoopAM().getValue() );
                
                // reset global GD (performance data)
                if(resetGlobButton.isSelected()){
                    myTrainer.resetAllSolvGlobalGData();
                    resetGlobButton.setSelected(false);
                }
                
                registerPChartsObs();
                
                myCandleController.updateSolversList(maxSCSlider.valueProperty().intValue(), udRCB.selectedProperty().getValue());
                break;
            case CIRCLE_FINISHED:
                if(doSTestCB.isSelected()){
                    myTrainerThread = new UThreadRun(myTrainer,3);
                    try {
                        myTrainerThread.join();
                    } catch (InterruptedException ex) {}
                }
                break;
        }
    }
    
    //a little cleaning on without_stopping_trainer_thread_EXIT
    public void stop() throws InterruptedException {
        stopTrainerThread();
    }
}