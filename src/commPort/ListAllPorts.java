
package commPort;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ListAllPorts {
    public void ListAllPorts(){
        String PortName;
        try {
               Parent root = FXMLLoader.load(getClass().getResource("PortViewer.fxml"));
               Stage stage = new Stage();
               Scene scene = new Scene(root);
               stage.setScene(scene);
               stage.setResizable(false);
               stage.showAndWait();
              }catch (Exception e) {
                
              }
    }
} //end class
