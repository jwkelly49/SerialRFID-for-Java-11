
package commPort;

import javafx.concurrent.Task;
import com.fazecast.jSerialComm.SerialPort;
import static com.fazecast.jSerialComm.SerialPort.TIMEOUT_NONBLOCKING;
import configuration.CommonVars;
import configuration.XML.Config;
import static configuration.XML.Config.configItems;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import databaseStructure.DatabaseHandler;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import programming.CarsController;

public class CommPort extends Task<Long> {
    public static SerialPort pollingPort;
    public static boolean goodPort = false;
    public static InputStream inputStream; 
    public static OutputStream outputStream; 
    public static String message;
    public static Charset charset = StandardCharsets.US_ASCII;
    public static byte[] inputBuffer = new byte[100];
    public static  char[] installedReaders = new char[26];
    DatabaseHandler database;
    CommonVars commonVars = CommonVars.getInstance();
/*
    Currently a known problem with the serial port is that at least Windows 11
    doesn't release the port when the program closes. My research reveals
    that this is usually caused by the USB to serial driver.
    */
    
     public CommPort () {
         String port = configItems.getComPort();
         //System.out.println("got to CommPort Function");
         pollingPort = SerialPort.getCommPort(port);
         pollingPort.openPort();
         if (!pollingPort.isOpen()){
             //System.out.println("Polling Port failed to open");
             pollingPort.closePort();
              pollingPort.closePort();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Serial port failed to open. ");
            alert.showAndWait();
            Config.objectToXML(); 
            configItems.OpenForm();
            System.exit(0);
         }else{
             //System.out.println("Polling Port is good to go!");
             goodPort = true;
             pollingPort.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
             inputStream = pollingPort.getInputStream();
             outputStream = pollingPort.getOutputStream();
             pollingPort.flushIOBuffers();
             pollingPort.setComPortTimeouts(TIMEOUT_NONBLOCKING, 0, 0);
         }
}

    @Override
    protected Long call() throws Exception {
         long sum = 0;
         //System.out.println("Got to task calling function");
        if (!goodPort){
            //System.out.println("Got to task with a bad port");
           return sum;
        }
        ThreadPrep();
        
        int x = 0;
        char notInstalled = '#';                                                // reader not installed
        String data;                                                        // which reader is being polled
        boolean response = false;                                  // good message structure
        int z;
        long milliseconds;
        long pollDelay;
        // meat and potatoes begins here
        try{
        while(true){
            if(Thread.currentThread().isInterrupted()) {
                //System.out.println("got interrupted : ");
                pollingPort.closePort();
                break;
            }
            if(!commonVars.getPollingMode()){
                continue;              // skip all polling actions
            }
            for (int i=0; i<26; ++i ) {                              // cycle through reader array
                char reading = installedReaders[i];
                if( notInstalled == reading){
                    continue;                                               // jump logic as this reader not installed
                }
                else{
                    pollDelay = 200;                                                              // anything less than 200 causes Arduino to reply twice for command 20/21/22
                    data = "*"+ String.valueOf(reading) + "01\n";              // normal query *A01\n
                    System.out.println("data sent : " + data + "   i = " + i);  // i of zero is reader A
                    
                    if((CarsController.tagTrigger.equals ("transmit")) & (i==0) ){         // write a new tag @ reader A
                        data = NewTag();                                                                            // roadname~roadNumber~type~color~owner
                        pollDelay = 200;                                                                              // reader returns "000000" a placeholder after a bad tag write
                        //System.out.println("tag data ready : ");                                          // a valid RFID number for a good write
                        //System.out.println("tag data = "+data);
                    }
                    if(((CarsController.tagTrigger.equals ("failed")) | (CarsController.tagTrigger.equals ("prohibit"))) & (i==0)  ){         // NewTag() failed to create proper data
                        data = "*"+ String.valueOf(reading) + "01\n";                            // reset for normal query *A01\n
                    }
                    writePort(data);                                                               // send query to a reader
                    /*
                    Millisecond timer required because I couldn't get JserialComm 
                    read port blocking to work.
                    */
                         Date d1 = new Date();
                         Date d2 = new Date();    
                         milliseconds = ((d2.getTime()) - (d1.getTime()));              
                         while (milliseconds <= pollDelay){
                                d2 = new Date();
                                milliseconds = (d2.getTime()-d1.getTime());
                         }
                         System.out.println("milliseconds = " + milliseconds );
                         milliseconds =0;
                          response = readPort();
                         
                          if(!response){
                                    System.out.println("NO REPLY  ");                                               // query reader 1 more time
                          }
                          else {
                                String replyWith = ProcessReply();
                                System.out.println("replyWith =  " + replyWith);
                                if(replyWith.equals("none")){
                                    int a =0; // do nothing
                                }else{
                                    writePort(replyWith);                // return True/false status to this reader request
                                }
                          }
                }
            } // end of for
         } // while true
   
        }
        catch (Exception e){
            System.out.println("something went wrong with the comm call loop ");
            System.out.println("Exception --> "+ e.getMessage());
        }
        return sum;
 }
    
    
 public boolean readPort() throws InterruptedException{
     //byte check;
     char check;
     boolean start = false;
     boolean result = false;
     for (int i = 0; i<inputBuffer.length; ++i ) {                                          
         try {
             //pollingPort.setComPortTimeouts(TIMEOUT_NONBLOCKING, 0, 0);
             pollingPort.readBytes(inputBuffer, 1);                   
         }
         catch (Exception e){
             System.out.println("trouble with input buffer ");
             break;
         }
           check = (char) inputBuffer[0];
           //check = inputBuffer[0];
           //System.out.println("check =  " + check);
           if (check =='*'){                                     // valid start to a message
                start = true;
                message ="";                                     
           }
           if(start) {
                message = message + check;          // capture message
                if (check == 10 ) {                          // valid end of message
                    System.out.println("message =  " + message);
                     result = true;
                     break;
                }     
           }
     }
     return result;
 }  // end readPort 
 
