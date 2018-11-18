package com.proyecto.arduinos.sillainteligente;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.proyecto.arduinos.sillainteligente.adaptadores.ViewPagerAdapter;
import com.proyecto.arduinos.sillainteligente.fragments.CoolerFragment;
import com.proyecto.arduinos.sillainteligente.fragments.LEDFragment;
import com.proyecto.arduinos.sillainteligente.hilos.HiloEntrada;
import com.proyecto.arduinos.sillainteligente.hilos.HiloSalida;

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
    private Sensor sensorLuminosidad;
    private SensorManager sensorManager;
    /***********************************/

    /****** INICIO ATRIBUTOS VIEW ******/
    private TextView tvTemperatura;
    private TextView tvHumedad;
    private TextView tvDistancia;
    private TextView tvLumninosidad;
    private TabLayout tabs;
    private ViewPager viewPager;
    private ViewPagerAdapter adaptadorVPA;
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
    private Handler bluetoothHandler;
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

    /****** INICIO ATRIBUTOS LUMINOSIDAD******/
    private float valorLumninosidad;
    /****************************************/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_sensores);

        // Vinculación textView de la vista y los Objetos.
        this.tvTemperatura = findViewById(R.id.tvTemperaturaInfo);
        this.tvHumedad = findViewById(R.id.tvHumedadInfo);
        this.tvDistancia = findViewById(R.id.tvProximidadInfo);
        this.tvLumninosidad = findViewById(R.id.tvLuminosidadInfo);

        this.tabs = findViewById(R.id.tabLayout);
        this.viewPager = findViewById(R.id.pageView);
        
        crearAdaptador();
        
        this.tabs.setupWithViewPager(this.viewPager);

        // Sensor Manager
        this.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        this.adaptador = BluetoothAdapter.getDefaultAdapter();

        // Declaracion de sensores
        this.sensorAcelerometro = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.sensorGiroscopio = this.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        this.sensorProximidad = this.sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        this.sensorLuminosidad = this.sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        //Inicialización atributos auxiliares
        this.aceleracionAnterior = SensorManager.GRAVITY_EARTH;
        this.aceleracionValor = SensorManager.GRAVITY_EARTH;
        this.shake = 0.00f;

        this.bluetoothHandler = HandlerMsg(); //Handler
    }

    private void crearAdaptador() {
        adaptadorVPA = new ViewPagerAdapter(getSupportFragmentManager());

        adaptadorVPA.addFragment(new LEDFragment(), "LED");
        adaptadorVPA.addFragment(new CoolerFragment(), "Cooler");

        this.viewPager.setAdapter(adaptadorVPA);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Obtengo mediante un Intent la dirección del dispositivo.
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        this.direccionMAC = data!=null? data.getString("direccionMAC"):null;

        //Obtiene el dispositivo a partir de la direccion MAC
        this.dispositivo = adaptador.getRemoteDevice(this.direccionMAC);

        try {
            this.socketBT = crearSocketBluetooth(this.dispositivo); // Creo Socket
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.socketBT.connect();    //Conecto Socket
        } catch (IOException e) {
            try {
                this.socketBT.close();  //Cierro Socket
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        // Creo Hilos de entrada y salida.
        this.hiloEntrada = new HiloEntrada(this.socketBT, bluetoothHandler);
        this.hiloSalida = new HiloSalida(this.socketBT);

        // Inicio la ejecución de hilos.
        hiloEntrada.start();
        hiloSalida.start();

        adaptadorVPA.setThreadFragment(hiloSalida);

        // Registro de escucha de los sensores.
        this.sensorManager.registerListener(this, this.sensorAcelerometro, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this, this.sensorGiroscopio, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this, this.sensorProximidad, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this, this.sensorLuminosidad, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //Crea el socket basado en el UUID de la maquina a conectar.
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
                int posicionFinalMensaje = recDataString.indexOf("\n");

                if(msg.what == CODIGO_MENSAJE_TEMPERATURA) {
                    if(posicionFinalMensaje > 0) {
                        String dataInPrint = recDataString.substring(0, posicionFinalMensaje);
                        Log.d("HANDLER", dataInPrint);
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
                recDataString.delete(0, recDataString.length());
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

            if(event.sensor.getType() == Sensor.TYPE_LIGHT) {
                this.valorLumninosidad = event.values[0];

                if(this.valorLumninosidad == 0.00) {
                    //notificacion();
                }
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
                //this.hiloSalida.enviarMensaje("Rotation.");
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

        if(this.shake > 70) {
            this.hiloSalida.enviarMensaje("MLU1." + '\n');
            Log.d("SHAKE", "MLU1.");
        }
    }

    private void notificacion() {
        NotificationCompat.Builder notif = new NotificationCompat.Builder(this).
                            setSmallIcon(R.drawable.lightbulb_on_outline).
                            setLargeIcon(((BitmapDrawable)getResources().getDrawable(R.drawable.lightbulb_on_outline)).
                                getBitmap()).setContentTitle("Aviso - Luz").
                            setContentText("Se ha detectado poca luminosidad, encienda la luz de la silla.").
                            setContentInfo("2").setTicker("Silla Inteligente - Poca Luz");

        Intent intentNotification = new Intent(this, ControlSensoresActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(ControlSensoresActivity.this,
                                                        0, intentNotification, 0);

        notif.setContentIntent(pendingIntent);
        NotificationManager managerNotification = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        managerNotification.notify(1 ,notif.build());
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        this.sensorManager.unregisterListener(this);

        try {
            this.socketBT.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.sensorManager.unregisterListener(this);

        try {
            this.socketBT.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
    //////////////////////////////////////////////////////////////////////////////////////////////
    /*
    public class HiloEntrada extends Thread {
        private InputStream flujoEntrada = null;
        private static final String LOGTAG = "LogsAndroid";
        private boolean esPrimerCaracter = true;
        private Handler miHandler = null;

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

                } catch (IOException e) {
                    break;
                }
            }
        }
    }
    */
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /*
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
                finish();
            }
        }
    }
    */
    ///////////////////////////////////////////////////////////////////////////////////////////////
