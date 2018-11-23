/*
 Author: Pramod Singh Rawat (iPramodSinghRawat)
 eMail: iPramodSinghRawat@gmail.com
 CopyRight: Pramod Singh Rawat
*/
/*
 Notes: HTTPS not Working Here
*/
//#include <NTPClient.h>
#include <ESP8266WiFi.h>
//#include <WiFiClient.h>
#include <ESP8266HTTPClient.h>
#include <DHT.h>
#include <WiFiUdp.h>
#include <ArduinoJson.h>
/* WiFi credentials. */
const char *ssid = "WiFi-SSID";//ENTER YOUR WIFI SETTINGS
const char *password = "WiFi_Password";

#define DHTPIN D6 // DHT Sensor Pin
// Uncomment whatever type you're using!
#define DHTTYPE DHT11 // DHT 11
//#define DHTTYPE DHT22 // DHT 22  (AM2302), AM2321
//#define DHTTYPE DHT21 // DHT 21 (AM2301)

//MQ135 Sensor Variables
int MQ135sensorValue;//AQI
int MQ135digitalValue;

#define MQ135_A_PIN A0 // MQ135 Analog Pin
//#define MQ135_D_PIN D6 // digital Pin to Detect Gas using MQ135 -> Not Using for NOw

/*URL Variables*/
String thingSpeakLink="http://api.thingspeak.com";
String link;
String thingsSpeakAPIKey="Enter_Your_Write_API";//
//field1=AQI,field2=Temperature,field3=Humidity,
bool postData=false;

String hardwareChannelId="ChannelTwoNumID";
String hardwareWriteKey="Channel_Two_Write_API";
String hardwareReadKey="Channel_Two_Read_API";
int isFireExtinguisherSystem=0,isAirPurifier=0,isAirCondition=0,isAirVents=0,isAirExhaustSystem=0;

int isFireExtinguisherSystemOld,isAirPurifierOld,isAirConditionOld,isAirVentsOld,isAirExhaustSystemOld;

#define isFireExtinguisherSystemD0 D0 //field1 on ThingsSpeak
#define isAirPurifierD1 D1 //field2 on ThingsSpeak
#define isAirConditionD2 D2 //field3 on ThingsSpeak
#define isAirVentsD3 D3 //field4 on ThingsSpeak
#define isAirExhaustSystemD4 D4 //field5 on ThingsSpeak

#define postSensorDataD8 D8
/*
 * 8 Digital Pins
 * 0 For Testing LED (isFireExtinguisherSystem)
 * 1 For Testing LED (isAirPurifier)
 * 2 For Testing LED (isAirCondition)
 * 3 For Testing LED (isAirVents)
 * 4 For Testing LED (isAirExhaustSystem)
 * 5 
 * 6 
 * 7 to red from DHT sensor 
 * 8 PostSensorDataLed
 * Analog A0 To Read AQI form MQ135
*/
//WiFiUDP ntpUDP;
//NTPClient timeClient(ntpUDP);

/*Sensor Action Variable*/
int fireTempThreshold = 100;
int smokePPMThreshold = 100;

float hmdt;
float tmpr;
float fmpr;

DHT dht(DHTPIN, DHTTYPE);
void setup() {

  Serial.begin(115200);
    
  setUpHardwarePins();
  setupDHTSensor();
  setupMQ135Sensor();
  setup_wifi();

  //timeClient.begin();
}

void loop(){
  
  //timeClient.update();
  //Serial.print("timeClient: ");
  //Serial.println(timeClient.getFormattedTime());
  
  dht_sensor_reading();
  readMQ135SensorReading();
  readAndProcessSensorData();
  if(postData == true){
    //digitalWrite(LED_D0,1);
    Serial.println("PostSensorData");
    postSensorData();
  }
  getHardwareData();
  //postHardwareData();
  setHardwareOutput();
  delay(4900); //AFTER 4.9 SECONDS
}

void setUpHardwarePins(){
  pinMode(isFireExtinguisherSystemD0, OUTPUT);
  pinMode(isAirPurifierD1, OUTPUT);
  pinMode(isAirConditionD2, OUTPUT);
  pinMode(isAirVentsD3, OUTPUT);
  pinMode(isAirExhaustSystemD4, OUTPUT);
  pinMode(postSensorDataD8, OUTPUT);  
}

void setupDHTSensor(){
  Serial.println("DHT11 begin!");
  dht.begin();
}

void setupMQ135Sensor(){
  pinMode(MQ135_A_PIN, INPUT);
  //pinMode(MQ135_D_PIN, INPUT);
}

void setup_wifi(){
  delay(1000);
  WiFi.mode(WIFI_OFF);//Prevents reconnection issue (taking too long to connect)
  delay(1000);
  WiFi.mode(WIFI_STA);//This line hides the viewing of ESP as wifi hotspot  
  WiFi.begin(ssid, password);//Connect to your WiFi router 
  Serial.println("Connecting");
  // Wait for connection
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  } 
  //If connection successful show IP address in serial monitor
  Serial.println("");
  Serial.print("Connected to ");
  Serial.println(ssid);
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());//IP address assigned to your ESP
}

