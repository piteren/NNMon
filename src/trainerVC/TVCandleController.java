/*
 * 2017 (c) piteren
 */
package trainerVC;

import dataUtilities.Histogram;
import deepLearn.DLNetworkedObject;
import deepLearn.DLNetworkedObject.DLlayType;
import java.util.LinkedList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ChoiceBox;
import trainer.trainerWorkers.NTSolversManager.SolvOrder;
import trainer.NetTrainer;
import trainer.NTaiSolver;
import utilities.ULogDoubleProperty;

/**
 * controls candle feature,
 * holds many choiceBoxes and listens to their changes,
 * finds proper histograms for listened changes
 * sends those histograms to canvasController as observable objects
 */
public class TVCandleController {
    
    private final NetTrainer                myTrainer;
    private List<NTaiSolver>        myDispSolvers = new LinkedList();   //list of solvers to display
                     
    private final int                       colCNum = 10,                       //number of candle columns
                                            rowCNum = 10;                       //number of candle rows
    
    private final ChoiceBox                 candPresetCB,
                                            cLLayCB,
                                            cLWhatCB;
    private final LinkedList<ChoiceBox>     cSLcbList,
                                            cSWcbList;
    
    private final TVCandleCanvasController  myCCanvController;
    
    //constructor
    protected TVCandleController(   NetTrainer myTr,                            //trainer
                                    Canvas cCanvas,                             //canvas of candles
                                    ChoiceBox mCB,                              //choiceBoxes ...
                                    ChoiceBox lLCB, 
                                    ChoiceBox lWCB, 
                                    LinkedList<ChoiceBox> cSLlist, 
                                    LinkedList<ChoiceBox> cSWlist){
        myTrainer = myTr;
        myCCanvController = new TVCandleCanvasController(cCanvas,colCNum,rowCNum);
        
        candPresetCB = mCB;
        cLLayCB = lLCB;
        cLWhatCB = lWCB;
        cSLcbList = cSLlist;
        cSWcbList = cSWlist;
        
        initStart();
        
        //main CB start value
        candPresetCB.setValue("LAY/SOLV");
        updateAllCandlesAndLeds();
    }
    
    public ULogDoubleProperty getRangeCandle(){
        return myCCanvController.rangeCandle;
    }
    
