#ifndef tagHeader
#include "runTag2.h"
#endif


// *********************** validate Message *****************************
void validMessage() {
  //xmitData("got to validmessage function");
  int cmd = getBuffer();
  if (cmd == 0) return;                            // keep looping through buffer testing message
  if (cmd == 110) {
    //xmitData("Error 110 - Unknown fault");      // currrently just for debug -- maybe future needs
  } else {
    //xmitData("got a cmd = " + String(cmd));
    processCommands(cmd);                         // process commands -- good reader address and good message start/stop
  }
}

// ************************** get buffer ****************************
int getBuffer() {
  //xmitData("got to get buffer");
  int pulling;
  char pulled;
  //String message;
  int next = 0;                    // buffer index
  bool collect = false;            // Don't save buffer until a valid start of message and correct reader address
  bool validStart = false;
  bool validStop = false;
  bool testAdress = false;        // match with global thisReader
while (softSerial.available() > 0) {
  delay(1);
  pulling = softSerial.read();               // next pass after start * is the reader Address
  pulled = (char)pulling;
  if (validStart) {
    if (pulled == thisReader) {              // if not addressed for this reader let buffer run until empty or new start detected
      collect = true;                        // begin to store message
      globalMessage[next] = '*';             // keep message structure intact
      ++next;
    }
    else {
      validStart = false;                    // allows loop to look for another start character
    }                                        // this resets even a good start but the collect had already captured it
  }
  if (pulled == '*') {                       // start character is a *  passes first half of valid message test
    validStart = true;                     // if not a * let buffer run looking for the start of a message or is empty
  }
  if (collect) {                             // ignores \n for any improperly addressed or missing start character
    if (pulled == '\n') {                  // message start and correct address were found
      validStop = true;                   // stop character is a newline -- message was properly terminated
    } else {                                // and passes second half of valid message test
      globalMessage[next] = pulled;       // capture message
      ++next;
    }
  }


  if (validStop == true) {                               // good message structure (start/reader address/stop)
    globalMessage[next] = '\0';                          // terminate string -- messages vary in length
    String msgCommand;                                   // find the command
    String x = String(globalMessage[2]);
    String y = String(globalMessage[3]);
    msgCommand = x;
    msgCommand = msgCommand + y;
    int cmd = msgCommand.toInt();                        // convert for return
    collect = false;                                     // reset loop for next message detection
    validStart = false;
    validStop = false;
    testAdress = false;
    next = 0;
    return cmd;                                          // return command to be processed
  }
  else {
    if (softSerial.available() == 0) {                  // reset after trash or wrong reader address
      collect = false;
      validStart = false;
      validStop = false;
      testAdress = false;
      next = 0;
      return 110;                                     // unknown error/fault  debug test feature
    }
  }
}  
  return 0;                                              // continue looping through buffer
}


//*********************** parse data *********************************************
void parseData() {                             // only needed for command 2
  //xmitData("got to parseData");

  /*
     Filling the array with space characters ensures that all unused elements appear to display nothing
     on the LCD display. this is needed because each variable can hold a "variable" sized string. Using
     the "space" character also gives us something to sort against when reading a new tag filled with NULL
     characters. See updateTag.clearValues()
     THIS loop clears arrays prior to processing data from the server (softSerial)
  */

  for (int i = 0; i < 16; ++i) {              // all these variables have lengths of 17
    carRoadName[i] = ' ';                    // this loop ensures that each array
    carRoadNumber[i] = ' ';                  // element has a printable "space" character
    carType[i] = ' ';                        // for the LCD display.
    carColor[i] = ' ';
    carOwner[i] = ' ';
  }
  carRoadName[16] = '\0';                     // each variable is then NULL terminated.
  carRoadNumber[16] = '\0';
  carType[16] = '\0';
  carColor[16] = '\0';
  carOwner[16] = '\0';

  char trash[5];
  char * strtokIndx;                           // this is used by strtok() as an index

  strtokIndx = strtok(globalMessage, "~");     // get the first part - the string
  strcpy(trash, strtokIndx);                   // drop start bit - address - command

  strtokIndx = strtok(NULL, "~");              // get road name
  strcpy(carRoadName, strtokIndx);
  strtokIndx = strtok(NULL, "~");              // get road number
  strcpy(carRoadNumber, strtokIndx);
  strtokIndx = strtok(NULL, "~");              // get type of car
  strcpy(carType, strtokIndx);
  strtokIndx = strtok(NULL, "~");              // get color of car
  strcpy(carColor, strtokIndx);
  strtokIndx = strtok(NULL, "~");              // get the owner of car
  strcpy(carOwner, strtokIndx);
}
//*******************************************************
