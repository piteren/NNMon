/*
 * 2017 (c) piteren
 */
//implement file chooser window
package trainerVC;

import java.io.File;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import rowDM.RowDMActor;
import rowDM.RowDMCase;
import rowDM.RowDMCharDataCreator;

public class appStarter extends Application {
    
    TVMainViewController myController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("TVMainView.fxml"));
        
        primaryStage.setScene(new Scene(loader.load(), 1400, 690));
        myController = loader.getController();
        primaryStage.setResizable(false);
        primaryStage.setTitle("nnT");
        primaryStage.show();
        
        /*
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Net File");
        File fileR = new File("./NETs/");
        fileChooser.setInitialDirectory( fileR ); 
        fileChooser.getExtensionFilters().add( new FileChooser.ExtensionFilter("nets txt", "*.txt") );
        fileChooser.showOpenDialog(primaryStage);
        */
    }    
    
    @Override
    public void stop() throws InterruptedException{
        myController.stop();
    }
    
    public static void main(String[] args) {

        // RowDMCharDataCreator test = new RowDMCharDataCreator("RDMdata/rowData.txt");

        /*
        RowDMCase casTest = new RowDMCase("RDMdata/rowData.txt");
        RowDMActor actTest = new RowDMActor(casTest);
        for(int i=0; i<100; i++){
            int dec = actTest.makeDecision(casTest.prepCurrentState());
            actTest.takeFeedback(casTest.prepFeedback(dec));
            casTest.moveToNextRow();
        }
        */

        appStarter.launch(args);
    }
}