package com.proyecto.arduinos.sillainteligente;

import android.Manifest;
import android.app.Activity;
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
import android.util.Log;
import android.view.View;


import com.beardedhen.androidbootstrap.BootstrapButton;

import java.util.ArrayList;
import java.util.Set;

public class ConectarBluetoothActivity extends AppCompatActivity {
    private BootstrapButton btnActivarBT;
    private BootstrapButton btnBuscarDispositivosBT;
    private BootstrapButton btnEmparejadosBT;
    private BluetoothAdapter adaptadorBT;
    private Set<BluetoothDevice> setVinculados;
    int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private Activity activity;
    private ProgressDialog mProgressDlg;
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

        this.mProgressDlg = new ProgressDialog(this);
        this.mProgressDlg.setMessage("Buscando dispositivos...");
        this.mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", btnCancelarDialogListener);


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
            printLog("El dispositivo no cuenta con servicio Bluetooth.");
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
    protected void onResume() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        this.direccionMAC = extras!=null? extras.getString("MacBluetooth"):null;

        if(this.direccionMAC != null) {
            printLog(this.direccionMAC);
        }
        super.onResume();
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

    @Override
    protected void onStop() {
        Intent data = new Intent();
        data.putExtra("MacBluetooth", direccionMAC);

        if(getParent() == null) {
            setResult(Activity.RESULT_OK, data);
        } else {
            getParent().setResult(Activity.RESULT_OK, data);
        }

        super.onStop();
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
                    printLog("El dispositivo ya se encuentra en busqueda.");
                } else {
                    if (adaptadorBT.startDiscovery()) {
                        printLog("Buscando Dispositivos.");
                    } else {
                        printLog("Error al buscar un dispositivos.");
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
                        printLog("Busqueda Iniciada.");
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        mProgressDlg.dismiss();
                        printLog("Busqueda Finalizada.");
                        Intent newIntent = new Intent(ConectarBluetoothActivity.this, DeviceListActivity.class);
                        newIntent.putParcelableArrayListExtra("listaDevice", arrayDispositivos);
                        startActivityForResult(newIntent, 2);
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        BluetoothDevice dispositivo = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        printLog("Dispositivo Encontrado." + dispositivo.getName());
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

    private void printLog(String mensaje) {
        Log.d(this.TAG, mensaje);
    }

}
