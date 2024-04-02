
package locations;

import configuration.CommonVars;
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
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ListTracksThisLocationController implements Initializable {

    @FXML
    private TableView<Locals> tableView;
    @FXML
    private TableColumn<Locals,String> readerCol;
    @FXML
    private TableColumn<Locals,String> textCol;
    @FXML
    private Label noTracks;
    @FXML
    private Button BtnClose;
    @FXML
    private Label displayLocation;

    /**
     * Initializes the controller class.
     */
    
    DatabaseHandler database;
    ObservableList<ListTracksThisLocationController.Locals> list = FXCollections.observableArrayList();
    CommonVars common  = CommonVars.getInstance();
    String activeReader;
    String activeReaderText;
    boolean emptyResultSet;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        database = new DatabaseHandler();
        activeReader = common.getActiveReader();
        activeReaderText = common.getWorkLocationText().get();
        displayLocation.setText(activeReaderText);
        noTracks.setVisible(false);    // hide no tracks assigned label
        emptyResultSet = true;                   // assume no data in table
        initlCol();
         loadData();
    }    

    private void initlCol(){
        readerCol.setCellValueFactory(new PropertyValueFactory<>("reader"));
        textCol.setCellValueFactory(new PropertyValueFactory<>("text"));
    }  

    
    
     public static class Locals {
          private final SimpleStringProperty reader;
          private final SimpleStringProperty text;
    
         Locals (String reader, String text) {
         this.reader = new SimpleStringProperty(reader);
         this.text = new SimpleStringProperty(text);
     }

        public String getReader() {
            return reader.get();
        }
        public String getText() {
            return text.get();
        }
}
     
     private void loadData() { 
        String qu = "SELECT * FROM Tracks WHERE reader = '" + activeReader + "'";
        ResultSet rs = database.execQuery(qu);
             try {
                 while (rs.next()){
                     emptyResultSet = false;
                     noTracks.setVisible(false);
                     String reader = rs.getString("reader");
                     String text = rs.getString("text");
                     list.add(new ListTracksThisLocationController.Locals(reader, text));
                 }
             } catch (SQLException ex) {
                 Logger.getLogger(ListTracksThisLocationController.class.getName()).log(Level.SEVERE, null, ex);
             }
         tableView.getItems().setAll(list);
         if(emptyResultSet){
             noTracks.setVisible(true);
                //System.out.println("result set was empty");
         }
    }  
    
    @FXML
    private void BtnCloseAction(ActionEvent event) {
        Stage stage = (Stage) BtnClose.getScene().getWindow();
         stage.close();
    }
    @FXML
    private void tableViewMouseAction(MouseEvent event) {
        //System.out.println("got to mouse action");
        try{
        Locals item = tableView.getSelectionModel().getSelectedItem();
        String r = item.getReader();
        String t = item.getText();
        } catch (Exception e){
            System.out.println("something went wrong with TableViewMouseAction()");
            System.out.println(e);
        }
    }
    
}
