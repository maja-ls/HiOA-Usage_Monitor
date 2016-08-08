package com.example.s198515_mappe3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

// Receiver som registrerer når brukeren låser opp telefonen, og viser en toast hvis det er stilt inn
public class Mottaker extends BroadcastReceiver {

    private static final String MYDEBUG = "Mottaker";



    @Override
    public void onReceive(Context context, Intent intent) {
        String PREFS = context.getPackageName()+"_preferences";
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        boolean toastpaa = prefs.getBoolean(context.getString(R.string.prefs_show_toast), false);
        boolean stortoast = prefs.getBoolean(context.getString(R.string.prefs_large_toast), false);

        DBHandler db = new DBHandler(context);

        int antallIdag = db.registrerBruk();

        if (toastpaa) {
            final Toast toast = Toast.makeText(context, String.valueOf(antallIdag), Toast.LENGTH_SHORT);

            if (stortoast) {
                toast.setGravity(Gravity.CENTER, 0, 0);
                ViewGroup vg = (ViewGroup) toast.getView();
                TextView toastTextView = (TextView) vg.getChildAt(0);
                toastTextView.setTextSize(120);
                toastTextView.setTextColor(Color.RED);
            }

            toast.show();

            // Avbryter toasten etter 200ms slik at den forsvinner fortere
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 200);
        }
    }
}
