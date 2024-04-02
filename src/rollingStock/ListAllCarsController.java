
package rollingStock;

import databaseStructure.DatabaseHandler;
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
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;




public class ListAllCarsController implements Initializable {

    @FXML
    private TableView<Locals> tableView;
    @FXML
    private TableColumn<Locals, String> roadName;
    @FXML
    private TableColumn<Locals, String> roadNumber;
    @FXML
    private TableColumn<Locals, String> type;
    @FXML
    private TableColumn<Locals, String> color;
    @FXML
    private TableColumn<Locals, String> owner;
    @FXML
    private Button close;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Label emptyInventory;
    
    /**
     * Initializes the controller class.
     */
    
    DatabaseHandler database;
    ObservableList<ListAllCarsController.Locals> list = FXCollections.observableArrayList();
    boolean emptyResultSet;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        database = new DatabaseHandler();
        emptyInventory.setVisible(false);    // hide no cars assigned label
        emptyResultSet = true;                   // assume no data in table
         loadData();
         initlCol();
    }    

    @FXML
    private void BtnClose(ActionEvent event) {
        Stage stage = (Stage) close.getScene().getWindow();
         stage.close();
    }
    
        private void initlCol(){
        roadName.setCellValueFactory(new PropertyValueFactory<>("roadName"));
        roadNumber.setCellValueFactory(new PropertyValueFactory<>("roadNumber"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        color.setCellValueFactory(new PropertyValueFactory<>("color"));
        owner.setCellValueFactory(new PropertyValueFactory<>("owner"));
    }   
    
        public static class Locals {
          private final SimpleStringProperty roadName;
          private final SimpleStringProperty roadNumber;
          private final SimpleStringProperty type;
          private final SimpleStringProperty color;
          private final SimpleStringProperty owner;
    
         Locals (String roadName1, String roadNumber1, String type1, String color1, String owner1) {
         this.roadName = new SimpleStringProperty(roadName1);
         this.roadNumber = new SimpleStringProperty(roadNumber1);
         this.type = new SimpleStringProperty(type1);
         this.color = new SimpleStringProperty(color1);
         this.owner = new SimpleStringProperty(owner1);
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
        public String getOwner() {
            return owner.get();
        }
}
        
    private void loadData() {
        String qu = "SELECT * FROM Stock WHERE locomotive = 'false'";
        ResultSet rs = database.execQuery(qu);
         if (rs != null) {
             try {
                 while (rs.next()){
                     emptyResultSet = false;
                     emptyInventory.setVisible(false);
                     String color1 = rs.getString("color");
                     String owner1 = rs.getString("owner");
                     String roadName1 = rs.getString("roadName");
                     String roadNumber1 = rs.getString("roadNumber");
                     String type1 = rs.getString("type");
                     list.add(new ListAllCarsController.Locals(roadName1, roadNumber1, type1, color1, owner1) );
                 }
             } catch (SQLException ex) {
                 Logger.getLogger(ListAllCarsController.class.getName()).log(Level.SEVERE, null, ex);
             }
        }
         tableView.getItems().setAll(list); 
         if(emptyResultSet){
             emptyInventory.setVisible(true);
                System.out.println("result set was empty");
         }
    }        
        
}  // end of controller
