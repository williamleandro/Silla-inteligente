package com.proyecto.arduinos.sillainteligente;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.proyecto.arduinos.sillainteligente.adaptadores.DeviceListAdapter;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class DeviceListActivity extends AppCompatActivity {
    private ListView mListView;
    private DeviceListAdapter mAdapter;
    private ArrayList<BluetoothDevice> mDeviceList;
    private int posicionListBluethoot;
    private String direccionMAC = null;

    private static final String TAG = "DeviceList Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        this.mListView = findViewById(R.id.lv_dispositivos);
        this.mDeviceList = getIntent().getExtras().getParcelableArrayList("listaDevice");

        this.mAdapter = new DeviceListAdapter(this);
        this.mAdapter.setData(mDeviceList);

        this.mListView.setAdapter(this.mAdapter);
        this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                emparejar(position);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mPairReceiver, filter);
    }

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {

                    Intent data = new Intent(DeviceListActivity.this, ControlSensoresActivity.class);
                    data.putExtra("direccionMAC", direccionMAC);
                    startActivity(data);

                } else {
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(mPairReceiver);
        this.mDeviceList = null;
        this.mAdapter = null;

        super.onDestroy();
    }

    public void emparejar(int posicion) {
        this.posicionListBluethoot = posicion;
        BluetoothDevice device = mDeviceList.get(posicionListBluethoot);
        this.direccionMAC = device.getAddress();

        if(device.getBondState() == BluetoothDevice.BOND_BONDED) {
            desemparejarDispositivo(device);
        } else {
            emparejarDispositivo(device);
        }

    }

    private void emparejarDispositivo(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void desemparejarDispositivo(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
