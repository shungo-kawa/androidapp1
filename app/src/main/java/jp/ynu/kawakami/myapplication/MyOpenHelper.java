package jp.ynu.kawakami.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by New account on 2017/11/28.
 */

public class MyOpenHelper extends SQLiteOpenHelper {

    public MyOpenHelper(Context context){
        super(context, "sensorDB", null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table acc(" + " time text not null," + "x text," + "y text," + "z text" + ");");
        db.execSQL("create table gyro("  + " time text not null," + "x text," + "y text," + "z text" + ");");
        db.execSQL("create table gyro_raw("  + " time text not null," + "x text," + "y text," + "z text" + ");");
        db.execSQL("create table jins(" + " time text not null," + "x text," + "y text," + "z text," + "roll text," + "pitch text," + "yaw text," + "speed text,"+ "strength text" + ");");
        Log.d("test","make db");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