 public void writePort (String sending) {
     String data = sending;
     int q;
     byte[] myTest = data.getBytes(charset);
     q = myTest.length;
     //System.out.println("write message length = " + q);
     System.out.println("writeBytes sending : " + data);
     try{
          pollingPort.writeBytes(myTest, myTest.length);
     }
     catch (Exception e) {
         System.out.println("writePort failed ");
         System.out.println("Exception --> "+ e.getMessage());
     }

 }
 public void ThreadPrep(){
     database = new DatabaseHandler(); 
     for (int i=0; i<26; ++i) { 
            installedReaders[i] = '#';   // assume all readers not installed 
        } 
        String qu = "SELECT * FROM READERS WHERE installed = true";
        ResultSet rs = database.execQuery(qu);
             try {
                 int i =0;
                 char c;
                 while (rs.next()){
                     String reader = rs.getString("reader");
                     c = reader.charAt(0);
                     if(c != 'Z'){                             // Z is installed and undeletable but reserved for future use
                          installedReaders[i] = c;   // add the installed reader
                          //System.out.println("installed reader = " + installedReaders[i]);
                          ++i;
                     }
                 }
             } catch (SQLException ex) {
                 System.out.println("installed readers array failure");
                 Logger.getLogger(CommPort.class.getName()).log(Level.SEVERE, null, ex);
             }
 }
 public String ProcessReply(){
     //System.out.println("got to process reply");
     String trimmed = message.replaceAll("\\n", "");    // remove new line
     //System.out.println("process message =  " + trimmed);
     String command;
     String cmd;
     int cmd1;
     String reader;
     //String engine;
     //String car;
     String status = "none";
     String reply;
     
     try{
           String[] sections = trimmed.split("~");
//         sections will vary in element length depending on each command
//         sections[0] = reader + command
//         sections[1] = engine RFID
//         sections[2] = car RFID

            command = sections[0];                              // *A01~ = 01234
            reader = command.substring(1, 2);             // 1 = A reader
            //System.out.println("reader =  " + reader);
            cmd = command.substring(2, 4);                 // 23 = 01  commnd 1
            cmd1 = Integer.valueOf(cmd);
            //System.out.println("cmd =  " + cmd);
      
            switch(cmd1) {
                  case 1:
                       status = "none";                                         // 1 -- from a reader is a reply
                        break;                                                        // telling server it has no requests
                  case 2:                                                             // 2 -- program a new car reply
                       //System.out.println("got to case 2  ");
                      reply =Process2(sections[1]);                    // pass the new RFID number
                       status = reply;                                           // send no reply (always none) to reader
                       break;
                  case 20:                                                           // 20 -- is this an engine -- used by assign
                       reply =Process20(sections[1]);                  // RFID to compare
                       status = command+"~"+reply+"\n";          // True/False
                       break;
                  case 21:                                                            // drop this car at reader
                      //System.out.println("reader = " + reader);
                      //System.out.println("section 1 = " + sections[1]); 
                      //System.out.println("section 2 = " + sections[2]); 
                      reply =Process21(reader, sections[2]);       // addresss A-Y, car RFID
                       status = command+"~"+reply+"\n";           // True/false
                       break;
                  case 22:                                                             // attach this car to this engine
                       reply =Process22(sections[1], sections[2]);// engine RFID, car RFID
                       status = command+"~"+reply+"\n";           // True/False
                       break;
           }
     } catch (Exception e) {
           System.out.println("split didn't work");
           CarsController.tagTrigger = "trash";  // reader or database sent bad data
           Logger.getLogger(CommPort.class.getName()).log(Level.SEVERE, null, e);
     }
     return status;                                                                  // back to polling loop
 }
 public String Process20(String isEngine){
     // is this an engine -- used by assign
      database = new DatabaseHandler();
      String reply = "False";
        String qu = "SELECT * FROM Stock WHERE RFID = '" + isEngine + "'";
        //System.out.println("qu = " + qu);
        ResultSet rs = database.execQuery(qu);
         if (rs != null) {
             try {
                 while (rs.next()){
                     boolean check = rs.getBoolean("locomotive");
                     if(check){
                         reply = "True";
                         break;
                     }
                 }
             } catch (SQLException ex) {
                 System.out.println("Process20() failed");
             }
        }
     return reply;
 }
 public String Process21(String reader, String car){
     // drop this car at reader
     database = new DatabaseHandler();
      String reply = "False";
      String qu;
        qu = "UPDATE Stock SET ENGINE = " + null + ", Reader = '" + reader + "' WHERE RFID = '" + car + "'";
        //System.out.println("drop off qu = " + qu);
        try {
            if (database.execAction(qu)) {
                     reply = "True";
           }
        }catch(Exception ex){
                     System.out.println("drop off Process21() failed");
        }
     return reply;
 }
 public String Process22(String engine, String car){
     // attach this car to this engine
     database = new DatabaseHandler();
      String reply = "False";
      String qu;
       qu = "UPDATE Stock SET Reader = " + null + ", ENGINE = '" + engine + "' WHERE RFID = '" +car + "'";
       if (database.execAction(qu)) {
            reply = "True";
        }
     return reply;
 }
 public String Process2(String newRFID){
//     program a new car reply
//     tagTrigger = "transmit"  database form sending info to the reader
//     tagTrigger = "success"   reader sent good tag and database was updated
//     tagTrigger = "failed"      reader sent bad tag database was not updated
//     tagTrigger = "idle"         normal polling mode
//     tagTrigger = "prohibit"  prior entry in database or trash data
//     tagTrigger = "timeout"  waited 3 seconds for a reply
      String receivedTag = newRFID;
      //System.out.println("got to process2()  ");
     database = new DatabaseHandler();
     String reply = "none";
     boolean fail = false;
     //System.out.println("new RFID tag = "+receivedTag);
     if(receivedTag.equals("000000")){
         CarsController.tagTrigger = "failed";  // reader or database sent a failed write
         //System.out.println("bad process2 tag =  " + receivedTag);
         CarsController.tagTrigger = "failed";
         fail = true;
     }else{
         
        String qu = "SELECT * FROM STOCK WHERE RFID = '"+ receivedTag + "'";
        //System.out.println("qu = " + qu);
        ResultSet rs = database.execQuery(qu);
             try {
                 while (rs.next()){
                     CarsController.tagTrigger = "prohibit";
                     CarsController.duplicateTag = receivedTag;
                     //System.out.println("process 2 prohibited  ");
                     fail = true;
                 }
             }
             catch (Exception e){
                         System.out.println("present in database test error " + qu);
                         fail = true;     // protect database from trach entry
                         CarsController.tagTrigger = "failed";
                         System.out.println("process 2 exception  ");
                         }
        if(!fail){
           qu = "UPDATE Stock SET RFID = '" + receivedTag +"' WHERE RFID = '111111'";
           if (database.execAction(qu)) {
               CarsController.tagTrigger = "success"; 
               //System.out.println("processed a good tag  ");
           }else{
               CarsController.tagTrigger = "failed"; 
               //System.out.println("processed a bad tag  ");
           }
        }
     }  
     return reply;
 }

    private String NewTag() {
        database = new DatabaseHandler();
        String reply = "False";
        String data="";
        String color="";
        String owner="";
        String roadName="";
        String roadNumber="";
        String type="";
        String qu = "SELECT * FROM Stock WHERE RFID ='111111'";
        ResultSet rs = database.execQuery(qu);
             try {
                 while (rs.next()){
                     color = rs.getString("color");
                     owner = rs.getString("owner");
                     roadName = rs.getString("roadName");
                     roadNumber = rs.getString("roadNumber");
                     type = rs.getString("type");
                 }
                 data = "*A02~"+roadName+"~"+roadNumber+"~"+type+"~"+color+"~"+owner+"\n";
                 //System.out.println("****** data = "+data);
                 pollingPort.flushIOBuffers();
                 //CarsController.tagTrigger = "success";
                 return data;
             } catch (Exception e) {
                     System.out.println("new tag failed");
                     CarsController.tagTrigger = "failed";;
             }
             CarsController.tagTrigger = "failed";
             return "000000";
    }
}  // end class 

