
#ifndef tagHeader
#include "runTag2.h"
#endif


extern int pendingRequest;
extern int retry;

bool commandReply();

// ************************** process commands **************************
void processCommands(int cmd) {
  //xmitData("process cmd = " + String(cmd));
  int myCmd = cmd;
  bool testReply;
  //xmitData("got to process commands");
  switch (myCmd) {
    case 1:
      //xmitData("doing case 1");
      command1();
      break;
    case 2:
      command2();
      break;
    case 20:                            // assign new engine
      //xmitData("case 20");
      testReply = commandReply();       // common code for True/False
      digitalWrite(greenPin, LOW);
      digitalWrite(redPin, LOW);
      if (testReply) {
        assignedEngine = "";
        assignedEngine = carID;         // remember this ID is an engine
        displayLCD(4);                  // display proper complete message
        //      xmitData("assignedEngine = " + String(assignedEngine));
        //      xmitData("got engine assignment");
      }
      break;
    case 21:                            // drop car at reader
      testReply = commandReply();       // common code for True/False
      if (testReply) {
           displayLCD(2);               // display proper complete message
      }
      break;
    case 22:                            // pickup this car and add to engine
      testReply = commandReply();       // common code for True/False
      if (testReply) {
           displayLCD(3);               // display proper complete message
      }
      break;
    default:
      displayLCD(14);                   // bad command received
      break;
  }
}

//********************* command one **************************************
// Server asking the reader if it has anything to process.
// Server sends *[reader address][command]\n  *A01\n
// Reader can reply with (20-is engine) (21-drop) (22-pick)
// (20) *[reader address][command]~[engine RFID]\n  *A20~1234567890\n
// (21 or 22) *[reader][command]~[engine RFID]~[car RFID]\n  *A20~1234567890~1234567890\n



void command1() {
  String request;
  switch (pendingRequest) {
    case 20:
      request = "*" + String(thisReader) + "20~" + carID + "\n";                // carID check if engine and link with this reader
      xmitData(request);
      pendingReply = true;
      displayLCD(16);
      break;
    case 21:
      request = "*" + String(thisReader) + "21~" + assignedEngine + "~" + carID + "\n"; // carID is car to assign to this reader (drop)
      xmitData(request);
      pendingReply = true;
      displayLCD(16);;
      break;
    case 22:
      request = "*" + String(thisReader) + "22~" + assignedEngine + "~" + carID + "\n"; // carID is car to assign to this engine (pick)
      xmitData(request);
      pendingReply = true;
      displayLCD(16);
      break;
    default:
      request = "*" + String(thisReader) + "01\n";                              // nothing pending just acknowledge command
      xmitData(request);
      pendingReply = false;
      break;                                                                    // don't clear as it removes car display
  }
  //xmitData(request);
}

//********************* command two **************************************
// Server request to write to the RFID tag (always reader A) (always command 02)
// *[A][02]~[roadName]~[roadNumber]~[type]~[color]~[owner]\n
// reader replies with the new RFID tag  *[A] [02]~[RFID]\n  *A02~1234567890\n

void command2() {
  //xmitData("got to command 2");
  parseData();
  updateTag();
}


//********************* command 20 / 21 / 22 **************************************
// common code for commands 20/21/22
// Server reply is
// *[reader][command]~[True/False]\n    *A20~True\n

bool commandReply() {
  //xmitData("globalMessage = " + globalMessage);
  char reply;
  byte reply1;
  bool testReply = false;
  reply1 = globalMessage[5];
  reply = reply1;
  //xmitData("reply = " + String(reply));
  //xmitData("pendingRequest = " + String(pendingRequest));
  //xmitData("retry = " + String(retry));
  if (reply == 'T') {
    testReply = true;
  } else {
     displayLCD(7);                                  // display failure
  }
  pendingRequest = 0;                             // stop asking server for this request
  pendingReply = false;                           // release blocking of panel switches
  return testReply;
}