void dht_sensor_reading(){
  // Reading temperature or humidity takes about 250 milliseconds!
  // Sensor readings may also be up to 2 seconds 'old' (its a very slow sensor)
  hmdt = dht.readHumidity();
  // Read temperature as Celsius (the default)
  tmpr = dht.readTemperature();
  // Read temperature as Fahrenheit (isFahrenheit = true)
  fmpr = dht.readTemperature(true);

  //Serial.println("Read hmdt: "+String(hmdt));  
  //Serial.println("Read tmpr: "+String(tmpr));
  //Serial.println("Read fmpr: "+String(fmpr));
  //Test Var
  //hmdt=55; tmpr=25; fmpr=60;
  
  // Check if any reads failed and exit early (to try again).
  if (isnan(hmdt) || isnan(tmpr) || isnan(fmpr)) {
    Serial.println("Failed to read from DHT sensor!");
    postData = false;
    return;
  }else{
    postData = true;
  }
  
  // Compute heat index in Fahrenheit (the default)
  float hif = dht.computeHeatIndex(fmpr, hmdt);
  // Compute heat index in Celsius (isFahreheit = false)
  float hic = dht.computeHeatIndex(tmpr, hmdt, false);

  Serial.print("Humidity: ");
  Serial.print(hmdt);
  Serial.print(" %\t");
  Serial.print("Temperature: ");
  Serial.print(tmpr);
  Serial.print(" *C ");
  Serial.print(fmpr);
  Serial.print(" *F\t");
  Serial.print("Heat index: ");
  Serial.print(hic);
  Serial.print(" *C ");
  Serial.print(hif);
  Serial.println(" *F");
}

void readMQ135SensorReading(){
  MQ135sensorValue = analogRead(MQ135_A_PIN);  
  //MQ135digitalValue = digitalRead(MQ135_D_PIN);
  Serial.println("MQ135sensorValue: "+String(MQ135sensorValue)+" PPM"); 
  //Serial.println("MQ135digitalValue(isGas): " +String(MQ135digitalValue));
}

void readAndProcessSensorData(){
  bool postHardwareDataflag = false;
 
  /*
  Good (0–50)
  Satisfactory (51–100)
  Moderately polluted (101–200)
  Poor (201–300)
  Very poor (301–400)
  Severe (401–500)
  */
  
  /*Testing Var*/
  //MQ135sensorValue=101; //Note: TestCases
  //tmpr=91; //Note: TestCases
  
  if(MQ135sensorValue>400){
    //Action Turn On AirPurifier with level 3
    Serial.println("Action: Turn On AirPurifier with level 3");//Print HTTP return code
    isFireExtinguisherSystem = 0;
    isAirPurifier=1;
    isAirVents=1;
    isAirExhaustSystem=1;
    postHardwareDataflag=true;
  }
  else if(MQ135sensorValue>300){
    //Action Turn On AirPurifier with level 2
    Serial.println("Action: Turn On AirPurifier with level 2");//Print HTTP return code
    isFireExtinguisherSystem = 0;
    isAirPurifier=1;
    isAirVents=1;
    isAirExhaustSystem=1;
    postHardwareDataflag=true;
  }  
  else if(MQ135sensorValue>200){
    //Action Turn On AirPurifier with level 1
    Serial.println("Action: Turn On AirPurifier with level 1");//Print HTTP return code
    isFireExtinguisherSystem = 0;
    isAirPurifier=1;
    isAirVents=1;
    isAirExhaustSystem=1;
    postHardwareDataflag=true;
  }else{
    isFireExtinguisherSystem = 0;
    isAirPurifier=0;
    isAirVents=0;
    isAirExhaustSystem=0;
  }
  
  if(MQ135sensorValue>smokePPMThreshold && tmpr>fireTempThreshold){
    //alert: Fire and Smoke
    //Action 1 Turn On FireExtinguisherSystem
    //Action 2 Turn Off AirPurifier
    //Action 3 Turn Off AirCondition
    //Action 4 Turn Off Air Vents
    //Action 5 Turn Off AirExhaustSystem
    Serial.println("Fire & Smoke: ");//Print HTTP return code
    
    isFireExtinguisherSystem = 1;
    isAirPurifier = 0;    
    isAirCondition = 0;
    isAirVents = 0;
    isAirExhaustSystem = 0;
    postHardwareDataflag=true;
  }
  
  if(isFireExtinguisherSystem==isFireExtinguisherSystemOld 
    && isAirPurifier==isAirPurifierOld
    && isAirVents==isAirVentsOld
    && isAirExhaustSystem==isAirExhaustSystemOld
    && isAirCondition==isAirConditionOld){
      Serial.println("Do Not POST Hardware Data");//Print HTTP return code
      postHardwareDataflag=false;
  }
  else{
      postHardwareDataflag=true;
  }

  //ToDo: Update Hardware Value With data new value
  if(postHardwareDataflag==true){
    Serial.println("POST Hardware Data: ");//Print HTTP return code
    postHardwareData();
  }
  //postHardwareData();
}

