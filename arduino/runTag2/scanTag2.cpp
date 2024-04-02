
#ifndef tagHeader
#include "runTag2.h"
#endif

byte _name = 4;
byte _number = 5;
byte _type = 6;
byte _color = 8;
byte _owner = 9;
byte _id = 10;
bool carPresent = false;
/*
   Timing windows from the MFRC522 are so short that capturing the read and a quick error status
   prevents a constant looping of the tag. Leaving mfrc522.PICC_HaltA() disabled allows repeat
   reads to the same tag. Variable carPresent blocks loop from reading tag until a hardware switch
   is toggled.
*/

void scanTag() {
  // Reset the loop if no new card present on the sensor/reader. This saves the entire process when idle.
  int readCheck = 0;
  int getCheck = 0;

  if ( ! mfrc522.PICC_IsNewCardPresent()) {
    //xmitData("IF is new");
    carPresent = false;
    //displayLCD(1);                                           // clear display
    return;
  } else {
    displayLCD(9);                                           // display new car detected
    //xmitData("ELSE is new");
  }

  // Select one of the cards
  if ( ! mfrc522.PICC_ReadCardSerial()) {
    return;
  } else {
    displayLCD(9);                                           // display new car detected
  }

  // Show some details of the PICC (that is: the tag/card)
  bool error = false;
  error = showCardID();
  error = authenication(_name);                                // sector 1
  if (!error) {
    getCheck = readBlock(_name);
    readCheck = readCheck + getCheck;
    getCheck = readBlock(_number);
    readCheck = readCheck + getCheck;
    getCheck = readBlock(_type);
    readCheck = readCheck + getCheck;
  }
  if (!error) {
    error = authenication(_color);                             // sector 2
  }
  if (!error) {
    getCheck = readBlock(_color);
    readCheck = readCheck + getCheck;
    getCheck = readBlock(_owner);
    readCheck = readCheck + getCheck;
  }

  if (readCheck >= 5) {                                // valid name/number/type/color/owner
    //xmitData("** all good reads **");
    carPresent = true;
    displayLCD(10);                                     // display car values
    unsigned long uid = ((unsigned long)mfrc522.uid.uidByte[0] << 24UL) |
                        ((unsigned long)mfrc522.uid.uidByte[1] << 16UL) |
                        (mfrc522.uid.uidByte[2] << 8UL) |
                        mfrc522.uid.uidByte[3];
    carID = String(uid);
    digitalWrite(greenPin, HIGH);
    //xmitData(carID);
  } else {
    //xmitData("*** sorry ***");
    displayLCD(12);                                     // display read failure
    carPresent = false;
    digitalWrite(greenPin, LOW);
    delay(1000);
    displayLCD(1);                                      // clear display
  }
  // Halt PICC
  // mfrc522.PICC_HaltA();                          // disabled allows continuous reading of the same tag
  // Stop encryption on PCD
  //mfrc522.PCD_StopCrypto1();
}
