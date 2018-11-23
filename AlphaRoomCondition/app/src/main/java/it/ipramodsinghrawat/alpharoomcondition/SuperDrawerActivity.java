package it.ipramodsinghrawat.alpharoomcondition;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;

public class SuperDrawerActivity extends SuperActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            go2MainActivity();
            // Handle the camera action
        } else if (id == R.id.nav_status) {

        } else if (id == R.id.nav_air_quality) {
            go2AirQualityVisualizationsActivity();
        } else if (id == R.id.nav_temperature) {
            go2TemperatureVisualizationActivity();
        } else if (id == R.id.nav_humidity) {
            go2HumidityVisualizationActivity();
        }else if (id == R.id.nav_all_visualization) {
            go2SensorsRecordsVisualizationsActivity();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void go2MainActivity(){
        this.startActivity(new Intent(this, MainActivity.class));
    }

    public void go2AirQualityVisualizationsActivity(){
        this.startActivity(new Intent(this, AirQualityVisualizationsActivity.class));
    }

    public void go2TemperatureVisualizationActivity(){
        this.startActivity(new Intent(this, TemperatureVisualizationActivity.class));
    }


    public void go2HumidityVisualizationActivity(){
        this.startActivity(new Intent(this, HumidityVisualizationActivity.class));
    }

    public void go2SensorsRecordsVisualizationsActivity(){
        this.startActivity(new Intent(this, SensorsRecordsVisualizationsActivity.class));
    }
}
