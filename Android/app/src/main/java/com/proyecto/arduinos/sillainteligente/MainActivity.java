package com.proyecto.arduinos.sillainteligente;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.beardedhen.androidbootstrap.BootstrapButton;

public class MainActivity extends AppCompatActivity {
    private BootstrapButton btnBluetooth;

    private static final String TAG = "Main Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.btnBluetooth = findViewById(R.id.btnBluetooth);
        this.btnBluetooth.setOnClickListener(btnHabilitarBluetooth);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private View.OnClickListener btnHabilitarBluetooth = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), ConectarBluetoothActivity.class);
            startActivity(intent);
        }
    };

    private void printLog(String mensaje) {
        Log.d(this.TAG, mensaje);
    }

}
