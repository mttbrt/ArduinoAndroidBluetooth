#include <SoftwareSerial.h>

// Pins
#define LED 10
#define BT_TX_PIN 11
#define BT_RX_PIN 12
#define anPin 0

// Temperature
float celsius = 0; // Celsius temperature
float millivolts; // Voltage variable
int sensor; // Sensor value [0-1023]
float sum = 0.0;
int frames = 0;
int count = 0;

// Bluetooth
byte input = 0; // Value passed in by the bluetooth
float output = 0;
SoftwareSerial bluetoothModule =  SoftwareSerial(BT_RX_PIN, BT_TX_PIN);






void setup() {
  pinMode(BT_RX_PIN, INPUT);
  pinMode(BT_TX_PIN, OUTPUT);
  pinMode(LED, OUTPUT);

  bluetoothModule.begin(9600);
}

void loop() {
  
  // Receive data from bluetooth module
  while (bluetoothModule.available() > 0) {
    input = bluetoothModule.read();
  }
  pinPower(input); // Set the LED value to the last value received

  // Temperature calculation
  if(frames%1000 == 0) { // Every 1000 frames get the value and sum
    sensor = analogRead(anPin);
    millivolts = (sensor*5000.0)/1023; // 1023:5000=sensor:x
    celsius = millivolts/10;
    
    sum += celsius;
    count++;
  }

  if(frames%100000 == 0) { // Every 100000 frames send the average of the last 100 values (then reset and restart)
    output = sum/count;
    sendData(output);
    
    frames=0;
    sum=0.0;
    count=0;
  }
  
  frames++;
  
  
}
 
void pinPower(int power) {
  analogWrite(LED, power);
}

void sendData(float value) {
  String string = String(value); // Convert to string and send
  bluetoothModule.print(' ' + string + '#'); // With the character "#" the app understands the sending is finished
}

    

