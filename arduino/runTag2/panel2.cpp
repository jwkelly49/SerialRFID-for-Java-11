#ifndef tagHeader
#include "runTag2.h"
#endif
/*

*/

//***************  pick up ************************
/*
   Tell the server to transfer the database record for this RFID tag from this
   location (reader) to the assigned engine.
*/
void pickUp() {
  if (assignedEngine == "000000") {
    //xmitData("must assign engine before pickup");
    displayLCD(6);
    if (carPresent) {
      displayLCD(10);
    } else {
      displayLCD(1);
    }
  } else {
    pendingRequest = 22;
    pendingReply = true;
    displayLCD(13);
  }
}

//****************** drop off **********************
/*
   Tell the server to transfer the database record for this RFID tag from the
   engine to this location (reader.)
*/
void dropOff() {
  if (assignedEngine == "000000") {     // power up default to test against
    displayLCD(6);                    // must assign engine
    if (carPresent) {
      displayLCD(10);               // car information
    } else {
      displayLCD(1);                // clear screen
    }
  } else {
    pendingRequest = 21;              // what to send to server
    pendingReply = true;
    displayLCD(13);                   // pending request
  }
}

//******************* assign it *********************
/*
   Tell the server to link the RFID tag of this engine to
   this location (reader.) This allows movement of rolling stock
   between locations (readers.)
*/
void assignIt() {
  //  xmitData("got to assign method");
  pendingRequest = 20;
  pendingReply = true;
  displayLCD(13);
}

//******************* car release *********************
void releaseCar(){
        carPresent = false;
      mfrc522.PCD_StopCrypto1();                // must stop or next tag will never read
      digitalWrite(redPin, LOW);
      digitalWrite(greenPin, LOW);
      for (int i = 0; i < 3; ++i) {
        displayLCD(15);
        badWrite = false;
        delay(1500);
        displayLCD(1);
        delay(500);
      }
}
/*
   Tell the nano to clear the block on the main loop scanning
   for new tags (cars) and rests related variables
*/
