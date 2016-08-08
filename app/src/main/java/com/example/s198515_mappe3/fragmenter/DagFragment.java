package com.example.s198515_mappe3.fragmenter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.s198515_mappe3.DBHandler;
import com.example.s198515_mappe3.R;
import com.example.s198515_mappe3.verktoy.DatoTidFormatter;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.NumberFormat;
import java.util.Calendar;

/**
 * Fragment som viser informasjon om bruk i løpet av en dag. Grafen fordeler det på timer
 */
public class DagFragment extends Fragment {

    private static final String MYDEBUG = "DagFragment";
    private DBHandler db;
    private DagInteraction iListener;

    private TextView tv_dato;
    private TextView tv_teller;
    private GraphView graf;
    private BarGraphSeries<DataPoint> bruksserie;
    private LineGraphSeries<DataPoint> snittserie;


    public interface DagInteraction {
        public DBHandler getDB();
        public Calendar getDato();
        public boolean viseSnitt();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            iListener = (DagInteraction) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement DagInteraction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        iListener = null;
    }


    public DagFragment() {
        // Obligatorisk tom konstruktør
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dag, container, false);
        //Log.d(MYDEBUG, "********\n I oncreateview \n*******");

        db = iListener.getDB();

        tv_dato = (TextView) v.findViewById(R.id.text_use_on_day);
        tv_teller = (TextView) v.findViewById(R.id.text_use_on_day_count);
        graf = (GraphView) v.findViewById(R.id.graph_day);



        Calendar cal = iListener.getDato();
        setTextSum(cal);
        lagGraf(cal);

        if (iListener.viseSnitt())
            lagSnittGraf();

        return v;
    }


    private void setTextSum (Calendar cal) {
        String dato_til_tv = DatoTidFormatter.getFullDatoForMenneske(cal) + ":";
        tv_dato.setText(dato_til_tv);

        int antall = db.getBrukOnDag(cal);
        tv_teller.setText(String.valueOf(antall));
    }

    // Henter informasjonen som skal plottes inn i grafen. Parametere avgjør om den henter detaljer eller snitt
    private DataPoint[] lagGrafData(Calendar cal, boolean gjennomsnitt) {
        if (!gjennomsnitt) {
            int[] bruksdetaljer = db.getBrukDetaljerOnDag(cal);
            DataPoint[] verdier = new DataPoint[bruksdetaljer.length];

            for (int i = 0; i < bruksdetaljer.length; i++) {
                DataPoint dp = new DataPoint(i, bruksdetaljer[i]);
                verdier[i] = dp;
            }

            return verdier;
        }
        else {
            double[] bruksdetaljer = db.getBrukSnittOnDag();
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

        int minsteverdi = 0;
        int timeNaa = cal.get(Calendar.HOUR_OF_DAY);
        int antVisesPaaSkjerm = 6;

        graf.setTitle(getResources().getString(R.string.title_day_graph));
        graf.addSeries(bruksserie);

        //Formaterer grafen slik at den går mellom kl 00 og 23, og stiller den til å vise nåværende time
        Viewport vp = graf.getViewport();
        vp.setXAxisBoundsManual(true);
        vp.setYAxisBoundsManual(true);

        if ((timeNaa-antVisesPaaSkjerm) >= minsteverdi) {
            vp.setMinX((timeNaa+(1-minsteverdi))-antVisesPaaSkjerm);
            vp.setMaxX(timeNaa);
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


        //Formaterer slik at bare heltall vises og ledende 0
        NumberFormat nfX = NumberFormat.getInstance();
        nfX.setMaximumFractionDigits(0);
        nfX.setMinimumIntegerDigits(2);

        NumberFormat nfY = NumberFormat.getInstance();
        nfY.setMaximumFractionDigits(1);

        graf.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nfX, nfY));

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