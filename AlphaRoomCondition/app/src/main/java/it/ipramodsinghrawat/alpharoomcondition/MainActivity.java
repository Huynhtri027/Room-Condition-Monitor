package it.ipramodsinghrawat.alpharoomcondition;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import it.ipramodsinghrawat.alpharoomcondition.SupportClasses.HardwareRecord;
import it.ipramodsinghrawat.alpharoomcondition.SupportClasses.SensorRecord;

public class MainActivity extends SuperDrawerActivity{

    /*Notes:
    * in Case of Fire: PPM > 200 and Temperature above 100
    * in case of (AQI)PPM
    * Good (0–50)
    * Satisfactory (51–100)
    * Moderately polluted (101–200)
    * Poor (201–300)
    * Very poor (301–400)
    * Severe (401–500)
    * */
    TextView mq135PPMReadingsTV,dhtTemperatureReadings,dhtHumidityReadings,smokeAlertTV,
            fireAlertTV,fireExtinguisherAlertTV,
            airPurifierAlertTV,airConditionAlertTV,airVentsAlertTV,airExhaustFanAlertTV,
            sensorReadingsErrorTV,hardwareEquipmentErrorTV;

    String thingsSpeakUrl ="https://api.thingspeak.com/";
    String dataChannel="Channel_ID";
    String thingsSpeakReadAPIKey="ChannelReadAPI";

    String HardwareDataChannel="Channel_Two_ID";
    String hardwareDataReadAPIKey="Channel_TWO_ReadAPI";
    String hardwareDataWriteAPIKey="Channel_Two_WritePI";

    float aqiSmokeThreshold=100,tmpFireThreshold=100;
    //float aqiReading,tmpReading,humReading;

    SensorRecord sensorRecord;
    HardwareRecord hardwareRecord;

    //int isFireExtinguisherSystem,isAirPurifier,isAirCondition,isAirVents,isAirExhaustSystem;
    Button airConditionBTN;// = (Button) findViewById(R.id.airConditionBTN);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mq135PPMReadingsTV = findViewById(R.id.mq135PPMReadingsTV);
        dhtTemperatureReadings = findViewById(R.id.dhtTemperatureReadings);
        dhtHumidityReadings = findViewById(R.id.dhtHumidityReadings);

        smokeAlertTV = (TextView) findViewById(R.id.smokeAlertTV);
        fireAlertTV = (TextView) findViewById(R.id.fireAlertTV);
        fireExtinguisherAlertTV = (TextView) findViewById(R.id.fireExtinguisherAlertTV);
        airPurifierAlertTV = (TextView) findViewById(R.id.airPurifierAlertTV);
        airConditionAlertTV = (TextView) findViewById(R.id.airConditionAlertTV);
        airVentsAlertTV = (TextView) findViewById(R.id.airVentsAlertTV);
        airExhaustFanAlertTV = (TextView) findViewById(R.id.airExhaustFanAlertTV);
        airConditionBTN = (Button) findViewById(R.id.airConditionBTN);

