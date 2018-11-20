package com.proyecto.arduinos.sillainteligente.hilos;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.proyecto.arduinos.sillainteligente.utilitarios.Constante;

import java.io.IOException;
import java.io.InputStream;

public class HiloEntrada extends Thread {
    private InputStream flujoEntrada = null;
    private static final String LOGTAG = "LogsAndroid";
    private boolean esPrimerCaracter = true;
    private Handler miHandler = null;
    private String codigo;
    /****** INICIO ATRIBUTOS CONSTANTES ******/

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

                    codigo = getCodigo(readMessage);

                    switch (codigo) {
                        case Constante.COD_TEMP:
                            miHandler.obtainMessage(Constante.CODIGO_MENSAJE_TEMPERATURA, bytes, -1, readMessage).sendToTarget();
                            break;
                        case  Constante.COD_POT:
                            miHandler.obtainMessage(Constante.CODIGO_MENSAJE_POTENCIOMETRO, bytes, -1, readMessage).sendToTarget();
                            break;
                        case Constante.COD_LUZ:
                            miHandler.obtainMessage(Constante.CODIGO_MENSAJE_LUMINOSIDAD, bytes, -1, readMessage).sendToTarget();
                            break;
                        case Constante.COD_US:
                            miHandler.obtainMessage(Constante.CODIGO_MENSAJE_DISTANCIA, bytes, -1, readMessage).sendToTarget();
                            break;
                    }
                    //Log.d(LOGTAG, readMessage);
                    esPrimerCaracter = true;
                }
            } catch (IOException e) {
                break;
            }
        }
    }

    private String getCodigo(String mensaje) {
        return mensaje.substring(0, 2);
    }
}
