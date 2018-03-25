/*
 * 2017 (c) piteren
 */
//implement file chooser window
package trainerVC;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
        appStarter.launch(args);
    }
}