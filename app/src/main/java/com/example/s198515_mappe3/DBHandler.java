package com.example.s198515_mappe3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.s198515_mappe3.verktoy.DatoTidFormatter;

import java.util.Calendar;


/**
 * Klasse som jobber mot database
 */
public class DBHandler extends SQLiteOpenHelper {

    private static final String MYDEBUG = "DBHandler.java";

    private static int DATABASE_VERSION = 1;
    private static String DATABASE_NAME = "BruksLogger";

    private static String TABLE_DATOER = "Datoer";
    private static String TABLE_TIDER = "Tider";

    private static String KEY_ID = "_ID";
    private static String KEY_DATO = "Dato";
    private static String KEY_ANTALL = "Antall";

    private static String KEY_TID = "Tid";
    private static String FKEY_DATO_ID = "D_Id";


    public DBHandler (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String LAG_DATO = "CREATE TABLE " + TABLE_DATOER + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_DATO + " TEXT," +
                KEY_ANTALL + " INTEGER" + ");";

        Log.d(MYDEBUG, "********\n LAG_DATO = " + LAG_DATO + "\n*******");
        db.execSQL(LAG_DATO);

        String LAG_TID = "CREATE TABLE " + TABLE_TIDER + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_TID + " TEXT," +
                FKEY_DATO_ID + " INTEGER," +
                " FOREIGN KEY  ("+FKEY_DATO_ID+") REFERENCES " + TABLE_DATOER + "(" + KEY_ID + "));";

        Log.d(MYDEBUG, "********\n LAG_TID = " + LAG_TID + "\n*******");
        db.execSQL(LAG_TID);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIDER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATOER);

