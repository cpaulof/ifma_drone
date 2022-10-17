package edu.ifma.ifmadrone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.log.DJILog;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LDMModule;
import dji.sdk.sdkmanager.LDMModuleType;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getName();
    private static final String[] REQUIRED_PERMISSION_LIST = new String[] {
            Manifest.permission.VIBRATE, // Gimbal rotation
            Manifest.permission.INTERNET, // API requests
            Manifest.permission.ACCESS_WIFI_STATE, // WIFI connected products
            Manifest.permission.ACCESS_COARSE_LOCATION, // Maps
            Manifest.permission.ACCESS_NETWORK_STATE, // WIFI connected products
            Manifest.permission.ACCESS_FINE_LOCATION, // Maps
            Manifest.permission.CHANGE_WIFI_STATE, // Changing between WIFI and USB connection
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // Log files
            Manifest.permission.BLUETOOTH, // Bluetooth connected products
            Manifest.permission.BLUETOOTH_ADMIN, // Bluetooth connected products
            Manifest.permission.READ_EXTERNAL_STORAGE, // Log files
            Manifest.permission.READ_PHONE_STATE, // Device UUID accessed upon registration
            Manifest.permission.RECORD_AUDIO // Speaker accessory
    };
    private static final int REQUEST_PERMISSION_CODE = 12345;

    private TextView productLabel;
    private TextView statusLabel;
    private Button btnOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusLabel = findViewById(R.id.statusLabel);
        productLabel = findViewById(R.id.productLabel);
        btnOpen = findViewById(R.id.btnOpen);

    }

    private void checkAndRequestPermissions() {
        // Check for permissions
        List<String> missingPermission = new ArrayList<>();
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions((Activity) this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }

    }

    public void registerApp(View view){

        statusLabel.setText("Registrando!");
        checkAndRequestPermissions();
    }

    public void openVideo(View view){
        Intent videoView = new Intent(this, VideoViewActivity.class);
        startActivity(videoView);
    }

    private void startSDKRegistration(){
        DJISDKManager.getInstance().getLDMManager().setModuleNetworkServiceEnabled(new LDMModule.Builder().moduleType(
                LDMModuleType.FIRMWARE_UPGRADE).enabled(false).build());

        DJISDKManager.getInstance().registerApp(this.getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
            @Override
            public void onRegister(DJIError djiError) {
                if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                    DJILog.e("App registration", DJISDKError.REGISTRATION_SUCCESS.getDescription());
                    DJISDKManager.getInstance().startConnectionToProduct();

                } else {
                    statusLabel.setText(djiError.getDescription());
                }
                Log.v(TAG, djiError.getDescription());
            }
            @Override
            public void onProductDisconnect() {
                Log.d(TAG, "onProductDisconnect");
            }
            @Override
            public void onProductConnect(BaseProduct baseProduct) {
                Log.d(TAG, String.format("onProductConnect newProduct:%s", baseProduct));
                statusLabel.setText("Produto Conectado!");
                productLabel.setText(baseProduct.getModel().getDisplayName());
                btnOpen.setEnabled(true);
            }

            @Override
            public void onProductChanged(BaseProduct baseProduct) {

            }

            @Override
            public void onComponentChange(BaseProduct.ComponentKey componentKey,
                                          BaseComponent oldComponent,
                                          BaseComponent newComponent) {
                Log.d(TAG,
                        String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                                componentKey,
                                oldComponent,
                                newComponent));
            }

            @Override
            public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {

            }

            @Override
            public void onDatabaseDownloadProgress(long current, long total) {

            }
        });
    }
}