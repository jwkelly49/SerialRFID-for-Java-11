
package configuration;

import com.fazecast.jSerialComm.SerialPort;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import static configuration.XML.Config.configItems;

public class PortViewerController implements Initializable {

    @FXML
    private Button BtnSave;
    @FXML
    private Button BtnCancel;
    @FXML
    private TextField TFselectedPort;
    @FXML
    private ListView<String> Listbox;

    /**
     * Initializes the controller class.
     */


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        PortList();
    }    

    @FXML
    private void BtnSaveAction(ActionEvent event) {
         configItems.setComPort(TFselectedPort.getText());
         Stage stage = (Stage) BtnSave.getScene().getWindow();
         stage.close();
    }

    @FXML
    private void BtnCancelAction(ActionEvent event) {
         Stage stage = (Stage) BtnCancel.getScene().getWindow();
         stage.close();
    }
    
   @FXML
    public void ListboxMouseClick(MouseEvent event) {
        //System.out.println("got to list box action ");
        String item;
        item = Listbox.getSelectionModel().getSelectedItem();
        TFselectedPort.setText(item);
        //System.out.println("item = "+item);
       
    }
    
    public void PortList() {
        SerialPort[] allAvailableComPorts = SerialPort.getCommPorts();
         List<String> names = new ArrayList<>();
        for(SerialPort eachComPort:allAvailableComPorts){
            //System.out.println("List of all available serial ports: " + eachComPort.getSystemPortName());  
            names.add(eachComPort.getSystemPortName());
        }
        Listbox.getItems().addAll(names);
    }
}


