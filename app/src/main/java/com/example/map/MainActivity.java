package com.example.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public LocationClient locationClient;
    private BaiduMap baiduMap;
    MapStatusUpdate update;
    private boolean ifFirst=true;
    private String locationDescribe;
    private OrientationListener orientationListener;

    private MapView mapView;
    public TextView showMes;
    private DrawerLayout drawerLayout;

    private float currentX;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationClient=new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());//在调用布局前初始化

        setContentView(R.layout.activity_main);

        mapView=findViewById(R.id.baiduMapView);
        baiduMap=mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        ImageView restoreButton=findViewById(R.id.restore);
        restoreButton.setOnClickListener(this);

        ImageView drawerButton=findViewById(R.id.openDrawer);
        drawerButton.setOnClickListener(this);

        //授权
        List<String> permissionList=new ArrayList<>();//权限列表
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions=permissionList.toArray(new String[permissionList.size()]);//权限组
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);//授权
        }else{
            requestLocation();
        }

        //侧滑布局
        drawerLayout=findViewById(R.id.drawer_layout);
        NavigationView navigationView=findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.normal:
                        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                        break;
                    case R.id.satellite:
                        baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                        break;
                    case R.id.traffic:
                        if(baiduMap.isTrafficEnabled()){
                            baiduMap.setTrafficEnabled(false);
                        }else{
                            baiduMap.setTrafficEnabled(true);
                        }
                        break;
                    case R.id.heatMap:
                        if(baiduMap.isBaiduHeatMapEnabled()){
                            baiduMap.setBaiduHeatMapEnabled(false);
                        }else{
                            baiduMap.setBaiduHeatMapEnabled(true);
                        }
                        break;
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }

    /*
    点击事件
     */
    public void onClick(View v){
        switch (v.getId()){
            case R.id.restore:
                baiduMap.animateMapStatus(update);
                Toast.makeText(this,locationDescribe,Toast.LENGTH_SHORT).show();
                break;
            case R.id.openDrawer:
                drawerLayout.openDrawer(GravityCompat.END);
                break;
        }
    }

    /*
    请求定位
     */
    private void requestLocation(){
        initLocation();
        locationClient.start();//启动SDK
    }

    /*
    重新定位
     */
    private void initLocation(){
        LocationClientOption option=new LocationClientOption();
        option.setScanSpan(1000);
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);//需要获取详细的位置信息
        option.setIsNeedLocationDescribe(true);
        locationClient.setLocOption(option);
        orientationListener=new OrientationListener(this);
        orientationListener.setOnOrientationListener(new OrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                currentX=x;
            }
        });
    }

    /*
    授权结果
     */
    public void onRequestPermissionResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"授权失败",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else{
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
                default:
        }
    }

    /*
    移动到当前位置
     */
    private void navigationTo(BDLocation location){
        if(ifFirst){
            LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());//获得当前经纬度
            update= MapStatusUpdateFactory.newLatLngZoom(latLng,20f);//设置经纬度及精度
            baiduMap.animateMapStatus(update);//更新
            ifFirst=false;
        }
        //在地图上显示自己的位置
        MyLocationData.Builder locationBuilder=new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        locationBuilder.direction(currentX);
        baiduMap.setMyLocationData(locationBuilder.build());
        MyLocationConfiguration configuration=new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,true,null);
        baiduMap.setMyLocationConfiguration(configuration);
        orientationListener.start();
    }

    /*
    监听器
     */
    public class MyLocationListener implements BDLocationListener{
        public void onReceiveLocation(final BDLocation location){
            if(location.getLocType()==BDLocation.TypeNetWorkLocation||location.getLocType()==BDLocation.TypeGpsLocation){
                navigationTo(location);
            }
            locationDescribe=location.getLocationDescribe();//描述周围信息

            //测试用
            double latitude=location.getLatitude();
            double longitude=location.getLongitude();
            String coordinate=latitude+"   "+longitude;
            showMes=findViewById(R.id.show_message);
            showMes.setText(coordinate);
        }
    }

    protected void onResume(){
        super.onResume();
        mapView.onResume();
    }

    protected void onPause(){
        super.onPause();
        mapView.onPause();
    }

    protected void onStop(){
        super.onStop();
        orientationListener.stop();
        locationClient.stop();
        baiduMap.setMyLocationEnabled(false);
    }
    /*
    结束定位
     */
    protected void onDestroy(){
        super.onDestroy();
        mapView.onDestroy();
    }
}
