package com.proyecto.arduinos.sillainteligente.utilitarios;

import java.util.UUID;

public class Constante {
    /***  CONSTANTES PARA CONEXION BLUETOOTH ***/
    public static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");


    /***  CONSTANTES DE MENSAJES QUE SE ENVIAN A ARDUINO  ***/
    public static final String SEÑAL_LUZ_LOW = "MLU0\n";
    public static final String SEÑAL_LUZ_HIGH = "MLU1\n";

    public static final String SEÑAL_SEEK_ARD = "PW";

    public static final String SEÑAL_COOLER_LOW = "MCO0\n";
    public static final String SEÑAL_COOLER_HIGH = "MCO1\n";

    public static final String SEÑAL_SOUND_LOW = "MSO0\n";
    public static final String SEÑAL_SOUND_HIGH = "MSO1\n";

    public static final String SEÑAL_TIME_ARD = "MTS1";

    /***  CONSTANTES DE MENSAJES QUE SE RECIBEN DESDE ARDUINO  ***/
    public static final String SEÑAL_TEMP_ARD_G = "ATE1";
    public static final String SEÑAL_TEMP_ARD_R = "ATE2";
    public static final String SEÑAL_TEMP_ARD_B = "ATE3";

    public static final String SEÑAL_HUM_ARD_G = "ATH1";
    public static final String SEÑAL_HUM_ARD_R = "ATH2";
    public static final String SEÑAL_HUM_ARD_B = "ATH3";

    public static final String SEÑAL_US_G = "AUS1";
    public static final String SEÑAL_US_B = "AUS2";

    public static final String SEÑAL_LUZ_ARD_G = "ALU1";
    public static final String SEÑAL_LUZ_ARD_R = "ALU2";
    public static final String SEÑAL_LUZ_ARD_B = "ALU3";

    public static final String SEÑAL_ESTPUL_ARD_H = "APU1";
    public static final String SEÑAL_ESTPUL_ARD_L = "APU0";

    public static final String SEÑAL_ESTLED_ARD_H = "ALE1";
    public static final String SEÑAL_ESTLED_ARD_L = "ALE0";

    public static final String COD_TEMP = "ATE";
    public static final String COD_US = "AUS";
    public static final String COD_LUZ = "ALU";
    public static final String COD_EST_PUL = "APU";
    public static final String COD_HUM = "ATH";
    public static final String COD_EST_LED= "ALE";

    /***  CONSTANTES DE MENSAJES AL HANDLER ***/
    public static final int CODIGO_MENSAJE_TEMPERATURA = 10;
    public static final int CODIGO_MENSAJE_HUMEDAD = 11;
    public static final int CODIGO_MENSAJE_LUMINOSIDAD = 12;
    public static final int CODIGO_MENSAJE_DISTANCIA = 13;
    public static final int CODIGO_MENSAJE_ESTADO_PULSADOR = 12;
    public static final int CODIGO_MENSAJE_ESTADO_LED = 13;

    /***  MENSAJES A PRINTEAR POR PANTALLA  ***/
    public static final String TEMP_G = "ALTA";
    public static final String TEMP_R = "MEDIA";
    public static final String TEMP_B = "BAJA";

    public static final String HUM_B = "ALTA";
    public static final String HUM_R = "MEDIA";
    public static final String HUM_G = "BAJA";

    public static final String DIS_B = "LEJOS";
    public static final String DIS_G = "CERCA";

    public static final String LUZ_G = "BUENA";
    public static final String LUZ_R = "REGULAR";
    public static final String LUZ_B = "POCA";
}
