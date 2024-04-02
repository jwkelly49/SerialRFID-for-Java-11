
#ifndef tagHeader
#include "runTag2.h"
#endif


//****************** AUTHENICATION ***************************************************
bool authenication(byte block) {
  // Authenticate using key A
  byte trailingBlock = setTrailingBlock(block);
  //    xmitData("Authenticating using key A...");
  status = (MFRC522::StatusCode) mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, trailingBlock, &key, &(mfrc522.uid));
  if (status != MFRC522::STATUS_OK) {
    //xmitData("PCD_Authenticate() failed: ");
    //xmitData(mfrc522.GetStatusCodeName(status));
    return true;
  }
  return false;
}

//********************** DUMP_BYTE_ARRAY ************************************************
// Helper routine to dump a byte array as hex values to Serial.
void dump_byte_array(byte *buffer, byte bufferSize) {
  for (byte i = 0; i < bufferSize; i++) {
    //prepData(buffer[i] < 0x10 ? " 0" : " ");
    //String x = String(buffer[i], HEX);
    //prepData(x);
  }
}

//******************** DUMP FUlL SECTOR ****************************************
void dumpFullSector(byte sector) {
  // Dump the sector data
  //    xmitData("Current data in sector:");
  mfrc522.PICC_DumpMifareClassicSectorToSerial(&(mfrc522.uid), &key, sector);
  //    xmitData(" ");
}

//******************** Show card ID *******************************************
bool showCardID() {
  // Show some details of the PICC (that is: the tag/card)
  bool error = false;
  //xmitData("Card UID:");
  //dump_byte_array(mfrc522.uid.uidByte, mfrc522.uid.size);
  //xmitData(" ");
  //xmitData("PICC type: ");
  MFRC522::PICC_Type piccType = mfrc522.PICC_GetType(mfrc522.uid.sak);
  //xmitData(mfrc522.PICC_GetTypeName(piccType));

  // Check for compatibility
  if (    piccType != MFRC522::PICC_TYPE_MIFARE_MINI
          &&  piccType != MFRC522::PICC_TYPE_MIFARE_1K
          &&  piccType != MFRC522::PICC_TYPE_MIFARE_4K) {
    //        xmitData("This sample only works with MIFARE Classic cards.");
    error = true;
  }
  return error;
}

//***********SET TRAILING BLOCK *****************************************************
byte setTrailingBlock(byte block) {
  //  xmitData("passed block = ");
  //  xmitData(String(block));
  if ( block > 3 && block < 7) {
    return (byte)7;
  };     // sector 1
  if ( block > 7 && block < 11) {
    return (byte)11;
  };    // sector 2
  if ( block > 11 && block < 15) {
    return (byte)15;
  };    // sector 3
  if ( block > 15 && block < 15) {
    return (byte)19;
  };    // sector 4
  if ( block > 19 && block < 15) {
    return (byte)23;
  };    // sector 5
  if ( block > 23 && block < 15) {
    return (byte)27;
  };    // sector 6
  if ( block > 27 && block < 15) {
    return (byte)31;
  };    // sector 7
  if ( block > 31 && block < 15) {
    return (byte)35;
  };    // sector 8
  if ( block > 35 && block < 15) {
    return (byte)39;
  };    // sector 9
  if ( block > 39 && block < 15) {
    return (byte)43;
  };    // sector 10
  if ( block > 43 && block < 15) {
    return (byte)47;
  };    // sector 11
  if ( block > 47 && block < 15) {
    return (byte)51;
  };    // sector 12
  if ( block > 51 && block < 15) {
    return (byte)55;
  };    // sector 13
  if ( block > 55 && block < 15) {
    return (byte)59;
  };    // sector 14
  if ( block > 59 && block < 15) {
    return (byte)63;
  };    // sector 15
}
