package com.ustc.wsn.mydataapp.activity;
/**
 * Created by halo on 2018/1/17.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nulana.NChart.NChartView;
import com.ustc.wsn.mydataapp.Listenter.TrackSensorListener;
import com.ustc.wsn.mydataapp.R;
import com.ustc.wsn.mydataapp.bean.cubeView.MyRender;
import com.ustc.wsn.mydataapp.service.TrackService;

import java.util.Timer;
import java.util.TimerTask;

public class ChartingDemoActivity extends Activity {
    private final String TAG = ChartingDemoActivity.class.toString();
    NChartView mNChartView;
    private RelativeLayout attLayout;
    MyRender myRender;

    TextView EulerxAxis;
    TextView EuleryAxis;
    TextView EulerzAxis;

    private float[] Euler = {0,0,0};

    TrackService track;
    private boolean threadDisable = false;
    private TrackSensorListener sensorListener;
    private SensorManager sm;
    private boolean ACCELERATOR_EXIST = false;
    private boolean GYROSCROPE_EXIST = false;
    private boolean MAGNETIC_EXIST = false;
    private int WindowSize;
    private int sampleInterval;
    private Sensor accelerator;
    private Sensor gyroscrope;
    private Sensor magnetic;
    private Toast t;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.nchart);
        mNChartView = (NChartView) findViewById(R.id.surface);
        mNChartView.getChart().setShouldAntialias(true);
        mNChartView.getChart().setShowFPS(true);

        attLayout = (RelativeLayout) findViewById(R.id.ncattView);
        initSensor();
        GLSurfaceView glView = new GLSurfaceView(this);
        myRender = new MyRender();
        glView.setRenderer(myRender);
        attLayout.addView(glView);

        EulerxAxis = (TextView) findViewById(R.id.value_x);
        EuleryAxis = (TextView) findViewById(R.id.value_y);
        EulerzAxis = (TextView) findViewById(R.id.value_z);

        initSensor();
        if (ACCELERATOR_EXIST && GYROSCROPE_EXIST && MAGNETIC_EXIST) {
            WindowSize = sensorListener.windowSize * sensorListener.DurationWindow;
            sampleInterval = sensorListener.sampleInterval;
            loadView();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!threadDisable) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // do
                        Euler = sensorListener.readEuler();
                        myRender.updateEuler(Euler);
                    }
                }
            }).start();

            Timer timer1 = new Timer();
            timer1.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.sendMessage(handler.obtainMessage());
                }
            }, 0, 50);
        }
    }

    private Handler handler = new Handler() {
        @Override
        //定时更新图表
        public void handleMessage(Message msg) {
            EulerxAxis.setText(String.valueOf((int)(Euler[0]/Math.PI*180)));
            EuleryAxis.setText(String.valueOf((int)(Euler[1]/Math.PI*180)));
            EulerzAxis.setText(String.valueOf((int)(Euler[2]/Math.PI*180)));
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.path_params_setting, menu);
        //setIconEnable(menu,true);
        return true;
    }

    private void showHelpDialog() {
        Dialog helpDialog = new Dialog(this);
        helpDialog.setCancelable(true);
        helpDialog.setCanceledOnTouchOutside(true);

        helpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        helpDialog.setContentView(getLayoutInflater().inflate(R.layout.help, null));

        helpDialog.show();
    }

    private void calibrate_state() {
        Intent intent = new Intent(this, CalibrateStateActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings_help:
                showHelpDialog();
                return true;
            case R.id.calibrate_state:
                calibrate_state();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadView() {
        // Paste your license key here.
        mNChartView.getChart().setLicenseKey("BLZHEYklLSsef/cFzOar6FI2jJyTFUPCZtg+WwSIMHH+AVD3JFv22uK8YebZfPWm10IZvA59H5H/sQOWiGUbfnSrdS7Oofs+gwe1gGY0ScOr0RutFoil1vbib42bA37rIeXovoJvTZqZtt1gjEFgVU2WpnuwiZrs4huwjXP0S+9ITRbblPJPd3DM4L8ruon8OMYpt5JW9C+9sfJMaeDtzEBT6wXoBPVbbcKy2qNPwtuy1BwMwmvlxhy2CjVgPVpyKcgaRo4V51Swqk19pdJ+yin/vQP2zXz02Vq9BhCHCXZEVYiURx4+0c5luJWVJLRd7WlQtyqZ1PBRI5HsI39weHoam/80qjBNUkPBLAg9Eh2EXuHYOCPj66ruJl9pkhQJ4UsxTRe5O0hnsJu1Mes9yG+AKL8ovwcDQhsCZNBJG/6OzmHlEF68IBAnPQPSraHqBBH3Xw0a5dIJq3KS/sgpTJ5IhuViFTQ/eBvYTSiJDi/yjJvKsrJn/3RDE6FdzvuYwkbWzWOj9N6HBUyGyp7NprDDl84R/KbInLYeELf0aX1KNJju5nSHyNw6QDi2dwZU9yJlhtz5sLmS7DFaspN+vonMcGp8MIoNww1ja2HfIr8HMICTf0XsZ/YlKZukmY+EJbInWUmR+talhQHdZBGU2hUYmBGfGhMrH4hSiKBAi38=");
        track = new TrackService(true, mNChartView, this, WindowSize);
        track.initView(0);
        track.updateData(0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                float[][] position;
                int i = 1;
                while (!threadDisable) {
                    try {
                        Thread.sleep(WindowSize * sampleInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (sensorListener.ifNewPath()) {
                        sensorListener.ifNewPath = false;
                        position = sensorListener.getPosition();
                        int mark = sensorListener.getPosition_mark();
                        if (position != null) {
                            track.setPosition(position, mark);
                            track.updateData(i);
                        }
                    }
                }
            }
        }).start();
    }

    @SuppressLint("InlinedApi")
    public void initSensor() {
        Log.d("Sensor", "InitSensor Over");
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerator = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        float accMax = accelerator.getMaximumRange();
        Log.d(TAG, "accMaxRange\t" + accMax);
        if (accelerator != null) {
            ACCELERATOR_EXIST = true;
        } else {
            t = Toast.makeText(this, "您的手机不支持加速度计", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }
        gyroscrope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        float gyroMax = gyroscrope.getMaximumRange();
        Log.d(TAG, "gyroMaxRange\t" + gyroMax);
        if (gyroscrope != null) {
            GYROSCROPE_EXIST = true;
        } else {
            t = Toast.makeText(this, "您的手机不支持陀螺仪", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }
        magnetic = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        float magMax = magnetic.getMaximumRange();
        Log.d(TAG, "magMaxRange\t" + magMax);
        if (magnetic != null) {
            MAGNETIC_EXIST = true;
        } else {
            t = Toast.makeText(this, "您的手机不支持电子罗盘", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }
        sensorListener = new TrackSensorListener(accMax, gyroMax, magMax, true);
        if (ACCELERATOR_EXIST) {
            sm.registerListener(sensorListener, accelerator, SensorManager.SENSOR_DELAY_GAME);
        }
        if (GYROSCROPE_EXIST) {
            sm.registerListener(sensorListener, gyroscrope, SensorManager.SENSOR_DELAY_GAME);
        }
        if (MAGNETIC_EXIST) {
            sm.registerListener(sensorListener, magnetic, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        threadDisable = true;
        if (track != null) {
            track.stopSelf();
        }
        sensorListener.closeSensorThread();
        sm.unregisterListener(sensorListener);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "bengin touch time \t" + System.currentTimeMillis());
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;

            default:
                break;
        }
        return super.onTouchEvent(event);
    }
}
