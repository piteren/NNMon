/*
 * 2017 (c) piteren
 */
package trainerVC;

import dataUtilities.Histogram;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import diffUtils.ULogDoubleProperty;
import diffUtils.ULogDoubleProperty.TSform;

/**
 * controls candle canvas, displays candle graphs of observed histograms
 */
public class TVCandleCanvasController implements Observer {
    
    private final Canvas            myCanvas;
    private final GraphicsContext   gc;

    private final List<Histogram>   myHObs = new LinkedList();                  //list of observed histograms, null means no histogram at given position
    protected ULogDoubleProperty    rangeCandle = new ULogDoubleProperty(TSform.DBL,0);    //candles display range
    
    private static final double     C_WIDTH     = 90,                           //candle graph width (pixels)
                                    C_HEIGHT    = 20,                           //candle graph height (pixels)
                                    C_SPACER    = 5,                            //space between graphs (pixels)   
                                    B_MRG       = 5,                            //background width margin
                                    ALLC_OFFX   = 20,                           //all candles offset at X axis (pixels)
                                    ALLC_OFFY   = 15;                           //all candles offset at Y axis (pixels)
                                    
    private static final Color      C_C_BKG     = Color.LIGHTGRAY,              //candle background color
                                    C_C_YAX     = Color.WHITE,                  //candle y axis color
                                    C_C_MM      = Color.DARKRED,                //min max color
                                    C_C_SD      = Color.rgb(65,105,245,0.7),    //deviation color Color.ROYALBLUE with 0.7trans
                                    C_C_AV      = Color.DARKORANGE;             //mean color
                                         
    private final int               colCNum,                                    //number of columns at canvas
                                    rowCNum;                                    //number of rows at canvas
    
    //constructor
    TVCandleCanvasController(Canvas myC, int cCNum, int rCNum){
        myCanvas = myC;
        colCNum = cCNum;
        rowCNum = rCNum;
        gc = myCanvas.getGraphicsContext2D();

        //set nulls for list of histograms
        for(int i=0; i<colCNum*rowCNum; i++) myHObs.add(null);
        //prepare background
        Platform.runLater(() -> {
            for(int nX=0; nX<colCNum; nX++)
                for(int nY=0; nY<rowCNum; nY++)
                    drawHBkg(nX,nY);
        });
    }
    
    //sets new histogram observable at given postition, accepts null as histogram (>> stops current at given pos)
    public void setHistogramObs(  Histogram newHO, int nX, int nY){
        int obsNum = nY+nX*rowCNum;
        if(myHObs.get(obsNum)!=null){
            myHObs.get(obsNum).stop(this);
            drawHBkg(nX,nY);
        }
        
        myHObs.set(obsNum, newHO);
        if(newHO!=null) newHO.start(this);
    }
   
    //draws X led for solvers
    protected void drawXled(int nX, Color c){
        final double px = ALLC_OFFX+nX*(C_WIDTH+C_SPACER+2*B_MRG);
        final double py = 5;
        Platform.runLater(() -> {
            if(c==null) gc.setFill(Color.WHITE);
            else gc.setFill(c);
            gc.fillRect(px,py,C_WIDTH-15,5);
        });
    } 
    //draws Y led for solvers
    protected void drawYled(int nY, Color c){
        final double px = 5;
        final double py = ALLC_OFFY+nY*(C_HEIGHT+C_SPACER);
        Platform.runLater(() -> {
            if(c==null) gc.setFill(Color.WHITE);
            else gc.setFill(c);
            gc.fillRect(px,py,5,C_HEIGHT);
        });
    } 
    
    //draws candle background at given position
    private void drawHBkg(int nX, int nY){
        final double px = ALLC_OFFX+nX*(C_WIDTH+C_SPACER+2*B_MRG);
        final double py = ALLC_OFFY+nY*(C_HEIGHT+C_SPACER);
        final double yz = py+C_HEIGHT/2;
        Platform.runLater(() -> {
            gc.setLineWidth(1);
            gc.setStroke(C_C_BKG);
            gc.strokeLine(px+C_WIDTH/2,py-C_SPACER/2,px+C_WIDTH/2,py+C_HEIGHT+C_SPACER/2);
            gc.setFill(C_C_BKG);
            gc.fillRect(px-B_MRG,py,C_WIDTH+2*B_MRG,C_HEIGHT);

            gc.setStroke(C_C_YAX);
            gc.strokeLine(px,yz,px+C_WIDTH,yz);
        });
    }
    
    //draws candle tick line
    private void drawTickLine(  int nX,
                                int nY,     
                                double dOT,             //line data point
                                final double dotW,      //line width (pixels)
                                final double height,    //line height (pixels)
                                final Color c){   
        double rRad = rangeCandle.getLinDoubleValue();
        final double px = ALLC_OFFX+nX*(C_WIDTH+C_SPACER+2*B_MRG);
        final double yz = ALLC_OFFY+nY*(C_HEIGHT+C_SPACER)+C_HEIGHT/2;
        double tempX;
        
        tempX  = px+(dOT+rRad)/(2*rRad)*C_WIDTH;
        if(tempX < px) tempX=px;
        if(tempX > px+C_WIDTH) tempX=px+C_WIDTH;
        final double xin = tempX;
        
        Platform.runLater(() -> {
            gc.setStroke(c);
            gc.setLineWidth(dotW);
            gc.strokeLine(xin,yz-height/2,xin,yz+height/2);
        });
    }
    
    //draws candle base line
    private void drawHLine(     int nX,
                                int nY,
                                double dIN,         //line data in-point
                                double dOUT,        //line data out-point
                                double height,      //height of line (pixels)
                                Color c){   
        double rRad = rangeCandle.getLinDoubleValue();
        final double px = ALLC_OFFX+nX*(C_WIDTH+C_SPACER+2*B_MRG);
        final double yz = ALLC_OFFY+nY*(C_HEIGHT+C_SPACER)+C_HEIGHT/2;
        double tempX;
        
        tempX  = px+(dIN+rRad)/(2*rRad)*C_WIDTH;
        if(tempX < px) tempX=px;
        if(tempX > px+C_WIDTH) tempX=px+C_WIDTH;
        final double xin = tempX;
        
        tempX = px+(dOUT+rRad)/(2*rRad)*C_WIDTH;
        if(tempX < px) tempX=px;
        if(tempX > px+C_WIDTH) tempX=px+C_WIDTH;
        final double xout = tempX;
        
        Platform.runLater(() -> {
            gc.setStroke(c);
            gc.setLineWidth(height);
            gc.strokeLine(xin,yz,xout,yz);
        });
    }
    
    //draws candle for histogram
    private void drawHCand( int nX,         //x position (column) 
                            int nY,         //y position (row) 
                            Histogram his)  //histogram data
    {        
        
        drawHBkg(       nX,nY);
        drawHLine(      nX,nY, his.getdMin(),                  his.getdMax(),                  4,  C_C_MM);
        drawHLine(      nX,nY, his.getdMean()-his.getdSDev(),  his.getdMean()+his.getdSDev(),  8,  C_C_SD);
        drawTickLine(   nX,nY,             his.getdMean(),4,                                   10, C_C_AV);
    }
   
    @Override
    public void update(Observable obs, Object arg) {
        int nHis = myHObs.indexOf(obs);
        int nX = nHis/rowCNum;
        int nY = (nHis-nX*rowCNum)%rowCNum; 
        Histogram hCopy = myHObs.get(nHis).copy();

        drawHCand(nX, nY, hCopy);
    }
}