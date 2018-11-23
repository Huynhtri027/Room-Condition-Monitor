package it.ipramodsinghrawat.alpharoomcondition;

import android.support.v7.app.AppCompatActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SuperActivity extends AppCompatActivity {
    //private static final DateFormat TimeZone = ;

    public String removeAlphabetFromTimeString(String timeString){
        return timeString.replaceAll("[^\\d-:]", " ");
    }

    public String uTCDate2IstDate(String utcDateTime) throws ParseException {
        utcDateTime=removeAlphabetFromTimeString(utcDateTime);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date myDate = simpleDateFormat.parse(utcDateTime);

        //SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss a");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd-MM-YYYY hh:mm:ss a");
        //return myDate;
        return simpleDateFormat2.format(myDate);
    }

    public Date string2date(String dateInString){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        ///SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.ENGLISH);
        //formatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        //String dateInString = "22-01-2015 10:15:55 AM";
        Date date = null;
        try {
            date = formatter.parse(dateInString);
        } catch (ParseException e){
            e.printStackTrace();
        }
        //String formattedDateString = formatter.format(date);
        return date;
    }

}