/*
 * 2017 (c) piteren
 */
package trainerVC;

import dataUtilities.GData;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.paint.Color;
import utilities.UColor;

/**
 * observes GData from trainer and solvers, noticed on change decides about / performs view update
 */
public class TVChartGDataController implements Observer {
    
    private final LineChart             myChart;                                //chart of this manager
    private final LinkedList<GData>     myObGDlist = new LinkedList();          //list of observable GData
    private final LinkedList<Integer>   numAlreadyDS = new LinkedList();        //list of sizes of already drawn series
    private final LinkedList<XYChart.Series<Integer,Double>>
                                        myDrawnSeries = new LinkedList(),       //list of data series actually drawn
                                        mySnapSeries = new LinkedList();        //list of snapData series
                                    
    private final boolean               snap;                                   //snapshot_when_reset_update flag
    
    //constructor
    TVChartGDataController(LineChart myCh, boolean sn){
        myChart = myCh;
        snap = sn;
        //create list of 10 series (snap & drawn) and add them to chart
        Platform.runLater(() -> {
            if(snap)
                for(int i=0; i<10; i++){
                    XYChart.Series<Integer,Double> newSer = new Series();
                    mySnapSeries.add( newSer );
                    myChart.getData().add( newSer );
                    newSer.nodeProperty().get().setStyle("-fx-stroke-width: 1px;"); 
                    newSer.nodeProperty().get().setStyle("-fx-stroke: "+UColor.RGBtoHEX( Color.LIGHTGRAY )+";");
                }
            for(int i=0; i<10; i++){
                XYChart.Series<Integer,Double> newSer = new Series();
                myDrawnSeries.add( newSer );
                myChart.getData().add( newSer );
                newSer.nodeProperty().get().setStyle("-fx-stroke-width: 1px;"); 
            }
        });
    }
    
    //sets new observables GData list, removes all previous
    public void setObservables(LinkedList<GData> newObs){
        removeAllObservables();
        
        for(GData gd: newObs){
            myObGDlist.add(gd);
            numAlreadyDS.add(0);
            gd.addObserver(this);
        } 
        //set new color of drawn series
        Platform.runLater(() -> {
            for(int i=0; i<myObGDlist.size(); i++)    
                myDrawnSeries.get(i).nodeProperty().get().setStyle("-fx-stroke: "+UColor.RGBtoHEX( myObGDlist.get(i).getColor() )+";");
        });  
        //draw added observables now
        for(int i=0; i<myObGDlist.size(); i++)
            addDataToChart(i, myObGDlist.get(i).getData());
    }
    public void setObservables(GData newObs){
        LinkedList<GData> obsDL = new LinkedList();
        obsDL.add(newObs);
        setObservables(obsDL);
    }
    //removes all observables and copies drawn to snap
    public void removeAllObservables(){
        if(!myObGDlist.isEmpty()){
            Platform.runLater(() -> {
                for(int i=0; i<10; i++){
                    if(snap){
                        mySnapSeries.get(i).getData().clear();
                        mySnapSeries.get(i).getData().addAll( myDrawnSeries.get(i).getData() );
                    }
                    myDrawnSeries.get(i).getData().clear();
                }
            });
            for(GData gd: myObGDlist) gd.deleteObserver(this);
            myObGDlist.clear();
            numAlreadyDS.clear();    
        }
    }
    
    //adds new data to given series
    private void addDataToChart( int obIX, LinkedList<Double> data){
        final XYChart.Series<Integer,Double> newSer = new Series();
        int startIX = numAlreadyDS.get(obIX);
        for(int i=0; i<data.size(); i++)
            newSer.getData().add( new XYChart.Data( startIX+i, data.get(i) ) );
        numAlreadyDS.set(obIX, startIX+data.size());
        
        Platform.runLater(() -> {
            myDrawnSeries.get(obIX).getData().addAll( newSer.getData() );
        });
    }
    
    @Override
    public void update(Observable obs, Object obj) {
        final int obIX = myObGDlist.indexOf(obs);
        
        if( ((LinkedList<Double>)obj).size()==0 ){
            numAlreadyDS.set(obIX, 0);
            Platform.runLater(() -> {
                if(snap){
                    mySnapSeries.get(obIX).getData().clear();
                    mySnapSeries.get(obIX).getData().addAll( myDrawnSeries.get(obIX).getData() );
                }
                myDrawnSeries.get(obIX).getData().clear();
            });
        }
        else addDataToChart(obIX, (LinkedList<Double>)obj );
    }
}