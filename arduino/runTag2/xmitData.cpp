
#ifndef tagHeader
#include "runTag2.h"
#endif

bool prepData(String sw) {       // only used internally
  String sw2 = sw;
  digitalWrite(TXcntlPin, HIGH); // set transmit mode
  softSerial.print(sw2);
  delay(30);
  digitalWrite(TXcntlPin, LOW);  // return to recieve mode
}

bool xmitData(String sw) {
  String sw2 = sw;
  digitalWrite(TXcntlPin, HIGH); // set transmit mode
  softSerial.println(sw2);
  delay(30);                     // empty buffer
  digitalWrite(TXcntlPin, LOW);  // return to recieve mode
}
