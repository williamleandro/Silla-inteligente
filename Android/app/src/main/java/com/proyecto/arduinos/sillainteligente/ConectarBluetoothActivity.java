package com.proyecto.arduinos.sillainteligente;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import com.beardedhen.androidbootstrap.BootstrapButton;

import java.util.ArrayList;
import java.util.Set;

import dmax.dialog.SpotsDialog;

import static dmax.dialog.SpotsDialog.*;

public class ConectarBluetoothActivity extends AppCompatActivity {
    private BootstrapButton btnActivarBT;
    private BootstrapButton btnBuscarDispositivosBT;
    private BootstrapButton btnEmparejadosBT;
    private BluetoothAdapter adaptadorBT;
    private Set<BluetoothDevice> setVinculados;
    int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private Activity activity;
    // private ProgressDialog mProgressDlg;
    private AlertDialog mProgressDlg;
    private String direccionMAC;

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
        this.mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", btnCancelarDialogListener);

        /*
        this.mProgressDlg = new ProgressDialog(this);
        this.mProgressDlg.setMessage("Buscando dispositivos...");
        this.mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", btnCancelarDialogListener);
        */

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
            Toast.makeText(getApplicationContext(), "El dispositivo no cuenta con Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        if (this.adaptadorBT.isEnabled()) {
            btnActivarBT.setText("Desactivar Bluetooth");
            btnBuscarDispositivosBT.setEnabled(true);
            btnEmparejadosBT.setEnabled(true);
        }

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
                    Toast.makeText(getApplicationContext(),
                            "El dispositivo ya se encuentra en búsqueda.", Toast.LENGTH_SHORT).show();
                } else {
                    if (adaptadorBT.startDiscovery()) {
                        //Toast.makeText(getApplicationContext(),
                           //     "Buscando dispositivos.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Error en la búsqueda.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    };

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

    private DialogInterface.OnClickListener btnCancelarDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            adaptadorBT.cancelDiscovery();
        }
    };
}
