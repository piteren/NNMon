/*
 * 2017 (c) piteren
 */
package dataUtilities;

import java.util.List;
import javafx.scene.paint.Color;

/**
 * interface enabling histogram support for object
 */
public interface HistoFace {
    
    //initializes histograms functionality
    public void initHistograms();
    //returns list of object histograms
    public List<Histogram> getHistograms();
    //disables and deletes object histograms
    public void killHistograms();
}