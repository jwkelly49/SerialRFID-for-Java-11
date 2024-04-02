 

#ifndef tagHeader
#include "runTag2.h"
#endif



#define RST_PIN         9
#define SS_PIN          10
MFRC522 mfrc522(SS_PIN, RST_PIN);   // Create MFRC522 instance.
MFRC522::MIFARE_Key key;
MFRC522::StatusCode status;
LiquidCrystal_I2C lcd(0x27, 20, 4);

//*******************************************
// ******************************************
//   set this variable to the proper       **    Legal addresses are capitol A thru Y
//      address for this reader            **    Z is reserved for future global address
//                                         **
char thisReader = 'A';                   //**   name must be in single quotes
//                                         **
//*******************************************
//*******************************************


byte buffer[18];
byte size = sizeof(buffer);

int pendingRequest = 0;
bool pendingReply = false;
int retry = 0;

//    communications variables
char globalMessage[100];         // hold valid message
//int next = 0;                    // buffer index
//bool collect = false;            // Don't save buffer until a valid start of message and correct reader address
//bool validStart = false;
//bool validStop = false;
//bool testAdress = false;        // match with global thisReader

char carRoadName[17];
char carRoadNumber[17];
char carType[17];
char carColor[17];
char carOwner[17];

String carID;
String assignedEngine = "000000";   // default RFID of engine at startup of program
bool badWrite = false;


//bool error;

SoftwareSerial softSerial =  SoftwareSerial(rxPin, txPin);

//*************************** SETUP ********************************************************
void setup() {


  // local panel hardware
  pinMode(pickPin, INPUT_PULLUP);    // picked
  pinMode(dropPin, INPUT_PULLUP);    // drop
  pinMode(assignPin, INPUT_PULLUP);  // assign
  pinMode(releasePin, INPUT_PULLUP); // car release
  pinMode(redPin, OUTPUT);           // Red LED
  pinMode(greenPin, OUTPUT);         // Green LED
  pinMode(TXcntlPin, OUTPUT);        // RX/TX control

  pinMode(rxPin, INPUT);             // softSerial
  pinMode(txPin, OUTPUT);            // softSerial
  softSerial.begin(9600);

  //initialize lcd screen
  lcd.init();
  // turn on the backlight
  lcd.backlight();
  lcd.setCursor(0, 0);



  //Serial.begin(9600); // Initialize serial communications with the PC
  //while (!Serial);    // Do nothing if no serial port is opened (added for Arduinos based on ATMEGA32U4)
  SPI.begin();        // Init SPI bus
  mfrc522.PCD_Init(); // Init MFRC522 card
  mfrc522.PCD_SetAntennaGain( MFRC522::RxGain_max);

  // Prepare the key (using key A)
  // using FFFFFFFFFFFFh which is the default at chip delivery from the factory
  for (byte i = 0; i < 6; i++) {
    key.keyByte[i] = 0xFF;
  }

  //Serial.println(F("Scan a MIFARE Classic PICC to demonstrate read and write."));
  //Serial.print(F("Using key A:"));
  //dump_byte_array(key.keyByte, MFRC522::MF_KEY_SIZE);
  //Serial.println();
  //Serial.println(F("BEWARE: Data will be written to the PICC, in sector #1"));
}

//********************************* MAIN LOOP **********************************************

unsigned long tagTimer =  millis();
unsigned long loopTimer = millis();


int picked;                                            // panel switch
int drop;                                              // panel switch
int assign;                                            // panel switch
int carRelease;                                        // panel switch

int retryReset = 0;


void loop() {

  if (softSerial.available() > 0) {                    // Server is querying all readers
    //xmitData("Got serial port data");                // must filter for start byte(*) and
    validMessage();                                    // proper reader address
  }
  else {
    if (loopTimer - tagTimer > 5000) {
      if (pendingReply) {                              // emergency release of retry in the event
        ++retryReset;                                  // the server never returns a reply to a request
        //xmitData("retry loop");
        if (retryReset > 3) {
          retryReset = 0;
          pendingReply = false;
          pendingRequest = 0;
          displayLCD(7);
          delay(1500);
          displayLCD(10);
          //xmitData("retry loop released");
        }
      }
      if (!pendingReply and (pendingRequest == 0)) {   // don't want to miss comm reply
        //xmitData("looking for tag");
        if (!carPresent) {
          scanTag();                                   // check for new car on reader
        }
        if (carPresent) {
          toggle();                                    // toggle LCD display car info / owner
        }
      }
      tagTimer = loopTimer;                            // reset 10 second timer
    }   // end loop timer
  }

  if (!pendingReply and (pendingRequest == 0)) {       // reader not communicating with server
    picked = digitalRead(pickPin);                     // remove car from this location and add to engine
    drop = digitalRead(dropPin);                       // remove car from engine and add to this location
    assign = digitalRead(assignPin);                   // link engine at this location with the computer
    carRelease = digitalRead(releasePin);              // reset reader for next car 
    if (!picked && drop && assign && carRelease) {     // test that only one switch has been pressed
      //xmitData("got to button pick up");
      pickUp();                                        // test for low input (!)
    }
    if (!drop && picked && assign && carRelease) {
      //xmitData("got to button drop off");
      dropOff();
    }
    if (!assign && drop && picked && carRelease) {
      //xmitData("got to button assign");
      //delay(1000);
      assignIt();
    }
    if (!carRelease && drop && picked && assign) {
      releaseCar();
    }
  }
  loopTimer =  millis();
}  

//*****************************************************
//************  End of main loop  *********************
//*****************************************************