        onCreate(db);
    }

    // Registrerer at mobilen er blitt brukt på gitt tidspunkt
    public int registrerBruk() {

        Calendar dagensDato = Calendar.getInstance();
        String datoNaa = DatoTidFormatter.getFullDato(dagensDato);
        String tidNaa = DatoTidFormatter.getStringTidFraCal(dagensDato);


        String sql = "SELECT " + KEY_ID + ", " + KEY_ANTALL + " FROM " + TABLE_DATOER;
        sql += " WHERE " + KEY_DATO + " LIKE '" + datoNaa + "'";
        //Log.d(MYDEBUG, "********\n regbruk = " + sql + "\n*******");

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(sql, null);

        int antall;
        int id;

        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
            antall = cursor.getInt(1);
            antall++;
            Log.d(MYDEBUG, "********\n Fant dato\n*******");
        }
        else {
            id = regNyDato(datoNaa, db);
            antall = 1;
            Log.d(MYDEBUG, "********\n Registrerte ny dato\n*******");
        }

        cursor.close();

        ContentValues tidValues = new ContentValues();
        ContentValues datoValues = new ContentValues();

        try {
            tidValues.put(KEY_TID, tidNaa);
            tidValues.put(FKEY_DATO_ID, id);
            db.insert(TABLE_TIDER, null, tidValues);

            datoValues.put(KEY_ANTALL, antall);
            db.update(TABLE_DATOER, datoValues, KEY_ID + "= ?", new String[]{String.valueOf(id)});

            Log.d(MYDEBUG, "********\n REGISTRERING OK, ID =" + id +"\n*******");
        }
        catch (Exception e) {
            Log.d(MYDEBUG, "********\n FEIL VED OPPDATERING AV DB\n*******");
        }

        db.close();

        return antall;
    }

    //Registrerer en ny dato og returnerer dennes id
    public int regNyDato (String s, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        try {
            values.put(KEY_DATO, s);
            long longId = db.insert(TABLE_DATOER, null, values);
            return (int) longId;
        }
        catch (Exception e) {
            Log.d(MYDEBUG, "********\n FEIL VED REGISTRERING AV NY DATO\n*******");
            return -1;
        }
    }


    // Henter id'en for en gitt dato. Returnerer -1 hvis datoen ikke er lagret
    public int getDatoId (String dato, SQLiteDatabase db) {
        String sql = "SELECT " + KEY_ID + " FROM " + TABLE_DATOER;
        sql += " WHERE " + KEY_DATO + " LIKE '" + dato + "'";

        Cursor cursor = db.rawQuery(sql, null);

        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
            Log.d(MYDEBUG, "********\n Fant dato\n*******");
        }
        else {
            Log.d(MYDEBUG, "********\ngetDatoId Fant ikke dato, returnerer -1\n*******");
        }

        cursor.close();

        return id;
    }


    // Returnerer bruk for en dag
    public int getBrukOnDag(Calendar cal, SQLiteDatabase db) {

        String dato = DatoTidFormatter.getFullDato(cal);

        int bruk = 0;

        String sql = "SELECT " + KEY_ANTALL + " FROM " + TABLE_DATOER;
        sql += " WHERE " + KEY_DATO + " LIKE '" + dato + "'";
        //Log.d(MYDEBUG, "********\n getBrukOnDag sql = " + sql + "\n*******");

        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst())
            bruk = cursor.getInt(0);

        cursor.close();

        return bruk;
    }

    // Overloadet utgave som oppretter databaseforbindelse hvis dette ikke ble sendt med
    public int getBrukOnDag(Calendar cal) {
        SQLiteDatabase db = this.getWritableDatabase();

        int bruk = getBrukOnDag(cal, db);
        db.close();

        return bruk;
    }

    //Returnerer detaljert bruk for en dag
    public int[] getBrukDetaljerOnDag (Calendar cal) {
        SQLiteDatabase db = this.getWritableDatabase();

        String dato = DatoTidFormatter.getFullDato(cal);
        int id = getDatoId(dato, db);


        int[] detaljbruk = new int[24];

        for (int i = 0; i < detaljbruk.length; i++) {
            String sql = "SELECT COUNT(*) FROM " + TABLE_TIDER;
            sql += " WHERE " + FKEY_DATO_ID + " = '" + id + "'";
            sql += " AND " + KEY_TID + " LIKE '";
            if (i < 10)
                sql += "0";
            sql += i + ":%:%'";

            //Log.d(MYDEBUG, "********\n getBrukDetaljerOnDag sql = " + sql + "\n*******");
            Cursor cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                detaljbruk[i] = cursor.getInt(0);
            }
            else {
                detaljbruk[i] = 0;
            }

            cursor.close();
        }

        db.close();
        return detaljbruk;
    }

    // Returnerer snittverdier for hver time
    public double[] getBrukSnittOnDag() {
        SQLiteDatabase db = this.getWritableDatabase();

        double[] brukPerTime = new double[24];

        for (int i = 0; i < brukPerTime.length; i++) {
            String sql = "SELECT COUNT(*) FROM " + TABLE_TIDER;
            sql += " WHERE " + KEY_TID + " LIKE '";
            if (i < 10)
                sql += "0";
            sql += i + ":%:%'";

            Cursor brukscursor = db.rawQuery(sql, null);
            brukscursor.moveToFirst();
            int bruk = brukscursor.getInt(0);

            if (bruk > 0) {
                String snittSQL = "SELECT COUNT(*) FROM " + TABLE_DATOER;
                Cursor snittcursor = db.rawQuery(snittSQL, null);
                snittcursor.moveToFirst();
                int antDager = snittcursor.getInt(0);
                snittcursor.close();
                
                brukPerTime[i] = ( (double) bruk / (double) antDager);
            }
            else {
                brukPerTime[i] = 0;
            }
            brukscursor.close();
        }

        db.close();
        return brukPerTime;
    }


    // Returnerer bruk for en måned
    public int getBrukOnMaaned(Calendar cal) {
        SQLiteDatabase db = this.getWritableDatabase();

        String dato = DatoTidFormatter.getAarMaanedDato(cal);
        int bruk = 0;

        String sql = "SELECT SUM(" + KEY_ANTALL + ") FROM " + TABLE_DATOER;
        sql += " WHERE " + KEY_DATO + " LIKE '" + dato + "-%'";
        //Log.d(MYDEBUG, "********\n getBrukOnMaaned sql = " + sql + "\n*******");

        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst())
            bruk = cursor.getInt(0);

        cursor.close();


        db.close();
        return bruk;
    }

    //Returnerer detaljert bruk for en måned
    public int[] getBrukDetaljerOnMaaned (Calendar cal) {
        SQLiteDatabase db = this.getWritableDatabase();

        String dato = DatoTidFormatter.getAarMaanedDato(cal);
        int antDagerIMaaned = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int[] detaljbruk = new int[antDagerIMaaned];

        for (int i = 0; i < antDagerIMaaned; i++) {
            String sql = "SELECT SUM(" + KEY_ANTALL + ") FROM " + TABLE_DATOER;
            sql += " WHERE " + KEY_DATO + " LIKE '" + dato + "-";
            if ((i+1) < 10)
                sql += "0";
            sql += (i+1) + "'";

            //Log.d(MYDEBUG, "********\n getBrukDetaljerOnMaaned sql = " + sql + "\n*******");
            Cursor cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst())
                detaljbruk[i] = cursor.getInt(0);

            else
                detaljbruk[i] = 0;

            cursor.close();
        }

        db.close();
        return detaljbruk;
    }

    // Returnerer snittverdier for datoer basert på datoen i måneden
    public double[] getBrukSnittOnMaaned() {
        SQLiteDatabase db = this.getWritableDatabase();

        double[] snittPerDato = new double[31];

        for (int i = 0; i < snittPerDato.length; i++) {
            String sumForDatoSQL = "SELECT SUM(" + KEY_ANTALL + ") FROM " + TABLE_DATOER;
            sumForDatoSQL += " WHERE " + KEY_DATO + " LIKE '%-%-";
            if ((i+1) < 10)
                sumForDatoSQL += "0";
            sumForDatoSQL += (i+1) + "'";

            Cursor sumcursor = db.rawQuery(sumForDatoSQL, null);
            int sum = 0;
            if (sumcursor.moveToFirst())
                sum = sumcursor.getInt(0);

            sumcursor.close();

            String antDagerSQL = "SELECT COUNT(*) FROM " + TABLE_DATOER;
            antDagerSQL += " WHERE " + KEY_DATO + " LIKE '%-%-";
            if ((i+1) < 10)
                antDagerSQL += "0";
            antDagerSQL += (i+1) + "'";

            Cursor antcursor = db.rawQuery(antDagerSQL, null);
            antcursor.moveToFirst();
            int antDagerMedDato = antcursor.getInt(0);
            antcursor.close();

            if (sum > 0 && antDagerMedDato > 0) {
                snittPerDato[i] = ((double) sum / (double) antDagerMedDato);
            }
            else {
                snittPerDato[i] = 0;
            }
        }

        db.close();
        return snittPerDato;
    }

    // Returnerer bruk for et år
    public int getBrukOnAar(Calendar cal) {
        SQLiteDatabase db = this.getWritableDatabase();

        int bruk = 0;

        String sql = "SELECT SUM(" + KEY_ANTALL + ") FROM " + TABLE_DATOER;
        sql += " WHERE " + KEY_DATO + " LIKE '" + cal.get(Calendar.YEAR) + "-%'";
        //Log.d(MYDEBUG, "********\n getBrukOnAar sql = " + sql + "\n*******");

        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst())
            bruk = cursor.getInt(0);

        cursor.close();

        db.close();
        return bruk;
    }


    //Returnerer detaljert bruk for et år
    public int[] getBrukDetaljerOnAar (Calendar cal) {
        SQLiteDatabase db = this.getWritableDatabase();

        int[] detaljbruk = new int[12];

        for (int i = 0; i < detaljbruk.length; i++) {
            String sql = "SELECT SUM(" + KEY_ANTALL + ") FROM " + TABLE_DATOER;
            sql += " WHERE " + KEY_DATO + " LIKE '" + cal.get(Calendar.YEAR) + "-";
            if ((i+1) < 10)
                sql += "0";
            sql += (i+1) + "-%'";

            //Log.d(MYDEBUG, "********\n getBrukDetaljerOnAar sql = " + sql + "\n*******");
            Cursor cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst())
                detaljbruk[i] = cursor.getInt(0);

            else
                detaljbruk[i] = 0;

            cursor.close();
        }

        db.close();
        return detaljbruk;
    }

    // Returnerer snittverdier for bruk per måned basert på måneden
    public double[] getBrukSnittOnAar() {
        SQLiteDatabase db = this.getWritableDatabase();

        double[] snittPerMaaned = new double[]{0,0,0,0,0,0,0,0,0,0,0,0};
        int aarStart;
        int maanedStart;

        Calendar cal = Calendar.getInstance();
        int aarNaa = cal.get(Calendar.YEAR);
        int maanedNaa = cal.get(Calendar.MONTH);

        // Henter den tidligst registrerte datoen
        String forsteDatoSQL = "SELECT " + KEY_DATO + " FROM " + TABLE_DATOER;
        forsteDatoSQL += " ORDER BY " + KEY_DATO + " ASC LIMIT 1";

        Cursor forsteCursor = db.rawQuery(forsteDatoSQL, null);
        if (forsteCursor.moveToFirst()) {
            String forsteDatoString = forsteCursor.getString(0);
            String[] forsteDatoStringArr = forsteDatoString.split("-");

            aarStart = Integer.valueOf(forsteDatoStringArr[0]);
            maanedStart = Integer.valueOf(forsteDatoStringArr[1])-1;
            forsteCursor.close();
        }
        // Returnerer array med nullverdier hvis ingen datoer er registrert
        else {
            forsteCursor.close();
            db.close();
            return snittPerMaaned;
        }

        // Henter total bruk for hver måned uavhengig av år
        int[] brukPerMaaned = new int[12];

        for (int i = 0; i < brukPerMaaned.length; i++) {
            String brukSQL = "SELECT SUM(" + KEY_ANTALL + ") FROM " + TABLE_DATOER;
            brukSQL += " WHERE " + KEY_DATO + " LIKE '%-";
            if ((i + 1) < 10)
                brukSQL += "0";
            brukSQL += (i + 1) + "-%'";

            Cursor cursor = db.rawQuery(brukSQL, null);

            if (cursor.moveToFirst())
                brukPerMaaned[i] = cursor.getInt(0);

            else
                brukPerMaaned[i] = 0;

            cursor.close();
        }


        // Sjekker om året nå er året registrering startet, for da kan jeg droppe mer avansert sjekking
        if (aarNaa - aarStart == 0) {
            for (int i = 0; i < snittPerMaaned.length; i++) {
                snittPerMaaned[i] = brukPerMaaned[i];
                //Log.d(MYDEBUG, "********\n Bare ett år registrert \n*******");
            }
        }
        else {
            int antMndDifferanse = ((aarNaa - aarStart) * 12) + (maanedNaa - maanedStart) + 1;
            //Log.d(MYDEBUG, "********\n Start = "+maanedStart+"\n NÅ = "+ maanedNaa+" \n*******");

            for (int i = 0; i < snittPerMaaned.length; i++) {
                if (brukPerMaaned[i] > 0) {
                    // Finner ut hvor mange forekomster som har vært av gjeldende måned fra første registrering til nå
                    double forekomster;
                    if (i >= maanedStart && i <= maanedNaa) {
                        forekomster = 1 + Math.floor(antMndDifferanse / 12);
                    }
                    else {
                        forekomster = Math.floor(antMndDifferanse / 12);
                    }

                    //Log.d(MYDEBUG, "********\n snittpermaaned[" + i + "], \n differanse = " + antMndDifferanse + "\nforekomster = " + forekomster + " \n*******");
                    snittPerMaaned[i] = (double) brukPerMaaned[i] / forekomster;
                }
                else {
                    snittPerMaaned[i] = 0;
                }
            }
        }

        db.close();
        return snittPerMaaned;
    }

    // Returnerer bruk for en uke
    public int getBrukOnUke(Calendar cal) {
        SQLiteDatabase db = this.getWritableDatabase();

        int bruk = 0;

        //Henter mandagen i uka
        Calendar ukedag = DatoTidFormatter.getMandagIUke(cal);

        for (int i = 0; i < 7; i++) {
            bruk += getBrukOnDag(ukedag, db);
            ukedag.add(Calendar.DAY_OF_MONTH, 1);
        }

        db.close();
        return bruk;
    }

    //Returnerer detaljert bruk for en uke
    public int[] getBrukDetaljerOnUke (Calendar cal) {
        SQLiteDatabase db = this.getWritableDatabase();

        int[] detaljbruk = new int[7];

        //Henter mandagen i uka
        Calendar ukedag = DatoTidFormatter.getMandagIUke(cal);

        for (int i = 0; i < 7; i++) {
            detaljbruk[i] = getBrukOnDag(ukedag, db);
            ukedag.add(Calendar.DAY_OF_MONTH, 1);
            //Log.d(MYDEBUG, "********\n detaljbruk[" + i + "] = " + detaljbruk[i] + " \n*******");
        }

        db.close();
        return detaljbruk;
    }

    // Returnerer snittbruk for dager basert på hvilken ukedag det er
    public double[] getBrukSnittOnUke() {
        SQLiteDatabase db = this.getWritableDatabase();

        double[] brukPerUkedag = new double[7];
        int[] dagForekomster = new int[]{0,0,0,0,0,0,0};
        int[] antRegistreringerPerDag = new int[]{0,0,0,0,0,0,0};

        String sql = "Select " + KEY_DATO + ", " + KEY_ANTALL + " FROM " + TABLE_DATOER;

        Cursor altcursor = db.rawQuery(sql, null);

        if (altcursor.moveToFirst()) {
            do {
                int dagen = DatoTidFormatter.getUkedag(altcursor.getString(0));
                int bruk = altcursor.getInt(1);

                dagForekomster[dagen]++;
                antRegistreringerPerDag[dagen] += bruk;

            } while (altcursor.moveToNext());
        }

        for (int i=0; i < brukPerUkedag.length; i++) {
            if (dagForekomster[i] > 0) {
                brukPerUkedag[i] = ((double) antRegistreringerPerDag[i] / (double) dagForekomster[i]);
            }
            else {
                brukPerUkedag[i] = 0;
            }
        }

        db.close();
        return brukPerUkedag;
    }
}
