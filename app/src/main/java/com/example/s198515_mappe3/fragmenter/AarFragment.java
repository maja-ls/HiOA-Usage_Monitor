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
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;

import java.util.Calendar;

/**
 * Fragment som viser informasjon om bruk i løpet av et år. Grafen fordeler det på måneder
 */
public class AarFragment extends Fragment {

    private static final String MYDEBUG = "AarFragment";
    private DBHandler db;
    private AarInteraction iListener;

    private TextView tv_aar;
    private TextView tv_teller;
    private GraphView graf;
    private BarGraphSeries<DataPoint> bruksserie;
    private LineGraphSeries<DataPoint> snittserie;


    public interface AarInteraction {
        public DBHandler getDB();
        public Calendar getDato();
        public boolean viseSnitt();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            iListener = (AarInteraction) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement AarInteraction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        iListener = null;
    }

    public AarFragment() {
        // Obligatorisk tom konstruktør
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(MYDEBUG, "********\n I oncreateview \n*******");
        View v = inflater.inflate(R.layout.fragment_aar, container, false);

        db = iListener.getDB();

        tv_aar = (TextView) v.findViewById(R.id.text_use_on_year);
        tv_teller = (TextView) v.findViewById(R.id.text_use_on_year_count);
        graf = (GraphView) v.findViewById(R.id.graph_year);



        Calendar cal = iListener.getDato();
        setTextSum(cal);
        lagGraf(cal);

        if (iListener.viseSnitt())
            lagSnittGraf();

        return v;
    }


    private void setTextSum (Calendar cal) {
        String aar_til_tv = cal.get(Calendar.YEAR) + ":";
        tv_aar.setText(aar_til_tv);

        int antall = db.getBrukOnAar(cal);
        tv_teller.setText(String.valueOf(antall));
    }

    // Henter informasjonen som skal plottes inn i grafen. Parametere avgjør om den henter detaljer eller snitt
    private DataPoint[] lagGrafData(Calendar cal, boolean gjennomsnitt) {
        if (!gjennomsnitt) {
            int[] bruksdetaljer = db.getBrukDetaljerOnAar(cal);
            DataPoint[] verdier = new DataPoint[bruksdetaljer.length];

            for (int i = 0; i < bruksdetaljer.length; i++) {
                DataPoint dp = new DataPoint(i, bruksdetaljer[i]);
                verdier[i] = dp;
            }

            return verdier;
        }
        else {
            double[] bruksdetaljer = db.getBrukSnittOnAar();
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
        int maanedNaa = cal.get(Calendar.MONTH);
        int antVisesPaaSkjerm = 6;

        graf.setTitle(getResources().getString(R.string.title_year_graph));
        graf.addSeries(bruksserie);


        //Setter viewport til å vise valgt måned og sørger for at viewporten holder seg innenfor grenseverdiene
        Viewport vp = graf.getViewport();
        vp.setXAxisBoundsManual(true);
        vp.setYAxisBoundsManual(true);

        if ((maanedNaa-antVisesPaaSkjerm) >= minsteverdi) {
            vp.setMinX((maanedNaa+(1-minsteverdi))-antVisesPaaSkjerm);
            vp.setMaxX(maanedNaa);
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


        // Formaterer verdiene i grafen til å vise måneden i stedet for tallverdien dens
        final String[] months = getResources().getStringArray(R.array.shortmonths);
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
                    return months[verdi];

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