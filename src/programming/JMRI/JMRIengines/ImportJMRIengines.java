
package programming.JMRI.JMRIengines;
import databaseStructure.DatabaseHandler;
import java.io.File;
import java.util.List;
import javafx.scene.control.Alert;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class ImportJMRIengines {

    public static void importEngines(String filePath) {
        String qu;
        DatabaseHandler database = new DatabaseHandler();
        try {
            System.out.println("file path = " + filePath);
            File file = new File(filePath);
            JAXBContext jaxbContext = JAXBContext.newInstance(Operations.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Operations operations = (Operations) jaxbUnmarshaller.unmarshal(file);
            Engines data = operations.getAllEngines();
            operations.setAllEngines(data);
            List<Engine> myEngines = data.getEngines();
            int count = 0;
            for (Engine engine : myEngines) {
                String ID = engine.getId();    
                String roadName = engine.getRoadName();
                String roadNumber = engine.getRoadNumber();
                String Type = engine.getType();
                String model = engine.getModel();
                qu = "INSERT INTO JMRIEngines VALUES ( '" + ID + "','" + roadName + "','" + roadNumber + "','" + Type + "','" + model +"')";
                System.out.println("qu = " + qu);
                database.execAction(qu);
                ++count;
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                     alert.setHeaderText(null);
                     alert.setContentText("Imported " + count + " engines into JMRI hold successfully");
                     alert.showAndWait();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
        
}// end class
