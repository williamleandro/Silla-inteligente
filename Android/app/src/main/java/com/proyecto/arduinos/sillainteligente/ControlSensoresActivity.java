package com.proyecto.arduinos.sillainteligente;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ControlSensoresActivity extends AppCompatActivity implements SensorEventListener {
    /****** INICIO ATRIBUTOS BLUETOOTH ******/
    private BluetoothDevice dispositivo;
    private BluetoothAdapter adaptador;
    private BluetoothSocket socketBT;
    private String direccionMAC;
    /**************************************/

    /****** INICIO ATRIBUTOS SENSOR ******/
    private Sensor sensorAcelerometro;
    private Sensor sensorGiroscopio;
    private Sensor sensorProximidad;
    private SensorManager sensorManager;
    /***********************************/

    /****** INICIO ATRIBUTOS TEXTVIEW ******/
    private TextView tvTemperatura;
    private TextView tvHumedad;
    private TextView tvDistancia;
    private TextView tvLumninosidad;
    /*************************************/

    private StringBuilder recDataString = new StringBuilder();

    /****** INICIO ATRIBUTOS CONSTANTES ******/
    private static final String SEPARADOR_SPLIT = "_";
    private static final int CODIGO_MENSAJE_TEMPERATURA = 10;
    private static final int CODIGO_MENSAJE_HUMEDAD = 11;
    private static final int CODIGO_MENSAJE_LUMINOSIDAD = 12;
    private static final int CODIGO_MENSAJE_DISTANCIA = 13;
    /***************************************/

    /****** INICIO ATRIBUTOS COMUNICACION ******/
    private Handler bluetoothIn;
    private HiloEntrada hiloEntrada;
    private HiloSalida hiloSalida;
    /***************************************/

    /****** INICIO ATRIBUTOS ACELEROMETRO ******/
    private float curX = 0, curY = 0, curZ = 0;
    private float gravedadX;
    private float gravedadY;
    private float gravedadZ;
    private float aceleracionValor;
    private float aceleracionAnterior;
    private float shake;
    /****************************************/

    /****** INICIO ATRIBUTOS GIROSCOPIO ******/
    private static final float ROTATION_THRESHOLD = 2.0f;
    private static final int ROTATION_WAIT_TIME_MS = 100;
    private long mRotationTime = 0;
    private float giroscopioX;
    private float giroscopioY;
    private float giroscopioZ;
    /****************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_sensores);

        this.tvTemperatura = findViewById(R.id.tvTemperaturaInfo);
        this.tvHumedad = findViewById(R.id.tvHumedadInfo);
        this.tvDistancia = findViewById(R.id.tvProximidadInfo);
        this.tvLumninosidad = findViewById(R.id.tvLuminosidadInfo);

        this.bluetoothIn = HandlerMsg();

        this.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        this.adaptador = BluetoothAdapter.getDefaultAdapter();

        this.sensorAcelerometro = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.sensorGiroscopio = this.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        this.sensorProximidad = this.sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        this.aceleracionAnterior = SensorManager.GRAVITY_EARTH;
        this.aceleracionValor = SensorManager.GRAVITY_EARTH;
        this.shake = 0.00f;
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

        this.hiloEntrada = new HiloEntrada(this.socketBT);
        this.hiloSalida = new HiloSalida(this.socketBT);

        hiloEntrada.start();
        hiloSalida.start();

        this.sensorManager.registerListener(this, this.sensorAcelerometro, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this, this.sensorGiroscopio, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this, this.sensorProximidad, SensorManager.SENSOR_DELAY_NORMAL);
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
                        tvTemperatura.setText(dataInPrint);
                    }
                }

                if(msg.what == CODIGO_MENSAJE_HUMEDAD) {
                    if(posicionFinalMensaje > 0) {
                        String dataInPrint = recDataString.substring(0, posicionFinalMensaje);
                        tvHumedad.setText(dataInPrint);
                    }
                }

                if(msg.what == CODIGO_MENSAJE_LUMINOSIDAD) {
                    if(posicionFinalMensaje > 0) {
                        String dataInPrint = recDataString.substring(0, posicionFinalMensaje);
                        tvLumninosidad.setText(dataInPrint);
                    }
                }

                if(msg.what == CODIGO_MENSAJE_DISTANCIA) {
                    if(posicionFinalMensaje > 0) {
                        String dataInPrint = recDataString.substring(0, posicionFinalMensaje);
                        tvDistancia.setText(dataInPrint);
                    }
                }
                recDataString.delete(0, posicionFinalMensaje);
            }
        };
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                this.curX = event.values[0];
                this.curY = event.values[1];
                this.curZ = event.values[2];

                detectShake(event);
            }

            if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                this.giroscopioX = event.values[0];
                this.giroscopioY = event.values[1];
                this.giroscopioZ = event.values[2];

                detectaRotacion(event);
            }
        }

    }

    private void detectaRotacion(SensorEvent event) {
        long now = System.currentTimeMillis();

        if ((now - mRotationTime) > ROTATION_WAIT_TIME_MS) {
            mRotationTime = now;

            if (Math.abs(event.values[0]) > ROTATION_THRESHOLD ||
                    Math.abs(event.values[1]) > ROTATION_THRESHOLD ||
                    Math.abs(event.values[2]) > ROTATION_THRESHOLD) {
                this.hiloSalida.enviarMensaje(2);
            }
        }
    }

    private void detectShake(SensorEvent event) {
        float delta = 0.00f;
        this.gravedadX = event.values[0];
        this.gravedadY = event.values[1];
        this.gravedadZ = event.values[2];

        this.aceleracionAnterior = this.aceleracionValor;
        this.aceleracionValor = (float) Math.sqrt((double)(gravedadX * gravedadX + gravedadY * gravedadY + gravedadZ * gravedadZ));
        delta = this.aceleracionValor - this.aceleracionAnterior;
        this.shake = this.shake * 0.9f + delta;

        if(this.shake > 12) {
            this.hiloSalida.enviarMensaje(1);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        this.sensorManager.unregisterListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.sensorManager.unregisterListener(this);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    private class HiloEntrada extends Thread {
        private final InputStream flujoEntrada;

        public HiloEntrada (BluetoothSocket socket) {
            InputStream flujoINTemporal = null;

            try {
                flujoINTemporal = socket.getInputStream();
            } catch (IOException e) { }

            this.flujoEntrada = flujoINTemporal;
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
                            bluetoothIn.obtainMessage(CODIGO_MENSAJE_LUMINOSIDAD, bytes, -1, mensaje).sendToTarget();
                            break;
                        case "DIST":
                            bluetoothIn.obtainMessage(CODIGO_MENSAJE_DISTANCIA, bytes, -1, mensaje).sendToTarget();
                            break;
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private class HiloSalida extends Thread {
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