        sensorReadingsErrorTV = findViewById(R.id.sensorReadingsErrorTV);
        hardwareEquipmentErrorTV = findViewById(R.id.hardwareEquipmentErrorTV);
        ////timeZone = Calendar.getInstance().getTimeZone().getID();
        //Toast.makeText(getApplicationContext(), "TimeZone:" +Calendar.getInstance().getTimeZone().getID(), Toast.LENGTH_SHORT).show();
        callAsynchronousTask();
    }

    public void startTextViewAlert(TextView textView){

        textView.setTextColor(Color.RED);
        //textView.setTypeface(Typeface.DEFAULT_BOLD);

        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(50); //You can manage the time of the blink with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        textView.startAnimation(anim);
    }

    public void stopTextViewAlert(TextView textView){
        textView.setTextColor(Color.BLACK);
        textView.clearAnimation();
    }

    public void callAsynchronousTask() {

        mq135PPMReadingsTV.setText("Loading ...");
        dhtTemperatureReadings.setText("Loading ...");
        dhtHumidityReadings.setText("Loading ...");

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            getRoomSensorsData();
                            getRoomHardwareStatusData();

                            //getJSONObjectFromUrl();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 40000); //execute in every 40000 ms
    }

    public void getRoomSensorsData(){

        stopTextViewAlert(smokeAlertTV);
        stopTextViewAlert(fireAlertTV);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        //fetching single last recent data
        String url = thingsSpeakUrl+"channels/"+dataChannel+"/feeds.json?api_key="+thingsSpeakReadAPIKey+"&results=1";

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject channel = response.getJSONObject("channel");
                            JSONArray feeds = response.getJSONArray("feeds");
                            //String ppm = feeds.getString(Integer.parseInt("field1"));
                            //mq135PPMReadingsTV.setText("feeds is: "+ feeds.toString());
                            if(feeds.length()>0){
                                for (int j = 0; j < feeds.length(); j++) {
                                    JSONObject feed = feeds.getJSONObject(j);
                                    String ppm = feed.getString("field1");
                                    String temperature = feed.getString("field2");
                                    String humidity = feed.getString("field3");

                                    mq135PPMReadingsTV.setText("Air Quality: "+ ppm+" PPM");
                                    dhtTemperatureReadings.setText("Temperature: "+ temperature+ (char) 0x00B0 +" C");
                                    dhtHumidityReadings.setText("Humidity: "+ humidity+" %");

                                    if(ppm.equals("null") || ppm.equals("nan")){
                                        ppm="0";
                                    }

                                    if(!temperature.equals("null") || !temperature.equals("nan")){
                                        temperature="0";
                                    }

                                    if(humidity.equals("null") || humidity.equals("nan")){
                                        humidity="0";
                                    }

                                    sensorRecord =  new SensorRecord(
                                            Float.parseFloat(ppm),
                                            Float.parseFloat(temperature),
                                            Float.parseFloat(humidity),
                                            feed.getString("created_at")
                                    );

                                    processSensorRecord(sensorRecord);
                                }
                            }else{
                                sensorReadingsErrorTV.setText("* No Sensors Feeds Yet");
                                Toast.makeText(getApplicationContext(),"No Sensors Feeds !",Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            sensorReadingsErrorTV.setText("Sensors Feeds Error: "+e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                sensorReadingsErrorTV.setText("That didn't work!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(jsonObjReq);
    }

    public void getRoomHardwareStatusData(){

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        //fetching single last recent data
        String url = thingsSpeakUrl+"channels/"+HardwareDataChannel+"/feeds.json?api_key="+hardwareDataReadAPIKey+"&results=1";

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject channel = response.getJSONObject("channel");
                            JSONArray feeds = response.getJSONArray("feeds");

                            if(feeds.length() > 0){
                                for (int j = 0; j < feeds.length(); j++) {
                                    JSONObject feed = feeds.getJSONObject(j);

                                    String fireExtinguisherSystemVR="0";
                                    if(!feed.getString("field1").equals("null")){
                                        fireExtinguisherSystemVR = feed.getString("field1");
                                    }

                                    String airPurifierVR="0";
                                    if(!feed.getString("field2").equals("null")){
                                        airPurifierVR = feed.getString("field2");
                                    }

                                    String airConditionVR="0";
                                    if(!feed.getString("field3").equals("null")){
                                        airConditionVR = feed.getString("field3");
                                    }

                                    String airVentsVR="0";
                                    if(!feed.getString("field4").equals("null")){
                                        airVentsVR = feed.getString("field4");
                                    }

                                    String airExhaustSystemVR="0";
                                    if(!feed.getString("field5").equals("null")){
                                        airExhaustSystemVR = feed.getString("field5");
                                    }

                                    hardwareRecord = new HardwareRecord(
                                            Integer.parseInt(fireExtinguisherSystemVR),
                                            Integer.parseInt(airPurifierVR),
                                            Integer.parseInt(airConditionVR),
                                            Integer.parseInt(airVentsVR),
                                            Integer.parseInt(airExhaustSystemVR),
                                            feed.getString("created_at"));

                                    processHardwareRecord(hardwareRecord);
                                }

                            }else{
                                Toast.makeText(getApplicationContext(),"No HardWare Feeds !",Toast.LENGTH_SHORT).show();
                                hardwareEquipmentErrorTV.setText("* No HardWare Feeds !");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            hardwareEquipmentErrorTV.setText("HardWare Feeds JSONException: "+e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mq135PPMReadingsTV.setText("That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsonObjReq);
    }

    public void processSensorRecord(SensorRecord sensorRecordObj){

        setAQIGraphic(mq135PPMReadingsTV,sensorRecordObj.getAqi());

        if(sensorRecordObj.getAqi()>=aqiSmokeThreshold && sensorRecordObj.getTemperature()>=tmpFireThreshold){
            startTextViewAlert(smokeAlertTV);
            startTextViewAlert(fireAlertTV);
            //Close Vents,
            //Enable FireExtinguisher,
            //Close AirPurifier,
            //Close Exhaust Fan,
        }

    }

    public void processHardwareRecord(HardwareRecord hardwareRecord){

        if(hardwareRecord.getFireExtinguisherSystem() == 1){
            fireExtinguisherAlertTV.setText("Fire Extinguisher: On");
        }else{
            fireExtinguisherAlertTV.setText("Fire Extinguisher: Off");
        }

        if(hardwareRecord.getAirVents() == 1){
            airVentsAlertTV.setText("Air Vents: On");
        }else{
            airVentsAlertTV.setText("Air Vents: Off");
        }

        if(hardwareRecord.airExhaustSystem == 1){
            airExhaustFanAlertTV.setText("Air Exhaust System: On");
        }else{
            airExhaustFanAlertTV.setText("Air Exhaust System: Off");
        }

        if(hardwareRecord.getAirPurifier() == 1){
            airPurifierAlertTV.setText("Air Purifier: On");
        }else{
            airPurifierAlertTV.setText("Air Purifier: Off");
        }

        if(hardwareRecord.getAirCondition() == 1){
            airConditionAlertTV.setText("Air Condition: On");
            airConditionBTN.setText("Switch Off AC");
        }else{
            airConditionAlertTV.setText("Air Condition: Off");
            airConditionBTN.setText("Switch On AC");
        }

    }

    public void setAQIGraphic(TextView textView, float reading){
        /*
         * in case of (AQI)PPM
         * Good (0–50)
         * Satisfactory (51–100)
         * Moderately polluted (101–200)
         * Poor (201–300)
         * Very poor (301–400)
         * Severe (401–500)
         */
        if(reading <50){
            //textView.setTextColor(Color.GREEN);
            textView.setTextColor(Color.rgb(0,204,0));
        }else if(reading >=50 && reading <100){
            textView.setTextColor(Color.rgb(102,204,0));
        }else if(reading >=100 && reading <200){
            textView.setTextColor(Color.rgb(255,255,0));
            textView.setTextSize(18);
            ///ToDo: above this reading
            //send/check for Purifier System
        }else if(reading >=200 && reading <300){
            textView.setTextColor(Color.rgb(255,153,0));
            textView.setTextSize(20);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        }else if(reading >=300 && reading <400){
            textView.setTextColor(Color.rgb(255,0,0));
            textView.setTextSize(22);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        }else if(reading >=400){
            textView.setTextColor(Color.rgb(165,42,42));
            textView.setTextSize(24);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        }
    }

    public void switchOnAc(View view){
        //Update HardwareRecord based on time
        //keep update Ac data with old data in Arduino
        if(hardwareRecord!=null){
            if(hardwareRecord.getAirCondition()==1){
                hardwareRecord.setAirCondition(0);
                airConditionBTN.setText("Switch On AC");
            }else{
                hardwareRecord.setAirCondition(1);
                airConditionBTN.setText("Switch Off AC");
            }
            postRoomHardwareStatusData(hardwareRecord);
        }

    }

    public void postRoomHardwareStatusData(final HardwareRecord hardwareRecord){

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        //fetching single last recent data
        String postData="&field1="+hardwareRecord.getFireExtinguisherSystem()
                +"&field2="+hardwareRecord.getAirPurifier()
                +"&field3="+hardwareRecord.getAirCondition()
                +"&field4="+hardwareRecord.getAirVents()
                +"&field5="+hardwareRecord.getAirExhaustSystem();
        String url = thingsSpeakUrl+"/update?api_key="+hardwareDataWriteAPIKey+postData;

        //Toast.makeText(getApplicationContext(),url,Toast.LENGTH_LONG).show();
        //hardwareDataWriteAPIKey
        //GET https://api.thingspeak.com/update?api_key=73PI5S8N1FHI61FD&field1=0&field2=0&field3=0
        // Request a string response from the provided URL.
        StringRequest  jsonObjReq = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();

                        if(Integer.parseInt(response)>0){

                            /*
                            if(hardwareRecord.getAirCondition()==1){
                                airConditionBTN.setText("Switch Off AC");
                            }else{
                                airConditionBTN.setText("Switch On AC");
                            }
                            */

                        }else{

                            if(hardwareRecord.getAirCondition()==1){
                                hardwareRecord.setAirCondition(0);
                                airConditionBTN.setText("Switch On AC");
                            }else{
                                hardwareRecord.setAirCondition(1);
                                airConditionBTN.setText("Switch Off AC");
                            }
                            //hardwareRecord.setAirCondition(0);
                            //airConditionBTN.setText("Switch Off AC");

                        }

                        /*
                        if(hardwareRecord.getAirCondition()==1){
                            hardwareRecord.setAirCondition(0);
                            airConditionBTN.setText("Switch On AC");
                        }else{
                            hardwareRecord.setAirCondition(1);
                            airConditionBTN.setText("Switch Off AC");
                        }
                        */

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mq135PPMReadingsTV.setText("That didn't work!");

            }
        });
        // Add the request to the RequestQueue.
        queue.add(jsonObjReq);
    }
}
