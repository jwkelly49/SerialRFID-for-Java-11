
#ifndef tagHeader
#include "runTag2.h"
#endif

/*
   Timing windows from the MFRC522 are so short that capturing the read and a quick error status
   prevents a constant looping of the tag. Leaving mfrc522.PICC_HaltA() disabled allows repeat
   reads to the same tag. Variable carPresent blocks loop from reading tag until a hardware switch
   is toggled.
*/

void updateTag() {
  int readCheck = 0;
  int getCheck = 0;


  bool error = false;



  error = showCardID();
  //error = true;                                             // ***** testing only
  if (!error) {
    error = authenication(_name);                             // sector 1
  }
  if (!error) {
    getCheck = writeBlock(_name);
    readCheck = readCheck + getCheck;
    getCheck = writeBlock(_number);
    readCheck = readCheck + getCheck;
    getCheck = writeBlock(_type);
    readCheck = readCheck + getCheck;
  }
  if (!error) {
    error = authenication(_color);                             // sector 2
  }
  if (!error) {
    getCheck = writeBlock(_color);
    readCheck = readCheck + getCheck;
    getCheck = writeBlock(_owner);
    readCheck = readCheck + getCheck; 
  }

  if (readCheck >= 5) {                                 // valid name/number/type/color/owner
    //xmitData("** all good write reads **");
    unsigned long uid = ((unsigned long)mfrc522.uid.uidByte[0] << 24UL) |
                        ((unsigned long)mfrc522.uid.uidByte[1] << 16UL) |
                        (mfrc522.uid.uidByte[2] << 8UL) |
                        mfrc522.uid.uidByte[3];
    carID = String(uid);
    String request = "*" + String(thisReader) + "02~" + carID + "\n";
    xmitData(request);
    displayLCD(5);                                       // display write complete
    carPresent = true;                                   // don't allow a new tag read
    displayLCD(10);                                      // display car data
     
  } else {
    //xmitData("*** sorry ***");
    carID = "000000";                                    // return bad RFID to server
    String request = "*" + String(thisReader) + "02~" + carID + "\n";
    xmitData(request);
    digitalWrite(redPin, HIGH);
    digitalWrite(greenPin, LOW);
    clearValues();                                       // fill data values with ?
    displayLCD(8);                                       // display write failure
    //delay(3000);
    carPresent = true;                                   // don't allow a new tag read
    badWrite = true;                                     // force a car release reset to clear display
    displayLCD(10);                                      // display ?????????
    
  }
  // Halt PICC
  // mfrc522.PICC_HaltA();                           // disabled allows continuous reading of the same tag
  // Stop encryption on PCD
  //mfrc522.PCD_StopCrypto1();
}

/*
   A failed write needs something to display to the user on the LCD display. I chose "?"
   hoping that it expresses the unknown state of the tag write operation.
*/
void clearValues() {
  for (int i = 0; i < 16; ++i) {              // all these variables have lengths of 17
    carRoadName[i] = '?';                    // this function ensures that each array
    carRoadNumber[i] = '?';                  // element has a printable character
    carType[i] = '?';                        // for the LCD display and ensures that
    carColor[i] = '?';                       // each variable is always NULL terminated.
    carOwner[i] = '?';
  }
  carRoadName[16] = '\0';
  carRoadNumber[16] = '\0';
  carType[16] = '\0';
  carColor[16] = '\0';
  carOwner[16] = '\0';
}
