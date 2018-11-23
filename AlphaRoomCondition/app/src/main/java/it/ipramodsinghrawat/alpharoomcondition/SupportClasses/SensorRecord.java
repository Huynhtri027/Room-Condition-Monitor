package it.ipramodsinghrawat.alpharoomcondition.SupportClasses;

public class SensorRecord {

    private float aqi;
    private float temperature;
    private float humidity;
    private String dateTime;

    public float getAqi() {
        return aqi;
    }

    public void setAqi(float aqi) {
        this.aqi = aqi;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public SensorRecord(float aqi, float temperature, float humidity, String dateTime){

        this.aqi = aqi;
        this.temperature = temperature;
        this.humidity = humidity;
        this.dateTime = dateTime;

    }
}
