package com.example.lbstest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public LocationClient mLocationClient;

    private TextView positionText;

    //显示地图
    private MapView mapView;

    //获取自己的位置
    private BaiduMap baiduMap;
    //防止多次调用animateMapStatus
    private boolean isFirstLocate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mapView = (MapView)findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        //开启让自己显示在地图上的功能
        baiduMap.setMyLocationEnabled(true);
        positionText = (TextView)findViewById(R.id.position_text_view);
        List<String> permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        //三次权限申请，如果有未通过的权限将放入数组中，重新进行一次性申请
        if(!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else {
            requestLocation();
        }
    }

    private void navigateTo(BDLocation location){
        if(isFirstLocate){
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocate = true;
        }
        //让自己显示在地图上。写在if外是因为if中的逻辑只需第一次移动到当前位置，后续位置应随自身位置改变而改变
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }


    //默认情况下只定位一次
    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(15000);
        //强制使用GPS
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);

        //显示详细信息
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    //管理MapView 及时释放资源
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    //关闭时要销毁防止严重消耗电量
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        mLocationClient.stop();
        //关闭显示个人功能
        baiduMap.setMyLocationEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length > 0){
                    for(int result :grantResults){
                        if(result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(this,"发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
                default:
        }
    }

    public class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(final BDLocation location) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    StringBuilder currentPosition = new StringBuilder();
//                    //显示文本上的详细信息
//                    currentPosition.append("纬度：").append(location.getLatitude()).append("\n");
//                    currentPosition.append("经度：").append(location.getLongitude()).append("\n");
//                    currentPosition.append("国家：").append(location.getCountry()).append("\n");
//                    currentPosition.append("省：").append(location.getProvince()).append("\n");
//                    currentPosition.append("市：").append(location.getCity()).append("\n");
//                    currentPosition.append("区：").append(location.getDistrict()).append("\n");
//                    currentPosition.append("街道：").append(location.getStreet()).append("\n");
//                    currentPosition.append("定位方式：");
//                    if(location.getLocType() == BDLocation.TypeGpsLocation){
//                        currentPosition.append("GPS");
//                    }else if(location.getLocType() == BDLocation.TypeNetWorkLocation){
//                        currentPosition.append("网络");
//                    }
//                    positionText.setText(currentPosition);

                    //显示个人在地图上的位置
                    if(location.getLocType() == BDLocation.TypeGpsLocation || location.getLocType() == BDLocation.TypeNetWorkLocation){
                        navigateTo(location);
                    }

                }
            });
        }
        public void onConnectHostSpotMessage(String s, int i){

        }

    }
}