    //inits starting values for controller UI elements
    private void initStart(){
        //init main choiceBox
        candPresetCB.getItems().add("off");
        candPresetCB.getItems().add("SOLV/LAY");
        candPresetCB.getItems().add("LAY/SOLV");
        
        //init LAY/SOLV LAY choiceBox
        cLLayCB.getItems().add("FC LAYs");
        cLLayCB.getItems().add("ALL LAYs");
        cLLayCB.setValue("ALL LAYs");
        
        //init LAY/SOLV WHAT choiceBox
        cLWhatCB.getItems().add("vIN");
        cLWhatCB.getItems().add("vW");
        cLWhatCB.getItems().add("dW");
        cLWhatCB.getItems().add("vNN");
        cLWhatCB.getItems().add("nnOff");
        cLWhatCB.getItems().add("nnScl");
        cLWhatCB.getItems().add("vNOD");
        cLWhatCB.getItems().add("vOUT");
        cLWhatCB.getItems().add("dOUT");
        cLWhatCB.setValue("vNOD");
        
        //init SOLV/LAY WHAT choiceBoxes
        for(ChoiceBox cb: cSWcbList){
            cb.getItems().add("vIN");
            cb.getItems().add("vW");
            cb.getItems().add("dW");
            cb.getItems().add("vNN");
            cb.getItems().add("nnOff");
            cb.getItems().add("nnScl");
            cb.getItems().add("vNOD");
            cb.getItems().add("vOUT");
            cb.getItems().add("dOUT");
            cb.setValue("vIN");
        }
        
        //init SOLV/LAY LAY choiceBoxes
        for(ChoiceBox cb: cSLcbList) cb.getItems().add("off");
        int count = 1;
        for(DLNetworkedObject lay: myTrainer.getSolvers().get(0).getALLsubLayers()){
            String layS = String.valueOf(count++)+"."+lay.getLayType().toString();
            for(ChoiceBox cb: cSLcbList) cb.getItems().add(layS);
        }
        for(ChoiceBox cb: cSLcbList) cb.setValue("off");
        
        //CB listeners
        candPresetCB.getSelectionModel().selectedIndexProperty().addListener( new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue ov, Number pVal, Number nVal){
                //update chioceBoxes disability
                switch((int)nVal){
                    case 0: cLLayCB.setDisable(true);                           //off
                            cLWhatCB.setDisable(true);
                            for(ChoiceBox cb: cSLcbList) cb.setDisable(true);
                            for(ChoiceBox cb: cSWcbList) cb.setDisable(true);
                            break;
                    case 1: cLLayCB.setDisable(true);                           //SOLV/LAY
                            cLWhatCB.setDisable(true);
                            for(ChoiceBox cb: cSLcbList) cb.setDisable(false);
                            break;
                    case 2: cLLayCB.setDisable(false);                          //LAY/SOLV
                            cLWhatCB.setDisable(false);
                            for(ChoiceBox cb: cSLcbList) cb.setDisable(true);
                            for(ChoiceBox cb: cSWcbList) cb.setDisable(true);
                            break;
                }
                updateAllCandlesAndLeds();
            }
        });
        cLLayCB.getSelectionModel().selectedIndexProperty().addListener( new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue ov, Number pVal, Number nVal){
                refreshLaySolvLayout();
            }
        });
        cLWhatCB.getSelectionModel().selectedIndexProperty().addListener( new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue ov, Number pVal, Number nVal){
                refreshLaySolvLayout();
            }
        });
        for(ChoiceBox cb: cSLcbList)
            cb.getSelectionModel().selectedIndexProperty().addListener( new ChangeListener<Number>(){
                @Override
                public void changed(ObservableValue ov, Number pVal, Number nVal){
                    refreshSolvColumn( cSLcbList.indexOf(cb) );
                }
            });
        for(ChoiceBox cb: cSWcbList)
            cb.getSelectionModel().selectedIndexProperty().addListener( new ChangeListener<Number>(){
                @Override
                public void changed(ObservableValue ov, Number pVal, Number nVal){
                    refreshSolvColumn( cSWcbList.indexOf(cb) );
                }
            });
    }
    
    //updates list of solvers to display with new number and display order, then updates canvas with this list
    public void updateSolversList(int num, boolean tbSolvOrder){
        int numObsSolv = num;
        if(numObsSolv > myTrainer.getSolvers().size()) numObsSolv = myTrainer.getSolvers().size();
        
        //prepare list of solvers to display
        myDispSolvers.clear();
        List<NTaiSolver> myTrSolvers = myTrainer.getSolvers(SolvOrder.SORTED);
        int numFromBack = 0;
        if(tbSolvOrder) numFromBack = numObsSolv/2;
        //add from the top
        for(int i=0; i<numObsSolv-numFromBack; i++)
            myDispSolvers.add( myTrSolvers.get(i) );
        //add from the end
        for(int i=numFromBack; i>0; i--)
            myDispSolvers.add( myTrSolvers.get( myTrSolvers.size()-i ) );
        
        updateAllCandlesAndLeds();
    }
    
    //updates all candles (whole canvas)
    private void updateAllCandlesAndLeds(){
        //unregister all histograms and clear all leds
        for(int nX=0; nX<colCNum; nX++)
            for(int nY=0; nY<rowCNum; nY++)
                myCCanvController.setHistogramObs(null,nX,nY);
        for(int nX=0; nX<colCNum; nX++) myCCanvController.drawXled(nX, null);
        for(int nY=0; nY<rowCNum; nY++) myCCanvController.drawYled(nY, null);
        
        switch(candPresetCB.getSelectionModel().selectedIndexProperty().getValue()){
            case 0:    
                break;
            case 1:
                for(int i=0; i<myDispSolvers.size(); i++) myCCanvController.drawYled(i, myDispSolvers.get(i).getMyColor());
                refreshSolvLayLayout();
                break;
            case 2:
                for(int i=0; i<myDispSolvers.size(); i++) myCCanvController.drawXled(i, myDispSolvers.get(i).getMyColor());
                refreshLaySolvLayout();
                break;
        }
    }
    
    //refreshes all histograms in LAY/SOLV mode according to choiceBoxes
    private void refreshLaySolvLayout(){
        int cLix = cLLayCB.getSelectionModel().getSelectedIndex(),              //lays selection type
            cWix = cLWhatCB.getSelectionModel().getSelectedIndex();             //histogram num (in lay)
        List<DLNetworkedObject> myTrFIRSTSolvSubLayersList = myTrainer.getSolvers().get(0).getALLsubLayers(); //layers of any solver

        int rowNum = 0;
        for(int i=0; i<myTrFIRSTSolvSubLayersList.size(); i++){                                                     //for every i layer (of first solver)
            if(cLix==0 && myTrFIRSTSolvSubLayersList.get(i).getLayType()==DLlayType.FC ||                           //check i layer for selection type fitness
               cLix==1                                                              )                               //...other cases to put here
            {
                for(int j=0; j<myDispSolvers.size(); j++){                                                          //for every solver
                    List<DLNetworkedObject> myTrSolvSubLayersList = myDispSolvers.get(j).getALLsubLayers(); //layers list of given solver
                    Histogram myTrSolvSubLayHistogram = myTrSolvSubLayersList.get(i).getHistograms().get(cWix);     //histogram of i layer
                    myCCanvController.setHistogramObs(myTrSolvSubLayHistogram,j,rowNum);                            //put in j column and rowNum row
                }
                rowNum++;
            }
        }
        //clear rest of candles
        for(int i=rowNum; i<rowCNum; i++)                                                                           
            for(int j=0; j<colCNum; j++)
                myCCanvController.setHistogramObs(null,j,i);
    }
    //refreshes all histograms in SOLV/LAY mode according to all columns choiceBoxes
    private void refreshSolvLayLayout(){
        for(int nX=0; nX<colCNum; nX++) refreshSolvColumn(nX);
    }
    //refreshes single column of histograms in SOLV/LAY mode according to column choiceBoxes
    private void refreshSolvColumn(int colN){
        int cLix = cSLcbList.get(colN).getSelectionModel().getSelectedIndex(),  //lay num
            cWix = cSWcbList.get(colN).getSelectionModel().getSelectedIndex();  //histogram num (in lay)
       
        if(cLix>0){
            cSWcbList.get(colN).setDisable(false);
            for(int i=0; i<myDispSolvers.size(); i++){
                DLNetworkedObject myTrSolvSubLayer = myDispSolvers.get(i).getALLsubLayers().get(cLix-1);
                Histogram myTrSolvSubLayHistogram = myTrSolvSubLayer.getHistograms().get(cWix);
                myCCanvController.setHistogramObs(myTrSolvSubLayHistogram,colN,i);
            }
            for(int i=myDispSolvers.size(); i<rowCNum; i++)
                myCCanvController.setHistogramObs(null,colN,i);
        }
        else{
            cSWcbList.get(colN).setDisable(true);
            for(int i=0; i<rowCNum; i++)
                myCCanvController.setHistogramObs(null,colN,i);
        }
    }
}