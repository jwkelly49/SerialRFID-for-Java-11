
package programming;

import configuration.CommonVars;
import databaseStructure.DatabaseHandler;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


/**
 * FXML Controller class
 *
 * @author jwkel
 */
public class CarsController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private TextField TFcarRFID;
    @FXML
    private TextField TFcolor;
    @FXML
    private TextField TFowner;
    @FXML
    private TextField TFroadname;
    @FXML
    private TextField TFroadnumber;
    @FXML
    private TextField TFtype;
    @FXML
    private Button BtnToReader;
    @FXML
    private Button BtnCancel;
    @FXML
    private RadioButton carRadio;
    @FXML
    private ToggleGroup toggleGroup;
    @FXML
    private RadioButton engineRadio;


    /**
     * Initializes the controller class.
     */
    
    String whichTable = "Car";  // database default selection
    DatabaseHandler database;
    public static String tagTrigger = "idle";
    public static String duplicateTag="";
    public boolean addJMRI = false;
    CommonVars commonVars = CommonVars.getInstance();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
         database = new DatabaseHandler(); 
          int maxLength = 16;
         TFcolor.textProperty().addListener(new ChangeListener(TFcolor, maxLength));
         TFowner.textProperty().addListener(new ChangeListener(TFowner, maxLength));
         TFroadname.textProperty().addListener(new ChangeListener(TFroadname, maxLength));
         TFroadnumber.textProperty().addListener(new ChangeListener(TFroadnumber, maxLength));
         TFtype.textProperty().addListener(new ChangeListener(TFtype, maxLength));
    }  
    
    @FXML
    private void BtnnSendToReaderAction(ActionEvent event) {
        saveSelection();
    }

    @FXML
    private void BtnCancelAction(ActionEvent event) {
        Stage stage = (Stage) BtnCancel.getScene().getWindow();
         stage.close();
    }

    @FXML
    private void groupClickAction(MouseEvent event) {
        RadioButton which = (RadioButton) toggleGroup.getSelectedToggle();
        String which2 = which.getText();
        if("Car".equals(which2)){
            whichTable = which2;
        }else if("Engine".equals(which2)){
             whichTable = which2;
        } else {
             whichTable = "Car";  // make sure there is always a vaild database table
        }
        //System.out.println("Selected  " + whichTable);
    }
    
        private void saveSelection(){
         //System.out.println("got to saveSelection() ");
         
        String qu = "SELECT * FROM STOCK WHERE RFID = '111111'";
        //System.out.println("qu = " + qu);
        ResultSet rs = database.execQuery(qu);
           try {
                 while (rs.next()){
                     RemovePending();
                     Alert alert = new Alert(Alert.AlertType.INFORMATION);
                     alert.setHeaderText(null);
                     alert.setContentText("Removed orphaned new tag from database");
                     alert.showAndWait();
                 }
           }catch (Exception ex){
                    System.out.println("removed orphaned new tag from database ");
                 }     
         Date d1 = new Date();
         Date d2 = new Date();
         long milliseconds = 0;            
         String a = "111111";    // RFID value used to find new entry from commPort.Process2()
         String b = TFcolor.getText();
         String c = TFowner.getText();
         String d = TFroadname.getText();
         String e = TFroadnumber.getText();
         String f = TFtype.getText();
         String g = "A";      // always insert new car/engine to location A
         String h = null;      // assigned to an engine
         boolean i = true;   // deletable
         boolean j = false;  // locomotive
         if("Engine".equals(whichTable)){
           j = true;
        } 
         qu = "INSERT INTO Stock VALUES ( '" + a + "','" + b + "','" + c + "','" + d + "','" + e + "','" + f + "','" + g + "'," +h + "," + i + "," + j +")";
         //System.out.println("qu = " + qu);
         if (database.execAction(qu)) {
             tagTrigger = "transmit";
             Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
             alert1.setHeaderText(null);
             alert1.setContentText("Waiting for reader reply -- Please do not close");
             alert1.show();
             /*
             Wait no more than 3 seconds for the reader to reply
             */
             milliseconds = (d2.getTime()-d1.getTime());              
                 while (milliseconds <= 3000){
                        d2 = new Date();
                        milliseconds = (d2.getTime()-d1.getTime());
                        //System.out.println("loop milli = " + milliseconds);
                        if(!tagTrigger.equals("transmit")){
                              break;
                        }
                 }
            alert1.close();
         }
            //System.out.println("Cars tag trigger = " + tagTrigger);
        if ((milliseconds >= 3000) & tagTrigger.equals("transmit")) {
            tagTrigger = "timeout";
        }   
        switch (tagTrigger) {
            case "success":
                {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    tagTrigger = "idle";
                    commonVars.setSavedJMRI(true);  // used by AddJMRIcarsController  
                    alert.setHeaderText(null);
                    alert.setContentText("Tag completed successfully");
                    alert.showAndWait();
                    resetForm();
                    break;
                }
            case "failed":
                {
                    tagTrigger = "idle"; 
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText(null);
                    alert.setContentText("Either the Database saw :\n More than one piece of stock needing a new RFID tag (All these entries have been deleted) \nOr the reader failed to properly write to the new tag .... try again.");
                    alert.showAndWait();
                    RemovePending();
                    resetForm();
                    break;
                }
            case "prohibit":
                {
                    tagTrigger = "idle";
                    displayDuplicateTag();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText(null);
                    alert.setContentText("This RFID tag is already entered in the database.\nOr data from the reader was malformed.\n\nNOTE: This car tag has been modified.");
                    alert.showAndWait();
                    resetForm();
                    break;
                }
           case "trash":
                {
                    tagTrigger = "idle";
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText(null);
                    alert.setContentText("Data from reader was not formatted correctly .... try again.");
                    alert.showAndWait();
                    RemovePending();
                    resetForm();
                    break;
                }
           case "timeout":
                {
                    tagTrigger = "idle";
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText(null);
                    alert.setContentText("Write operation has timed out\n If it exists delete the database entry\nStatus of the tag is unknown\n .... try again.");
                    alert.showAndWait();
                    RemovePending();
                    resetForm();
 
                    break;
                }                
            default:
                Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText(null);
                    alert.setContentText("Something went wrong with tagTrigger switch.");
                    alert.showAndWait();
                break;
        }
        if(addJMRI){    // go back to AddJMRIcarsController via loadTextFieldData()
            System.out.println("got to close");
             BtnCancel.fire();
        }
 }
        
    private void RemovePending(){
        String qu;
              qu = "DELETE FROM Stock  WHERE RFID = '111111'" ;
              //System.out.println("qu = " + qu);
              if (database.execAction(qu)) {
                   Alert alert = new Alert(Alert.AlertType.INFORMATION);
                   alert.setHeaderText(null);
                   alert.setContentText("Delete of all pending new tags has succeeded");
                   alert.showAndWait();
             }else {
                   Alert alert = new Alert(Alert.AlertType.ERROR);
                   alert.setHeaderText(null);
                   alert.setContentText("Delete of pending new tags has failed");
                   alert.showAndWait();
             }
    }    
    private void resetForm(){
        TFcarRFID.setText("");
        TFcolor.setText("");
        TFowner.setText("");
        TFroadname.setText("");
        TFroadnumber.setText("");
        TFtype.setText("");
    }
    private void displayDuplicateTag(){
       String display =""; 
       String qu = "SELECT * FROM STOCK WHERE RFID = '"+ duplicateTag + "'";
        //System.out.println("qu = " + qu);
        ResultSet rs = database.execQuery(qu);
           try {
                 while (rs.next()){
                     String name1 = rs.getString("roadName");
                     String number1 = rs.getString("roadNumber");
                     String type1 = rs.getString("type");
                     String color1 = rs.getString("color");
                     String owner1 = rs.getString("owner");
                     String rfid1 = rs.getString("RFID");
//                     String ENGINE1 = rs.getString("ENGINE");
//                     boolean locomotive = rs.getBoolean("locomotive");  
                     display = "  "+name1+"\n "+number1+"\n "+type1+"\n "+color1+"\n "+owner1+" ";
                 }
                 Alert alert = new Alert(Alert.AlertType.ERROR);
                 alert.setHeaderText(null);
                 alert.setContentText("This RFID tag is already entered in the database.\n"+display+"\n\nNOTE: The tag data at the reader has been modified.");
                 alert.showAndWait();
           }catch (Exception e){
                         System.out.println("displayDuplicateTag() error  ");
            }
             
    }
    
/*
    loadTextFieldData called from AddJMRIcarsController and AddJMRIenginesController
    */
      public void loadTextFieldData(boolean car, String color, String name, String number, String type) throws IOException, InterruptedException {
//        System.out.println("shared Color = " + color );
//        System.out.println("shared roadName = " + name );
//        System.out.println("shared roadNumber = " + number );
//        System.out.println("shared Type = " + type );
        addJMRI = true;
        TFcolor.setText(color);
        TFowner.setText("????????????????");
        TFroadname.setText(name);
        TFroadnumber.setText(number);
        TFtype.setText(type);
        if(car){
            toggleGroup.selectToggle(carRadio);
            whichTable = "Car";
        }else{
            toggleGroup.selectToggle(engineRadio);
            whichTable = "Engine";
        }
//        System.out.println("car = " + car );
    }
}
