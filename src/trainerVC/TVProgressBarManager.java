/*
 * 2017 (c) piteren
 */
package trainerVC;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressBar;

/**
 * manager of progressBar
 */
public class TVProgressBarManager implements ChangeListener {
    
    private final ProgressBar myProgressBar;
    private ObservableNumberValue myObsNVal;
    private double myBound = 1;

    //constructor(progressBar)
    TVProgressBarManager(ProgressBar pgB, ObservableNumberValue oNV){
        myProgressBar = pgB;
        addObservalble(oNV);
    }

    //adds observable value
    private void addObservalble(ObservableNumberValue oNV){
        myObsNVal = oNV;
        myObsNVal.addListener(this);
    }
    
    //unregisters manager (btw. there is no way to register again...)
    public void unregister(){
        myObsNVal.removeListener(this);
        myObsNVal = null;
    }

    //sets upper bound of manager
    public void setMyBound(double nB){
        myBound = nB;
    }

    @Override
    public void changed(ObservableValue ov, Object t, Object t1) {
        myProgressBar.progressProperty().setValue( ((Number)t1).doubleValue()/myBound );
    }
}