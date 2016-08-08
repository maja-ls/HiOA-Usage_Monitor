package com.example.s198515_mappe3.fragmenter.dialoger;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.s198515_mappe3.R;

/**
 * Created by Maja on 23.11.2015.
 */
public class Infodialog extends DialogFragment {
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String tittel = getResources().getString(R.string.om_tittel);
        String info = getResources().getString(R.string.om_tekst);

        Dialog d = getDialog();
        d.setTitle(tittel);

        ScrollView s = new ScrollView(getActivity());

        LinearLayout l = new LinearLayout(getActivity());
        l.setOrientation(LinearLayout.VERTICAL);

        TextView tv = new TextView(getActivity());
        tv.setText(info);

        Button b = new Button(getActivity());
        b.setText(getResources().getString(R.string.om_knapp));
        b.setOnClickListener(knappelytter);

        l.addView(tv);
        l.addView(b);
        s.addView(l);
        return s;
    }


    private OnClickListener knappelytter = new OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };
}
