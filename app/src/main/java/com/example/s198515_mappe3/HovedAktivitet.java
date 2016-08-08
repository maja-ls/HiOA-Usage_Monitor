package com.example.s198515_mappe3;


import android.app.DialogFragment;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.Toast;

import com.example.s198515_mappe3.fragmenter.*;
import com.example.s198515_mappe3.fragmenter.dialoger.DatePickerFragment;
import com.example.s198515_mappe3.fragmenter.dialoger.Infodialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// Hovedaktiviteten som instansierer og kommuniserer med tabsene/fragmentene
public class HovedAktivitet extends AppCompatActivity implements
        DagFragment.DagInteraction,
        UkeFragment.UkeInteraction,
        MaanedFragment.MaanedInteraction,
        AarFragment.AarInteraction,
        DatePickerFragment.OnDatePickerInteraction {

    private static final String MYDEBUG = "HovedAktivitet";
    private DBHandler db;
    private Calendar valgtDato;
    private boolean visSnitt;

    private DagFragment dagFragment;
    private UkeFragment ukeFragment;
    private MaanedFragment maanedFragment;
    private AarFragment aarFragment;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hoved_aktivitet);

        visSnitt = false;
        valgtDato = Calendar.getInstance();

        if(savedInstanceState != null) {
            Long valgtDatoIMillis = savedInstanceState.getLong("VALGTTID");
            visSnitt = savedInstanceState.getBoolean("VISESNITT");
            valgtDato.setTimeInMillis(valgtDatoIMillis);
        }

        db = new DBHandler(this);

        dagFragment = new DagFragment();
        ukeFragment = new UkeFragment();
        maanedFragment = new MaanedFragment();
        aarFragment = new AarFragment();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Long valgtDatoIMillis = valgtDato.getTimeInMillis();
        outState.putLong("VALGTTID", valgtDatoIMillis);
        outState.putBoolean("VISESNITT", visSnitt);

        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hoved_aktivitet, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(this, PreferanseAktivitet.class);
                startActivity(i);
                break;

            case R.id.action_about:
                Infodialog infodialog = new Infodialog();
                infodialog.show(getFragmentManager(), "Dialog");
                break;

            case R.id.option_refresh:
                oppdaterGrafer();
                break;

            case R.id.option_select_today:
                valgtDato = Calendar.getInstance();
                oppdaterGrafer();
                break;

            case R.id.option_show_average:
                if (visSnitt) {
                    visSnitt = false;
                    dagFragment.fjernSnittGraf();
                    ukeFragment.fjernSnittGraf();
                    maanedFragment.fjernSnittGraf();
                    aarFragment.fjernSnittGraf();
                    Toast.makeText(getApplicationContext(), getString(R.string.options_hide_avg_toast)
                            , Toast.LENGTH_SHORT).show();
                }
                else {
                    visSnitt = true;
                    dagFragment.lagSnittGraf();
                    ukeFragment.lagSnittGraf();
                    maanedFragment.lagSnittGraf();
                    aarFragment.lagSnittGraf();
                    Toast.makeText(getApplicationContext(), getString(R.string.options_show_avg_toast)
                            , Toast.LENGTH_SHORT).show();
                }
                oppdaterGrafer();
                break;

            case R.id.option_select_dates:
                DialogFragment picker = new DatePickerFragment();
                picker.show(getFragmentManager(), "datePicker");
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    // Legger inn fragmentene i ViewPagerAdapter
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(dagFragment, getString(R.string.tab_tittel_day));
        adapter.addFragment(ukeFragment, getString(R.string.tab_tittel_week));
        adapter.addFragment(maanedFragment, getString(R.string.tab_tittel_month));
        adapter.addFragment(aarFragment, getString(R.string.tab_tittel_year));
        viewPager.setAdapter(adapter);
    }

    // Adapter som inneholder fragmentene/tabsene og tittelen på disse
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        // Returnerer position_none slik at adapteren oppdaterer viewet etter at man endrer f.eks dato
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }



    private void oppdaterGrafer () {
        viewPager.getAdapter().notifyDataSetChanged();
        dagFragment.oppdaterAlleFelt(valgtDato);
        maanedFragment.oppdaterAlleFelt(valgtDato);
        aarFragment.oppdaterAlleFelt(valgtDato);
        ukeFragment.oppdaterAlleFelt(valgtDato);
    }


    ////////////////////////////////////////////////////////
    // Implementerte interface-metoder
    ////////////////////////////////////////////////////////

    // Returnerer databasen til fragmentene slik at de kan jobbe mot den
    @Override
    public DBHandler getDB() {
        if(db == null)
            db = new DBHandler(this);
        return db;
    }

    // Denne metoden brukes i interface til alle fragmentene for å hente valgt dato
    @Override
    public Calendar getDato() {
        return valgtDato;
    }

    // Returnerer om gjennomsnitt skal vises eller ikke
    @Override
    public boolean viseSnitt() {
        return visSnitt;
    }

    // Setter den valgte datoen
    @Override
    public void setDatoTilAktivitet(Calendar cal) {
        valgtDato = cal;
        oppdaterGrafer();
    }

}

