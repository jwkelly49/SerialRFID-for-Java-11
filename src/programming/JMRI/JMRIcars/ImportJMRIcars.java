
package programming.JMRI.JMRIcars;

import databaseStructure.DatabaseHandler;
import java.io.File;
import java.util.List;
import javafx.scene.control.Alert;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/*
Pull cars from JMRI operations cars XML file
and place into JMRICars database table
*/

public class ImportJMRIcars {
     
    public static void importCars(String filePath) {
        String qu;
        DatabaseHandler database = new DatabaseHandler();
        try {
            
            //System.out.println("file path = " + filePath);
            File file = new File(filePath);
            JAXBContext jaxbContext = JAXBContext.newInstance(Operations.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Operations operations = (Operations) jaxbUnmarshaller.unmarshal(file);
            Cars data = operations.getAllCars(); 
            operations.setAllCars(data);
            List<Car> myCars = data.getCars();
            int count = 0;
            for (Car car : myCars) {
                String ID = car.getId();    
                String roadName = car.getRoadName();
                String roadNumber = car.getRoadNumber();
                String Type = car.getType();
                String Color = car.getColor();
                qu = "INSERT INTO JMRICars VALUES ( '" + ID + "','" + Color + "','" + roadName + "','" + roadNumber + "','" + Type +"')";
                System.out.println("qu = " + qu);
                database.execAction(qu);
                ++count;
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                     alert.setHeaderText(null);
                     alert.setContentText("Imported " + count + " cars into JMRI hold successfully");
                     alert.showAndWait();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

}// end class
