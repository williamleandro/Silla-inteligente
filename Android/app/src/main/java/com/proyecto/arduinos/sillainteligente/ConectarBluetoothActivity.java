package com.proyecto.arduinos.sillainteligente;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.beardedhen.androidbootstrap.BootstrapButton;
import com.proyecto.arduinos.sillainteligente.adaptadores.DeviceListAdapter;

import java.util.ArrayList;
import java.util.Set;

import dmax.dialog.SpotsDialog;

public class ConectarBluetoothActivity extends AppCompatActivity {
    private BootstrapButton btnActivarBT;
    private BootstrapButton btnBuscarDispositivosBT;
    private BootstrapButton btnEmparejadosBT;
    private BluetoothAdapter adaptadorBT;
    private Set<BluetoothDevice> setVinculados;
    int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private Activity activity;
    private AlertDialog mProgressDlg;

    private static final String TAG = "Bluetooth Activity";

    /** ATRIBUTOS LIST VIEW **/
    public ArrayList<BluetoothDevice> arrayDispositivos = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conectar_bluetooth);

        this.activity = this;

        this.mProgressDlg = new SpotsDialog(ConectarBluetoothActivity.this, R.style.Custom);
        this.mProgressDlg.setCancelable(false);

        this.btnActivarBT = findViewById(R.id.btnHabilitar);
        this.btnActivarBT.setOnClickListener(btnActivarBluetooth);

        this.btnBuscarDispositivosBT = findViewById(R.id.btnDispositivos);
        this.btnBuscarDispositivosBT.setOnClickListener(btnBuscarBluetooth);

        this.btnEmparejadosBT = findViewById(R.id.btnEmparejados);
        this.btnEmparejadosBT.setOnClickListener(btnEmparejarBluetooth);

        this.arrayDispositivos = new ArrayList<>();

        this.btnBuscarDispositivosBT.setEnabled(false);
        this.btnEmparejadosBT.setEnabled(false);

        this.adaptadorBT = BluetoothAdapter.getDefaultAdapter();

        if(this.adaptadorBT == null) {
            return;
        }

        if (this.adaptadorBT.isEnabled()) {
            btnActivarBT.setText("Desactivar Bluetooth");
            btnBuscarDispositivosBT.setEnabled(true);
            btnEmparejadosBT.setEnabled(true);
        }

        //  Se declara IntentFilter para registrar los Eventos del Broadcast Receiver
        IntentFilter filterBusqueda = new IntentFilter();
        filterBusqueda.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filterBusqueda.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filterBusqueda.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastReceiver, filterBusqueda);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        this.arrayDispositivos = null;
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(adaptadorBT != null) {
            if(adaptadorBT.isDiscovering()) {
                adaptadorBT.cancelDiscovery();
            }
        }
    }

    private View.OnClickListener btnActivarBluetooth = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (adaptadorBT.isEnabled()) {
                adaptadorBT.disable();
                btnActivarBT.setText("Activar Bluetooth");

                btnBuscarDispositivosBT.setEnabled(false);
                btnEmparejadosBT.setEnabled(false);
            } else {
                // Intent para lanzar Actividad
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 1000);

                btnActivarBT.setText("Desactivar Bluetooth");
                btnBuscarDispositivosBT.setEnabled(true);
                btnEmparejadosBT.setEnabled(true);
            }
        }
    };

    private View.OnClickListener btnBuscarBluetooth = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                if (adaptadorBT.isDiscovering()) {
                    Log.d("BT", "Buscando Dispositivo");
                } else {
                    if (adaptadorBT.startDiscovery()) {
                        Log.d("BT", "Iniciando Busqueda");
                    } else {
                        Log.d("BT", "Error en Busqueda");
                    }
                }
            }
    };

    //  Carga los dispositivos ya emparejados y los envia a la activity que los lista.
    private View.OnClickListener btnEmparejarBluetooth = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Set<BluetoothDevice> pairedDevices = adaptadorBT.getBondedDevices();

            if (pairedDevices != null || pairedDevices.size() > 0) {
                ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();

                list.addAll(pairedDevices);

                Intent intent = new Intent(ConectarBluetoothActivity.this, DeviceListActivity.class);
                intent.putParcelableArrayListExtra("listaDevice", list);
                startActivityForResult(intent, 2);
            }
        }
    };

    //  Broadcast Receiver que obtengo los eventos de Bluetooth
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String accion = intent.getAction();

                switch (accion) {
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        mProgressDlg.show();
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        mProgressDlg.dismiss();
                        Intent newIntent = new Intent(ConectarBluetoothActivity.this, DeviceListActivity.class);
                        newIntent.putParcelableArrayListExtra("listaDevice", arrayDispositivos);
                        startActivityForResult(newIntent, 2);
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        BluetoothDevice dispositivo = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        arrayDispositivos.add(dispositivo);
                        break;
                }
            }
        };
}
