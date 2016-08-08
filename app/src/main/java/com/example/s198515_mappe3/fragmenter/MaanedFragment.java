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
 * Fragment som viser informasjon om bruk i løpet av en måned. Grafen fordeler det på dager
 */
public class MaanedFragment extends Fragment {

    private static final String MYDEBUG = "MaanedFragment";
    private DBHandler db;
    private MaanedInteraction iListener;

    private TextView tv_maaned;
    private TextView tv_teller;
    private GraphView graf;
    private BarGraphSeries<DataPoint> bruksserie;
    private LineGraphSeries<DataPoint> snittserie;

    public interface MaanedInteraction {
        public DBHandler getDB();
        public Calendar getDato();
        public boolean viseSnitt();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            iListener = (MaanedInteraction) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement MaanedInteraction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        iListener = null;
    }

    public MaanedFragment() {
        // Obligatorisk tom konstruktør
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(MYDEBUG, "********\n I oncreateview \n*******");
        View v = inflater.inflate(R.layout.fragment_maaned, container, false);

        db = iListener.getDB();

        tv_maaned = (TextView) v.findViewById(R.id.text_use_on_month);
        tv_teller = (TextView) v.findViewById(R.id.text_use_on_month_count);
        graf = (GraphView) v.findViewById(R.id.graph_month);



        Calendar cal = iListener.getDato();
        setTextSum(cal);
        lagGraf(cal);

        if (iListener.viseSnitt())
            lagSnittGraf();

        return v;
    }


    private void setTextSum (Calendar cal) {
        String[] months = getResources().getStringArray(R.array.months);
        String maaned_til_tv = months[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.YEAR) + ":";
        tv_maaned.setText(maaned_til_tv);

        int antall = db.getBrukOnMaaned(cal);
        tv_teller.setText(String.valueOf(antall));
    }

    // Henter informasjonen som skal plottes inn i grafen. Parametere avgjør om den henter detaljer eller snitt
    private DataPoint[] lagGrafData(Calendar cal, boolean gjennomsnitt) {
        if (!gjennomsnitt) {
            int[] bruksdetaljer = db.getBrukDetaljerOnMaaned(cal);

            DataPoint[] verdier = new DataPoint[bruksdetaljer.length];

            for (int i = 0; i < bruksdetaljer.length; i++) {
                DataPoint dp = new DataPoint(i, bruksdetaljer[i]);
                verdier[i] = dp;
            }

            return verdier;
        }
        else {
            double[] bruksdetaljer = db.getBrukSnittOnMaaned();
            DataPoint[] verdier = new DataPoint[bruksdetaljer.length];

            for (int i = 0; i < bruksdetaljer.length; i++) {
                DataPoint dp = new DataPoint(i, bruksdetaljer[i]);
                verdier[i] = dp;
                //Log.d(MYDEBUG, "********\n BRUKSDETALJER["+i+"] = "+bruksdetaljer[i]+" \n*******");
            }

            return verdier;
        }
    }

    // Lager grafen og formaterer visningen av den
    private void lagGraf(Calendar cal) {

        bruksserie = new BarGraphSeries<DataPoint>(lagGrafData(cal, false));

        graf.setTitle(getResources().getString(R.string.title_month_graph));
        graf.addSeries(bruksserie);

        int minsteverdi = 0;
        int datoNaa = cal.get(Calendar.DAY_OF_MONTH);
        //Trekker fra 1 siden DAY_OF_MONTH har range 1-31
        datoNaa--;
        int antVisesPaaSkjerm = 7;

        //Formaterer grafen slik at den går mellom 1. og siste dato i måneden
        Viewport vp = graf.getViewport();
        vp.setXAxisBoundsManual(true);
        vp.setYAxisBoundsManual(true);

        if ((datoNaa-antVisesPaaSkjerm) >= minsteverdi) {
            vp.setMinX((datoNaa+(1-minsteverdi))-antVisesPaaSkjerm);
            vp.setMaxX(datoNaa);
        }
        else {
            vp.setMinX(minsteverdi);
            vp.setMaxX(antVisesPaaSkjerm-(1-minsteverdi));
        }

        vp.setMinY(minsteverdi);
        graf.getGridLabelRenderer().setNumHorizontalLabels(antVisesPaaSkjerm);

        //Gjør at man kan scrolle i grafen. Tabsene gjør det litt vanskelig å scrolle, hvertfall på emulator
        // men hvis man begynner med å dra litt opp eller ned i grafen før man drar til siden
        // så vil den låse at man er i grafen
        vp.setScrollable(true);


        // Formaterer verdiene i grafen til å vise datoen med punktum
        graf.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    //Formaterer x-verdier

                    return super.formatLabel(value+1, isValueX) + ".";
                } else {
                    // Vanlig tallverdi for y-verdier
                    return super.formatLabel(value, isValueX);
                }
            }
        });

        //Viser tall øverst på hver stolpe
        bruksserie.setDrawValuesOnTop(true);
        bruksserie.setValuesOnTopColor(Color.BLACK);
        bruksserie.setSpacing(1);

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