package com.proyecto.arduinos.sillainteligente;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ControlSensoresActivity extends AppCompatActivity {
    private BluetoothDevice dispositivo;
    private BluetoothAdapter adaptador;
    private BluetoothSocket socketBT;
    private SensorManager sensorManager;
    private String direccionMAC;

    private Sensor acelerometro;
    private Sensor giroscopio;
    private Sensor proximidad;

    private StringBuilder recDataString = new StringBuilder();

    private static final String SEPARADOR_SPLIT = "_";
    private static final int CODIGO_MENSAJE_TEMPERATURA = 10;
    private static final int CODIGO_MENSAJE_HUMEDAD = 11;
    private static final int CODIGO_MENSAJE_LUMINOSIDAD = 12;

    private Handler bluetoothIn;

    private Hilo hiloEntrada;
    private Hilo hiloSalida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_sensores);

        this.bluetoothIn = HandlerMsg();

        this.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        this.adaptador = BluetoothAdapter.getDefaultAdapter();

        this.acelerometro = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.giroscopio = this.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        this.proximidad = this.sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        Bundle data = intent.getExtras();

        this.direccionMAC = data!=null? data.getString("direccionMAC"):null;
        this.dispositivo = adaptador.getRemoteDevice(this.direccionMAC);

        try {
            this.socketBT = crearSocketBluetooth(this.dispositivo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.socketBT.connect();
        } catch (IOException e) {
            try {
                this.socketBT.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        this.hiloEntrada = new Hilo(this.socketBT);
        this.hiloSalida = new Hilo(this.socketBT);

        hiloEntrada.start();
        hiloSalida.start();
    }

    private BluetoothSocket crearSocketBluetooth(BluetoothDevice dispositivo) throws IOException {
        UUID uuid = this.dispositivo.getUuids()[0].getUuid();
        return dispositivo.createRfcommSocketToServiceRecord(uuid);
    }

    private String obtenerCodigoMensaje(String readMessage) {
        String codigo = null;
        String[] arraySplit = null;

        arraySplit = readMessage.split(SEPARADOR_SPLIT);
        codigo = arraySplit[0];

        return codigo;
    }

    private String obtenerInformacionMensaje(String readMessage) {
        String codigo = null;
        String[] arraySplit = null;

        arraySplit = readMessage.split(SEPARADOR_SPLIT);
        codigo = arraySplit[1];

        return codigo;
    }

    private Handler HandlerMsg() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String mensajeIN = (String) msg.obj;
                recDataString.append(mensajeIN);
                int posicionFinalMensaje = recDataString.indexOf("\r\n");

                if(msg.what == CODIGO_MENSAJE_TEMPERATURA) {
                    if(posicionFinalMensaje > 0) {
                        String dataInPrint = recDataString.substring(0, posicionFinalMensaje);
                        //SETEAR TEXTVIEW DE TEMPERATURA
                    }
                }

                if(msg.what == CODIGO_MENSAJE_HUMEDAD) {
                    if(posicionFinalMensaje > 0) {
                        String dataInPrint = recDataString.substring(0, posicionFinalMensaje);
                        //SETEAR TEXTVIEW DE HUMEDAD
                    }
                }

                if(msg.what == CODIGO_MENSAJE_LUMINOSIDAD) {
                    if(posicionFinalMensaje > 0) {
                        String dataInPrint = recDataString.substring(0, posicionFinalMensaje);
                        //SETEAR TEXTVIEW DE LED
                    }
                }
                recDataString.delete(0, posicionFinalMensaje);
            }
        };
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    private class Hilo extends Thread {
        private final InputStream flujoEntrada;
        private final OutputStream flujoSalida;

        public Hilo(BluetoothSocket socket) {
            InputStream flujoINTemporal = null;
            OutputStream flujoOUTTemporal = null;

            try {
                flujoINTemporal = socket.getInputStream();
                flujoOUTTemporal = socket.getOutputStream();
            } catch (IOException e) { }

            this.flujoEntrada = flujoINTemporal;
            this.flujoSalida = flujoOUTTemporal;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            while (true) {
                try {
                    bytes = this.flujoEntrada.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    String codigo = obtenerCodigoMensaje(readMessage);
                    String mensaje = obtenerInformacionMensaje(readMessage);

                    switch (codigo) {
                        case "TEMP":
                            bluetoothIn.obtainMessage(CODIGO_MENSAJE_TEMPERATURA, bytes, -1, mensaje).sendToTarget();
                            break;
                        case  "HUM":
                            bluetoothIn.obtainMessage(CODIGO_MENSAJE_HUMEDAD, bytes, -1, mensaje).sendToTarget();
                            break;
                        case "LED":
                            bluetoothIn.obtainMessage(CODIGO_MENSAJE_HUMEDAD, bytes, -1, mensaje).sendToTarget();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void enviarMensaje(int i) {
            String mensaje = Integer.toString(i);
            mensaje += "\n";

            try {
                this.flujoSalida.write(mensaje.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                finish();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
}

