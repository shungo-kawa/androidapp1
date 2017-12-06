package jp.ynu.kawakami.myapplication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.jins_jp.meme.MemeConnectListener;
import com.jins_jp.meme.MemeLib;
import com.jins_jp.meme.MemeRealtimeData;
import com.jins_jp.meme.MemeRealtimeListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends Activity implements SensorEventListener {
    private static final String APP_ID = "746174217918182";
    private static final String APP_SECRET = "uxk9nf8hustc70p0k3eoocqbl5l7ztin";

    private TextView statusLabel;
    private Button connectButton;
    private MemeLib memeLib;

    SensorManager manager;
    Sensor mAcc;
    Sensor mGyro;
    Sensor mMg;
    TextView xTextView;
    TextView yTextView;
    TextView zTextView;
    TextView xgTextView;
    TextView ygTextView;
    TextView zgTextView;
    TextView xmgTextView;
    TextView ymgTextView;
    TextView zmgTextView;
    TextView memexTextView;
    TextView memeyTextView;
    TextView memezTextView;
    TextView rollTextView;
    TextView pitchTextView;
    TextView yawTextView;
    TextView speed;
    TextView strength;
    TextView dateTextView;
    TextView pitch;
    TextView roll;
    TextView yaw;
    //FOR ORIENTATION
    private static final int MATRIX_SIZE = 16;
    float[] in = new float[MATRIX_SIZE];
    float[] out = new float[MATRIX_SIZE];
    float[] I = new float[MATRIX_SIZE];

    float[] o = new float[3];
    float[] m = new float[3];
    float[] a = new float[3];

    //FOR DB
    MyOpenHelper helper = new MyOpenHelper(this);
    int start = 0;
    boolean display1 = false;
    boolean display2 = false;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd HH:mm:ss.SSS");

    final private MemeConnectListener memeConnectListener = new MemeConnectListener() {
        @Override
        public void memeConnectCallback(boolean b) {
            //describe actions after connection with JINS MEME
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    changeViewStatus(true);
                }
            });
        }

        @Override
        public void memeDisconnectCallback() {
            //describe actions after disconnection from JINS MEME
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    changeViewStatus(false);
                    Toast.makeText(MainActivity.this, "DISCONNECTED", Toast.LENGTH_LONG).show();
                }
            });
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        xTextView = (TextView) findViewById(R.id.xValue);
        yTextView = (TextView) findViewById(R.id.yValue);
        zTextView = (TextView) findViewById(R.id.zValue);
        xgTextView = (TextView) findViewById(R.id.xValue_gyro);
        ygTextView = (TextView) findViewById(R.id.yValue_gyro);
        zgTextView = (TextView) findViewById(R.id.zValue_gyro);
        xmgTextView = (TextView) findViewById(R.id.xValue_gyro_raw);
        ymgTextView = (TextView) findViewById(R.id.yValue_gyro_raw);
        zmgTextView = (TextView) findViewById(R.id.zValue_gyro_raw);
        memexTextView = (TextView) findViewById(R.id.meme_accX);
        memeyTextView = (TextView) findViewById(R.id.meme_accY);
        memezTextView = (TextView) findViewById(R.id.meme_accZ);
        rollTextView = (TextView) findViewById(R.id.meme_roll);
        pitchTextView = (TextView) findViewById(R.id.meme_pitch);
        yawTextView = (TextView) findViewById(R.id.meme_yaw);
        speed = (TextView) findViewById(R.id.speed);
        strength = (TextView) findViewById(R.id.strength);
         pitch = (TextView)findViewById(R.id.pitch);
         roll = (TextView)findViewById(R.id.roll);
         yaw = (TextView)findViewById(R.id.yaw);

        dateTextView = (TextView) findViewById(R.id.date);

        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAcc = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMg = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        final Button startbutton = (Button) findViewById(R.id.start);
        startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start == 0) {
                    start = 1;
                    startbutton.setText(String.valueOf("停止"));
                } else {
                    start = 0;
                    startbutton.setText(String.valueOf("開始"));
                }
            }
        });

        Button deleteAllButton = (Button) findViewById(R.id.deleteAll);
        deleteAllButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SQLiteDatabase db = helper.getWritableDatabase();
                db.delete("acc", null, null);
                db.delete("gyro", null, null);
                db.delete("mgn", null, null);
                db.delete("jins", null, null);
                db.close();
                Log.d("test", "deleted");
            }
        });

        Button pulldata = (Button) findViewById(R.id.pulldata);
        pulldata.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    copyDb2Sd(MainActivity.this, "sensorDB");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dbg("test");

            }
        });


        final CompoundButton sw1 = (Switch) findViewById(R.id.android_switch);
        final CompoundButton sw2 = (Switch) findViewById(R.id.meme_switch);
        sw1.setChecked(true);
        sw2.setChecked(true);

        sw1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                display1 = isChecked;
                // 状態が変更された
                Toast.makeText(MainActivity.this, "isChecked : " + isChecked, Toast.LENGTH_SHORT).show();
            }
        });

        sw2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                display2 = isChecked;
                // 状態が変更された
                Toast.makeText(MainActivity.this, "isChecked : " + isChecked, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        manager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_UI);
        manager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_UI);
        manager.registerListener(this, mMg, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        long currentTimeMillis = System.currentTimeMillis();
        Date date = new Date(currentTimeMillis);
        String time = simpleDateFormat.format(date);
        String timestamp = String.valueOf(event.timestamp);
        String x = String.valueOf(event.values[0]);
        String y = String.valueOf(event.values[1]);
        String z = String.valueOf(event.values[2]);
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                a = event.values.clone();


                if (display1) {
                    xTextView.setText(x);
                    yTextView.setText(y);
                    zTextView.setText(z);
                    dateTextView.setText(time);
                }

                //db入力処理
                if (start == 1) {

                    final SQLiteDatabase db = helper.getWritableDatabase();
                    ContentValues insertValues = new ContentValues();
                    insertValues.put("timestamp", timestamp);
                    insertValues.put("time", time);
                    insertValues.put("x", x);
                    insertValues.put("y", y);
                    insertValues.put("z", z);
                    long id = db.insert("acc", time, insertValues);

                    db.close();
                }
                break;
            case Sensor.TYPE_GYROSCOPE:


                if (display1) {
                    xgTextView.setText(x);
                    ygTextView.setText(y);
                    zgTextView.setText(z);
                    dateTextView.setText(time);
                }
                //db入力処理
                if (start == 1) {

                    final SQLiteDatabase db = helper.getWritableDatabase();
                    ContentValues insertValues = new ContentValues();
                    insertValues.put("timestamp", timestamp);
                    insertValues.put("time", time);
                    insertValues.put("x", x);
                    insertValues.put("y", y);
                    insertValues.put("z", z);
                    long id = db.insert("gyro", time, insertValues);

                    db.close();
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                m = event.values.clone();

//
//                Log.d("SensorTest",
//                        String.format("data %d : %d,%d",
//                                event.timestamp,System.nanoTime(),System.currentTimeMillis()));
                if (display1) {
                    xmgTextView.setText(x);
                    ymgTextView.setText(y);
                    zmgTextView.setText(z);
                    dateTextView.setText(time);
                }
                //db入力処理
                if (start == 1) {

                    final SQLiteDatabase db = helper.getWritableDatabase();
                    ContentValues insertValues = new ContentValues();
                    insertValues.put("timestamp", timestamp);
                    insertValues.put("time", time);
                    insertValues.put("x", x);
                    insertValues.put("y", y);
                    insertValues.put("z", z);
                    long id = db.insert("mgn", time, insertValues);

                    db.close();
                }
                break;
        }

        if( a != null && m != null ) {
            SensorManager.getRotationMatrix(in, I, a, m);
            SensorManager.remapCoordinateSystem(in, SensorManager.AXIS_X, SensorManager.AXIS_Z, out);
            SensorManager.getOrientation(out, o);

            pitch.setText(String.valueOf((o[1] * 180 / 3.1415 )));
            roll.setText(String.valueOf((o[0] * 180 / 3.1415 )));
            yaw.setText(String.valueOf((o[2] * 180 / 3.1415 )));
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    /**
     * DBファイルをSDカードにコピーする
     * AndroidManifest.xmlにWRITE_EXTERNAL_STORAGEを設定すること
     *
     * @param //Context context メソッド呼び出し元(Activity等)のContext
     * @param //String  dbName コピー元となるデータベースファイル名
     * @return コピーに成功した場合true
     * @throws //IOException なんかエラーが起きた場合にthrow
     */

    public static boolean copyDb2Sd(Context context, String dbName) throws IOException {

        final String TAG = "copyDb2Sd";

//保存先(SDカード)のディレクトリを確保
        List<String> path_1 = getSdCardFilesDirPathListForLollipop(context);
        String pathSd = new StringBuilder()
                .append(Environment.getExternalStorageDirectory().getPath())
                .append("/")
                .append(context.getPackageName())
                .toString();
        pathSd = path_1.get(0);
        File filePathToSaved = new File(pathSd);
        Log.d("", pathSd);
        if (!filePathToSaved.exists() && !filePathToSaved.mkdirs()) {
            throw new IOException("FAILED_TO_CREATE_PATH_ON_SD");
        }

        final String fileDb = context.getDatabasePath(dbName).getPath();
        final String fileSd = new StringBuilder()
                .append(pathSd)
                .append("/")
                .append(dbName)
                .append((new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date()))
                .append(".db")
                .toString();

        Log.i(TAG, "copy from(DB): " + fileDb);
        Log.i(TAG, "copy to(SD) : " + fileSd);

        FileChannel channelSource = new FileInputStream(fileDb).getChannel();
        FileChannel channelTarget = new FileOutputStream(fileSd).getChannel();

        channelSource.transferTo(0, channelSource.size(), channelTarget);

        channelSource.close();
        channelTarget.close();

        return true;
    }

    private static void dbg(String msg) {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        String className = stack[1].getClassName();
        String method = stack[1].getMethodName();
        int line = stack[1].getLineNumber();
        StringBuilder buf = new StringBuilder(60);
        buf.append(msg)
                .append("[")
                // sample.package.ClassName.methodName:1234
                .append(className).append(".").append(method).append(":").append(line)
                .append("]");
        android.util.Log.d("tag", buf.toString());
    }

    /**
     * SDカードのfilesディレクトリパスのリストを取得する。
     * Android5.0以上対応。
     *
     * @param context
     * @return SDカードのfilesディレクトリパスのリスト
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static List<String> getSdCardFilesDirPathListForLollipop(Context context) {
        List<String> sdCardFilesDirPathList = new ArrayList<>();

        // getExternalFilesDirsはAndroid4.4から利用できるAPI。
        // filesディレクトリのリストを取得できる。
        File[] dirArr = context.getExternalFilesDirs(null);

        for (File dir : dirArr) {
            if (dir != null) {
                String path = dir.getAbsolutePath();

                // isExternalStorageRemovableはAndroid5.0から利用できるAPI。
                // 取り外し可能かどうか（SDカードかどうか）を判定している。
                if (Environment.isExternalStorageRemovable(dir)) {

                    // 取り外し可能であればSDカード。
                    if (!sdCardFilesDirPathList.contains(path)) {
                        sdCardFilesDirPathList.add(path);
                    }

                } else {
                    // 取り外し不可能であれば内部ストレージ。
                }
            }
        }
        return sdCardFilesDirPathList;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        //Sets MemeConnectListener to get connection result.
        memeLib.setMemeConnectListener(memeConnectListener);

        changeViewStatus(memeLib.isConnected());

        //Starts receiving realtime data if MEME is connected
        if (memeLib.isConnected()) {
            memeLib.startDataReport(memeRealtimeListener);
        }
    }

    private void init() {
        //Authentication and authorization of App and SDK
        MemeLib.setAppClientID(getApplicationContext(), APP_ID, APP_SECRET);
        memeLib = MemeLib.getInstance();

        statusLabel = (TextView) findViewById(R.id.status_label);

        connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (memeLib.isConnected()) {
                    memeLib.disconnect();
                } else {
                    Intent intent = new Intent(MainActivity.this, ConnectActivity.class);
                    startActivity(intent);
                }
            }
        });

        changeViewStatus(memeLib.isConnected());
    }


    private void changeViewStatus(boolean connected) {
        if (connected) {
            statusLabel.setText(R.string.connected);
            statusLabel.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
            connectButton.setBackground(ContextCompat.getDrawable(this, R.drawable.disconnect_button));
        } else {
            statusLabel.setText(R.string.not_connected);
            statusLabel.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
            connectButton.setBackground(ContextCompat.getDrawable(this, R.drawable.connect_button));
        }
    }

    private final MemeRealtimeListener memeRealtimeListener = new MemeRealtimeListener() {
        @Override
        public void memeRealtimeCallback(final MemeRealtimeData memeRealtimeData) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateMemeData(memeRealtimeData);
                }
            });
        }
    };

    private void updateMemeData(MemeRealtimeData d) {
//
//        // for blink
        long currentTimeMillis = System.currentTimeMillis();
        Date date = new Date(currentTimeMillis);
        String time = simpleDateFormat.format(date);
        String blink_speed = String.valueOf(d.getBlinkSpeed());
        String blink_strength = String.valueOf(d.getBlinkStrength());
        String memeX = String.valueOf(d.getAccX());
        String memeY = String.valueOf(d.getAccY());
        String memeZ = String.valueOf(d.getAccZ());
        String roll = String.valueOf(d.getRoll());
        String pitch = String.valueOf(d.getPitch());
        String yaw = String.valueOf(d.getYaw());

        Log.d("test", "MEME DATA GET");
        if (display2) {
            speed.setText(blink_speed);
            strength.setText(blink_strength);
            memexTextView.setText(memeX);
            memeyTextView.setText(memeY);
            memezTextView.setText(memeZ);
            rollTextView.setText(roll);
            pitchTextView.setText(pitch);
            yawTextView.setText(yaw);
        }
//        Log.d("MainActivity", "time:" + time);
//        Log.d("MainActivity", "Blink Speed:" + blink_speed);
//        Log.d("MainActivity", "Blink Strength:" + blink_strength);

        if (start == 1) {

            final SQLiteDatabase db = helper.getWritableDatabase();
            Log.d("test", "MEME DATA UPDATED");


            ContentValues insertValues = new ContentValues();
            insertValues.put("time", time);
            insertValues.put("x", memeX);
            insertValues.put("y", memeY);
            insertValues.put("z", memeZ);
            insertValues.put("roll", roll);
            insertValues.put("pitch", pitch);
            insertValues.put("yaw", yaw);
            insertValues.put("speed", blink_speed);
            insertValues.put("strength", blink_strength);

            long id = db.insert("jins", time, insertValues);


            db.close();
        }
//        if (d.getBlinkSpeed() > 0) {
//            blinkImage.setVisibility(View.INVISIBLE);
//            blink();
//        }
//
//        // for body (Y axis rotation)
//        double radian = Math.atan2(d.getAccX(), d.getAccZ());
//        rotate(Math.toDegrees(-radian)); // for mirroring display(radian x -1)
//    }
//    private void blink(){
//        blinkView.seekTo(0);
//        blinkView.start();
//    }
//    private void rotate(double degree) {
//        int width = bodyImage.getDrawable().getBounds().width();
//        int height = bodyImage.getDrawable().getBounds().height();
//
//        Matrix matrix = new Matrix();
//        bodyImage.setScaleType(ImageView.ScaleType.MATRIX);
//        matrix.postRotate((float)degree, width/2, height/2);
//        matrix.postScale(0.5f, 0.5f);
//        bodyImage.setImageMatrix(matrix);
    }
}
