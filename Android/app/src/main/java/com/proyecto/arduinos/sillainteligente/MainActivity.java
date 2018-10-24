package com.proyecto.arduinos.sillainteligente;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.beardedhen.androidbootstrap.BootstrapButton;

public class MainActivity extends AppCompatActivity {
    private String direccionMAC;
    private BootstrapButton btnBluetooth;
    private BootstrapButton btnSensores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.btnBluetooth = findViewById(R.id.btnBluetooth);
        this.btnSensores = findViewById(R.id.btnSensores);

        this.btnBluetooth.setOnClickListener(btnHabilitarBluetooth);
        this.btnSensores.setOnClickListener(btnManejarSensores);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        this.direccionMAC = extras!=null? extras.getString("MacBluetooth"):null;

        if(this.direccionMAC == null) {
            this.btnSensores.setEnabled(false);
        }
    }

    private View.OnClickListener btnHabilitarBluetooth = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), ConectarBluetoothActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener btnManejarSensores = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), ControlSensoresActivity.class);
            startActivity(intent);
        }
    };

}
