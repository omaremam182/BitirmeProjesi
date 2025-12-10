package com.example.myprojcet.deviceControl;

import static android.widget.Toast.makeText;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Calendar;

public class ileriZamanliIslem extends BroadcastReceiver {
    public ileriZamanliIslem(Context context){
        myContext = context;
    }
    Context myContext;
    @Override
    public void onReceive(Context context, Intent intent) {
//        this.myContext = context;
        Toast.makeText(context,"Alarm Trigerred", Toast.LENGTH_LONG).show();
    }

    public void setExactAlarm(int saat, int dk) {
        // Get the AlarmManager system service
        AlarmManager alarmManager = (AlarmManager) myContext.getSystemService(Context.ALARM_SERVICE);

        // Create an Intent that will trigger the AlarmReceiver
        Intent intent = new Intent(this.myContext, ileriZamanliIslem.class);

        // Create a PendingIntent to wrap the intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.myContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the alarm to go off at a specific time in the future (e.g., 10 AM tomorrow)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, saat);  // Set to 10 AM
        calendar.set(Calendar.MINUTE, dk);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long futureTime = calendar.getTimeInMillis();

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, futureTime, pendingIntent);

            makeText(this.myContext, "Alarm set "+saat +" : "+dk, Toast.LENGTH_SHORT).show();
        }
    }
}
