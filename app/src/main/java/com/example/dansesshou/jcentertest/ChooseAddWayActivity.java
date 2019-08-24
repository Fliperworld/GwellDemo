package com.example.dansesshou.jcentertest;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

import com.gwelldemo.R;
import com.jwkj.smartlinkdemo.AddDeviceActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChooseAddWayActivity extends AppCompatActivity {
    private String userId;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_add_way);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        userId = intent.getStringExtra(LoginActivity.USERID);
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE}, 102);
    }

    @OnClick(R.id.smartink_btn)
    public void onSmartLinkClick() {
        Intent addDevice = new Intent(this, AddDeviceActivity.class);
        addDevice.putExtra(LoginActivity.USERID, userId);
        startActivity(addDevice);
    }

    @OnClick(R.id.scan_btn)
    public void onScanAddClick() {
        Intent scanAdd = new Intent(this, ScanActivity.class);
        startActivity(scanAdd);
    }
}
