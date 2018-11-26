package com.proyecto.arduinos.sillainteligente.hilos;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;

public class HiloAlarma extends Thread {
    private InputStream flujoEntrada = null;
    private Handler miHandler = null;

    public HiloAlarma(BluetoothSocket socket, Handler miHandler) {
        InputStream flujoINTemporal = null;

        try {
            flujoINTemporal = socket.getInputStream();
            this.miHandler = miHandler;
        } catch (IOException e) { }

        this.flujoEntrada = flujoINTemporal;
    }

    @Override
    public void run() {
       Long timeLimite = (System.currentTimeMillis()+(1000*10));

       while (System.currentTimeMillis() < timeLimite) {

       }

       miHandler.obtainMessage(20, 0, -1, null).sendToTarget();
    }
}
