package com.proyecto.arduinos.sillainteligente.hilos;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class HiloEntrada extends Thread {
    private InputStream flujoEntrada = null;
    private static final String LOGTAG = "LogsAndroid";
    private boolean esPrimerCaracter = true;
    private Handler miHandler = null;

    /****** INICIO ATRIBUTOS CONSTANTES ******/
    private static final String SEPARADOR_SPLIT = "_";
    private static final int CODIGO_MENSAJE_TEMPERATURA = 10;
    private static final int CODIGO_MENSAJE_HUMEDAD = 11;
    private static final int CODIGO_MENSAJE_LUMINOSIDAD = 12;
    private static final int CODIGO_MENSAJE_DISTANCIA = 13;
    /***************************************/


    public HiloEntrada (BluetoothSocket socket, Handler miHandler) {
        InputStream flujoINTemporal = null;

        try {
            flujoINTemporal = socket.getInputStream();
            this.miHandler = miHandler;
        } catch (IOException e) { }

        this.flujoEntrada = flujoINTemporal;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[256];
        int bytes;
        String readMessage = null;
        String auxiliarMensaje = null;

        while (true) {
            try {
                if(esPrimerCaracter) {
                    bytes = this.flujoEntrada.read(buffer);
                    readMessage = new String(buffer, 0, bytes);
                    esPrimerCaracter = false;
                } else {
                    bytes = this.flujoEntrada.read(buffer);
                    auxiliarMensaje = new String(buffer, 0, bytes);
                    readMessage += auxiliarMensaje;
                    readMessage+='\n';
                    Log.d(LOGTAG, readMessage);
                    miHandler.obtainMessage(CODIGO_MENSAJE_TEMPERATURA,bytes, -1, readMessage).sendToTarget();
                    esPrimerCaracter = true;
                }

                    /*
                    switch (codigo) {
                        case "TEMP":
                            bluetoothHandler.obtainMessage(CODIGO_MENSAJE_TEMPERATURA, bytes, -1, mensaje).sendToTarget();
                            break;
                        case  "HUM":
                            bluetoothHandler.obtainMessage(CODIGO_MENSAJE_HUMEDAD, bytes, -1, mensaje).sendToTarget();
                            break;
                        case "LED":
                            bluetoothHandler.obtainMessage(CODIGO_MENSAJE_LUMINOSIDAD, bytes, -1, mensaje).sendToTarget();
                            break;
                        case "DIST":
                            bluetoothHandler.obtainMessage(CODIGO_MENSAJE_DISTANCIA, bytes, -1, mensaje).sendToTarget();
                            break;
                    }
                    */
            } catch (IOException e) {
                break;
            }
        }
    }
}
