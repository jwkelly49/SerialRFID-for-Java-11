#ifndef tagHeader
#define tagHeader
#include "runTag2.h"
#include <string.h>
#include <arduino.h>
#include <SPI.h>
#include <MFRC522.h>
#include  "Wire.h"
#include "LiquidCrystal_I2C.h"
#include <SoftwareSerial.h>
#include <LiquidCrystal_I2C.h>
extern MFRC522 mfrc522;
extern SoftwareSerial softSerial;
extern MFRC522::MIFARE_Key key;
extern MFRC522::StatusCode status;
extern LiquidCrystal_I2C lcd;
extern bool carPresent;

extern byte buffer[18];
extern byte size;

extern char thisReader;

extern char globalMessage[100];

extern char carRoadName[];
extern char carRoadNumber[];
extern char carType[];
extern char carColor[];
extern char carOwner[];
extern bool badWrite;

extern byte _name;
extern byte _number;
extern byte _type;
extern byte _color;
extern byte _owner;

extern String carID;
extern String assignedEngine;

bool authenication(byte block);
void dump_byte_array(byte *buffer, byte bufferSize);
void dumpFullSector(byte sector);
bool showCardID();
int readBlock(byte whichBlock);

void updateTag();
int writeBlock(byte whichBlock);
int checkWrite(byte checkBlock);
byte setTrailingBlock(byte block);
void scanTag();
void validMessage();
int getBuffer();
void parseData();
void processCommands(int cmd);
void command1();
void command2();
void command20();
void command21();
void command22();
void pickUp();
void dropOff();
void assignIt();
void releaseCar();
bool prepData(String sw);
void toggle();
bool xmitData(String sw);
void displayLCD(int value);
void flashLoop();
const byte pickPin = A0;
const byte dropPin = A1;
const byte assignPin = 2;
const byte releasePin = 3;
const byte greenPin = A3;
const byte redPin = A2;
const byte TXcntlPin = 4;
const byte txPin = 7;
const byte rxPin = 8;
extern int pendingRequest;
extern bool pendingReply;
extern int retry;
#endif
