
package programming.JMRI.JMRIcars;

import configuration.CommonVars;
import databaseStructure.DatabaseHandler;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import programming.CarsController;



/**
 display to JMRI car hold scene
 and allow cars to be deleted from JMRICars table
 before they are imported into SerialRFID stock table
 */
public class AddJMRIcarsController implements Initializable {

    @FXML
    private Button cancel;
    @FXML
    private TableView<Locals> tableView;
    @FXML
    private TableColumn<Locals, String> roadCol;
    @FXML
    private TableColumn<Locals, String> numberCol;
    @FXML
    private TableColumn<Locals, String> typeCol;
    @FXML
    private TableColumn<Locals, String> colorCol;
    @FXML
    private TableColumn<Locals, String> rfidCol;
    @FXML
    private Label emptyInventory;
    @FXML
    private Button add;
     
    /**
     * Initializes the controller class.
     */
    DatabaseHandler database;
    ObservableList<AddJMRIcarsController.Locals> list = FXCollections.observableArrayList();
    String deleteRecord;
    String rfid = "111111";
    String color;
    String name;
    String number;
    String type;
    boolean emptyResultSet;
    CommonVars commonVars = CommonVars.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        database = new DatabaseHandler();
        emptyInventory.setVisible(false);    // hide no cars assigned label
        emptyResultSet = true;                   // assume no data in table
         loadData();
         initlCol();
         selectTableViewItem();
    }    

    private void initlCol(){
        roadCol.setCellValueFactory(new PropertyValueFactory<>("roadName"));
        numberCol.setCellValueFactory(new PropertyValueFactory<>("roadNumber"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));
        rfidCol.setCellValueFactory(new PropertyValueFactory<>("rfid"));  //carRFID
    }

    public static class Locals {
          private final SimpleStringProperty roadName;
          private final SimpleStringProperty roadNumber;
          private final SimpleStringProperty type;
          private final SimpleStringProperty color;
          private final SimpleStringProperty rfid;
    
         Locals (String roadName1, String roadNumber1, String type1, String color1, String rfid1) {
         this.roadName = new SimpleStringProperty(roadName1);
         this.roadNumber = new SimpleStringProperty(roadNumber1);
         this.type = new SimpleStringProperty(type1);
         this.color = new SimpleStringProperty(color1);
         this.rfid = new SimpleStringProperty(rfid1);
     }

        public String getRoadName() {
            return roadName.get();
        }
        public String getRoadNumber() {
            return roadNumber.get();
        }
        public String getType() {
            return type.get();
        }
        public String getColor() {
            return color.get();
        }
        public String getRFID() {
            return rfid.get();
        }
}
    
    private void loadData() {
        list.removeAll(list);
        String qu = "SELECT * FROM JMRICars";
        ResultSet rs = database.execQuery(qu);
         if (rs != null) {
             try {
                 while (rs.next()){
                     emptyResultSet = false;
                     emptyInventory.setVisible(false);
                     //System.out.println("doing while loop");
                     String rfid1 = rs.getString("id");
                     String color1 = rs.getString("color");
                     String roadName1 = rs.getString("roadName");
                     String roadNumber1 = rs.getString("roadNumber");
                     String type1 = rs.getString("type");
                     list.add(new AddJMRIcarsController.Locals(roadName1, roadNumber1, type1, color1, rfid1) );
                 }
             } catch (SQLException ex) {
                 Logger.getLogger(AddJMRIcarsController.class.getName()).log(Level.SEVERE, null, ex);
             }
        }
         tableView.getItems().setAll(list); 
         if(emptyResultSet){
             emptyInventory.setVisible(true);
                System.out.println("result set was empty");
         }
    }  
    
    
    @FXML
    private void BtnCancel(ActionEvent event) {
        Stage stage = (Stage) cancel.getScene().getWindow();
         stage.close();
    }

    @FXML
    private void tableViewMouseAction(MouseEvent event) {
        selectTableViewItem();
    }
    
    private void selectTableViewItem(){
        tableView.getSelectionModel().selectFirst();
        tableView.getSelectionModel().getSelectedItem();
        try{
        AddJMRIcarsController.Locals item = tableView.getSelectionModel().getSelectedItem();
        deleteRecord = item.getRFID();   //  hold selected record for delete action
        /*
        global variables color --> type hold values to be shared with CarsController
        if import button is clicked  via BtnAdd(ActionEvent event)
        */
        color = item.getColor();
        name = item.getRoadName();
        number = item.getRoadNumber();
        type = item.getType();
        
        } catch (Exception e){
            System.out.println("something went wrong with TableViewMouseAction()");
            System.out.println(e);
        }               
    }   
    
    public void deleteFromHold(){
         //System.out.println("got to delete from hold");
         if (deleteRecord == null){
            return;
        }
        String qu;
              qu = "DELETE FROM JMRICars  WHERE id = '" + deleteRecord + "'" ;
              //System.out.println("qu = " + qu);
              if (database.execAction(qu)) {
                   emptyResultSet = true;
                   emptyInventory.setVisible(false);
                   loadData();
                   Alert alert = new Alert(Alert.AlertType.INFORMATION);
                   alert.setHeaderText(null);
                   alert.setContentText("Success");
                   alert.showAndWait();
             }else {
                   Alert alert = new Alert(Alert.AlertType.ERROR);
                   alert.setHeaderText(null);
                   alert.setContentText("Failed");
                   alert.showAndWait();
             }
    }
    @FXML
    private void BtnAdd(ActionEvent event) throws InterruptedException {
//        System.out.println("RFID = " + rfid );    // used locally -- not shared
//        System.out.println("Color = " + color );
//        System.out.println("roadName = " + name );
//        System.out.println("roadNumber = " + number );
//        System.out.println("Type = " + type );
        openCarsController();
               
    }
    /*
    opens CarsController passes car values to scene. allows CarsController
    to process the new tag into SerialRFID stock database. CarsController sets 
    commonVars with good/bad tag write. A good commonVars deletes 
    car from JMRIhold database.
    */
     @FXML
    private void openCarsController() throws InterruptedException {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                   alert.setHeaderText(null);
                   alert.setContentText("Be sure to provide the (human) owner's name.\n Data entries may also be modified before sending to Reader A");
                   alert.showAndWait();
        try {
            // Load the FXML file for CarsController
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/programming/cars.fxml"));
            Parent root2 = loader.load();
            // Get the controller of CarsController
            CarsController carsControl = loader.getController();
            // set ture for car false for engine
            carsControl.loadTextFieldData(true, color, name, number, type);  
            Scene scene = new Scene(root2);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();
            if(commonVars.getSavedJMRI()){
                System.out.println("got a good return");
                deleteFromHold();
                loadData();       // reload the table view
                commonVars.setSavedJMRI(false);   // reset so CarsController must be called again if needed
            }else{
                System.out.println("got a bad return");
            }
            
        } catch (IOException ex) {
            Logger.getLogger(AddJMRIcarsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }   
}
