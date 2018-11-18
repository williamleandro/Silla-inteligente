package com.proyecto.arduinos.sillainteligente.hilos;

import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.OutputStream;

public class HiloSalida extends Thread {
    private final OutputStream flujoSalida;

    public HiloSalida (BluetoothSocket socket) {
        OutputStream flujoOUTTemporal = null;

        try {
            flujoOUTTemporal = socket.getOutputStream();
        } catch (IOException e) { }

        this.flujoSalida = flujoOUTTemporal;
    }

    @Override
    public void run() {

        while (true) { }
    }

    public void enviarMensaje(String i) {
        String mensaje = i;
        mensaje += "\n";

        try {
            this.flujoSalida.write(mensaje.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