void postHardwareData(){
  setHardwareOutput();
  if (WiFi.status() == WL_CONNECTED) {//Check WiFi connection status
      HTTPClient http;//Declare object of class HTTPClient
      String data2post="&field1="+String(isFireExtinguisherSystem)
                        +"&field2="+String(isAirPurifier)
                        +"&field3="+String(isAirCondition)
                        +"&field4="+String(isAirVents)
                        +"&field5="+String(isAirExhaustSystem);
      link = thingSpeakLink+"/update?api_key="+hardwareWriteKey+data2post;
      
      http.begin(link);//Specify request destination
      
      int httpCode = http.GET();//Send the request
      Serial.println("PostHardwareData HttpCode: " + String(httpCode));//Print HTTP return code
      
      if(httpCode > 0){
        String payload = http.getString();//Get the response payload
        Serial.println("PostHardwareData: " + payload);//Print request response payload
      }    
      http.end();//Close connection
    }
}

void postSensorData(){
  if (WiFi.status() == WL_CONNECTED) {//Check WiFi connection status
    
    HTTPClient http;//Declare object of class HTTPClient
    String data2post="&field1="+String(MQ135sensorValue)+"&field2="+String(tmpr)+"&field3="+String(hmdt);// + getData;
    link = thingSpeakLink+"/update?api_key="+thingsSpeakAPIKey+data2post;///"&field1="+String(MQ135_A_PIN);// + getData;
    
    http.begin(link);//Specify request destination
    
    int httpCode = http.GET();//Send the request
    Serial.println("httpCode: " + String(httpCode));//Print HTTP return code
    
    if(httpCode > 0){
      String payload = http.getString();//Get the response payload
      Serial.println("payload: " + payload);//Print request response payload
      
      digitalWrite(postSensorDataD8,1);
      delay(100);
    }
    digitalWrite(postSensorDataD8,0);
    http.end();//Close connection
  }
  //delay(6000);//GET Data at every 6 seconds
}

void getHardwareData(){
  if (WiFi.status() == WL_CONNECTED) {//Check WiFi connection status
    
    HTTPClient http;//Declare object of class HTTPClient
    link = thingSpeakLink+"/channels/"+hardwareChannelId+"/feeds.json?api_key="+hardwareReadKey+"&results=1";
    
    http.begin(link);//Specify request destination
    
    int httpCode = http.GET();//Send the request
    Serial.println("getHardware HttpCode: " + String(httpCode));//Print HTTP return code
    
    if(httpCode > 0){
      String payload = http.getString();//Get the response payload
      Serial.println("getHardware Payload: " + payload);//Print request response payload

      DynamicJsonBuffer jsonBuffer;
      JsonObject& root = jsonBuffer.parseObject(payload);
      JsonArray& requests = root["feeds"];
      if(requests.size()>0){
        for (auto& request : requests) {
          String field1 = request["field1"];
          String field2 = request["field2"];
          String field3 = request["field3"];
          String field4 = request["field4"];
          String field5 = request["field5"];
          String created_at = request["created_at"];

          Serial.print("Hardware Payload: field1 " + field1+" field2: " + field2);
          Serial.print(" field3: " + field3);
          Serial.print(" field4: " + field4);
          Serial.print(" field5: " + field5);
          Serial.println(" created_at: " + created_at);

         isAirCondition=field3.toInt();
          isFireExtinguisherSystemOld=field1.toInt();
          isAirPurifierOld=field2.toInt();
          isAirConditionOld=field3.toInt();
          isAirVentsOld=field4.toInt();
          isAirExhaustSystemOld=field5.toInt();
        }
        
      }else{
        isFireExtinguisherSystem=isAirPurifier=isAirCondition=isAirVents=isAirExhaustSystem=0;
        isFireExtinguisherSystemOld=isAirPurifierOld=isAirConditionOld=isAirVentsOld=isAirExhaustSystemOld=0;      
        postHardwareData();
      }            
    }    
    http.end();//Close connection

  }
  //delay(6000);//GET Data at every 6 seconds
}

void setHardwareOutput(){
  digitalWrite(isFireExtinguisherSystemD0,isFireExtinguisherSystem);
  digitalWrite(isAirPurifierD1,isAirPurifier);
  digitalWrite(isAirConditionD2,isAirCondition);
  digitalWrite(isAirVentsD3,isAirVents);
  digitalWrite(isAirExhaustSystemD4,isAirExhaustSystem);
}
