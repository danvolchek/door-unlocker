#include <Servo.h> 
#include <FileIO.h>
#include <Mailbox.h>
#include <Process.h>
#include <rBase64.h>

// Base64 decoding
rBase64generic<250> mybase64;

// Servo calibration and seek code taken from https://learn.adafruit.com/analog-feedback-servos/using-feedback 
Servo myservo;

// Control and feedback pins
int servoPin = 9;
int feedbackPin = A0;
 
// Calibration values
int minDegrees;
int maxDegrees;
int minFeedback;
int maxFeedback;
int tolerance = 2; // max feedback measurement error

int state = 0; //0 = locked, 1 = unlocked

void setup() 
{ 
  // Initialize Serial
  Serial.begin(9600);
  while (!Serial);
  
  // Set up servo
  myservo.attach(servoPin); 

  Serial.print("Calibrating servo\n");
  calibrate(myservo, feedbackPin, 0, 180);

  // Init wifi, filesystem access
  Serial.print("Starting bridge, mailbox, and filesystem\n");
  pinMode(13, OUTPUT);
  digitalWrite(13, LOW);
  
  Bridge.begin();
  Mailbox.begin();
  FileSystem.begin();
  
  updateState(0);
  digitalWrite(13, HIGH);

  Serial.print("Ready");
  Serial.println();
} 
 
void loop()
{
  // get message
  if(Mailbox.messageAvailable()){
    Serial.print("Got Message: ");
    String message;
    Mailbox.readMessage(message);

    Serial.print(message);
    Serial.print(" -> ");
    
    // decode it
    mybase64.decode(message);
    message = mybase64.result();
    Serial.print(message);
    Serial.print(" -> ");
    
    // decrypt it
    String decrypted = decrypt(message);

    Serial.print(decrypted);
    Serial.println();
    
    // check for valid commands
    if(decrypted.startsWith("lock") || decrypted.startsWith("unlock")){
      // make sure command time is recent
      long sentAt = atol(decrypted.substring(decrypted.indexOf(';') + 1).c_str());
      if (getDate() - sentAt < 5){
        int lock = decrypted.startsWith("lock") ? 0 : 1;
        
        // move the servo as neccessary
        if(lock == 0 && state == 1){
          Seek(myservo, feedbackPin, 0);
          updateState(0);
        } else if (lock == 1 && state == 0){
          Seek(myservo, feedbackPin, 180);
          updateState(1);
        }
      }
    }
  }
  
  delay(100);
}

void updateState(int mState){
  state = mState;
  Bridge.put("state", String(state));
  Serial.print("State changed to ");
  Serial.print(state);
  Serial.println();
}

long getDate(){
  Process date;

  date.begin("date");
  date.addParameter("+%s");
  date.run();

  String currDate = "";
  while(date.available() > 0){
    char c = date.read();
    currDate.concat(c);
  }

  return atol(currDate.c_str());
}

String decrypt(String message){
  FileSystem.remove("/root/enc");
  FileSystem.remove("/root/dec");
  
  File encoded = FileSystem.open("/root/enc", FILE_WRITE);
  encoded.print(message);
  encoded.flush();
  encoded.close();
  
  Process openssl;

  openssl.begin("openssl");
  openssl.addParameter("enc");
  openssl.addParameter("-nosalt");
  openssl.addParameter("-d");
  openssl.addParameter("-aes-128-cbc");
  openssl.addParameter("-in");
  openssl.addParameter("/root/enc");
  openssl.addParameter("-out");
  openssl.addParameter("/root/dec");
  openssl.addParameter("-K");
  openssl.addParameter("00000000000000000000000000000000"); // TODO: replace with your key, as a hex string like 3442... for 0x34, 0x42, ...
  openssl.addParameter("-iv");
  openssl.addParameter("00000000000000000000000000000000"); // TODO: replace with your iv, same format as the key

  openssl.run();

  File decoded = FileSystem.open("/root/dec", FILE_READ);

  char c = 0;

  String result="";
  while((c = decoded.read()) != -1){
    result.concat(c);
  }
  return result;
}

/*
  This function establishes the feedback values for 2 positions of the servo.
  With this information, we can interpolate feedback values for intermediate positions
*/
void calibrate(Servo servo, int analogPin, int minPos, int maxPos)
{
  // Move to the minimum position and record the feedback value
  servo.write(minPos);
  minDegrees = minPos;
  delay(2000); // make sure it has time to get there and settle
  minFeedback = analogRead(analogPin);
  
  // Move to the maximum position and record the feedback value
  servo.write(maxPos);
  maxDegrees = maxPos;
  delay(2000); // make sure it has time to get there and settle
  maxFeedback = analogRead(analogPin);
}

void Seek(Servo servo, int analogPin, int pos)
{
  // Start the move...
  servo.write(pos);
  
  // Calculate the target feedback value for the final position
  int target = map(pos, minDegrees, maxDegrees, minFeedback, maxFeedback); 

  int tries = 0;
  // Wait until it reaches the target
  while(abs(analogRead(analogPin) - target) > tolerance && tries++ < 5000){} // wait...
}
