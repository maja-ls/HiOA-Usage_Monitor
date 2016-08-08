package com.example.s198515_mappe3.fragmenter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.s198515_mappe3.DBHandler;
import com.example.s198515_mappe3.R;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;

/**
 * Fragment som viser informasjon om bruk i løpet av en uke. Grafen fordeler det på dager
 */
public class UkeFragment extends Fragment {

    private static final String MYDEBUG = "UkeFragment";
    private DBHandler db;
    private UkeInteraction iListener;

    private TextView tv_uke;
    private TextView tv_teller;
    private GraphView graf;
    private BarGraphSeries<DataPoint> bruksserie;
    private LineGraphSeries<DataPoint> snittserie;


    public interface UkeInteraction {
        public DBHandler getDB();
        public Calendar getDato();
        public boolean viseSnitt();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            iListener = (UkeInteraction) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement UkeInteraction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        iListener = null;
    }

    public UkeFragment() {
        // Obligatorisk tom konstruktør
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_uke, container, false);

        db = iListener.getDB();

        tv_uke = (TextView) v.findViewById(R.id.text_use_on_week);
        tv_teller = (TextView) v.findViewById(R.id.text_use_on_week_count);
        graf = (GraphView) v.findViewById(R.id.graph_week);

        Calendar cal = iListener.getDato();
        setTextSum(cal);
        lagGraf(cal);

        if (iListener.viseSnitt())
            lagSnittGraf();

        return v;
    }


    private void setTextSum (Calendar cal) {
        String uke_til_tv = getString(R.string.count_text_week) + " " + cal.get(Calendar.WEEK_OF_YEAR) + ", " + cal.get(Calendar.YEAR) + ":";
        tv_uke.setText(uke_til_tv);

        int antall = db.getBrukOnUke(cal);
        tv_teller.setText(String.valueOf(antall));
    }


    // Henter informasjonen som skal plottes inn i grafen. Parametere avgjør om den henter detaljer eller snitt
    private DataPoint[] lagGrafData(Calendar cal, boolean gjennomsnitt) {
        if (!gjennomsnitt) {
            int[] bruksdetaljer = db.getBrukDetaljerOnUke(cal);
            DataPoint[] verdier = new DataPoint[bruksdetaljer.length];

            for (int i = 0; i < bruksdetaljer.length; i++) {
                DataPoint dp = new DataPoint(i, bruksdetaljer[i]);
                verdier[i] = dp;
            }

            return verdier;
        }
        else {
            double[] bruksdetaljer = db.getBrukSnittOnUke();
            DataPoint[] verdier = new DataPoint[bruksdetaljer.length];

            for (int i = 0; i < bruksdetaljer.length; i++) {
                DataPoint dp = new DataPoint(i, bruksdetaljer[i]);
                verdier[i] = dp;
            }

            return verdier;
        }
    }

    // Lager grafen og formaterer visningen av den
    private void lagGraf(Calendar cal) {

        bruksserie = new BarGraphSeries<DataPoint>(lagGrafData(cal, false));

        // Setter grenseverdier for grafen
        int minsteverdi = 0;
        int antVisesPaaSkjerm = 7;

        graf.setTitle(getResources().getString(R.string.title_week_graph));
        graf.addSeries(bruksserie);


        //Setter viewport til å vise ukedager
        Viewport vp = graf.getViewport();
        vp.setXAxisBoundsManual(true);
        vp.setYAxisBoundsManual(true);

        vp.setMinX(minsteverdi);
        vp.setMaxX(6);

        vp.setMinY(minsteverdi);
        graf.getGridLabelRenderer().setNumHorizontalLabels(antVisesPaaSkjerm);


        // Formaterer verdiene i grafen til å vise dagen i stedet for tallverdien dens
        final String[] ukedager = getResources().getStringArray(R.array.weekdays);
        graf.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    //Formaterer x-verdier
                    int verdi = 0;
                    try {
                        verdi = (int) value;
                    } catch (Exception e) {
                        Log.d(MYDEBUG, "********\n Feil ved casting til int \n*******");
                        return super.formatLabel(value, isValueX);
                    }

                    return ukedager[verdi];

                } else {
                    // Vanlig tallverdi for y-verdier
                    return super.formatLabel(value, isValueX);
                }
            }
        });


        //Viser tall øverst på hver stolpe
        bruksserie.setDrawValuesOnTop(true);
        bruksserie.setValuesOnTopColor(Color.BLACK);
        bruksserie.setSpacing(5);

    }


    public void lagSnittGraf() {
        if (db != null) {
            snittserie = new LineGraphSeries<>(lagGrafData(null, true));
            snittserie.setColor(Color.RED);
            graf.addSeries(snittserie);

            Viewport vp = graf.getViewport();
            double hoyesteBruk = vp.getMaxY(true);
            vp.setMaxY(hoyesteBruk);
        }
    }

    public void fjernSnittGraf() {
        if (graf != null)
            graf.removeSeries(snittserie);
    }

    // Oppdaterer feltene hvis db er initialisert
    public void oppdaterAlleFelt (Calendar cal) {
        if (db != null) {
            bruksserie.resetData(lagGrafData(cal, false));
            setTextSum(cal);
        }
    }
}