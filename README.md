Fundamentally SerialRFID provides the user with an easy to use Liquid Crystal Display (LCD) showing the road markings of your rolling stock. 
To accomplish this each piece of stock must have a Radio Frequency Identification (RFID) tag. The local LCD display will show 

1- Type (box car), 

2- Color (Green), 

3- Road name/number (Milw123)

4- Owner (John Doe). 

Items 1-3 were selected because of the ease they create while working a yard; say during an operations session. Item 4 can help clubs and OPS hosts identify who owns that piece of stock when there is duplicate stock with identical markings.

SerialRFID is not integrated with JMRI but it does have a "hook" into its XML files so that if you have entered your engines, cars and locations that information can be pulled into SerialRFID and even send that information to a RFID chip without having to manually reenter all that data. More details are explained later in this manual.

SerialRFID is written in Java and that means if you already have JMRI running it's a quick and easy install. 

The Arduino Nano chip was programmed using the Arduino IDE (C++) and each reader has one required program change. The default code for a reader is addressed as reader 'A' as every system must have a reader 'A' to use the SerialRFID programming features. Each additional reader must have this address changed before connecting it to the communications bus. This is not a complicated task and more details are explained later in this manual. The program can support up to 25 readers ( address A-Y.) 
