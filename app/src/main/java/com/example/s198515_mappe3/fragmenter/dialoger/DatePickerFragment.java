package com.example.s198515_mappe3.fragmenter.dialoger;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Calendar;

// Fragment som lar bruker velge en dato å vise
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {


    private static final String MYDEBUG = "Datepickerfragment";

    private Calendar dato;

    private OnDatePickerInteraction iListener;

    public interface OnDatePickerInteraction {
        public void setDatoTilAktivitet(Calendar dato);
        public Calendar getDato();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            iListener = (OnDatePickerInteraction) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDatePickerInteraction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        iListener = null;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        dato = iListener.getDato();

        if (dato == null)
            dato = Calendar.getInstance();

        int year = dato.get(Calendar.YEAR);
        int month = dato.get(Calendar.MONTH);
        int day = dato.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Log.d(MYDEBUG, "\n********\n\n ER I ONDATESET *******");
        dato.set(year, month, day);

        iListener.setDatoTilAktivitet(dato);
        dismiss();
    }
}