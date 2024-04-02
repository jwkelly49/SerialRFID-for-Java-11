package main;


import databaseStructure.DatabaseHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import commPort.CommPort;
import static commPort.CommPort.pollingPort;
import configuration.XML.Config;
import static configuration.XML.Config.configItems;
import java.io.*;
import java.io.File;
//import programming.CarsController;


public class SerialRFID extends Application {

    main.controllers.SerialRFIDController controller;
    @Override
    public void start(Stage stage) throws Exception {
     
      DatabaseHandler dataBase = new DatabaseHandler();  
      dataBase.dataBaseStartup();
      File f = new File("configValues.xml");
      if (f.exists ()) {
           Config.xmlToObject("configValues.xml");
      }else{
           Config.objectToXML(); 
            configItems.OpenForm();
           System.exit(0);      // force restart after config change
      }
        

      FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/fxml/SerialRFID.fxml"));
      Parent root = loader.load();
      Scene scene = new Scene(root);
      stage.setScene(scene);
      stage.setResizable(false);
      stage.show();

      CommPort task = new CommPort();
      Thread bgThread = new Thread(task);
      bgThread.setDaemon(true);
      bgThread.start();
      Runtime.getRuntime().addShutdownHook(new Thread(() ->{
          bgThread.interrupt();
        try{
            bgThread.join();
        }  catch(InterruptedException e){
              e.printStackTrace();
        }
        
        if(pollingPort.isOpen()){
            pollingPort.closePort();
            //System.out.println("Serial port Closed");
        }
    }));
  }
    

    public static void main(String[] args)  throws IOException{
      
        launch(args);
    }
    

} // end of class
