package com.proyecto.arduinos.sillainteligente;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.proyecto.arduinos.sillainteligente.adaptadores.ViewPagerAdapter;
import com.proyecto.arduinos.sillainteligente.hilos.HiloAlarma;
import com.proyecto.arduinos.sillainteligente.utilitarios.AlarmReceiver;
import com.proyecto.arduinos.sillainteligente.utilitarios.Constante;
import com.proyecto.arduinos.sillainteligente.fragments.CoolerFragment;
import com.proyecto.arduinos.sillainteligente.fragments.LEDFragment;
import com.proyecto.arduinos.sillainteligente.hilos.HiloEntrada;
import com.proyecto.arduinos.sillainteligente.hilos.HiloSalida;

import java.io.IOException;
import java.util.GregorianCalendar;
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
    private Sensor sensorContadorPasos;
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
    private NotificationManagerCompat notificationManagerCompat;
    private static final String CHANNEL_NOTIF_LED_ID = "Led_Uno";
    private static final String CHANNEL_NOTIF_LED_ID_2 = "Led_Dos";

    /****** INICIO ATRIBUTOS COMUNICACION ******/
    private Handler bluetoothHandler;
    private HiloEntrada hiloEntrada;
    private HiloSalida hiloSalida;
    private HiloAlarma hiloAlarma;
    /***************************************/

    /****** INICIO ATRIBUTOS ACELEROMETRO ******/
    private float gravedadX;
    private float gravedadY;
    private float gravedadZ;
    private float aceleracionValor;
    private float aceleracionAnterior;
    private float shake;
    /****************************************/

    /****** INICIO ATRIBUTOS GIROSCOPIO ******/
    private static final float ROTATION_THRESHOLD = 8.6f;
    private static final int ROTATION_WAIT_TIME_MS = 100;
    private long mRotationTime = 0;
    /****************************************/

    /****** INICIO ATRIBUTOS LUMINOSIDAD******/
    private float valorLumninosidad;
    private boolean estadoLED = false;
    /****************************************/

    /****** ATRIBUTOS PASOS/PULSADOR/ALARMA *****/
    public static boolean estadoPulsador = false;
    public static boolean primerPulso = true;
    private static final int INTERVALO_TIME = 1000*15; //DEFINO 15 segundos
    private static final int ALARM_REQUEST_CODE = 133; //Alarm Request Code
    private PendingIntent pendingIntent;
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
        this.sensorContadorPasos = this.sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        this.sensorLuminosidad = this.sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        //Inicialización atributos auxiliares
        this.aceleracionAnterior = SensorManager.GRAVITY_EARTH;
        this.aceleracionValor = SensorManager.GRAVITY_EARTH;
        this.shake = 0.00f;

        this.bluetoothHandler = HandlerMsg(); //Handler

        this.notificationManagerCompat = NotificationManagerCompat.from(this);
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
        this.hiloAlarma = new HiloAlarma(this.socketBT, bluetoothHandler);

        // Inicio la ejecución de hilos.
        hiloEntrada.start();
        hiloSalida.start();

        adaptadorVPA.setThreadFragment(hiloSalida);

        // Registro de escucha de los sensores.
        this.sensorManager.registerListener(this, this.sensorAcelerometro, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this, this.sensorGiroscopio, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this, this.sensorContadorPasos, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this, this.sensorLuminosidad, SensorManager.SENSOR_DELAY_NORMAL);

    }

    //Crea el socket basado en el UUID de la maquina a conectar.
    private BluetoothSocket crearSocketBluetooth(BluetoothDevice dispositivo) throws IOException {
        UUID uuid = this.dispositivo.getUuids()[0].getUuid();
        return dispositivo.createRfcommSocketToServiceRecord(uuid);
    }


    //HANDLER QUE ESCUCHA LOS MENSAJES ENVIADOS A LA COLA DE MENSAJES DESDE EL THREAD DE ENTRADA
    private Handler HandlerMsg() {
        return new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String mensajeIN = (String) msg.obj;
                recDataString.append(mensajeIN);
                int posicionFinalMensaje = msg.arg1;

                if(msg.what == Constante.CODIGO_MENSAJE_TEMPERATURA) {
                    if(posicionFinalMensaje > 0) {
                        String dataInPrint = recDataString.substring(0, posicionFinalMensaje);
                        tvTemperatura.setText(dataInPrint);

                        if(dataInPrint.equals(Constante.SEÑAL_TEMP_ARD_G)) {
                            tvTemperatura.setText(Constante.TEMP_G);
                            tvTemperatura.setTextColor(Color.GREEN);
                        }

                        if(dataInPrint.equals(Constante.SEÑAL_TEMP_ARD_R)) {
                            tvTemperatura.setText(Constante.TEMP_R);
                            tvTemperatura.setTextColor(Color.YELLOW);
                        }

                        if(dataInPrint.equals(Constante.SEÑAL_TEMP_ARD_B)) {
                            tvTemperatura.setText(Constante.TEMP_B);
                            tvTemperatura.setTextColor(Color.RED);
                        }
                    }
                }

                if(msg.what == Constante.CODIGO_MENSAJE_HUMEDAD) {
                    if(posicionFinalMensaje > 0) {
                        String dataInPrint = recDataString.substring(0, posicionFinalMensaje);
                        tvHumedad.setText(dataInPrint);

                        if(dataInPrint.equals(Constante.SEÑAL_HUM_ARD_G)) {
                            tvHumedad.setText(Constante.HUM_G);
                            tvHumedad.setTextColor(Color.GREEN);
                        }

                        if(dataInPrint.equals(Constante.SEÑAL_HUM_ARD_R)) {
                            tvHumedad.setText(Constante.HUM_R);
                            tvHumedad.setTextColor(Color.YELLOW);
                        }

                        if(dataInPrint.equals(Constante.SEÑAL_HUM_ARD_B)) {
                            tvHumedad.setText(Constante.HUM_B);
                            tvHumedad.setTextColor(Color.RED);
                        }
                    }
                }

                if(msg.what == Constante.CODIGO_MENSAJE_LUMINOSIDAD) {
                    if(posicionFinalMensaje > 0) {
                        String dataInPrint = recDataString.substring(0, posicionFinalMensaje);

                        if(dataInPrint.equals(Constante.SEÑAL_LUZ_ARD_G)) {
                            tvLumninosidad.setText(Constante.LUZ_G);
                            tvLumninosidad.setTextColor(Color.GREEN);
                        }

                        if(dataInPrint.equals(Constante.SEÑAL_LUZ_ARD_R)) {
                            tvLumninosidad.setText(Constante.LUZ_R);
                            tvLumninosidad.setTextColor(Color.YELLOW);
                        }

                        if(dataInPrint.equals(Constante.SEÑAL_LUZ_ARD_B)) {
                            tvLumninosidad.setText(Constante.LUZ_B);
                            tvLumninosidad.setTextColor(Color.RED);
                        }
                    }
                }

                if(msg.what == Constante.CODIGO_MENSAJE_DISTANCIA) {
                    if(posicionFinalMensaje > 0) {
                        String dataInPrint = recDataString.substring(0, posicionFinalMensaje);

                        if(dataInPrint.equals(Constante.SEÑAL_US_G)) {
                            tvDistancia.setText(Constante.DIS_G);
                            tvDistancia.setTextColor(Color.GREEN);
                        }

                        if(dataInPrint.equals(Constante.SEÑAL_US_B)) {
                            tvDistancia.setText(Constante.DIS_B);
                            tvDistancia.setTextColor(Color.RED);
                        }
                    }
                }

                if(msg.what == Constante.CODIGO_MENSAJE_ESTADO_LED) {
                    if(posicionFinalMensaje > 0) {
                        String dataInPrint = recDataString.substring(0, posicionFinalMensaje);

                        if(dataInPrint.equals(Constante.SEÑAL_ESTLED_ARD_H)) {
                            estadoLED = true;
                            adaptadorVPA.setSenialLuminosidad(estadoLED);
                        }

                        if(dataInPrint.equals(Constante.SEÑAL_ESTLED_ARD_L)) {
                            estadoLED = false;
                            adaptadorVPA.setSenialLuminosidad(estadoLED);
                            verificarEstadoNotificacion();
                        }
                    }
                }

                if(msg.what == Constante.CODIGO_MENSAJE_ESTADO_PULSADOR) {
                    if(posicionFinalMensaje > 0) {
                        String dataInPrint = recDataString.substring(0, posicionFinalMensaje);

                        if(dataInPrint.equals(Constante.SEÑAL_ESTPUL_ARD_H)) {
                            estadoPulsador = true;

                            if(primerPulso) {
                                hiloAlarma.start();
                                primerPulso = false;
                            }
                        }

                        if(dataInPrint.equals(Constante.SEÑAL_ESTPUL_ARD_L)) {
                            estadoPulsador = false;
                            primerPulso = true;
                        }

                    }
                }

                if(msg.what == 20) {
                    if(estadoPulsador)
                        enviarACanalAlarm();
                }
                recDataString.delete(0, recDataString.length());
            }
        };
    }

    //VALIDO SI DISPARO LA NOTIFICACION
    private void verificarEstadoNotificacion() {
        if(this.valorLumninosidad < 1.5 && !estadoLED) {
            enviarACanalLED();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) { //SI CAMBIO EL VALOR DE ACELEROMETRO
                detectShake(event);
            }

            if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) { //SI CAMBIO EL VALOR DE GIROSCOPIO
                detectaRotacion(event);
            }

            if(event.sensor.getType() == Sensor.TYPE_LIGHT) { //SI CAMBIO EL VALOR DEL SENSOR DE LUMINOSIDAD
                this.valorLumninosidad = event.values[0];
                verificarEstadoNotificacion();
            }

            if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {  //SI CAMBIO EL VALOR DE CONTADOR DE PASOS
                   // this.contadorPasos++;
            }
        }
    }

    //DETECTO SI SE REALIZO MOVIMIENTO DE ROTACION CON EL DISPOSITIVO
    private void detectaRotacion(SensorEvent event) {
        long now = System.currentTimeMillis();

        if ((now - mRotationTime) > ROTATION_WAIT_TIME_MS) {
            mRotationTime = now;

            if (Math.abs(event.values[0]) > ROTATION_THRESHOLD ||
                    Math.abs(event.values[1]) > ROTATION_THRESHOLD ||
                    Math.abs(event.values[2]) > ROTATION_THRESHOLD) {
                this.hiloSalida.enviarMensaje(Constante.SEÑAL_COOLER_HIGH);
            }
        }
    }

    //DETECTO SI SE REALIZO MOVIMIENTO DE SHAKE CON EL DISPOSITIVO
    private void detectShake(SensorEvent event) {
        float delta = 0.00f;
        this.gravedadX = event.values[0];
        this.gravedadY = event.values[1];
        this.gravedadZ = event.values[2];

        this.aceleracionAnterior = this.aceleracionValor;
        this.aceleracionValor = (float) Math.sqrt((double)(gravedadX * gravedadX + gravedadY * gravedadY + gravedadZ * gravedadZ));
        delta = this.aceleracionValor - this.aceleracionAnterior;
        this.shake = this.shake * 0.9f + delta;

        if(this.shake > 65) {
            if(!estadoLED) {
                this.hiloSalida.enviarMensaje(Constante.SEÑAL_LUZ_HIGH);
                adaptadorVPA.setSenialLuminosidad(true);

            } else {
                this.hiloSalida.enviarMensaje(Constante.SEÑAL_LUZ_LOW);
                adaptadorVPA.setSenialLuminosidad(false);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //METODO QUE LANZA LA NOTIFICACION EN LA BARRA DE ESTADO DE
    public void enviarACanalLED() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_NOTIF_LED_ID)
                .setSmallIcon(R.drawable.lightbulb_on_outline)
                .setContentTitle("Aviso - Luz")
                .setContentText("Se ha detectado poca luminosidad, encienda la luz de la silla.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE).build();

        notificationManagerCompat.notify(1, notification);
    }

    public void enviarACanalAlarm() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_NOTIF_LED_ID_2)
                .setSmallIcon(R.drawable.run)
                .setContentTitle("Aviso - Inactividad")
                .setContentText("Estuvo mucho tiempo inactivo.")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE).build();

        notificationManagerCompat.notify(1, notification);
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