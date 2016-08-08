package com.example.s198515_mappe3.verktoy;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Verktøyklasse som formatterer datoer og tider
 */
public class DatoTidFormatter {

    private static final String MYDEBUG = "DatoTidFormatter.java";

    private static final SimpleDateFormat DATO_FULL_FORMATTER_FOR_MASKIN = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private static final SimpleDateFormat DATO_FULL_FORMATTER_FOR_MENNESKE = new SimpleDateFormat("dd. MMM yyyy", Locale.getDefault());

    private static final SimpleDateFormat DATO_AAR_MAANED_FORMATTER_FOR_MASKIN = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

    private static final SimpleDateFormat TID_FORMATTER = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());



    // Returnerer en dato i form av string som er formatert for lettest å brukes av en maskin
    public static String getFullDato(Calendar cal) {
        String dato;
        dato = DatoTidFormatter.DATO_FULL_FORMATTER_FOR_MASKIN.format(cal.getTime());

        return dato;
    }

    // Returnerer en  dato i form av string som er formatert for å være lettlest for et menneske
    public static String getFullDatoForMenneske(Calendar cal) {
        String dato;
        dato = DatoTidFormatter.DATO_FULL_FORMATTER_FOR_MENNESKE.format(cal.getTime());

        return dato;
    }


    // Returnerer en dato i form av string som er formatert for lettest å brukes av en maskin
    public static String getAarMaanedDato(Calendar cal) {
        String dato;
        dato = DatoTidFormatter.DATO_AAR_MAANED_FORMATTER_FOR_MASKIN.format(cal.getTime());

        return dato;
    }


    // Returnerer en  dato i form av  calendar object fra en string
    public static Calendar getCalendarDatoFraStringMaskin(String dato) {

        Calendar cal = Calendar.getInstance();
        //Har en try her fordi DATO_FULL_FORMATTER_FOR_MASKIN.parse krever det
        try {
            cal.setTime(DatoTidFormatter.DATO_FULL_FORMATTER_FOR_MASKIN.parse(dato));
        }
        catch (Exception e) {
            Log.d(MYDEBUG, "********\n Det skjedde en feil ved konvertering fra String til Calendar \n*******");
            return null;
        }

        return cal;
    }


    // Returnerer et tidspunkt i form av string som er formatert for å være lettlest for et menneske
    public static String getStringTidFraCal (Calendar cal) {
        String tid;
        tid = DatoTidFormatter.TID_FORMATTER.format(cal.getTime());

        return tid;
    }

    // Returnerer en klone av et kalender objekt som er satt til mandagen i uka parameterdatoen er i
    // Klone da det ikke er ønskelig at det skal gjøres endringer på parameteren siden denne
    // også kan brukes andre steder
    public static Calendar getMandagIUke(Calendar cal) {

        Calendar klone = (Calendar) cal.clone();
        //Henter hvilken ukedag cal er. Mandag har verdi 2
        int dagIUke = klone.get(Calendar.DAY_OF_WEEK);
        //Setter søndag til å ha høyest verdi for å forenkle sjekkinga
        if (dagIUke == 1)
            dagIUke = 8;

        int differanseMandagOgNaa = Calendar.MONDAY - dagIUke;
        if (differanseMandagOgNaa != 0) {
            klone.add(Calendar.DAY_OF_MONTH, differanseMandagOgNaa);
        }
        //Log.d(MYDEBUG, "********\n Mandag oppgitt uke = " + DatoTidFormatter.getFullDatoForMenneske(cal) + "\n*******");

        return klone;
    }

    public static int getUkedag(String d) {

        Calendar dato = getCalendarDatoFraStringMaskin(d);
        //Henter hvilken ukedag cal er. Mandag har verdi 2
        int dagIUke = dato.get(Calendar.DAY_OF_WEEK);
        //Setter søndag til å ha høyest verdi for å forenkle sjekkinga
        if (dagIUke == 1)
            dagIUke = 8;

        //Justerer slik at uka går fra 0-6
        dagIUke -= 2;

        return dagIUke;
    }
}
