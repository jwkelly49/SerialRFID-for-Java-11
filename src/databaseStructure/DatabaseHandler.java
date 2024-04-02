
package databaseStructure;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;


public final class DatabaseHandler {
    private static DatabaseHandler databaseHandler;
    private static final String DB_URL = "jdbc:derby:database; create=true";
    private static Connection conn = null;
    private static Statement stmt = null;
    
   public DatabaseHandler() {
       createConnection();
   }
    
   public void dataBaseStartup(){
       setupStockTable();
       setupTracksTable() ;
       setupReadersTable();
       setupJMRICarsTable();
       setupJMRIEnginesTable();
       setupJMRILocationsTable();
       setupJMRITracksTable();
   }
   
    void createConnection() {
    try{
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            conn = DriverManager.getConnection(DB_URL);
         } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException e) {
             System.out.println("create connection failed " + e.getMessage());
            //e.printStackTrace();
         }
     }
    
    void setupStockTable() {
            /*
            Engines = individual rolling stock locomotives which are deletable.
            SerialRFID requires 1 permanent engine created here with the table
            with deletable set to false.
            */
        String TABLE_NAME = "Stock";
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null,null, TABLE_NAME.toUpperCase(),null );
            if(tables.next()) {
                  //System.out.println("Table " + TABLE_NAME +" already exists. Ready to go!");
            }else {
                   stmt.execute("CREATE TABLE "+ TABLE_NAME + "("
                           + "    RFID varchar(80) primary key, \n"
                           + "    color varchar(20) , \n"                             // holds model for n engine
                           + "    owner varchar(20) , \n"
                           + "    roadName varchar(20) , \n"
                           + "    roadNumber varchar(20) , \n"
                           + "    type varchar(20), \n"
                           + "    Reader varchar(2) default 'A' ,\n"             // assigned to a location
                           + "    ENGINE varchar(80) default null , \n"    // or assigned to an ENGINE
                           + "    deletable boolean default true, \n"            // to protect the permanent engine
                           + "    locomotive boolean default false"                    // real rolling stock engine
                           + ")" );
                   stmt.execute("INSERT INTO STOCK VALUES ( '0123456789','black','SerialRFID','Permanent','1234','Switcher','A',null,false,true)");
            }
        }
        catch (SQLException e) {
                 System.err.println(e.getMessage() + " ..... Engines Table error");
        }
        
    }
        
    void setupTracksTable() {
            /*
            Tracks = Names assigned to small sections of the layout. They are meaningful designators
            for portions of a layout used by humans. (i.e.) siding, business, track within a yard, etc.
           They don't have a physical reader and are only indirectly associated with a reader
            by being listed with a location display.
            SerialRFID requires 1 permanent Track setup here with the table.
            */
        String TABLE_NAME = "Tracks";
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null,null, TABLE_NAME.toUpperCase(),null );
            if(tables.next()) {
                  //System.out.println("Table " + TABLE_NAME +" already exists. Ready to go!");
            }else {
                   stmt.execute("CREATE TABLE "+ TABLE_NAME + "("
                           + "    text varchar(80) , \n"  // human name for a track
                           + "    reader varchar(20) , \n"
                           + "   deletable boolean default true"
                           + ")" );
                   stmt.execute("INSERT INTO TRACKS VALUES ( 'Programming Track','A',false)");
            }
        }
        catch (SQLException e) {
                 System.err.println(e.getMessage() + " ..... Tracks Table error");
        }
    }  

    void setupReadersTable() {
            /*
            Readers = Physical devicesused to read RFID tags
            SerialRFID requires 1 permanent Reader setup here with the table.
           Legal addresses are capitol A thru Y (Z - reserved) also setup here with the table.
            */
        String TABLE_NAME = "Readers";
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null,null, TABLE_NAME.toUpperCase(),null );
            if(tables.next()) {
                  //System.out.println("Table " + TABLE_NAME +" already exists. Ready to go!");
            }else {
                   stmt.execute("CREATE TABLE "+ TABLE_NAME + "("
                           + "    text varchar(80) , \n"     // human name for a reader (location)
                           + "    reader varchar(2) , \n"
                           + "   installed boolean default false, \n"
                           + "   uninstallable boolean default true"
                           + ")" );
                   stmt.execute("INSERT INTO READERS VALUES ( 'Programmer','A',true,false)");
                   stmt.execute("INSERT INTO READERS VALUES ( 'Reserved','Z',true,false)");
                   List<String> readers = Arrays.asList( "B", "C","D","E", "F", "G","H","I", "J", "K","L","M", "N", "O","P","Q", "U", "R","S","T", "U", "V","W","X", "Y");
                   for (String letter : readers) {
                        stmt.execute("INSERT INTO READERS VALUES ( '" + letter +"','" + letter + "',"+"false,true)"); 
                        //System.out.println("INSERT INTO READERS VALUES ( " + letter +"," + letter + ","+"false,true)");
                   }
            }
        }
        catch (SQLException e) {
                 System.err.println(e.getMessage() + " ..... Readers Table error");
        }
    }  
    
    public ResultSet execQuery(String query) {
        ResultSet result;
        try {
            stmt = conn.createStatement();
            result = stmt.executeQuery(query);
        }
        catch(SQLException ex) {
            System.out.println("Exception at execQuery: dataHandler" + ex.getLocalizedMessage());
            return null;
        }
        return result;
    }
    
    public boolean execAction(String qu) {
        try {
            stmt = conn.createStatement();
            stmt.execute(qu);
            return true;
        }
        catch(SQLException ex){
            JOptionPane.showMessageDialog(null, "Error " + ex.getMessage(), "Error Occured", JOptionPane.ERROR_MESSAGE);
            System.out.println("Exception at execQuery: dataHandler" + ex.getLocalizedMessage());
            return false;
        }
    }
    
        void setupJMRICarsTable() {
        String TABLE_NAME = "JMRICars";
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null,null, TABLE_NAME.toUpperCase(),null );
            if(tables.next()) {
                  //System.out.println("Table " + TABLE_NAME +" already exists. Ready to go!");
            }else {
                   stmt.execute("CREATE TABLE "+ TABLE_NAME + "("
                           + "    id varchar(20) , \n"
                           + "    color varchar(20) , \n"
                           + "    roadName varchar(80) , \n"
                           + "    roadNumber varchar(20) , \n"
                           + "    type varchar(20)"
                           + ")" );
                          // + "   isAvail boolean default true"
            }
        }
        catch (SQLException e) {
                 System.err.println(e.getMessage() + " ..... JMRICars Table error");
        }
        
    }
        
        void setupJMRIEnginesTable() {
        String TABLE_NAME = "JMRIEngines";
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null,null, TABLE_NAME.toUpperCase(),null );
            if(tables.next()) {
                  //System.out.println("Table " + TABLE_NAME +" already exists. Ready to go!");
            }else {
                   stmt.execute("CREATE TABLE "+ TABLE_NAME + "("
                           + "    id varchar(20) , \n"
                           + "    roadName varchar(80) , \n"
                           + "    roadNumber varchar(20) , \n"
                           + "    type varchar(20) , \n"
                           + "    model varchar(20)"
                           + ")" );
            }
        }
        catch (SQLException e) {
                 System.err.println(e.getMessage() + " ..... JMRIEngines Table error");
        }
        
    }    
    
        void setupJMRITracksTable() {
        String TABLE_NAME = "JMRITracks";
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null,null, TABLE_NAME.toUpperCase(),null );
            if(tables.next()) {
                  //System.out.println("Table " + TABLE_NAME +" already exists. Ready to go!");
            }else {
                   stmt.execute("CREATE TABLE "+ TABLE_NAME + "("
                           + "    name varchar(80) , \n"
                           + "    trackID varchar(20)"
                           + ")" );
            }
        }
        catch (SQLException e) {
                 System.err.println(e.getMessage() + " ..... JMRITracks Table error");
        }
    }
        
        void setupJMRILocationsTable() {
        String TABLE_NAME = "JMRILocations";
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null,null, TABLE_NAME.toUpperCase(),null );
            if(tables.next()) {
                  //System.out.println("Table " + TABLE_NAME +" already exists. Ready to go!");
            }else {
                   stmt.execute("CREATE TABLE "+ TABLE_NAME + "("
                           + "    name varchar(80) , \n"
                           + "    locID varchar(20)"
                           + ")" );
            }
        }
        catch (SQLException e) {
                 System.err.println(e.getMessage() + " ..... JMRILocations Table error");
        }
        
    }       
        
        
} // end of class


