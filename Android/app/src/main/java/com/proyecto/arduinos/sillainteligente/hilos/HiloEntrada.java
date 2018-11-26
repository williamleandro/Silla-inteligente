package com.proyecto.arduinos.sillainteligente.hilos;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.proyecto.arduinos.sillainteligente.utilitarios.Constante;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HiloEntrada extends Thread {
    private InputStream flujoEntrada = null;
    private static final String LOGTAG = "LogsAndroid";
    private boolean seConcatena = false;
    private boolean esPrimerMensaje = false;
    private boolean cadenaFormada = false;
    private Handler miHandler = null;
    private List<String> listaCodigo = new ArrayList<>();
    private String subcadena = null;
    private int bytes = 0;
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
        byte[] buffer = new byte[52];
        String readMessage = null;
        String auxiliarCadena = null;
        String mensaje = null;

        while (true) {
            try {
                bytes = this.flujoEntrada.read(buffer);
                readMessage = new String(buffer, 0, bytes);
                mensaje = new String(readMessage);

                if(mensaje.length() == 1 && !seConcatena) {
                    auxiliarCadena = new String(mensaje);
                    seConcatena = true;
                }

                if (seConcatena && mensaje.length() > 1) {
                    auxiliarCadena = auxiliarCadena.concat(mensaje);

                    if(auxiliarCadena.contains("#") && auxiliarCadena.contains("@")) {
                        mensaje = auxiliarCadena;
                        auxiliarCadena = null;
                        seConcatena = false;
                    }
                }

                if(mensaje.contains("#") && mensaje.contains("@")) {
                    subcadena = obtenerSubcadena(mensaje);
                    cadenaFormada = subcadena!=null? true:false;
                }

                if(cadenaFormada && subcadena.length() == 24) {
                    this.listaCodigo = getCodigo(subcadena);

                    for (String codigo: listaCodigo) {
                        switch (codigo) {
                            case Constante.COD_TEMP:
                                miHandler.obtainMessage(Constante.CODIGO_MENSAJE_TEMPERATURA, codigo.length()+1, -1, subcadena.substring(4, 8)).sendToTarget();
                                break;
                            case  Constante.COD_HUM:
                                miHandler.obtainMessage(Constante.CODIGO_MENSAJE_HUMEDAD, codigo.length()+1, -1, subcadena.substring(12, 16)).sendToTarget();
                                break;
                            case Constante.COD_LUZ:
                                miHandler.obtainMessage(Constante.CODIGO_MENSAJE_LUMINOSIDAD, codigo.length()+1, -1, subcadena.substring(8, 12)).sendToTarget();
                                break;
                            case Constante.COD_US:
                                miHandler.obtainMessage(Constante.CODIGO_MENSAJE_DISTANCIA, codigo.length()+1, -1, subcadena.substring(0, 4)).sendToTarget();
                                break;
                            case Constante.COD_EST_LED:
                                miHandler.obtainMessage(Constante.CODIGO_MENSAJE_ESTADO_LED, codigo.length()+1, -1, subcadena.substring(20, 24)).sendToTarget();
                                break;
                            case Constante.COD_EST_PUL:
                                miHandler.obtainMessage(Constante.CODIGO_MENSAJE_ESTADO_PULSADOR, codigo.length()+1, -1, subcadena.substring(16, 20)).sendToTarget();
                                break;
                        }
                    }
                }
            } catch (IOException e) {
                break;
            }
        }
    }

    private String obtenerSubcadena(String mensaje) {
        int start = mensaje.indexOf("#");
        int end = mensaje.indexOf("@");

        return start<end? mensaje.substring(start+1, end):null;
    }

    private ArrayList<String> getCodigo(String mensaje) {
        ArrayList<String> auxiliar = new ArrayList<>();
        auxiliar.add(mensaje.substring(0, 3));
        auxiliar.add(mensaje.substring(4, 7));
        auxiliar.add(mensaje.substring(8, 11));
        auxiliar.add(mensaje.substring(12, 15));
        auxiliar.add(mensaje.substring(16, 19));
        auxiliar.add(mensaje.substring(20, 23));

        return auxiliar;
    }
}
