package it.ipramodsinghrawat.alpharoomcondition;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import it.ipramodsinghrawat.alpharoomcondition.SupportClasses.SensorRecord;

public class SensorsRecordsVisualizationsActivity extends SuperDrawerActivity{

    String thingsSpeakUrl ="https://api.thingspeak.com/channels/";
    String dataChannel="Channel_ID";
    String thingsSpeakReadAPIKey="ChannelReadAPI";

    GraphView graphView;

    DataPoint[] aqiDataPoint;// = new DataPoint[rowCount];//v_count
    DataPoint[] tmpDataPoint;// = new DataPoint[rowCount];//amount
    DataPoint[] humDataPoint;// = new DataPoint[rowCount];//amount
    String[] dataPointDateTime;// = new DataPoint[rowCount];//amount

    List<SensorRecord> sensorRecordsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors_records_visualizations);
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

        graphView = (GraphView) findViewById(R.id.graph);

        //jsonDataTV = findViewById(R.id.jsonDataTV);
        callAsynchronousTask();
    }

    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            getJSONObjectFromUrl();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block

                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 50000); //execute in every 50000 ms
    }

    public void getJSONObjectFromUrl(){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        //fetching single last recent data
        //String url = thingsSpeakUrl+dataChannel+"/fields"+"/feeds.json?api_key="+thingsSpeakReadAPIKey;
        String url = thingsSpeakUrl+dataChannel+"/feeds.json?api_key="+thingsSpeakReadAPIKey;//+"&results=1";

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //jsonDataTV.setText("Response is: "+ response.toString());
                        try {
                            JSONObject channel = response.getJSONObject("channel");
                            //mq135PPMReadings.setText("channel is: "+ channel.toString());

                            JSONArray feeds = response.getJSONArray("feeds");

                            //mq135PPMReadings.setText("feeds is: "+ feeds.toString());

                            //String ppm = feeds.getString(Integer.parseInt("field1"));
                            //mq135PPMReadings.setText("feeds is: "+ feeds.toString());

                            aqiDataPoint = new DataPoint[feeds.length()];//v_count
                            tmpDataPoint = new DataPoint[feeds.length()];//v_count
                            humDataPoint = new DataPoint[feeds.length()];//amount

                            String[] horizontalLabels = new String[feeds.length()];
                            dataPointDateTime = new String[feeds.length()];

                            sensorRecordsList = new ArrayList<SensorRecord>();

                            for (int j = 0; j < feeds.length(); j++) {
                                JSONObject feed = feeds.getJSONObject(j);
                                String aqi = feed.getString("field1");
                                String temperature = feed.getString("field2");
                                String humidity = feed.getString("field3");

                                String dateTime=feed.getString("created_at");
                                try {
                                    dateTime = uTCDate2IstDate(dateTime);
                                } catch (java.text.ParseException e) {
                                    dateTime = dateTime +" UTC";
                                    e.printStackTrace();
                                }

                                dataPointDateTime[j] = dateTime;
                                aqiDataPoint[j] = new DataPoint(j, Float.parseFloat(aqi));
                                tmpDataPoint[j] = new DataPoint(j, Float.parseFloat(temperature));
                                humDataPoint[j] = new DataPoint(j, Float.parseFloat(humidity));

                                //horizontalLabels[j] = "L"+j+aqi;
                                sensorRecordsList.add(new SensorRecord(Float.parseFloat(aqi),
                                        Float.parseFloat(temperature),
                                        Float.parseFloat(humidity),
                                        dateTime));
                            }

                            plotLineGraphViewType(graphView,aqiDataPoint,tmpDataPoint,humDataPoint,"PPM",(char) 0x00B0+" C"," %");

                            String[] tableColumn = { "#", "AQI (ppm)", "Temperature ("+(char) 0x00B0+" C)", "Humidity (%)", "DateTime"};
                            showLaneTransactionDataOnTableLayOut(sensorRecordsList,tableColumn);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //mq135PPMReadings.setText("That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsonObjReq);
    }

    public void showLaneTransactionDataOnTableLayOut(List<SensorRecord> sensorDataList,String[] column){

        //Toast.makeText(this, "laneTransactionList : " + laneTransactionList.size(), Toast.LENGTH_SHORT).show();
        //RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) reportViewDataLL
        //      .getLayoutParams();

        ScrollView sv = findViewById(R.id.reportViewDataMainSV); //new ScrollView(this);
        sv.removeAllViews();
        if(sensorRecordsList.size()>1){

            TableLayout tableLayout = createLaneTransactionTableLayout(sensorRecordsList, column);
            HorizontalScrollView hsv = new HorizontalScrollView(this);

            hsv.addView(tableLayout);
            sv.addView(hsv);
        }else{
            graphView.setVisibility(View.GONE);
        }
        //reportViewDataLL.addView(sv);
    }

    private TableLayout createLaneTransactionTableLayout(List<SensorRecord> sensorRecordsList, String [] cv) {

        int rowCount = sensorRecordsList.size();
        // 1) Create a tableLayout and its params
        TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
        TableLayout tableLayout = new TableLayout(this);
        tableLayout.setBackgroundColor(Color.BLACK);
        //tableLayout.setLayoutParams(tableLayoutParams);

        // 2) create tableRow params
        TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams();
        tableRowParams.setMargins(1, 1, 1, 1);
        tableRowParams.weight = 1;
        //tableRowParams.width = TableRow.LayoutParams.MATCH_PARENT;
        //tableRow.setLayoutParams(TableRow.LayoutParams.MATCH_PARENT);
        // 3) create tableRow
        TableRow tableRow = new TableRow(this);
        tableRow.setBackgroundColor(Color.BLACK);

        tableRow.addView(setGetTextViewFrTableRow(cv[0]), tableRowParams);
        tableRow.addView(setGetTextViewFrTableRow(cv[1]), tableRowParams);
        tableRow.addView(setGetTextViewFrTableRow(cv[2]), tableRowParams);
        tableRow.addView(setGetTextViewFrTableRow(cv[3]), tableRowParams);
        tableRow.addView(setGetTextViewFrTableRow(cv[4]), tableRowParams);
        tableLayout.addView(tableRow, tableLayoutParams);

        for (int i = 0; i < rowCount; i++) {
            // 3) create tableRow
            //TableRow
            tableRow = new TableRow(this);
            tableRow.setBackgroundColor(Color.BLACK);

            SensorRecord sensorRecord = sensorRecordsList.get(i);

            tableRow.addView(setGetTextViewFrTableRow(String.valueOf(i+1)), tableRowParams);
            tableRow.addView(setGetTextViewFrTableRow(String.valueOf(sensorRecord.getAqi())), tableRowParams);
            tableRow.addView(setGetTextViewFrTableRow(String.valueOf(sensorRecord.getTemperature())), tableRowParams);
            tableRow.addView(setGetTextViewFrTableRow(String.valueOf(sensorRecord.getHumidity())), tableRowParams);
            tableRow.addView(setGetTextViewFrTableRow(sensorRecord.getDateTime()), tableRowParams);

            // 6) add tableRow to tableLayout
            tableLayout.addView(tableRow, tableLayoutParams);
        }

        return tableLayout;
    }

    public TextView setGetTextViewFrTableRow(String text){

        TextView textView = new TextView(this);
        textView.setBackgroundColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(5,5,5,5);
        textView.setText(text);

        return textView;
    }

    //plotLineGraphViewType(graphView,aqiDataPoint,tmpDataPoint,humDataPoint,"PPM"," C"," %");
    public void plotLineGraphViewType(GraphView graph,DataPoint[] aqiDataPoint,
                                      DataPoint[] tmpDataPoint,
                                      DataPoint[] humDataPoint,String aqiTitle,String tmpTitle,String humTitle) {
        graphView.removeAllSeries();

        LineGraphSeries<DataPoint> aqiSeries = new LineGraphSeries<>(aqiDataPoint);

        aqiSeries.setColor(Color.RED);
        //series.setSpacing(20);
        aqiSeries.setAnimated(true);
        aqiSeries.setDrawDataPoints(true);
        aqiSeries.setDataPointsRadius(10);
        aqiSeries.setTitle(aqiTitle);
        aqiSeries.setAnimated(true);
        aqiSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                //dataPointDateTime
                int index = (int)dataPoint.getX();
                Toast.makeText(getApplicationContext(),
                              "AQI: "+  sensorRecordsList.get(index).getAqi()+" PPM"
                                +"Humidity: "+  sensorRecordsList.get(index).getHumidity()+" %"
                                +"Temperature:" + sensorRecordsList.get(index).getTemperature()+(char) 0x00B0
                                +" C, Time: "+sensorRecordsList.get(index).getDateTime(), Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(), "PPM:" + dataPoint+", Time: "+dataPointDateTime[index], Toast.LENGTH_SHORT).show();
            }
        });


        LineGraphSeries<DataPoint> tmpSeries = new LineGraphSeries<>(tmpDataPoint);
        tmpSeries.setColor(Color.BLUE);
        //series.setSpacing(20);
        tmpSeries.setAnimated(true);
        tmpSeries.setDrawDataPoints(true);
        tmpSeries.setDataPointsRadius(10);
        tmpSeries.setTitle(tmpTitle);
        tmpSeries.setAnimated(true);
        tmpSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                //dataPointDateTime
                int index = (int)dataPoint.getX();
                Toast.makeText(getApplicationContext(), "Temperature:" + dataPoint+" C, Time: "+dataPointDateTime[index], Toast.LENGTH_SHORT).show();
            }
        });

        LineGraphSeries<DataPoint> humSeries = new LineGraphSeries<>(humDataPoint);
        humSeries.setColor(Color.GREEN);
        //series.setSpacing(20);
        humSeries.setAnimated(true);
        humSeries.setDrawDataPoints(true);
        humSeries.setDataPointsRadius(10);
        humSeries.setTitle(humTitle);
        humSeries.setAnimated(true);
        humSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                //dataPointDateTime
                int index = (int)dataPoint.getX();
                Toast.makeText(getApplicationContext(), "Humidity:" + dataPoint+"% , Time: "+dataPointDateTime[index], Toast.LENGTH_SHORT).show();
            }
        });

        graph.addSeries(aqiSeries);
        graph.addSeries(tmpSeries);
        graph.addSeries(humSeries);

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        //graph.setShowLegend(true);
        //graph.setLegendAlign(LegendAlign.TOP);

        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        //staticLabelsFormatter.setHorizontalLabels(horizontalLabels);
        //staticLabelsFormatter.setVerticalLabels(new String[] {"Sun","Mon","Tue","Sun2","Mon2","Tue2"});
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        graph.getGridLabelRenderer().setTextSize(12f);
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time");
        graph.getGridLabelRenderer().setVerticalAxisTitle("AQI/Temprature,Humidity");
        //graph.getGridLabelRenderer().setHorizontalAxisTitleTextSize(12f);
        //graph.getGridLabelRenderer().setVerticalAxisTitleTextSize(12f);
        graph.getGridLabelRenderer().reloadStyles();

        // set the viewport wider than the data, to have a nice view
        //graph.getViewport().setMinX(0d);
        //graph.getViewport().setMaxX(8d);
        //graph.getViewport().setXAxisBoundsManual(true);
        //graph.getViewport().setXAxisBoundsManual(false);

        //graph.getViewport().setMinX(series.getLowestValueX() );
        //graph.getViewport().setMaxX(series.getHighestValueX());

        graph.getViewport().setScrollable(true); // enables horizontal scrolling
        graph.getViewport().setScrollableY(true); // enables vertical scrolling
        graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        graph.getViewport().setScalableY(true); // enables vertical zooming and scrolling
        graph.getViewport().scrollToEnd();

    }
}
