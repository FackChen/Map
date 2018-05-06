package com.example.map;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientationListener implements SensorEventListener {
    private SensorManager sensorManager;
    private Context context;
    private Sensor sensor;// 传感器
    private OnOrientationListener onOrientationListener;
    private float lastX;

    public OrientationListener(Context context) {
        this.context = context;
    }

    /*
    开启监听
     */
    @SuppressWarnings("deprecation")
    public void start() {
        //获得系统服务
        sensorManager=(SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager!=null){
            sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);//获得方向传感器
        }
        if(sensor!=null){
            sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_UI);//注册监听器
        }
    }

    /*
    结束监听
     */
    public void stop() {
        sensorManager.unregisterListener(this);//停止定位
    }

    /*
    方向发生改变时
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        //如果事件返回的类型是方向传感器
        if(event.sensor.getType()==Sensor.TYPE_ORIENTATION){
            float x=event.values[SensorManager.DATA_X];
            //如果变化大于一度
            if(Math.abs(x-lastX)>1.0){
                //通知主界面进行回调
                if(onOrientationListener!=null){
                    onOrientationListener.onOrientationChanged(x);
                }
            }
            lastX=x;
        }
    }

    public void setOnOrientationListener(OnOrientationListener onOrientationListener) {
        this.onOrientationListener = onOrientationListener;
    }

    public interface OnOrientationListener{
        void onOrientationChanged(float x);
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {

    }
}
